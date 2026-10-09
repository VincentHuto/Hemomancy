package com.vincenthuto.hemomancy.common.manipulation;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Transient server cooldowns, isolated by player and manipulation id. */
public final class ManipulationCooldownState {
    private final Map<UUID, Map<String, Long>> cooldowns = new ConcurrentHashMap<>();

    public void start(UUID playerId, String manipulationId, long now, long durationTicks) {
        if (durationTicks <= 0L) return;
        cooldowns.computeIfAbsent(playerId, ignored -> new ConcurrentHashMap<>())
                .put(manipulationId, now + durationTicks);
    }

    public boolean isOnCooldown(UUID playerId, String manipulationId, long now) {
        return remainingTicks(playerId, manipulationId, now) > 0L;
    }

    public long remainingTicks(UUID playerId, String manipulationId, long now) {
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (playerCooldowns == null) return 0L;
        Long expiry = playerCooldowns.get(manipulationId);
        if (expiry == null) return 0L;
        long remaining = expiry - now;
        if (remaining > 0L) return remaining;
        playerCooldowns.remove(manipulationId, expiry);
        if (playerCooldowns.isEmpty()) cooldowns.remove(playerId, playerCooldowns);
        return 0L;
    }

    /** Shortens an active cooldown to at most {@code maxRemaining}; never starts or lengthens one. */
    public long capRemaining(UUID playerId, String manipulationId, long now, long maxRemaining) {
        long remaining = remainingTicks(playerId, manipulationId, now);
        if (remaining <= 0L || remaining <= maxRemaining) return remaining;
        Map<String, Long> playerCooldowns = cooldowns.get(playerId);
        if (maxRemaining <= 0L) {
            playerCooldowns.remove(manipulationId);
            if (playerCooldowns.isEmpty()) cooldowns.remove(playerId, playerCooldowns);
            return 0L;
        }
        playerCooldowns.put(manipulationId, now + maxRemaining);
        return maxRemaining;
    }

    public void clear() {
        cooldowns.clear();
    }
}
