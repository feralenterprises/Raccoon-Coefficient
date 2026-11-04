package com.raccooncoefficient.score;

/** One occupied index in the worn-equipment container. */
public final class WornItem
{
	private final int slotIndex;
	private final int itemId;

	public WornItem(int slotIndex, int itemId)
	{
		this.slotIndex = slotIndex;
		this.itemId = itemId;
	}

	public int getSlotIndex()
	{
		return slotIndex;
	}

	public int getItemId()
	{
		return itemId;
	}
}
