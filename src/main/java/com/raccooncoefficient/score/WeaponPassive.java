package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.List;

/**
 * Wave 3 extension point. Not used by the Wave 1 calculator.
 * <p>
 * Intended verified general-purpose passives include Osmumten's fang, the
 * Venator bow, and the Twinflame staff. Monster-specific modifiers stay out
 * of this interface. Return null when the passive cannot be verified.
 */
public interface WeaponPassive
{
	BigDecimal gearScore(List<WornItem> equipment, ItemStatSource stats);
}
