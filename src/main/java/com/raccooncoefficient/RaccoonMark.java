package com.raccooncoefficient;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Tiny raccoon drawn beside the equipment-screen score. The game font has no
 * emoji glyph, and Java2D does not paint the color emoji font, so the line
 * uses this mark instead of a missing character.
 */
public final class RaccoonMark
{
	public static final int WIDTH = 12;
	public static final int HEIGHT = 11;

	private static final Color EAR = new Color(90, 78, 62);
	private static final Color FUR = new Color(176, 168, 156);
	private static final Color MASK = new Color(45, 42, 40);
	private static final Color EYE = new Color(245, 245, 240);
	private static final Color NOSE = new Color(40, 32, 32);

	private static final int[][] PIXELS = {
		{0, 0, 2, 2, 0, 0, 0, 2, 2, 0, 0, 0},
		{0, 2, 2, 2, 2, 0, 2, 2, 2, 2, 0, 0},
		{0, 2, 3, 3, 3, 3, 3, 3, 3, 2, 0, 0},
		{2, 3, 4, 4, 4, 4, 4, 4, 3, 3, 2, 0},
		{2, 3, 4, 5, 4, 4, 4, 5, 4, 3, 2, 0},
		{2, 3, 4, 4, 4, 4, 4, 4, 4, 3, 2, 0},
		{0, 2, 3, 4, 4, 4, 4, 4, 3, 2, 0, 0},
		{0, 2, 3, 3, 3, 6, 6, 3, 3, 2, 0, 0},
		{0, 0, 2, 3, 3, 3, 3, 3, 2, 0, 0, 0},
		{0, 0, 0, 2, 2, 3, 3, 2, 2, 0, 0, 0},
		{0, 0, 0, 0, 2, 2, 2, 2, 0, 0, 0, 0},
	};

	private RaccoonMark()
	{
	}

	public static void draw(Graphics2D graphics, int x, int y)
	{
		for (int row = 0; row < PIXELS.length; row++)
		{
			for (int column = 0; column < PIXELS[row].length; column++)
			{
				Color color = color(PIXELS[row][column]);
				if (color == null)
				{
					continue;
				}
				graphics.setColor(color);
				graphics.fillRect(x + column, y + row, 1, 1);
			}
		}
	}

	private static Color color(int pixel)
	{
		switch (pixel)
		{
			case 2:
				return EAR;
			case 3:
				return FUR;
			case 4:
				return MASK;
			case 5:
				return EYE;
			case 6:
				return NOSE;
			default:
				return null;
		}
	}
}
