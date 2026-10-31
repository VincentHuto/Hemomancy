package com.vincenthuto.hemomancy.client.render.world;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Client memory the strain world effects need and the server does not send: where each wearer has been (Dead Man's
 * Rope's Primal train, Mermaid's Purse's wake) and where a Lifeline strand was left (the bit carries no position, so
 * the strand anchors where the host stood when it came on). Cleared when the client leaves a level.
 */
public final class MorphlingStrainTrails {
	/** Ticks of history kept: three seconds, enough for a train several blocks long at a walk. */
	public static final int HISTORY = 60;
	private static final Map<UUID, Deque<Vec3>> HISTORIES = new HashMap<>();
	private static final Map<UUID, Vec3> LIFELINES = new HashMap<>();

	private MorphlingStrainTrails() {
	}

	/** Called once a client tick for each wearer that needs a trail. */
	public static void record(Player player) {
		Deque<Vec3> history = HISTORIES.computeIfAbsent(player.getUUID(), id -> new ArrayDeque<>());
		Vec3 at = player.position();
		if (history.isEmpty() || history.peekFirst().distanceToSqr(at) > 0.0004D) history.addFirst(at);
		while (history.size() > HISTORY) history.removeLast();
	}

	public static List<Vec3> history(Player player) {
		Deque<Vec3> history = HISTORIES.get(player.getUUID());
		return history == null ? List.of() : List.copyOf(history);
	}

	public static void lifeline(Player player, boolean on) {
		if (!on) LIFELINES.remove(player.getUUID());
		else LIFELINES.putIfAbsent(player.getUUID(), player.position());
	}

	public static Vec3 lifelineAnchor(Player player) {
		return LIFELINES.get(player.getUUID());
	}

	public static void forget(UUID player) {
		HISTORIES.remove(player);
		LIFELINES.remove(player);
	}

	public static void clear() {
		HISTORIES.clear();
		LIFELINES.clear();
	}
}
