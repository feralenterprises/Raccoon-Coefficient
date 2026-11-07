package com.raccooncoefficient;

import com.raccooncoefficient.score.ScoringModel;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;

@ConfigGroup(RaccoonCoefficientConfig.GROUP)
public interface RaccoonCoefficientConfig extends Config
{
	String GROUP = "raccooncoefficient";

	@ConfigItem(
		keyName = "scoringModel",
		name = "Scoring model",
		description = "Locked v1 is the approved Gear Score. Experimental E1 is a PROPOSED, unapproved layer"
			+ " that adds attack speed, ammunition, powered-staff and set/passive adjustments; it is labelled E1.",
		position = 0
	)
	default ScoringModel scoringModel()
	{
		return ScoringModel.LOCKED_V1;
	}
}
