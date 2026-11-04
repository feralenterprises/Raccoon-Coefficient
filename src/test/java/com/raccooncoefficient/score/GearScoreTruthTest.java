package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Wave 4 regression suite: honest base-only swaps and visible-score consistency. */
public class GearScoreTruthTest
{
	private static final class Items implements ItemStatSource, SetEffectDetector.ItemNameSource
	{
		private final Map<Integer, EquipmentBonus> stats = new HashMap<Integer, EquipmentBonus>();
		private final Map<Integer, String> names = new HashMap<Integer, String>();

		Items add(int id, int slot, String name, boolean twoHanded, int meleeStrength)
		{
			return add(id, slot, name, twoHanded, meleeStrength, "0");
		}

		Items add(int id, int slot, String name, boolean twoHanded, int meleeStrength, String magicDamage)
		{
			stats.put(Integer.valueOf(id), new EquipmentBonus(
				slot, twoHanded, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, meleeStrength, 0, new BigDecimal(magicDamage), 0));
			names.put(Integer.valueOf(id), name);
			return this;
		}

		@Override
		public EquipmentBonus getVerifiedBonus(int id)
		{
			return stats.get(Integer.valueOf(id));
		}

		@Override
		public String nameOf(int id)
		{
			return names.get(Integer.valueOf(id));
		}
	}

	private static List<WornItem> worn(int... slotsAndIds)
	{
		List<WornItem> gear = new ArrayList<WornItem>();
		for (int i = 0; i < slotsAndIds.length; i += 2)
		{
			gear.add(new WornItem(slotsAndIds[i], slotsAndIds[i + 1]));
		}
		return gear;
	}

