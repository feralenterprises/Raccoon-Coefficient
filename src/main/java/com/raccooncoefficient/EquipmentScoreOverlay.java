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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays the total equipped Gear Score immediately above the helmet slot.
 *
 * IMPORTANT: The original MANUAL drawAfterInterface(InterfaceID.EQUIPMENT)
 * hook compiled and our unit render() test passed, yet the actual logged-in
 * player screenshot showed no label. Use the same ABOVE_WIDGETS top-level
 * render phase as RuneLite's working tooltip overlay instead. RuneLite maps
 * ABOVE_WIDGETS to all three top-level interface variants.
 *
 * This checks the live equipment widgets each frame; no widget is modified.
 */
public class EquipmentScoreOverlay extends Overlay
{
	private static final Logger log = LoggerFactory.getLogger(EquipmentScoreOverlay.class);

	private final Client client;
	private final ItemStatSource stats;
	private final GearScoreCalculator calculator;
	private String lastRenderState;

	@Inject
	EquipmentScoreOverlay(Client client, RuneLiteItemStatSource stats, RuneLiteItemNameSource names)
	{
		this(client, stats, GearScoreCalculator.wave3(names));
	}

	/** Test seam; the production constructor uses real RuneLite sources. */
	EquipmentScoreOverlay(Client client, ItemStatSource stats, GearScoreCalculator calculator)
	{
		this.client = client;
		this.stats = stats;
		this.calculator = calculator;
		setPosition(OverlayPosition.DYNAMIC);
		// Registered by OverlayManager for TOPLEVEL, TOPLEVEL_OSRS_STRETCH
		// and TOPLEVEL_PRE_EOC. Avoid double rendering through another hook.
		setLayer(OverlayLayer.ABOVE_WIDGETS);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		Widget helmet = client.getWidget(InterfaceID.Equipment.SLOT0);
		if (helmet == null)
		{
			report("helmet widget unavailable; equipment screen not open or ID unresolvable");
			return null;
		}
		if (helmet.isHidden())
		{
			report("helmet widget hidden");
			return null;
		}

		Rectangle helmetBounds = helmet.getBounds();
		if (helmetBounds == null || helmetBounds.width < 20 || helmetBounds.height < 20)
		{
			report("helmet widget bounds unavailable");
			return null;
		}

		Widget panel = client.getWidget(InterfaceID.Equipment.UNIVERSE);
		if (panel != null && panel.isHidden())
		{
			report("equipment panel hidden");
			return null;
		}
		Rectangle panelBounds = panel == null ? null : panel.getBounds();
		if (!GearScoreHelmetLayout.isUsablePanel(panelBounds, helmetBounds))
		{
			// Some native layouts expose a widget with zero/unrelated root
			// bounds. Anchor the guarded strip to the observed helmet widget
			// rather than discarding a valid visible equipment screen.
			panelBounds = GearScoreHelmetLayout.fallbackPanel(helmetBounds);
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

		boolean drew = drawLine(graphics, panelBounds, helmetBounds, line);
		report(drew ? "visible over helmet" : "layout blocked; label hidden to avoid overlap");
		return null;
	}

	private boolean drawLine(Graphics2D graphics, Rectangle panel, Rectangle helmet, String line)
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
				return false;
			}

			String display = placement.getLabel() == GearScoreHelmetLayout.Label.FULL
				? line : compact;
			int baseline = placement.getBaseline();
			int x = placement.getX();

			graphics.clipRect(x - 1, baseline - 16, placement.getWidth() + 3, 19);
			RaccoonMark.draw(graphics, x, baseline - RaccoonMark.HEIGHT);

			int textX = x + RaccoonMark.WIDTH + 3;
			graphics.setColor(Color.BLACK);
			graphics.drawString(display, textX + 1, baseline + 1);
			graphics.setColor(GearScoreText.EQUIPMENT_ORANGE);
			graphics.drawString(display, textX, baseline);
			return true;
		}
		finally
		{
			graphics.setFont(previousFont);
			graphics.setColor(previousColor);
			graphics.setClip(previousClip);
		}
	}

	/**
	 * Log only when the display state changes. This is native diagnostic
	 * evidence without a constant per-frame log flood or item/account data.
	 */
	private void report(String current)
	{
		if (!current.equals(lastRenderState))
		{
			lastRenderState = current;
			log.info("Raccoon Coefficient total: {}", current);
		}
	}
}
