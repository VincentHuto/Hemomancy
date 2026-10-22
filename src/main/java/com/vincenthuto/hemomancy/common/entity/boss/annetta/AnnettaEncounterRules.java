package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/**
 * Pure timing and decision rules for Annetta herself: when the Harbinger break may begin, the beats of the break
 * and of the Unstained purge, and which frantic move she reaches for in Phase 1. No Minecraft types.
 */
public final class AnnettaEncounterRules {
    /** Phase 1 cannot end before the player has lasted two minutes and dealt fifty damage. */
    public static final int BREAK_MIN_TICKS = 2400;
    public static final float BREAK_MIN_DAMAGE = 50.0F;

    // The break, in scene ticks.
    public static final int SHIELD_DROP = 12;
    public static final int LAST_WORDS = 24;
    /** Her left hand closes over her right on the hilt and both raise it. */
    public static final int GRIP = 28;
    public static final int STAB = 40;
    /** The dagger is in the tick; it swells against the blade until it pops and takes her head with it. */
    public static final int BURST = 54;
    public static final int DAGGER_DROP = 84;
    public static final int FALL_START = 86;
    public static final int FALL_END = 104;
    public static final int RISE = 314;

    // Phase 1 shield counter: a blocked blow is turned aside, then she drives the shield into the attacker.
    public static final int SHIELD_BASH_TICKS = 16;
    public static final int SHIELD_BASH_HIT = 7;

    // The purge, in scene ticks.
    public static final int DRINK_END = 30;
    public static final int TEAR = 90;
    public static final int KNEEL_END = 122;
    public static final int PURGE_END = 140;

    public enum Move { DAGGER_COMBO, STILL_DASH, STILL_BOLT, SHIELD_GUARD, SILVER_AURA }

    private AnnettaEncounterRules() {}

    public static boolean breakReady(int phaseOneTicks, float damageTaken) {
        return phaseOneTicks >= BREAK_MIN_TICKS && damageTaken >= BREAK_MIN_DAMAGE;
    }

    /** Damage she may take in Phase 1: whatever leaves her standing on one heart. */
    public static float guardedDamage(float health, float amount) {
        return Math.max(0.0F, Math.min(amount, health - 1.0F));
    }

    public static boolean headless(int sceneTick) {
        return sceneTick >= BURST;
    }

    /** How far the reanimating blood has crept up her fallen body. */
    public static float creep(float sceneTick) {
        return HemoEasing.smoothstep((sceneTick - FALL_END) / (float) (RISE - FALL_END));
    }

    public static float reanimationPoolRadius(float sceneTick) {
        return 0.4F + 2.4F * HemoEasing.smoothstep((sceneTick - FALL_END + 10) / (float) (RISE - FALL_END));
    }

    /** Red draining out of her hair while the Draught works. */
    public static float hairDrain(float sceneTick) {
        return HemoEasing.smoothstep((sceneTick - DRINK_END) / (float) (TEAR - DRINK_END));
    }

    /** She fights frantically: close in she cuts, guards when hit and slips away; further out she throws and dashes. */
    public static Move choose(double distance, boolean recentlyHurt, int roll) {
        if (distance <= 3.2D) {
            if (recentlyHurt && roll < 35) return Move.SHIELD_GUARD;
            if (roll < 70) return Move.DAGGER_COMBO;
            if (roll < 85) return Move.SILVER_AURA;
            return Move.STILL_DASH;
        }
        if (distance <= 6.0D) {
            if (roll < 30) return Move.SILVER_AURA;
            if (roll < 65) return Move.STILL_DASH;
            return Move.STILL_BOLT;
        }
        if (distance <= 11.0D) return roll < 50 ? Move.STILL_DASH : Move.STILL_BOLT;
        return roll < 35 ? Move.STILL_DASH : Move.STILL_BOLT;
    }

    public static int duration(Move move) {
        return switch (move) {
            case DAGGER_COMBO -> 28;
            case STILL_DASH -> 12;
            case STILL_BOLT -> 18;
            case SHIELD_GUARD -> 32;
            case SILVER_AURA -> 20;
        };
    }

    /** Pause after a move; she gets faster and more frantic as she weakens. */
    public static int cooldownAfter(Move move, float healthFraction) {
        int base = switch (move) {
            case DAGGER_COMBO -> 16;
            case STILL_DASH -> 8;
            case STILL_BOLT -> 22;
            case SHIELD_GUARD -> 10;
            case SILVER_AURA -> 30;
        };
        float scale = 0.45F + 0.55F * Math.max(0.0F, Math.min(1.0F, healthFraction));
        return Math.max(2, Math.round(base * scale));
    }

    /** How swollen the tick is in the break: its usual Phase 1 size until the stab, then it fills against the blade. */
    public static float breakSwell(float sceneTick, float phaseSwell) {
        return phaseSwell + (0.5F * (1.0F - phaseSwell) + 0.25F) * HemoEasing.smoothstep((sceneTick - STAB) / (float) (BURST - STAB));
    }
}
