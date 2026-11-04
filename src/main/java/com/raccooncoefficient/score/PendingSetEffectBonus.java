package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wave 2 bridge. Detect real equipped set combinations but never invent
 * numeric Gear Score coefficients. A detected or unresolved effect makes the
 * displayed aggregate a labeled base-only subtotal until the owner approves
 * a numeric mapping. Wave 1's base coefficients remain unchanged.
 */
public final class PendingSetEffectBonus implements SetBonus
{
	private final SetEffectDetector detector;
	private final SetEffectDetector.ItemNameSource identities;

	public PendingSetEffectBonus(SetEffectDetector detector, SetEffectDetector.ItemNameSource identities)
	{
		if (detector == null || identities == null)
		{
			throw new IllegalArgumentException("detector and identities are required");
		}
		this.detector = detector;
		this.identities = identities;
	}

	@Override
	public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
	{
		for (SetEffectDetector.Effect effect : detector.evaluate(equipment, identities))
		{
			if (effect.getState() != SetEffectDetector.State.INACTIVE)
			{
				return null; // active but unrated, or unknown: never invent zero
			}
		}
		return BigDecimal.ZERO;
	}
}
