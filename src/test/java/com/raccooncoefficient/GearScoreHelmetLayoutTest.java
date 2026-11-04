package com.raccooncoefficient;

import java.awt.Rectangle;
import org.junit.Test;
import static org.junit.Assert.*;

/** Regression: position against Equipment.SLOT0, never the title-bar left gutter. */
public class GearScoreHelmetLayoutTest
{
	@Test
	public void centersTheWholeLineInScreenshotHelmetGap()
	{
		// Representative bounds measured from the owner's 1269x793 screenshot.
		Rectangle equipment = new Rectangle(143, 127, 897, 583);
		Rectangle helm = new Rectangle(272, 240, 62, 62);
		GearScoreHelmetLayout.Placement p =
			GearScoreHelmetLayout.choose(equipment, helm, 116, 72);
		assertEquals(GearScoreHelmetLayout.Label.FULL, p.getLabel());
		assertEquals(229, p.getBaseline());
		assertEquals(helm.x + helm.width / 2, p.getX() + p.getWidth() / 2);
		assertTrue("Must be below the vanilla title strip", p.getBaseline() - 15 > 187);
		assertTrue("Must be above helmet sprite", p.getBaseline() < helm.y);
		assertTrue("Never touch the right-hand Attack Bonus column", p.getX() + p.getWidth() < 650);
	}

	@Test
	public void followsHelmetWhenWindowMoves()
	{
		Rectangle originalPanel = new Rectangle(143, 127, 897, 583);
		Rectangle originalHelm = new Rectangle(272, 240, 62, 62);
		GearScoreHelmetLayout.Placement a =
			GearScoreHelmetLayout.choose(originalPanel, originalHelm, 116, 72);
		GearScoreHelmetLayout.Placement b =
			GearScoreHelmetLayout.choose(new Rectangle(243, 190, 897, 583),
				new Rectangle(372, 303, 62, 62), 116, 72);
		assertEquals(a.getX() + 100, b.getX());
		assertEquals(a.getBaseline() + 63, b.getBaseline());
	}

	@Test
	public void fallbackIsCompactWhenHelmetIsCloseToLeftEdge()
	{
		Rectangle panel = new Rectangle(0, 0, 340, 340);
		Rectangle helm = new Rectangle(45, 125, 52, 52);
		GearScoreHelmetLayout.Placement p =
			GearScoreHelmetLayout.choose(panel, helm, 150, 70);
		assertEquals(GearScoreHelmetLayout.Label.COMPACT, p.getLabel());
		assertTrue(p.getX() >= panel.x + 8);
		assertTrue(p.getX() + p.getWidth() < panel.x + 200);
	}

	@Test
	public void hideInsteadOfClippingWhenScreenIsCrampedOrMissing()
	{
		Rectangle panel = new Rectangle(0, 0, 180, 220);
		assertEquals(GearScoreHelmetLayout.Label.HIDDEN,
			GearScoreHelmetLayout.choose(panel, new Rectangle(25, 80, 40, 40), 250, 200).getLabel());
		assertEquals(GearScoreHelmetLayout.Label.HIDDEN,
			GearScoreHelmetLayout.choose(panel, null, 100, 60).getLabel());
		assertEquals(GearScoreHelmetLayout.Label.HIDDEN,
			GearScoreHelmetLayout.choose(null, new Rectangle(25, 80, 40, 40), 100, 60).getLabel());
		assertEquals(GearScoreHelmetLayout.Label.HIDDEN,
			GearScoreHelmetLayout.choose(panel, new Rectangle(600, 600, 50, 50), 80, 45).getLabel());
	}

	@Test
	public void neverPaintsAcrossTheVanillaTitleEvenWithAnInvalidHelmetPosition()
	{
		GearScoreHelmetLayout.Placement p = GearScoreHelmetLayout.choose(
			new Rectangle(100, 100, 800, 550), new Rectangle(250, 150, 62, 62), 112, 68);
		assertEquals(GearScoreHelmetLayout.Label.HIDDEN, p.getLabel());
	}
}
