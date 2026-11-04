package com.raccooncoefficient;

import com.raccooncoefficient.score.GearScoreBreakdown;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.GearScoreText;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

/**
 * Draws one score line in the empty left side of the equipment stats title bar.
 * The overlay is hooked to interface 84, so client scripts that rebuild that
 * interface cannot wipe it, and later dialogs still paint over it.
 */
public class EquipmentScoreOverlay extends Overlay
{
	private final Client client;
	private final RuneLiteItemStatSource stats;
	private final GearScoreCalculator calculator;

	@Inject
	EquipmentScoreOverlay(Client client, RuneLiteItemStatSource stats, RuneLiteItemNameSource names)
	{
		this.client = client;
		this.stats = stats;
		this.calculator = GearScoreCalculator.wave3(names);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.MANUAL);
		drawAfterInterface(InterfaceID.EQUIPMENT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget root = client.getWidget(InterfaceID.Equipment.UNIVERSE);
		if (root == null)
		{
			return null;
		}

		Rectangle bounds = root.getBounds();
		if (bounds == null || bounds.width < 80 || bounds.height < 80)
		{
			return null;
		}

		java.util.List<com.raccooncoefficient.score.WornItem> worn = WornEquipment.read(client);
		if (worn == null)
		{
			drawLine(graphics, bounds, root, "Gear Score: ?");
			return null;
		}
		GearScoreBreakdown score = calculator.score(worn, stats);
		drawLine(graphics, bounds, root, GearScoreText.equipmentLine(score));
		return null;
	}

	private void drawLine(Graphics2D graphics, Rectangle bounds, Widget root, String line)
	{
		Font previousFont = graphics.getFont();
		Color previousColor = graphics.getColor();
		Rectangle previousClip = graphics.getClipBounds();
		try
		{
			Font font = FontManager.getRunescapeSmallFont();
			graphics.setFont(font);

			String compact = GearScoreText.compactEquipmentLine(line);
			Widget title = findTitle(root, bounds, 0);
			Rectangle titleBounds = title == null ? null : title.getBounds();
			GearScoreHeaderLayout.Placement placement = GearScoreHeaderLayout.choose(
				bounds, titleBounds,
				graphics.getFontMetrics().stringWidth(line),
				graphics.getFontMetrics().stringWidth(compact));
			if (placement.getLabel() == GearScoreHeaderLayout.Label.HIDDEN)
			{
				return; // don't collide with the game title or show a truncated number
			}

			String display = placement.getLabel() == GearScoreHeaderLayout.Label.FULL ? line : compact;
			int baseline = placement.getBaseline();
			int iconTop = baseline - RaccoonMark.HEIGHT;
			int x = placement.getX();
			int clipWidth = placement.getRight() - bounds.x;
			if (clipWidth <= 0)
			{
				return;
			}
			graphics.clipRect(bounds.x, iconTop - 1, clipWidth, RaccoonMark.HEIGHT + 4);
			RaccoonMark.draw(graphics, x, iconTop);

			int textX = x + RaccoonMark.WIDTH + 3;
			graphics.setColor(Color.BLACK);
			graphics.drawString(display, textX + 1, baseline + 1);
			graphics.setColor(GearScoreText.EQUIPMENT_ORANGE);
			graphics.drawString(display, textX, baseline);
		}
		finally
		{
			graphics.setFont(previousFont);
			graphics.setColor(previousColor);
			graphics.setClip(previousClip);
		}
	}

	private Widget findTitle(Widget widget, Rectangle rootBounds, int depth)
	{
		if (widget == null || depth > 12)
		{
			return null;
		}

		String text = widget.getText();
		if (text != null && text.contains("Equip Your Character"))
		{
			Rectangle titleBounds = widget.getBounds();
			if (titleBounds != null && titleBounds.y <= rootBounds.y + rootBounds.height * 0.15)
			{
				return widget;
			}
		}

		Widget found = findTitle(widget.getStaticChildren(), rootBounds, depth);
		if (found != null)
		{
			return found;
		}
		found = findTitle(widget.getDynamicChildren(), rootBounds, depth);
		if (found != null)
		{
			return found;
		}
		return findTitle(widget.getNestedChildren(), rootBounds, depth);
	}

	private Widget findTitle(Widget[] children, Rectangle rootBounds, int depth)
	{
		if (children == null)
		{
			return null;
		}
		for (int i = 0; i < children.length; i++)
		{
			Widget found = findTitle(children[i], rootBounds, depth + 1);
			if (found != null)
			{
				return found;
			}
		}
		return null;
	}
}
