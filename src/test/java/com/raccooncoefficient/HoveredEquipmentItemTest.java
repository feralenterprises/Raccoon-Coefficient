package com.raccooncoefficient;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import net.runelite.api.MenuAction;
import net.runelite.api.MenuEntry;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Source-level default-action and menu type checks. These prove the policy
 * against synthetic RuneLite entries, not a real in-game mouse hover.
 */
public class HoveredEquipmentItemTest
{
	private Widget widget(final int id, final int parentId, final int itemId)
	{
		return (Widget) Proxy.newProxyInstance(Widget.class.getClassLoader(),
			new Class<?>[] {Widget.class}, new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
				{
					switch (method.getName())
					{
						case "getId": return id;
						case "getParentId": return parentId;
						case "getItemId": return itemId;
						default: return primitiveDefault(method.getReturnType());
					}
				}
			});
	}

	private MenuEntry entry(final Widget widget, final int itemId,
		final MenuAction type, final int param1, final String option)
	{
		return (MenuEntry) Proxy.newProxyInstance(MenuEntry.class.getClassLoader(),
			new Class<?>[] {MenuEntry.class}, new InvocationHandler()
			{
				@Override
				public Object invoke(Object proxy, Method method, Object[] args)
				{
					switch (method.getName())
					{
						case "getWidget": return widget;
						case "getItemId": return itemId;
						case "getType": return type;
						case "getParam1": return param1;
						case "getOption": return option;
						default: return primitiveDefault(method.getReturnType());
					}
				}
			});
	}

	private MenuEntry item(Widget widget, int id)
	{
		return entry(widget, id, MenuAction.ITEM_FIRST_OPTION, widget == null ? 0 : widget.getId(), "Wear");
	}

	private static Object primitiveDefault(Class<?> type)
	{
		if (type == boolean.class) return false;
		if (type == int.class) return 0;
		return null;
	}

	@Test
	public void explicitMenuItemIdBeatsContainerWidgetItemId()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 999);
		assertEquals(4151, HoveredEquipmentItem.resolve(new MenuEntry[] {item(inv, 4151)}));
	}

	@Test
	public void itemChildFallbackUsesItsOwnIdentity()
	{
		Widget child = widget(InterfaceID.Inventory.ITEMS + 1, InterfaceID.Inventory.ITEMS, 12954);
		assertEquals(12954, HoveredEquipmentItem.resolve(new MenuEntry[] {item(child, -1)}));
	}

	@Test
	public void latestDefaultItemWinsAmongSeveralItemActions()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 4151);
		Widget bank = widget(InterfaceID.Bankmain.ITEMS, 0, 11840);
		assertEquals(11840, HoveredEquipmentItem.resolve(new MenuEntry[] {
			item(inv, 4151), item(bank, 11840)
		}));
	}

	@Test
	public void unrelatedTopActionMustNotExposeAnItemBehindIt()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 4151);
		MenuEntry npc = entry(inv, 4151, MenuAction.NPC_FIRST_OPTION, inv.getId(), "Attack");
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {item(inv, 4151), npc}));
	}

	@Test
	public void pluginOverlayAndLowPriorityActionsCannotExposeBackgroundItems()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 4151);
		for (MenuAction type : new MenuAction[] {
			MenuAction.RUNELITE_OVERLAY,
			MenuAction.RUNELITE_OVERLAY_CONFIG,
			MenuAction.CC_OP_LOW_PRIORITY,
			MenuAction.RUNELITE_LOW_PRIORITY,
			MenuAction.EXAMINE_ITEM_GROUND
		})
		{
			assertEquals("type=" + type, EquipmentHover.NO_ITEM,
				HoveredEquipmentItem.resolve(new MenuEntry[] {
					item(inv, 4151), entry(inv, 4151, type, inv.getId(), "Other")
				}));
		}
	}

	@Test
	public void ccOpFromInventoryOrEquipmentSideIsAnItemAction()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, -1);
		assertEquals(4151, HoveredEquipmentItem.resolve(new MenuEntry[] {
			entry(inv, 4151, MenuAction.CC_OP, InterfaceID.Inventory.ITEMS, "Wield")
		}));
		Widget side = widget((InterfaceID.EQUIPMENT_SIDE << 16) | 5, 0, 10828);
		assertEquals(10828, HoveredEquipmentItem.resolve(new MenuEntry[] {
			entry(side, -1, MenuAction.CC_OP,
				(InterfaceID.EQUIPMENT_SIDE << 16) | 5, "Remove")
		}));
	}

	@Test
	public void ccOpFromUnrelatedInterfaceCannotClaimAnItem()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, 4151);
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {
				entry(inv, 4151, MenuAction.CC_OP,
					(InterfaceID.PRAYERBOOK << 16) | 2, "Pray")
			}));
	}

	@Test
	public void noItemIdentityOrMissingMenuInformationFailsClosed()
	{
		Widget inv = widget(InterfaceID.Inventory.ITEMS, 0, -1);
		assertEquals(EquipmentHover.NO_ITEM, HoveredEquipmentItem.resolve(null));
		assertEquals(EquipmentHover.NO_ITEM, HoveredEquipmentItem.resolve(new MenuEntry[0]));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {null}));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {item(null, 4151)}));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {item(inv, -1)}));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {
				entry(inv, 4151, null, inv.getId(), "Wear")
			}));
		assertEquals(EquipmentHover.NO_ITEM,
			HoveredEquipmentItem.resolve(new MenuEntry[] {
				entry(inv, 4151, MenuAction.ITEM_FIRST_OPTION, inv.getId(), null)
			}));
	}

	@Test
	public void bankGrandExchangeAndWornSurfacesAreStillSupported()
	{
		assertEquals(11286, HoveredEquipmentItem.resolve(new MenuEntry[] {
			item(widget(InterfaceID.Bankmain.ITEMS, 0, -1), 11286)
		}));
		assertEquals(4151, HoveredEquipmentItem.resolve(new MenuEntry[] {
			item(widget(InterfaceID.GeOffersSide.ITEMS, 0, -1), 4151)
		}));
		assertEquals(10828, HoveredEquipmentItem.resolve(new MenuEntry[] {
			item(widget((InterfaceID.WORNITEMS << 16) | 8, 0, 10828), -1)
		}));
	}

	@Test
	public void nonItemWidgetNeverBecomesAHitEvenIfActionIsItem()
	{
		Widget unrelated = widget(InterfaceID.Equipment.STABATT, 0, 4151);
		assertEquals(EquipmentHover.NO_ITEM, HoveredEquipmentItem.resolve(new MenuEntry[] {
			item(unrelated, 4151)
		}));
	}
}
