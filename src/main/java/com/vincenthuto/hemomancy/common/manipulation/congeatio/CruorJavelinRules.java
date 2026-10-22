package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation timeline for Cruor Javelin, free of Minecraft types so it can be tested on the JVM.
 * The shaft freezes together at the hand, flies, bursts into frost fingers on the body, then shatters.
 */
public final class CruorJavelinRules {
    public static final int FORM_END = 2;
    public static final int FLIGHT_END = 4;
    public static final int IMPACT_END = 7;
    public static final int VISUAL_TICKS = 18;

    private CruorJavelinRules() {
    }

    /** How much of the shaft has frozen together at the hand. */
    public static double formation(double age) {
        return HemoEasing.smoothstep(age / FORM_END);
    }

    /** Fraction of the flight covered, accelerating like a thrown weight. */
    public static double travel(double age) {
        double t = HemoEasing.clamp01((age - FORM_END) / (FLIGHT_END - FORM_END));
        return t * t;
    }

    /** Frost bursting from the point of impact, 0 to 1. */
    public static double impact(double age) {
        return HemoEasing.smoothstep((age - FLIGHT_END) / (IMPACT_END - FLIGHT_END));
    }

    /** The lodged shaft shattering and melting away, 0 whole to 1 gone. */
    public static double shatter(double age) {
        return HemoEasing.smoothstep((age - IMPACT_END) / (VISUAL_TICKS - IMPACT_END));
    }
}
