package com.raccooncoefficient.score;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the structural assumption shared by base stats and set/passive
 * detectors: a worn-equipment slot cannot contain two occupied items.
 *
 * RuneLite's normal worn container enumerates one item per slot. If a caller
 * supplies inconsistent snapshots, fail closed rather than count duplicate
 * equipment or claim a complete equipment-set effect.
 */
final class WornLayout
{
	private WornLayout()
	{
	}

	static boolean hasDuplicateOccupiedSlot(List<WornItem> worn)
	{
		if (worn == null)
		{
			return false; // unavailable is separately unknown
		}
		Set<Integer> slots = new HashSet<Integer>();
		for (WornItem item : worn)
		{
			if (item != null && item.getItemId() >= 0
				&& !slots.add(Integer.valueOf(item.getSlotIndex())))
			{
				return true;
			}
		}
		return false;
	}
}
