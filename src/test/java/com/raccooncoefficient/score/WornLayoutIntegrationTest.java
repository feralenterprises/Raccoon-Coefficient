package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Cross-wave integrity tests: malformed worn state cannot create fake items,
 * upgraded slot winners or active armour/weapon passive evidence.
 */
public class WornLayoutIntegrationTest
{
	private static final class Catalog implements ItemStatSource, SetEffectDetector.ItemNameSource
	{
		private final Map<Integer, EquipmentBonus> values = new HashMap<Integer, EquipmentBonus>();
		private final Map<Integer, String> names = new HashMap<Integer, String>();

		Catalog item(int id, int slot, int strength, String name)
		{
			values.put(id, new EquipmentBonus(slot, false,
				0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				strength, 0, BigDecimal.ZERO, 0));
			names.put(id, name);
			return this;
		}

		@Override public EquipmentBonus getVerifiedBonus(int id) { return values.get(id); }
		@Override public String nameOf(int id) { return names.get(id); }
	}

	private static List<WornItem> worn(int... pairs)
	{
		WornItem[] items = new WornItem[pairs.length / 2];
		for (int i = 0; i < pairs.length; i += 2)
		{
			items[i / 2] = new WornItem(pairs[i], pairs[i + 1]);
		}
		return Arrays.asList(items);
	}

	private static SetEffectDetector.State effect(
		SetEffectDetector detector, List<WornItem> worn, Catalog source, String key)
	{
		for (SetEffectDetector.Effect value : detector.evaluate(worn, source))
		{
			if (key.equals(value.getKey())) return value.getState();
		}
		throw new AssertionError(key);
	}

	private static WeaponPassiveDetector.State passive(
		WeaponPassiveDetector detector, List<WornItem> worn, Catalog source, String key)
	{
		for (WeaponPassiveDetector.Effect value : detector.evaluate(worn, source))
		{
			if (key.equals(value.getKey())) return value.getState();
		}
		throw new AssertionError(key);
	}

	@Test
	public void repeatedOccupiedSlotDoesNotDoubleCountGearScore()
	{
		Catalog source = new Catalog()
			.item(1, EquipmentSlot.WEAPON, 10, "Abyssal whip")
			.item(2, EquipmentSlot.WEAPON, 35, "Dragon scimitar");
		List<WornItem> inconsistent = worn(EquipmentSlot.WEAPON, 1, EquipmentSlot.WEAPON, 2);
		GearScoreBreakdown result = GearScoreCalculator.wave1().score(inconsistent, source);
		assertEquals("10.0", result.displayedTotal().toPlainString());
		assertFalse("This subtotal must not be certified complete", result.isComplete());
		assertEquals("Gear Score: 10.0 ?", GearScoreText.equipmentLine(result));
	}

	@Test
	public void ambiguousDisplacedSlotHasNoVerifiedSwapDelta()
	{
		Catalog source = new Catalog()
			.item(1, EquipmentSlot.WEAPON, 10, "Abyssal whip")
			.item(2, EquipmentSlot.WEAPON, 35, "Dragon scimitar")
			.item(3, EquipmentSlot.WEAPON, 20, "Rune scimitar");
		for (GearScoreCalculator calculator : Arrays.asList(
			GearScoreCalculator.wave1(), GearScoreCalculator.wave3(source)))
		{
			GearScoreComparison compared = calculator.compare(3,
				worn(EquipmentSlot.WEAPON, 1, EquipmentSlot.WEAPON, 2), source);
			assertTrue(compared.isItemKnown());
			assertNull(compared.getDelta());
			assertNull(compared.getBaseDelta());
			assertEquals(GearScoreComparison.Verdict.UNKNOWN, compared.getVerdict());
		}
	}

	@Test
	public void ambiguousArmourSlotCannotProveFullVoidEffect()
	{
		Catalog source = new Catalog()
			.item(1, 0, 0, "Void melee helm")
			.item(2, 4, 0, "Void knight top")
			.item(3, 7, 0, "Void knight robe")
			.item(4, 9, 0, "Void knight gloves")
			.item(5, 0, 0, "Rune full helm");
		List<WornItem> genuine = worn(0,1,4,2,7,3,9,4);
		assertEquals(SetEffectDetector.State.ACTIVE,
			effect(new SetEffectDetector(), genuine, source, "void.melee"));
		List<WornItem> corrupted = worn(0,1,4,2,7,3,9,4,0,5);
		assertEquals(SetEffectDetector.State.UNKNOWN,
			effect(new SetEffectDetector(), corrupted, source, "void.melee"));
		assertFalse(GearScoreCalculator.wave3(source).score(corrupted, source).isComplete());
	}

	@Test
	public void duplicatedUnrelatedSlotAlsoInvalidatesWeaponEvidence()
	{
		Catalog source = new Catalog()
			.item(1, EquipmentSlot.WEAPON, 20, "Osmumten's fang")
			.item(2, EquipmentSlot.HEAD, 5, "Rune full helm")
			.item(3, EquipmentSlot.HEAD, 3, "Adamant full helm");
		List<WornItem> genuine = worn(EquipmentSlot.WEAPON,1,EquipmentSlot.HEAD,2);
		assertEquals(WeaponPassiveDetector.State.PRESENT_CONDITIONAL,
			passive(new WeaponPassiveDetector(),genuine,source,"fang.stab_accuracy"));
		List<WornItem> corrupted = worn(EquipmentSlot.WEAPON,1,EquipmentSlot.HEAD,2,EquipmentSlot.HEAD,3);
		assertEquals(WeaponPassiveDetector.State.UNKNOWN,
			passive(new WeaponPassiveDetector(),corrupted,source,"fang.stab_accuracy"));
	}

	@Test
	public void anObservedEmptyAndNormalUniqueSlotsRemainComplete()
	{
		Catalog source = new Catalog().item(1, EquipmentSlot.WEAPON, 10, "Abyssal whip");
		assertTrue(GearScoreCalculator.wave3(source)
			.score(Collections.<WornItem>emptyList(),source).isComplete());
		assertTrue(GearScoreCalculator.wave3(source)
			.score(worn(EquipmentSlot.WEAPON,1),source).isComplete());
		assertFalse(WornLayout.hasDuplicateOccupiedSlot(worn(EquipmentSlot.WEAPON,1)));
		assertTrue(WornLayout.hasDuplicateOccupiedSlot(
			worn(EquipmentSlot.WEAPON,1,EquipmentSlot.WEAPON,1)));
	}
}
