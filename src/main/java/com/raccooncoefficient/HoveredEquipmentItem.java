package com.raccooncoefficient;

import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;

/**
 * Resolves the current DEFAULT hovered item, not an unrelated lower-priority
 * action hidden underneath it. Uses RuneLite metadata, without editing menus
 * or sharing state/code with HoverLite.
 */
final class HoveredEquipmentItem
{
	private HoveredEquipmentItem()
	{
	}

	static int resolve(MenuEntry[] entries)
	{
		if (entries == null || entries.length == 0)
		{
			return EquipmentHover.NO_ITEM;
		}

		// Match the actual default action shown to the player.
		// Searching backward for *any* eligible widget can accidentally label
		// a background item when a non-item/menu action is currently on top.
		MenuEntry entry = entries[entries.length - 1];
		if (!ItemHoverAction.isItemAction(entry))
		{
			return EquipmentHover.NO_ITEM;
		}

		Widget widget = entry.getWidget();
		if (widget == null)
		{
			return EquipmentHover.NO_ITEM;
		}

		int widgetId = widget.getId();
		int group = WidgetUtil.componentToInterface(widgetId);
		return EquipmentHover.menuItemId(group, widgetId,
			widget.getParentId(), entry.getItemId(), widget.getItemId());
	}
}
