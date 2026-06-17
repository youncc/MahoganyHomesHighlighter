package com.mahoganyhomeshighlighter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;

@ConfigGroup(MahoganyHomesHighlighterConfig.GROUP_NAME)
public interface MahoganyHomesHighlighterConfig extends Config
{
	String GROUP_NAME = "mahoganyhomeshighlighter";
	String CURRENT_HOME_KEY = "currentHome";

	@ConfigSection(
		name = "Highlight colors",
		description = "Colors used when highlighting contract objects",
		position = 0
	)
	String colorSection = "colorSection";

	@Alpha
	@ConfigItem(
		keyName = "removeColor",
		name = "Remove color",
		description = "Highlight color for furniture that needs to be removed",
		section = colorSection,
		position = 0
	)
	default Color removeColor()
	{
		return new Color(255, 0, 0, 80);
	}

	@Alpha
	@ConfigItem(
		keyName = "buildColor",
		name = "Build color",
		description = "Highlight color for hotspots and furniture that needs to be built",
		section = colorSection,
		position = 1
	)
	default Color buildColor()
	{
		return new Color(0, 255, 0, 80);
	}

	@Alpha
	@ConfigItem(
		keyName = "repairColor",
		name = "Repair color",
		description = "Highlight color for furniture that needs to be repaired",
		section = colorSection,
		position = 2
	)
	default Color repairColor()
	{
		return new Color(255, 165, 0, 80);
	}

	@ConfigSection(
		name = "Door highlighting",
		description = "Highlight doors in your current contract house",
		position = 50
	)
	String doorSection = "doorSection";

	@ConfigItem(
		keyName = "highlightDoors",
		name = "Highlight doors",
		description = "Highlight doors inside your current Mahogany Homes contract house",
		section = doorSection,
		position = 0
	)
	default boolean highlightDoors()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "openDoorColor",
		name = "Open door color",
		description = "Highlight color for open doors",
		section = doorSection,
		position = 1
	)
	default Color openDoorColor()
	{
		return new Color(0, 200, 200, 80);
	}

	@Alpha
	@ConfigItem(
		keyName = "closedDoorColor",
		name = "Closed door color",
		description = "Highlight color for closed doors",
		section = doorSection,
		position = 2
	)
	default Color closedDoorColor()
	{
		return new Color(160, 80, 255, 80);
	}

	@ConfigItem(
		keyName = "showDoorStatusText",
		name = "Show door status",
		description = "Show Open or Closed text above doors in your contract house",
		section = doorSection,
		position = 3
	)
	default boolean showDoorStatusText()
	{
		return false;
	}

	@ConfigSection(
		name = "Render options",
		description = "Options for how objects are highlighted",
		position = 100,
		closedByDefault = true
	)
	String renderSection = "renderSection";

	@ConfigItem(
		keyName = "highlightHull",
		name = "Highlight hull",
		description = "Highlight the object hull",
		section = renderSection,
		position = 0
	)
	default boolean highlightHull()
	{
		return true;
	}

	@ConfigItem(
		keyName = "highlightClickbox",
		name = "Highlight clickbox",
		description = "Highlight the object clickbox",
		section = renderSection,
		position = 1
	)
	default boolean highlightClickbox()
	{
		return false;
	}
}
