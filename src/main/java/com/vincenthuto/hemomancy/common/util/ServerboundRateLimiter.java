package com.vincenthuto.hemomancy.common.util;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Minimum intervals between a player's serverbound requests and key presses, per payload, so a modified client cannot
 * turn a request-and-reply payload into an amplifier or a key press into a particle storm (cleanup audit RT-23, RT-24).
 * Session state keyed by player UUID; cleared through {@code TransientServerStateEvents}.
 */
public final class ServerboundRateLimiter {
	private static final Map<UUID, Map<String, Long>> LAST = new HashMap<>();

	private ServerboundRateLimiter() {
	}

	/** Whether {@code player} may send {@code key} now; an allowed send starts the next {@code intervalTicks} wait. */
	public static boolean tryAcquire(ServerPlayer player, String key, int intervalTicks) {
		long tick = player.level().getGameTime();
		Map<String, Long> byKey = LAST.computeIfAbsent(player.getUUID(), ignored -> new HashMap<>());
		Long last = byKey.get(key);
		if (last != null && tick >= last && tick - last < intervalTicks) return false;
		byKey.put(key, tick);
		return true;
	}

	public static void clearSessionState() {
		LAST.clear();
	}

	public static void logout(Player player) {
		LAST.remove(player.getUUID());
	}
}
