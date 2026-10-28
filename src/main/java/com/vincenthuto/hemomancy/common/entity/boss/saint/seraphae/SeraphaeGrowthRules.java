package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

/** A healing transfusion becomes uncontrolled growth when there is no wound left to close. */
public final class SeraphaeGrowthRules {
    public static final int CYCLE = 80, WARNING = 24, RELEASE = 16;
    public static final double REACH = 10;
    private SeraphaeGrowthRules() {}
    public static float healing(float health, float maximum) { return Math.max(0, Math.min(2, maximum - health)); }
    public static boolean convulses(int exposure) { return exposure >= 2; }
}
