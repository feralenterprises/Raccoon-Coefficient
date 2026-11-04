package com.raccooncoefficient;

import com.raccooncoefficient.score.EquipmentBonus;
import com.raccooncoefficient.score.EquipmentSlot;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.ItemStatSource;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.Menu;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.RuneLiteConfig;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.tooltip.Tooltip;
import net.runelite.client.ui.overlay.tooltip.TooltipManager;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Renders the real Raccoon overlay using RuneLite API proxies. The second
 * producer models HoverLite's source-confirmed TooltipManager.addFront call,
 * NOT its full plugin. No cross-repository dependency or native game claim.
 */
public class TooltipCoexistenceTest
{
	private static final String HOVERLITE_TEXT = "Wield <col=ff9040>Abyssal whip</col>";
	private static final int WHIP = 4151;

	private static Object def(Class<?> type)
	{
		if (type == boolean.class) return false;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		return null;
	}

	private static <T> T proxy(Class<T> api, InvocationHandler handler)
	{
		return api.cast(Proxy.newProxyInstance(api.getClassLoader(), new Class<?>[]{api}, handler));
	}

	private static Widget widget()
	{
		return proxy(Widget.class, (p,m,a) ->
		{
			switch (m.getName())
			{
				case "getId": return InterfaceID.Inventory.ITEMS;
				case "getParentId": return 0;
				case "getItemId": return -1;
				default: return def(m.getReturnType());
			}
		});
	}

	private static MenuEntry entry(MenuAction action)
	{
		return proxy(MenuEntry.class, (p,m,a) ->
		{
			switch (m.getName())
			{
				case "getWidget": return widget();
				case "getItemId": return WHIP;
				case "getOption": return "Wield";
				case "getType": return action;
				case "getParam1": return InterfaceID.Inventory.ITEMS;
				default: return def(m.getReturnType());
			}
		});
	}

	private static Client client(MenuAction type, boolean menuOpen, boolean wornAvailable)
	{
		MenuEntry[] entries = {entry(type)};
		Menu menu = proxy(Menu.class, (p,m,a) ->
			"getMenuEntries".equals(m.getName()) ? entries : def(m.getReturnType()));
		ItemContainer container = proxy(ItemContainer.class, (p,m,a) ->
			"getItems".equals(m.getName()) ? (wornAvailable ? new Item[0] : null)
				: def(m.getReturnType()));
		return proxy(Client.class, (p,m,a) ->
		{
			switch (m.getName())
			{
				case "isMenuOpen": return menuOpen;
				case "getMenu": return menu;
				case "getItemContainer": return container;
				default: return def(m.getReturnType());
			}
		});
	}

	private static RuneLiteConfig config()
	{
		return proxy(RuneLiteConfig.class, (p,m,a) ->
			"overlayBackgroundColor".equals(m.getName()) ? new Color(22,22,22)
				: def(m.getReturnType()));
	}

	private static final ItemStatSource STATS = id -> id == WHIP
		? new EquipmentBonus(EquipmentSlot.WEAPON, false,
			0,0,0,0,0,0,0,0,0,0,10,0,BigDecimal.ZERO,0)
		: null;

	private static GearScoreTooltipOverlay overlay(
		TooltipManager manager, MenuAction action, boolean open, boolean available)
	{
		return new GearScoreTooltipOverlay(client(action,open,available),
			STATS, GearScoreCalculator.wave1(), manager, config());
	}

