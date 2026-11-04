package com.raccooncoefficient;

import com.raccooncoefficient.score.EquipmentBonus;
import com.raccooncoefficient.score.EquipmentSlot;
import com.raccooncoefficient.score.GearScoreCalculator;
import java.math.BigDecimal;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemStats;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class RuneLiteItemStatSourceTest
{
	@Test
	public void mapsVerifiedEquipmentFieldsAndIgnoresWeightAndSpeed()
	{
		EquipmentBonus bonus = RuneLiteItemStatSource.fromItemStats(stats(true, 9.5, equipment()
			.slot(EquipmentSlot.WEAPON)
			.isTwoHanded(true)
			.astab(-4)
			.aslash(82)
			.acrush(0)
			.amagic(0)
			.arange(0)
			.dstab(0)
			.dslash(0)
			.dcrush(0)
			.dmagic(-1)
			.drange(0)
			.str(82)
			.rstr(0)
			.mdmg(15.5f)
			.prayer(0)
			.aspeed(4)
			.build()));

		assertEquals(EquipmentSlot.WEAPON, bonus.getSlot());
		assertTrue(bonus.isTwoHanded());
		assertEquals(-4, bonus.getAttackStab());
		assertEquals(82, bonus.getAttackSlash());
		assertEquals(-1, bonus.getDefenceMagic());
		assertEquals(82, bonus.getMeleeStrength());
		assertEquals(0, new BigDecimal("15.5").compareTo(bonus.getMagicDamagePercent()));

		EquipmentBonus slower = RuneLiteItemStatSource.fromItemStats(stats(true, 0.1, equipment()
			.slot(EquipmentSlot.WEAPON)
			.astab(-4)
			.aslash(82)
			.dmagic(-1)
			.str(82)
			.mdmg(15.5f)
			.aspeed(99)
			.isTwoHanded(true)
			.build()));
		assertEquals(0, GearScoreCalculator.wave1().scoreBonus(bonus).compareTo(GearScoreCalculator.wave1().scoreBonus(slower)));
	}

	@Test
	public void missingOrNonEquipableStatsAreUnknown()
	{
		assertNull(RuneLiteItemStatSource.fromItemStats(null));
		assertNull(RuneLiteItemStatSource.fromItemStats(stats(false, 0, equipment().str(5).build())));
		assertNull(RuneLiteItemStatSource.fromItemStats(new ItemStats(true, 0, 0, null)));
		assertNull(RuneLiteItemStatSource.fromItemStats(stats(true, 0, equipment().mdmg(Float.NaN).build())));
	}

	private static ItemStats stats(boolean equipable, double weight, ItemEquipmentStats equipment)
	{
		return new ItemStats(equipable, weight, 0, equipment);
	}

	private static ItemEquipmentStats.ItemEquipmentStatsBuilder equipment()
	{
		return ItemEquipmentStats.builder();
	}
}
