package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * EXPERIMENTAL, NOT WIRED INTO RUNE LITE UI.
 *
 * Gear Score v2 stat-distribution hypothesis based on owner-observed item
 * comparisons. These values are proposals, NOT claims about actual DPS.
 * Full set/weapon conditional effects remain separately unresolved.
 *
 * The production RaccoonCoefficientPlugin, hover display and equipped total
 * still call GearScoreCalculator.wave3(), using the locked Wave 1 formula.
 */
public final class GearScoreV2Preview
{
	private static final BigDecimal PHYSICAL_ATTACK = new BigDecimal("0.10");
	private static final BigDecimal RANGED_ATTACK = new BigDecimal("0.10");
	private static final BigDecimal MAGIC_ATTACK = new BigDecimal("0.40");
	private static final BigDecimal DEFENCE = new BigDecimal("0.08");
	private static final BigDecimal MELEE_STRENGTH = new BigDecimal("0.30");
	private static final BigDecimal RANGED_STRENGTH = new BigDecimal("0.30");
	private static final BigDecimal MAGIC_DAMAGE_PERCENT = new BigDecimal("2.00");
	private static final BigDecimal PRAYER = new BigDecimal("0.50");

	private GearScoreV2Preview()
	{
	}

	public static BigDecimal itemRaw(EquipmentBonus bonus)
	{
		if (bonus == null)
		{
			throw new IllegalArgumentException("No score for unverified equipment stats");
		}

		// All signed bonuses are preserved, including negative attack/defence.
		int physicalAttack = bonus.getAttackStab()
			+ bonus.getAttackSlash()
			+ bonus.getAttackCrush();
		int defence = bonus.getDefenceStab()
			+ bonus.getDefenceSlash()
			+ bonus.getDefenceCrush()
			+ bonus.getDefenceMagic()
			+ bonus.getDefenceRanged();

		return BigDecimal.valueOf(physicalAttack).multiply(PHYSICAL_ATTACK)
			.add(BigDecimal.valueOf(bonus.getAttackRanged()).multiply(RANGED_ATTACK))
			.add(BigDecimal.valueOf(bonus.getAttackMagic()).multiply(MAGIC_ATTACK))
			.add(BigDecimal.valueOf(defence).multiply(DEFENCE))
			.add(BigDecimal.valueOf(bonus.getMeleeStrength()).multiply(MELEE_STRENGTH))
			.add(BigDecimal.valueOf(bonus.getRangedStrength()).multiply(RANGED_STRENGTH))
			.add(bonus.getMagicDamagePercent().multiply(MAGIC_DAMAGE_PERCENT))
			.add(BigDecimal.valueOf(bonus.getPrayer()).multiply(PRAYER));
	}

	public static BigDecimal itemDisplay(EquipmentBonus bonus)
	{
		return itemRaw(bonus).setScale(1, RoundingMode.HALF_UP);
	}

	/**
	 * Only report a total when all *item stats* are verified. This is strictly
	 * a BASE score, not a complete set/weapon passive effectiveness estimate.
	 * No unapproved set-effect numbers are ever added.
	 */
	public static BigDecimal verifiedBaseTotal(List<WornItem> worn, ItemStatSource stats)
	{
		if (worn == null || stats == null || WornLayout.hasDuplicateOccupiedSlot(worn))
		{
			return null;
		}
		BigDecimal total = BigDecimal.ZERO;
		for (WornItem item : worn)
		{
			if (item == null || item.getItemId() < 0)
			{
				continue;
			}
			EquipmentBonus bonus = stats.getVerifiedBonus(item.getItemId());
			if (bonus == null)
			{
				return null;
			}
			total = total.add(itemRaw(bonus));
		}
		return total.setScale(1, RoundingMode.HALF_UP);
	}

	public static String policy()
	{
		return "EXPERIMENTAL v2; no live scoring, no universal set/passive weights";
	}
}
