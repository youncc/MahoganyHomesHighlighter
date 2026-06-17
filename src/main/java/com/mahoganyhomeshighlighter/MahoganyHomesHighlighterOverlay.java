package com.mahoganyhomeshighlighter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import javax.annotation.Nullable;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import com.mahoganyhomeshighlighter.ContractStairUtil.ClimbDirection;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
import net.runelite.api.NPC;
import net.runelite.api.Point;
import net.runelite.api.TileObject;
import net.runelite.api.WallObject;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

class MahoganyHomesHighlighterOverlay extends Overlay
{
	private static final Color BORDER_COLOR = Color.WHITE;
	private static final Color HOVER_BORDER_COLOR = Color.GRAY;
	private static final float BORDER_WIDTH = 2f;

	private final MahoganyHomesHighlighterPlugin plugin;
	private final MahoganyHomesHighlighterConfig config;
	private final Client client;

	@Inject
	MahoganyHomesHighlighterOverlay(
		MahoganyHomesHighlighterPlugin plugin,
		MahoganyHomesHighlighterConfig config,
		Client client)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		final Home home = plugin.getCurrentHome();
		if (home == null)
		{
			return null;
		}

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		if (playerLocation == null || distanceBetween(home.getArea(), playerLocation) > 0)
		{
			return null;
		}

		final int playerPlane = playerLocation.getPlane();
		final Point mousePosition = client.getMouseCanvasPosition();
		final Stroke stroke = new BasicStroke(BORDER_WIDTH);

		if (config.highlightFurniture() && !plugin.getObjectsToMark().isEmpty())
		{
			for (TileObject object : plugin.getObjectsToMark())
			{
				if (object.getPlane() != playerPlane)
				{
					continue;
				}

				if (!home.isContractObject(object.getId()))
				{
					continue;
				}

				if (distanceBetween(home.getArea(), object.getWorldLocation()) > 0)
				{
					continue;
				}

				final Hotspot hotspot = home.getHotspotForContractObject(object.getId());
				if (hotspot == null)
				{
					continue;
				}

				final HotspotAction action = plugin.getHotspotAction(hotspot.getVarb());
				if (action == null)
				{
					continue;
				}

				final Color highlightColor = getColorForAction(action);
				renderHighlight(graphics, object, highlightColor, mousePosition, stroke);
			}
		}

		if (config.highlightDoors() || config.showDoorStatusText())
		{
			renderDoors(graphics, home, playerPlane, mousePosition, stroke);
		}

		if (config.highlightStairs() || config.showStairStatusText())
		{
			renderStairs(graphics, home, playerPlane, mousePosition, stroke);
		}

		if (config.highlightHomeowner() && plugin.isContractComplete())
		{
			renderHomeowner(graphics, stroke);
		}

