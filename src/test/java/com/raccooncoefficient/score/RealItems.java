package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GENERATED test data: real item bonuses copied from the OSRS Wiki DPS
 * calculator dataset (weirdgloop/osrs-dps-calc cdn/json/equipment.json,
 * commit 4c9a0bd056106eb244ece0251295c055a8c2aa37, fetched 2026-11-07).
 * Ids are that dataset's ids. Generator: tools/e1-reference/make_fixture.py. Amulet of the damned (id 12853)
 * is not in that dataset; its stats are from w/Amulet_of_the_damned.
 * Not derived from plugin output.
 */
final class RealItems implements ItemStatSource, SetEffectDetector.ItemNameSource
{
	private final Map<Integer, EquipmentBonus> stats = new HashMap<Integer, EquipmentBonus>();
	private final Map<Integer, String> names = new HashMap<Integer, String>();

	RealItems()
	{
		put(861, "Magic shortbow", 3, true, 0, 0, 0, 0, 69, 0, 0, 0, 0, 0, 0, 0, "0.0", 0, 4);
		put(892, "Rune arrow", 13, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 49, "0.0", 0, 0);
		put(1319, "Rune 2h sword", 3, true, -4, 69, 50, -4, 0, 0, 0, 0, 0, -1, 70, 0, "0.0", 0, 7);
		put(1712, "Amulet of glory", 2, false, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 6, 0, "0.0", 3, 0);
		put(2491, "Black d'hide vambraces", 9, false, 0, 0, 0, -10, 11, 4, 5, 5, 8, 0, 0, 0, "0.0", 0, 0);
		put(2497, "Black d'hide chaps", 7, false, 0, 0, 0, -10, 17, 18, 20, 26, 23, 26, 0, 0, "0.0", 0, 0);
		put(2503, "Black d'hide body", 4, false, 0, 0, 0, -15, 30, 30, 38, 45, 45, 50, 0, 0, "0.0", 0, 0);
		put(2581, "Robin Hood hat", 0, false, 0, 0, 0, -10, 8, 4, 6, 8, 4, 4, 0, 0, "0.0", 0, 0);
		put(2866, "Ogre arrow", 13, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 22, "0.0", 0, 0);
		put(4151, "Abyssal whip", 3, false, 0, 82, 0, 0, 0, 0, 0, 0, 0, 0, 82, 0, "0.0", 0, 4);
		put(4708, "Ahrim's hood", 0, false, 0, 0, 0, 6, -2, 15, 13, 16, 6, 0, 0, 0, "1.0", 0, 0);
		put(4710, "Ahrim's staff", 3, false, 12, -1, 65, 15, 0, 3, 5, 2, 15, 0, 68, 0, "5.0", 0, 6);
		put(4712, "Ahrim's robetop", 4, false, 0, 0, 0, 30, -10, 52, 37, 63, 30, 0, 0, 0, "1.0", 0, 0);
		put(4714, "Ahrim's robeskirt", 7, false, 0, 0, 0, 22, -7, 33, 30, 36, 22, 0, 0, 0, "1.0", 0, 0);
		put(4716, "Dharok's helm", 0, false, 0, 0, 0, -3, -1, 45, 48, 44, -1, 51, 0, 0, "0.0", 0, 0);
		put(4718, "Dharok's greataxe", 3, true, -4, 103, 95, -4, 0, 0, 0, 0, 0, -1, 105, 0, "0.0", 0, 7);
		put(4720, "Dharok's platebody", 4, false, 0, 0, 0, -30, -10, 122, 120, 107, -6, 132, 0, 0, "0.0", 0, 0);
		put(4722, "Dharok's platelegs", 7, false, 0, 0, 0, -21, -11, 85, 82, 83, -4, 92, 0, 0, "0.0", 0, 0);
		put(4732, "Karil's coif", 0, false, 0, 0, 0, -1, 7, 6, 9, 12, 6, 10, 0, 0, "0.0", 0, 0);
		put(4734, "Karil's crossbow", 3, true, 0, 0, 0, 0, 84, 0, 0, 0, 0, 0, 0, 0, "0.0", 0, 4);
		put(4736, "Karil's leathertop", 4, false, 0, 0, 0, -15, 30, 47, 42, 50, 65, 57, 0, 0, "0.0", 0, 0);
		put(4738, "Karil's leatherskirt", 7, false, 0, 0, 0, -10, 17, 26, 20, 28, 35, 33, 0, 0, "0.0", 0, 0);
		put(4740, "Bolt rack", 13, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 55, "0.0", 0, 0);
		put(4753, "Verac's helm", 0, false, 0, 0, 0, -6, -2, 55, 58, 54, 0, 56, 0, 0, "0.0", 3, 0);
		put(4755, "Verac's flail", 3, true, 68, -2, 82, 0, 0, 0, 0, 0, 0, 0, 72, 0, "0.0", 6, 5);
		put(4757, "Verac's brassard", 4, false, 0, 0, 0, -6, -2, 81, 95, 85, 0, 81, 0, 0, "0.0", 5, 0);
		put(4759, "Verac's plateskirt", 7, false, 0, 0, 0, -21, -11, 85, 82, 83, 0, 84, 0, 0, "0.0", 4, 0);
		put(4827, "Comp ogre bow", 3, true, 0, 0, 0, 0, 38, 0, 0, 0, 0, 0, 0, 0, "0.0", 0, 5);
		put(4980, "Verac's helm 0", 0, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "0.0", 0, 0);
		put(6570, "Fire cape", 1, false, 1, 1, 1, 1, 1, 11, 11, 11, 11, 11, 4, 0, "0.0", 2, 0);
		put(6585, "Amulet of fury", 2, false, 10, 10, 10, 10, 10, 15, 15, 15, 15, 15, 8, 0, "0.0", 5, 0);
		put(6889, "Mage's book", 5, false, 0, 0, 0, 15, 0, 0, 0, 0, 15, 0, 0, 0, "2.0", 0, 0);
		put(7462, "Barrows gloves", 9, false, 12, 12, 12, 6, 12, 12, 12, 12, 6, 12, 12, 0, "0.0", 0, 0);
		put(8839, "Void knight top", 4, false, 0, 0, 0, 0, 0, 45, 45, 45, 45, 45, 0, 0, "0.0", 0, 0);
		put(8840, "Void knight robe", 7, false, 0, 0, 0, 0, 0, 30, 30, 30, 30, 30, 0, 0, "0.0", 0, 0);
		put(8842, "Void knight gloves", 9, false, 0, 0, 0, 0, 0, 6, 6, 6, 4, 6, 0, 0, "0.0", 0, 0);
		put(9144, "Runite bolts", 13, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 115, "0.0", 0, 0);
		put(9185, "Rune crossbow", 3, false, 0, 0, 0, 0, 90, 0, 0, 0, 0, 0, 0, 0, "0.0", 0, 6);
		put(10828, "Helm of Neitiznot", 0, false, 0, 0, 0, 0, 0, 31, 29, 34, 3, 30, 3, 0, "0.0", 3, 0);
		put(11212, "Dragon arrow", 13, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 60, "0.0", 0, 0);
		put(11663, "Void mage helm", 0, false, 0, 0, 0, 0, 0, 6, 6, 6, 6, 6, 0, 0, "0.0", 0, 0);
		put(11664, "Void ranger helm", 0, false, 0, 0, 0, 0, 0, 6, 6, 6, 6, 6, 0, 0, "0.0", 0, 0);
		put(11665, "Void melee helm", 0, false, 0, 0, 0, 0, 0, 6, 6, 6, 6, 6, 0, 0, "0.0", 0, 0);
		put(11773, "Berserker ring (i)", 12, false, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 8, 0, "0.0", 0, 0);
		put(11802, "Armadyl godsword", 3, true, 0, 132, 80, 0, 0, 0, 0, 0, 0, 0, 132, 0, "0.0", 8, 6);
		put(11832, "Bandos chestplate", 4, false, 0, 0, 0, -15, -10, 98, 93, 105, -6, 133, 4, 0, "0.0", 1, 0);
		put(11834, "Bandos tassets", 7, false, 0, 0, 0, -21, -7, 71, 63, 66, -4, 93, 2, 0, "0.0", 1, 0);
		put(11840, "Dragon boots", 10, false, 0, 0, 0, -3, -1, 16, 17, 18, 0, 0, 4, 0, "0.0", 0, 0);
		put(11905, "Trident of the Seas", 3, false, 0, 0, 0, 15, 0, 2, 3, 1, 15, 0, 0, 0, "0.0", 0, 4);
		put(12002, "Occult necklace", 2, false, 0, 0, 0, 12, 0, 0, 0, 0, 0, 0, 0, 0, "5.0", 2, 0);
		put(12853, "Amulet of the damned", 2, false, 10, 10, 10, 10, 10, 3, 3, 3, 3, 3, 6, 0, "0.0", 3, 0);
		put(12926, "Toxic blowpipe", 3, true, 0, 0, 0, 0, 30, 0, 0, 0, 0, 0, 0, 20, "0.0", 0, 3);
		put(12954, "Dragon defender", 5, false, 25, 24, 23, -3, -2, 25, 24, 23, -3, -2, 6, 0, "0.0", 0, 0);
		put(13072, "Elite void top", 4, false, 0, 0, 0, 0, 0, 45, 45, 45, 45, 45, 0, 0, "0.0", 3, 0);
		put(13073, "Elite void robe", 7, false, 0, 0, 0, 0, 0, 30, 30, 30, 30, 30, 0, 0, "0.0", 3, 0);
		put(20997, "Twisted bow", 3, true, 0, 0, 0, 0, 70, 0, 0, 0, 0, 0, 0, 20, "0.0", 0, 6);
		put(21006, "Kodai wand", 3, false, 0, 0, 0, 28, 0, 0, 3, 3, 20, 0, 0, 0, "15.0", 0, 4);
		put(22324, "Ghrazi rapier", 3, false, 100, 55, 0, 0, 0, 0, 0, 0, 0, 0, 93, 0, "0.0", 0, 4);
		put(23971, "Crystal helm", 0, false, 0, 0, 0, -10, 9, 12, 8, 14, 10, 18, 0, 0, "0.0", 2, 0);
		put(23975, "Crystal body", 4, false, 0, 0, 0, -18, 31, 46, 38, 48, 44, 68, 0, 0, "0.0", 3, 0);
		put(23979, "Crystal legs", 7, false, 0, 0, 0, -12, 18, 26, 21, 30, 34, 38, 0, 0, "0.0", 2, 0);
		put(25865, "Bow of Faerdhinen", 3, true, 0, 0, 0, 0, 128, 0, 0, 0, 0, 0, 0, 106, "0.0", 0, 5);
		put(25867, "Bow of Faerdhinen (c)", 3, true, 0, 0, 0, 0, 128, 0, 0, 0, 0, 0, 0, 106, "0.0", 0, 5);
		put(26219, "Osmumten's fang", 3, false, 105, 75, 0, 0, 0, 0, 0, 0, 0, 0, 103, 0, "0.0", 0, 5);
		put(27275, "Tumeken's shadow", 3, true, 0, 0, 0, 35, 0, 0, 0, 0, 20, 0, 0, 0, "0.0", 1, 5);
		put(27610, "Venator bow", 3, true, 0, 0, 0, 0, 90, 0, 0, 0, 0, 0, 0, 25, "0.0", 0, 5);
		put(27769, "Crystal body (Amlodd)", 4, false, 0, 0, 0, -18, 31, 46, 38, 48, 44, 68, 0, 0, "0.0", 3, 0);
		put(30634, "Twinflame staff", 3, false, 7, -1, 28, 12, 0, 2, 3, 1, 12, 0, 22, 0, "0.0", 0, 6);
	}

	private void put(int id, String name, int slot, boolean twoHanded, int as, int asl, int ac, int am, int ar,
		int ds, int dsl, int dc, int dm, int dr, int str, int rstr, String mdmg, int prayer, int speed)
	{
		stats.put(id, new EquipmentBonus(slot, twoHanded, as, asl, ac, am, ar, ds, dsl, dc, dm, dr,
			str, rstr, new BigDecimal(mdmg), prayer, speed));
		names.put(id, name);
	}

	/** Same item under another (variant) name, e.g. an ornament kit. */
	void alias(int newId, int sourceId, String name)
	{
		stats.put(newId, stats.get(sourceId));
		names.put(newId, name);
	}

	void forgetStats(int id)
	{
		stats.remove(id);
	}

	@Override
	public EquipmentBonus getVerifiedBonus(int itemId)
	{
		return stats.get(itemId);
	}

	@Override
	public String nameOf(int itemId)
	{
		return names.get(itemId);
	}

	List<WornItem> kit(int... ids)
	{
		List<WornItem> worn = new ArrayList<WornItem>();
		for (int id : ids)
		{
			worn.add(new WornItem(stats.get(id).getSlot(), id));
		}
		return worn;
	}
}
