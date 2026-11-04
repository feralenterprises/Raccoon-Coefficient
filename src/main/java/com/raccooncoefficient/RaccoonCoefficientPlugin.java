package com.raccooncoefficient;

import javax.inject.Inject;
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
	private EquipmentScoreOverlay equipmentScoreOverlay;

	@Inject
	private GearScoreTooltipOverlay gearScoreTooltipOverlay;

	@Override
	protected void startUp()
	{
		overlayManager.add(equipmentScoreOverlay);
		overlayManager.add(gearScoreTooltipOverlay);
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(equipmentScoreOverlay);
		overlayManager.remove(gearScoreTooltipOverlay);
	}
}
