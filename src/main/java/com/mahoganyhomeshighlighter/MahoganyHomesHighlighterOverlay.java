package com.mahoganyhomeshighlighter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.util.HashSet;
import java.util.Set;
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
import net.runelite.client.ui.overlay.outline.ModelOutlineRenderer;
import net.runelite.client.util.ColorUtil;

class MahoganyHomesHighlighterOverlay extends Overlay
{
	private final MahoganyHomesHighlighterPlugin plugin;
	private final MahoganyHomesHighlighterConfig config;
	private final Client client;
	private final ModelOutlineRenderer modelOutlineRenderer;

	@Inject
	MahoganyHomesHighlighterOverlay(
		MahoganyHomesHighlighterPlugin plugin,
		MahoganyHomesHighlighterConfig config,
		Client client,
		ModelOutlineRenderer modelOutlineRenderer)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_SCENE);
		this.plugin = plugin;
		this.config = config;
		this.client = client;
		this.modelOutlineRenderer = modelOutlineRenderer;
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
		final Stroke stroke = new BasicStroke((float) config.borderWidth());
		final int outlineWidth = (int) config.borderWidth();

		if (((config.highlightFurniture() && hasActiveRenderStyle()) || config.showFurnitureActionText())
			&& !plugin.getObjectsToMark().isEmpty())
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

				if (config.highlightFurniture() && hasActiveRenderStyle())
				{
					renderTileObjectHighlight(graphics, object, highlightColor, mousePosition, stroke, outlineWidth);
				}

				if (config.showFurnitureActionText())
				{
					final String text = action.getLabel();
					final Point textLocation = getObjectStatusTextLocation(graphics, object, text);
					if (textLocation != null)
					{
						OverlayUtil.renderTextLocation(graphics, textLocation, text, highlightColor);
					}
				}
			}
		}

		if ((config.highlightDoors() && hasActiveRenderStyle()) || config.showDoorStatusText())
		{
			renderDoors(graphics, home, playerPlane, mousePosition, stroke, outlineWidth);
		}

		if ((config.highlightStairs() && hasActiveRenderStyle()) || config.showStairStatusText())
		{
			renderStairs(graphics, home, playerPlane, mousePosition, stroke, outlineWidth);
		}

		if (config.highlightHomeowner() && hasActiveRenderStyle() && plugin.isContractComplete())
		{
			renderHomeowner(graphics, stroke, outlineWidth);
		}

		return null;
	}

	private void renderDoors(
		Graphics2D graphics,
		Home home,
		int playerPlane,
		Point mousePosition,
		Stroke stroke,
		int outlineWidth)
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

			if (config.highlightDoors() && hasActiveRenderStyle())
			{
				renderTileObjectHighlight(graphics, object, highlightColor, mousePosition, stroke, outlineWidth);
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
		Stroke stroke,
		int outlineWidth)
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
		final boolean renderStairVisuals = config.highlightStairs() && hasActiveRenderStyle();
		final Set<WorldPoint> indicatedStairTiles = new HashSet<>();

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

			final WorldPoint ladderLocation = ladder.getWorldLocation();
			if (ladderLocation != null && !indicatedStairTiles.add(ladderLocation))
			{
				continue;
			}

			if (renderStairVisuals)
			{
				renderTileObjectHighlight(graphics, ladder, highlightColor, mousePosition, stroke, outlineWidth);
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

	private void renderHomeowner(Graphics2D graphics, Stroke stroke, int outlineWidth)
	{
		final NPC homeowner = plugin.getHomeownerNpc();
		if (homeowner == null)
		{
			return;
		}

		final Color highlightColor = config.homeownerColor();
		final Color hullFillColor = getHullFillColor(highlightColor);

		if (config.highlightHull())
		{
			final Shape hull = homeowner.getConvexHull();
			if (hull != null)
			{
				OverlayUtil.renderPolygon(graphics, hull, highlightColor, hullFillColor, stroke);
			}
		}

		if (config.highlightTile())
		{
			final Polygon tilePoly = homeowner.getCanvasTilePoly();
			if (tilePoly != null)
			{
				OverlayUtil.renderPolygon(
					graphics,
					tilePoly,
					highlightColor,
					getLightFillColor(highlightColor),
					stroke);
			}
		}

		if (config.highlightOutline())
		{
			modelOutlineRenderer.drawOutline(
				homeowner,
				outlineWidth,
				highlightColor,
				config.outlineFeather());
		}

		if (config.highlightClickbox())
		{
			OverlayUtil.renderActorOverlay(graphics, homeowner, "", highlightColor);
		}
	}

	private void renderTileObjectHighlight(
		Graphics2D graphics,
		TileObject object,
		Color highlightColor,
		Point mousePosition,
		Stroke stroke,
		int outlineWidth)
	{
		if (config.highlightClickbox())
		{
			OverlayUtil.renderHoverableArea(
				graphics,
				object.getClickbox(),
				mousePosition,
				highlightColor,
				config.clickboxBorderColor(),
				config.clickboxHoverBorderColor());
		}

		if (config.highlightHull())
		{
			renderHull(graphics, object, highlightColor, stroke, !config.highlightTile());
		}

		if (config.highlightOutline())
		{
			modelOutlineRenderer.drawOutline(
				object,
				outlineWidth,
				highlightColor,
				config.outlineFeather());
		}

		if (config.highlightTile())
		{
			final Polygon tilePoly = object.getCanvasTilePoly();
			if (tilePoly != null)
			{
				OverlayUtil.renderPolygon(
					graphics,
					tilePoly,
					highlightColor,
					getLightFillColor(highlightColor),
					stroke);
			}
		}
	}

	private Color getHullFillColor(Color highlightColor)
	{
		return new Color(
			highlightColor.getRed(),
			highlightColor.getGreen(),
			highlightColor.getBlue(),
			config.fillOpacity());
	}

	private Color getLightFillColor(Color highlightColor)
	{
		final int alpha = Math.max(1, config.fillOpacity() / 12);
		return ColorUtil.colorWithAlpha(highlightColor, alpha);
	}

	private boolean hasActiveRenderStyle()
	{
		return config.highlightHull()
			|| config.highlightOutline()
			|| config.highlightClickbox()
			|| config.highlightTile();
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
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;
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

			minX = Math.min(minX, bounds.x);
			minY = Math.min(minY, bounds.y);
			maxX = Math.max(maxX, bounds.x + bounds.width);
			maxY = Math.max(maxY, bounds.y + bounds.height);
			found = true;
		}

		final FontMetrics fontMetrics = graphics.getFontMetrics();
		final int textWidth = fontMetrics.stringWidth(text);

		if (found)
		{
			final int centerX = (minX + maxX) / 2;
			switch (config.statusTextPosition())
			{
				case ON:
					return new Point(
						centerX - textWidth / 2,
						(minY + maxY) / 2 + fontMetrics.getAscent() / 2);
				case BELOW:
					return new Point(
						centerX - textWidth / 2,
						maxY + fontMetrics.getDescent() + 2);
				case ABOVE:
					return new Point(centerX - textWidth / 2, minY - 2);
			}
		}

		switch (config.statusTextPosition())
		{
			case ON:
				return object.getCanvasTextLocation(graphics, text, 0);
			case BELOW:
				return object.getCanvasTextLocation(graphics, text, -40);
			case ABOVE:
				return object.getCanvasTextLocation(graphics, text, 120);
		}

		return null;
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

	private void renderHull(Graphics2D graphics, TileObject object, Color color, Stroke stroke, boolean allowTileFallback)
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
		else if (allowTileFallback)
		{
			polygon = object.getCanvasTilePoly();
		}
		else
		{
			polygon = null;
		}

		final Color fillColor = getHullFillColor(color);

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
