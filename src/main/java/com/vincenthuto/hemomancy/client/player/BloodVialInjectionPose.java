package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Model-space contact geometry shared by the hand, needle and impact effects. */
public final class BloodVialInjectionPose {
    public static final float VIAL_SCALE = .4F;
    private static final float ARM_LENGTH = 10F / 16F;
    private static final float NEEDLE_LENGTH = (float) Math.sqrt(2) * .5625F * VIAL_SCALE;

    private BloodVialInjectionPose() { }

    public static boolean rightArm(InteractionHand hand, HumanoidArm mainArm) {
        return (hand == InteractionHand.MAIN_HAND) == (mainArm == HumanoidArm.RIGHT);
    }

    public static Motion motion(float tick) {
        float raise = smooth(tick / 8);
        float cock = smooth((tick - 8) / 4);
        float strike = smooth((tick - 12) / 4);
        float recover = smooth((tick - 18) / 6);
        return new Motion(-1.15F * raise - .65F * cock + 1.25F * strike,
                strike, smooth(tick / 3) * (1 - recover), raise, cock, recover);
    }

    public static void apply(HumanoidModel<?> model, float tick, boolean right) {
        Motion m = motion(tick);
        float side = right ? -1 : 1;
        model.body.xRot = Mth.lerp(m.weight, model.body.xRot, .12F + .12F * m.strike);
        model.body.yRot = Mth.lerp(m.weight, model.body.yRot, side * (.12F - .20F * m.strike));
        model.head.xRot += .12F * m.weight;
        var leg = right ? model.rightLeg : model.leftLeg;
        leg.xRot = Mth.lerp(m.weight, leg.xRot, -.16F);
        leg.zRot = Mth.lerp(m.weight, leg.zRot, side * .06F);
        var otherLeg = right ? model.leftLeg : model.rightLeg;
        otherLeg.xRot = Mth.lerp(m.weight * .65F, otherLeg.xRot, .08F);
        var arm = right ? model.rightArm : model.leftArm;
        var freeArm = right ? model.leftArm : model.rightArm;
        model.rightArm.x = -Mth.cos(model.body.yRot) * 5;
        model.rightArm.z = Mth.sin(model.body.yRot) * 5;
        model.leftArm.x = Mth.cos(model.body.yRot) * 5;
        model.leftArm.z = -Mth.sin(model.body.yRot) * 5;

        Vector3f shoulder = new Vector3f(arm.x, arm.y, arm.z).div(16);
        Vector3f wrist = contactWrist(shoulder, thighContact(model, right));
        Vector3f reach = wrist.sub(shoulder);
        float contactPitch = (float) Math.asin(Mth.clamp(reach.z / ARM_LENGTH, -1, 1));
        float contactRoll = (float) Math.atan2(-reach.x, reach.y);
        float pitch = Mth.lerp(m.strike, m.armPitch, contactPitch);
        float roll = Mth.lerp(m.strike, -side * (.25F + .25F * m.cock), contactRoll);
        arm.xRot = Mth.lerp(m.weight, arm.xRot, pitch);
        arm.yRot = Mth.lerp(m.weight, arm.yRot, 0);
        arm.zRot = Mth.lerp(m.weight, arm.zRot, roll);
        freeArm.xRot = Mth.lerp(m.weight * .65F, freeArm.xRot, -.12F);
        freeArm.zRot = Mth.lerp(m.weight, freeArm.zRot, side * .15F);
        model.hat.copyFrom(model.head);
        if (model instanceof PlayerModel<?> player) {
            player.rightSleeve.copyFrom(model.rightArm);
            player.leftSleeve.copyFrom(model.leftArm);
            player.rightPants.copyFrom(model.rightLeg);
            player.leftPants.copyFrom(model.leftLeg);
            player.jacket.copyFrom(model.body);
        }
    }

    // Intersection of the shoulder's reach sphere and the needle's contact sphere.
    // Choosing the forward intersection keeps the wrist outside the thigh, with the point leading inward.
    private static Vector3f contactWrist(Vector3f shoulder, Vector3f contact) {
        Vector3f direction = new Vector3f(contact).sub(shoulder);
        float distance = direction.length();
        direction.div(distance);
        float along = (ARM_LENGTH * ARM_LENGTH - NEEDLE_LENGTH * NEEDLE_LENGTH + distance * distance)
                / (2 * distance);
        along = Mth.clamp(along, -ARM_LENGTH, ARM_LENGTH);
        Vector3f forward = new Vector3f(0, 0, -1);
        forward.fma(-forward.dot(direction), direction).normalize();
        float radius = (float) Math.sqrt(Math.max(0, ARM_LENGTH * ARM_LENGTH - along * along));
        return new Vector3f(shoulder).fma(along, direction).fma(radius, forward);
    }

    public static Vector3f thighContact(HumanoidModel<?> model, boolean right) {
        var poses = new PoseStack();
        (right ? model.rightLeg : model.leftLeg).translateAndRotate(poses);
        return poses.last().pose().transformPosition(new Vector3f(0, 2.5F / 16, -2.05F / 16));
    }

    public static void vialTransform(PoseStack poses, HumanoidModel<?> model, float tick, boolean right) {
        var hand = new PoseStack();
        (right ? model.rightArm : model.leftArm).translateAndRotate(hand);
        Vector3f grip = hand.last().pose().transformPosition(new Vector3f(0, ARM_LENGTH, 0));
        Vector3f direction = thighContact(model, right).sub(grip).normalize();
        orientVial(poses, grip, direction);
    }

    public static void orientVial(PoseStack poses, Vector3f grip, Vector3f direction) {
        poses.translate(grip.x, grip.y, grip.z);
        poses.mulPose(new Quaternionf().rotationTo(new Vector3f(-1, -1, 0).normalize(), direction));
        poses.scale(VIAL_SCALE, VIAL_SCALE, VIAL_SCALE);
        poses.translate(-.125F, -.125F, 0);
    }

    public static void blendTransform(PoseStack target, PoseStack ordinary, PoseStack injection, float weight) {
        var from = ordinary.last().pose();
        var to = injection.last().pose();
        Vector3f translation = from.getTranslation(new Vector3f()).lerp(to.getTranslation(new Vector3f()), weight);
        Vector3f scale = from.getScale(new Vector3f()).lerp(to.getScale(new Vector3f()), weight);
        Quaternionf rotation = from.getUnnormalizedRotation(new Quaternionf())
                .slerp(to.getUnnormalizedRotation(new Quaternionf()), weight);
        target.translate(translation.x, translation.y, translation.z);
        target.mulPose(rotation);
        target.scale(scale.x, scale.y, scale.z);
    }

    public static Vector3f firstPersonGrip(float tick, boolean right) {
        Motion m = motion(tick);
        float side = right ? 1 : -1;
        return new Vector3f(side * (.48F - .10F * m.raise + .08F * m.strike),
                -.48F + .32F * m.raise + .10F * m.cock - .64F * m.strike + .22F * m.recover,
                -.68F - .08F * m.raise + .15F * m.strike);
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }

    public record Motion(float armPitch, float strike, float weight, float raise, float cock, float recover) { }
}
