package com.raccooncoefficient;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.runelite.api.gameval.InterfaceID;

/**
 * Decides which hovered widget is an equippable item surface.
 * The numbers come from the menu entry's widget. This does not scan a bank.
 */
public final class EquipmentHover
{
	public static final int NO_ITEM = -1;

	private static final Set<Integer> EQUIPMENT_SLOTS = Collections.unmodifiableSet(new HashSet<Integer>(Arrays.asList(
		InterfaceID.Equipment.SLOT0,
		InterfaceID.Equipment.SLOT1,
		InterfaceID.Equipment.SLOT2,
		InterfaceID.Equipment.SLOT3,
		InterfaceID.Equipment.SLOT4,
		InterfaceID.Equipment.SLOT5,
		InterfaceID.Equipment.SLOT7,
		InterfaceID.Equipment.SLOT9,
		InterfaceID.Equipment.SLOT10,
		InterfaceID.Equipment.SLOT12,
		InterfaceID.Equipment.SLOT13,
		InterfaceID.Equipment.EXTRA_QUIVER_SLOT,
		InterfaceID.Equipment.EXTRA_QUIVER_AMMO)));

	private EquipmentHover()
	{
	}

	public static int itemId(int groupId, int widgetId, int parentId, int widgetItemId, int childItemId)
	{
		if (groupId == InterfaceID.WORNITEMS
			|| (groupId == InterfaceID.BANKMAIN && parentId == InterfaceID.Bankside.WORNOPS))
		{
			return positive(childItemId);
		}

		if (widgetId == InterfaceID.Inventory.ITEMS
			|| widgetId == InterfaceID.Bankmain.ITEMS
			|| widgetId == InterfaceID.SharedBank.ITEMS
			|| widgetId == InterfaceID.GeOffersSide.ITEMS
			|| groupId == InterfaceID.EQUIPMENT_SIDE
			|| groupId == InterfaceID.BANKSIDE
			|| groupId == InterfaceID.SHARED_BANK_SIDE
			|| EQUIPMENT_SLOTS.contains(Integer.valueOf(widgetId)))
		{
			return positive(widgetItemId);
		}

		return NO_ITEM;
	}

	private static int positive(int itemId)
	{
		return itemId > 0 ? itemId : NO_ITEM;
	}
}
