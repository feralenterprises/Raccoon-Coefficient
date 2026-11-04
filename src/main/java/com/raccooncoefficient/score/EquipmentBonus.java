package com.raccooncoefficient.score;

import java.math.BigDecimal;

/**
 * Verified bonuses for one equippable item. A zero field is a real zero.
 * Missing items are represented by a null bonus, not by this object.
 */
public final class EquipmentBonus
{
	private final int slot;
	private final boolean twoHanded;
	private final int attackStab;
	private final int attackSlash;
	private final int attackCrush;
	private final int attackMagic;
	private final int attackRanged;
	private final int defenceStab;
	private final int defenceSlash;
	private final int defenceCrush;
	private final int defenceMagic;
	private final int defenceRanged;
	private final int meleeStrength;
	private final int rangedStrength;
	private final BigDecimal magicDamagePercent;
	private final int prayer;

	public EquipmentBonus(
		int slot,
		boolean twoHanded,
		int attackStab,
		int attackSlash,
		int attackCrush,
		int attackMagic,
		int attackRanged,
		int defenceStab,
		int defenceSlash,
		int defenceCrush,
		int defenceMagic,
		int defenceRanged,
		int meleeStrength,
		int rangedStrength,
		BigDecimal magicDamagePercent,
		int prayer)
	{
		if (magicDamagePercent == null)
		{
			throw new IllegalArgumentException("magic damage is required");
		}
		this.slot = slot;
		this.twoHanded = twoHanded;
		this.attackStab = attackStab;
		this.attackSlash = attackSlash;
		this.attackCrush = attackCrush;
		this.attackMagic = attackMagic;
		this.attackRanged = attackRanged;
		this.defenceStab = defenceStab;
		this.defenceSlash = defenceSlash;
		this.defenceCrush = defenceCrush;
		this.defenceMagic = defenceMagic;
		this.defenceRanged = defenceRanged;
		this.meleeStrength = meleeStrength;
		this.rangedStrength = rangedStrength;
		this.magicDamagePercent = magicDamagePercent;
		this.prayer = prayer;
	}

	public int getSlot()
	{
		return slot;
	}

	public boolean isTwoHanded()
	{
		return twoHanded;
	}

	public int getAttackStab()
	{
		return attackStab;
	}

	public int getAttackSlash()
	{
		return attackSlash;
	}

	public int getAttackCrush()
	{
		return attackCrush;
	}

	public int getAttackMagic()
	{
		return attackMagic;
	}

	public int getAttackRanged()
	{
		return attackRanged;
	}

	public int getDefenceStab()
	{
		return defenceStab;
	}

	public int getDefenceSlash()
	{
		return defenceSlash;
	}

	public int getDefenceCrush()
	{
		return defenceCrush;
	}

	public int getDefenceMagic()
	{
		return defenceMagic;
	}

	public int getDefenceRanged()
	{
		return defenceRanged;
	}

	public int getMeleeStrength()
	{
		return meleeStrength;
	}

	public int getRangedStrength()
	{
		return rangedStrength;
	}

	public BigDecimal getMagicDamagePercent()
	{
		return magicDamagePercent;
	}

	public int getPrayer()
	{
		return prayer;
	}
}
