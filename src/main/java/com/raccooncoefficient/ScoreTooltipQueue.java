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

		for (Tooltip tooltip : manager.getTooltips())
		{
			if (tooltip != null && tooltip.getComponent() instanceof ScoreComponent)
			{
				// A second render within this tooltip cycle must not stack an
				// identical-looking Raccoon score line.
				return;
			}
		}
		manager.add(new Tooltip(scoreComponent));
	}
}