	private static void render(GearScoreTooltipOverlay score)
	{
		BufferedImage image = new BufferedImage(4,4,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = image.createGraphics();
		try { score.render(g); }
		finally { g.dispose(); }
	}

	private static void addHoverLiteLabel(TooltipManager manager)
	{
		// Exact queue operation used by HoverLiteOverlay.
		manager.addFront(new Tooltip(HOVERLITE_TEXT));
	}

	private static void assertBoth(TooltipManager manager)
	{
		assertEquals(2,manager.getTooltips().size());
		assertEquals(HOVERLITE_TEXT,manager.getTooltips().get(0).getText());
		assertNull(manager.getTooltips().get(0).getComponent());
		assertTrue(manager.getTooltips().get(1).getComponent()
			instanceof ScoreTooltipQueue.ScoreComponent);
		assertNull(manager.getTooltips().get(1).getText());
	}

	@Test public void hoverLiteThenRaccoon()
	{
		TooltipManager manager = new TooltipManager();
		addHoverLiteLabel(manager);
		render(overlay(manager,MenuAction.ITEM_FIRST_OPTION,false,true));
		assertBoth(manager);
	}

	@Test public void raccoonThenHoverLite()
	{
		TooltipManager manager = new TooltipManager();
		render(overlay(manager,MenuAction.ITEM_FIRST_OPTION,false,true));
		addHoverLiteLabel(manager);
		assertBoth(manager);
	}

	@Test public void preserveNativeTooltip()
	{
		TooltipManager manager = new TooltipManager();
		Tooltip nativeTooltip = new Tooltip("Native game text");
		manager.add(nativeTooltip);
		render(overlay(manager,MenuAction.ITEM_FIRST_OPTION,false,true));
		addHoverLiteLabel(manager);
		assertEquals(3,manager.getTooltips().size());
		assertEquals(HOVERLITE_TEXT,manager.getTooltips().get(0).getText());
		assertSame(nativeTooltip,manager.getTooltips().get(1));
		assertTrue(manager.getTooltips().get(2).getComponent()
			instanceof ScoreTooltipQueue.ScoreComponent);
	}

	@Test public void twoRendersDoNotDuplicateTheScore()
	{
		TooltipManager manager = new TooltipManager();
		GearScoreTooltipOverlay score = overlay(manager,MenuAction.ITEM_FIRST_OPTION,false,true);
		addHoverLiteLabel(manager);
		render(score);
		render(score);
		assertBoth(manager);
	}

	@Test public void queueClearStartsFreshFrame()
	{
		TooltipManager manager = new TooltipManager();
		GearScoreTooltipOverlay score = overlay(manager,MenuAction.ITEM_FIRST_OPTION,false,true);
		render(score);
		assertEquals(1,manager.getTooltips().size());
		manager.clear();
		render(score);
		assertEquals(1,manager.getTooltips().size());
		assertTrue(manager.getTooltips().get(0).getComponent()
			instanceof ScoreTooltipQueue.ScoreComponent);
	}

	@Test public void openMenuAndMissingWornDataNeverDeleteOtherTooltips()
	{
		for (boolean menuOpen : new boolean[]{true,false})
		{
			TooltipManager manager = new TooltipManager();
			addHoverLiteLabel(manager);
			render(overlay(manager,MenuAction.ITEM_FIRST_OPTION,menuOpen,false));
			assertEquals(1,manager.getTooltips().size());
			assertEquals(HOVERLITE_TEXT,manager.getTooltips().get(0).getText());
		}
	}

	@Test public void nonItemDefaultActionDoesNotAttachScore()
	{
		TooltipManager manager = new TooltipManager();
		addHoverLiteLabel(manager);
		render(overlay(manager,MenuAction.NPC_FIRST_OPTION,false,true));
		assertEquals(1,manager.getTooltips().size());
	}

	@Test public void unverifiableStatNeverFabricatesScore()
	{
		TooltipManager manager = new TooltipManager();
		addHoverLiteLabel(manager);
		GearScoreTooltipOverlay score = new GearScoreTooltipOverlay(
			client(MenuAction.ITEM_FIRST_OPTION,false,true),
			id -> null,GearScoreCalculator.wave1(),manager,config());
		render(score);
		assertEquals(1,manager.getTooltips().size());
	}

	@Test public void fullScreenDrawHookMatchesRuneLiteTooltipHost()
	{
		GearScoreTooltipOverlay score = overlay(
			new TooltipManager(),MenuAction.ITEM_FIRST_OPTION,false,true);
		assertEquals(OverlayPosition.DYNAMIC,score.getPosition());
		assertEquals(OverlayLayer.ABOVE_WIDGETS,score.getLayer());
		assertTrue(score.getDrawHooks().contains((InterfaceID.TOPLEVEL_DISPLAY << 16) | 0xffff));
	}
}
