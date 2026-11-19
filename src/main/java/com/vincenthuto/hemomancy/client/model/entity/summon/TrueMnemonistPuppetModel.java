package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMnemonistMoves;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_mnemonist_puppet.py (the Mnemonist Puppet's true self, 2026-11-17); regenerate
// the model, atlas and BBModel together.
/**
 * The Mnemonist Puppet's true self: memory made flesh. A marionette's walk (long strides, the body hung from strings
 * and lifting on each step, the arms dangling loose and swinging late), the head cocked to listen, the spool's threads
 * standing out and stirring, and its ordeal moves ({@link OrdealMnemonistMoves}), each a gather, a fast blow and a
 * recovery: the recital's three blows walked forward, the threads cast from the spool, the echo let out of its open
 * jaw, and the lunge.
 */
public final class TrueMnemonistPuppetModel extends EntityModel<MnemonistPuppetEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_mnemonist_puppet"), "main");
    private final ModelPart root, pelvis, spine, chest, neck, head, jaw, spool;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thighs = new ModelPart[2], shins = new ModelPart[2], feet = new ModelPart[2],
            threads = new ModelPart[4], threadTips = new ModelPart[4];
    private final ModelPart[][] fingers = new ModelPart[2][3], tips = new ModelPart[2][3];

    public TrueMnemonistPuppetModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        spine = pelvis.getChild("spine");
        chest = spine.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        spool = chest.getChild("spool");
        for (int t = 0; t < 4; t++) {
            threads[t] = spool.getChild("thread_" + t);
            threadTips[t] = threads[t].getChild("thread_" + t + "_tip");
        }
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            shoulders[i] = chest.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            for (int f = 0; f < 3; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -16.0F, 0.0F));
        PartDefinition spine = pelvis.addOrReplaceChild("spine", CubeListBuilder.create().texOffs(11, 0).addBox(-0.75F, -3.0F, -0.75F, 1.5F, 3.0F, 1.5F).texOffs(20, 0).addBox(-1.5F, -4.5F, -1.0F, 3.0F, 2.0F, 2.0F), PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition chest = spine.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(31, 0).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 5.0F, 3.0F).texOffs(46, 0).addBox(-2.5F, -5.5F, -1.75F, 2.0F, 3.0F, 3.5F).texOffs(59, 0).addBox(0.5F, -5.0F, -1.75F, 2.0F, 2.5F, 3.5F).texOffs(72, 0).addBox(-1.0F, -1.5F, -1.75F, 2.5F, 1.5F, 3.5F), PartPose.offset(0.0F, -4.0F, 0.0F));
        PartDefinition face_0 = chest.addOrReplaceChild("face_0", CubeListBuilder.create().texOffs(87, 0).addBox(-0.75F, -1.0F, -0.5F, 1.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(-1.25F, -3.5F, -1.75F, 0.0F, 0.0F, -0.20943951023931956F));
        PartDefinition face_1 = chest.addOrReplaceChild("face_1", CubeListBuilder.create().texOffs(94, 0).addBox(-0.75F, -1.0F, -0.5F, 1.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(1.0F, -2.0F, -1.75F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition face_2 = chest.addOrReplaceChild("face_2", CubeListBuilder.create().texOffs(101, 0).addBox(-0.75F, -1.0F, -0.5F, 1.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(-0.25F, -1.0F, -1.75F, 0.0F, 0.0F, 0.06981317007977318F));
        PartDefinition spool = chest.addOrReplaceChild("spool", CubeListBuilder.create().texOffs(108, 0).addBox(-3.0F, -2.0F, 0.0F, 6.0F, 4.0F, 1.0F).texOffs(0, 9).addBox(-2.0F, -3.0F, 0.0F, 4.0F, 6.0F, 1.0F).texOffs(11, 9).addBox(-2.0F, -2.0F, 1.0F, 4.0F, 4.0F, 2.5F).texOffs(26, 9).addBox(-3.0F, -2.0F, 3.5F, 6.0F, 4.0F, 1.0F).texOffs(41, 9).addBox(-2.0F, -3.0F, 3.5F, 4.0F, 6.0F, 1.0F).texOffs(52, 9).addBox(-0.5F, -0.5F, 4.5F, 1.0F, 1.0F, 1.5F), PartPose.offset(0.0F, -3.0F, 1.5F));
        PartDefinition thread_0 = spool.addOrReplaceChild("thread_0", CubeListBuilder.create().texOffs(59, 9).addBox(-0.25F, -6.0F, -0.25F, 0.5F, 6.0F, 0.5F), PartPose.offsetAndRotation(-1.5F, -1.5F, 2.0F, 0.6981317007977318F, 0.0F, -0.6108652381980153F));
        PartDefinition thread_0_tip = thread_0.addOrReplaceChild("thread_0_tip", CubeListBuilder.create().texOffs(64, 9).addBox(-0.25F, -5.0F, -0.25F, 0.5F, 5.0F, 0.5F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition thread_1 = spool.addOrReplaceChild("thread_1", CubeListBuilder.create().texOffs(69, 9).addBox(-0.25F, -6.0F, -0.25F, 0.5F, 6.0F, 0.5F), PartPose.offsetAndRotation(1.5F, -1.5F, 2.0F, 0.6981317007977318F, 0.0F, 0.6108652381980153F));
        PartDefinition thread_1_tip = thread_1.addOrReplaceChild("thread_1_tip", CubeListBuilder.create().texOffs(74, 9).addBox(-0.25F, -5.0F, -0.25F, 0.5F, 5.0F, 0.5F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition thread_2 = spool.addOrReplaceChild("thread_2", CubeListBuilder.create().texOffs(79, 9).addBox(-0.25F, -6.0F, -0.25F, 0.5F, 6.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 1.0F, 2.0F, 1.2217304763960306F, 0.0F, -0.3490658503988659F));
        PartDefinition thread_2_tip = thread_2.addOrReplaceChild("thread_2_tip", CubeListBuilder.create().texOffs(84, 9).addBox(-0.25F, -5.0F, -0.25F, 0.5F, 5.0F, 0.5F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition thread_3 = spool.addOrReplaceChild("thread_3", CubeListBuilder.create().texOffs(89, 9).addBox(-0.25F, -6.0F, -0.25F, 0.5F, 6.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 1.0F, 2.0F, 1.2217304763960306F, 0.0F, 0.3490658503988659F));
        PartDefinition thread_3_tip = thread_3.addOrReplaceChild("thread_3_tip", CubeListBuilder.create().texOffs(94, 9).addBox(-0.25F, -5.0F, -0.25F, 0.5F, 5.0F, 0.5F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, -0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(99, 9).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offset(0.0F, -5.5F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(104, 9).addBox(-2.5F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -2.0F, 0.0F, 0.10471975511965978F, 0.0F, 0.20943951023931956F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(0, 19).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 1.5F, 3.5F), PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition right_shoulder = chest.addOrReplaceChild("right_shoulder", CubeListBuilder.create().texOffs(17, 19).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(-3.0F, -4.5F, 0.0F));
        PartDefinition right_upper = right_shoulder.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(26, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(31, 19).addBox(-0.75F, 5.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offset(0.0F, 0.5F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(40, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.5F, 1.0F).texOffs(45, 19).addBox(-0.75F, 2.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(54, 19).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(61, 19).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.5F, 0.5F), PartPose.offset(-0.75F, 1.5F, 0.0F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(66, 19).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(71, 19).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.5F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(76, 19).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(81, 19).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.5F, 0.5F), PartPose.offset(0.75F, 1.5F, 0.0F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(86, 19).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(91, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F).texOffs(96, 19).addBox(-0.75F, 7.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(-1.0F, 0.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(105, 19).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(110, 19).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 7.0F, 0.0F));
        PartDefinition left_shoulder = chest.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(119, 19).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offset(3.0F, -4.5F, 0.0F));
        PartDefinition left_upper = left_shoulder.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(0, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(5, 29).addBox(-0.75F, 5.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offset(0.0F, 0.5F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(14, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.5F, 1.0F).texOffs(19, 29).addBox(-0.75F, 2.5F, -0.75F, 1.5F, 1.0F, 1.5F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(28, 29).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(35, 29).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.5F, 0.5F), PartPose.offset(-0.75F, 1.5F, 0.0F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(40, 29).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(45, 29).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.5F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(50, 29).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(55, 29).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.5F, 0.5F), PartPose.offset(0.75F, 1.5F, 0.0F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(60, 29).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(65, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 8.0F, 1.0F).texOffs(70, 29).addBox(-0.75F, 7.0F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offset(1.0F, 0.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(79, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(84, 29).addBox(-0.5F, 0.0F, -2.0F, 1.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 7.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.yScale = 1.0F;
        });
        float movement = Mth.clamp(limbSwingAmount * 1.4F, 0.0F, 1.0F);
        float stride = limbSwing * .55F;
        // Hung from strings: the body lifts on each long step and sways over the planted leg.
        pelvis.y -= Math.abs(Mth.sin(stride)) * 1.4F * movement + Mth.sin(age * .09F) * .25F;
        chest.zRot += Mth.cos(stride) * .12F * movement + Mth.sin(age * .05F) * .03F;
        chest.xRot += .12F + .1F * movement;
        head.yRot += Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD;
        // Listening: the cocked head twitches now and then.
        head.zRot += Mth.sin(age * .7F) * .08F * HemoEasing.smoothstep(.85F, 1.0F, Mth.sin(age * .043F));
        jaw.xRot += .1F * Mth.sin(age * .08F);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI) * movement;
            thighs[i].xRot = swing * .85F;
            shins[i].xRot = Math.max(0.0F, -swing) * 1.3F + .1F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            // Dangling arms that swing late, as if only the strings move them.
            float late = Mth.cos(stride - .9F + i * Mth.PI + Mth.PI) * movement;
            uppers[i].xRot = .1F + late * .55F + Mth.sin(age * .06F + i) * .04F;
            uppers[i].zRot = -sign * (.12F + .05F * Mth.sin(age * .07F + i * 2));
            fores[i].xRot = -.15F - Math.max(0.0F, late) * .4F;
            curl(i, .25F + .1F * Mth.sin(age * .11F + i));
        }
        for (int t = 0; t < 4; t++) {
            threads[t].xRot += Mth.sin(age * .13F + t * 1.7F) * .1F;
            threadTips[t].xRot += Mth.sin(age * .19F + t) * .25F;
            threads[t].zRot += Mth.sin(age * .11F + t * 2.3F) * .08F;
        }
        spool.zRot = age * .03F;
        int move = entity.ordealMove();
        if (move >= 0) move(move, entity.ordealMoveAge(partialTick));
    }

    private void curl(int i, float amount) {
        for (int f = 0; f < 3; f++) {
            fingers[i][f].xRot = -amount * (1.0F + f * .1F);
            tips[i][f].xRot = -amount * 1.3F;
        }
    }

    /** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating. */
    private static float fall(float since, float at) {
        float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
        return t * t;
    }

    private static float strike(float since, float at, float back) {
        return HemoEasing.smoothstep(at - back, at - 2.0F, since) * (1.0F - fall(since, at));
    }

    private void move(int move, float since) {
        switch (move) {
            case OrdealMnemonistMoves.RECITAL -> {
                float w = OrdealMnemonistMoves.RECITAL_WINDUP, beat = OrdealMnemonistMoves.RECITAL_BEAT;
                float end = 1.0F - HemoEasing.smoothstep(w + 2 * beat + 4, w + 2 * beat + 16, since);
                float crouch = HemoEasing.smoothstep(0.0F, w - 2, since) * end;
                // Right, left, then both overhead: each arm cocked back through its gather and driven down on its beat.
                float r1 = strike(since, w, w), l1 = strike(since, w + beat, beat + 1), both = strike(since, w + 2 * beat, beat + 1);
                float d1 = fall(since, w) * (1 - fall(since, w + beat)), d2 = fall(since, w + beat) * (1 - fall(since, w + 2 * beat));
                float d3 = fall(since, w + 2 * beat) * end;
                uppers[0].xRot = Mth.lerp(crouch, uppers[0].xRot, -2.8F * Math.max(r1, both) - .6F * (d1 + d3) - .2F);
                uppers[1].xRot = Mth.lerp(crouch, uppers[1].xRot, -2.8F * Math.max(l1, both) - .6F * (d2 + d3) - .2F);
                for (int i = 0; i < 2; i++) {
                    fores[i].xRot = Mth.lerp(crouch, fores[i].xRot, -.5F);
                    curl(i, Mth.lerp(crouch, .25F, -.3F));
                }
                chest.xRot += crouch * .2F + .35F * (d1 + d2) + .5F * d3;
                chest.yRot += .35F * (d1 - d2);
                head.xRot -= .3F * Math.max(r1, both);
                jaw.xRot += .5F * d3;
                thighs[0].xRot -= .6F * crouch;
                shins[0].xRot += .5F * crouch;
                thighs[1].xRot += .35F * crouch;
                for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            }
            case OrdealMnemonistMoves.THREAD_CAST -> {
                float w = OrdealMnemonistMoves.THREAD_WINDUP;
                float end = 1.0F - HemoEasing.smoothstep(w + 6, w + 18, since);
                float wind = HemoEasing.smoothstep(0.0F, w - 2, since) * (1.0F - fall(since, w));
                float cast = fall(since, w) * end;
                // Arms flung wide and back, the spool thrust out; then everything thrown forward and the threads lash out.
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].xRot = Mth.lerp(wind + cast, uppers[i].xRot, Mth.lerp(cast, .9F, -1.7F));
                    uppers[i].zRot = Mth.lerp(wind + cast, uppers[i].zRot, -sign * Mth.lerp(cast, 1.2F, .3F));
                    curl(i, Mth.lerp(cast, .2F, -.4F));
                }
                chest.xRot += -.4F * wind + .45F * cast;
                head.xRot += -.3F * wind + .2F * cast;
                for (int t = 0; t < 4; t++) {
                    threads[t].xRot += -.5F * wind + 1.1F * cast;
                    threadTips[t].xRot += .6F * cast;
                    threads[t].yScale = 1.0F + 1.2F * cast;
                    threadTips[t].yScale = 1.0F + 1.4F * cast;
                }
            }
            case OrdealMnemonistMoves.ECHO -> {
                float w = OrdealMnemonistMoves.ECHO_WINDUP, gap = OrdealMnemonistMoves.ECHO_GAP;
                float end = 1.0F - HemoEasing.smoothstep(w + gap + 4, w + gap + 18, since);
                float draw = HemoEasing.smoothstep(0.0F, w - 2, since) * end;
                // The head thrown back, the jaw wide on its hollow; the cry and its echo jolt it twice.
                float cry = fall(since, w) * (1.0F - HemoEasing.smoothstep(w + 2, w + 6, since))
                        + fall(since, w + gap) * (1.0F - HemoEasing.smoothstep(w + gap + 2, w + gap + 6, since));
                head.xRot += -.7F * draw + .5F * cry;
                jaw.xRot += .9F * draw;
                chest.xRot += -.3F * draw + .3F * cry;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(draw, uppers[i].zRot, -sign * 1.0F);
                    uppers[i].xRot = Mth.lerp(draw, uppers[i].xRot, .3F);
                    curl(i, Mth.lerp(draw, .25F, -.5F));
                    thighs[i].xRot -= .3F * cry;
                    shins[i].xRot += .5F * cry;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                pelvis.y += 1.0F * cry;
            }
            case OrdealMnemonistMoves.LUNGE -> {
                float w = OrdealMnemonistMoves.LUNGE_WINDUP, run = 8.0F;
                float crouch = HemoEasing.smoothstep(0.0F, w - 2, since) * (1.0F - HemoEasing.smoothstep(w + run, w + run + 14, since));
                float running = HemoEasing.smoothstep(w - 1, w + 1, since) * (1.0F - HemoEasing.smoothstep(w + run, w + run + 4, since));
                chest.xRot += .55F * crouch;
                head.xRot -= .4F * crouch;
                pelvis.y += 2.0F * crouch * (1.0F - running);
                uppers[0].xRot = Mth.lerp(crouch, uppers[0].xRot, Mth.lerp(running, .9F, -1.9F));
                uppers[1].xRot = Mth.lerp(crouch, uppers[1].xRot, Mth.lerp(running, -.6F, -1.6F));
                curl(0, Mth.lerp(running, .3F, -.4F));
                curl(1, Mth.lerp(running, .3F, -.4F));
                for (int i = 0; i < 2; i++) {
                    float sprint = Mth.cos(since * 1.6F + i * Mth.PI) * running;
                    thighs[i].xRot += -.5F * crouch * (1.0F - running) + sprint * 1.0F;
                    shins[i].xRot += .8F * crouch * (1.0F - running) + Math.max(0.0F, -sprint) * 1.0F;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                for (int t = 0; t < 4; t++) threads[t].xRot -= .6F * running;
            }
            default -> {
            }
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
