package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Bounded per-call memoization regression: real ItemManager stats should not
 * be resolved repeatedly while comparing before/after one equipment swap.
 */
public class ComparisonStatSnapshotTest
{
	private static EquipmentBonus bonus(int slot, boolean twoHanded, int str)
	{
		return new EquipmentBonus(slot, twoHanded,
			0,0,0,0,0,0,0,0,0,0,str,0,BigDecimal.ZERO,0);
	}

	private static final class CountingSource implements ItemStatSource
	{
		final Map<Integer, EquipmentBonus> values = new HashMap<Integer, EquipmentBonus>();
		final Map<Integer, Integer> calls = new HashMap<Integer, Integer>();

		CountingSource set(int id, EquipmentBonus value)
		{
			values.put(Integer.valueOf(id), value);
			return this;
		}

		@Override
		public EquipmentBonus getVerifiedBonus(int id)
		{
			Integer key = Integer.valueOf(id);
			Integer previous = calls.get(key);
			calls.put(key, Integer.valueOf(previous == null ? 1 : previous.intValue() + 1));
			return values.get(key);
		}

		int times(int id)
		{
			Integer count = calls.get(Integer.valueOf(id));
			return count == null ? 0 : count.intValue();
		}
	}

	@Test
	public void everyKnownItemIsLookedUpOnlyOnceDuringOneTwoHandedSwap()
	{
		CountingSource source = new CountingSource()
			.set(1, bonus(EquipmentSlot.WEAPON,false,10))
			.set(2, bonus(EquipmentSlot.SHIELD,false,4))
			.set(3, bonus(EquipmentSlot.HEAD,false,2))
			.set(4, bonus(EquipmentSlot.RING,false,1))
			.set(5, bonus(EquipmentSlot.AMMO,false,7))
			.set(6, bonus(EquipmentSlot.WEAPON,true,25));
		List<WornItem> worn = new ArrayList<WornItem>();
		for (int id = 1; id <= 5; id++)
		{
			worn.add(new WornItem(source.values.get(Integer.valueOf(id)).getSlot(),id));
		}
		GearScoreComparison compare = GearScoreCalculator.wave1().compare(6,worn,source);
		assertEquals("11.0",compare.getDelta().toPlainString());
		for (int id = 1; id <= 6; id++)
		{
			assertEquals("id=" + id,1,source.times(id));
		}
		assertEquals(6,source.calls.size());
	}

	@Test
	public void unknownUnrelatedItemIsAlsoMemoizedAsNullWithinOneSwap()
	{
		CountingSource source = new CountingSource()
			.set(1, bonus(EquipmentSlot.WEAPON,false,10))
			.set(2, bonus(EquipmentSlot.WEAPON,false,13));
		List<WornItem> worn = new ArrayList<WornItem>();
		worn.add(new WornItem(EquipmentSlot.WEAPON,1));
		worn.add(new WornItem(EquipmentSlot.HEAD,999999)); // unknown but unchanged
		GearScoreComparison compare = GearScoreCalculator.wave1().compare(2,worn,source);
		assertEquals("3.0",compare.getDelta().toPlainString());
		assertEquals(1,source.times(999999));
		assertEquals(1,source.times(1));
		assertEquals(1,source.times(2));
	}

	@Test
	public void noCacheCrossesComparisonsOrHidesSubsequentEquipmentChanges()
	{
		CountingSource source = new CountingSource()
			.set(1, bonus(EquipmentSlot.WEAPON,false,10))
			.set(2, bonus(EquipmentSlot.WEAPON,false,13));
		List<WornItem> worn = new ArrayList<WornItem>();
		worn.add(new WornItem(EquipmentSlot.WEAPON,1));
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		assertEquals("3.0",calculator.compare(2,worn,source).getDelta().toPlainString());
		source.set(2,bonus(EquipmentSlot.WEAPON,false,20));
		assertEquals("10.0",calculator.compare(2,worn,source).getDelta().toPlainString());
		assertEquals(2,source.times(2));
		assertEquals(2,source.times(1));
	}
}
