package com.mahoganyhomeshighlighter;

import com.google.common.collect.ImmutableSet;
import javax.annotation.Nullable;
import lombok.Getter;
import net.runelite.api.NpcID;
import net.runelite.api.coords.WorldArea;
import net.runelite.api.coords.WorldPoint;
import org.apache.commons.text.WordUtils;

@Getter
enum Home
{
	JESS(new WorldArea(2611, 3290, 14, 7, 0), NpcID.JESS, true,
		new int[]{40171, 40172, 40173, 40174, 40175, 40176, 40177, 40299},
		17026, 16685),
	NOELLA(new WorldArea(2652, 3317, 15, 8, 0), NpcID.NOELLA, false,
		new int[]{40156, 40157, 40158, 40159, 40160, 40161, 40162, 40163},
		17026, 16685, 15645, 15648),
	ROSS(new WorldArea(2609, 3313, 11, 9, 0), NpcID.ROSS, false,
		new int[]{40164, 40165, 40166, 40167, 40168, 40169, 40170},
		16683, 16679),
	LARRY(new WorldArea(3033, 3360, 10, 9, 0), NpcID.LARRY_10418, false,
		new int[]{40297, 40095, 40096, 40097, 40298, 40098, 40099},
		24075, 24076),
	NORMAN(new WorldArea(3034, 3341, 8, 8, 0), NpcID.NORMAN, true,
		new int[]{40296, 40089, 40090, 40091, 40092, 40093, 40094},
		24082, 24085),
	TAU(new WorldArea(3043, 3340, 10, 11, 0), NpcID.TAU, false,
		new int[]{40083, 40084, 40085, 40086, 40087, 40088, 40295}),
	BARBARA(new WorldArea(1746, 3531, 10, 11, 0), NpcID.BARBARA, false,
		new int[]{40011, 40293, 40012, 40294, 40013, 40014, 40015}),
	LEELA(new WorldArea(1781, 3589, 9, 8, 0), NpcID.LEELA_10423, false,
		new int[]{40007, 40008, 40290, 40291, 40009, 40010, 40292},
		11794, 11802),
	MARIAH(new WorldArea(1762, 3618, 10, 7, 0), NpcID.MARIAH, false,
		new int[]{40002, 40287, 40003, 40288, 40004, 40005, 40006, 40289},
		11794, 11802),
	BOB(new WorldArea(3234, 3482, 10, 10, 0), NpcID.BOB_10414, false,
		new int[]{39981, 39982, 39983, 39984, 39985, 39986, 39987, 39988},
		11797, 11799),
	JEFF(new WorldArea(3235, 3445, 10, 12, 0), NpcID.JEFF_10415, false,
		new int[]{39989, 39990, 39991, 39992, 39993, 39994, 39995, 39996},
		11789, 11793),
	SARAH(new WorldArea(3232, 3381, 8, 7, 0), NpcID.SARAH_10416, false,
		new int[]{39997, 39998, 39999, 40000, 40286, 40001});

	private static final ImmutableSet<Integer> LADDER_IDS;

	static
	{
		final ImmutableSet.Builder<Integer> builder = ImmutableSet.builder();
		for (final Home home : values())
		{
			builder.addAll(home.ladderIds);
		}
		LADDER_IDS = builder.build();
	}

	private final WorldArea area;
	private final int npcId;
	private final boolean npcUpstairs;
	private final int[] contractObjectIds;
	private final ImmutableSet<Integer> contractObjectIdSet;
	private final ImmutableSet<Integer> ladderIds;

	Home(
		final WorldArea area,
		final int npcId,
		final boolean npcUpstairs,
		final int[] contractObjectIds,
		final Integer... ladders)
	{
		this.area = area;
		this.npcId = npcId;
		this.npcUpstairs = npcUpstairs;
		this.contractObjectIds = contractObjectIds;
		final ImmutableSet.Builder<Integer> contractIds = ImmutableSet.builder();
		for (final int objectId : contractObjectIds)
		{
			contractIds.add(objectId);
		}
		this.contractObjectIdSet = contractIds.build();
		this.ladderIds = ladders.length == 0 ? ImmutableSet.of() : ImmutableSet.copyOf(ladders);
	}

	int getContractHotspotCount()
	{
		return contractObjectIds.length;
	}

	boolean isContractObject(final int objectId)
	{
		return contractObjectIdSet.contains(objectId);
	}

	@Nullable
	Hotspot getHotspotForContractObject(final int objectId)
	{
		for (int i = 0; i < contractObjectIds.length; i++)
		{
			if (contractObjectIds[i] == objectId)
			{
				return Hotspot.values()[i];
			}
		}

		return null;
	}

	String getDisplayName()
	{
		return WordUtils.capitalize(name().toLowerCase());
	}

	static boolean isLadder(final int objectId)
	{
		return LADDER_IDS.contains(objectId);
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

	boolean containsWorldPoint(final WorldPoint location)
	{
		return area.distanceTo(new WorldPoint(location.getX(), location.getY(), area.getPlane())) == 0;
	}
}
