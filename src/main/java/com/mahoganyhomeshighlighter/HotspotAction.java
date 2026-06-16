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
