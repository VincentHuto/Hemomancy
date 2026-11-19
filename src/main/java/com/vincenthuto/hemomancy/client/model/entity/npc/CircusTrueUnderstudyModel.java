package com.vincenthuto.hemomancy.client.model.entity.npc;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.UnderstudyChoreography;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_understudy.py; regenerate the model, atlas and BBModel together.
/**
 * The Retired Understudy's true self: standing straight in the prompt corner of their stage, the mask down, the
 * mantle hanging, the prompter's staff planted at their side with its cue light lit, their head following the fighter.
 * Each intervention is a gesture ({@link CircusFacultyEntity#gesture}) timed from its wind-up: the staff raised and the
 * light aimed (the cue, then the lock and the thrust at the burn), both hands hauling the fly rope down for the curtain,
 * a hand conducting through the reprise, and the staff's heel knocked three times on the boards for the trapdoors. Their
 * double ({@link #renderDouble}) is this same rig posed on its own, drawn life-sized in red light at the gesture's
 * synced point: stepping onto each reprise mark to strike it, and rising through the trapdoor under the fighter.
 */
public final class CircusTrueUnderstudyModel extends EntityModel<CircusFacultyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_understudy"), "main");
    private final ModelPart root, hips, torso, neck, head, mantle, hairTail, staff;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2],
            feet = new ModelPart[2], drapes = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    private final List<ModelPart> parts = new ArrayList<>();
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;
    /** This frame's double: how strongly it shows, where its feet stand (model pixels), how high it has risen, its pose. */
    private float doubleWeight, doubleRise;
    private Vec3 doubleAt = Vec3.ZERO;
    private int doubleGesture = -1;
    private float doubleSince;

    public CircusTrueUnderstudyModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        hips = root.getChild("hips");
        torso = hips.getChild("torso");
        mantle = torso.getChild("mantle");
        neck = torso.getChild("neck");
        head = neck.getChild("head");
        hairTail = head.getChild("hair_knot").getChild("hair_tail");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            drapes[i] = torso.getChild(side + "_drape");
            shoulders[i] = torso.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = hips.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
        staff = hands[1].getChild("staff");
        root.getAllParts().forEach(parts::add);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition hips = root.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -3.0F, -3.5F, 12.0F, 5.0F, 7.0F), PartPose.offset(0.0F, -26.0F, 0.0F));
        PartDefinition torso = hips.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(39, 0).addBox(-6.0F, -10.0F, -3.5F, 12.0F, 9.0F, 7.0F).texOffs(78, 0).addBox(-7.0F, -20.0F, -4.0F, 14.0F, 10.0F, 8.0F), PartPose.offset(0.0F, -2.0F, 0.0F));
        PartDefinition collar = torso.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(123, 0).addBox(-8.5F, -1.5F, -4.75F, 17.0F, 3.0F, 9.5F), PartPose.offset(0.0F, -19.0F, 0.0F));
        PartDefinition mantle = torso.addOrReplaceChild("mantle", CubeListBuilder.create().texOffs(178, 0).addBox(-8.0F, 0.0F, 0.0F, 16.0F, 40.0F, 1.5F), PartPose.offsetAndRotation(0.0F, -19.0F, 4.25F, 0.10471975511965978F, 0.0F, 0.0F));
        PartDefinition right_drape = torso.addOrReplaceChild("right_drape", CubeListBuilder.create().texOffs(215, 0).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 22.0F, 5.0F), PartPose.offsetAndRotation(-7.5F, -18.5F, 1.0F, 0.0F, 0.0F, -0.06981317007977318F));
        PartDefinition left_drape = torso.addOrReplaceChild("left_drape", CubeListBuilder.create().texOffs(230, 0).addBox(-1.0F, 0.0F, -2.5F, 2.0F, 22.0F, 5.0F), PartPose.offsetAndRotation(7.5F, -18.5F, 1.0F, 0.0F, 0.0F, 0.06981317007977318F));
        PartDefinition neck = torso.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 43).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 4.0F), PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(17, 43).addBox(-5.0F, -11.0F, -5.0F, 10.0F, 11.0F, 10.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create().texOffs(58, 43).addBox(-4.5F, -2.5F, -1.0F, 9.0F, 5.0F, 1.0F), PartPose.offset(0.0F, -6.0F, -5.0F));
        PartDefinition hair_knot = head.addOrReplaceChild("hair_knot", CubeListBuilder.create().texOffs(79, 43).addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 2.0F), PartPose.offset(0.0F, -6.0F, 5.0F));
        PartDefinition hair_tail = hair_knot.addOrReplaceChild("hair_tail", CubeListBuilder.create().texOffs(92, 43).addBox(-1.5F, 0.0F, -0.75F, 3.0F, 12.0F, 1.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.5F, 0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition right_shoulder = torso.addOrReplaceChild("right_shoulder", CubeListBuilder.create().texOffs(103, 43).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 4.0F, 5.0F), PartPose.offset(-8.0F, -18.0F, 0.0F));
        PartDefinition right_upper = right_shoulder.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(124, 43).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(141, 43).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 8.0F, 3.5F).texOffs(158, 43).addBox(-2.25F, 6.5F, -2.25F, 4.5F, 1.5F, 4.5F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(179, 43).addBox(-1.25F, 0.0F, -1.75F, 2.5F, 3.0F, 3.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(194, 43).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 1.75F, 0.75F), PartPose.offset(0.0F, 3.0F, -1.25F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(201, 43).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(206, 43).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 2.25F, 0.75F), PartPose.offset(0.0F, 3.0F, -0.5F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(213, 43).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.25F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(218, 43).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.25F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(225, 43).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(230, 43).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 1.5F, 0.75F), PartPose.offset(0.0F, 3.0F, 1.0F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(237, 43).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(242, 43).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(1.0F, 1.0F, -1.75F, -0.4363323129985824F, 0.0F, 0.4363323129985824F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(247, 43).addBox(-0.25F, 0.0F, -0.25F, 0.75F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition right_thigh = hips.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(0, 65).addBox(-2.25F, 0.0F, -2.25F, 4.5F, 12.0F, 4.5F), PartPose.offset(-3.0F, 1.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(21, 65).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(38, 65).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        PartDefinition left_shoulder = torso.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(59, 65).addBox(-2.5F, -2.0F, -2.5F, 5.0F, 4.0F, 5.0F), PartPose.offset(8.0F, -18.0F, 0.0F));
        PartDefinition left_upper = left_shoulder.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(80, 65).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(97, 65).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 8.0F, 3.5F).texOffs(114, 65).addBox(-2.25F, 6.5F, -2.25F, 4.5F, 1.5F, 4.5F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(135, 65).addBox(-1.25F, 0.0F, -1.75F, 2.5F, 3.0F, 3.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(150, 65).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 1.75F, 0.75F), PartPose.offset(0.0F, 3.0F, -1.25F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(157, 65).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(162, 65).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 2.25F, 0.75F), PartPose.offset(0.0F, 3.0F, -0.5F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(169, 65).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.25F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(174, 65).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 2.0F, 0.75F), PartPose.offset(0.0F, 3.0F, 0.25F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(181, 65).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(186, 65).addBox(-0.75F, 0.0F, -0.25F, 1.5F, 1.5F, 0.75F), PartPose.offset(0.0F, 3.0F, 1.0F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(193, 65).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.75F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(198, 65).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(-1.0F, 1.0F, -1.75F, -0.4363323129985824F, 0.0F, -0.4363323129985824F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(203, 65).addBox(-0.25F, 0.0F, -0.25F, 0.75F, 1.5F, 0.75F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition left_thigh = hips.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(208, 65).addBox(-2.25F, 0.0F, -2.25F, 4.5F, 12.0F, 4.5F), PartPose.offset(3.0F, 1.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(229, 65).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 11.0F, 4.0F), PartPose.offset(0.0F, 12.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 83).addBox(-2.0F, 0.0F, -4.0F, 4.0F, 2.0F, 6.0F), PartPose.offset(0.0F, 11.0F, 0.0F));
        PartDefinition staff = left_hand.addOrReplaceChild("staff", CubeListBuilder.create().texOffs(21, 83).addBox(-0.75F, -34.0F, -0.75F, 1.5F, 58.0F, 1.5F).texOffs(30, 83).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 1.5F, 2.0F).texOffs(39, 83).addBox(-1.0F, 6.0F, -1.0F, 2.0F, 1.5F, 2.0F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition lamp = staff.addOrReplaceChild("lamp", CubeListBuilder.create().texOffs(48, 83).addBox(-2.0F, -5.0F, -2.0F, 4.0F, 1.0F, 4.0F).texOffs(65, 83).addBox(-1.25F, -4.0F, -1.25F, 2.5F, 3.0F, 2.5F).texOffs(78, 83).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F).texOffs(95, 83).addBox(-0.5F, -6.5F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, -34.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusFacultyEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        int gesture = entity.gesture();
        float since = entity.gestureAge(partialTick);
        doubleWeight = 0.0F;
        if (gesture == UnderstudyChoreography.GESTURE_BEAT || gesture == UnderstudyChoreography.GESTURE_KNOCK) {
            doubleGesture = gesture;
            doubleSince = since;
            doubleAt = model(entity.gestureAt());
            if (gesture == UnderstudyChoreography.GESTURE_BEAT) {
                doubleWeight = HemoEasing.smoothstep(0.0F, 2.0F, since) * (1.0F - HemoEasing.smoothstep(6.0F, 10.0F, since));
                doubleRise = 1.0F;
            } else {
                float knock = UnderstudyChoreography.KNOCKS[2] + 2.0F;
                doubleWeight = HemoEasing.smoothstep(10.0F, 14.0F, since) * (1.0F - HemoEasing.smoothstep(knock + 4.0F, knock + 12.0F, since));
                doubleRise = HemoEasing.smoothstep(12.0F, knock, since);
            }
        }
        pose(age, limbSwing, limbSwingAmount, yaw, pitch, gesture, since);
    }

    private void reset() {
        for (ModelPart part : parts) {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        }
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float age, float limbSwing, float limbSwingAmount, float yaw, float pitch, int gesture, float since) {
        reset();
        float breath = Mth.sin(age * .06F);
        float walk = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
        float stride = limbSwing * .6662F;
        // Standing straight, chin up: no stoop in here.
        torso.xRot -= .03F + breath * .015F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI) * .45F * walk;
            thighs[i].xRot = swing;
            shins[i].xRot = Math.max(0.0F, -swing) * 1.2F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            shoulders[i].y -= breath * .25F;
            uppers[i].zRot = -sign * .1F;
            curl(i, .45F);
        }
        hips.y += Math.abs(Mth.sin(stride)) * -.6F * walk;
        // The right hand held a little out from the body, the fingers moving as if over lines; the staff planted.
        uppers[0].xRot = -.25F + Mth.sin(age * .05F) * .04F;
        fores[0].xRot = -.75F;
        hands[0].yRot = -.4F;
        curl(0, .35F + .2F * Mth.sin(age * .17F));
        uppers[1].xRot = -.3F;
        uppers[1].zRot = .12F;
        fores[1].xRot = -.25F;
        staff.xRot = .55F;
        curl(1, 1.2F);
        mantle.xRot += breath * .02F + walk * .15F;
        hairTail.xRot += breath * .02F + walk * .2F;
        for (int i = 0; i < 2; i++) drapes[i].xRot += walk * .08F;
        if (gesture >= 0) gesture(gesture, since);
    }

    private void curl(int i, float amount) {
        for (int f = 0; f < 4; f++) {
            fingers[i][f].xRot = amount * (1.0F + f * .08F);
            tips[i][f].xRot = amount * 1.2F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .5F;
    }

    /** The beats of a blow: a slow gather, a fast drop of a few ticks onto the impact, a hit-stop, a heavy recovery. */
    private static float drop(float since, float impact) {
        float t = Mth.clamp((since - (impact - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    private static float held(float since, float impact, float hold, float recover) {
        return 1.0F - HemoEasing.smoothstep(impact + hold, impact + hold + recover, since);
    }

    private void gesture(int gesture, float since) {
        switch (gesture) {
            case UnderstudyChoreography.GESTURE_CUE -> {
                // The staff lifted high and the cue light swung out over the ring, hunting with the light.
                float raise = HemoEasing.smoothstep(0.0F, 8.0F, since);
                staffRaised(raise, Mth.sin(since * .35F) * .12F * raise);
                torso.xRot += .08F * raise;
            }
            case UnderstudyChoreography.GESTURE_LOCK -> {
                // Held dead still on the settled light, trembling, then driven down at it for the burn.
                float impact = UnderstudyChoreography.LOCK;
                float tension = HemoEasing.smoothstep(impact - 6.0F, impact - 1.0F, since) * (1.0F - drop(since, impact));
                float down = drop(since, impact) * held(since, impact, 6.0F, 16.0F);
                float raise = held(since, impact, 6.0F, 16.0F);
                staffRaised(raise, Mth.sin(since * 3.1F) * .04F * tension - .5F * down);
                torso.xRot += .08F * raise + .22F * down;
                head.xRot += .12F * down;
                // The free hand thrown out after it.
                uppers[0].xRot = Mth.lerp(down, uppers[0].xRot, -1.6F);
                uppers[0].zRot = Mth.lerp(down, uppers[0].zRot, .5F);
                curl(0, Mth.lerp(down, .35F, -.2F));
            }
            case UnderstudyChoreography.GESTURE_CURTAIN -> {
                // Both hands reach up for the fly rope over the right shoulder, gather, then haul it down hand over hand.
                float impact = 24.0F;
                float reach = HemoEasing.smoothstep(0.0F, 14.0F, since);
                float tension = HemoEasing.smoothstep(impact - 5.0F, impact - 1.0F, since) * (1.0F - drop(since, impact));
                float haul = drop(since, impact);
                float hold = reach * held(since, impact, 40.0F, 14.0F);
                float hand = Math.max(0.0F, Mth.sin((since - impact) * .4F)) * haul;
                for (int i = 0; i < 2; i++) {
                    float top = -2.8F + (i == 0 ? .05F : -.1F) + Mth.sin(since * 3.1F) * .05F * tension;
                    float hauled = -1.1F - (i == 0 ? hand : 1.0F - hand) * .9F;
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, Mth.lerp(haul, top, hauled));
                    uppers[i].zRot = Mth.lerp(hold, uppers[i].zRot, i == 0 ? .25F : -.45F);
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, -.3F);
                    curl(i, Mth.lerp(hold, .45F, 1.5F));
                }
                staff.xRot = Mth.lerp(hold, staff.xRot, 1.4F);
                torso.xRot += hold * (-.1F * (1.0F - haul) + .25F * haul);
                torso.yRot += hold * -.25F;
                hips.y += hold * haul * 1.5F;
                for (int i = 0; i < 2; i++) {
                    thighs[i].xRot -= hold * haul * .35F;
                    shins[i].xRot += hold * haul * .7F;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
            }
            case UnderstudyChoreography.GESTURE_REPRISE -> {
                // The head bowed over the prompt, the free hand conducting each step as it is learned.
                float hold = HemoEasing.smoothstep(0.0F, 6.0F, since);
                float beat = (since % UnderstudyChoreography.BEAT) / UnderstudyChoreography.BEAT;
                float stroke = beat < .3F ? beat / .3F : 1.0F - (beat - .3F) / .7F;
                head.xRot += .3F * hold;
                uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, -1.5F + .5F * stroke);
                uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, .3F);
                fores[0].xRot = Mth.lerp(hold, fores[0].xRot, -.6F - .4F * stroke);
                curl(0, Mth.lerp(hold, .35F, .1F));
            }
            case UnderstudyChoreography.GESTURE_BEAT -> {
                // Each beat of the reprise given its downbeat, as the double strikes the mark.
                float impact = UnderstudyChoreography.LEAD;
                float lift = HemoEasing.smoothstep(0.0F, impact - 2.0F, since) * (1.0F - drop(since, impact));
                float down = drop(since, impact) * held(since, impact, 1.0F, 5.0F);
                head.xRot += .3F;
                uppers[0].xRot = -1.5F - .8F * lift + 1.2F * down;
                uppers[0].zRot = .3F;
                fores[0].xRot = -.6F - .5F * lift;
                torso.xRot += .1F * down;
                curl(0, .1F + 1.2F * down);
            }
            case UnderstudyChoreography.GESTURE_KNOCK -> {
                // The staff's heel lifted and knocked on the boards three times; then both arms flung wide as the doors drop.
                float lift = 0.0F;
                for (int knock : UnderstudyChoreography.KNOCKS) {
                    float before = HemoEasing.smoothstep(knock - 6.0F, knock - 2.0F, since);
                    lift = Math.max(lift, before * (1.0F - drop(since, knock)) * (since < knock + 1.0F ? 1.0F : 0.0F));
                }
                float drop = UnderstudyChoreography.KNOCKS[2] + 2.0F;
                float wide = HemoEasing.smoothstep(drop - 1.5F, drop, since) * held(since, drop, 8.0F, 14.0F);
                staff.xRot = Mth.lerp(Math.max(lift, wide * .5F), staff.xRot, .1F);
                uppers[1].xRot = Mth.lerp(lift, uppers[1].xRot, -1.0F);
                fores[1].xRot = Mth.lerp(lift, fores[1].xRot, -.9F);
                hips.y += lift * -.4F;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    if (i == 0) {
                        uppers[i].xRot = Mth.lerp(wide, uppers[i].xRot, -1.2F);
                        uppers[i].zRot = Mth.lerp(wide, uppers[i].zRot, -sign * 1.3F);
                        fores[i].xRot = Mth.lerp(wide, fores[i].xRot, -.1F);
                        curl(i, Mth.lerp(wide, .35F, -.25F));
                    }
                }
                torso.xRot += .12F * wide;
            }
            default -> {
            }
        }
    }

    /** The left arm lifting the staff overhead and out over the ring, the cue light at its head; {@code aim} tips it. */
    private void staffRaised(float raise, float aim) {
        uppers[1].xRot = Mth.lerp(raise, uppers[1].xRot, -2.75F + aim);
        uppers[1].zRot = Mth.lerp(raise, uppers[1].zRot, -.15F);
        fores[1].xRot = Mth.lerp(raise, fores[1].xRot, -.2F);
        staff.xRot = Mth.lerp(raise, staff.xRot, 1.25F);
        curl(1, 1.3F);
    }

    /** The double's own pose: stepping in and striking the mark, or rising through the trapdoor and stamping. */
    private void doublePose() {
        reset();
        float since = doubleSince;
        if (doubleGesture == UnderstudyChoreography.GESTURE_BEAT) {
            float impact = UnderstudyChoreography.LEAD;
            float lift = HemoEasing.smoothstep(0.0F, impact - 1.5F, since) * (1.0F - drop(since, impact));
            float down = drop(since, impact) * held(since, impact, 2.0F, 6.0F);
            torso.xRot += .25F * lift - .1F + .45F * down;
            uppers[0].xRot = -2.9F * lift - .9F * down;
            fores[0].xRot = -.4F * lift - .2F * down;
            uppers[1].xRot = -.6F * lift + .8F * down;
            uppers[1].zRot = .5F;
            thighs[0].xRot = -.7F * Math.max(lift, down);
            shins[0].xRot = .4F * Math.max(lift, down);
            thighs[1].xRot = .4F * Math.max(lift, down);
            hips.y += 2.5F * down;
            for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            curl(0, 1.4F);
            curl(1, .2F);
        } else {
            float stamp = UnderstudyChoreography.KNOCKS[2] + 2.0F;
            float raise = HemoEasing.smoothstep(stamp - 6.0F, stamp - 2.0F, since) * (1.0F - drop(since, stamp));
            thighs[0].xRot = -1.1F * raise;
            shins[0].xRot = 1.1F * raise;
            feet[0].xRot = -thighs[0].xRot - shins[0].xRot;
            for (int i = 0; i < 2; i++) {
                float sign = i == 0 ? -1 : 1;
                uppers[i].xRot = -1.0F - .6F * raise;
                uppers[i].zRot = -sign * .9F;
                curl(i, -.2F);
            }
            head.xRot -= .2F;
        }
        staff.visible = false;
        mantle.xRot += .3F;
    }

    /** The double's size against the true self's: a performer's own height on the boards. */
    private static final float DOUBLE_SIZE = 2.4F / (62.0F / 16.0F * UnderstudyChoreography.SCALE);

    /** Draws the double in red light at this frame's synced point, faced out to the house; the true self is untouched. */
    public void renderDouble(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        if (doubleWeight <= .01F) return;
        List<PartPose> saved = new ArrayList<>(parts.size());
        for (ModelPart part : parts) saved.add(part.storePose());
        boolean staffVisible = staff.visible;
        doublePose();
        pose.pushPose();
        // From the floor under the mark: the rise lifts it up through the boards.
        pose.translate(doubleAt.x / 16.0F, doubleAt.y / 16.0F + (1.0F - doubleRise) * 2.4F / UnderstudyChoreography.SCALE, doubleAt.z / 16.0F);
        pose.mulPose(Axis.YP.rotation((float) Math.atan2(-doubleAt.x, -doubleAt.z)));
        pose.scale(DOUBLE_SIZE, DOUBLE_SIZE, DOUBLE_SIZE);
        pose.translate(0.0F, -1.5F, 0.0F);
        int alpha = (int) (Mth.clamp(doubleWeight, 0.0F, 1.0F) * ((color >>> 24) & 255));
        root.render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
        pose.popPose();
        for (int i = 0; i < parts.size(); i++) parts.get(i).loadPose(saved.get(i));
        staff.visible = staffVisible;
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip and lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / UnderstudyChoreography.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
