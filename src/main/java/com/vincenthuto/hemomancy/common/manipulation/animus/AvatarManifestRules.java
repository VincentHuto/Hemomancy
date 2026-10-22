package com.vincenthuto.hemomancy.common.manipulation.animus;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation rules for the blood avatar's manifestation and dismissal cue, free of Minecraft types so they can be
 * tested on the JVM. The timeline matches the avatar layer's 20-tick transition: blood wells from the body and climbs
 * over it, sinew lashes out to the parts this stage grows, and the avatar breaks the surface with a splash; dismissed,
 * it slides down the body into a pool that drains back up the legs.
 */
public final class AvatarManifestRules {
    /** Matches {@code AvatarManifestationTransition.DURATION_TICKS}. */
    public static final int TRANSITION_TICKS = 20;
    public static final int VISUAL_TICKS = 24;

    /** Blood wells from the body's exits and climbs it in rings. */
    public static final int RISE_END = 8;
    /** Sinew lashes out to the avatar's grown parts. */
    public static final int GROW_END = 14;
    /** The avatar breaks the surface (the layer's overshoot runs at progress .70-.95). */
    public static final int EMERGE_END = 19;

    /** Dismissal: the shell slides down the body, pools, then drains back up the legs. */
    public static final int MELT_END = 12;
    public static final int DRAIN_START = 12;

    public static final int STAGES = 5;

    private AvatarManifestRules() {
    }

    /** Count for a manifestation at {@code stage} (0 bare avatar .. 4 complete). */
    public static int manifest(int stage) {
        return 1 + clampStage(stage);
    }

    /** Count for a dismissal of an avatar at {@code stage}. */
    public static int dismiss(int stage) {
        return 11 + clampStage(stage);
    }

    public static boolean dismissing(int count) {
        return count > 10;
    }

    public static int stage(int count) {
        return clampStage((Math.max(1, count) - 1) % 10);
    }

    public static double rise(double age) {
        return HemoEasing.smoothstep(age / RISE_END);
    }

    /** How far sinew has lashed out to the grown parts, with a slight overshoot before it settles. */
    public static double grow(double age) {
        double t = Math.max(0, Math.min(1, (age - RISE_END + 2) / (GROW_END - RISE_END + 2)));
        double c1 = 1.70158, c3 = c1 + 1;
        return t == 0 ? 0 : 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    /** Splash strength as the avatar breaks the surface. */
    public static double emerge(double age) {
        double t = (age - GROW_END) / (EMERGE_END - GROW_END);
        return t < 0 || t > 1 ? 0 : Math.sin(t * Math.PI);
    }

    /** 0 → 1 as everything left over frays upward after the emergence. */
    public static double fray(double age) {
        return HemoEasing.smoothstep((age - GROW_END - 1) / (VISUAL_TICKS - GROW_END - 1));
    }

    /** Height fraction the dismissing shell has slid down to: 1 the crown, 0 the feet. */
    public static double melt(double age) {
        return 1 - HemoEasing.smoothstep(age / MELT_END);
    }

    /** 0 → 1 as the ground pool drains back up into the legs. */
    public static double drain(double age) {
        return HemoEasing.smoothstep((age - DRAIN_START) / (VISUAL_TICKS - DRAIN_START));
    }

    /**
     * Height fraction (0 feet, 1 crown) of climbing ring {@code ring} of {@code rings} during the rise, or a negative
     * value before it leaves the feet.
     */
    public static double ringHeight(double age, int ring, int rings) {
        double start = ring * RISE_END * 0.5 / Math.max(1, rings);
        double t = (age - start) / (RISE_END * 0.8);
        return t < 0 ? -1 : Math.min(1, HemoEasing.smoothstep(t));
    }

    private static int clampStage(int stage) {
        return Math.max(0, Math.min(STAGES - 1, stage));
    }
}
