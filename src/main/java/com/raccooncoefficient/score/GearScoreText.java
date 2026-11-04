package com.raccooncoefficient.score;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

/** Player-facing score lines. The equipment screen draws the raccoon beside the equipment line. */
public final class GearScoreText
{
	public static final Color BETTER = new Color(0x00, 0xC8, 0x46);
	public static final Color WORSE = new Color(0xFF, 0x40, 0x40);
	public static final Color NEUTRAL = new Color(0xB4, 0xB4, 0xB4);
	public static final Color EQUIPMENT_ORANGE = new Color(0xFF, 0x98, 0x1F);

	private GearScoreText()
	{
	}

	public static String equipmentLine(GearScoreBreakdown score)
	{
		String number = score.displayedTotal().toPlainString();
		if (!score.isComplete())
		{
			return "Gear Score: " + number + " ?";
		}
		return "Gear Score: " + number;
	}

	/**
	 * Tooltip body, or null when the hovered id is not verified equipment and
	 * is not currently worn. The first line is left untagged so it stays white.
	 */
	public static String tooltip(GearScoreComparison comparison, boolean worn)
	{
		if (!comparison.isItemKnown())
		{
			if (!worn)
			{
				return null;
			}
			return "Gear Score: unknown";
		}

		StringBuilder text = new StringBuilder();
		text.append("Gear Score: ").append(comparison.getItemScore().toPlainString());
		text.append("</br>");
		switch (comparison.getVerdict())
		{
			case BETTER:
				text.append(color(BETTER, "\u2191 " + formatDelta(comparison.getDelta())));
				break;
			case WORSE:
				text.append(color(WORSE, "\u2193 " + formatDelta(comparison.getDelta())));
				break;
			case EQUAL:
				text.append(color(NEUTRAL, "\u2192 " + formatDelta(comparison.getDelta())));
				break;
			case UNKNOWN:
			default:
				if (comparison.getBaseDelta() != null)
				{
					BigDecimal baseDelta = comparison.getBaseDelta();
					Color tint = baseDelta.signum() > 0 ? BETTER
						: baseDelta.signum() < 0 ? WORSE : NEUTRAL;
					String arrow = baseDelta.signum() > 0 ? "\u2191"
						: baseDelta.signum() < 0 ? "\u2193" : "\u2192";
					text.append("Base ");
					text.append(color(tint, arrow + " " + formatDelta(baseDelta)));
					text.append(color(NEUTRAL, " (total ?)"));
				}
				else
				{
					text.append(color(NEUTRAL, "\u2192 ?"));
				}
				break;
		}
		return text.toString();
	}

	public static String formatDelta(BigDecimal delta)
	{
		BigDecimal scaled = delta.setScale(1, RoundingMode.HALF_UP);
		int sign = scaled.signum();
		if (sign > 0)
		{
			return "+" + scaled.toPlainString();
		}
		if (sign < 0)
		{
			return "\u2212" + scaled.abs().toPlainString();
		}
		return "0.0";
	}

	private static String color(Color color, String text)
	{
		return String.format("<col=%02x%02x%02x>%s</col>", color.getRed(), color.getGreen(), color.getBlue(), text);
	}
}
