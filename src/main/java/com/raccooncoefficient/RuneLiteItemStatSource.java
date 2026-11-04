package com.raccooncoefficient;

import com.raccooncoefficient.score.EquipmentBonus;
import com.raccooncoefficient.score.ItemStatSource;
import java.math.BigDecimal;
import javax.inject.Inject;
import net.runelite.client.game.ItemEquipmentStats;
import net.runelite.client.game.ItemManager;
import net.runelite.client.game.ItemStats;

/**
 * Adapts RuneLite's item stat database. Null, non-equipable, and non-finite
 * magic damage stay unknown. They are not reported as zero.
 */
public final class RuneLiteItemStatSource implements ItemStatSource
{
	private final ItemManager itemManager;

	@Inject
	public RuneLiteItemStatSource(ItemManager itemManager)
	{
		this.itemManager = itemManager;
	}

	@Override
	public EquipmentBonus getVerifiedBonus(int itemId)
	{
		return fromItemStats(itemManager.getItemStats(itemId));
	}

	public static EquipmentBonus fromItemStats(ItemStats stats)
	{
		if (stats == null || !stats.isEquipable())
		{
			return null;
		}

		ItemEquipmentStats equipment = stats.getEquipment();
		if (equipment == null || !Float.isFinite(equipment.getMdmg()))
		{
			return null;
		}

		return new EquipmentBonus(
			equipment.getSlot(),
			equipment.isTwoHanded(),
			equipment.getAstab(),
			equipment.getAslash(),
			equipment.getAcrush(),
			equipment.getAmagic(),
			equipment.getArange(),
			equipment.getDstab(),
			equipment.getDslash(),
			equipment.getDcrush(),
			equipment.getDmagic(),
			equipment.getDrange(),
			equipment.getStr(),
			equipment.getRstr(),
			new BigDecimal(Float.toString(equipment.getMdmg())),
			equipment.getPrayer());
	}
}
