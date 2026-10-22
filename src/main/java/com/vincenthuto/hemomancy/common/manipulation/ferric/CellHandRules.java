package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation rules for the cell hands (Blood Projection and Blood Absorption), free of Minecraft types so they can be
 * tested on the JVM. Summoning bursts the forearm's veins and wraps the blood into the hardened hand; dispelling
 * unravels it back into the arm; while the hand is used it either pushes pressure out of the palm or draws strands in.
 */
public final class CellHandRules {
    public static final int SUMMON_PROJECTION = 1, SUMMON_ABSORPTION = 2, DISPEL = 3, PROJECTING = 4, ABSORBING = 5;

    public static final int SUMMON_TICKS = 14;
    /** Strands burst out of the forearm. */
    public static final int ERUPT_END = 3;
    /** The strands coil round the hand and a shell closes over it. */
    public static final int WRAP_END = 9;
    /** The hand sets with a flash. */
    public static final int SET_END = 11;

    public static final int DISPEL_TICKS = 12;
    /** Use cues are refreshed this often while the hand is held in use, and last a little longer. */
    public static final int USE_REFRESH = 10;
    public static final int USE_TICKS = 14;

    public static final int STRANDS = 5;

    private CellHandRules() {
    }

    public static int summonMode(boolean absorption) {
        return absorption ? SUMMON_ABSORPTION : SUMMON_PROJECTION;
    }

    public static int useMode(boolean absorption) {
        return absorption ? ABSORBING : PROJECTING;
    }

    public static boolean summon(int mode) {
        return mode == SUMMON_PROJECTION || mode == SUMMON_ABSORPTION;
    }

    public static boolean using(int mode) {
        return mode == PROJECTING || mode == ABSORBING;
    }

    public static boolean absorption(int mode) {
        return mode == SUMMON_ABSORPTION || mode == ABSORBING;
    }

    public static int ticks(int mode) {
        return summon(mode) ? SUMMON_TICKS : mode == DISPEL ? DISPEL_TICKS : USE_TICKS;
    }

    /** 0 → 1 as the strands leave the forearm. */
    public static double erupt(double age) {
        return HemoEasing.smoothstep(age / ERUPT_END);
    }

    /** 0 → 1 as the strands wrap the hand. */
    public static double wrap(double age) {
        return HemoEasing.smoothstep((age - ERUPT_END) / (WRAP_END - ERUPT_END));
    }

    /** Flash strength at the set, peaking a tick in. */
    public static double flash(double age) {
        double t = (age - WRAP_END) / (SET_END - WRAP_END);
        return t < 0 || t > 1 ? 0 : Math.sin(t * Math.PI);
    }

    /** 0 → 1 as a summon's leftover strands fray after the set. */
    public static double settle(double age) {
        return HemoEasing.smoothstep((age - SET_END) / (SUMMON_TICKS - SET_END));
    }

    /** 0 → 1 as a dispelled hand unravels and its blood sinks back into the arm. */
    public static double unravel(double age) {
        return HemoEasing.smoothstep(age / (DISPEL_TICKS * 0.75));
    }

    /**
     * Phase 0..1 of a use pulse: projection pushes rings out of the palm, absorption draws them in, so the same phase
     * runs the opposite way.
     */
    public static double usePulse(double time, int ring, boolean absorption) {
        double phase = (time * 0.07 + ring / 3.0) % 1;
        return absorption ? 1 - phase : phase;
    }
}
