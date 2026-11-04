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

/**
 * Wave 3 source-level tests; these are NOT in-game RuneLite validation.
 * In particular the tests do not imply that combat style, target layout,
 * charging, or a spell cast can be inferred from equipment alone.
 */
public class WeaponPassiveDetectorTest
{
	private final WeaponPassiveDetector detector = new WeaponPassiveDetector();

	private static final class Catalog
		implements SetEffectDetector.ItemNameSource, ItemStatSource
	{
		private final Map<Integer, String> names = new HashMap<Integer, String>();
		private final Map<Integer, EquipmentBonus> stats = new HashMap<Integer, EquipmentBonus>();

		Catalog item(int id, String name, int slot, int strength)
		{
			names.put(Integer.valueOf(id), name);
			stats.put(Integer.valueOf(id), new EquipmentBonus(
				slot, false,
				0, 0, 0, 0, 0,
				0, 0, 0, 0, 0,
				strength, 0, BigDecimal.ZERO, 0));
			return this;
		}

		@Override
		public String nameOf(int id)
		{
			return names.get(Integer.valueOf(id));
		}

		@Override
		public EquipmentBonus getVerifiedBonus(int id)
		{
			return stats.get(Integer.valueOf(id));
		}
	}

	private static List<WornItem> wear(int id)
	{
		List<WornItem> worn = new ArrayList<WornItem>();
		worn.add(new WornItem(EquipmentSlot.WEAPON, id));
		return worn;
	}

	private WeaponPassiveDetector.State state(
		List<WornItem> equipment,
		SetEffectDetector.ItemNameSource catalog,
		String key)
	{
		for (WeaponPassiveDetector.Effect effect : detector.evaluate(equipment, catalog))
		{
			if (effect.getKey().equals(key))
			{
				return effect.getState();
			}
		}
		throw new AssertionError("Unknown passive rule: " + key);
	}

	@Test
	public void fangAndOrnamentVariantIdentifySeparatePassiveMechanics()
	{
		Catalog catalog = new Catalog()
			.item(1, "Osmumten's fang", EquipmentSlot.WEAPON, 10)
			.item(2, "Osmumten's fang (or)", EquipmentSlot.WEAPON, 10);
		for (int id = 1; id <= 2; id++)
		{
			assertEquals(WeaponPassiveDetector.State.PRESENT_CONDITIONAL,
				state(wear(id), catalog, "fang.stab_accuracy"));
			assertEquals(WeaponPassiveDetector.State.PRESENT_UNRATED,
				state(wear(id), catalog, "fang.damage_band"));
			assertEquals(WeaponPassiveDetector.State.ABSENT,
				state(wear(id), catalog, "venator.ricochet"));
		}
	}

	@Test
	public void venatorIsIdentifiedAsConditionalButNeverClaimedActive()
	{
		Catalog catalog = new Catalog()
			.item(1, "Venator bow", EquipmentSlot.WEAPON, 10)
			.item(2, "Venator bow (uncharged)", EquipmentSlot.WEAPON, 10);
		for (int id = 1; id <= 2; id++)
		{
			assertEquals(WeaponPassiveDetector.State.PRESENT_CONDITIONAL,
				state(wear(id), catalog, "venator.ricochet"));
		}
		WeaponPassiveDetector.Effect effect = detector.evaluate(wear(2), catalog).get(2);
		assertTrue(effect.getRequirement().contains("charges"));
		assertFalse(effect.hasApprovedScore());
	}

	@Test
	public void twinflameDoubleHitDoesNotClaimEverySpellQualifies()
	{
		Catalog catalog = new Catalog().item(1, "Twinflame staff", EquipmentSlot.WEAPON, 10);
		assertEquals(WeaponPassiveDetector.State.PRESENT_CONDITIONAL,
			state(wear(1), catalog, "twinflame.standard_spell_bonus"));
		assertEquals(WeaponPassiveDetector.State.PRESENT_CONDITIONAL,
			state(wear(1), catalog, "twinflame.second_hit"));
		WeaponPassiveDetector.Effect doubleHit = detector.evaluate(wear(1), catalog).get(4);
		assertTrue(doubleHit.getRequirement().contains("Strike"));
		assertFalse(doubleHit.hasApprovedScore());
	}

	@Test
	public void otherWeaponsAndUnoccupiedSlotsAreNotPassives()
	{
		Catalog catalog = new Catalog()
			.item(1, "Abyssal whip", EquipmentSlot.WEAPON, 10)
			.item(2, "Osmumten's fang", EquipmentSlot.BODY, 10);
		for (WeaponPassiveDetector.Effect effect : detector.evaluate(wear(1), catalog))
		{
			assertEquals(WeaponPassiveDetector.State.ABSENT, effect.getState());
		}
		List<WornItem> misplaced = new ArrayList<WornItem>();
		misplaced.add(new WornItem(EquipmentSlot.BODY, 2));
		assertEquals(WeaponPassiveDetector.State.ABSENT,
			state(misplaced, catalog, "fang.stab_accuracy"));
	}

