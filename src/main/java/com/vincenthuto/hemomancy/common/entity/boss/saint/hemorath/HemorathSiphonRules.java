package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

/** Server timings and costs for the blood-starved marrow. */
public final class HemorathSiphonRules {
    public static final int CYCLE = 100, WARNING = 24, RELEASE = 16, AURA_INTERVAL = 20;
    public static final double AURA_RADIUS = 6, SIPHON_RADIUS = 12;
    public static final int AURA_BLOOD = 20, SIPHON_BLOOD = 60;
    private HemorathSiphonRules() {}
    public static float leak(float stored) { return Math.max(0, stored - .25F); }
}
