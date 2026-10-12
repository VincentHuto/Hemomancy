package com.vincenthuto.hemomancy.common.manipulation.lux;

/**
 * Presentation rules for Radiant Wake's wings, free of Minecraft types so they can be tested on the JVM.
 * The wings unfurl when the channel starts, beat slowly while held, and stream light beneath a gliding caster.
 */
public final class RadiantWakeRules {
    public static final int UNFURL_TICKS = 5;
    /** Each pulse refreshes the cue for a little over a second, so a held channel never lapses between pulses. */
    public static final int CUE_TICKS = 25;
    public static final int FEATHERS = 5;
    public static final int STANDING = 1, GLIDING = 2;

    private RadiantWakeRules() {
    }

    public static int encode(boolean gliding) {
        return gliding ? GLIDING : STANDING;
    }

    public static boolean gliding(int count) {
        return count == GLIDING;
    }

    /** How far the wings have opened, 0 folded to 1 spread. */
    public static double unfurl(double age) {
        double t = Math.max(0, Math.min(1, age / UNFURL_TICKS));
        return 1 - Math.pow(1 - t, 3);
    }

    /** Slow wingbeat, -1..1; gliding holds the wings flatter and steadier. */
    public static double beat(double time, boolean gliding) {
        return Math.sin(time * (gliding ? 0.12 : 0.22)) * (gliding ? 0.35 : 1);
    }

    /** Feather {@code index}'s sweep back from the shoulder line in radians: the outer feathers trail furthest. */
    public static double featherSweep(int index, double spread) {
        return Math.toRadians(15 + index * 14) * spread;
    }
}
