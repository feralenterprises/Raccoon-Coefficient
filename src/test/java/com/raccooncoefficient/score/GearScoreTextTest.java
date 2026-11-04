package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GearScoreTextTest
{
	@Test
	public void equipmentLineShowsOneDecimalAndMarksIncompleteScores()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		assertEquals("Gear Score: 0.0", GearScoreText.equipmentLine(calculator.score(Collections.<WornItem>emptyList(), null)));

		GearScoreBreakdown positive = calculator.score(
			Collections.singletonList(new WornItem(EquipmentSlot.WEAPON, 1)),
			source(meleeSlash(142, 6)));
		assertEquals("Gear Score: 142.6", GearScoreText.equipmentLine(positive));

		GearScoreBreakdown negative = calculator.score(
			Collections.singletonList(new WornItem(EquipmentSlot.BODY, 2)),
			source(magicDefence(-65)));
		assertEquals("Gear Score: -6.5", GearScoreText.equipmentLine(negative));

		GearScoreBreakdown unknown = calculator.score(
			Collections.singletonList(new WornItem(EquipmentSlot.HEAD, 3)),
			source(meleeSlash(142, 6)));
		assertEquals("Gear Score: 0.0 ?", GearScoreText.equipmentLine(unknown));
	}

	@Test
	public void tooltipShowsTheItemScoreInWhiteAndColorsTheComparison()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		ItemStatSource source = new ItemStatSource()
		{
			@Override
			public EquipmentBonus getVerifiedBonus(int itemId)
			{
				if (itemId == 1)
				{
					return melee(10);
				}
				if (itemId == 2)
				{
					return meleeSlash(18, 4);
				}
				return null;
			}
		};

		String better = GearScoreText.tooltip(
			calculator.compare(2, Collections.singletonList(new WornItem(EquipmentSlot.WEAPON, 1)), source),
			false);
		assertTrue(better.startsWith("Gear Score: 18.4</br>"));
		assertTrue(better.contains("<col=00c846>\u2191 +8.4</col>"));

		String worse = GearScoreText.tooltip(
			calculator.compare(1, Collections.singletonList(new WornItem(EquipmentSlot.WEAPON, 2)), source),
			false);
		assertTrue(worse.startsWith("Gear Score: 10.0</br>"));
		assertTrue(worse.contains("<col=ff4040>\u2193 \u22128.4</col>"));

		String equal = GearScoreText.tooltip(
			calculator.compare(1, Collections.singletonList(new WornItem(EquipmentSlot.WEAPON, 1)), source),
			true);
		assertTrue(equal.startsWith("Gear Score: 10.0</br>"));
		assertTrue(equal.contains("<col=b4b4b4>\u2192 0.0</col>"));
	}

	@Test
	public void unknownHoverDoesNotInventANumber()
	{
		GearScoreComparison unknown = GearScoreComparison.unknownItem();
		assertNull(GearScoreText.tooltip(unknown, false));
		assertEquals("Gear Score: unknown", GearScoreText.tooltip(unknown, true));

		String blocked = GearScoreText.tooltip(GearScoreComparison.knownItemUnknownDelta(new BigDecimal("4.0")), false);
		assertEquals("Gear Score: 4.0</br><col=b4b4b4>\u2192 ?</col>", blocked);
	}

	@Test
	public void deltaFormatUsesALeadingPlusAndARealMinus()
	{
		assertEquals("+8.4", GearScoreText.formatDelta(new BigDecimal("8.4")));
		assertEquals("\u22128.4", GearScoreText.formatDelta(new BigDecimal("-8.4")));
		assertEquals("0.0", GearScoreText.formatDelta(new BigDecimal("0.0")));
		assertEquals("+0.2", GearScoreText.formatDelta(new BigDecimal("0.15")));
		assertEquals("\u22120.2", GearScoreText.formatDelta(new BigDecimal("-0.15")));
	}

	private static ItemStatSource source(final EquipmentBonus bonus)
	{
		return new ItemStatSource()
		{
			@Override
			public EquipmentBonus getVerifiedBonus(int itemId)
			{
				return itemId == 1 || itemId == 2 ? bonus : null;
			}
		};
	}

	private static EquipmentBonus melee(int strength)
	{
		return new EquipmentBonus(EquipmentSlot.WEAPON, false, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, strength, 0, BigDecimal.ZERO, 0);
	}

	private static EquipmentBonus meleeSlash(int strength, int slash)
	{
		return new EquipmentBonus(EquipmentSlot.WEAPON, false, 0, slash, 0, 0, 0, 0, 0, 0, 0, 0, strength, 0, BigDecimal.ZERO, 0);
	}

	private static EquipmentBonus magicDefence(int magic)
	{
		return new EquipmentBonus(EquipmentSlot.BODY, false, 0, 0, 0, 0, 0, 0, 0, 0, magic, 0, 0, 0, BigDecimal.ZERO, 0);
	}
}
