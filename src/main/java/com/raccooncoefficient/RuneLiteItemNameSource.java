package com.raccooncoefficient;

import com.raccooncoefficient.score.SetEffectDetector;
import javax.inject.Inject;
import net.runelite.api.ItemComposition;
import net.runelite.client.game.ItemManager;

/** Item identity adapter. Never reads bank, accounts, or other plugins. */
public final class RuneLiteItemNameSource implements SetEffectDetector.ItemNameSource
{
	private final ItemManager itemManager;

	@Inject
	public RuneLiteItemNameSource(ItemManager itemManager)
	{
		this.itemManager = itemManager;
	}

	@Override
	public String nameOf(int itemId)
	{
		ItemComposition item = itemManager.getItemComposition(itemId);
		return item == null ? null : item.getMembersName();
	}
}
