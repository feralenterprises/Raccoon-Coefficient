package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Reproducible v2 proposal based only on stats visible in owner's screenshots.
 *
 * NO CHANGE to production GearScoreCalculator.wave3 or hover/equipment UI.
 */
public class GearScoreV2PreviewTest
{
	private static EquipmentBonus stats(
		int slot, int attackStab, int attackSlash, int attackCrush,
		int attackMagic, int attackRanged,
		int defenceStab, int defenceSlash, int defenceCrush,
		int defenceMagic, int defenceRanged,
		int strength, int rangedStrength, String magicDamage, int prayer)
	{
		return new EquipmentBonus(slot, false,
			attackStab, attackSlash, attackCrush, attackMagic, attackRanged,
			defenceStab, defenceSlash, defenceCrush, defenceMagic, defenceRanged,
			strength, rangedStrength, new BigDecimal(magicDamage), prayer);
	}

	private static EquipmentBonus whip()
	{
		// Owner's Abyssal whip tooltip: Slash +82, Melee Strength +82.
		return stats(EquipmentSlot.WEAPON,0,82,0,0,0,0,0,0,0,0,82,0,"0",0);
	}

	private static EquipmentBonus ahrimTop()
	{
		// Owner's Ahrim's robetop: Magic Atk +30, Ranged Atk -10,
		// Defence Stab 52 Slash 37 Crush 63 Magic 30, Magic DMG +1%.
		return stats(EquipmentSlot.BODY,0,0,0,30,-10,52,37,63,30,0,0,0,"1",0);
	}

	private static EquipmentBonus guthanBody()
	{
		// Owner's Guthan's platebody: Magic Atk -30, Ranged Atk -10,
		// Defence Stab 122 Slash 120 Crush 107 Magic -6 Ranged 132.
		return stats(EquipmentSlot.BODY,0,0,0,-30,-10,122,120,107,-6,132,0,0,"0",0);
	}

	@Test
	public void matchesTheThreeRealWorldScreenshotsUnderTheLockedV1()
	{
		GearScoreCalculator v1 = GearScoreCalculator.wave1();
		assertEquals("90.2",v1.scoreBonus(whip()).toPlainString());
		assertEquals("21.2",v1.scoreBonus(ahrimTop()).toPlainString());
		assertEquals("43.5",v1.scoreBonus(guthanBody()).toPlainString());
	}

	@Test
	public void v2PreviewMakesStrengthLessDominantAndMagicEquipmentMoreComparable()
	{
		assertEquals("32.8", GearScoreV2Preview.itemDisplay(whip()).toPlainString());
		assertEquals("27.6", GearScoreV2Preview.itemDisplay(ahrimTop()).toPlainString());
		assertEquals("25.0", GearScoreV2Preview.itemDisplay(guthanBody()).toPlainString());
		assertTrue(GearScoreV2Preview.itemDisplay(whip()).compareTo(new BigDecimal("90.2")) < 0);
		assertTrue(GearScoreV2Preview.itemDisplay(ahrimTop()).compareTo(new BigDecimal("21.2")) > 0);
		assertTrue(GearScoreV2Preview.itemDisplay(guthanBody()).compareTo(new BigDecimal("43.5")) < 0);
	}

	@Test
	public void strengthAndMagicAttackCoefficientsAreExactlyThreeTenthsAndFourTenths()
	{
		EquipmentBonus onlyStrength = stats(EquipmentSlot.WEAPON,0,0,0,0,0,
			0,0,0,0,0,10,0,"0",0);
		EquipmentBonus onlyMagicAttack = stats(EquipmentSlot.BODY,0,0,0,10,0,
			0,0,0,0,0,0,0,"0",0);
		EquipmentBonus onlyRangedStrength = stats(EquipmentSlot.WEAPON,0,0,0,0,0,
			0,0,0,0,0,0,10,"0",0);
		assertEquals("3.0",GearScoreV2Preview.itemDisplay(onlyStrength).toPlainString());
		assertEquals("4.0",GearScoreV2Preview.itemDisplay(onlyMagicAttack).toPlainString());
		assertEquals("3.0",GearScoreV2Preview.itemDisplay(onlyRangedStrength).toPlainString());
	}

	@Test
	public void negativeMagicAttackAndNegativeDefencesRemainNegative()
	{
		EquipmentBonus negatives = stats(EquipmentSlot.BODY,0,0,0,-30,-10,
			0,0,0,-6,0,0,0,"0",0);
		assertEquals("-13.5",GearScoreV2Preview.itemDisplay(negatives).toPlainString());
	}

	@Test
	public void changingAnySingleBonusByOnePointCanNeverReduceTheRawScore()
	{
		EquipmentBonus empty = stats(EquipmentSlot.BODY,0,0,0,0,0,
			0,0,0,0,0,0,0,"0",0);
		BigDecimal base = GearScoreV2Preview.itemRaw(empty);
		EquipmentBonus[] improvements = new EquipmentBonus[] {
			stats(EquipmentSlot.BODY,1,0,0,0,0,0,0,0,0,0,0,0,"0",0),
			stats(EquipmentSlot.BODY,0,0,0,1,0,0,0,0,0,0,0,0,"0",0),
			stats(EquipmentSlot.BODY,0,0,0,0,0,0,0,0,0,1,0,0,"0",0),
			stats(EquipmentSlot.BODY,0,0,0,0,0,0,0,0,0,0,1,0,"0",0),
			stats(EquipmentSlot.BODY,0,0,0,0,0,0,0,0,0,0,0,0,"1",0),
			stats(EquipmentSlot.BODY,0,0,0,0,0,0,0,0,0,0,0,0,"0",1)
		};
		for (EquipmentBonus plus : improvements)
		{
			assertTrue("Increasing a bonus should not lower item score",
				GearScoreV2Preview.itemRaw(plus).compareTo(base) > 0);
		}
	}

	@Test
	public void missingItemStatRemainsUnknownAndIsNotRewardedAsZero()
	{
		ItemStatSource source = itemId -> itemId == 10 ? whip() : null;
		assertNull(GearScoreV2Preview.verifiedBaseTotal(
			Arrays.asList(new WornItem(EquipmentSlot.WEAPON,10),
				new WornItem(EquipmentSlot.HEAD,999999)), source));
		assertNull(GearScoreV2Preview.verifiedBaseTotal(null,source));
		assertNull(GearScoreV2Preview.verifiedBaseTotal(Collections.<WornItem>emptyList(),null));
	}

	@Test
	public void thePreviewIsNotAUniversalSetBonusOrCombatDpsCalculator()
	{
		assertTrue(GearScoreV2Preview.policy().contains("no universal set/passive weights"));
		assertTrue(GearScoreV2Preview.policy().contains("no live scoring"));
	}
}
