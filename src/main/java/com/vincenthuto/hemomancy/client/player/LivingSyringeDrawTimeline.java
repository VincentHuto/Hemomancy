package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** The sample draw, in ticks: reach, pull the blood across, fill the vial, then hold still while the drum turns. */
public final class LivingSyringeDrawTimeline {
    public static final float BLOOD_START = 4, BLOOD_ARRIVES = 9, FILL = 10, TAIL_START = 11, BLOOD_END = 15;
    public static final float INDEX = 15, RECOVER = 24, DURATION = 30;

    private LivingSyringeDrawTimeline() { }

    /** How far the pose has taken over from the ordinary hold. */
    public static float weight(float tick) {
        return HemoEasing.smoothstep(tick / 3) * (1 - HemoEasing.smoothstep((tick - RECOVER) / (DURATION - RECOVER)));
    }

    /** The jab into the mob, withdrawn once the blood has crossed. */
    public static float thrust(float tick) {
        return HemoEasing.smoothstep(tick / BLOOD_START) * (1 - HemoEasing.smoothstep((tick - TAIL_START) / 4));
    }

    /** The syringe raised towards the body so the drum can be seen turning. */
    public static float lift(float tick) {
        return HemoEasing.smoothstep((tick - TAIL_START) / 4);
    }

    /** The heartbeat of the pull, -1 to 1, silent outside it. */
    public static float pull(float tick) {
        boolean pulling = tick >= BLOOD_START && tick < BLOOD_END;
        return pulling ? (float) Math.sin((tick - BLOOD_START) * 1.6F) : 0;
    }

    /** How far along the path from mob to vial the blood's leading edge has run. */
    public static float head(float tick) {
        return HemoEasing.smoothstep((tick - BLOOD_START) / (BLOOD_ARRIVES - BLOOD_START));
    }

    /** How far along the path the blood's trailing end has left the mob. */
    public static float tail(float tick) {
        return HemoEasing.smoothstep((tick - TAIL_START) / (BLOOD_END - TAIL_START));
    }

    /** The vials on show: the new sample's only appears once the blood has reached it. */
    public static int shownLoad(int load, float tick) {
        return tick < FILL ? load - 1 : load;
    }

    /** The drum position: it holds on the filling vial until the blood has finished. */
    public static int drumLoad(int load, float tick) {
        return tick < INDEX ? load - 1 : load;
    }
}
