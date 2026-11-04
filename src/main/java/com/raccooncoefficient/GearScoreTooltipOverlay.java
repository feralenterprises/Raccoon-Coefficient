package com.raccooncoefficient;

import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.GearScoreComparison;
import com.raccooncoefficient.score.GearScoreText;
import com.raccooncoefficient.score.WornItem;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.components.LayoutableRenderableEntity;
import net.runelite.client.ui.overlay.components.TooltipComponent;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;

/**
 * Adds one temporary tooltip while an equippable item is hovered.
 * The tooltip manager clears its list every frame, so this does not edit
 * the item's stored tooltip or remove lines added by other plugins.
 */
public class GearScoreTooltipOverlay extends Overlay
{
	private final Client client;
	private final RuneLiteItemStatSource stats;
	private final TooltipManager tooltipManager;
	private final RuneLiteConfig runeLiteConfig;
	private final GearScoreCalculator calculator;

	@Inject
	GearScoreTooltipOverlay(
		Client client,
		RuneLiteItemStatSource stats,
		RuneLiteItemNameSource names,
		TooltipManager tooltipManager,
		RuneLiteConfig runeLiteConfig)
	{
		this.client = client;
		this.stats = stats;
		this.calculator = GearScoreCalculator.wave3(names);
		this.tooltipManager = tooltipManager;
		this.runeLiteConfig = runeLiteConfig;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int itemId = hoveredItemId();
		if (itemId == EquipmentHover.NO_ITEM)
		{
			return null;
		}

		List<WornItem> worn = WornEquipment.read(client);
		if (worn == null)
		{
			return null; // no trustworthy equipment state; never preview a fake zero
		}
		GearScoreComparison comparison = calculator.compare(itemId, worn, stats);
		String text = GearScoreText.tooltip(comparison, WornEquipment.contains(worn, itemId));
		if (text != null)
		{
			tooltipManager.add(new Tooltip(new ReadableTooltip(text, runeLiteConfig.overlayBackgroundColor())));
		}
		return null;
	}

	private int hoveredItemId()
	{
		if (client.isMenuOpen() || client.getMenu() == null)
		{
			return EquipmentHover.NO_ITEM;
		}
		return HoveredEquipmentItem.resolve(client.getMenu().getMenuEntries());
	}

	/**
	 * RuneLite's default tooltip font has no arrows or unicode minus. Fall
	 * back to a font that can draw them for this tooltip only.
	 */
	private static final class ReadableTooltip implements LayoutableRenderableEntity
	{
		private final TooltipComponent component = new TooltipComponent();

		private ReadableTooltip(String text, Color background)
		{
			component.setText(text);
			component.setBackgroundColor(background);
		}

		@Override
		public Dimension render(Graphics2D graphics)
		{
			Font current = graphics.getFont();
			if (!current.canDisplay('\u2191') || !current.canDisplay('\u2212'))
			{
				graphics.setFont(new Font(Font.SANS_SERIF, current.getStyle(), Math.max(12, current.getSize())));
			}
			try
			{
				return component.render(graphics);
			}
			finally
			{
				graphics.setFont(current);
			}
		}

		@Override
		public Rectangle getBounds()
		{
			return component.getBounds();
		}

		@Override
		public void setPreferredLocation(Point position)
		{
			component.setPreferredLocation(position);
		}

		@Override
		public void setPreferredSize(Dimension dimension)
		{
			component.setPreferredSize(dimension);
		}
	}
}
