package com.raccooncoefficient;

import com.raccooncoefficient.score.EquipmentBonus;
import com.raccooncoefficient.score.EquipmentSlot;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.ItemStatSource;
import java.awt.Rectangle;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.WidgetType;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Tests the production EquipmentTotalWidget using stateful RuneLite API fakes:
 * verifies live interface-child creation, reuse and cleanup, not a mock
 * Graphics2D overlay. This does NOT claim native RuneLite visual acceptance.
 */
public class EquipmentTotalWidgetTest
{
	private static final Rectangle CONTENT = new Rectangle(100, 100, 430, 430);
	private static final Rectangle HELMET = new Rectangle(245, 223, 62, 62);

	private static Object defaultResult(Class<?> type)
	{
		if (type == boolean.class) return false;
		if (type == int.class) return 0;
		if (type == long.class) return 0L;
		return null;
	}

	private static <T> T proxy(Class<T> type, InvocationHandler impl)
	{
		return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, impl));
	}

	private static final class FakeWidget implements InvocationHandler
	{
		final Widget widget;
		final List<FakeWidget> children = new ArrayList<FakeWidget>();
		FakeWidget parent;
		Rectangle bounds;
		String name;
		String text;
		int x, y, width, height;
		boolean hidden;
		int created, deleted, validated;
		boolean noClickThrough;

		FakeWidget(Rectangle bounds)
		{
			this.bounds = bounds;
			this.widget = proxy(Widget.class, this);
		}

		@Override public Object invoke(Object p, java.lang.reflect.Method m, Object[] args)
		{
			switch (m.getName())
			{
				case "getBounds": return bounds;
				case "isHidden": return hidden;
				case "isSelfHidden": return hidden;
				case "getParent": return parent == null ? null : parent.widget;
				case "getName": return name;
				case "getText": return text;
				case "getDynamicChildren":
					Widget[] items = new Widget[children.size()];
					for (int i = 0; i < children.size(); i++) items[i] = children.get(i).widget;
					return items;
				case "createChild":
					assertEquals(-1, ((Integer)args[0]).intValue());
					assertEquals(WidgetType.TEXT, ((Integer)args[1]).intValue());
					FakeWidget child = new FakeWidget(new Rectangle(0, 0, 0, 0));
					child.parent = this;
					children.add(child);
					created++;
					return child.widget;
				case "deleteAllChildren": children.clear(); deleted++; return null;
				case "setPos":
					x = (Integer)args[0]; y = (Integer)args[1];
					return widget;
				case "setSize":
					width = (Integer)args[0]; height = (Integer)args[1];
					return widget;
				case "setName": name=(String)args[0]; return widget;
				case "setText": text=(String)args[0]; return widget;
				case "setHidden": hidden=(Boolean)args[0]; return widget;
				case "setNoClickThrough": noClickThrough=(Boolean)args[0]; return null;
				case "revalidate": validated++; return null;
				default: return m.getReturnType() == Widget.class ? widget
					: defaultResult(m.getReturnType());
			}
		}

		FakeWidget soleChild()
		{
			assertEquals(1, children.size());
			return children.get(0);
		}
	}

	private static final class Fixture
	{
		final FakeWidget contents = new FakeWidget(CONTENT);
		final FakeWidget helmet = new FakeWidget(HELMET);
		final Item[][] equipped = {new Item[14]};
		final Client client;
		final EquipmentTotalWidget total;

		Fixture()
		{
			helmet.parent = contents;
			equipped[0][EquipmentSlot.HEAD] = new Item(100, 1);
			ItemContainer inventory = proxy(ItemContainer.class, (p,m,a) ->
				"getItems".equals(m.getName()) ? equipped[0] : defaultResult(m.getReturnType()));
			client = proxy(Client.class, (p,m,a) ->
			{
				if ("getWidget".equals(m.getName()))
				{
					int id = (Integer)a[0];
					if (id == InterfaceID.Equipment.SLOT0) return helmet.widget;
					if (id == InterfaceID.Equipment.CONTENTS) return contents.widget;
					return null;
				}
				if ("getItemContainer".equals(m.getName())) return inventory;
				return defaultResult(m.getReturnType());
			});
			ItemStatSource stats = id -> new EquipmentBonus(
				EquipmentSlot.HEAD, false, 0,0,0,0,0,0,0,0,0,0,
				id == 100 ? 25 : 40,0,BigDecimal.ZERO,0);
			total = new EquipmentTotalWidget(client, stats, GearScoreCalculator.wave1());
		}
	}

	@Test public void createsAnActualTextChildAboveHelmetAndKeepsItSingle()
	{
		Fixture f = new Fixture();
		f.total.refresh();
		FakeWidget label = f.contents.soleChild();
		assertEquals("Gear Score: 25.0", label.text);
		assertEquals("raccoon-coefficient-equipped-total", label.name);
		assertEquals(170, label.width);
		assertEquals(20, label.height);
		assertEquals(245 + 62 / 2 - 170 / 2 - 100, label.x);
		assertEquals(223 - 5 - 20 - 100, label.y);
		assertFalse(label.hidden);
		assertFalse(label.noClickThrough);
		assertTrue(label.validated > 0);
		f.total.refresh();
		assertEquals("Must not add duplicates on every game tick",1,f.contents.created);
	}

	@Test public void swapsChangeRealWidgetTextWithoutRecreatingIt()
	{
		Fixture f = new Fixture();
		f.total.refresh();
		FakeWidget label = f.contents.soleChild();
		f.equipped[0][EquipmentSlot.HEAD] = new Item(101,1);
		f.total.refresh();
		assertEquals("Gear Score: 40.0", label.text);
		assertEquals(1, f.contents.created);
	}

	@Test public void panelClosedOrHiddenHidesTheChildWithoutDeletingOthers()
	{
		Fixture f = new Fixture();
		f.total.refresh();
		FakeWidget owned = f.contents.soleChild();
		f.helmet.hidden = true;
		f.total.refresh();
		assertTrue(owned.hidden);
		assertEquals(0,f.contents.deleted);
		f.helmet.hidden = false;
		f.total.refresh();
		assertFalse(owned.hidden);
		assertEquals(1,f.contents.created);
	}

	@Test public void restartReusesItsPreviouslyOwnedTextAndDoesNotClearOtherPlugins()
	{
		Fixture f = new Fixture();
		f.total.refresh();
		FakeWidget owned = f.contents.soleChild();
		f.total.shutdown();
		assertTrue(owned.hidden);
		new EquipmentTotalWidget(f.client, id -> new EquipmentBonus(
			EquipmentSlot.HEAD,false,0,0,0,0,0,0,0,0,0,0,
			25,0,BigDecimal.ZERO,0), GearScoreCalculator.wave1()).refresh();
		assertEquals(1,f.contents.created);
		assertFalse(owned.hidden);
		assertEquals(0,f.contents.deleted);
	}

	@Test public void rejectsMissingAndTooSmallEquipmentWidgets()
	{
		Fixture f = new Fixture();
		f.contents.bounds = new Rectangle(0,0,30,30);
		f.total.refresh();
		assertEquals(0,f.contents.created);
	}

	@Test public void prefersContentsWhenImmediateSlotParentIsTooSmall()
	{
		Fixture f = new Fixture();
		FakeWidget tinySlotFrame = new FakeWidget(new Rectangle(245,223,62,62));
		f.helmet.parent = tinySlotFrame;
		f.total.refresh();
		assertEquals(1,f.contents.created);
		assertEquals(0,tinySlotFrame.created);
	}
}
