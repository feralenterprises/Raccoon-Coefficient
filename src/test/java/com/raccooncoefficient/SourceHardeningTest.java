package com.raccooncoefficient;

import com.raccooncoefficient.score.GearScoreBreakdown;
import com.raccooncoefficient.score.GearScoreCalculator;
import com.raccooncoefficient.score.GearScoreText;
import com.raccooncoefficient.score.WornItem;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/** Adversarial source tests: no logged-in game session or real hover evidence. */
public class SourceHardeningTest
{
	@Test
	public void itemContainerWidgetIsNotAutomaticallyAnItemIfMenuLacksAnItemId()
	{
		assertEquals(EquipmentHover.NO_ITEM,
			EquipmentHover.menuItemId(InterfaceID.INVENTORY,
				InterfaceID.Inventory.ITEMS, 0, -1, 4151));
		assertEquals(EquipmentHover.NO_ITEM,
			EquipmentHover.menuItemId(InterfaceID.BANKMAIN,
				InterfaceID.Bankmain.ITEMS, 0, -1, 11840));
		assertEquals(EquipmentHover.NO_ITEM,
			EquipmentHover.menuItemId(InterfaceID.GE_OFFERS,
				InterfaceID.GeOffersSide.ITEMS, 0, -1, 11286));
	}

	@Test
	public void explicitMenuItemOrItemChildStillResolves()
	{
		assertEquals(4151,
			EquipmentHover.menuItemId(InterfaceID.INVENTORY,
				InterfaceID.Inventory.ITEMS, 0, 4151, 999));
		assertEquals(12954,
			EquipmentHover.menuItemId(InterfaceID.INVENTORY,
				InterfaceID.Inventory.ITEMS + 1, InterfaceID.Inventory.ITEMS, -1, 12954));
		assertEquals(11286,
			EquipmentHover.menuItemId(InterfaceID.BANKMAIN,
				InterfaceID.Bankmain.ITEMS + 1, InterfaceID.Bankmain.ITEMS, -1, 11286));
		assertEquals(EquipmentHover.NO_ITEM,
			EquipmentHover.menuItemId(InterfaceID.PRAYERBOOK,
				InterfaceID.Equipment.STABATT, 0, 4151, 4151));
	}

	@Test
	public void unavailableWornContainerIsNotAnEmptyKit()
	{
		assertNull(WornEquipment.read(client(null)));
		assertNull(WornEquipment.read(client(container(null))));
		assertFalse(WornEquipment.contains(null, 4151));
		assertFalse(WornEquipment.contains(Collections.<WornItem>emptyList(), 4151));

		List<WornItem> actualEmpty = WornEquipment.read(client(container(new Item[0])));
		assertTrue(actualEmpty.isEmpty());
	}

	@Test
	public void wave1NullListIsNotCertifiedAsComplete()
	{
		GearScoreCalculator calculator = GearScoreCalculator.wave1();
		GearScoreBreakdown unknown = calculator.score(null, null);
		assertFalse(unknown.isComplete());
		assertEquals("Gear Score: 0.0 ?", GearScoreText.equipmentLine(unknown));
		GearScoreBreakdown observedEmpty = calculator.score(
			Collections.<WornItem>emptyList(), null);
		assertTrue(observedEmpty.isComplete());
		assertEquals("Gear Score: 0.0", GearScoreText.equipmentLine(observedEmpty));
	}

	private static ItemContainer container(final Item[] items)
	{
		return (ItemContainer) Proxy.newProxyInstance(
			ItemContainer.class.getClassLoader(), new Class<?>[] {ItemContainer.class},
			new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] arguments)
				{
					if ("getItems".equals(method.getName()))
					{
						return items;
					}
					return primitiveDefault(method.getReturnType());
				}
			});
	}

	private static Client client(final ItemContainer itemContainer)
	{
		return (Client) Proxy.newProxyInstance(
			Client.class.getClassLoader(), new Class<?>[] {Client.class},
			new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] arguments)
				{
					if ("getItemContainer".equals(method.getName()))
					{
						return itemContainer;
					}
					return primitiveDefault(method.getReturnType());
				}
			});
	}

	private static Object primitiveDefault(Class<?> type)
	{
		if (type == int.class)
		{
			return 0;
		}
		if (type == boolean.class)
		{
			return false;
		}
		return null;
	}
}
