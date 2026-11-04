package com.raccooncoefficient;

import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

/**
 * Adds exactly one Raccoon score row to the current RuneLite tooltip queue.
 *
 * Other plugins own their text/components. Never clear, reorder, replace,
 * or remove them. The manager clears its queue after rendering each frame.
 */
final class ScoreTooltipQueue
{
	/** Marker reserved for this plugin's own tooltip components. */
	interface ScoreComponent extends LayoutableRenderableEntity
	{
	}

	private ScoreTooltipQueue()
	{
	}

	static void addOnce(TooltipManager manager, ScoreComponent scoreComponent)
	{
		if (manager == null || scoreComponent == null)
		{
			return;
		}

		for (int i = 0; i < manager.getTooltips().size(); i++)
		{
			Tooltip tooltip = manager.getTooltips().get(i);
			if (tooltip != null && tooltip.getComponent() instanceof ScoreComponent)
			{
				// A repeated draw hook may see fresher hovered-item evidence.
				// Replace ONLY the component owned by this plugin, in-place,
				// so the label is current without duplicating it or changing
				// third-party tooltip order.
				manager.getTooltips().set(i, new Tooltip(scoreComponent));
				return;
			}
		}
		manager.add(new Tooltip(scoreComponent));
	}
}
