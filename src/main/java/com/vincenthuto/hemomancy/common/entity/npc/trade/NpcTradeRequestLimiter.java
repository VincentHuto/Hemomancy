package com.vincenthuto.hemomancy.common.entity.npc.trade;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * One trade request per player every {@link NpcTradeRules#REQUEST_INTERVAL_TICKS} server ticks. Session state, cleared
 * by {@code TransientServerStateEvents}.
 */
public final class NpcTradeRequestLimiter {
	private static final Map<UUID, Long> LAST_REQUEST = new HashMap<>();

	private NpcTradeRequestLimiter() {}

	/** Whether a request at {@code tick} is allowed; an allowed request starts the next wait. */
	public static boolean tryAcquire(UUID player, long tick) {
		Long last = LAST_REQUEST.get(player);
		if (last != null && tick >= last && tick - last < NpcTradeRules.REQUEST_INTERVAL_TICKS) return false;
		LAST_REQUEST.put(player, tick);
		return true;
	}

	public static void clearSessionState() {
		LAST_REQUEST.clear();
	}

	public static void logout(Player player) {
		LAST_REQUEST.remove(player.getUUID());
	}
}
