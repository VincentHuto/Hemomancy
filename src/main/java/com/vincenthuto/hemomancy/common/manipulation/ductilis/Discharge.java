package com.vincenthuto.hemomancy.common.manipulation.ductilis;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One cast and its conductor relays share this ledger; each later pulse gets fresh hits. */
public final class Discharge {
    private final Set<UUID> victims = new HashSet<>();
    private long tick = Long.MIN_VALUE;
    private int limit = 64;

    private void prepare(long now) {
        if (tick != now) { victims.clear(); tick = now; limit = 64; }
    }

    public void beginPulse(long now) {
        prepare(now);
        limit = Math.min(limit,3);
    }

    public void finishChain(long now) {
        prepare(now);
        limit = 0;
    }

    public boolean canClaim(UUID victim, long now) {
        prepare(now);
        return victims.size() < limit && !victims.contains(victim);
    }

    public boolean claim(UUID victim, long now) {
        prepare(now);
        return victims.size() < limit && victims.add(victim);
    }

    public boolean hasHit(UUID victim, long now) {
        return tick == now && victims.contains(victim);
    }
}
