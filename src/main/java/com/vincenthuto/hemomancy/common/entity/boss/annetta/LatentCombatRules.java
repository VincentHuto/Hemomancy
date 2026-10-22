package com.vincenthuto.hemomancy.common.entity.boss.annetta;

import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Pure rules for the Latent Infection: its emergence and which attack it reaches for. No Minecraft types. */
public final class LatentCombatRules {
    public enum Attack { WHIP_LASH, SPEAR_THRUST, CLAW_RAKE, INFECTION_BLOOM, ROOT_ERUPTION }

    /** Hidden in her until it tears out (matching the purge's TEAR beat), then it rises. */
    public static final int EMERGE_HIDDEN = AnnettaEncounterRules.TEAR;
    public static final int EMERGE_END = EMERGE_HIDDEN + 34;

    private LatentCombatRules() {}

    public static float rise(float emergeTick) {
        return HemoEasing.smoothstep((emergeTick - EMERGE_HIDDEN) / (float) (EMERGE_END - EMERGE_HIDDEN));
    }

    public static Attack choose(double distance, int roll, boolean bloomReady, boolean rootsReady) {
        if (bloomReady && distance <= 6.0D && roll < 45) return Attack.INFECTION_BLOOM;
        if (distance > 8.0D) return rootsReady ? Attack.ROOT_ERUPTION : Attack.WHIP_LASH;
        if (distance <= 3.5D) return roll < 65 ? Attack.CLAW_RAKE : Attack.SPEAR_THRUST;
        return roll < 60 ? Attack.WHIP_LASH : Attack.SPEAR_THRUST;
    }

    public static int duration(Attack attack) {
        return switch (attack) {
            case WHIP_LASH -> 30;
            case SPEAR_THRUST -> 32;
            case CLAW_RAKE -> 24;
            case INFECTION_BLOOM -> 28;
            case ROOT_ERUPTION -> 36;
        };
    }

    /** The tick the blow lands (the rake also lands a second time eight ticks later). */
    public static int hitTick(Attack attack) {
        return switch (attack) {
            case WHIP_LASH -> 14;
            case SPEAR_THRUST -> 16;
            case CLAW_RAKE -> 8;
            case INFECTION_BLOOM -> 14;
            case ROOT_ERUPTION -> 22;
        };
    }

    public static int cooldown(Attack attack) {
        return switch (attack) {
            case WHIP_LASH -> 18;
            case SPEAR_THRUST -> 22;
            case CLAW_RAKE -> 12;
            case INFECTION_BLOOM -> 26;
            case ROOT_ERUPTION -> 30;
        };
    }

    /** Every so often it remembers the host that was taken from it. */
    public static boolean turnOnAnnetta(int roll, int ticksSinceLast) {
        return ticksSinceLast >= 300 && roll < 35;
    }
}
