package com.raccooncoefficient;

import com.raccooncoefficient.score.GearScoreBreakdown;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.GearScoreText;
import com.raccooncoefficient.score.ItemStatSource;
import java.awt.Rectangle;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.runelite.api.Client;
import net.runelite.api.FontID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetPositionMode;
import net.runelite.api.widgets.WidgetTextAlignment;
import net.runelite.api.widgets.WidgetType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The displayed equipped total is a REAL RuneLite equipment-interface text
 * widget, NOT an overlay competing with the modal's drawing order.
 *
 * This is the same interface-child API RuneLite's own Wiki DPS manager uses
 * to add a button to the Equipment screen.
 *
 * Must only be called on RuneLite's client thread (event subscribers and
 * ClientThread.invoke). Never deletes children owned by other plugins.
 */
@Singleton
final class EquipmentTotalWidget
{
	private static final Logger log = LoggerFactory.getLogger(EquipmentTotalWidget.class);
	private static final String OWNER_NAME = "raccoon-coefficient-equipped-total";
	private static final int LABEL_WIDTH = 170;
	private static final int LABEL_HEIGHT = 20;
	private static final int GAP_ABOVE_HELMET = 5;
	private static final int TEXT_COLOR = 0xff981f;

	private final Client client;
	private final ItemStatSource stats;
	private final GearScoreCalculator calculator;
	private Widget label;
	private Widget parent;
	private String lastStatus;

	@Inject
	EquipmentTotalWidget(Client client, RuneLiteItemStatSource stats, RuneLiteItemNameSource names)
	{
		this(client, stats, GearScoreCalculator.wave3(names));
	}

	/** Test seam; normal construction uses the RuneLite item data sources. */
	EquipmentTotalWidget(Client client, ItemStatSource stats, GearScoreCalculator calculator)
	{
		this.client = client;
		this.stats = stats;
		this.calculator = calculator;
	}

	void refresh()
	{
		Widget helmet = client.getWidget(InterfaceID.Equipment.SLOT0);
		Widget contents = client.getWidget(InterfaceID.Equipment.CONTENTS);
		if (helmet == null || contents == null || helmet.isHidden() || contents.isHidden())
		{
			hide();
			status("equipment interface or helmet not visible");
			return;
		}

		Rectangle helm = helmet.getBounds();
		Widget actualParent = helmet.getParent();
		if (actualParent == null || actualParent.isHidden()
			|| !canContainLabel(actualParent.getBounds(), helm))
		{
			actualParent = contents;
		}
		Rectangle area = actualParent.getBounds();
		if (helm == null || area == null || helm.width < 20 || helm.height < 20
			|| area.width < 160 || area.height < 80)
		{
			hide();
			status("equipment widget bounds unavailable");
			return;
		}
		// Parent-relative geometry in the actual game UI, not screen-space
		// Graphics2D coordinates and not the modal's top-level overlay pass.
		int x = helm.x + helm.width / 2 - LABEL_WIDTH / 2 - area.x;
		int y = helm.y - GAP_ABOVE_HELMET - LABEL_HEIGHT - area.y;
		if (x < 0 || y < 0 || x + LABEL_WIDTH > area.width
			|| y + LABEL_HEIGHT > area.height)
		{
			hide();
			status("helmet label would exceed equipment parent bounds");
			return;
		}

		Widget current = locateOwnedChild(actualParent);
		if (current == null)
		{
			current = actualParent.createChild(-1, WidgetType.TEXT);
			if (current == null)
			{
				status("failed to create equipment interface text child");
				return;
			}
			current.setName(OWNER_NAME)
				.setTextColor(TEXT_COLOR)
				.setFontId(FontID.BOLD_12)
				.setTextShadowed(true)
				.setXTextAlignment(WidgetTextAlignment.CENTER)
				.setYTextAlignment(WidgetTextAlignment.CENTER)
				.setHasListener(false);
			current.setNoClickThrough(false);
			current.setNoScrollThrough(false);
		}

		parent = actualParent;
		label = current;
		List<com.raccooncoefficient.score.WornItem> worn = WornEquipment.read(client);
		String text = worn == null ? "Gear Score: ?"
			: GearScoreText.equipmentLine(calculator.score(worn, stats));
		if (!text.equals(current.getText()))
		{
			current.setText(text);
		}
		current.setPos(x, y, WidgetPositionMode.ABSOLUTE_LEFT,
			WidgetPositionMode.ABSOLUTE_TOP);
		current.setSize(LABEL_WIDTH, LABEL_HEIGHT);
		current.setHidden(false);
		current.revalidate();
		status("equipment text child active above helmet");
	}

	void hide()
	{
		if (label != null)
		{
			label.setHidden(true);
		}
	}

	void shutdown()
	{
		// A single child cannot be deleted through Widget API. Keep it
		// invisible, without deleting unrelated dynamic children; a later
		// plugin restart reuses the matching owned-name child.
		hide();
		label = null;
		parent = null;
		lastStatus = null;
	}

	private static boolean canContainLabel(Rectangle area, Rectangle helm)
	{
		if (area == null || helm == null || area.width < LABEL_WIDTH
			|| area.height < LABEL_HEIGHT || helm.width < 20 || helm.height < 20)
		{
			return false;
		}
		int centre = helm.x + helm.width / 2;
		int left = centre - LABEL_WIDTH / 2;
		int top = helm.y - GAP_ABOVE_HELMET - LABEL_HEIGHT;
		return left >= area.x && top >= area.y
			&& left + LABEL_WIDTH <= area.x + area.width
			&& top + LABEL_HEIGHT <= area.y + area.height;
	}

	private Widget locateOwnedChild(Widget actualParent)
	{
		if (actualParent == parent && label != null)
		{
			// Ensure this reference still belongs to this live parent.
			Widget[] children = actualParent.getDynamicChildren();
			if (children != null)
			{
				for (Widget child : children)
				{
					if (child == label && OWNER_NAME.equals(child.getName()))
					{
						return child;
					}
				}
			}
		}
		Widget[] children = actualParent.getDynamicChildren();
		if (children != null)
		{
			for (Widget child : children)
			{
				if (child != null && OWNER_NAME.equals(child.getName()))
				{
					return child;
				}
			}
		}
		return null;
	}

	private void status(String next)
	{
		if (!next.equals(lastStatus))
		{
			lastStatus = next;
			log.info("Raccoon Coefficient total widget: {}", next);
		}
	}
}
