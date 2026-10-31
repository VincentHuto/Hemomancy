package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Short-lived per-host strain timers and counters (a window's end, a per-target re-trigger time, a hit count) that do
 * not belong on the stack: they change every few ticks and mean nothing after a relog. Keyed by host UUID and a power
 * key; cleared on logout and server stop through {@code TransientServerStateEvents}. Cooldowns that must survive a
 * relog stay on the stack ({@link MorphlingItem#setLastAbilityTick}).
 */
public final class MorphlingSession {
	private static final Map<UUID, Map<String, Long>> VALUES = new ConcurrentHashMap<>();

	private MorphlingSession() {
	}

	public static long get(Player player, String key) {
		Map<String, Long> values = VALUES.get(player.getUUID());
		return values == null ? 0L : values.getOrDefault(key, 0L);
	}

	public static void put(Player player, String key, long value) {
		VALUES.computeIfAbsent(player.getUUID(), id -> new ConcurrentHashMap<>()).put(key, value);
	}

	public static void remove(Player player, String key) {
		Map<String, Long> values = VALUES.get(player.getUUID());
		if (values != null) values.remove(key);
	}

	/** Opens a window that lasts {@code ticks} from now. */
	public static void open(Player player, String key, int ticks) {
		put(player, key, player.level().getGameTime() + ticks);
	}

	/** True while a window opened with {@link #open} has not ended. */
	public static boolean isOpen(Player player, String key) {
		return player.level().getGameTime() < get(player, key);
	}

	/** Ticks left on a window, or 0. */
	public static long remaining(Player player, String key) {
		return Math.max(0L, get(player, key) - player.level().getGameTime());
	}

	/** Drops this host's entries under {@code prefix} whose time has passed, so per-target keys do not pile up. */
	public static void prune(Player player, String prefix) {
		Map<String, Long> values = VALUES.get(player.getUUID());
		if (values == null) return;
		long now = player.level().getGameTime();
		values.entrySet().removeIf(entry -> entry.getKey().startsWith(prefix) && entry.getValue() <= now);
	}

	public static void clear(UUID player) {
		VALUES.remove(player);
	}

	public static void clearSessionState() {
		VALUES.clear();
	}
}
