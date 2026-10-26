package com.vincenthuto.hemomancy.common.entity.boss.saint.seraphae;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Decorative timelines shared by the skeleton, glow and material geometry; never gates damage or containment. */
public final class SeraphaeVisualRules {
    public static final BossMoveTimeline FRACTURE = new BossMoveTimeline(18, 62, 20);
    public static final BossMoveTimeline CONDENSE = new BossMoveTimeline(18, 44, 18);
    public static final int RESTORE_TICKS = 28;
    public static final int HIT = 1, FRACTURE_BURST = 2, DISPERSE_BURST = 3, CAPTURE = 4, SEALED = 5, HAZARD = 6;
    public static final int BURST_TICKS = 24, SEAL_TICKS = 44;
    public static final float MODEL_SCALE = 1.22F;

    public record Pose(float spread, float fold, float recoil, float strain, float lift, float glow) {}

    private SeraphaeVisualRules() {}

    public static float elapsed(SeraphaeState state, float remaining) {
        return Math.max(0, Math.min(state.duration(), state.duration() - remaining));
    }

    public static Pose pose(SeraphaeState state, float remaining, float age) {
        float t = elapsed(state, remaining);
        float breath = (float) Math.sin(age * .075F);
        return switch (state) {
            case STABLE -> new Pose(0, 1 - HemoEasing.smoothstep(t / RESTORE_TICKS), 0, 0, 0, .32F + breath * .07F);
            case FRACTURING -> {
                float spread = (float) HemoEasing.easeInOutCubic((t - FRACTURE.windup()) / 44.0);
                float recoil = HemoEasing.pulse(t, 8, 28);
                float strain = HemoEasing.smoothstep((t - 58) / 22);
                yield new Pose(spread, recoil * .3F, recoil, strain, spread * .16F,
                        .4F + spread * .45F + strain * .15F);
            }
            case DISPERSED -> new Pose(1, 0, 0, 0, .16F, .9F);
            case CONDENSING -> {
                float fold = HemoEasing.smoothstep(t / 42);
                float settle = HemoEasing.smoothstep((t - CONDENSE.windup()) / CONDENSE.active());
                yield new Pose(1 - fold, fold, 0, (1 - settle) * .5F, (1 - fold) * .16F,
                        .95F - settle * .55F);
            }
        };
    }

    /** A seeded, delayed wave: feather tips lag the shoulder instead of moving as one rigid sheet. */
    public static float featherWave(float age, int feather, int side, float strain) {
        return (float) Math.sin(age * (.12F + strain * .23F) - feather * .58F + side * .7F)
                * (.045F + strain * .14F) * (1 + feather * .07F);
    }

    public static float burstOpacity(float age, float life) {
        if (age < 0 || age >= life) return 0;
        return 1 - HemoEasing.smoothstep((age - 3) / (life - 3));
    }
}
