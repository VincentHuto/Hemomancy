package com.vincenthuto.hemomancy.common.circus;

public final class CinderBellowsRules {
    public enum Phase { INHALE, BREATHE, RECOVER }
    private CinderBellowsRules() {}
    public static Phase phase(int tick) {
        int cycle = Math.floorMod(tick - 1, 100) + 1;
        return cycle <= 20 ? Phase.INHALE : cycle <= 60 ? Phase.BREATHE : Phase.RECOVER;
    }
    public static boolean damagePulse(int tick) { return phase(tick) == Phase.BREATHE && tick % 10 == 0; }
    public static boolean inCone(double distance, double facingDot) { return distance <= 5 && facingDot >= Math.cos(Math.PI / 6); }
}
