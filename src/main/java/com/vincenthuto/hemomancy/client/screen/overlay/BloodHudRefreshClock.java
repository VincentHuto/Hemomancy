package com.vincenthuto.hemomancy.client.screen.overlay;

final class BloodHudRefreshClock {
    private static final long INTERVAL_TICKS = 10;
    private long lastTick = Long.MIN_VALUE;

    boolean due(long tick) {
        if (lastTick == Long.MIN_VALUE || tick < lastTick || tick - lastTick >= INTERVAL_TICKS) {
            lastTick = tick;
            return true;
        }
        return false;
    }

    void reset() {
        lastTick = Long.MIN_VALUE;
    }
}
