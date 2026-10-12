package com.vincenthuto.hemomancy.client.render.tile.harbinger.crafting;

/**
 * Pure timing and allocation maths for the Somatic Loom's ritual effects: which warp threads carry which
 * tendency, how full the pirns and veins stand, and how the ganglion beats. No Minecraft types.
 */
public final class SomaticLoomEffectRules {
    private SomaticLoomEffectRules() {
    }

    /**
     * Tendency index per warp thread, or -1 for a bare thread. Required tendencies take contiguous bands in
     * tendency order, sized by requirement (largest remainder); each keeps at least one thread while threads last.
     */
    public static int[] threadTendencies(int[] requirements, int threads) {
        int[] out = new int[threads];
        java.util.Arrays.fill(out, -1);
        int total = 0, required = 0;
        for (int r : requirements) {
            total += Math.max(0, r);
            if (r > 0) required++;
        }
        if (total == 0 || threads <= 0) return out;
        int[] share = new int[requirements.length];
        double[] remainder = new double[requirements.length];
        int given = 0, minimums = 0;
        for (int t = 0; t < requirements.length; t++) {
            if (requirements[t] <= 0) continue;
            if (minimums >= threads) break;
            double quota = (double) requirements[t] * threads / total;
            share[t] = Math.max(1, (int) quota);
            remainder[t] = quota - (int) quota;
            given += share[t];
            minimums++;
        }
        // Minimums can overshoot: take back from the widest bands. Undershoot goes to the largest remainders.
        while (given > threads) {
            int widest = 0;
            for (int t = 1; t < share.length; t++) if (share[t] > share[widest]) widest = t;
            share[widest]--;
            given--;
        }
        while (given < threads && required > 0) {
            int best = -1;
            for (int t = 0; t < share.length; t++)
                if (share[t] > 0 && (best < 0 || remainder[t] > remainder[best])) best = t;
            share[best]++;
            remainder[best] = -1;
            given++;
        }
        int i = 0;
        for (int t = 0; t < share.length; t++)
            for (int k = 0; k < share[t] && i < threads; k++) out[i++] = t;
        return out;
    }

    /** Fraction of capacity, clamped to 0..1; empty, negative or unknown amounts read as empty. */
    public static float fill(double amount, double capacity) {
        if (!(capacity > 0) || !(amount > 0)) return 0f;
        return (float) Math.min(1.0D, amount / capacity);
    }

    /** Vial liquid height for stored stock: square-root, so a weave's few enzymes still show against 64. */
    public static float vialFill(float stock, int capacity) {
        return (float) Math.sqrt(fill(stock, capacity));
    }

    /** Pirn thread height for enzyme spun for the current weave; a tendency never needs more than 8. */
    public static float spunFill(float spun) {
        return fill(spun, 8);
    }

    /** A sharp 0..1 heartbeat that quickens as the ritual charge rises. */
    public static float heartbeat(float time, float charge) {
        double rate = 0.1D + 0.2D * Math.max(0f, Math.min(1f, charge));
        return (float) Math.pow(Math.max(0.0D, Math.sin(time * rate)), 6);
    }

    /** Where a pulse climbing the veins has reached, 0 at the sill to 1 at the ganglion; one pulse per period. */
    public static float pulseFront(float time, float period) {
        float t = time % period;
        return (t < 0 ? t + period : t) / period;
    }

    /** Depth of each woven band along the cloth, narrowing so every band fits between fell and breast beam. */
    public static float bandStep(int bands, float clothLength) {
        return Math.min(0.7f, clothLength / Math.max(1, bands));
    }

    /** Catalyst x across the cloth: one sits in the middle, more spread evenly from x0 to x1. */
    public static float catalystX(int index, int count, float x0, float x1) {
        if (count <= 1) return (x0 + x1) / 2f;
        return x0 + (x1 - x0) * index / (count - 1);
    }

    /** How far a strand's tether sags below the straight line to the fell: more for longer tethers, within limits. */
    public static float tetherSag(float length) {
        return Math.max(0.15f, Math.min(0.9f, length * 0.12f));
    }

    /** Where along a tether (0 at the strand, 1 at the fell) its travelling bead of light has reached. */
    public static float shimmerPosition(float time, float seed) {
        float t = (time * 0.02f + seed) % 1f;
        return t < 0 ? t + 1f : t;
    }

    /** Brightness 0..1 a point at `t` along the tether takes from a bead at `bead`: a short smooth peak. */
    public static float shimmer(float bead, float t) {
        float d = Math.abs(t - bead) / 0.08f;
        return d >= 1f ? 0f : 1f - d * d * (3f - 2f * d);
    }

