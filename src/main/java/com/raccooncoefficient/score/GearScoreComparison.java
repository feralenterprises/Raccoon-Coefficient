package com.raccooncoefficient.score;

import java.math.BigDecimal;

/** An item's own score and the change in the displayed total if it is equipped. */
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
	private final Verdict verdict;

	private GearScoreComparison(boolean itemKnown, BigDecimal itemScore, BigDecimal delta, Verdict verdict)
	{
		this.itemKnown = itemKnown;
		this.itemScore = itemScore;
		this.delta = delta;
		this.verdict = verdict;
	}

	public static GearScoreComparison unknownItem()
	{
		return new GearScoreComparison(false, null, null, Verdict.UNKNOWN);
	}

	public static GearScoreComparison knownItem(BigDecimal itemScore, BigDecimal delta)
	{
		Verdict verdict;
		int sign = delta.signum();
		if (sign > 0)
		{
			verdict = Verdict.BETTER;
		}
		else if (sign < 0)
		{
			verdict = Verdict.WORSE;
		}
		else
		{
			verdict = Verdict.EQUAL;
		}
		return new GearScoreComparison(true, itemScore, delta, verdict);
	}

	public static GearScoreComparison knownItemUnknownDelta(BigDecimal itemScore)
	{
		return new GearScoreComparison(true, itemScore, null, Verdict.UNKNOWN);
	}

	public boolean isItemKnown()
	{
		return itemKnown;
	}

	public BigDecimal getItemScore()
	{
		return itemScore;
	}

	public BigDecimal getDelta()
	{
		return delta;
	}

	public Verdict getVerdict()
	{
		return verdict;
	}
}
