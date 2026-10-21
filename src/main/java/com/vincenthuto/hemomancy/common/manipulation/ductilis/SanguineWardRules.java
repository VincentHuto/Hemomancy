package com.vincenthuto.hemomancy.common.manipulation.ductilis;

/**
 * Presentation rules for Sanguine Ward's nerve lattice, free of Minecraft types so they can be tested on the JVM. Two
 * counter-wound families of nerve strands climb the body into a lattice; each point of pool keeps one strand of each
 * family lit, and a crown ring closes over the top while the pool is full. The cue's {@code count} is the pool.
 */
public final class SanguineWardRules {
    /** Matches {@code ManipulationReactiveEvents.SANGUINE_WARD_POOL}: one strand per family per point of pool. */
    public static final int STRANDS = 6;
    /** Strands climb from the feet over the first ticks of the channel. */
    public static final int RAISE_TICKS = 6;
    /** Turns each strand makes as it climbs. */
    public static final double TURNS = 0.6;

    private SanguineWardRules() {
    }

    /** Strands still lit in each family for a pool of {@code count}. */
    public static int live(int count) {
        return Math.max(0, Math.min(STRANDS, count));
    }

    public static boolean crowned(int count) {
        return count >= STRANDS;
    }

    /** 0 → 1 as the lattice climbs at the start of the channel; later refreshes keep it raised. */
    public static double raise(double age) {
        double t = Math.max(0, Math.min(1, age / RAISE_TICKS));
        return 1 - Math.pow(1 - t, 3);
    }

    /**
     * Angle in radians of strand {@code index} of a family at climb fraction {@code t}; {@code family} +1 winds one way
     * and -1 the other, so the two families cross into diamonds.
     */
    public static double angle(int index, int family, double t, double time) {
        return index * Math.PI * 2 / STRANDS + family * t * TURNS * Math.PI * 2 + time * 0.012 * family;
    }

    /**
     * Climb fraction at which strand {@code up} of the +1 family crosses strand {@code down} of the -1 family, or -1
     * when they do not cross on the body. The lattice turns, so crossings slide as {@code time} runs.
     */
    public static double crossing(int up, int down, double time) {
        // angle(up, +1) - angle(down, -1) = (up - down) * 2pi/N + 2t * TURNS * 2pi + 2 * time * 0.012 must be a whole turn.
        double turns = -((up - down) / (double) STRANDS + time * 0.024 / (Math.PI * 2));
        double frac = turns - Math.floor(turns);
        double t = frac / (2 * TURNS);
        return t > 0.04 && t < 0.97 ? t : -1;
    }
}
