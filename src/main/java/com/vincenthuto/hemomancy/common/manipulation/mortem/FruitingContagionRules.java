package com.vincenthuto.hemomancy.common.manipulation.mortem;

/**
 * Presentation timeline for Fruiting Contagion, free of Minecraft types so it can be tested on the JVM.
 * Pale stalks fruit out of a carrier and burst; where the rot spreads, a spore arc carries it to the neighbour,
 * which sprouts a small bloom of its own.
 */
public final class FruitingContagionRules {
    public static final int FRUIT_END = 6;
    public static final int BURST_END = 9;
    public static final int ARC_END = 15;
    public static final int VISUAL_TICKS = 24;
    public static final int FRUITING = 1, SPREADING = 2;

    private FruitingContagionRules() {
    }

    public static boolean spreading(int count) {
        return count == SPREADING;
    }

    /** How far the stalks have grown out of the carrier. */
    public static double growth(double age) {
        return smooth(age / FRUIT_END);
    }

    /** The caps bursting into spore clouds, a pulse peaking mid-burst. */
    public static double burst(double age) {
        if (age <= FRUIT_END - 1 || age >= BURST_END + 2) return 0;
        return Math.sin((age - (FRUIT_END - 1)) / (BURST_END - FRUIT_END + 3.0) * Math.PI);
    }

    /** How far the spore arc has carried toward the neighbour. */
    public static double arc(double age) {
        return smooth((age - FRUIT_END) / (ARC_END - FRUIT_END));
    }

    /** The neighbour's own small bloom, sprouting once the spores land. */
    public static double bloom(double age) {
        return smooth((age - ARC_END + 1) / 4);
    }

    /** Fade-out, 0 present to 1 gone. */
    public static double wither(double age) {
        return smooth((age - ARC_END - 2) / (VISUAL_TICKS - ARC_END - 2));
    }

    /** Height of the spore arc above the straight line at {@code t} along it, as a fraction of the distance. */
    public static double lift(double t) {
        return 0.35 * 4 * t * (1 - t);
    }

    public static double smooth(double t) {
        double c = Math.max(0, Math.min(1, t));
        return c * c * (3 - 2 * c);
    }
}
