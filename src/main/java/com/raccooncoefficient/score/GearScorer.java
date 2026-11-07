package com.raccooncoefficient.score;

import java.util.List;

/** What the UI needs from a scoring model. */
public interface GearScorer
{
	GearScoreBreakdown score(List<WornItem> worn, ItemStatSource source);

	GearScoreComparison compare(int candidateItemId, List<WornItem> worn, ItemStatSource source);
}
