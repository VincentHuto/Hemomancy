package com.vincenthuto.hemomancy.client.player;

/** A confirmed impact begins an eight-tick cosmetic recovery, independently of packet latency. */
public final class BloodVialInjectionPlayback {
    private final long start;
    private long impact;
    private boolean confirmed;

    public BloodVialInjectionPlayback(long start) { this.start = start; }

    public boolean confirm(long now) {
        if (confirmed) return false;
        confirmed = true;
        impact = now;
        return true;
    }

    public boolean confirmed() { return confirmed; }

    public float elapsed(long now, float partialTick) {
        return confirmed ? 16 + now - impact + partialTick
                : Math.max(0, Math.min(15.5F, now - start + partialTick));
    }

    public boolean expired(long now) { return confirmed ? now - impact >= 8 : now - start >= 60; }
}
