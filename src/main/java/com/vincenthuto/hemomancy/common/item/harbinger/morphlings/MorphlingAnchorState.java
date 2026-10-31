package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The one still-tracker every Morphling power shares: how long a host has stood still, and how long they have been
 * anchored (sneaking without moving). Moving or jumping uproots. Anchoring is never petrification: nothing about it
 * outlives the stance. Session state, updated once a tick by {@code EquippedMorphlingEvents} for any host with a
 * Morphling and cleared through {@code TransientServerStateEvents}.
 */
public final class MorphlingAnchorState {
	/** Squared horizontal drift per tick that still counts as standing still. */
	public static final double STILL_DRIFT_SQR = 0.0036D;

	private static final Map<UUID, Track> TRACKS = new ConcurrentHashMap<>();

	private MorphlingAnchorState() {
	}

	private static final class Track {
		Vec3 last;
		long stillSince = -1L;
		long anchoredSince = -1L;
	}

	/** Pure rule: a step is still when it barely moves horizontally and the host is not rising off the ground. */
	public static boolean isStillStep(double dx, double dz, boolean jumping) {
		return !jumping && dx * dx + dz * dz <= STILL_DRIFT_SQR;
	}

	public static void update(Player player) {
		long now = player.level().getGameTime();
		Track track = TRACKS.computeIfAbsent(player.getUUID(), id -> new Track());
		Vec3 pos = player.position();
		boolean jumping = !player.onGround() && !player.isInWater() && player.getDeltaMovement().y > 0.05D;
		boolean still = track.last != null && isStillStep(pos.x - track.last.x, pos.z - track.last.z, jumping);
		track.last = pos;
		if (!still) {
			track.stillSince = -1L;
			track.anchoredSince = -1L;
			return;
		}
		if (track.stillSince < 0L) track.stillSince = now;
		if (player.isShiftKeyDown()) {
			if (track.anchoredSince < 0L) track.anchoredSince = now;
		} else {
			track.anchoredSince = -1L;
		}
	}

	public static int stillTicks(Player player) {
		Track track = TRACKS.get(player.getUUID());
		return track == null || track.stillSince < 0L ? 0
				: (int) Math.min(Integer.MAX_VALUE, player.level().getGameTime() - track.stillSince);
	}

	public static int anchoredTicks(Player player) {
		Track track = TRACKS.get(player.getUUID());
		return track == null || track.anchoredSince < 0L ? 0
				: (int) Math.min(Integer.MAX_VALUE, player.level().getGameTime() - track.anchoredSince);
	}

	public static boolean isStill(Player player, int ticks) {
		return stillTicks(player) >= ticks;
	}

	public static boolean isAnchored(Player player, int ticks) {
		return anchoredTicks(player) >= ticks;
	}

	/** Forces a host to count as still and anchored from now, for tests and for powers that root the host. */
	public static void settle(Player player, boolean anchored, int ticksAgo) {
		Track track = TRACKS.computeIfAbsent(player.getUUID(), id -> new Track());
		long since = player.level().getGameTime() - Math.max(0, ticksAgo);
		track.last = player.position();
		track.stillSince = since;
		track.anchoredSince = anchored ? since : -1L;
	}

	public static void logout(UUID player) {
		TRACKS.remove(player);
	}

	public static void clearSessionState() {
		TRACKS.clear();
	}
}
