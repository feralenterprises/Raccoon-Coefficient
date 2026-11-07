package com.raccooncoefficient.score;

/** Which scoring contract is displayed. LOCKED_V1 is the default and the only approved one. */
public enum ScoringModel
{
	/** Approved base formula; detected set/passive effects mark the total "?" (unrated). */
	LOCKED_V1("Locked v1 (approved)"),
	/** PROPOSED, not approved: v1 base plus explained E1 adjustments. Labelled "E1" wherever shown. */
	EXPERIMENTAL_E1("Experimental E1 (proposed)");

	private final String displayName;

	ScoringModel(String displayName)
	{
		this.displayName = displayName;
	}

	@Override
	public String toString()
	{
		return displayName;
	}
}
