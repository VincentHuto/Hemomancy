package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.mob.monster.ExcoriatedEntity;
import com.vincenthuto.hemomancy.common.init.FluidInit;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class ExcoriatedModel extends HierarchicalModel<ExcoriatedEntity> {
    private static final float MODEL_SCALE = 2.125F;
    private static final float HARPOON_AIM_YAW = (float) Math.atan2(7.25, 8.25);
    private static final Vector3f TOP_TIP = bowPoint(7.25F, 29.5F, -13.5F);
    private static final Vector3f BOTTOM_TIP = bowPoint(9, 13, -13.75F);
    private static final Vector3f GRIP = bowPoint(6.5F, 21.25F, -11.75F);
    private static final Vector3f TACKLE = bodyPoint(2, 13, 6);
    private final ModelPart root = ExcoriatedMesh.createRoot();
    private final ModelPart body = root.getChild("horse_body");
    private final ModelPart horseHead = body.getChild("horse_head");
    private final ModelPart tail = body.getChild("tail");
    private final ModelPart vessels = body.getChild("vessels");
    private final ModelPart torso = body.getChild("human_torso");
    private final ModelPart head = torso.getChild("human_head");
    private final ModelPart leftArm = torso.getChild("left_arm");
    private final ModelPart leftForearm = leftArm.getChild("left_forearm");
    private final ModelPart bow = leftForearm.getChild("fused_bow");
    private final ModelPart upperLimb = bow.getChild("upper_limb");
    private final ModelPart lowerLimb = bow.getChild("lower_limb");
    private final ModelPart arrow = bow.getChild("harpoon");
    private final ModelPart string = bow.getChild("bowstring");
    private final ModelPart upperString = string.getChild("upper_string");
    private final ModelPart lowerString = string.getChild("lower_string");
    private final ModelPart rightArm = torso.getChild("right_arm");
    private final ModelPart rightForearm = rightArm.getChild("right_forearm");
    private final ModelPart rightHand = rightForearm.getChild("right_hand");
    private final ModelPart returnLine = body.getChild("return_line");
    private final ModelPart[] returnSegments = new ModelPart[5];
    private final Leg frontLeft = leg("front_left");
    private final Leg frontRight = leg("front_right");
    private final Leg hindLeft = leg("hind_left");
    private final Leg hindRight = leg("hind_right");
    private final Vector3f nockBind = position(arrow);
    private final Vector3f upperBind = position(rightForearm);
    private final Vector3f lowerBind = position(rightHand);

    public ExcoriatedModel() {
        for (int i = 0; i < returnSegments.length; i++) {
            returnSegments[i] = returnLine.getChild("line_" + i);
        }
    }

    @Override
    public ModelPart root() {
        return root;
    }

    public static ModelPart createRecallBarb() {
        ModelPart harpoon = ExcoriatedMesh.createHarpoon();
        harpoon.setPos(0, 0, 0);
        return harpoon;
    }

    @Override
    public void setupAnim(ExcoriatedEntity entity, float swing, float amount, float age, float yaw, float pitch) {
        boolean wading = entity.getFluidTypeHeight(FluidInit.PHLEGETHONTIC_ICHOR_TYPE.get()) > .1;
        float partial = Mth.clamp(age - entity.tickCount, 0, 1);
        pose(swing, amount, age, yaw, pitch, entity.attackState(), entity.attackTime() + partial,
                wading, entity.clotted(), entity.deathTime > 0 ? entity.deathTime + partial : 0);
    }

    void pose(float swing, float amount, float age, float yaw, float pitch, int state, float time,
              boolean wading, boolean clotted, float deathTime) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float ready = switch (state) {
            case ExcoriatedEntity.DRAW -> smooth(time / 8);
            case ExcoriatedEntity.FIRE -> 1 - smooth((time - 2) / 6);
            default -> 0;
        };
        float rear = switch (state) {
            case ExcoriatedEntity.REAR -> smooth(time / 20);
            case ExcoriatedEntity.BREATH -> 1 - smooth(time / 12);
            default -> 0;
        };
        float stride = amount * (clotted ? .22F : wading ? .37F : .56F) * (1 - rear);
        float cycle = swing * .65F;
        frontLeft.step(cycle, stride, wading, rear);
        frontRight.step(cycle + Mth.PI, stride, wading, rear);
        hindLeft.step(cycle + Mth.PI, stride, wading, 0);
        hindRight.step(cycle, stride, wading, 0);
        body.y += Mth.sin(cycle * 2) * stride * .85F;
        body.xRot = -.34F * rear;
        body.y -= rear * 3;
        horseHead.xRot = Mth.sin(age * .075F) * .045F + rear * .18F;
        tail.yRot = Mth.sin(age * .065F + cycle * .35F) * (.12F + stride * .22F);
        torso.zRot = Mth.sin(cycle) * stride * .08F;
        torso.yRot = yaw * Mth.DEG_TO_RAD * Mth.lerp(ready, .35F, 1F) + HARPOON_AIM_YAW * ready;
        torso.xRot = pitch * Mth.DEG_TO_RAD * ready - rear * .13F;
        head.yRot = yaw * Mth.DEG_TO_RAD * .65F * (1 - ready) - HARPOON_AIM_YAW * ready;
        head.xRot = pitch * Mth.DEG_TO_RAD * (1 - ready) - rear * .18F;
        if (clotted) {
            body.y += 1.2F;
            torso.xRot += .13F;
            head.xRot += .22F;
        }
        float collapse = smooth(deathTime / 12);
        torso.xRot += collapse * .28F;
        head.xRot += collapse * .62F;
        animateArms(ready, state, time, age, cycle, stride);
    }

    private void animateArms(float ready, int state, float time, float age, float cycle, float stride) {
        float sway = Mth.sin(age * .05F) * .035F + Mth.sin(cycle) * stride * .12F;
        Quaternionf leftRest = aiming(position(leftForearm), new Vector3f(.44F, 1, .16F + sway));
        Quaternionf leftForeRest = aiming(position(bow),
                new Vector3f(.18F, .76F, .58F + sway).rotate(new Quaternionf(leftRest).conjugate()));
        blend(leftArm, leftRest, new Quaternionf(), ready);
        blend(leftForearm, leftForeRest, new Quaternionf(), ready);

        Quaternionf lowBow = new Quaternionf(leftRest).mul(leftForeRest).conjugate()
                .mul(new Quaternionf().rotationX(1.25F).rotateZ(-.12F));
        float recoil = state == ExcoriatedEntity.FIRE ? Mth.sin(Mth.PI * Mth.clamp(time / 6, 0, 1)) * .09F : 0;
        blend(bow, lowBow, new Quaternionf().rotationX(-recoil), ready);

        float draw = switch (state) {
            case ExcoriatedEntity.DRAW -> smooth((time - 4) / 16);
            case ExcoriatedEntity.FIRE -> 1 - smooth(time / 2.25F);
            default -> 0;
        };
        Vector3f relaxed = new Vector3f(TOP_TIP).lerp(BOTTOM_TIP, .5F);
        Vector3f loaded = new Vector3f(nockBind).lerp(relaxed, .3F * (1 - draw));
        Vector3f nock = relaxed.lerp(loaded, ready);
        upperLimb.xRot = -.085F * draw * ready;
        lowerLimb.xRot = .10F * draw * ready;
        segment(upperString, transform(TOP_TIP, upperLimb), nock);
        segment(lowerString, transform(BOTTOM_TIP, lowerLimb), nock);
        arrow.setPos(nock.x, nock.y, nock.z);
        arrow.visible = state == ExcoriatedEntity.DRAW && ready >= .72F;

        float handWeight = smooth(ready);
        Vector3f handTarget = transform(nock, leftArm, leftForearm, bow);
        Quaternionf rightRest = aiming(upperBind, new Vector3f(-.42F, 1, .14F - sway));
        Quaternionf rightForeRest = aiming(lowerBind,
                new Vector3f(-.15F, 1, .14F - sway).rotate(new Quaternionf(rightRest).conjugate()));
        poseDrawingHand(handTarget.sub(position(rightArm)), rightRest, rightForeRest, handWeight);

        Vector3f grip = transform(GRIP, torso, leftArm, leftForearm, bow);
        Vector3f lead = transform(nock, torso, leftArm, leftForearm, bow);
        animateReturnLine(grip.lerp(lead, handWeight * draw));
    }

    private void poseDrawingHand(Vector3f target, Quaternionf armRest, Quaternionf forearmRest, float ready) {
        float upper = upperBind.length();
        float lower = lowerBind.length();
        float distance = Mth.clamp(target.length(), Math.abs(upper - lower) + .001F, upper + lower - .001F);
        Vector3f direction = target.normalize();
        Vector3f bend = new Vector3f(upperBind).fma(-upperBind.dot(direction), direction).normalize();
        float along = (upper * upper - lower * lower + distance * distance) / (2 * distance);
        Vector3f elbow = new Vector3f(direction).mul(along)
                .fma(Mth.sqrt(Math.max(0, upper * upper - along * along)), bend);
        Quaternionf upperPose = aiming(upperBind, elbow);
        Vector3f lowerDirection = direction.mul(distance).sub(elbow)
                .rotate(new Quaternionf(upperPose).conjugate());
        blend(rightArm, armRest, upperPose, ready);
        blend(rightForearm, forearmRest, aiming(lowerBind, lowerDirection), ready);
    }

    private void animateReturnLine(Vector3f start) {
        Vector3f lead = new Vector3f(start).lerp(bodyPoint(3, 18.75F, -5.5F), .55F);
        lead.x = Math.max(lead.x, 4 * MODEL_SCALE);
        Vector3f flank = bodyPoint(3.5F, 16.25F, -1.5F);
        Vector3f hip = bodyPoint(2.75F, 14.5F, 2.5F);
        Vector3f entry = bodyPoint(2.25F, 13.5F, 4.75F);
        segment(returnSegments[0], start, lead);
        segment(returnSegments[1], lead, flank);
        segment(returnSegments[2], flank, hip);
        segment(returnSegments[3], hip, entry);
        segment(returnSegments[4], entry, TACKLE);
    }

    private Leg leg(String name) {
        ModelPart upper = body.getChild(name);
        ModelPart lower = upper.getChild(name + "_lower");
        return new Leg(upper, lower, lower.getChild(name + "_hoof"));
    }

    private record Leg(ModelPart upper, ModelPart lower, ModelPart hoof) {
        void step(float phase, float stride, boolean wading, float rear) {
            float swing = Mth.cos(phase) * stride;
            float lift = Math.max(0, Mth.sin(phase)) * stride;
            upper.xRot = swing - rear * .68F;
            lower.xRot = -lift * (wading ? 1.15F : .64F) + rear * .82F;
            hoof.xRot = -lower.xRot * .37F;
        }
    }

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    private static Vector3f bowPoint(float x, float y, float z) {
        return new Vector3f(x - 6.75F, 21.5F - y, z + 12).mul(MODEL_SCALE);
    }

    private static Vector3f bodyPoint(float x, float y, float z) {
        return new Vector3f(x, 13 - y, z - 1).mul(MODEL_SCALE);
    }

    private static Vector3f position(ModelPart part) {
        return new Vector3f(part.x, part.y, part.z);
    }

    private static Vector3f transform(Vector3f point, ModelPart... chain) {
        PoseStack pose = new PoseStack();
        for (ModelPart part : chain) part.translateAndRotate(pose);
        return pose.last().pose().transformPosition(new Vector3f(point).div(16)).mul(16);
    }

    private static Quaternionf aiming(Vector3f bind, Vector3f target) {
        return new Quaternionf().rotationTo(bind, target);
    }

    private static void segment(ModelPart part, Vector3f from, Vector3f to) {
        Vector3f center = new Vector3f(from).lerp(to, .5F);
        Vector3f direction = new Vector3f(to).sub(from);
        part.setPos(center.x, center.y, center.z);
        part.yScale = direction.length();
        orient(part, aiming(new Vector3f(0, 1, 0), direction));
    }

    private static void blend(ModelPart part, Quaternionf rest, Quaternionf target, float readiness) {
        orient(part, rest.slerp(target, readiness));
    }

    private static void orient(ModelPart part, Quaternionf rotation) {
        // The X denominator in JOML 1.10.5's ZYX conversion has the opposite sign.
        float x = rotation.x, y = rotation.y, z = rotation.z, w = rotation.w;
        part.setRotation((float) Math.atan2(2 * (w * x + y * z), 1 - 2 * (x * x + y * y)),
                (float) Math.asin(Mth.clamp(2 * (w * y - z * x), -1, 1)),
                (float) Math.atan2(2 * (w * z + x * y), 1 - 2 * (y * y + z * z)));
    }

    public void renderVessels(PoseStack pose, VertexConsumer buffer, int light, int overlay, int color) {
        pose.pushPose();
        root.translateAndRotate(pose);
        body.translateAndRotate(pose);
        vessels.render(pose, buffer, light, overlay, color);
        pose.popPose();
    }

    public void translateToBow(PoseStack pose) {
        root.translateAndRotate(pose);
        body.translateAndRotate(pose);
        torso.translateAndRotate(pose);
        leftArm.translateAndRotate(pose);
        leftForearm.translateAndRotate(pose);
        bow.translateAndRotate(pose);
        pose.translate(GRIP.x / 16, GRIP.y / 16, GRIP.z / 16);
    }
}