		return null;
	}

	private void renderDoors(
		Graphics2D graphics,
		Home home,
		int playerPlane,
		Point mousePosition,
		Stroke stroke)
	{
		ContractDoorUtil.forEachDoorInArea(client, home.getArea(), playerPlane, (object, state) ->
		{
			if (distanceBetween(home.getArea(), object.getWorldLocation()) > 0)
			{
				return;
			}

			final Color highlightColor = state == ContractDoorUtil.DoorState.OPEN
				? config.openDoorColor()
				: config.closedDoorColor();

			if (config.highlightDoors())
			{
				renderHighlight(graphics, object, highlightColor, mousePosition, stroke);
			}

			if (config.showDoorStatusText())
			{
				final String text = state == ContractDoorUtil.DoorState.OPEN ? "Open" : "Closed";
				final Point textLocation = getObjectStatusTextLocation(graphics, object, text);
				if (textLocation != null)
				{
					OverlayUtil.renderTextLocation(graphics, textLocation, text, highlightColor);
				}
			}
		});
	}

	private void renderStairs(
		Graphics2D graphics,
		Home home,
		int playerPlane,
		Point mousePosition,
		Stroke stroke)
	{
		final int remainingTasks = plugin.getRemainingTaskCount();
		final int countOnFloor = plugin.getRemainingTasksOnPlane(playerPlane);
		if (!ContractStairUtil.shouldHighlightStairs(home, playerPlane, countOnFloor, remainingTasks))
		{
			return;
		}

		final int visibleAbove = plugin.getRemainingTasksAbovePlane(playerPlane);
		final int visibleBelow = plugin.getRemainingTasksBelowPlane(playerPlane);
		final int tasksAbove = ContractStairUtil.countTasksAbove(playerPlane, countOnFloor, remainingTasks, visibleAbove);
		final int tasksBelow = ContractStairUtil.countTasksBelow(playerPlane, countOnFloor, remainingTasks, visibleBelow);
		final Color highlightColor = config.stairColor();

		for (final GameObject ladder : plugin.getLaddersToMark())
		{
			if (ladder.getPlane() != playerPlane)
			{
				continue;
			}

			if (distanceBetween(home.getArea(), ladder.getWorldLocation()) > 0)
			{
				continue;
			}

			final ClimbDirection climbDirection = ContractStairUtil.getClimbDirection(client, ladder.getId());
			if (climbDirection == null)
			{
				continue;
			}

			if (!ContractStairUtil.shouldHighlightLadder(
				home, climbDirection, playerPlane, tasksAbove, tasksBelow, remainingTasks))
			{
				continue;
			}

			if (config.highlightStairs())
			{
				renderHighlight(graphics, ladder, highlightColor, mousePosition, stroke);
			}

			if (config.showStairStatusText())
			{
				final String text = ContractStairUtil.getStairLabel(
					home, climbDirection, playerPlane, tasksAbove, tasksBelow, remainingTasks);
				if (text != null)
				{
					final Point textLocation = getObjectStatusTextLocation(graphics, ladder, text);
					if (textLocation != null)
					{
						OverlayUtil.renderTextLocation(graphics, textLocation, text, highlightColor);
					}
				}
			}
		}
	}

	private void renderHomeowner(Graphics2D graphics, Stroke stroke)
	{
		final NPC homeowner = plugin.getHomeownerNpc();
		if (homeowner == null)
		{
			return;
		}

		final Color highlightColor = config.homeownerColor();
		final Shape hull = homeowner.getConvexHull();
		if (hull != null)
		{
			final Color fillColor = new Color(
				highlightColor.getRed(),
				highlightColor.getGreen(),
				highlightColor.getBlue(),
				Math.min(highlightColor.getAlpha(), 50));
			OverlayUtil.renderPolygon(graphics, hull, highlightColor, fillColor, stroke);
		}

		if (config.highlightClickbox())
		{
			OverlayUtil.renderActorOverlay(graphics, homeowner, "", highlightColor);
		}
	}

	private void renderHighlight(
		Graphics2D graphics,
		TileObject object,
		Color highlightColor,
		Point mousePosition,
		Stroke stroke)
	{
		if (config.highlightClickbox())
		{
			OverlayUtil.renderHoverableArea(
				graphics,
				object.getClickbox(),
				mousePosition,
				highlightColor,
				BORDER_COLOR,
				HOVER_BORDER_COLOR);
		}

		if (config.highlightHull())
		{
			renderHull(graphics, object, highlightColor, stroke);
		}
	}

	private Color getColorForAction(HotspotAction action)
	{
		switch (action)
		{
			case REMOVE:
				return config.removeColor();
			case BUILD:
				return config.buildColor();
			case REPAIR:
				return config.repairColor();
			default:
				return config.buildColor();
		}
	}

	@Nullable
	private Point getObjectStatusTextLocation(Graphics2D graphics, TileObject object, String text)
	{
		int topY = Integer.MAX_VALUE;
		int centerX = 0;
		boolean found = false;

		for (final Shape hull : getHulls(object))
		{
			if (hull == null)
			{
				continue;
			}

			final Rectangle bounds = hull.getBounds();
			if (bounds.width <= 0 || bounds.height <= 0)
			{
				continue;
			}

			if (bounds.y < topY)
			{
				topY = bounds.y;
				centerX = bounds.x + bounds.width / 2;
				found = true;
			}
		}

		if (found)
		{
			final FontMetrics fontMetrics = graphics.getFontMetrics();
			return new Point(centerX - fontMetrics.stringWidth(text) / 2, topY - 2);
		}

		return object.getCanvasTextLocation(graphics, text, 120);
	}

	private Shape[] getHulls(TileObject object)
	{
		if (object instanceof GameObject)
		{
			return new Shape[]{((GameObject) object).getConvexHull()};
		}
		else if (object instanceof WallObject)
		{
			final WallObject wallObject = (WallObject) object;
			return new Shape[]{wallObject.getConvexHull(), wallObject.getConvexHull2()};
		}
		else if (object instanceof DecorativeObject)
		{
			final DecorativeObject decorativeObject = (DecorativeObject) object;
			return new Shape[]{decorativeObject.getConvexHull(), decorativeObject.getConvexHull2()};
		}
		else if (object instanceof GroundObject)
		{
			return new Shape[]{((GroundObject) object).getConvexHull()};
		}

		return new Shape[]{object.getCanvasTilePoly()};
	}

	private void renderHull(Graphics2D graphics, TileObject object, Color color, Stroke stroke)
	{
		final Shape polygon;
		Shape polygon2 = null;

		if (object instanceof GameObject)
		{
			polygon = ((GameObject) object).getConvexHull();
		}
		else if (object instanceof WallObject)
		{
			polygon = ((WallObject) object).getConvexHull();
			polygon2 = ((WallObject) object).getConvexHull2();
		}
		else if (object instanceof DecorativeObject)
		{
			polygon = ((DecorativeObject) object).getConvexHull();
			polygon2 = ((DecorativeObject) object).getConvexHull2();
		}
		else if (object instanceof GroundObject)
		{
			polygon = ((GroundObject) object).getConvexHull();
		}
		else
		{
			polygon = object.getCanvasTilePoly();
		}

		final Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.min(color.getAlpha(), 50));

		if (polygon != null)
		{
			OverlayUtil.renderPolygon(graphics, polygon, color, fillColor, stroke);
		}

		if (polygon2 != null)
		{
			OverlayUtil.renderPolygon(graphics, polygon2, color, fillColor, stroke);
		}
	}

	private static int distanceBetween(final WorldArea area, final WorldPoint point)
	{
		return area.distanceTo(horizontalPoint(area, point));
	}

	private static WorldPoint horizontalPoint(final WorldArea area, final WorldPoint point)
	{
		return new WorldPoint(point.getX(), point.getY(), area.getPlane());
	}
}
