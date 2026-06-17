package com.mahoganyhomeshighlighter;

import com.google.inject.Binder;
import com.google.inject.Provides;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;
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
import net.runelite.api.NPC;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.GameObjectDespawned;
import net.runelite.api.events.GameObjectSpawned;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.api.events.VarbitChanged;
import net.runelite.api.widgets.ComponentID;
import net.runelite.api.widgets.Widget;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@Slf4j
@PluginDescriptor(
	name = "Mahogany Homes Highlighter",
	configName = MahoganyHomesHighlighterConfig.GROUP_NAME,
	description = "Highlights Mahogany Homes contract objects and doors with customizable colors",
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

	private static final String MAHOGANY_HOMES_GROUP = "MahoganyHomes";
	private static final String MAHOGANY_HOMES_HOME_KEY = "currentHome";

	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private ConfigManager configManager;

	@Inject
	private MahoganyHomesHighlighterConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private MahoganyHomesHighlighterOverlay highlightOverlay;

	@Getter
	private final List<GameObject> objectsToMark = new ArrayList<>();

	@Getter
	private final List<GameObject> laddersToMark = new ArrayList<>();

	private final HashMap<Integer, Integer> varbMap = new HashMap<>();

	@Getter
	@Nullable
	private Home currentHome;

	private boolean varbChange;

	@Override
	public void configure(Binder binder)
	{
		binder.bind(MahoganyHomesHighlighterOverlay.class);
	}

	@Override
	protected void startUp()
	{
		if (highlightOverlay == null)
		{
			throw new IllegalStateException("MahoganyHomesHighlighterOverlay was not injected");
		}

		overlayManager.add(highlightOverlay);
		if (client.getGameState() == GameState.LOGGED_IN)
		{
			varbChange = true;
			clientThread.invoke(this::initializeLoggedInState);
		}
	}

	@Override
	protected void shutDown()
	{
		overlayManager.remove(highlightOverlay);
		objectsToMark.clear();
		laddersToMark.clear();
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
			laddersToMark.clear();
		}
		else if (event.getGameState() == GameState.LOGGED_IN)
		{
			varbChange = true;
			clientThread.invoke(this::initializeLoggedInState);
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

		tryDetectHomeFromLocation();
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
			setCurrentHome(null);
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

	int getRemainingTaskCount()
	{
		if (currentHome == null)
		{
			return 0;
		}

		int count = 0;
		for (int i = 0; i < currentHome.getContractHotspotCount(); i++)
		{
			if (getHotspotAction(Hotspot.values()[i].getVarb()) != null)
			{
				count++;
			}
		}
		return count;
	}

	int getRemainingTasksOnPlane(final int plane)
	{
		return countActiveContractObjects(object -> object.getPlane() == plane);
	}

	int getRemainingTasksAbovePlane(final int plane)
	{
		return countActiveContractObjects(object -> object.getPlane() > plane);
	}

	int getRemainingTasksBelowPlane(final int plane)
	{
		return countActiveContractObjects(object -> object.getPlane() < plane);
	}

	private int countActiveContractObjects(final Predicate<GameObject> planeFilter)
	{
		if (currentHome == null)
		{
			return 0;
		}

		int count = 0;
		for (final GameObject object : objectsToMark)
		{
			if (!planeFilter.test(object))
			{
				continue;
			}

			if (isActiveContractObject(object))
			{
				count++;
			}
		}
		return count;
	}

	boolean isActiveContractObject(final GameObject object)
	{
		if (currentHome == null)
		{
			return false;
		}

		if (!currentHome.isContractObject(object.getId()))
		{
			return false;
		}

		final WorldPoint location = object.getWorldLocation();
		if (location == null || !currentHome.containsWorldPoint(location))
		{
			return false;
		}

		final Hotspot hotspot = currentHome.getHotspotForContractObject(object.getId());
		return hotspot != null && getHotspotAction(hotspot.getVarb()) != null;
	}

	boolean isContractComplete()
	{
		return currentHome != null && getRemainingTaskCount() == 0;
	}

	@Nullable
	NPC getHomeownerNpc()
	{
		if (currentHome == null)
		{
			return null;
		}

		for (final NPC npc : client.getNpcs())
		{
			if (npc.getId() != currentHome.getNpcId())
			{
				continue;
			}

			final WorldPoint location = npc.getWorldLocation();
			if (location != null && currentHome.getArea().distanceTo(
				new WorldPoint(location.getX(), location.getY(), currentHome.getArea().getPlane())) == 0)
			{
				return npc;
			}
		}

		return null;
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
			setCurrentHome(home);
			updateVarbMap();
		}
	}

	private void initializeLoggedInState()
	{
		loadCurrentHome();
		updateVarbMap();
		tryDetectHomeFromLocation();
	}

	private void loadCurrentHome()
	{
		if (currentHome != null)
		{
			return;
		}

		final String savedHome = configManager.getRSProfileConfiguration(
			MahoganyHomesHighlighterConfig.GROUP_NAME,
			MahoganyHomesHighlighterConfig.CURRENT_HOME_KEY);
		final Home home = parseHomeName(savedHome);
		if (home != null)
		{
			currentHome = home;
			return;
		}

		final long accountHash = client.getAccountHash();
		if (accountHash == -1L)
		{
			return;
		}

		final String mahoganyHomesGroup = MAHOGANY_HOMES_GROUP + "." + accountHash;
		final String mahoganyHomesHome = configManager.getConfiguration(
			mahoganyHomesGroup,
			MAHOGANY_HOMES_HOME_KEY);
		final Home mahoganyHomesContract = parseHomeName(mahoganyHomesHome);
		if (mahoganyHomesContract != null)
		{
			currentHome = mahoganyHomesContract;
		}
	}

	@Nullable
	private Home parseHomeName(@Nullable final String name)
	{
		if (name == null || name.isEmpty())
		{
			return null;
		}

		final Home byEnumName = Home.getByEnumName(name);
		if (byEnumName != null)
		{
			return byEnumName;
		}

		return Home.getByName(name);
	}

	private void tryDetectHomeFromLocation()
	{
		if (currentHome != null || client.getLocalPlayer() == null)
		{
			return;
		}

		final Home homeAtLocation = Home.getByLocation(client.getLocalPlayer().getWorldLocation());
		if (homeAtLocation != null && hasActiveContractWork())
		{
			setCurrentHome(homeAtLocation);
		}
	}

	private void setCurrentHome(@Nullable final Home home)
	{
		currentHome = home;

		if (home == null)
		{
			configManager.unsetRSProfileConfiguration(
				MahoganyHomesHighlighterConfig.GROUP_NAME,
				MahoganyHomesHighlighterConfig.CURRENT_HOME_KEY);
			return;
		}

		configManager.setRSProfileConfiguration(
			MahoganyHomesHighlighterConfig.GROUP_NAME,
			MahoganyHomesHighlighterConfig.CURRENT_HOME_KEY,
			home.name());
	}

	private void processGameObject(@Nullable GameObject current, @Nullable GameObject previous)
	{
		if (previous != null)
		{
			objectsToMark.remove(previous);
			laddersToMark.remove(previous);
		}

		if (current == null)
		{
			return;
		}

		if (Hotspot.isHotspotObject(current.getId()))
		{
			objectsToMark.add(current);
		}
		else if (Home.isLadder(current.getId()))
		{
			laddersToMark.add(current);
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
		if (currentHome != null)
		{
			return getRemainingTaskCount() > 0;
		}

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
