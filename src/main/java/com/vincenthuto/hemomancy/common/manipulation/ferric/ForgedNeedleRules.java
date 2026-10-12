package com.vincenthuto.hemomancy.common.manipulation.ferric;

/**
 * Shape and timing rules for forged Blood Needles, free of Minecraft types so they can be tested on the JVM.
 * Blood drawn from the wrist hardens into square-section iron flechettes with tail fins; lances grow barbs.
 */
public final class ForgedNeedleRules {
    public static final double LENGTH = 0.8, LANCE_LENGTH = 1.4;
    public static final double WIDTH = 0.032, LANCE_WIDTH = 0.045;
    /** Where the bodkin head swells, as a fraction of the length from the tail. */
    public static final double SHOULDER = 0.7;
    public static final int LANCE_BARBS = 3;
    /** A full-charge lance pins what it strikes. */
    public static final int LANCE_PIN_TICKS = 20;
    public static final int LANCE_PIN_AMPLIFIER = 2;

    private ForgedNeedleRules() {
    }

    /** Half-width of the shaft at {@code t} from tail (0) to point (1): tapered tail, swollen shoulder, sharp point. */
    public static double profile(double t, double width) {
        if (t <= 0 || t >= 1) return t <= 0 ? width * 0.6 : 0;
        if (t < SHOULDER) return width * (0.6 + 0.55 * t / SHOULDER);
        return width * 1.15 * (1 - (t - SHOULDER) / (1 - SHOULDER));
    }

    /** Barb {@code index}'s root along the shaft, from the tail; barbs sit just behind the shoulder. */
    public static double barbStation(int index) {
        return SHOULDER - 0.12 - index * 0.14;
    }

    /** Charge-time forging: how much of needle {@code i} of {@code count} has hardened at {@code progress}. */
    public static double forged(double progress, int i, int count) {
        double lag = count <= 1 ? 0 : 0.35 * i / (count - 1.0);
        double t = Math.max(0, Math.min(1, (progress - lag) / (1 - lag) * 1.15));
        return t * t * (3 - 2 * t);
    }
}
