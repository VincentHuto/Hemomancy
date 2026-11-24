package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class MarrowSpitterModel extends EntityModel<MarrowSpitterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("marrow_spitter"), "main");
    private static final float CASCADE_SPEED = .13F;
    private final ModelPart root, body, hood, armBank, props;
    private final ModelPart[] mounts = new ModelPart[6], arms = new ModelPart[6],
            forearms = new ModelPart[6], hands = new ModelPart[6];
    private final ModelPart[][] fingers = new ModelPart[6][3];
    private final ModelPart[] knives = new ModelPart[3];
    private final ModelPart[] hems;

    public MarrowSpitterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        body = root.getChild("body");
        hood = body.getChild("hood");
        armBank = body.getChild("arms");
        props = root.getChild("props");
        for (int i = 0; i < arms.length; i++) {
            mounts[i] = armBank.getChild("mount_" + i);
            arms[i] = mounts[i].getChild("arm_" + i);
            forearms[i] = arms[i].getChild("forearm_" + i);
            hands[i] = forearms[i].getChild("hand_" + i);
            fingers[i][0] = hands[i].getChild("index_" + i);
            fingers[i][1] = hands[i].getChild("middle_" + i);
            fingers[i][2] = hands[i].getChild("ring_" + i);
        }
        for (int i = 0; i < knives.length; i++) knives[i] = props.getChild("knife_" + i);
        ModelPart skirt = body.getChild("skirt");
        hems = new ModelPart[]{skirt.getChild("front_left"), skirt.getChild("front_right"),
                skirt.getChild("rear_left"), skirt.getChild("rear_right"),
                skirt.getChild("side_left"), skirt.getChild("side_right")};
    }

    public static LayerDefinition createBodyLayer() {
        return MarrowSpitterMesh.createBodyLayer();
    }

    @Override
    public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        setupPose(limbSwingAmount, age, yaw, pitch);
    }

    public void setupPose(float movement, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        // A swing starts on each real dagger release. The arm snaps forward, then settles back into the act.
        float flick = Mth.sin(Mth.sqrt(Mth.clamp(attackTime, 0, 1)) * Mth.PI);
        root.y += Mth.sin(age * .07F) * .45F;
        body.zRot += Mth.sin(age * .05F) * .065F - flick * .065F;
        body.xRot += Mth.clamp(movement, 0, 1) * .13F + flick * .08F;
        hood.yRot += Mth.clamp(yaw, -40, 40) * Mth.DEG_TO_RAD * .6F;
        hood.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD * .4F;
        for (int i = 0; i < hems.length; i++) {
            float wave = age * .085F - i * .8F;
            hems[i].xRot += Mth.sin(wave) * .09F - flick * .05F;
            hems[i].zRot += Mth.cos(wave) * .046F;
        }
        for (int i = 0; i < knives.length; i++) {
            float phase = age * CASCADE_SPEED + i * Mth.TWO_PI / knives.length;
            float transfer = (1 - Mth.cos(phase)) * .5F;
            poseHand(i * 2, phase, 1 - transfer, 0);
            poseHand(i * 2 + 1, phase, transfer, i == 1 ? flick : 0);
            poseKnife(i, phase, transfer);
        }
    }

    private void poseHand(int index, float phase, float grip, float flick) {
        float side = index % 2 == 0 ? -1 : 1;
        float lift = Mth.sin(phase) * side;
        arms[index].zRot -= side * Mth.sin(phase) * .13F;
        arms[index].xRot += lift * .12F - flick * .37F;
        forearms[index].xRot -= lift * .21F + flick * .55F;
        hands[index].xRot += lift * .14F - flick * .28F;
        hands[index].zRot += Mth.cos(phase) * .09F;
        float close = Mth.clamp((grip - .76F) / .24F, 0, 1) * (1 - flick);
        for (ModelPart finger : fingers[index]) {
            finger.xRot = Mth.lerp(close, Math.max(finger.xRot, -.25F), -.82F);
        }
    }

    private void poseKnife(int index, float phase, float transfer) {
        PoseStack.Pose left = gripPose(index * 2);
        PoseStack.Pose right = gripPose(index * 2 + 1);
        Vector3f position = left.pose().getTranslation(new Vector3f())
                .lerp(right.pose().getTranslation(new Vector3f()), transfer).mul(16);
        float lift = Mth.sin(phase);
        lift *= lift;

        ModelPart knife = knives[index];
        knife.x = position.x - props.x;
        knife.y = position.y - props.y - lift * (8 + index * 1.5F);
        // The lower pairs pass in front of the other arms and the doublet.
        knife.z = position.z - props.z - lift * (6 + index * 2);
        Quaternionf rotation = left.pose().getNormalizedRotation(new Quaternionf())
                .slerp(right.pose().getNormalizedRotation(new Quaternionf()), transfer)
                .rotateZ(phase * 2);
        Vector3f euler = rotation.getEulerAnglesZYX(new Vector3f());
        knife.setRotation(euler.x, euler.y, euler.z);
    }

    private PoseStack.Pose gripPose(int index) {
        PoseStack pose = new PoseStack();
        body.translateAndRotate(pose);
        armBank.translateAndRotate(pose);
        mounts[index].translateAndRotate(pose);
        arms[index].translateAndRotate(pose);
        forearms[index].translateAndRotate(pose);
        hands[index].translateAndRotate(pose);
        pose.translate(0, 2F / 16, -1.25F / 16);
        // Match the tilt of the dagger authored in the right middle hand.
        pose.mulPose(new Quaternionf().rotationZYX(68 * Mth.DEG_TO_RAD, 0, -9 * Mth.DEG_TO_RAD));
        return pose.last();
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
