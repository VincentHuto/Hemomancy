package com.vincenthuto.hemomancy.common.capability.player.harbinger.manip;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Transient Synaptic pattern apply lockouts, keyed by player and stamped in game time. Like manipulation
 * cooldowns they are session state: cleared at server start/stop and on logout.
 */
public final class SynapticLoadoutLockout {
	/** After a pattern applies, further applies are refused for this long. */
	public static final int APPLY_LOCKOUT_TICKS = 200;
	private static final Map<UUID, Long> APPLY_LOCKOUT_UNTIL = new ConcurrentHashMap<>();

	private SynapticLoadoutLockout() {
	}

	public static void start(UUID playerId, long now) {
		APPLY_LOCKOUT_UNTIL.put(playerId, now + APPLY_LOCKOUT_TICKS);
	}

	public static long remaining(UUID playerId, long now) {
		Long until = APPLY_LOCKOUT_UNTIL.get(playerId);
		if (until == null) return 0L;
		long remaining = until - now;
		// Expired, or stamped on another world's clock (game time went backwards): discard it.
		if (remaining <= 0L || remaining > APPLY_LOCKOUT_TICKS) {
			APPLY_LOCKOUT_UNTIL.remove(playerId, until);
			return 0L;
		}
		return remaining;
	}

	public static void clear(UUID playerId) {
		APPLY_LOCKOUT_UNTIL.remove(playerId);
	}

	public static void clearSessionState() {
		APPLY_LOCKOUT_UNTIL.clear();
	}
}
