package com.vincenthuto.hemomancy.client.player;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Anchors are centered item-model coordinates, shared by the held models and both hands. */
public final class HematicMicroscopePose {
    public static final float INSTRUMENT_SCALE = .7F;
    public static final float VIAL_SCALE = .38F;
    private HematicMicroscopePose() { }

    public static float weight(float tick) { return smooth(tick / 6); }
    public static float overlayAlpha(float tick) { return smooth((tick - 12) / 4); }
    public static Vector3f seatedHead(boolean right) { return new Vector3f(right ? -.075F : .075F, .125F, 0); }

    public static void firstPersonInstrument(PoseStack poses, float tick, boolean right) {
        float raise = smooth(tick / 6), viewing = overlayAlpha(tick), side = right ? 1 : -1;
        poses.translate(side * Mth.lerp(viewing, .25F, 0),
                Mth.lerp(viewing, -.48F + .32F * raise, 0), Mth.lerp(viewing, -.7F, -.2F));
        poses.scale(INSTRUMENT_SCALE, INSTRUMENT_SCALE, INSTRUMENT_SCALE);
        poses.translate(0, -.125F, -.325F);
    }

    public static void thirdPersonInstrument(PoseStack poses, HumanoidModel<?> model, float tick, boolean right) {
        model.head.translateAndRotate(poses);
        float raise = smooth(tick / 6), viewing = overlayAlpha(tick), side = right ? -1 : 1;
        poses.translate(side * Mth.lerp(viewing, .18F, .125F),
                Mth.lerp(viewing, .35F - .25F * raise, -.25F), Mth.lerp(viewing, -.65F, -.25F));
        poses.mulPose(Axis.ZP.rotationDegrees(180));
        poses.scale(INSTRUMENT_SCALE, INSTRUMENT_SCALE, INSTRUMENT_SCALE);
        poses.translate(0, -.125F, -.325F);
    }

    public static void vial(PoseStack poses, PoseStack instrument, float tick, boolean right) {
        poses.mulPose(instrument.last().pose());
        var head = seatedHead(right);
        float inserted = smooth((tick - 6) / 6);
        head.x += (right ? -1 : 1) * .42F * (1 - inserted);
        head.y -= .15F * (1 - smooth(tick / 6));
        poses.translate(head.x, head.y, head.z);
        poses.mulPose(new Quaternionf().rotationTo(new Vector3f(1, 1, 0).normalize(),
                new Vector3f(right ? 1 : -1, 0, 0)));
        poses.scale(VIAL_SCALE, VIAL_SCALE, VIAL_SCALE);
        poses.translate(-.25F, -.25F, 0);
    }

    public static void apply(HumanoidModel<?> model, float tick, boolean right) {
        var instrument = new PoseStack();
        thirdPersonInstrument(instrument, model, tick, right);
        var vial = new PoseStack();
        vial(vial, instrument, tick, right);
        var mainGrip = instrument.last().pose().transformPosition(new Vector3f(0, -.21875F, -.0625F));
        var sampleGrip = vial.last().pose().transformPosition(new Vector3f(-.1875F, -.1875F, 0));
        aimArm(right ? model.rightArm : model.leftArm, mainGrip, weight(tick));
        aimArm(right ? model.leftArm : model.rightArm, sampleGrip, weight(tick));
        if (model instanceof PlayerModel<?> player) {
            player.rightSleeve.copyFrom(model.rightArm);
            player.leftSleeve.copyFrom(model.leftArm);
        }
    }

    private static void aimArm(ModelPart arm, Vector3f grip, float weight) {
        Vector3f shoulder = new Vector3f(arm.x, arm.y, arm.z).div(16);
        Vector3f direction = new Vector3f(grip).sub(shoulder).normalize();
        Vector3f rotation = new Quaternionf().rotationTo(new Vector3f(0, 1, 0), direction)
                .getEulerAnglesZYX(new Vector3f());
        // Keep the block arm's length fixed; move its pivot just enough for the wrist to meet the grip.
        Vector3f pivot = new Vector3f(grip).fma(-.625F, direction).mul(16);
        arm.x = Mth.lerp(weight, arm.x, pivot.x);
        arm.y = Mth.lerp(weight, arm.y, pivot.y);
        arm.z = Mth.lerp(weight, arm.z, pivot.z);
        arm.xRot = lerpRadians(weight, arm.xRot, rotation.x);
        arm.yRot = lerpRadians(weight, arm.yRot, rotation.y);
        arm.zRot = lerpRadians(weight, arm.zRot, rotation.z);
    }

    private static float lerpRadians(float weight, float from, float to) {
        return from + weight * (float) Math.atan2(Math.sin(to - from), Math.cos(to - from));
    }

    private static float smooth(float t) {
        t = Mth.clamp(t, 0, 1);
        return t * t * (3 - 2 * t);
    }
}
