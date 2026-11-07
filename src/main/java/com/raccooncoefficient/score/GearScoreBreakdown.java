package com.raccooncoefficient.score;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** Base equipment score plus the set and weapon-passive terms. */
public final class GearScoreBreakdown
{
	private final BigDecimal baseEquipment;
	private final BigDecimal setBonus;
	private final BigDecimal weaponPassive;
	private final boolean itemDataComplete;
	private final boolean extensionDataComplete;
	private final int unknownItems;
	private final String modelLabel;

	public GearScoreBreakdown(
		BigDecimal baseEquipment,
		BigDecimal setBonus,
		BigDecimal weaponPassive,
		boolean itemDataComplete,
		boolean extensionDataComplete,
		int unknownItems)
	{
		this(baseEquipment, setBonus, weaponPassive, itemDataComplete, extensionDataComplete, unknownItems, null);
	}

	public GearScoreBreakdown(
		BigDecimal baseEquipment,
		BigDecimal setBonus,
		BigDecimal weaponPassive,
		boolean itemDataComplete,
		boolean extensionDataComplete,
		int unknownItems,
		String modelLabel)
	{
		this.modelLabel = modelLabel;
		this.baseEquipment = baseEquipment;
		this.setBonus = setBonus;
		this.weaponPassive = weaponPassive;
		this.itemDataComplete = itemDataComplete;
		this.extensionDataComplete = extensionDataComplete;
		this.unknownItems = unknownItems;
	}

	public BigDecimal getBaseEquipment()
	{
		return baseEquipment;
	}

	public BigDecimal getSetBonus()
	{
		return setBonus;
	}

	public BigDecimal getWeaponPassive()
	{
		return weaponPassive;
	}

	public boolean isItemDataComplete()
	{
		return itemDataComplete;
	}

	public boolean isExtensionDataComplete()
	{
		return extensionDataComplete;
	}

	public boolean isComplete()
	{
		return itemDataComplete && extensionDataComplete;
	}

	public int getUnknownItems()
	{
		return unknownItems;
	}

	public BigDecimal totalUnrounded()
	{
		return baseEquipment.add(setBonus).add(weaponPassive);
	}

	public BigDecimal displayedTotal()
	{
		return totalUnrounded().setScale(1, RoundingMode.HALF_UP);
	}

	/** Null for the locked v1 contract; e.g. "E1" for the experimental layer. */
	public String getModelLabel()
	{
		return modelLabel;
	}
}
