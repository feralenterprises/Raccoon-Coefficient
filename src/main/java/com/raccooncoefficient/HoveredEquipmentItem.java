package com.raccooncoefficient;

import net.runelite.api.MenuEntry;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetUtil;

/**
 * Resolves the hovered equippable item without modifying RuneLite's menu.
 *
 * MenuEntry.getItemId() is the authoritative item identity when provided.
 * Widget.getItemId() is a guarded fallback. Never assume a hard-coded child
 * index is the item: that can refer to an entirely different UI element.
 */
final class HoveredEquipmentItem
{
	private HoveredEquipmentItem()
	{
	}

	static int resolve(MenuEntry[] entries)
	{
		if (entries == null)
		{
			return EquipmentHover.NO_ITEM;
		}

		// RuneLite menus store the top-priority entries at the end. Skip
		// synthetic/plugin entries with no widget, but do not mutate them.
		for (int i = entries.length - 1; i >= 0; i--)
		{
			MenuEntry entry = entries[i];
			if (entry == null)
			{
				continue;
			}
			Widget widget = entry.getWidget();
			if (widget == null)
			{
				continue;
			}

			int widgetId = widget.getId();
			int group = WidgetUtil.componentToInterface(widgetId);
			int resolved = EquipmentHover.menuItemId(
				group, widgetId, widget.getParentId(),
				entry.getItemId(), widget.getItemId());
			if (resolved != EquipmentHover.NO_ITEM)
			{
				return resolved;
			}
		}

		return EquipmentHover.NO_ITEM;
	}
}
