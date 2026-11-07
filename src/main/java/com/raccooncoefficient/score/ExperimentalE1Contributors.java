package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.List;

/**
 * Adapts {@link ExperimentalE1} to the calculator's set and weapon extension
 * points. Each returns null (unknown) whenever E1 cannot evaluate the layout,
 * so an unknown input never becomes a confident 0.
 */
final class ExperimentalE1Contributors
{
	private ExperimentalE1Contributors()
	{
	}

	static final class Sets implements SetBonus
	{
		private final ExperimentalE1 model;
		private final SetEffectDetector.ItemNameSource names;

		Sets(ExperimentalE1 model, SetEffectDetector.ItemNameSource names)
		{
			this.model = model;
			this.names = names;
		}

		@Override
		public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
		{
			ExperimentalE1.Result result = model.evaluate(equipment, stats, names);
			return result.isKnown() ? result.sum("set.") : null;
		}
	}

	static final class Weapon implements WeaponPassive
	{
		private final ExperimentalE1 model;
		private final SetEffectDetector.ItemNameSource names;

		Weapon(ExperimentalE1 model, SetEffectDetector.ItemNameSource names)
		{
			this.model = model;
			this.names = names;
		}

		@Override
		public BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats)
		{
			ExperimentalE1.Result result = model.evaluate(equipment, stats, names);
			return result.isKnown() ? result.sum("weapon.").add(result.sum("passive.")) : null;
		}
	}
}
