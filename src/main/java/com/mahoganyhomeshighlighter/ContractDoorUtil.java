package com.mahoganyhomeshighlighter;

import javax.annotation.Nullable;
import net.runelite.api.Client;
import net.runelite.api.Constants;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.ObjectComposition;
import net.runelite.api.Scene;
import net.runelite.api.Tile;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.WorldView;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import java.util.function.BiConsumer;

final class ContractDoorUtil
{
	enum DoorState
	{
		OPEN,
		CLOSED
	}

	private static final int SCENE_OFFSET = (Constants.EXTENDED_SCENE_SIZE - Constants.SCENE_SIZE) / 2;

	private ContractDoorUtil()
	{
	}

	static void forEachDoorInArea(
		Client client,
		WorldArea area,
		int plane,
		BiConsumer<TileObject, DoorState> consumer)
	{
		final WorldView worldView = client.getTopLevelWorldView();
		if (worldView == null)
		{
			return;
		}

		final Scene scene = worldView.getScene();
		if (scene == null)
		{
			return;
		}

		final Tile[][][] tiles = scene.getExtendedTiles();
		if (tiles == null)
		{
			return;
		}

		for (int x = area.getX(); x < area.getX() + area.getWidth(); x++)
		{
			for (int y = area.getY(); y < area.getY() + area.getHeight(); y++)
			{
				final WorldPoint worldPoint = new WorldPoint(x, y, plane);
				if (!area.contains2D(worldPoint))
				{
					continue;
				}

				final Tile tile = getTile(worldView, tiles, worldPoint);
				if (tile == null)
				{
					continue;
				}

				final WallObject wallObject = tile.getWallObject();
				if (wallObject != null)
				{
					acceptDoor(client, wallObject, consumer);
				}

				for (final GameObject gameObject : tile.getGameObjects())
				{
					if (gameObject != null)
					{
						acceptDoor(client, gameObject, consumer);
					}
				}

				final DecorativeObject decorativeObject = tile.getDecorativeObject();
				if (decorativeObject != null)
				{
					acceptDoor(client, decorativeObject, consumer);
				}
			}
		}
	}

	@Nullable
	static DoorState getDoorState(final Client client, final TileObject object)
	{
		final ObjectComposition composition = getResolvedComposition(client, object.getId());
		if (composition == null)
		{
			return null;
		}

		final String name = composition.getName();
		if (name == null || !name.toLowerCase().contains("door"))
		{
			return null;
		}

		final String[] actions = composition.getActions();
		if (actions == null)
		{
			return null;
		}

		for (final String action : actions)
		{
			if ("Close".equalsIgnoreCase(action))
			{
				return DoorState.OPEN;
			}
		}

		for (final String action : actions)
		{
			if ("Open".equalsIgnoreCase(action))
			{
				return DoorState.CLOSED;
			}
		}

		return null;
	}

	private static void acceptDoor(
		final Client client,
		final TileObject object,
		final BiConsumer<TileObject, DoorState> consumer)
	{
		final DoorState state = getDoorState(client, object);
		if (state != null)
		{
			consumer.accept(object, state);
		}
	}

	@Nullable
	private static Tile getTile(
		final WorldView worldView,
		final Tile[][][] tiles,
		final WorldPoint worldPoint)
	{
		for (final WorldPoint localPoint : WorldPoint.toLocalInstance(worldView, worldPoint))
		{
			if (localPoint.getPlane() != worldPoint.getPlane())
			{
				continue;
			}

			int sceneX = localPoint.getX() - worldView.getBaseX() + SCENE_OFFSET;
			int sceneY = localPoint.getY() - worldView.getBaseY() + SCENE_OFFSET;

			if (sceneX < 0 || sceneY < 0
				|| sceneX >= Constants.EXTENDED_SCENE_SIZE
				|| sceneY >= Constants.EXTENDED_SCENE_SIZE)
			{
				continue;
			}

			return tiles[localPoint.getPlane()][sceneX][sceneY];
		}

		return null;
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
