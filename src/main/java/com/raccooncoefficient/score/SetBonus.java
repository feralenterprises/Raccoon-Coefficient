package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wave 2 extension point. Not used by the Wave 1 calculator.
 * <p>
 * Intended verified set bonuses include Elite Void, Barrows, Obsidian, and
 * other equipment sets whose bonus is not already present on the individual
 * items. Return null when the bonus cannot be verified. Do not return zero
 * for an unknown set.
 */
public interface SetBonus
{
	BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats);
}
