package com.mahoganyhomeshighlighter;

import javax.annotation.Nullable;
import lombok.Getter;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.text.WordUtils;

@Getter
enum Home
{
	JESS(new WorldArea(2611, 3290, 14, 7, 0)),
	NOELLA(new WorldArea(2652, 3317, 15, 8, 0)),
	ROSS(new WorldArea(2609, 3313, 11, 9, 0)),
	LARRY(new WorldArea(3033, 3360, 10, 9, 0)),
	NORMAN(new WorldArea(3034, 3341, 8, 8, 0)),
	TAU(new WorldArea(3043, 3340, 10, 11, 0)),
	BARBARA(new WorldArea(1746, 3531, 10, 11, 0)),
	LEELA(new WorldArea(1781, 3589, 9, 8, 0)),
	MARIAH(new WorldArea(1762, 3618, 10, 7, 0)),
	BOB(new WorldArea(3234, 3482, 10, 10, 0)),
	JEFF(new WorldArea(3235, 3445, 10, 12, 0)),
	SARAH(new WorldArea(3232, 3381, 8, 7, 0));

	private final WorldArea area;

	Home(final WorldArea area)
	{
		this.area = area;
	}

	String getDisplayName()
	{
		return WordUtils.capitalize(name().toLowerCase());
	}

	@Nullable
	static Home getByName(final String name)
	{
		for (final Home home : values())
		{
			if (home.getDisplayName().equalsIgnoreCase(name))
			{
				return home;
			}
		}
		return null;
	}

	@Nullable
	static Home getByLocation(final WorldPoint location)
	{
		for (final Home home : values())
		{
			if (home.containsLocation(location))
			{
				return home;
			}
		}
		return null;
	}

	@Nullable
	static Home getByEnumName(final String name)
	{
		if (name == null || name.isEmpty())
		{
			return null;
		}

		try
		{
			return valueOf(name.trim().toUpperCase());
		}
		catch (IllegalArgumentException e)
		{
			return null;
		}
	}

	boolean containsLocation(final WorldPoint location)
	{
		return area.contains(new WorldPoint(location.getX(), location.getY(), area.getPlane()));
	}
}
