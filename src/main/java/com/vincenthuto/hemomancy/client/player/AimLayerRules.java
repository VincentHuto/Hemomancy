package com.vincenthuto.hemomancy.client.player;

import com.vincenthuto.hemomancy.common.manipulation.animation.CastPhase;

/** The aim layer owns the lead (right, pre-mirror) arm and head from release through recovery. */
public final class AimLayerRules {
    private AimLayerRules() {}
    /** 0 before release; ramps in over the first two release ticks; fades across recovery. */
    public static float weight(CastPhase phase, float time, int releaseDuration, int recoveryDuration) {
        return switch (phase) {
            case RELEASE -> Math.clamp(time/2, 0, 1);
            case RECOVERY -> recoveryDuration <= 0 ? 0 : Math.clamp(1 - time/recoveryDuration, 0, 1);
            default -> 0;
        };
    }
    public static CastingPose apply(CastingPose base, CastingPose aim, float w) {
        if (w <= 0) return base;
        return new CastingPose(base.head().blend(aim.head(), w), base.body(), base.rightArm().blend(aim.rightArm(), w),
                base.leftArm(), base.rightLeg(), base.leftLeg(), base.pitch());
    }
    public static HandPose applyFirst(HandPose base, HandPose aim, float w) {
        return w <= 0 ? base : new HandPose(base.lead().blend(aim.lead(), w), base.support());
    }
}
