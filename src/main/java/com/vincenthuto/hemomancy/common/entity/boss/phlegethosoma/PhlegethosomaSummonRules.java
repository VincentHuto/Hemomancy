package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.world.chum.ChumRules;

/**
 * CR-7, the pure numbers of the Phlegethosoma's summoning. Blood projected into basin ichor is counted per pool, at the
 * chum rate, and the count fades like chum (half in 45 seconds), so a short break in the pour does not reset it. At
 * {@link #SUMMON_ML} the animal rises. A summoned one that has had no target for {@link #DIVE_AFTER_TICKS} dives and is
 * gone {@link #DEPART_TICKS} later, with nothing left behind (NQ-06-8).
 */
public final class PhlegethosomaSummonRules {
    public static final double SUMMON_ML = 500.0D;
    public static final double ML_PER_TICK = ChumRules.ML_PER_TICK;
    /** Pours this close to a counted pour, across the surface, add to the same count. */
    public static final double POOL_REACH = 3.5D;
    public static final int DIVE_AFTER_TICKS = 1200;
    public static final int DEPART_TICKS = 40;
    /** Another living Phlegethosoma this close refuses a summon. */
    public static final double SPACING = 64.0D;

    private PhlegethosomaSummonRules() {
    }

    /** What a pool counted at {@code ml} holds {@code ticks} later. */
    public static double fade(double ml, long ticks) {
        return ChumRules.fade(ml, ticks, false);
    }

    /** A count of {@code ml}, faded for {@code ticks}, after one more tick's pour. */
    public static double pour(double ml, long ticks, double added) {
        return Math.min(SUMMON_ML, fade(ml, ticks) + Math.max(0.0D, added));
    }

    public static boolean reached(double ml) {
        return ml >= SUMMON_ML - 1.0E-6D;
    }

    public static boolean shouldDive(int ticksWithoutTarget) {
        return ticksWithoutTarget >= DIVE_AFTER_TICKS;
    }
}
