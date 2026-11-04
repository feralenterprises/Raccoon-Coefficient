package com.raccooncoefficient;

import javax.inject.Inject;
import net.runelite.api.gameval.InventoryID;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.events.WidgetLoaded;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;

@PluginDescriptor(
	name = "Raccoon Coefficient",
	description = "Number go up, brrr.",
	tags = {"gear", "equipment", "score"}
)
public class RaccoonCoefficientPlugin extends Plugin
{
	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ClientThread clientThread;

	@Inject
	private EquipmentTotalWidget equipmentTotalWidget;

	@Inject
	private GearScoreTooltipOverlay gearScoreTooltipOverlay;

	@Override
	protected void startUp()
	{
		// Tooltips remain an Overlay. The equipped total is a TEXT child
		// inside the Equipment modal, so it cannot be painted behind it.
		overlayManager.add(gearScoreTooltipOverlay);
		clientThread.invokeLater(equipmentTotalWidget::refresh);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(gearScoreTooltipOverlay);
		clientThread.invokeLater(equipmentTotalWidget::shutdown);
	}

	@Subscribe
	public void onWidgetLoaded(WidgetLoaded event)
	{
		if (event.getGroupId() == InterfaceID.EQUIPMENT)
		{
			equipmentTotalWidget.refresh();
		}
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event)
	{
		if (event.getContainerId() == InventoryID.WORN)
		{
			equipmentTotalWidget.refresh();
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		// Covers opening an already-loaded interface and any clientscript
		// reconstruction of its children; prevents a stale equipped total.
		equipmentTotalWidget.refresh();
	}
}