	@Test
	public void missingIdentityContainerAndMalformedDuplicateRemainUnknown()
	{
		Catalog catalog = new Catalog().item(1, null, EquipmentSlot.WEAPON, 10);
		assertEquals(WeaponPassiveDetector.State.UNKNOWN,
			state(wear(1), catalog, "fang.stab_accuracy"));
		assertEquals(WeaponPassiveDetector.State.UNKNOWN,
			state(null, catalog, "venator.ricochet"));
		assertEquals(WeaponPassiveDetector.State.UNKNOWN,
			state(wear(1), null, "twinflame.second_hit"));

		List<WornItem> duplicate = wear(1);
		duplicate.add(new WornItem(EquipmentSlot.WEAPON, 2));
		assertEquals(WeaponPassiveDetector.State.UNKNOWN,
			state(duplicate, catalog, "venator.ricochet"));
	}

	@Test
	public void wave3KeepsBaseScoreAndMarksUnratedWeaponIncomplete()
	{
		Catalog catalog = new Catalog()
			.item(1, "Abyssal whip", EquipmentSlot.WEAPON, 10)
			.item(2, "Osmumten's fang", EquipmentSlot.WEAPON, 20);
		GearScoreCalculator wave1 = GearScoreCalculator.wave1();
		GearScoreCalculator wave3 = GearScoreCalculator.wave3(catalog);

		assertEquals("10.0", wave1.score(wear(1), catalog).displayedTotal().toPlainString());
		GearScoreBreakdown ordinary = wave3.score(wear(1), catalog);
		assertTrue(ordinary.isComplete());
		assertEquals("10.0", ordinary.displayedTotal().toPlainString());
		assertEquals(BigDecimal.ZERO, ordinary.getWeaponPassive());

		GearScoreBreakdown fang = wave3.score(wear(2), catalog);
		assertFalse(fang.isComplete());
		assertFalse(fang.isExtensionDataComplete());
		assertEquals("20.0", fang.displayedTotal().toPlainString());
		assertEquals("Gear Score: 20.0 ?", GearScoreText.equipmentLine(fang));
		assertEquals(BigDecimal.ZERO, fang.getWeaponPassive());
	}

	@Test
	public void hypotheticalSwapNeverInventsUnratedDelta()
	{
		Catalog catalog = new Catalog()
			.item(1, "Abyssal whip", EquipmentSlot.WEAPON, 10)
			.item(2, "Osmumten's fang", EquipmentSlot.WEAPON, 20)
			.item(3, "Dragon scimitar", EquipmentSlot.WEAPON, 15);
		GearScoreCalculator calculator = GearScoreCalculator.wave3(catalog);

		GearScoreComparison toFang = calculator.compare(2, wear(1), catalog);
		assertTrue(toFang.isItemKnown());
		assertEquals("20.0", toFang.getItemScore().toPlainString());
		assertNull(toFang.getDelta());
		assertEquals(GearScoreComparison.Verdict.UNKNOWN, toFang.getVerdict());

		GearScoreComparison fromFang = calculator.compare(3, wear(2), catalog);
		assertNull(fromFang.getDelta());
		assertEquals(GearScoreComparison.Verdict.UNKNOWN, fromFang.getVerdict());

		GearScoreComparison ordinary = calculator.compare(3, wear(1), catalog);
		assertEquals("5.0", ordinary.getDelta().toPlainString());
	}

	@Test
	public void genuinelyEmptyVsUnavailableEquipmentRemainDistinct()
	{
		Catalog catalog = new Catalog().item(1, "Abyssal whip", EquipmentSlot.WEAPON, 10);
		List<WornItem> empty = new ArrayList<WornItem>();
		GearScoreCalculator calculator = GearScoreCalculator.wave3(catalog);
		assertTrue(calculator.score(empty, catalog).isComplete());
		assertFalse(calculator.score(null, catalog).isComplete());
		assertEquals("Gear Score: 0.0 ?", GearScoreText.equipmentLine(calculator.score(null, catalog)));
		assertNull(calculator.compare(1, null, catalog).getDelta());
	}

	@Test
	public void pendingBridgeReturnsZeroOnlyWhenAllPassivesAbsent()
	{
		Catalog catalog = new Catalog()
			.item(1, "Abyssal whip", EquipmentSlot.WEAPON, 10)
			.item(2, "Twinflame staff", EquipmentSlot.WEAPON, 10);
		PendingWeaponPassiveBonus pending = new PendingWeaponPassiveBonus(detector, catalog);
		assertEquals(BigDecimal.ZERO, pending.gearScore(wear(1), catalog));
		assertNull(pending.gearScore(wear(2), catalog));
		assertNull(pending.gearScore(null, catalog));
	}
}
