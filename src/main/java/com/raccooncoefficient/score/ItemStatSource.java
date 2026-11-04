package com.raccooncoefficient.score;

/**
 * Looks up verified equipment bonuses.
 * <p>
 * Returning null means the item has no verified equipment data. Callers must
 * not treat that as a confirmed zero.
 */
public interface ItemStatSource
{
	EquipmentBonus getVerifiedBonus(int itemId);
}
