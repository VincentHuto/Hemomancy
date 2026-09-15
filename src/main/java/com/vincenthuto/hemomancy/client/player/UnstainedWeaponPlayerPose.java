package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.util.Mth;

public final class UnstainedWeaponPlayerPose {
    private UnstainedWeaponPlayerPose() { }

    public static ArmPose dagger(float progress, boolean rightHanded) {
        Motion motion = daggerMotion(progress);
        float side = rightHanded ? -1 : 1;
        return new ArmPose(.30F * motion.windup - 1.35F * motion.strike,
                side * (.24F * motion.windup - .14F * motion.strike),
                side * (.10F * motion.windup + .10F * motion.strike),
                side * (.10F * motion.windup - .16F * motion.strike), motion.weight);
    }

    public static ArmPose glaive(float progress, boolean rightHanded) {
        Motion motion = glaiveMotion(progress);
        float side = rightHanded ? 1 : -1;
        // The held-item grip turns an upright model through 90 degrees relative to the arm.
        // Rolling the arm puts both the blade and its sweep in the horizontal plane.
        return new ArmPose(1.10F * motion.windup - 2.20F * motion.strike,
                0, side * Mth.HALF_PI,
                side * (-.32F * motion.windup + .64F * motion.strike), motion.weight);
    }

    public static void firstPersonDagger(PoseStack poses, float progress, float equip, boolean rightHanded) {
        Motion motion = daggerMotion(progress);
        rest(poses, equip * (1 - motion.weight), rightHanded);
        float side = rightHanded ? 1 : -1;
        poses.translate(side * (.04F * motion.windup - .34F * motion.strike) * motion.weight,
                (-.34F * motion.windup + .64F * motion.strike) * motion.weight,
                (.04F * motion.windup - .22F * motion.strike) * motion.weight);
        // Rotate around the authored model's grip so the blade leads the rising cut.
        poses.translate(side * .10F, -.185F, -.16F);
        poses.mulPose(Axis.XP.rotation((-1.90F * motion.windup + 1.15F * motion.strike) * motion.weight));
        poses.mulPose(Axis.ZP.rotation(side * (-.18F * motion.windup + .68F * motion.strike) * motion.weight));
        poses.translate(-side * .10F, .185F, .16F);
    }

    public static void firstPersonHammer(PoseStack poses, float progress, float equip, boolean rightHanded) {
        LivingAxePlayerPose.AxePose pose = LivingAxePlayerPose.swing(progress, rightHanded);
        float swing = Mth.clamp(progress, 0, 1);
        float raised = smooth(swing / .38F);
        float drop = smooth((swing - .42F) / .22F);
        float side = rightHanded ? 1 : -1;
        rest(poses, equip * (1 - pose.weight()), rightHanded);
        poses.translate(-side * .18F * pose.weight(),
                (.20F * raised - .40F * drop) * pose.weight(), -.14F * pose.weight());
        // First-person pitch keeps the bell above its handle during the overhead lift.
        poses.mulPose(Axis.XP.rotation((.35F * raised - 1.10F * drop) * pose.weight()));
        poses.mulPose(Axis.YP.rotation(pose.armYRot()));
        poses.mulPose(Axis.ZP.rotation(pose.armZRot() * .5F));
    }

    public static void firstPersonGlaive(PoseStack poses, float progress, float equip, boolean rightHanded) {
        Motion motion = glaiveMotion(progress);
        rest(poses, equip * (1 - motion.weight), rightHanded);
        float side = rightHanded ? 1 : -1;
        poses.translate(side * (.22F * motion.windup - 1.15F * motion.strike) * motion.weight,
                .36F * motion.weight, -.18F * motion.weight);
        poses.mulPose(Axis.YP.rotation(side * (-1.10F * motion.windup + 2.20F * motion.strike) * motion.weight));
        poses.mulPose(Axis.XP.rotation(-Mth.HALF_PI * motion.weight));
    }

    public static void applyThirdPerson(HumanoidModel<?> model, ArmPose pose, boolean rightHanded) {
        var arm = rightHanded ? model.rightArm : model.leftArm;
        arm.xRot = Mth.lerp(pose.weight, arm.xRot, pose.armXRot);
        arm.yRot = Mth.lerp(pose.weight, arm.yRot, pose.armYRot);
        arm.zRot = Mth.lerp(pose.weight, arm.zRot, pose.armZRot);
        model.body.yRot = Mth.lerp(pose.weight, model.body.yRot, pose.bodyYRot);
        model.rightArm.x = -Mth.cos(model.body.yRot) * 5.0F;
        model.rightArm.z = Mth.sin(model.body.yRot) * 5.0F;
        model.leftArm.x = Mth.cos(model.body.yRot) * 5.0F;
        model.leftArm.z = -Mth.sin(model.body.yRot) * 5.0F;
    }

    private static Motion daggerMotion(float progress) {
        float swing = Mth.clamp(progress, 0, 1);
        return new Motion(smooth(swing / .24F), smooth((swing - .22F) / .28F),
                smooth(swing / .12F) * (1 - smooth((swing - .66F) / .34F)));
    }

    private static Motion glaiveMotion(float progress) {
        float swing = Mth.clamp(progress, 0, 1);
        return new Motion(smooth(swing / .22F), smooth((swing - .24F) / .48F),
                smooth(swing / .20F) * (1 - smooth((swing - .76F) / .24F)));
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static void rest(PoseStack poses, float equip, boolean rightHanded) {
        poses.translate(rightHanded ? .56F : -.56F, -.52F - equip * .6F, -.72F);
    }

    public record ArmPose(float armXRot, float armYRot, float armZRot, float bodyYRot, float weight) { }
    private record Motion(float windup, float strike, float weight) { }
}
