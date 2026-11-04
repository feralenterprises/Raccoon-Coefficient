package com.raccooncoefficient;

import java.awt.Rectangle;

/**
 * Pure-Java equipment-title placement calculation.
 *
 * The draw call must measure text with the actual RuneScape font before
 * selection. A line that does not fit is shortened, then hidden, never
 * clipped across the vanilla title. This does not alter any game widget.
 */
final class GearScoreHeaderLayout
{
	enum Label { FULL, COMPACT, HIDDEN }

	static final class Placement
	{
		private final Label label;
		private final int x;
		private final int baseline;
		private final int right;

		private Placement(Label label, int x, int baseline, int right)
		{
			this.label = label;
			this.x = x;
			this.baseline = baseline;
			this.right = right;
		}

		Label getLabel() { return label; }
		int getX() { return x; }
		int getBaseline() { return baseline; }
		int getRight() { return right; }
	}

	private static final float MAX_LEFT_FRACTION = 0.38f;
	private static final int ICON_AND_SPACING = RaccoonMark.WIDTH + 3;

	private GearScoreHeaderLayout()
	{
	}

	static Placement choose(Rectangle root, Rectangle title, int fullTextWidth, int compactTextWidth)
	{
		if (root == null || root.width < 80 || root.height < 80
			|| fullTextWidth < 0 || compactTextWidth < 0)
		{
			return new Placement(Label.HIDDEN, 0, 0, 0);
		}

		int x = root.x + Math.max(6, Math.round(root.width * 8f / 500f));
		int baseline = root.y + Math.round(root.height * 22f / 324f);
		int right = root.x + Math.round(root.width * MAX_LEFT_FRACTION);

		if (title != null && title.width > 0 && title.height > 0
			&& title.y >= root.y - 2
			&& title.y <= root.y + Math.round(root.height * 0.15f))
		{
			int titleLeft = Math.max(root.x, title.x - 4);
			right = Math.min(right, titleLeft);
			int aligned = title.y + title.height - 2;
			int maxAligned = root.y + Math.round(root.height * 0.09f);
			if (aligned >= root.y + 11 && aligned <= maxAligned)
			{
				baseline = aligned;
			}
		}

		int textSpace = right - x - ICON_AND_SPACING - 2;
		Label label = textSpace >= fullTextWidth ? Label.FULL
			: textSpace >= compactTextWidth ? Label.COMPACT : Label.HIDDEN;
		return new Placement(label, x, baseline, right);
	}
}
