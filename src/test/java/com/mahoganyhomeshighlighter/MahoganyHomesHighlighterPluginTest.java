package com.mahoganyhomeshighlighter;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public class MahoganyHomesHighlighterPluginTest
{
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(MahoganyHomesHighlighterPlugin.class);
		RuneLite.main(args);
	}
}
