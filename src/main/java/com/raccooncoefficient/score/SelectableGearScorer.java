package com.raccooncoefficient.score;

import java.util.List;

/**
 * Picks the configured model on every call, so a config change applies on
 * the next refresh. Both calculations of one hover comparison use the SAME
 * model because compare() is delegated as a whole.
 */
public final class SelectableGearScorer implements GearScorer
{
	/** Supplies the currently configured model. */
	public interface ModelSource
	{
		ScoringModel current();
	}

	private final ModelSource models;
	private final GearScoreCalculator locked;
	private final GearScoreCalculator experimental;

	public SelectableGearScorer(ModelSource models, SetEffectDetector.ItemNameSource names)
	{
		if (models == null || names == null)
		{
			throw new IllegalArgumentException("model source and names are required");
		}
		this.models = models;
		this.locked = GearScoreCalculator.wave3(names);
		this.experimental = GearScoreCalculator.experimentalE1(names);
	}

	private GearScoreCalculator active()
	{
		ScoringModel model = models.current();
		return model == ScoringModel.EXPERIMENTAL_E1 ? experimental : locked;
	}

	@Override
	public GearScoreBreakdown score(List<WornItem> worn, ItemStatSource source)
	{
		return active().score(worn, source);
	}

	@Override
	public GearScoreComparison compare(int candidateItemId, List<WornItem> worn, ItemStatSource source)
	{
		return active().compare(candidateItemId, worn, source);
	}
}
