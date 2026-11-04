package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.List;

/**
 * Holds the Wave 3 scoring boundary: identified weapon passives have no
 * owner-approved conversion to generic Gear Score. Return null rather than
 * silently counting an unrated/contextual passive as zero.
 *
 * Wave 1's verified per-item bonuses are still displayed as a subtotal.
 */
public final class PendingWeaponPassiveBonus implements WeaponPassive
{
	private final WeaponPassiveDetector detector;
	private final SetEffectDetector.ItemNameSource identities;

	public PendingWeaponPassiveBonus(
		WeaponPassiveDetector detector,
		SetEffectDetector.ItemNameSource identities)
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
		for (WeaponPassiveDetector.Effect effect : detector.evaluate(equipment, identities))
		{
			if (effect.getState() != WeaponPassiveDetector.State.ABSENT)
			{
				return null;
			}
		}
		return BigDecimal.ZERO;
	}
}
