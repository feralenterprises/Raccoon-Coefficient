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
	private static final int NOMINAL_WIDTH = 500;
	private static final int NOMINAL_HEIGHT = 324;

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
		Font font = FontManager.getRunescapeSmallFont();
		graphics.setFont(font);

		int baseline = bounds.y + Math.round(bounds.height * 22f / NOMINAL_HEIGHT);
		int x = bounds.x + Math.max(6, Math.round(bounds.width * 8f / NOMINAL_WIDTH));
		int clipRight = bounds.x + Math.round(bounds.width * 0.32f);
		Widget title = findTitle(root, bounds, 0);
		if (title != null)
		{
			Rectangle titleBounds = title.getBounds();
			int alignedBaseline = titleBounds.y + titleBounds.height - 2;
			int maxBaseline = bounds.y + Math.round(bounds.height * 0.09f);
			if (alignedBaseline <= maxBaseline && titleBounds.x - 4 > x + 80)
			{
				baseline = alignedBaseline;
				clipRight = Math.min(clipRight, titleBounds.x - 4);
			}
		}

		int iconTop = baseline - 11;
		Rectangle previousClip = graphics.getClipBounds();
		graphics.clipRect(bounds.x, iconTop - 1, Math.max(0, clipRight - bounds.x), baseline - iconTop + 4);
		try
		{
			RaccoonMark.draw(graphics, x, iconTop);
			int textX = x + RaccoonMark.WIDTH + 3;
			graphics.setColor(Color.BLACK);
			graphics.drawString(line, textX + 1, baseline + 1);
			graphics.setColor(GearScoreText.EQUIPMENT_ORANGE);
			graphics.drawString(line, textX, baseline);
		}
		finally
		{
			graphics.setFont(previousFont);
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
