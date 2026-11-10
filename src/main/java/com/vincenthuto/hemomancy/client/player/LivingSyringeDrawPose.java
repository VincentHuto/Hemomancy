package com.vincenthuto.hemomancy.client.player;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;

/** Points the syringe arm at the mob being sampled, then brings it in to show the drum. */
public final class LivingSyringeDrawPose {
    private LivingSyringeDrawPose() { }

    /** {@code aimPitch} and {@code aimYaw} use the same convention as the head's xRot and yRot. */
    public static void apply(HumanoidModel<?> model, float tick, boolean right, float aimPitch, float aimYaw) {
        float weight = LivingSyringeDrawTimeline.weight(tick);
        float lift = LivingSyringeDrawTimeline.lift(tick);
        float inward = right ? 1 : -1;
        var arm = right ? model.rightArm : model.leftArm;
        float pitch = Mth.HALF_PI * -1 + Mth.lerp(lift, aimPitch, -.45F) + .05F * LivingSyringeDrawTimeline.pull(tick);
        arm.xRot = Mth.lerp(weight, arm.xRot, pitch);
        arm.yRot = Mth.lerp(weight, arm.yRot, Mth.lerp(lift, aimYaw, inward * .55F));
        arm.zRot = Mth.lerp(weight, arm.zRot, 0);
        model.body.xRot = Mth.lerp(weight, model.body.xRot, .14F * LivingSyringeDrawTimeline.thrust(tick));
        if (model instanceof PlayerModel<?> player) {
            player.rightSleeve.copyFrom(model.rightArm);
            player.leftSleeve.copyFrom(model.leftArm);
            player.jacket.copyFrom(model.body);
        }
    }
}