	@Test
	public void fangSwapHasKnownBaseDeltaButUnknownTotalDelta()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Abyssal whip", false, 10)
			.add(2, EquipmentSlot.WEAPON, "Osmumten's fang", false, 20);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(2, worn(EquipmentSlot.WEAPON, 1), source);
		assertEquals("20.0", comparison.getItemScore().toPlainString());
		assertNull(comparison.getDelta());
		assertEquals(GearScoreComparison.Verdict.UNKNOWN, comparison.getVerdict());
		assertEquals("10.0", comparison.getBaseDelta().toPlainString());
		String rendered = GearScoreText.tooltip(comparison, false);
		assertTrue(rendered.contains("Base <col=00c846>\u2191 +10.0</col>"));
		assertTrue(rendered.contains("(total ?)"));
	}

	@Test
	public void weaponSwapOffTheFangProducesNegativeBaseOnlyDelta()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Osmumten's fang", false, 20)
			.add(2, EquipmentSlot.WEAPON, "Dragon scimitar", false, 14);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(2, worn(EquipmentSlot.WEAPON, 1), source);
		assertNull(comparison.getDelta());
		assertEquals("-6.0", comparison.getBaseDelta().toPlainString());
		assertTrue(GearScoreText.tooltip(comparison, false)
			.contains("Base <col=ff4040>\u2193 \u22126.0</col>"));
	}

	@Test
	public void knownTwoHandedSwapSubtractsShieldAsWellAsWeapon()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Abyssal whip", false, 10)
			.add(2, EquipmentSlot.SHIELD, "Dragon defender", false, 4)
			.add(3, EquipmentSlot.WEAPON, "Venator bow", true, 20);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(3, worn(EquipmentSlot.WEAPON, 1, EquipmentSlot.SHIELD, 2), source);
		assertNull(comparison.getDelta());
		assertEquals("6.0", comparison.getBaseDelta().toPlainString());
	}

	@Test
	public void unknownRemovedSlotBlocksEvenBaseOnlyDelta()
	{
		Items source = new Items()
			.add(3, EquipmentSlot.WEAPON, "Venator bow", true, 20);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(3, worn(EquipmentSlot.WEAPON, 1, EquipmentSlot.SHIELD, 2), source);
		assertNull(comparison.getDelta());
		assertNull(comparison.getBaseDelta());
		assertTrue(GearScoreText.tooltip(comparison, false).contains("\u2192 ?"));
	}

	@Test
	public void unrelatedUnknownStatsDoNotFabricateTotalButAllowVerifiedBaseDelta()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Abyssal whip", false, 10)
			.add(2, EquipmentSlot.WEAPON, "Dragon scimitar", false, 15);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(2, worn(EquipmentSlot.WEAPON, 1, EquipmentSlot.HEAD, 9999), source);
		assertNull(comparison.getDelta());
		assertEquals("5.0", comparison.getBaseDelta().toPlainString());
	}

	@Test
	public void missingEquipmentContainerIsNeverASyntheticEmptyLoadout()
	{
		Items source = new Items().add(2, EquipmentSlot.WEAPON, "Dragon scimitar", false, 15);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source).compare(2, null, source);
		assertNull(comparison.getDelta());
		assertNull(comparison.getBaseDelta());
		assertTrue(comparison.isItemKnown());
		assertFalse(GearScoreCalculator.wave3(source).score(null, source).isComplete());
	}

	@Test
	public void noPendingEffectsPreservesVerifiedTotalAndNormalArrow()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Abyssal whip", false, 10)
			.add(2, EquipmentSlot.WEAPON, "Dragon scimitar", false, 15);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(2, worn(EquipmentSlot.WEAPON, 1), source);
		assertEquals("5.0", comparison.getDelta().toPlainString());
		assertNull(comparison.getBaseDelta());
		assertEquals(GearScoreComparison.Verdict.BETTER, comparison.getVerdict());
		assertTrue(GearScoreText.tooltip(comparison, false)
			.contains("<col=00c846>\u2191 +5.0</col>"));
	}

	@Test
	public void displayedTotalDeltaMatchesVisibleHalfUpScores()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Abyssal whip", false, 0, "0.05")
			.add(2, EquipmentSlot.WEAPON, "Dragon scimitar", false, 0, "0.14");
		GearScoreCalculator calculator = GearScoreCalculator.wave3(source);
		assertEquals("0.1", calculator.score(worn(EquipmentSlot.WEAPON, 1), source)
			.displayedTotal().toPlainString());
		assertEquals("0.1", calculator.score(worn(EquipmentSlot.WEAPON, 2), source)
			.displayedTotal().toPlainString());
		assertEquals("0.0", calculator.compare(2, worn(EquipmentSlot.WEAPON, 1), source)
			.getDelta().toPlainString());
	}

	@Test
	public void baseOnlyEqualIsNeutralAndTotalRemainsUnknown()
	{
		Items source = new Items()
			.add(1, EquipmentSlot.WEAPON, "Osmumten's fang", false, 20)
			.add(2, EquipmentSlot.WEAPON, "Twinflame staff", false, 20);
		GearScoreComparison comparison = GearScoreCalculator.wave3(source)
			.compare(2, worn(EquipmentSlot.WEAPON, 1), source);
		assertNull(comparison.getDelta());
		assertEquals("0.0", comparison.getBaseDelta().toPlainString());
		assertTrue(GearScoreText.tooltip(comparison, false)
			.contains("Base <col=b4b4b4>\u2192 0.0</col>"));
	}

	@Test
	public void originalWave1CoefficientsAreIdenticalInWave4()
	{
		Items source = new Items().add(1, EquipmentSlot.WEAPON, "Abyssal whip", false, 10);
		GearScoreCalculator legacy = GearScoreCalculator.wave1();
		GearScoreCalculator current = GearScoreCalculator.wave3(source);
		assertEquals(legacy.score(worn(EquipmentSlot.WEAPON, 1), source).displayedTotal(),
			current.score(worn(EquipmentSlot.WEAPON, 1), source).displayedTotal());
		assertTrue(current.score(worn(EquipmentSlot.WEAPON, 1), source).isComplete());
	}
}
