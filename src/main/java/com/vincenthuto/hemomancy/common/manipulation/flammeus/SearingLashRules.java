package com.vincenthuto.hemomancy.common.manipulation.flammeus;

/**
 * Presentation timeline for Searing Lash, free of Minecraft types so it can be tested on the JVM.
 * A whip of boiling blood uncoils from the hand, cracks against the target, then sags and burns away.
 */
public final class SearingLashRules {
    public static final int UNCOIL_END = 4;
    public static final int CRACK_END = 6;
    public static final int VISUAL_TICKS = 16;

    private SearingLashRules() {
    }

    /** Packs the side the whip swings from into a count that is always at least one. */
    public static int encode(boolean leftHanded) {
        return leftHanded ? 2 : 1;
    }

    public static int side(int count) {
        return count == 2 ? -1 : 1;
    }

    /** How much of the line between hand and target the whip has reached. */
    public static double extent(double age) {
        double t = clamp(age / UNCOIL_END);
        return 1 - Math.pow(1 - t, 3);
    }

    /**
     * Sideways displacement of the whip at {@code t} along it: a wave running from hand to tip while it uncoils,
     * dying away once it cracks.
     */
    public static double wave(double t, double age, int side) {
        double amplitude = 0.45 * (1 - extent(age)) + 0.12 * (1 - slack(age)) * Math.max(0, 1 - Math.abs(age - UNCOIL_END));
        return side * amplitude * Math.sin(t * Math.PI * 2.5 - age * 1.8) * Math.sin(t * Math.PI);
    }

    /** The crack at the tip: peaks as the whip arrives. */
    public static double crack(double age) {
        if (age <= UNCOIL_END - 1 || age >= CRACK_END + 1) return 0;
        return Math.sin((age - (UNCOIL_END - 1)) / (CRACK_END - UNCOIL_END + 2.0) * Math.PI);
    }

    /** How far the spent whip has sagged and burned away, 0 taut to 1 gone. */
    public static double slack(double age) {
        return smooth((age - CRACK_END) / (VISUAL_TICKS - CRACK_END));
    }

    public static double smooth(double t) {
        double c = clamp(t);
        return c * c * (3 - 2 * c);
    }

    private static double clamp(double t) {
        return Math.max(0, Math.min(1, t));
    }
}
