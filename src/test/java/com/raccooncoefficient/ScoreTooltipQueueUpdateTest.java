package com.raccooncoefficient;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import org.junit.Test;

import static org.junit.Assert.*;

/** Guard the shared queue when Raccoon's repeated hooks see fresher evidence. */
public class ScoreTooltipQueueUpdateTest
{
	private static final class Marker implements ScoreTooltipQueue.ScoreComponent
	{
		private final String label;
		private Marker(String label) { this.label = label; }

		@Override public Dimension render(Graphics2D graphics) { return new Dimension(1,1); }
		@Override public Rectangle getBounds() { return new Rectangle(0,0,1,1); }
		@Override public void setPreferredLocation(Point location) { }
		@Override public void setPreferredSize(Dimension size) { }
	}

	@Test
	public void newerRaccoonTooltipReplacesOnlyItsOldPosition()
	{
		TooltipManager manager = new TooltipManager();
		Tooltip hoverLite = new Tooltip("Wield Abyssal whip");
		Tooltip game = new Tooltip("Game-owned text");
		manager.addFront(hoverLite);
		manager.add(game);

		Marker oldScore = new Marker("old item");
		Marker latest = new Marker("new item");
		ScoreTooltipQueue.addOnce(manager, oldScore);
		ScoreTooltipQueue.addOnce(manager, latest);

		assertEquals(3, manager.getTooltips().size());
		assertSame(hoverLite, manager.getTooltips().get(0));
		assertSame(game, manager.getTooltips().get(1));
		assertSame(latest, manager.getTooltips().get(2).getComponent());
		assertNotSame(oldScore, manager.getTooltips().get(2).getComponent());
	}

	@Test
	public void clearThenNextFrameAllowsFreshScoreWithoutStaleReference()
	{
		TooltipManager manager = new TooltipManager();
		Marker first = new Marker("first");
		Marker second = new Marker("second");
		ScoreTooltipQueue.addOnce(manager, first);
		assertSame(first, manager.getTooltips().get(0).getComponent());
		manager.clear();
		ScoreTooltipQueue.addOnce(manager, second);
		assertEquals(1, manager.getTooltips().size());
		assertSame(second, manager.getTooltips().get(0).getComponent());
	}

	@Test
	public void nullInputsNeverAlterThirdPartyEntries()
	{
		TooltipManager manager = new TooltipManager();
		Tooltip external = new Tooltip("External tooltip");
		manager.add(external);
		ScoreTooltipQueue.addOnce(null, new Marker("unused"));
		ScoreTooltipQueue.addOnce(manager, null);
		assertEquals(1, manager.getTooltips().size());
		assertSame(external, manager.getTooltips().get(0));
	}
}
