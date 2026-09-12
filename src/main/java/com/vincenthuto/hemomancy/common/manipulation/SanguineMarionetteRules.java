package com.vincenthuto.hemomancy.common.manipulation;

public final class SanguineMarionetteRules {
    public static final int MAX_TICKS = 160;
    public static final int PULSE_COST = 50;
    public static final int COOLDOWN = 120;
    private SanguineMarionetteRules() {}

    public static boolean maintain(long now, long started, double distance) {
        return now - started < MAX_TICKS && distance <= 16;
    }
}
