package com.vincenthuto.hemomancy.common.entity.boss.saint.hemorath;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Decorative motion and shared attack durations. Visual envelopes never decide hits, blood costs or rewards. */
public final class HemorathVisualRules {
    public static final int COLLAPSE_WINDUP = 60, FAST_COLLAPSE_WINDUP = 30, COLLAPSE_IMPACT = 14;
    public static final int PULSE_WINDUP = 25, PULSE_IMPACT = 12, OVERLOAD_TICKS = 36;
    // These goals use Minecraft's default alternate-tick scheduler; visuals count world ticks.
    public static final int GOAL_TICK_INTERVAL = 2;
    public static final double PULSE_RADIUS = 6;
    public static final int DEBT_HIT = 1, PULSE_RELEASE = 2, PHASE_BREAK = 3, INTAKE = 4, EXSANGUINATE = 5, MELEE = 6;
    public static final int BURST_TICKS = 24, FINALE_TICKS = 32;
    public static final int SIPHON = 7;

    public record Pose(float lift, float reach, float bow, float recoil, float ribs, float slump, float pressure) {}

    private HemorathVisualRules() {}

    public static BossMoveTimeline collapse(boolean phase2) {
        int duration = (phase2 ? FAST_COLLAPSE_WINDUP : COLLAPSE_WINDUP) * GOAL_TICK_INTERVAL;
        return new BossMoveTimeline(duration / 5, duration - duration / 5, COLLAPSE_IMPACT);
    }

    public static float scale(boolean phase2) { return phase2 ? 2.12F : 2.06F; }

    public static float storedPressure(float absorbed) {
        return HemoEasing.clamp01(absorbed / HemorathEntity.OVERLOAD_THRESHOLD);
    }

    public static float elapsed(int duration, float remaining) { return Math.max(0, Math.min(duration, duration - remaining)); }

    public static Pose pose(int visual, boolean charging, boolean phase2, float remaining) {
        if (visual == HemorathEntity.VISUAL_OVERLOAD) {
            float t = elapsed(OVERLOAD_TICKS, remaining);
            float swell = HemoEasing.smoothstep(t / 11);
            float slump = HemoEasing.smoothstep((t - 18) / 16);
            return new Pose(swell * (1 - slump), swell * .4F * (1 - slump), -.25F * swell + .85F * slump,
                    0, swell * (1 - slump * .7F), slump, 1 - slump);
        }
        if (visual == HemorathEntity.VISUAL_COLLAPSE_IMPACT) {
            float t = elapsed(COLLAPSE_IMPACT, remaining);
            float recover = 1 - HemoEasing.smoothstep(t / COLLAPSE_IMPACT);
            float snap = HemoEasing.pulse(t, 2, 11);
            return new Pose(recover * (1 - snap), snap * .6F, -.1F * recover + snap * .38F,
                    snap, recover * .45F, 0, recover);
        }
        if (visual == HemorathEntity.VISUAL_EMPTY_PULSE_IMPACT) {
            float t = elapsed(PULSE_IMPACT, remaining);
            float recover = 1 - HemoEasing.smoothstep(t / PULSE_IMPACT);
            float recoil = HemoEasing.pulse(t, 2, 9);
            return new Pose(recover * .35F, recover + recoil * .25F, -.08F * recover + recoil * .2F,
                    recoil, recover * .55F, 0, recover);
        }
        if (charging || visual == HemorathEntity.VISUAL_COLLAPSE_WINDUP) {
            int duration = collapse(phase2).commitTick() + collapse(phase2).active();
            float t = elapsed(duration, remaining), speed = duration / 60F;
            float gather = (float) HemoEasing.easeInOutCubic((t - duration / 5F) / (duration * .65F));
            float brace = HemoEasing.pulse(t / speed, 8, 25);
            return new Pose(gather, 0, brace * .2F - gather * .1F, 0, gather * .45F, 0, gather);
        }
        if (visual == HemorathEntity.VISUAL_EMPTY_PULSE) {
            float t = elapsed(PULSE_WINDUP * GOAL_TICK_INTERVAL, remaining) / GOAL_TICK_INTERVAL;
            float gather = (float) HemoEasing.easeInOutCubic((t - 6) / 18.0);
            float brace = HemoEasing.pulse(t, 5, 16);
            return new Pose(gather * .35F, gather, brace * .23F - gather * .08F, 0, gather * .55F, 0, gather);
        }
        return new Pose(0, 0, 0, 0, 0, 0, 0);
    }

    public static float fade(float age, int life) {
        return age < 0 || age >= life ? 0 : 1 - HemoEasing.smoothstep((age - 2) / (life - 2));
    }
}
