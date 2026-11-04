package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Wave 10: verify factual mechanical percentages without translating them
 * into owner-unapproved Gear Score points or implied DPS percentages.
 */
public class EffectCalibrationLedgerTest
{
	private final EffectCalibrationLedger ledger = new EffectCalibrationLedger();

	private static final class Catalog
		implements SetEffectDetector.ItemNameSource, ItemStatSource
	{
		private final Map<Integer, String> names = new HashMap<Integer, String>();
		private final Map<Integer, EquipmentBonus> stats = new HashMap<Integer, EquipmentBonus>();

		Catalog item(int id, String name, int slot, int strength)
		{
			names.put(id, name);
			stats.put(id, new EquipmentBonus(slot, false,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				strength, 0, BigDecimal.ZERO, 0));
			return this;
		}

		@Override public String nameOf(int id) { return names.get(id); }
		@Override public EquipmentBonus getVerifiedBonus(int id) { return stats.get(id); }
	}

	private static List<WornItem> wear(int... slotsAndIds)
	{
		List<WornItem> gear = new ArrayList<WornItem>();
		for (int i = 0; i < slotsAndIds.length; i += 2)
		{
			gear.add(new WornItem(slotsAndIds[i], slotsAndIds[i + 1]));
		}
		return gear;
	}

	private EffectCalibrationLedger.Observation observation(
		List<WornItem> worn, Catalog catalog, String key)
	{
		for (EffectCalibrationLedger.Observation item : ledger.observe(worn, catalog))
		{
			if (item.getMechanic().getKey().equals(key))
			{
				return item;
			}
		}
		throw new AssertionError("Missing effect: " + key);
	}

	@Test
	public void allCurrentlyRegisteredEffectsHaveSourceBackedFacts()
	{
		Catalog catalog = new Catalog();
		List<EffectCalibrationLedger.Observation> lines =
			ledger.observe(wear(), catalog);
		assertEquals(27, lines.size());
		assertEquals(27, ledger.catalog().size());
		for (EffectCalibrationLedger.Observation line : lines)
		{
			assertEquals(EffectCalibrationLedger.Evidence.ABSENT, line.getEvidence());
			assertTrue(line.getSourceUrl().startsWith("https://oldschool.runescape.wiki/w/"));
			assertFalse(line.getMechanic().getNote().isEmpty());
			assertNotNull(line.getMechanic().getContext());
			assertFalse("No effect is approved for universal score points",
				line.getMechanic().hasApprovedGearScoreWeight());
		}
	}

	@Test
	public void voidMagicIsAccuracyOnlyWhileEliteMagicAddsDamage()
	{
		assertEquals("45", ledger.catalog().get("void.magic")
			.getAccuracyPercent().toPlainString());
		assertNull(ledger.catalog().get("void.magic").getDamagePercent());
		assertNull(ledger.catalog().get("void.elite.magic").getAccuracyPercent());
		assertEquals("5", ledger.catalog().get("void.elite.magic")
			.getDamagePercent().toPlainString());

		assertEquals("10", ledger.catalog().get("void.ranged")
			.getDamagePercent().toPlainString());
		assertEquals("2.5", ledger.catalog().get("void.elite.ranged")
			.getDamagePercent().toPlainString());
	}

	@Test
	public void inquisitorCurrent2026PiecesDoNotClaimOldSetOrMaceMultipliers()
	{
		assertEquals("0.5", ledger.catalog().get("inquisitor.helm")
			.getDamagePercent().toPlainString());
		assertEquals("1", ledger.catalog().get("inquisitor.body")
			.getDamagePercent().toPlainString());
		assertEquals("1", ledger.catalog().get("inquisitor.legs")
			.getDamagePercent().toPlainString());
		assertEquals("0.5", ledger.catalog().get("inquisitor.helm")
			.getAccuracyPercent().toPlainString());
		assertEquals("1", ledger.catalog().get("inquisitor.body")
			.getAccuracyPercent().toPlainString());
	}

	@Test
	public void crystalPiecesAreContextualButHaveExactPublishedPercentages()
	{
		BigDecimal totalAccuracy = BigDecimal.ZERO;
		BigDecimal totalDamage = BigDecimal.ZERO;
		for (String piece : new String[]{"helm", "body", "legs"})
		{
			EffectCalibrationLedger.Mechanic m = ledger.catalog().get("crystal." + piece);
			assertEquals(EffectCalibrationLedger.Context.RANGED_STYLE, m.getContext());
			totalAccuracy = totalAccuracy.add(m.getAccuracyPercent());
			totalDamage = totalDamage.add(m.getDamagePercent());
		}
		assertEquals(0, new BigDecimal("30").compareTo(totalAccuracy));
		assertEquals(0, new BigDecimal("15").compareTo(totalDamage));
	}

