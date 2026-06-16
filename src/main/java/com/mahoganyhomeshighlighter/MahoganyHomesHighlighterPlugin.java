package com.mahoganyhomeshighlighter;

import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import javax.inject.Inject;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameObject;
import net.runelite.api.GameState;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Mahogany Homes Highlighter",
	description = "Highlights Mahogany Homes contract objects with customizable colors for remove, build, and repair tasks",
	tags = {"mahogany", "homes", "construction", "highlight"}
)
public class MahoganyHomesHighlighterPlugin extends Plugin
{
	private static final Pattern CONTRACT_PATTERN = Pattern.compile(
		"(Please could you g|G)o see (\\w*)[ ,][\\w\\s,-]*[?.] You can get another job once you have furnished \\w* home\\.");
	private static final Pattern REMINDER_PATTERN = Pattern.compile(
		"You're currently on an? (\\w*) Contract\\. Go see (\\w*)[ ,][\\w\\s,-]*\\. You can get another job once you have furnished \\w* home\\.");
	private static final Pattern CONTRACT_FINISHED = Pattern.compile(
		"You have completed [\\d,]* contracts with a total of [\\d,]* points?\\.");

	@Inject
	private Client client;

	@Inject
	private MahoganyHomesHighlighterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MahoganyHomesHighlighterOverlay highlightOverlay;

	@Getter
	private final List<GameObject> objectsToMark = new ArrayList<>();

	private final HashMap<Integer, Integer> varbMap = new HashMap<>();

	@Getter
	@Nullable
	private Home currentHome;

	private boolean varbChange;

	@Override
	protected void startUp()
	{
		overlayManager.add(highlightOverlay);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			updateVarbMap();
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(highlightOverlay);
		objectsToMark.clear();
		varbMap.clear();
		currentHome = null;
	}

	@Provides
	MahoganyHomesHighlighterConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(MahoganyHomesHighlighterConfig.class);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		if (event.getGameState() == GameState.LOADING)
		{
			objectsToMark.clear();
		}
	}

	@Subscribe
	public void onGameObjectSpawned(GameObjectSpawned event)
	{
		processGameObject(event.getGameObject(), null);
	}

	@Subscribe
	public void onGameObjectDespawned(GameObjectDespawned event)
	{
		processGameObject(null, event.getGameObject());
	}

	@Subscribe
	public void onVarbitChanged(VarbitChanged event)
	{
		for (final Hotspot hotspot : Hotspot.values())
		{
			if (event.getVarbitId() == hotspot.getVarb())
			{
				varbChange = true;
				return;
			}
		}
	}

	@Subscribe
	public void onGameTick(GameTick event)
	{
		checkForAssignmentDialog();

		if (varbChange)
		{
			varbChange = false;
			updateVarbMap();
		}

		if (currentHome == null && client.getLocalPlayer() != null)
		{
			final Home homeAtLocation = Home.getByLocation(client.getLocalPlayer().getWorldLocation());
			if (homeAtLocation != null && hasActiveContractWork())
			{
				currentHome = homeAtLocation;
			}
		}
	}

	@Subscribe
	public void onChatMessage(net.runelite.api.events.ChatMessage event)
	{
		if (!event.getType().equals(ChatMessageType.GAMEMESSAGE))
		{
			return;
		}

		if (CONTRACT_FINISHED.matcher(Text.removeTags(event.getMessage())).matches())
		{
			currentHome = null;
			varbMap.clear();
		}
	}

	@Nullable
	HotspotAction getHotspotAction(final int varb)
	{
		final Integer value = varbMap.get(varb);
		if (value == null)
		{
			return null;
		}

		return HotspotAction.fromVarbValue(value);
	}

	private void checkForAssignmentDialog()
	{
		final Widget dialog = client.getWidget(ComponentID.DIALOG_NPC_TEXT);
		if (dialog == null)
		{
			return;
		}

		final String npcText = Text.sanitizeMultilineText(dialog.getText());
		final Matcher startContractMatcher = CONTRACT_PATTERN.matcher(npcText);
		final Matcher reminderContractMatcher = REMINDER_PATTERN.matcher(npcText);

		String name = null;
		if (startContractMatcher.matches())
		{
			name = startContractMatcher.group(2);
		}
		else if (reminderContractMatcher.matches())
		{
			name = reminderContractMatcher.group(2);
		}

		if (name == null)
		{
			return;
		}

		final Home home = Home.getByName(name);
		if (home != null)
		{
			currentHome = home;
			updateVarbMap();
		}
	}

	private void processGameObject(@Nullable GameObject current, @Nullable GameObject previous)
	{
		if (previous != null)
		{
			objectsToMark.remove(previous);
		}

		if (current != null && Hotspot.isHotspotObject(current.getId()))
		{
			objectsToMark.add(current);
		}
	}

	private void updateVarbMap()
	{
		varbMap.clear();

		for (final Hotspot hotspot : Hotspot.values())
		{
			varbMap.put(hotspot.getVarb(), client.getVarbitValue(hotspot.getVarb()));
		}
	}

	private boolean hasActiveContractWork()
	{
		for (final Hotspot hotspot : Hotspot.values())
		{
			if (getHotspotAction(hotspot.getVarb()) != null)
			{
				return true;
			}
		}
		return false;
	}
}
