package com.raccooncoefficient;

import com.raccooncoefficient.score.GearScoreText;
import java.awt.Rectangle;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Numeric title-placement constraints. Source-only checks: actual RuneLite
 * title geometry and font rendering still require later native verification.
 */
public class GearScoreHeaderLayoutTest
{
	@Test
	public void fullLabelIsPreferredWheneverItFits()
	{
		GearScoreHeaderLayout.Placement full = GearScoreHeaderLayout.choose(
			new Rectangle(100, 200, 500, 324), null, 125, 65);
		assertEquals(GearScoreHeaderLayout.Label.FULL, full.getLabel());
		assertTrue(full.getRight() > full.getX() + RaccoonMark.WIDTH + 3 + 125);
	}

	@Test
	public void narrowHeaderUsesACompactRatherThanClippedLine()
	{
		GearScoreHeaderLayout.Placement compact = GearScoreHeaderLayout.choose(
			new Rectangle(0, 0, 300, 324), null, 128, 75);
		assertEquals(GearScoreHeaderLayout.Label.COMPACT, compact.getLabel());
		assertTrue(compact.getRight() >= compact.getX() + RaccoonMark.WIDTH + 3 + 75);
	}

	@Test
	public void noRoomHidesLabelRatherThanDrawingAcrossTitle()
	{
		GearScoreHeaderLayout.Placement hidden = GearScoreHeaderLayout.choose(
			new Rectangle(0, 0, 180, 324), null, 145, 75);
		assertEquals(GearScoreHeaderLayout.Label.HIDDEN, hidden.getLabel());
	}

	@Test
	public void visibleVanillaTitleTightensRightEdge()
	{
		Rectangle root = new Rectangle(100, 100, 500, 324);
		Rectangle title = new Rectangle(220, 108, 220, 18);
		GearScoreHeaderLayout.Placement result = GearScoreHeaderLayout.choose(
			root, title, 130, 60);
		assertEquals(216, result.getRight());
		assertEquals(GearScoreHeaderLayout.Label.COMPACT, result.getLabel());
	}

	@Test
	public void incorrectlyLocatedTitleDoesNotForceTitleClipping()
	{
		Rectangle root = new Rectangle(0, 0, 500, 324);
		Rectangle unrelated = new Rectangle(100, 200, 200, 20);
		GearScoreHeaderLayout.Placement result = GearScoreHeaderLayout.choose(
			root, unrelated, 125, 50);
		assertEquals(GearScoreHeaderLayout.Label.FULL, result.getLabel());
	}

	@Test
	public void unavailableAndTinyWidgetsNeverDrawScores()
	{
		assertEquals(GearScoreHeaderLayout.Label.HIDDEN,
			GearScoreHeaderLayout.choose(null, null, 100, 60).getLabel());
		assertEquals(GearScoreHeaderLayout.Label.HIDDEN,
			GearScoreHeaderLayout.choose(new Rectangle(0, 0, 60, 30), null, 100, 60).getLabel());
	}

	@Test
	public void compactScorePreservesUnknownMarkerAndNegativeValues()
	{
		assertEquals("GS: 142.6 ?", GearScoreText.compactEquipmentLine("Gear Score: 142.6 ?"));
		assertEquals("GS: -6.5", GearScoreText.compactEquipmentLine("Gear Score: -6.5"));
		assertEquals("GS: ?", GearScoreText.compactEquipmentLine("Gear Score: ?"));
	}
}
