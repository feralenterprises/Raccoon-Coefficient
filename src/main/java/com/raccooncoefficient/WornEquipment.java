package com.raccooncoefficient;

import java.util.ArrayList;
import java.util.List;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.gameval.InventoryID;
import com.raccooncoefficient.score.WornItem;

/** Reads worn equipment. Null means unavailable; a real empty container means empty. */
final class WornEquipment
{
	private WornEquipment()
	{
	}

	static List<WornItem> read(Client client)
	{
		ItemContainer container = client.getItemContainer(InventoryID.WORN);
		if (container == null)
		{
			return null; // unavailable container is not a confirmed empty loadout
		}

		Item[] items = container.getItems();
		List<WornItem> worn = new ArrayList<WornItem>(items.length);
		for (int slot = 0; slot < items.length; slot++)
		{
			Item item = items[slot];
			if (item == null || item.getId() < 0)
			{
				continue;
			}
			worn.add(new WornItem(slot, item.getId()));
		}
		return worn;
	}

	static boolean contains(List<WornItem> worn, int itemId)
	{
		for (int i = 0; i < worn.size(); i++)
		{
			if (worn.get(i).getItemId() == itemId)
			{
				return true;
			}
		}
		return false;
	}
}
