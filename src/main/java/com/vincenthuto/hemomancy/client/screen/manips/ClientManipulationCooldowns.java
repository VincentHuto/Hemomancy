package com.vincenthuto.hemomancy.client.screen.manips;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ClientManipulationCooldowns {
    private static final Map<String, Long> EXPIRY_TICKS = new ConcurrentHashMap<>();

    private ClientManipulationCooldowns() {
    }

    public static void start(String manipulationId, int durationTicks, long now) {
        if (durationTicks > 0) EXPIRY_TICKS.put(manipulationId, now + durationTicks);
        else EXPIRY_TICKS.remove(manipulationId);
    }

    public static long remainingTicks(String manipulationId, long now) {
        Long expiry = EXPIRY_TICKS.get(manipulationId);
        if (expiry == null) return 0L;
        long remaining = expiry - now;
        if (remaining > 0L) return remaining;
        EXPIRY_TICKS.remove(manipulationId, expiry);
        return 0L;
    }

    public static void clear() {
        EXPIRY_TICKS.clear();
    }
}
