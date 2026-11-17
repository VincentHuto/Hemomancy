package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import net.minecraft.util.Mth;

/**
 * The hematic HUD's slow heart: a lub-dub that darkens the degree red rather than lighting it, quickening and fading
 * as the body's blood runs out.
 */
public final class HudHeartbeat {
    public static final float FULL_BPM = 25f;
    public static final float EMPTY_BPM = 45f;
    /** Strongest darkening mix, at full blood. */
    public static final float PEAK = 0.45f;
    /** How far behind the base, as a share of one beat, the farthest vein beats. */
    public static final float MAX_DELAY = 0.3f;
    /** One hour of ticks. */
    public static final long CLOCK_WRAP_TICKS = 72_000L;
    private static final int DEEP_RED = 0x280006;
    private static final int DEEP_AMBER = 0x461600;

    private HudHeartbeat() {
    }

    /** The HUD's clock in seconds, wrapped every hour so a float keeps whole-frame steps on a world of any age. */
    public static float seconds(long gameTime, float partialTick) {
        return (float) ((gameTime % CLOCK_WRAP_TICKS + (double) partialTick) / 20.0);
    }

    public static float bpm(float bloodRatio) {
        return EMPTY_BPM + (FULL_BPM - EMPTY_BPM) * Mth.clamp(bloodRatio, 0f, 1f);
    }

    /** One beat's shape over phase 0..1: a strong stroke, a softer one, then rest. */
    public static float stroke(float phase) {
        return Math.min(1f, (float) (Math.pow(peak(phase, 0.06f, 0.09f), 1.5) + 0.6 * Math.pow(peak(phase, 0.26f, 0.08f), 1.5)));
    }

    /** The darkening mix for a vein whose beat lags the base by {@code delayFraction} of the full travel. */
    public static float darkness(float seconds, float bloodRatio, float delayFraction) {
        float period = 60f / bpm(bloodRatio);
        float t = (seconds - delayFraction * MAX_DELAY * period) / period;
        float phase = t - (float) Math.floor(t);
        return stroke(phase) * PEAK * (0.5f + 0.5f * Mth.clamp(bloodRatio, 0f, 1f));
    }

    public static int darken(int rgb, float amount, boolean apotheos) {
        int deep = apotheos ? DEEP_AMBER : DEEP_RED;
        int out = 0;
        for (int shift = 0; shift < 24; shift += 8) {
            int from = (rgb >> shift) & 0xFF;
            int to = (deep >> shift) & 0xFF;
            out |= Math.round(from + (to - from) * amount) << shift;
        }
        return out;
    }

    private static float peak(float phase, float centre, float width) {
        return Math.max(0f, 1f - Math.abs(phase - centre) / width);
    }
}
