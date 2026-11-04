package com.raccooncoefficient;

import java.awt.Rectangle;

/**
 * Places the TOTAL worn Gear Score in the empty space directly above the
 * helmet slot on the "Equip Your Character" screen (InterfaceID.Equipment).
 *
 * Anchor the position to the actual SLOT0 widget, not the screen/title text:
 * this survives resizable layouts, title translations and alternate UI sizes.
 * Never move or edit game widgets.
 */
final class GearScoreHelmetLayout
{
	enum Label { FULL, COMPACT, HIDDEN }

	static final class Placement
	{
		private final Label label;
		private final int x;
		private final int baseline;
		private final int width;

		private Placement(Label label, int x, int baseline, int width)
		{
			this.label = label;
			this.x = x;
			this.baseline = baseline;
			this.width = width;
		}

		Label getLabel() { return label; }
		int getX() { return x; }
		int getBaseline() { return baseline; }
		int getWidth() { return width; }
	}

	private static final int TEXT_GAP = 3;
	private static final int TOP_GAP = 11;
	private static final int EDGE_MARGIN = 8;

	private GearScoreHelmetLayout()
	{
	}

	static Placement choose(Rectangle panel, Rectangle helmet, int fullWidth, int compactWidth)
	{
		if (panel == null || helmet == null || panel.width < 160 || panel.height < 145
			|| helmet.width < 20 || helmet.height < 20
			|| fullWidth < 0 || compactWidth < 0 || !panel.intersects(helmet))
		{
			return hidden();
		}

		final int centre = helmet.x + helmet.width / 2;
		// This is the gap directly above helmet SLOT0, not the top title bar.
		final int baseline = helmet.y - TOP_GAP;
		final int textTop = baseline - 15;
		if (textTop < panel.y + 42)
		{
			// A cramped or displaced panel should hide, never paint the
			// Gear Score over the native title.
			return hidden();
		}

		// Constrain to the equipment/character half of the panel, so even
		// an extremely long score never overlaps the attack/defence column.
		final int left = panel.x + EDGE_MARGIN;
		final int right = Math.min(panel.x + panel.width - EDGE_MARGIN,
			panel.x + Math.round(panel.width * 0.58f));
		final int fullSize = RaccoonMark.WIDTH + TEXT_GAP + fullWidth;
		final int compactSize = RaccoonMark.WIDTH + TEXT_GAP + compactWidth;

		if (fitsCentered(centre, fullSize, left, right))
		{
			return new Placement(Label.FULL, centre - fullSize / 2, baseline, fullSize);
		}
		if (fitsCentered(centre, compactSize, left, right))
		{
			return new Placement(Label.COMPACT, centre - compactSize / 2, baseline, compactSize);
		}
		return hidden();
	}

	private static boolean fitsCentered(int centre, int size, int left, int right)
	{
		int x = centre - size / 2;
		return size > RaccoonMark.WIDTH && x >= left && x + size <= right;
	}

	private static Placement hidden()
	{
		return new Placement(Label.HIDDEN, 0, 0, 0);
	}
}
