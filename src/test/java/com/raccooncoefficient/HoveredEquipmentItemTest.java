package com.raccooncoefficient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.runelite.api.MenuEntry;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/** Source-level hover routing checks; native RuneLite mouse tests remain pending. */
public class HoveredEquipmentItemTest
{
	private Widget widget(final int id, final int parentId, final int itemId)
	{
		return (Widget) Proxy.newProxyInstance(Widget.class.getClassLoader(),
			new Class<?>[] {Widget.class}, new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] arguments)
				{
					switch (method.getName())
					{
						case "getId":
							return id;
						case "getParentId":
							return parentId;
						case "getItemId":
							return itemId;
						default:
							return primitiveDefault(method.getReturnType());
					}
				}
			});
	}

	private MenuEntry entry(final Widget widget, final int itemId)
	{
		return (MenuEntry) Proxy.newProxyInstance(MenuEntry.class.getClassLoader(),
			new Class<?>[] {MenuEntry.class}, new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] arguments)
				{
					switch (method.getName())
					{
						case "getWidget":
							return widget;
						case "getItemId":
							return itemId;
						default:
							return primitiveDefault(method.getReturnType());
					}
				}
			});
	}

	private Object primitiveDefault(Class<?> type)
	{
		if (type == boolean.class)
		{
			return false;
		}
		if (type == int.class)
		{
			return 0;
		}
		return null;
	}

	@Test
	public void menuItemIdTakesPrecedenceOverContainerWidgetId()
	{
		Widget container = widget(InterfaceID.Inventory.ITEMS, 0, 999);
		assertEquals(4151, HoveredEquipmentItem.resolve(new MenuEntry[] {entry(container, 4151)}));
	}

	@Test
	public void directChildUsesItemContainerParentAndFallsBackToWidgetItem()
	{
		Widget child = widget(InterfaceID.Inventory.ITEMS + 1, InterfaceID.Inventory.ITEMS, 12954);
		assertEquals(12954, HoveredEquipmentItem.resolve(new MenuEntry[] {entry(child, -1)}));
	}

	@Test
	public void stackedEntriesPreferHighestPriorityEligibleItem()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 4151);
		Widget bank = widget(InterfaceID.Bankmain.ITEMS, 0, 11840);
		MenuEntry[] entries = {entry(inv, 4151), entry(bank, 11840), entry(null, -1)};
		assertEquals(11840, HoveredEquipmentItem.resolve(entries));
	}

	@Test
	public void skipsUnrelatedWidgetsEvenWithPlausibleItemIds()
	{
		Widget unrelated = widget(InterfaceID.Equipment.STABATT, 0, 4151);
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 11840);
		assertEquals(11840, HoveredEquipmentItem.resolve(
			new MenuEntry[] {entry(inv, 11840), entry(unrelated, 4151)}));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {entry(unrelated, 4151)}));
	}

	@Test
	public void fallsBackSafelyWhenMenuEntriesOrWidgetIdentityMissing()
	{
		assertEquals(EquipmentHover.NO_ITEM, HoveredEquipmentItem.resolve(null));
		assertEquals(EquipmentHover.NO_ITEM, HoveredEquipmentItem.resolve(new MenuEntry[0]));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {null, entry(null, 4151)}));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {
				entry(widget(InterfaceID.Inventory.ITEMS, 0, -1), -1)}));
	}

	@Test
	public void bankOfferAndWornSurfacesResolveWithoutScanningContainers()
	{
		assertEquals(11286, HoveredEquipmentItem.resolve(new MenuEntry[] {
			entry(widget(InterfaceID.Bankmain.ITEMS, 0, -1), 11286)}));
		assertEquals(4151, HoveredEquipmentItem.resolve(new MenuEntry[] {
			entry(widget(InterfaceID.GeOffersSide.ITEMS, 0, -1), 4151)}));
		assertEquals(10828, HoveredEquipmentItem.resolve(new MenuEntry[] {
			entry(widget((InterfaceID.WORNITEMS << 16) | 8, 0, 10828), -1)}));
	}

	@Test
	public void menuItemIdWithoutAssociatedAllowedWidgetIsNotEnough()
	{
		assertEquals(EquipmentHover.NO_ITEM, HoveredEquipmentItem.resolve(
			new MenuEntry[] {entry(null, 4151)}));
	}
}
