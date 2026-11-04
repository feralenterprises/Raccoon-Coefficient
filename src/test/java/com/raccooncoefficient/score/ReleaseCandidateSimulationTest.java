package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Repeatable source-only engineering simulation, not a RuneLite in-game test.
 *
 * Builds 2,048 structurally valid random loadouts from item identities with
 * known stat bonuses. Independently calculates score/swap expectations without
 * calling GearScoreCalculator.scoreBonus or score for the expected values.
 */
public class ReleaseCandidateSimulationTest
{
	private static final int[] SLOTS = {0, 1, 2, 3, 4, 5, 7, 9, 10, 12, 13};
	private static final long SEED = 2026110411L;
	private static final int SCENARIOS = 2048;

	private static final class Catalog
		implements ItemStatSource, SetEffectDetector.ItemNameSource
	{
		final Map<Integer, EquipmentBonus> bonuses = new HashMap<Integer, EquipmentBonus>();
		final Map<Integer, String> names = new HashMap<Integer, String>();
		int next = 100;

		int add(EquipmentBonus bonus)
		{
			int id = next++;
			bonuses.put(Integer.valueOf(id), bonus);
			names.put(Integer.valueOf(id), "Neutral test equipment " + id);
			return id;
		}
		@Override public EquipmentBonus getVerifiedBonus(int id) { return bonuses.get(Integer.valueOf(id)); }
		@Override public String nameOf(int id) { return names.get(Integer.valueOf(id)); }
	}

	private static EquipmentBonus randomBonus(Random rng, int slot, boolean twoHanded)
	{
		return new EquipmentBonus(slot, twoHanded,
			rng.nextInt(101)-50, rng.nextInt(101)-50, rng.nextInt(101)-50,
			rng.nextInt(101)-50, rng.nextInt(101)-50,
			rng.nextInt(101)-50, rng.nextInt(101)-50, rng.nextInt(101)-50,
			rng.nextInt(101)-50, rng.nextInt(101)-50,
			rng.nextInt(81)-20, rng.nextInt(71)-20,
			BigDecimal.valueOf(rng.nextInt(701)-150).movePointLeft(2),
			rng.nextInt(17)-6);
	}

	/** A separate direct implementation of the locked Wave 1 formula. */
	private static BigDecimal referenceItemScore(EquipmentBonus item)
	{
		long stats =
			(long)item.getAttackStab() + item.getAttackSlash() +
			item.getAttackCrush() + item.getAttackMagic() + item.getAttackRanged() +
			item.getDefenceStab() + item.getDefenceSlash() +
			item.getDefenceCrush() + item.getDefenceMagic() + item.getDefenceRanged();
		return BigDecimal.valueOf(stats).movePointLeft(1)
			.add(BigDecimal.valueOf(item.getMeleeStrength()))
			.add(BigDecimal.valueOf(item.getRangedStrength()))
			.add(item.getMagicDamagePercent())
			.add(BigDecimal.valueOf(item.getPrayer()).multiply(new BigDecimal("0.5")));
	}

	private static BigDecimal referenceLoadoutScore(List<WornItem> items, Catalog source)
	{
		BigDecimal total = BigDecimal.ZERO;
		for (WornItem item : items)
		{
			total = total.add(referenceItemScore(source.getVerifiedBonus(item.getItemId())));
		}
		return total.setScale(1, RoundingMode.HALF_UP);
	}

	private static List<WornItem> after(
		List<WornItem> before, int candidateId, EquipmentBonus candidate, Catalog source)
	{
		Set<Integer> removed = new HashSet<Integer>();
		removed.add(Integer.valueOf(candidate.getSlot()));
		if (candidate.getSlot() == EquipmentSlot.WEAPON && candidate.isTwoHanded())
		{
			removed.add(Integer.valueOf(EquipmentSlot.SHIELD));
		}
		if (candidate.getSlot() == EquipmentSlot.SHIELD)
		{
			for (WornItem item : before)
			{
				if (item.getSlotIndex() == EquipmentSlot.WEAPON
					&& source.getVerifiedBonus(item.getItemId()).isTwoHanded())
				{
					removed.add(Integer.valueOf(EquipmentSlot.WEAPON));
				}
			}
		}

		List<WornItem> result = new ArrayList<WornItem>();
		for (WornItem item : before)
		{
			if (!removed.contains(Integer.valueOf(item.getSlotIndex())))
			{
				result.add(item);
			}
		}
		result.add(new WornItem(candidate.getSlot(), candidateId));
		return result;
	}

