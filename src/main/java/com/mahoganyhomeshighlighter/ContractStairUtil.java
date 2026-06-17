package com.mahoganyhomeshighlighter;

import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.ObjectComposition;

final class ContractStairUtil
{
	enum ClimbDirection
	{
		UP,
		DOWN
	}

	private ContractStairUtil()
	{
	}

	@Nullable
	static ClimbDirection getClimbDirection(final Client client, final int objectId)
	{
		final ObjectComposition composition = getResolvedComposition(client, objectId);
		if (composition == null)
		{
			return null;
		}

		final String[] actions = composition.getActions();
		if (actions == null)
		{
			return null;
		}

		boolean canClimbUp = false;
		boolean canClimbDown = false;

		for (final String action : actions)
		{
			if (action == null)
			{
				continue;
			}

			if ("Climb-up".equalsIgnoreCase(action))
			{
				canClimbUp = true;
			}
			else if ("Climb-down".equalsIgnoreCase(action))
			{
				canClimbDown = true;
			}
		}

		if (canClimbUp && !canClimbDown)
		{
			return ClimbDirection.UP;
		}

		if (canClimbDown && !canClimbUp)
		{
			return ClimbDirection.DOWN;
		}

		return null;
	}

	static boolean shouldHighlightStairs(
		final Home home,
		final int playerPlane,
		final int countOnFloor,
		final int remainingTasks)
	{
		final boolean onSameFloorAsNpc = isOnHomeownerFloor(home, playerPlane);

		if (remainingTasks == 0)
		{
			return !onSameFloorAsNpc;
		}

		return countOnFloor != remainingTasks || (!onSameFloorAsNpc && countOnFloor == 0);
	}

	static boolean shouldHighlightLadder(
		final Home home,
		final ClimbDirection climbDirection,
		final int playerPlane,
		final int tasksAbove,
		final int tasksBelow,
		final int remainingTasks)
	{
		if (remainingTasks == 0)
		{
			if (home.isNpcUpstairs())
			{
				return climbDirection == ClimbDirection.UP && playerPlane == 0;
			}

			return climbDirection == ClimbDirection.DOWN && playerPlane > 0;
		}

		if (climbDirection == ClimbDirection.UP)
		{
			return tasksAbove > 0;
		}

		return tasksBelow > 0;
	}

	@Nullable
	static String getStairLabel(
		final Home home,
		final ClimbDirection climbDirection,
		final int playerPlane,
		final int tasksAbove,
		final int tasksBelow,
		final int remainingTasks)
	{
		if (remainingTasks == 0)
		{
			if (home.isNpcUpstairs() && climbDirection == ClimbDirection.UP && playerPlane == 0)
			{
				return "Speak to " + home.getDisplayName();
			}

			if (!home.isNpcUpstairs() && climbDirection == ClimbDirection.DOWN && playerPlane > 0)
			{
				return "Speak to " + home.getDisplayName();
			}

			return null;
		}

		if (climbDirection == ClimbDirection.UP && tasksAbove > 0)
		{
			return formatTaskCount(tasksAbove, "upstairs");
		}

		if (climbDirection == ClimbDirection.DOWN && tasksBelow > 0)
		{
			return formatTaskCount(tasksBelow, "below");
		}

		return null;
	}

	static boolean isOnHomeownerFloor(final Home home, final int playerPlane)
	{
		return home.isNpcUpstairs() ? playerPlane > 0 : playerPlane == 0;
	}

	static int countTasksAbove(final int playerPlane, final int countOnFloor, final int remainingTasks, final int visibleAbove)
	{
		if (visibleAbove > 0)
		{
			return visibleAbove;
		}

		final int tasksElsewhere = remainingTasks - countOnFloor;
		if (tasksElsewhere > 0 && playerPlane == 0)
		{
			return tasksElsewhere;
		}

		return 0;
	}

	static int countTasksBelow(final int playerPlane, final int countOnFloor, final int remainingTasks, final int visibleBelow)
	{
		if (visibleBelow > 0)
		{
			return visibleBelow;
		}

		final int tasksElsewhere = remainingTasks - countOnFloor;
		if (tasksElsewhere > 0 && playerPlane > 0)
		{
			return tasksElsewhere;
		}

		return 0;
	}

	private static String formatTaskCount(final int count, final String location)
	{
		return count + (count == 1 ? " task " : " tasks ") + location;
	}

	@Nullable
	private static ObjectComposition getResolvedComposition(final Client client, final int objectId)
	{
		ObjectComposition composition = client.getObjectDefinition(objectId);
		if (composition == null)
		{
			return null;
		}

		if (composition.getImpostorIds() != null)
		{
			composition = composition.getImpostor();
		}

		return composition;
	}
}
