package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

/** Moving resists stillness; a successful strike opens a longer release from the field. */
public final class VelorumStillnessRules {
    public static final int MAXIMUM = 100, ROOT_THRESHOLD = 80, BREAK_TICKS = 60;
    public static final int BREAK_COOLDOWN = 180;
    public static final double RADIUS = 12;
    private VelorumStillnessRules() {}
    public static int next(int current, boolean moving, boolean interrupted) {
        return Math.max(0, Math.min(MAXIMUM, current + (interrupted ? -5 : moving ? -2 : 1)));
    }
    public static boolean rooted(int pressure) { return pressure >= ROOT_THRESHOLD; }
}
