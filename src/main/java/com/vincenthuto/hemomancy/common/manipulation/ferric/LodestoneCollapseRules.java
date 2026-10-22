package com.vincenthuto.hemomancy.common.manipulation.ferric;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Presentation timeline for Lodestone Collapse, free of Minecraft types so it can be tested on the JVM.
 * The magnetic field contracts, iron shards spiral in from the edge, crush together at the point, then sink.
 */
public final class LodestoneCollapseRules {
    public static final int FIELD_END = 5;
    public static final int DRAW_END = 13;
    public static final int CRUSH_END = 17;
    public static final int VISUAL_TICKS = 30;
    public static final int SHARDS = 10;
    private static final double STAGGER = 0.6;

    private LodestoneCollapseRules() {
    }

    /** Field ring radius as a fraction of the cast radius: it tightens as the shards are drawn. */
    public static double fieldRadius(double age) {
        return 1 - 0.75 * HemoEasing.smoothstep((age - 1) / (DRAW_END - 1));
    }

    /** How far shard {@code index} has travelled from the edge to the point, 0..1; shards set off staggered. */
    public static double draw(double age, int index) {
        double start = FIELD_END + (index % 4) * STAGGER;
        // Every group has the same flight time, so the last to leave still lands as the crush begins.
        return HemoEasing.easeInCubic((age - start) / (DRAW_END - FIELD_END - 3 * STAGGER));
    }

    /**
     * Polar offset of shard {@code index} at travel {@code t}: a quarter-turn spiral from the rim to a tight knot.
     * Returns {angle, distance fraction, height fraction}.
     */
    public static double[] shard(int index, double t, int seed) {
        double start = index * 2.39996 + seed * 0.37;
        double angle = start + t * Math.PI * 0.5;
        double distance = 1 - t * 0.92;
        double height = 0.15 + 0.55 * ((index * 7 + seed) % 5) / 4.0 * (1 - t);
        return new double[] {angle, distance, height};
    }

    /** The crush pulse as the shards meet: rises past full scale to 1.25, then falls back to nothing. */
    public static double crush(double age) {
        if (age <= DRAW_END || age >= CRUSH_END) return 0;
        return Math.sin((age - DRAW_END) / (CRUSH_END - DRAW_END) * Math.PI) * 1.25;
    }

    /** Sink and dissolve, 0 intact to 1 gone. */
    public static double sink(double age) {
        return HemoEasing.smoothstep((age - CRUSH_END) / (VISUAL_TICKS - CRUSH_END));
    }
}
