package com.raccooncoefficient;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class RaccoonCoefficientPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(RaccoonCoefficientPlugin.class);
		RuneLite.main(args);
	}
}