	@Test
	public void fangBandMustNeverBeScoredAsDamageIncrease()
	{
		EffectCalibrationLedger.Mechanic band = ledger.catalog().get("fang.damage_band");
		assertEquals(EffectCalibrationLedger.Context.DAMAGE_DISTRIBUTION, band.getContext());
		assertNull(band.getAccuracyPercent());
		assertNull(band.getDamagePercent());
		assertNull(band.getFollowupHitPercent());
		assertTrue(band.getNote().contains("not expected DPS"));
	}

	@Test
	public void twinflameSecondHitIsNotAUniversalFortyPercentDamageBoost()
	{
		EffectCalibrationLedger.Mechanic spell = ledger.catalog()
			.get("twinflame.standard_spell_bonus");
		assertEquals("10", spell.getAccuracyPercent().toPlainString());
		assertEquals("10", spell.getDamagePercent().toPlainString());

		EffectCalibrationLedger.Mechanic second = ledger.catalog()
			.get("twinflame.second_hit");
		assertNull(second.getDamagePercent());
		assertEquals("40", second.getFollowupHitPercent().toPlainString());
		assertEquals(EffectCalibrationLedger.Context.SPELL_SELECTION, second.getContext());
		assertTrue(second.getNote().contains("Strike/Surge"));
	}

	@Test
	public void fangAndVenatorRequireCombatContextForAnyImpactEstimate()
	{
		assertEquals(EffectCalibrationLedger.Context.TARGET_DEFENCE,
			ledger.catalog().get("fang.stab_accuracy").getContext());
		assertNull(ledger.catalog().get("fang.stab_accuracy").getAccuracyPercent());
		assertEquals(EffectCalibrationLedger.Context.MULTITARGET,
			ledger.catalog().get("venator.ricochet").getContext());
		assertNull(ledger.catalog().get("venator.ricochet").getDamagePercent());
	}

	@Test
	public void anObservedSetIsUnratedAndMustNotChangeLockedBaseNumber()
	{
		Catalog catalog = new Catalog()
			.item(1, "Void ranger helm", 0, 1)
			.item(2, "Void knight top", 4, 2)
			.item(3, "Void knight robe", 7, 3)
			.item(4, "Void knight gloves", 9, 4);
		List<WornItem> gear = wear(0,1,4,2,7,3,9,4);

		assertEquals(EffectCalibrationLedger.Evidence.DETECTED_UNRATED,
			observation(gear, catalog, "void.ranged").getEvidence());
		assertEquals(EffectCalibrationLedger.Evidence.ABSENT,
			observation(gear, catalog, "void.melee").getEvidence());
		GearScoreBreakdown wave1 = GearScoreCalculator.wave1().score(gear, catalog);
		GearScoreBreakdown wave3 = GearScoreCalculator.wave3(catalog).score(gear, catalog);
		assertTrue(wave1.isComplete());
		assertFalse(wave3.isComplete());
		assertEquals("10.0", wave3.displayedTotal().toPlainString());
		assertEquals(wave1.displayedTotal(), wave3.displayedTotal());
		assertNull("No set may be assigned a fabricated universal score",
			new PendingSetEffectBonus(new SetEffectDetector(), catalog).gearScore(gear, catalog));
	}

	@Test
	public void knownWeaponDoesNotMeanItsContextIsVerified()
	{
		Catalog catalog = new Catalog().item(1, "Venator bow", EquipmentSlot.WEAPON, 25);
		List<WornItem> worn = wear(EquipmentSlot.WEAPON,1);
		assertEquals(EffectCalibrationLedger.Evidence.DETECTED_UNRATED,
			observation(worn, catalog, "venator.ricochet").getEvidence());
		assertEquals(EffectCalibrationLedger.Evidence.ABSENT,
			observation(worn, catalog, "fang.stab_accuracy").getEvidence());
		assertFalse(GearScoreCalculator.wave3(catalog).score(worn, catalog).isComplete());
	}

	@Test
	public void unavailableEquipmentCannotPretendEffectsAreAbsent()
	{
		Catalog catalog = new Catalog();
		for (EffectCalibrationLedger.Observation line : ledger.observe(null, catalog))
		{
			assertEquals(EffectCalibrationLedger.Evidence.UNKNOWN, line.getEvidence());
		}
	}
}