    /**
     * Glow alpha of a warp thread: faint while its tendency is only asked for, brightening as that tendency's
     * strands are woven home, guttering below the faint level when the loom holds too little of its enzyme.
     */
    public static float threadGlow(float completed, boolean shortStock, float time, int thread) {
        if (shortStock) {
            float flicker = (float) Math.max(0.0D, Math.sin(time * 0.9D + thread * 1.7D));
            return 0.05f + 0.15f * flicker * flicker;
        }
        float c = Math.max(0f, Math.min(1f, completed));
        return 0.28f + 0.67f * c;
    }

    /**
     * How the echo presents on a loom of each stage: its scale on the warp and at the climax, how much of its
     * school's colour washes the grey (from unbound to fully woven), its alpha floor and span, and how badly it
     * stutters. A base loom barely holds a small grey memory; a Ganglionic one holds it steady and coloured; a
     * Cortical one sees the past Harbinger whole, at full stature.
     */
    public record EchoLook(float weavingScale, float climaxScale, float washMin, float washMax,
            float alphaMin, float alphaSpan, float stutter) {}

    private static final EchoLook[] ECHO_LOOKS = {
            new EchoLook(0.42f, 0.8f, 0.04f, 0.1f, 0.4f, 0.45f, 1.0f),
            new EchoLook(0.5f, 0.9f, 0.3f, 0.45f, 0.5f, 0.4f, 0.25f),
            new EchoLook(1.0f, 1.0f, 0.34f, 0.5f, 0.42f, 0.38f, 0.0f)};

    public static EchoLook echoLook(int stage) {
        return ECHO_LOOKS[Math.max(0, Math.min(ECHO_LOOKS.length - 1, stage))];
    }

    /** Alpha multiplier 0..1: with `stutter` the echo drops out now and then, a memory the loom barely holds. */
    public static float echoStutter(float time, float stutter) {
        if (stutter <= 0f) return 1f;
        double slow = Math.sin(time * 0.37D) * Math.sin(time * 0.113D + 1.7D);
        float dropout = (float) Math.pow(Math.max(0.0D, slow), 4);
        float jitter = 0.5f + 0.5f * (float) Math.sin(time * 2.3D);
        return 1f - stutter * Math.min(1f, dropout * 1.6f + 0.12f * jitter);
    }

    /**
     * Blocks from the loom to the wall of a Cortical weave's Chamber dome: past the 12-block ritual radius, so no
     * thread hunting the weaver can fall behind the far wall's depth and vanish.
     */
    public static final float DOME_RADIUS = 13.0f;
    private static final float DOME_RISE_TICKS = 40f;

    /**
     * Radius of the Chamber dome a Cortical loom raises while it weaves: it swells over the first ticks, holds
     * (paused or not), and closes into the fell as the echo sinks at the end of the climax.
     */
    public static float domeRadius(int stage, boolean weaving, float weaveTicks, boolean climax, float climaxProgress) {
        if (stage < 2 || !weaving) return 0f;
        if (climax) {
            float sink = Math.max(0f, Math.min(1f, (climaxProgress - 0.7f) / 0.3f));
            return DOME_RADIUS * (1f - sink * sink * (3f - 2f * sink));
        }
        float rise = Math.max(0f, Math.min(1f, weaveTicks / DOME_RISE_TICKS));
        return DOME_RADIUS * (1f - (1f - rise) * (1f - rise));
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }

    private static float smooth(float t) {
        return t * t * (3f - 2f * t);
    }

    /**
     * How much of the dome's ground seam is torn open, 0..1, as an arc running around the circle: it tears open as
     * the dome rises and knits shut as the echo sinks at the end of the climax.
     */
    public static float seamOpen(float weaveTicks, boolean climax, float climaxProgress) {
        if (climax) return 1f - smooth(clamp01((climaxProgress - 0.7f) / 0.3f));
        return clamp01(weaveTicks / 30f);
    }

    /** How far the seam's veins have crept across the floor: after the tear, and gone before the seam closes. */
    public static float veinGrowth(float weaveTicks, boolean climax, float climaxProgress) {
        if (climax) return 1f - smooth(clamp01((climaxProgress - 0.7f) / 0.2f));
        return smooth(clamp01((weaveTicks - 10f) / 50f));
    }

    /** Sinews tie the sky to the ground once the seam is open, and snap when the echo begins to sink. */
    public static float sinewTension(float weaveTicks, boolean climax, float climaxProgress) {
        if (climax) return climaxProgress < 0.7f ? 1f : 0f;
        return clamp01((weaveTicks - 30f) / 20f);
    }
}
