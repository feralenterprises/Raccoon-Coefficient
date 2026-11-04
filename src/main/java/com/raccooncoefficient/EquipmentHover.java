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

	/**
	 * Resolve a menu entry only if its widget is a known inventory/equipment
	 * surface. Prefer the menu's own item ID, which identifies the specific
	 * item under the cursor, over a container or generic parent widget ID.
	 * A direct child item widget may have its parent as the item container.
	 * Unrelated menus and missing IDs are never guessed.
	 */
	public static int menuItemId(
		int groupId,
		int widgetId,
		int parentId,
		int menuItemId,
		int widgetItemId)
	{
		if (!supportedSurface(groupId, widgetId, parentId))
		{
			return NO_ITEM;
		}
		if (menuItemId > 0)
		{
			return menuItemId;
		}
		// A container widget (for example Inventory.ITEMS) is not itself a
		// uniquely hovered item. If the menu doesn't provide an item ID,
		// use the widget's ID only for explicit slot or item-child surfaces.
		// Unknown is safer than showing the first/stale item in a container.
		if (explicitItemWidget(widgetId, parentId, groupId))
		{
			return positive(widgetItemId);
		}
		return NO_ITEM;
	}

	private static boolean explicitItemWidget(int widgetId, int parentId, int groupId)
	{
		return EQUIPMENT_SLOTS.contains(Integer.valueOf(widgetId))
			|| EQUIPMENT_SLOTS.contains(Integer.valueOf(parentId))
			|| parentId == InterfaceID.Inventory.ITEMS
			|| parentId == InterfaceID.Bankmain.ITEMS
			|| parentId == InterfaceID.SharedBank.ITEMS
			|| parentId == InterfaceID.GeOffersSide.ITEMS
			|| parentId == InterfaceID.Bankside.WORNOPS
			|| groupId == InterfaceID.WORNITEMS
			|| groupId == InterfaceID.EQUIPMENT_SIDE
			|| groupId == InterfaceID.BANKSIDE
			|| groupId == InterfaceID.SHARED_BANK_SIDE;
	}

	private static boolean supportedSurface(int groupId, int widgetId, int parentId)
	{
		if (widgetId == InterfaceID.Inventory.ITEMS || parentId == InterfaceID.Inventory.ITEMS
			|| widgetId == InterfaceID.Bankmain.ITEMS || parentId == InterfaceID.Bankmain.ITEMS
			|| widgetId == InterfaceID.SharedBank.ITEMS || parentId == InterfaceID.SharedBank.ITEMS
			|| widgetId == InterfaceID.GeOffersSide.ITEMS || parentId == InterfaceID.GeOffersSide.ITEMS
			|| EQUIPMENT_SLOTS.contains(Integer.valueOf(widgetId))
			|| EQUIPMENT_SLOTS.contains(Integer.valueOf(parentId)))
		{
			return true;
		}
		// Worn equipment, side equipment, and bank side slots are item
		// surfaces, but their dynamic child IDs can change across UIs.
		return groupId == InterfaceID.WORNITEMS
			|| groupId == InterfaceID.EQUIPMENT_SIDE
			|| groupId == InterfaceID.BANKSIDE
			|| groupId == InterfaceID.SHARED_BANK_SIDE
			|| (groupId == InterfaceID.BANKMAIN
				&& parentId == InterfaceID.Bankside.WORNOPS);
	}

	private static int positive(int itemId)
	{
		return itemId > 0 ? itemId : NO_ITEM;
	}
}