	@Test
	public void twoThousandFortyEightDeterministicLoadoutsMatchAnIndependentOracle()
	{
		Random rng = new Random(SEED);
		GearScoreCalculator base = GearScoreCalculator.wave1();
		int positives = 0, negatives = 0, zeroes = 0, twoHanded = 0, shieldReplacements = 0;

		for (int scenario = 0; scenario < SCENARIOS; scenario++)
		{
			Catalog source = new Catalog();
			List<WornItem> current = new ArrayList<WornItem>();
			boolean existingTwoHanded = false;
			for (int slot : SLOTS)
			{
				if (rng.nextInt(5) == 0)
				{
					continue;
				}
				if (slot == EquipmentSlot.SHIELD && existingTwoHanded)
				{
					continue;
				}
				boolean isTwoHand = slot == EquipmentSlot.WEAPON && rng.nextInt(4) == 0;
				int id = source.add(randomBonus(rng, slot, isTwoHand));
				current.add(new WornItem(slot, id));
				if (isTwoHand)
				{
					existingTwoHanded = true;
				}
			}

			int slot = SLOTS[rng.nextInt(SLOTS.length)];
			boolean candidateTwoHanded = slot == EquipmentSlot.WEAPON && rng.nextBoolean();
			EquipmentBonus candidate = randomBonus(rng, slot, candidateTwoHanded);
			int candidateId = source.add(candidate);
			List<WornItem> replacement = after(current, candidateId, candidate, source);

			BigDecimal before = referenceLoadoutScore(current, source);
			BigDecimal expectedAfter = referenceLoadoutScore(replacement, source);
			BigDecimal expectedDelta = expectedAfter.subtract(before);
			BigDecimal expectedItem = referenceItemScore(candidate)
				.setScale(1, RoundingMode.HALF_UP);

			GearScoreBreakdown oldScore = base.score(current, source);
			GearScoreBreakdown newScore = base.score(replacement, source);
			assertTrue("old complete case=" + scenario, oldScore.isComplete());
			assertTrue("new complete case=" + scenario, newScore.isComplete());
			assertEquals("before case=" + scenario, before, oldScore.displayedTotal());
			assertEquals("after case=" + scenario, expectedAfter, newScore.displayedTotal());

			GearScoreComparison comparison = base.compare(candidateId, current, source);
			assertEquals("candidate case=" + scenario, expectedItem, comparison.getItemScore());
			assertEquals("delta case=" + scenario, expectedDelta, comparison.getDelta());
			assertNull(comparison.getBaseDelta());

			// The current production Wave 3 integration must agree exactly
			// when the synthetic gear has no catalogued set/weapon effects.
			GearScoreCalculator production = GearScoreCalculator.wave3(source);
			assertEquals("wave3 before case=" + scenario,
				before, production.score(current, source).displayedTotal());
			assertEquals("wave3 after case=" + scenario,
				expectedAfter, production.score(replacement, source).displayedTotal());
			assertEquals("wave3 delta case=" + scenario,
				expectedDelta, production.compare(candidateId, current, source).getDelta());

			if (expectedDelta.signum() > 0) positives++;
			else if (expectedDelta.signum() < 0) negatives++;
			else zeroes++;
			if (candidateTwoHanded) twoHanded++;
			if (slot == EquipmentSlot.SHIELD && existingTwoHanded) shieldReplacements++;
		}

		assertTrue("must hit both good and bad swaps", positives > 500 && negatives > 500);
		assertTrue("must cover two-handed replacement", twoHanded > 50);
		assertTrue("must cover shield replacing 2H", shieldReplacements > 10);
		assertTrue(positives + negatives + zeroes == SCENARIOS);
	}

	@Test
	public void missingEquipmentStatsDoNotBecomeCertifiedZero()
	{
		Catalog source = new Catalog();
		int weapon = source.add(randomBonus(new Random(10L), EquipmentSlot.WEAPON, false));
		List<WornItem> worn = Arrays.asList(
			new WornItem(EquipmentSlot.WEAPON, weapon),
			new WornItem(EquipmentSlot.HEAD, 999999));
		GearScoreBreakdown breakdown = GearScoreCalculator.wave3(source).score(worn, source);
		assertFalse(breakdown.isComplete());
		assertEquals(1, breakdown.getUnknownItems());
		assertTrue(GearScoreText.equipmentLine(breakdown).endsWith(" ?"));
	}

	@Test
	public void realisticSetAndWeaponEffectsKeepTheirTotalHonest()
	{
		Catalog source = new Catalog();
		int helm = source.add(randomBonus(new Random(20L), 0, false));
		int body = source.add(randomBonus(new Random(21L), 4, false));
		int legs = source.add(randomBonus(new Random(22L), 7, false));
		int gloves = source.add(randomBonus(new Random(23L), 9, false));
		source.names.put(helm, "Void ranger helm");
		source.names.put(body, "Void knight top");
		source.names.put(legs, "Void knight robe");
		source.names.put(gloves, "Void knight gloves");

		List<WornItem> set = Arrays.asList(new WornItem(0,helm),
			new WornItem(4,body),new WornItem(7,legs),new WornItem(9,gloves));
		assertTrue(GearScoreCalculator.wave1().score(set, source).isComplete());
		assertFalse("Void ranged contribution must remain unapproved",
			GearScoreCalculator.wave3(source).score(set, source).isComplete());

		int bowfa = source.add(randomBonus(new Random(24L), EquipmentSlot.WEAPON, true));
		source.names.put(bowfa, "Bow of faerdhinen (c)");
		int crystalBody = source.add(randomBonus(new Random(25L), EquipmentSlot.BODY, false));
		source.names.put(crystalBody, "Crystal body");
		List<WornItem> crystal = Arrays.asList(
			new WornItem(EquipmentSlot.WEAPON,bowfa),
			new WornItem(EquipmentSlot.BODY,crystalBody));
		assertFalse(GearScoreCalculator.wave3(source).score(crystal, source).isComplete());

		int fang = source.add(randomBonus(new Random(26L), EquipmentSlot.WEAPON, false));
		source.names.put(fang, "Osmumten's fang");
		assertFalse(GearScoreCalculator.wave3(source).score(
			Arrays.asList(new WornItem(EquipmentSlot.WEAPON,fang)),source).isComplete());

		int venator = source.add(randomBonus(new Random(27L), EquipmentSlot.WEAPON, true));
		source.names.put(venator, "Venator bow");
		assertFalse(GearScoreCalculator.wave3(source).score(
			Arrays.asList(new WornItem(EquipmentSlot.WEAPON,venator)),source).isComplete());
	}
}
