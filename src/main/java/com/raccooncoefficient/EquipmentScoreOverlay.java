package com.raccooncoefficient;

import com.raccooncoefficient.score.GearScoreBreakdown;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.GearScoreText;
import com.raccooncoefficient.score.ItemStatSource;
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
 * Draw the TOTAL equipped Gear Score above the helmet SLOT0 widget in the
 * "Equip Your Character" interface. The player-supplied screenshot identifies
 * the gap below the native title and above the helmet as the target.
 *
 * Uses RuneLite's draw-after-interface hook. It does not alter the Equipment
 * interface, slot icons, hover tooltip overlay or locked Wave 1 score formula.
 */
public class EquipmentScoreOverlay extends Overlay
{
	private final Client client;
	private final ItemStatSource stats;
	private final GearScoreCalculator calculator;

	@Inject
	EquipmentScoreOverlay(Client client, RuneLiteItemStatSource stats, RuneLiteItemNameSource names)
	{
		this(client, stats, GearScoreCalculator.wave3(names));
	}

	/** Test-only seam. Production always uses the injected RuneLite sources. */
	EquipmentScoreOverlay(Client client, ItemStatSource stats, GearScoreCalculator calculator)
	{
		this.client = client;
		this.stats = stats;
		this.calculator = calculator;
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.MANUAL);
		drawAfterInterface(InterfaceID.EQUIPMENT);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget panel = client.getWidget(InterfaceID.Equipment.UNIVERSE);
		Widget helmet = client.getWidget(InterfaceID.Equipment.SLOT0);
		if (panel == null || helmet == null || panel.isHidden() || helmet.isHidden())
		{
			return null;
		}

		Rectangle panelBounds = panel.getBounds();
		Rectangle helmetBounds = helmet.getBounds();
		if (panelBounds == null || helmetBounds == null)
		{
			return null;
		}

		java.util.List<com.raccooncoefficient.score.WornItem> worn = WornEquipment.read(client);
		String line;
		if (worn == null)
		{
			line = "Gear Score: ?";
		}
		else
		{
			GearScoreBreakdown score = calculator.score(worn, stats);
			line = GearScoreText.equipmentLine(score);
		}

		drawLine(graphics, panelBounds, helmetBounds, line);
		return null;
	}

	private void drawLine(Graphics2D graphics, Rectangle panel, Rectangle helmet, String line)
	{
		Font previousFont = graphics.getFont();
		Color previousColor = graphics.getColor();
		java.awt.Shape previousClip = graphics.getClip();
		try
		{
			graphics.setFont(FontManager.getRunescapeSmallFont());
			String compact = GearScoreText.compactEquipmentLine(line);
			GearScoreHelmetLayout.Placement placement = GearScoreHelmetLayout.choose(
				panel, helmet,
				graphics.getFontMetrics().stringWidth(line),
				graphics.getFontMetrics().stringWidth(compact));
			if (placement.getLabel() == GearScoreHelmetLayout.Label.HIDDEN)
			{
				return;
			}

			String display = placement.getLabel() == GearScoreHelmetLayout.Label.FULL
				? line : compact;
			int baseline = placement.getBaseline();
			int x = placement.getX();
			// Only paint in the strip assigned to the score above SLOT0.
			graphics.clipRect(x - 1, baseline - 16, placement.getWidth() + 3, 19);
			RaccoonMark.draw(graphics, x, baseline - RaccoonMark.HEIGHT);

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
}
