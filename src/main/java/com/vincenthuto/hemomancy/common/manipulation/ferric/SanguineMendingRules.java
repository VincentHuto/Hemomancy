package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation timeline for one Sanguine Mending pulse, free of Minecraft types so it can be tested on the JVM. Blood
 * leaves the wrist and winds along the held tool, each crack in it is stitched closed and riveted with blood-iron, and
 * the blood draws back into the wrist. Every paid channel pulse replays the whole timeline.
 */
public final class SanguineMendingRules {
    /** The server cue length for one pulse; channel pulses are 20 ticks apart, so each pulse is a fresh cue. */
    public static final int VISUAL_TICKS = 18;
    public static final int DRAW_END = 5;
    public static final int SEAL_END = 12;
    public static final int CRACKS = 3;

    private SanguineMendingRules() {
    }

    /** 0 → 1 as the strands wind out along the tool. */
    public static double draw(double age) {
        return HemoEasing.smoothstep(age / DRAW_END);
    }

    /** 0 → 1 as crack {@code index} is stitched closed; cracks close one after another. */
    public static double seal(double age, int index) {
        double span = (SEAL_END - DRAW_END) / (double) CRACKS;
        return HemoEasing.smoothstep((age - DRAW_END - index * span) / (span * 1.4));
    }

    /** 0 → 1 as the strands draw back into the wrist. */
    public static double recede(double age) {
        return HemoEasing.smoothstep((age - SEAL_END) / (VISUAL_TICKS - SEAL_END));
    }

    /** Where along the tool (0 grip, 1 tip) crack {@code index} sits. */
    public static double crackAt(int index) {
        return 0.3 + 0.6 * (index + 0.5) / CRACKS;
    }
}
