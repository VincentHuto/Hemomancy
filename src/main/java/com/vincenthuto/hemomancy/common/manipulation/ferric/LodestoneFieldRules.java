package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingAxeLodestoneRules;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation timeline for the Living Axe's Lodestone field, free of Minecraft types so it can be tested on the JVM.
 * The bite drives a lodestone up out of the ground and cracks the floor to the rim. The held field draws filings in
 * along arched flux lines, and every gameplay pulse sends a contraction ripple from rim to core. At the end, the
 * lodestone rusts and sinks while the filings drop.
 */
public final class LodestoneFieldRules {
    public static final int VISUAL_TICKS = LivingAxeLodestoneRules.LIFETIME_TICKS;
    public static final int PULSE = LivingAxeLodestoneRules.PULSE_INTERVAL_TICKS;
    public static final int RISE_END = 4;
    public static final int SHOCK_END = 7;
    public static final int RIPPLE_TICKS = 7;
    public static final int DISSOLVE_START = VISUAL_TICKS - 14;
    public static final int FILINGS = 16;
    public static final int TEETH = 12;
    public static final int FLUX_LINES = 8;

    private LodestoneFieldRules() {
    }

    /** Lodestone height as a fraction of full: punches up past full, settles back, and sinks during the dissolve. */
    public static double rise(double age) {
        double up = age <= 0 ? 0 : age >= RISE_END ? 1
                : 1 + 0.22 * Math.sin(age / RISE_END * Math.PI) - Math.pow(1 - age / RISE_END, 3);
        return Math.max(0, up) * (1 - dissolve(age));
    }

    /** Fracture shockwave reach, 0..1 of the field radius, over the bite. */
    public static double shock(double age) {
        return HemoEasing.easeOutCubic(age / SHOCK_END);
    }

    /** Fracture brightness: full on the bite, faded once the shock lands. */
    public static double shockFade(double age) {
        return 1 - HemoEasing.smoothstep((age - SHOCK_END) / 6.0);
    }

    /** Ticks since the latest gameplay pulse; pulses fire on the bite and then once per interval. */
    public static double sincePulse(double age) {
        if (age < 0) return Double.MAX_VALUE;
        return age % PULSE;
    }

    /** Pulse envelope: snaps to 1 on the pulse and decays over the ripple. */
    public static double pulse(double age) {
        double since = sincePulse(age);
        if (since >= RIPPLE_TICKS || age >= VISUAL_TICKS) return 0;
        return Math.pow(1 - since / RIPPLE_TICKS, 2);
    }

    /** Contraction ripple radius as a fraction of the field, 1 at the rim closing to the core, or -1 when idle. */
    public static double ripple(double age) {
        double since = sincePulse(age);
        if (since >= RIPPLE_TICKS || age < SHOCK_END * 0.5 || age >= DISSOLVE_START) return -1;
        return 1 - 0.85 * HemoEasing.easeInCubic(since / RIPPLE_TICKS);
    }

    /**
     * Filing {@code index}'s travel from the rim to the lodestone, 0..1. Filings creep inward, lurch on each pulse,
     * and loop back to the rim so the field never empties.
     */
    public static double filing(double age, int index) {
        double phase = index * 0.618034 % 1;
        double creep = age / (PULSE * 3.0);
        double lurch = Math.floor(age / PULSE) * 0.18 + 0.18 * HemoEasing.easeOutCubic(Math.min(1, sincePulse(age) / 3.0));
        return (phase + creep + lurch) % 1;
    }

    /** Dissolve progress, 0 intact to 1 gone. */
    public static double dissolve(double age) {
        return HemoEasing.smoothstep((age - DISSOLVE_START) / (VISUAL_TICKS - DISSOLVE_START));
    }

    /** Rim tooth lean toward the core, in radians: they snap inward on each pulse. */
    public static double toothLean(double age) {
        return 0.18 + 0.45 * pulse(age);
    }
}
