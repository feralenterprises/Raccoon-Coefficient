package com.raccooncoefficient;

import net.runelite.api.gameval.InterfaceID;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class EquipmentHoverTest
{
	@Test
	public void inventoryAndEquipmentSurfacesExposeTheHoveredItem()
	{
		assertEquals(4151, EquipmentHover.itemId(InterfaceID.INVENTORY, InterfaceID.Inventory.ITEMS, 0, 4151, -1));
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(InterfaceID.INVENTORY, InterfaceID.Inventory.ITEMS, 0, 0, 4151));
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(0, 1, 0, 4151, -1));

		assertEquals(1201, EquipmentHover.itemId(InterfaceID.WORNITEMS, 0, 0, 999, 1201));
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(InterfaceID.WORNITEMS, 0, 0, 1201, -1));

		assertEquals(88, EquipmentHover.itemId(InterfaceID.EQUIPMENT_SIDE, 5, 0, 88, -1));
		assertEquals(9, EquipmentHover.itemId(InterfaceID.EQUIPMENT, InterfaceID.Equipment.SLOT3, 0, 9, 8));
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(InterfaceID.EQUIPMENT, InterfaceID.Equipment.STABATT, 0, 9, -1));
	}

	@Test
	public void bankAndOfferSurfacesAreHoverTargetsWithoutScanningThem()
	{
		assertEquals(50, EquipmentHover.itemId(InterfaceID.BANKMAIN, InterfaceID.Bankmain.ITEMS, 0, 50, -1));
		assertEquals(44, EquipmentHover.itemId(InterfaceID.BANKMAIN, 0, InterfaceID.Bankside.WORNOPS, 1, 44));
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(InterfaceID.BANKMAIN, 0, 0, 44, 1));
		assertEquals(77, EquipmentHover.itemId(InterfaceID.BANKSIDE, 123, 0, 77, -1));
		assertEquals(61, EquipmentHover.itemId(InterfaceID.SHARED_BANK, InterfaceID.SharedBank.ITEMS, 0, 61, -1));
		assertEquals(62, EquipmentHover.itemId(InterfaceID.SHARED_BANK_SIDE, 4, 0, 62, -1));
		assertEquals(63, EquipmentHover.itemId(InterfaceID.GE_OFFERS, InterfaceID.GeOffersSide.ITEMS, 0, 63, -1));
	}

	@Test
	public void unrelatedWidgetsAreNotCompared()
	{
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(InterfaceID.PRAYERBOOK, InterfaceID.Equipment.PRAYER, 0, 4151, 4151));
		assertEquals(EquipmentHover.NO_ITEM, EquipmentHover.itemId(InterfaceID.INVENTORY, InterfaceID.Inventory.ITEMS, 0, -1, -1));
	}
}
