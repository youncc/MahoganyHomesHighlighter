package com.mahoganyhomeshighlighter;

import javax.annotation.Nullable;

enum HotspotAction
{
	REPAIR(1),
	REMOVE(3),
	BUILD(4);

	private final int varbValue;

	HotspotAction(final int varbValue)
	{
		this.varbValue = varbValue;
	}

	String getLabel()
	{
		switch (this)
		{
			case REMOVE:
				return "Remove";
			case BUILD:
				return "Build";
			case REPAIR:
				return "Repair";
			default:
				return name();
		}
	}

	@Nullable
	static HotspotAction fromVarbValue(final int value)
	{
		for (final HotspotAction action : values())
		{
			if (action.varbValue == value)
			{
				return action;
			}
		}
		return null;
	}
}
