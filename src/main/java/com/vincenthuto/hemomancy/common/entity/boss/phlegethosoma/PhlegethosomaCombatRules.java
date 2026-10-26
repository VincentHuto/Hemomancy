package com.vincenthuto.hemomancy.common.entity.boss.phlegethosoma;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;

/** Balance and phase rules shared by the arterial fluke's server combat and presentation. */
public final class PhlegethosomaCombatRules {
    public static final float MAX_HEALTH = 120;
    public static final float BITE_DAMAGE = 7;
    public static final int STAGGER_TICKS = 35;
    public static final int CLOT_LIFETIME = 160;
    public static final int MAX_CLOTS = 6;
    public static final int INTRO_TICKS = 60;
    public static final double SUCTION_RANGE = 10;
    private PhlegethosomaCombatRules() {}

    public static int phase(int current, float health, float maximum) {
        int reached = health <= maximum * .25F ? 3 : health <= maximum * .6F ? 2 : 1;
        return Math.max(current, reached);
    }

    public static BossMoveTimeline suction(int phase) {
        return phase >= 3 ? new BossMoveTimeline(20, 25, 25) : new BossMoveTimeline(30, 35, 25);
    }

    public static int suctionCooldown(int phase) { return phase >= 3 ? 90 : 120; }

    public static boolean interrupts(boolean suction, boolean directAttack, float accumulatedDamage) {
        return suction && directAttack && accumulatedDamage >= 6;
    }

    public static int clotCount(int phase, int existing) {
        return Math.max(0, Math.min(Math.clamp(phase, 1, 3), MAX_CLOTS - existing));
    }
}
