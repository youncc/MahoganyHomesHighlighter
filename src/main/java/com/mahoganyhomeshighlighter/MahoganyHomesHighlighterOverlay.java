package com.mahoganyhomeshighlighter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import javax.inject.Inject;
import net.runelite.api.Client;
import net.runelite.api.DecorativeObject;
import net.runelite.api.GameObject;
import net.runelite.api.GroundObject;
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
		if (home == null || plugin.getObjectsToMark().isEmpty())
		{
			return null;
		}

		final WorldPoint playerLocation = client.getLocalPlayer().getWorldLocation();
		if (playerLocation == null || distanceBetween(home.getArea(), playerLocation) > 0)
		{
			return null;
		}

		final int playerPlane = client.getTopLevelWorldView().getPlane();
		final Point mousePosition = client.getMouseCanvasPosition();
		final Stroke stroke = new BasicStroke(BORDER_WIDTH);

		for (TileObject object : plugin.getObjectsToMark())
		{
			if (object.getPlane() != playerPlane)
			{
				continue;
			}

			if (distanceBetween(home.getArea(), object.getWorldLocation()) > 0)
			{
				continue;
			}

			final Hotspot hotspot = Hotspot.getByObjectId(object.getId());
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

		return null;
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
