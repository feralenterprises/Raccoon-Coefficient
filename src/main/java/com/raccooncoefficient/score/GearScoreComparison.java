package com.raccooncoefficient.score;

import java.math.BigDecimal;

/**
 * Item's own verified stat score, optional complete-total delta, and an
 * explicitly labeled base-only delta when effects/other inputs are unknown.
 */
public final class GearScoreComparison
{
	public enum Verdict
	{
		BETTER,
		WORSE,
		EQUAL,
		UNKNOWN
	}

	private final boolean itemKnown;
	private final BigDecimal itemScore;
	private final BigDecimal delta;
	private final BigDecimal baseDelta;
	private final Verdict verdict;

	private GearScoreComparison(
		boolean itemKnown,
		BigDecimal itemScore,
		BigDecimal delta,
		BigDecimal baseDelta,
		Verdict verdict)
	{
		this.itemKnown = itemKnown;
		this.itemScore = itemScore;
		this.delta = delta;
		this.baseDelta = baseDelta;
		this.verdict = verdict;
	}

	public static GearScoreComparison unknownItem()
	{
		return new GearScoreComparison(false, null, null, null, Verdict.UNKNOWN);
	}

	public static GearScoreComparison knownItem(BigDecimal itemScore, BigDecimal delta)
	{
		return new GearScoreComparison(true, itemScore, delta, null, verdict(delta));
	}

	public static GearScoreComparison knownItemUnknownDelta(BigDecimal itemScore)
	{
		return new GearScoreComparison(true, itemScore, null, null, Verdict.UNKNOWN);
	}

	/**
	 * A verified change in the fixed Wave 1 item-stat portion only.
	 * Never masquerade as the overall score change when set/passive
	 * coefficients, the equipment state, or other components are unknown.
	 */
	public static GearScoreComparison knownItemBaseOnlyDelta(BigDecimal itemScore, BigDecimal baseDelta)
	{
		if (itemScore == null || baseDelta == null)
		{
			throw new IllegalArgumentException("Item score and base delta are required");
		}
		return new GearScoreComparison(true, itemScore, null, baseDelta, Verdict.UNKNOWN);
	}

	private static Verdict verdict(BigDecimal delta)
	{
		if (delta.signum() > 0)
		{
			return Verdict.BETTER;
		}
		if (delta.signum() < 0)
		{
			return Verdict.WORSE;
		}
		return Verdict.EQUAL;
	}

	public boolean isItemKnown()
	{
		return itemKnown;
	}

	public BigDecimal getItemScore()
	{
		return itemScore;
	}

	/** Verified change in the complete scoring model, or null if unknown. */
	public BigDecimal getDelta()
	{
		return delta;
	}

	/** Base-only delta (NOT complete), or null when unavailable/not needed. */
	public BigDecimal getBaseDelta()
	{
		return baseDelta;
	}

	public Verdict getVerdict()
	{
		return verdict;
	}
}
