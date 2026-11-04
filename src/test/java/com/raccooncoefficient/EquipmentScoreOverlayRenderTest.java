package com.raccooncoefficient;

import com.raccooncoefficient.score.EquipmentBonus;
import com.raccooncoefficient.score.EquipmentSlot;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.ItemStatSource;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.junit.Test;
import static org.junit.Assert.*;

/** Tests actual Overlay.render(Graphics2D) with RuneLite interfaces mocked. */
public class EquipmentScoreOverlayRenderTest
{
	private static final Rectangle PANEL = new Rectangle(143, 127, 897, 583);
	private static final Rectangle HELMET = new Rectangle(272, 240, 62, 62);

	private static <T> T proxy(Class<T> type, InvocationHandler handler)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},handler));
	}

	private static Object other(Class<?> result)
	{
		if (result == boolean.class) return false;
		if (result == int.class) return 0;
		if (result == long.class) return 0L;
		return null;
	}

	private static Widget widget(Rectangle bounds)
	{
		return proxy(Widget.class,(p,m,args) ->
		{
			if ("getBounds".equals(m.getName())) return bounds;
			if ("isHidden".equals(m.getName())) return false;
			return other(m.getReturnType());
		});
	}

	private static Client client(Item[][] wornState, boolean hasHelmetWidget)
	{
		Widget panel = widget(PANEL);
		Widget helmet = widget(HELMET);
		ItemContainer container = proxy(ItemContainer.class,(p,m,args) ->
			"getItems".equals(m.getName()) ? wornState[0] : other(m.getReturnType()));
		return proxy(Client.class,(p,m,args) ->
		{
			if ("getWidget".equals(m.getName()))
			{
				int id=(Integer) args[0];
				if (id==InterfaceID.Equipment.UNIVERSE) return panel;
				if (id==InterfaceID.Equipment.SLOT0) return hasHelmetWidget ? helmet : null;
				return null;
			}
			if ("getItemContainer".equals(m.getName())) return container;
			return other(m.getReturnType());
		});
	}

	private static final ItemStatSource STATS = id -> new EquipmentBonus(
		EquipmentSlot.HEAD,false,0,0,0,0,0,0,0,0,0,0,
		id == 100 ? 20 : 60,0,BigDecimal.ZERO,0);

	private static BufferedImage render(EquipmentScoreOverlay overlay)
	{
		BufferedImage image=new BufferedImage(1200,800,BufferedImage.TYPE_INT_ARGB);
		Graphics2D g=image.createGraphics();
		try { overlay.render(g); }
		finally { g.dispose(); }
		return image;
	}

	private static int opaque(BufferedImage image,int x1,int y1,int x2,int y2)
	{
		int count=0;
		for(int y=y1;y<y2;y++)
		{
			for(int x=x1;x<x2;x++)
			{
				if((image.getRGB(x,y)>>>24)!=0) count++;
			}
		}
		return count;
	}

	@Test public void actuallyDrawsInOwnersHelmetGapInsteadOfTitleBar()
	{
		Item[][] worn={new Item[14]};
		worn[0][EquipmentSlot.HEAD]=new Item(100,1);
		EquipmentScoreOverlay overlay=new EquipmentScoreOverlay(
			client(worn,true),STATS,GearScoreCalculator.wave1());
		BufferedImage image=render(overlay);
		assertTrue("Must display centered total score ABOVE the helmet slot",
			opaque(image,205,207,405,236)>25);
		assertEquals("Must not draw on Equip Your Character title",
			0,opaque(image,150,129,470,188));
		assertEquals("Must not draw inside helmet icon",
			0,opaque(image,272,240,334,290));
		assertEquals(OverlayPosition.DYNAMIC,overlay.getPosition());
		assertEquals(OverlayLayer.MANUAL,overlay.getLayer());
		assertTrue(overlay.getDrawHooks().contains(
			(InterfaceID.EQUIPMENT << 16) | 0xffff));
	}

	@Test public void equipmentChangeRedrawsTheActualDisplayedScore()
	{
		Item[][] worn={new Item[14]};
		worn[0][EquipmentSlot.HEAD]=new Item(100,1);
		EquipmentScoreOverlay overlay=new EquipmentScoreOverlay(
			client(worn,true),STATS,GearScoreCalculator.wave1());
		BufferedImage before=render(overlay);
		worn[0][EquipmentSlot.HEAD]=new Item(101,1);
		BufferedImage after=render(overlay);
		int changes=0;
		for(int y=207;y<238;y++)
		{
			for(int x=205;x<405;x++)
			{
				if(before.getRGB(x,y)!=after.getRGB(x,y)) changes++;
			}
		}
		assertTrue("The painted total must update when worn gear changes",changes>0);
	}

	@Test public void missingHelmetWidgetFailsClosedAndDoesNotDrawOverOtherUi()
	{
		Item[][] worn={new Item[14]};
		EquipmentScoreOverlay overlay=new EquipmentScoreOverlay(
			client(worn,false),STATS,GearScoreCalculator.wave1());
		BufferedImage image=render(overlay);
		assertEquals(0,opaque(image,0,0,image.getWidth(),image.getHeight()));
	}

	@Test public void unknownEquipmentShowsAnUnverifiedScoreLine()
	{
		Item[][] worn={null};
		EquipmentScoreOverlay overlay=new EquipmentScoreOverlay(
			client(worn,true),STATS,GearScoreCalculator.wave1());
		assertTrue(opaque(render(overlay),205,207,405,236)>10);
	}
}
