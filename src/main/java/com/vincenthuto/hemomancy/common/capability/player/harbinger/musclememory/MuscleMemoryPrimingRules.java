package com.vincenthuto.hemomancy.common.capability.player.harbinger.musclememory;

public final class MuscleMemoryPrimingRules {
    public static final int TICKS_PER_DOSE = 6_000;
    public static final int MAX_RESERVE_TICKS = 36_000;
    public static final int FLASK_DOSES = 3;
    public static final int JUG_DOSES = 6;
    public static final int OVEREXERT_WINDOW_TICKS = 200;
    public static final double OVEREXERT_BLOOD_MULTIPLIER = 2.0D;
    public static final float OVEREXERT_STRAIN_MULTIPLIER = 2.5F;

    private MuscleMemoryPrimingRules() {}

    public static int addReserve(int currentTicks, int addedTicks) {
        long total = (long) Math.max(0, currentTicks) + Math.max(0, addedTicks);
        return (int) Math.min(MAX_RESERVE_TICKS, total);
    }
}
