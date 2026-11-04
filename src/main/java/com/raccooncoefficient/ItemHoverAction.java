package com.raccooncoefficient;

import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.WidgetUtil;

/**
 * Independent item-action policy grounded in RuneLite's menu entry metadata.
 *
 * HoverLite inspired the idea to validate the top action and its source, but
 * no HoverLite source, dependency, state, or configuration is imported.
 * The widget identity is checked separately by EquipmentHover.
 */
final class ItemHoverAction
{
	private ItemHoverAction()
	{
	}

	static boolean isItemAction(MenuEntry entry)
	{
		if (entry == null || entry.getType() == null)
		{
			return false;
		}

		MenuAction action = entry.getType();
		switch (action)
		{
			// These menu actions explicitly describe item interactions.
			case ITEM_USE_ON_ITEM:
			case WIDGET_USE_ON_ITEM:
			case ITEM_FIRST_OPTION:
			case ITEM_SECOND_OPTION:
			case ITEM_THIRD_OPTION:
			case ITEM_FOURTH_OPTION:
			case ITEM_FIFTH_OPTION:
			case ITEM_USE:
			case EXAMINE_ITEM:
				return true;

			// CC_OP and widget actions can describe ordinary buttons as well.
			// Confirm the action comes from a supported item interface.
			case CC_OP:
			case WIDGET_FIRST_OPTION:
			case WIDGET_SECOND_OPTION:
			case WIDGET_THIRD_OPTION:
			case WIDGET_FOURTH_OPTION:
			case WIDGET_FIFTH_OPTION:
			case WIDGET_TARGET:
			case WIDGET_TARGET_ON_WIDGET:
			case RUNELITE_WIDGET:
				return isItemGroup(WidgetUtil.componentToInterface(entry.getParam1()));

			// In particular, RUNELITE_OVERLAY/CONFIG, LOW_PRIORITY, NPC,
			// object, ground-item and player actions cannot select a worn or
			// inventory item for our score tooltip.
			default:
				return false;
		}
	}

	private static boolean isItemGroup(int group)
	{
		return group == InterfaceID.INVENTORY
			|| group == InterfaceID.WORNITEMS
			|| group == InterfaceID.EQUIPMENT_SIDE
			|| group == InterfaceID.BANKSIDE
			|| group == InterfaceID.SHARED_BANK_SIDE
			|| group == InterfaceID.BANKMAIN
			|| group == InterfaceID.SHARED_BANK
			|| group == InterfaceID.GE_OFFERS_SIDE
			|| group == InterfaceID.GE_OFFERS
			|| group == InterfaceID.EQUIPMENT;
	}
}
