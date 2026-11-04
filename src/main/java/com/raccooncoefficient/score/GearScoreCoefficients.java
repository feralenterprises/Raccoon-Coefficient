package com.raccooncoefficient.score;

import java.math.BigDecimal;

/**
 * Wave 1 weights. Build a new instance to calibrate the score without changing
 * how bonuses are summed.
 */
public final class GearScoreCoefficients
{
	private final BigDecimal attackPerPoint;
	private final BigDecimal defencePerPoint;
	private final BigDecimal meleeStrengthPerPoint;
	private final BigDecimal rangedStrengthPerPoint;
	private final BigDecimal magicDamagePerPoint;
	private final BigDecimal prayerPerPoint;

	public GearScoreCoefficients(
		BigDecimal attackPerPoint,
		BigDecimal defencePerPoint,
		BigDecimal meleeStrengthPerPoint,
		BigDecimal rangedStrengthPerPoint,
		BigDecimal magicDamagePerPoint,
		BigDecimal prayerPerPoint)
	{
		this.attackPerPoint = attackPerPoint;
		this.defencePerPoint = defencePerPoint;
		this.meleeStrengthPerPoint = meleeStrengthPerPoint;
		this.rangedStrengthPerPoint = rangedStrengthPerPoint;
		this.magicDamagePerPoint = magicDamagePerPoint;
		this.prayerPerPoint = prayerPerPoint;
	}

	public static GearScoreCoefficients wave1()
	{
		return new GearScoreCoefficients(
			new BigDecimal("0.1"),
			new BigDecimal("0.1"),
			new BigDecimal("1.0"),
			new BigDecimal("1.0"),
			new BigDecimal("1.0"),
			new BigDecimal("0.5"));
	}

	public BigDecimal getAttackPerPoint()
	{
		return attackPerPoint;
	}

	public BigDecimal getDefencePerPoint()
	{
		return defencePerPoint;
	}

	public BigDecimal getMeleeStrengthPerPoint()
	{
		return meleeStrengthPerPoint;
	}

	public BigDecimal getRangedStrengthPerPoint()
	{
		return rangedStrengthPerPoint;
	}

	public BigDecimal getMagicDamagePerPoint()
	{
		return magicDamagePerPoint;
	}

	public BigDecimal getPrayerPerPoint()
	{
		return prayerPerPoint;
	}
}
