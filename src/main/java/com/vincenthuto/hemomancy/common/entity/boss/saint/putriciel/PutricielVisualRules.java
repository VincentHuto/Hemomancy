package com.vincenthuto.hemomancy.common.entity.boss.saint.putriciel;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

/** Presentation envelopes; the server independently owns damage, windows and victory. */
public final class PutricielVisualRules {
    public static final int NOVA_INTERVAL = 120, NOVA_WARNING = 24, NOVA_RELEASE = 18;
    public static final int ABSOLUTION_CYCLE = 300, ABSOLUTION_WINDOW = 80, ABSOLUTIONS_NEEDED = 5;
    public static final int HIT_TICKS = 14, BURST_TICKS = 32, REST_TICKS = 40;
    public static final int NOVA = 1, OPEN = 2, CLOSE = 3, ABSOLVE = 4, REST = 5, MELEE = 6;
    public static final int CLEANSE = 7;
    public static final float SCALE = 1.4F;
    public record Pose(float gather, float release, float offering, float flinch, float spores) {}
    private PutricielVisualRules() {}

    private static final BossMoveTimeline NOVA_MOTION = new BossMoveTimeline(8, 16, NOVA_RELEASE);
    public static BossMoveTimeline nova() { return NOVA_MOTION; }
    public static float warning(float remaining) {
        int duration = nova().windup() + nova().active();
        return remaining > 0 && remaining <= duration ? HemoEasing.smoothstep(1 - remaining / duration) : 0;
    }
    public static float radius(boolean escalated) { return escalated ? 12 : 8; }
    public static Pose pose(int visual, boolean absolved, int count, float remaining, float window, float nova) {
        return pose(visual, absolved, count, remaining, window, nova, visual == 1 ? remaining : 0);
    }
    public static Pose pose(int visual, boolean absolved, int count, float remaining, float window, float nova, float novaRelease) {
        float gather = warning(nova), release = 0;
        if (novaRelease > 0) {
            float t = Math.max(0, NOVA_RELEASE - novaRelease);
            gather = 1 - HemoEasing.smoothstep(t / 6);
            release = HemoEasing.pulse(t, 3, NOVA_RELEASE);
        }
        float offer = absolved ? HemoEasing.smoothstep((ABSOLUTION_WINDOW - window) / 12)
                * HemoEasing.smoothstep(window / 12) : 0;
        float flinch = visual == 2 ? HemoEasing.pulse(Math.max(0, HIT_TICKS - remaining), 3, HIT_TICKS) : 0;
        return new Pose(gather, release, offer, flinch,
                gather * .2F + release * .13F + offer * (.04F + progress(count) * .04F) - flinch * .08F);
    }
    public static float progress(int count) { return HemoEasing.clamp01(count / (float) ABSOLUTIONS_NEEDED); }
    public static float fade(float age, int life) {
        return age < 0 || age >= life ? 0 : 1 - HemoEasing.smoothstep((age - 3) / (life - 3));
    }
}
