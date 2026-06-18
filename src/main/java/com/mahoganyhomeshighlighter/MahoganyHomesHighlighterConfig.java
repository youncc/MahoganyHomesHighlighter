package com.mahoganyhomeshighlighter;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(MahoganyHomesHighlighterConfig.GROUP_NAME)
public interface MahoganyHomesHighlighterConfig extends Config
{
	String GROUP_NAME = "mahoganyhomeshighlighter";
	String CURRENT_HOME_KEY = "currentHome";

	@ConfigSection(
		name = "Furniture highlighting",
		description = "Highlight contract furniture by task type",
		position = 0
	)
	String furnitureSection = "furnitureSection";

	@ConfigItem(
		keyName = "highlightFurniture",
		name = "Highlight furniture",
		description = "Highlight contract furniture that needs to be removed, built, or repaired",
		section = furnitureSection,
		position = 0
	)
	default boolean highlightFurniture()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "removeColor",
		name = "Remove color",
		description = "Highlight color for furniture that needs to be removed",
		section = furnitureSection,
		position = 1
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
		section = furnitureSection,
		position = 2
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
		section = furnitureSection,
		position = 3
	)
	default Color repairColor()
	{
		return new Color(255, 165, 0, 80);
	}

	@ConfigItem(
		keyName = "showFurnitureActionText",
		name = "Show action",
		description = "Show Remove, Build, or Repair text on contract furniture",
		section = furnitureSection,
		position = 4
	)
	default boolean showFurnitureActionText()
	{
		return false;
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
		description = "Show Open or Closed text on doors in your contract house",
		section = doorSection,
		position = 3
	)
	default boolean showDoorStatusText()
	{
		return false;
	}

	@ConfigSection(
		name = "Stair highlighting",
		description = "Highlight stairs and ladders when tasks remain on another floor",
		position = 75
	)
	String stairSection = "stairSection";

	@ConfigItem(
		keyName = "highlightStairs",
		name = "Highlight stairs",
		description = "Highlight stairs and ladders that lead toward remaining contract tasks using the render options below",
		section = stairSection,
		position = 0
	)
	default boolean highlightStairs()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "stairColor",
		name = "Stair color",
		description = "Highlight color for stairs and ladders",
		section = stairSection,
		position = 1
	)
	default Color stairColor()
	{
		return new Color(255, 255, 0, 80);
	}

	@ConfigItem(
		keyName = "showStairStatusText",
		name = "Show stair status",
		description = "Show task counts or turn-in hints on relevant stairs. Text only; visuals use the render options",
		section = stairSection,
		position = 2
	)
	default boolean showStairStatusText()
	{
		return true;
	}

	@ConfigSection(
		name = "Homeowner highlighting",
		description = "Highlight the contract homeowner when all tasks are complete",
		position = 80
	)
	String homeownerSection = "homeownerSection";

	@ConfigItem(
		keyName = "highlightHomeowner",
		name = "Highlight homeowner",
		description = "Highlight the contract homeowner when all tasks are complete",
		section = homeownerSection,
		position = 0
	)
	default boolean highlightHomeowner()
	{
		return true;
	}

	@Alpha
	@ConfigItem(
		keyName = "homeownerColor",
		name = "Homeowner color",
		description = "Highlight color for the contract homeowner",
		section = homeownerSection,
		position = 1
	)
	default Color homeownerColor()
	{
		return new Color(0, 255, 255, 80);
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
		keyName = "highlightOutline",
		name = "Highlight outline",
		description = "Highlight the 3D model outline",
		section = renderSection,
		position = 1
	)
	default boolean highlightOutline()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightClickbox",
		name = "Highlight clickbox",
		description = "Highlight the object clickbox",
		section = renderSection,
		position = 2
	)
	default boolean highlightClickbox()
	{
		return false;
	}

	@ConfigItem(
		keyName = "highlightTile",
		name = "Highlight tile",
		description = "Highlight the tile under the object",
		section = renderSection,
		position = 3
	)
	default boolean highlightTile()
	{
		return false;
	}

	@ConfigItem(
		keyName = "borderWidth",
		name = "Border width",
		description = "Width of highlighted borders",
		section = renderSection,
		position = 4
	)
	default double borderWidth()
	{
		return 2;
	}

	@ConfigItem(
		keyName = "outlineFeather",
		name = "Outline feather",
		description = "Specify between 0-4 how much of the model outline should be faded",
		section = renderSection,
		position = 5
	)
	@Range(
		min = 0,
		max = 4
	)
	default int outlineFeather()
	{
		return 0;
	}

	@ConfigItem(
		keyName = "fillOpacity",
		name = "Fill opacity",
		description = "Opacity of hull and NPC highlight fills",
		section = renderSection,
		position = 6
	)
	@Range(
		max = 255
	)
	default int fillOpacity()
	{
		return 50;
	}

	@Alpha
	@ConfigItem(
		keyName = "clickboxBorderColor",
		name = "Clickbox border color",
		description = "Border color for highlighted clickboxes",
		section = renderSection,
		position = 7
	)
	default Color clickboxBorderColor()
	{
		return Color.WHITE;
	}

	@Alpha
	@ConfigItem(
		keyName = "clickboxHoverBorderColor",
		name = "Clickbox hover border color",
		description = "Border color for highlighted clickboxes when hovered",
		section = renderSection,
		position = 8
	)
	default Color clickboxHoverBorderColor()
	{
		return Color.GRAY;
	}

	@ConfigItem(
		keyName = "statusTextPosition",
		name = "Status text position",
		description = "Where door, stair, and furniture status text is drawn relative to the object",
		section = renderSection,
		position = 9
	)
	default StatusTextPosition statusTextPosition()
	{
		return StatusTextPosition.ABOVE;
	}
}
