package com.vincenthuto.hemomancy.common.manipulation.ductilis;

/**
 * Presentation rules for Hematic Rebuke and Impressment's nerve command, free of Minecraft types so they can be
 * tested on the JVM. Rebuke is a crackling, recoiling seizure of the nerves; Impressment a steady conducted leash.
 */
public final class NerveCommandRules {
    public static final int REBUKE = 1, IMPRESS = 2;
    /** The cast impulse that carries the command from the caster's eyes. */
    public static final int IMPULSE_TICKS = 6;
    /** Ticks between loyal pulses along an impressed tether. */
    public static final double LEASH_PERIOD = 24;
    public static final int NET_STRANDS = 6;

    private NerveCommandRules() {
    }

    public static boolean rebuke(int count) {
        return count != IMPRESS;
    }

    /** How far the cast impulse has travelled from the caster to the commanded head, then 1 once it lands. */
    public static double impulse(double age) {
        double t = Math.max(0, Math.min(1, age / (IMPULSE_TICKS - 2.0)));
        return t * t;
    }

    /** Impulse visibility: bright while travelling, gone shortly after it lands. */
    public static double impulseFade(double age) {
        return age < IMPULSE_TICKS - 2 ? 1 : Math.max(0, 1 - (age - (IMPULSE_TICKS - 2)) / 2);
    }

    /** Nerve jitter amplitude: rebuke recoils hard on arrival and settles into a steady tremor; impressment barely moves. */
    public static double jitter(double age, boolean rebuke) {
        if (!rebuke) return 0.01;
        double recoil = Math.max(0, 1 - Math.max(0, age - (IMPULSE_TICKS - 2)) / 10);
        return 0.025 + 0.09 * recoil;
    }

    /** Phase 0..1 of the pulse running down the spine: fast and fearful for rebuke, slow for impressment. */
    public static double spinePulse(double time, boolean rebuke, int seed) {
        double period = rebuke ? 7 : 18;
        return ((time + seed) % period) / period;
    }

    /** Phase 0..1 of the loyal pulse travelling from caster to the impressed body. */
    public static double leashPulse(double time, int seed) {
        return ((time + seed * 3) % LEASH_PERIOD) / LEASH_PERIOD;
    }
}
