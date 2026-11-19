package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealFlight;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealMoves;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_strongman.py; regenerate the model, atlas and BBModel together.
/**
 * The Strongman's true self: a colossus seen from the waist up, wading round his ring. At rest he leans over the ring
 * with his hands held out over it, fingers half closed, breathing, his head following the fighter. Each intervention is
 * a gesture ({@link CircusFacultyEntity#gesture}) timed from its wind-up. His own arms stay outside the ring and make
 * the motion where he stands; spectral arms, larger copies of his own drawn glowing ({@link #renderGhosts}), manifest
 * over the ring and do it there, at a synced point: a fist held over the weight's mark and driven down on it, both fists
 * brought down on the rim nearest him for the heave, a hand riding the bar across the ring, and both hands taking hold of
 * the fighter and hauling for the line.
 */
public final class CircusTrueStrongmanModel extends EntityModel<CircusFacultyEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_strongman"), "main");
    /** His arm's lengths in pixels: upper arm and forearm, then the hand with its fingers. */
    private static final float ARM = 28.0F, HAND = 13.0F;
    private final ModelPart root, chest, neck, head;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    /** This frame's body: where it stands and faces, to bring a world point into model space. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw;

    public CircusTrueStrongmanModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        chest = root.getChild("belly").getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            shoulders[i] = chest.getChild(side + "_shoulder");
            uppers[i] = shoulders[i].getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition belly = root.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(0, 0).addBox(-13.0F, -15.0F, -8.0F, 26.0F, 15.0F, 16.0F).texOffs(85, 0).addBox(-14.0F, -4.0F, -9.0F, 28.0F, 4.0F, 18.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition buckle = belly.addOrReplaceChild("buckle", CubeListBuilder.create().texOffs(178, 0).addBox(-4.0F, -2.5F, -1.0F, 8.0F, 5.0F, 1.0F).texOffs(197, 0).addBox(-2.0F, -1.5F, -1.5F, 4.0F, 3.0F, 0.5F), PartPose.offset(0.0F, -2.0F, -9.0F));
        PartDefinition chest = belly.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 32).addBox(-16.0F, -16.0F, -9.0F, 32.0F, 16.0F, 18.0F).texOffs(101, 32).addBox(-18.0F, -20.0F, -8.0F, 36.0F, 4.0F, 15.0F), PartPose.offset(0.0F, -15.0F, 0.0F));
        PartDefinition strap = chest.addOrReplaceChild("strap", CubeListBuilder.create().texOffs(204, 32).addBox(-2.5F, -18.0F, -0.5F, 5.0F, 28.0F, 1.0F), PartPose.offsetAndRotation(2.0F, -6.0F, -12.25F, 0.0F, 0.0F, 0.6108652381980153F));
        PartDefinition back_strap = chest.addOrReplaceChild("back_strap", CubeListBuilder.create().texOffs(217, 32).addBox(-2.5F, -18.0F, -0.5F, 5.0F, 28.0F, 1.0F), PartPose.offsetAndRotation(2.0F, -6.0F, 9.25F, 0.0F, 0.0F, 0.6108652381980153F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 67).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 5.0F, 12.0F), PartPose.offset(0.0F, -21.0F, -1.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(49, 67).addBox(-7.0F, -14.0F, -7.0F, 14.0F, 14.0F, 14.0F), PartPose.offset(0.0F, -3.0F, -1.0F));
        PartDefinition brow = head.addOrReplaceChild("brow", CubeListBuilder.create().texOffs(106, 67).addBox(-7.25F, -1.0F, -1.5F, 14.5F, 2.0F, 1.5F), PartPose.offset(0.0F, -10.0F, -7.0F));
        PartDefinition nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(141, 67).addBox(-1.5F, -2.5F, -1.75F, 3.0F, 5.0F, 1.75F), PartPose.offset(0.0F, -6.0F, -7.0F));
        PartDefinition moustache = head.addOrReplaceChild("moustache", CubeListBuilder.create().texOffs(152, 67).addBox(-5.0F, -1.0F, -1.5F, 10.0F, 2.0F, 1.5F).texOffs(177, 67).addBox(5.0F, -1.5F, -1.5F, 3.0F, 1.5F, 1.5F).texOffs(188, 67).addBox(-8.0F, -1.5F, -1.5F, 3.0F, 1.5F, 1.5F).texOffs(199, 67).addBox(7.0F, -4.0F, -1.5F, 1.5F, 2.5F, 1.5F).texOffs(208, 67).addBox(-8.5F, -4.0F, -1.5F, 1.5F, 2.5F, 1.5F), PartPose.offset(0.0F, -3.5F, -7.0F));
        PartDefinition right_ear = head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(217, 67).addBox(-1.0F, -2.5F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(-7.0F, -7.0F, 0.0F));
        PartDefinition right_pec = chest.addOrReplaceChild("right_pec", CubeListBuilder.create().texOffs(0, 96).addBox(-8.0F, -5.0F, -3.0F, 16.0F, 10.0F, 3.0F), PartPose.offset(-8.0F, -11.0F, -9.0F));
        PartDefinition right_lat = chest.addOrReplaceChild("right_lat", CubeListBuilder.create().texOffs(39, 96).addBox(-2.0F, -7.0F, -6.0F, 4.0F, 14.0F, 11.0F), PartPose.offsetAndRotation(-15.0F, -8.0F, 1.0F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition right_trap = chest.addOrReplaceChild("right_trap", CubeListBuilder.create().texOffs(70, 96).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 5.0F, 12.0F), PartPose.offsetAndRotation(-5.0F, -20.0F, 0.0F, 0.0F, 0.0F, -0.3490658503988659F));
        PartDefinition right_shoulder = chest.addOrReplaceChild("right_shoulder", CubeListBuilder.create().texOffs(119, 96).addBox(-6.0F, -5.0F, -6.0F, 12.0F, 11.0F, 12.0F), PartPose.offset(-18.0F, -17.0F, 0.0F));
        PartDefinition right_pauldron = right_shoulder.addOrReplaceChild("right_pauldron", CubeListBuilder.create().texOffs(168, 96).addBox(-7.0F, -3.0F, -7.0F, 14.0F, 5.0F, 14.0F), PartPose.offsetAndRotation(-1.0F, -4.0F, 0.0F, 0.0F, 0.0F, -0.3141592653589793F));
        PartDefinition right_upper = right_shoulder.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(0, 122).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 15.0F, 10.0F).texOffs(41, 122).addBox(-4.0F, 2.0F, -6.5F, 8.0F, 9.0F, 1.5F), PartPose.offset(-1.0F, 4.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(62, 122).addBox(-5.5F, 0.0F, -5.5F, 11.0F, 13.0F, 11.0F).texOffs(107, 122).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 2.0F, 12.0F).texOffs(156, 122).addBox(-6.0F, 6.5F, -6.0F, 12.0F, 2.0F, 12.0F).texOffs(205, 122).addBox(-5.0F, 10.5F, -5.0F, 10.0F, 3.0F, 10.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(0, 148).addBox(-2.5F, 0.0F, -4.5F, 5.0F, 7.0F, 9.0F).texOffs(29, 148).addBox(-3.5F, 1.0F, -4.0F, 1.0F, 5.0F, 8.0F), PartPose.offset(0.0F, 13.5F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(48, 148).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 7.0F, -3.25F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(61, 148).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(74, 148).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 4.75F, 2.0F), PartPose.offset(0.0F, 7.0F, -1.0F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(87, 148).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 4.75F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(100, 148).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 4.5F, 2.0F), PartPose.offset(0.0F, 7.0F, 1.25F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(113, 148).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 4.5F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(126, 148).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 3.75F, 2.0F), PartPose.offset(0.0F, 7.0F, 3.5F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(139, 148).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 3.75F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(152, 148).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 4.0F, 2.5F), PartPose.offsetAndRotation(2.0F, 2.0F, -4.5F, -0.4363323129985824F, 0.0F, 0.3490658503988659F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(165, 148).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition left_ear = head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(174, 148).addBox(0.0F, -2.5F, -1.5F, 1.0F, 5.0F, 3.0F), PartPose.offset(7.0F, -7.0F, 0.0F));
        PartDefinition left_pec = chest.addOrReplaceChild("left_pec", CubeListBuilder.create().texOffs(183, 148).addBox(-8.0F, -5.0F, -3.0F, 16.0F, 10.0F, 3.0F), PartPose.offset(8.0F, -11.0F, -9.0F));
        PartDefinition left_lat = chest.addOrReplaceChild("left_lat", CubeListBuilder.create().texOffs(222, 148).addBox(-2.0F, -7.0F, -6.0F, 4.0F, 14.0F, 11.0F), PartPose.offsetAndRotation(15.0F, -8.0F, 1.0F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition left_trap = chest.addOrReplaceChild("left_trap", CubeListBuilder.create().texOffs(0, 174).addBox(-6.0F, -4.0F, -6.0F, 12.0F, 5.0F, 12.0F), PartPose.offsetAndRotation(5.0F, -20.0F, 0.0F, 0.0F, 0.0F, 0.3490658503988659F));
        PartDefinition left_shoulder = chest.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(49, 174).addBox(-6.0F, -5.0F, -6.0F, 12.0F, 11.0F, 12.0F), PartPose.offset(18.0F, -17.0F, 0.0F));
        PartDefinition left_pauldron = left_shoulder.addOrReplaceChild("left_pauldron", CubeListBuilder.create().texOffs(98, 174).addBox(-7.0F, -3.0F, -7.0F, 14.0F, 5.0F, 14.0F), PartPose.offsetAndRotation(1.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.3141592653589793F));
        PartDefinition left_upper = left_shoulder.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(155, 174).addBox(-5.0F, 0.0F, -5.0F, 10.0F, 15.0F, 10.0F).texOffs(196, 174).addBox(-4.0F, 2.0F, -6.5F, 8.0F, 9.0F, 1.5F), PartPose.offset(1.0F, 4.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(0, 200).addBox(-5.5F, 0.0F, -5.5F, 11.0F, 13.0F, 11.0F).texOffs(45, 200).addBox(-6.0F, 2.0F, -6.0F, 12.0F, 2.0F, 12.0F).texOffs(94, 200).addBox(-6.0F, 6.5F, -6.0F, 12.0F, 2.0F, 12.0F).texOffs(143, 200).addBox(-5.0F, 10.5F, -5.0F, 10.0F, 3.0F, 10.0F), PartPose.offset(0.0F, 15.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(184, 200).addBox(-2.5F, 0.0F, -4.5F, 5.0F, 7.0F, 9.0F).texOffs(213, 200).addBox(2.5F, 1.0F, -4.0F, 1.0F, 5.0F, 8.0F), PartPose.offset(0.0F, 13.5F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(232, 200).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 4.0F, 2.0F), PartPose.offset(0.0F, 7.0F, -3.25F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(0, 225).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(13, 225).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 4.75F, 2.0F), PartPose.offset(0.0F, 7.0F, -1.0F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(26, 225).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 4.75F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(39, 225).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 4.5F, 2.0F), PartPose.offset(0.0F, 7.0F, 1.25F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(52, 225).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 4.5F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(65, 225).addBox(-2.0F, 0.0F, -1.0F, 4.0F, 3.75F, 2.0F), PartPose.offset(0.0F, 7.0F, 3.5F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(78, 225).addBox(-1.75F, 0.0F, -1.0F, 3.5F, 3.25F, 2.0F), PartPose.offset(0.0F, 3.75F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(91, 225).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 4.0F, 2.5F), PartPose.offsetAndRotation(-2.0F, 2.0F, -4.5F, -0.4363323129985824F, 0.0F, -0.3490658503988659F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(104, 225).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offset(0.0F, 4.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusFacultyEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(age, yaw, pitch, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float age, float yaw, float pitch, int gesture, float since, Vec3 target) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        float breath = Mth.sin(age * .055F);
        // Leaning in over the ring, the chest rising and falling.
        chest.xRot += .14F + breath * .02F;
        neck.yRot += Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -60, 60) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD - .14F;
        // Every so often the hands close and open, as if weighing the ring.
        float clench = HemoEasing.smoothstep(.8F, 1.0F, Mth.sin(age * .021F));
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            shoulders[i].y -= breath * .35F;
            uppers[i].xRot = -.6F + breath * .03F;
            uppers[i].zRot = -sign * .32F;
            fores[i].xRot = -1.0F;
            fores[i].zRot = sign * .12F;
            hands[i].yRot = sign * 1.35F;
            hands[i].xRot = .25F;
            curl(i, .35F + clench * .9F);
        }
        ghostWeight[0] = ghostWeight[1] = 0.0F;
        if (gesture >= 0) gesture(gesture, since, target);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.25F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** This frame's spectral arms: how strongly each shows, where its fist lands (model pixels), how closed it is. */
    private final float[] ghostWeight = new float[2], ghostFist = new float[2];
    private final Vec3[] ghostTarget = {Vec3.ZERO, Vec3.ZERO};

    /**
     * A blow's beats, in ticks from the wind-up's start: a slow gather to {@link #GATHERED}, a trembling hold, the drop
     * (accelerating, a few ticks long) landing on {@link CircusFacultyEntity#GESTURE_IMPACT}, a hit-stop on the floor,
     * then a heavy recovery.
     */
    private static final float GATHERED = 14.0F, DROP = 2.5F, HELD = 8.0F, RECOVER = 18.0F;

    /** 0 until the drop, then 1 at impact, accelerating like a falling weight. */
    private static float drop(float since) {
        float t = Mth.clamp((since - (CircusFacultyEntity.GESTURE_IMPACT - DROP)) / DROP, 0.0F, 1.0F);
        return t * t;
    }

    /** 1 through the gather and the blow, falling to 0 over the recovery after the hit-stop. */
    private static float held(float since) {
        float after = CircusFacultyEntity.GESTURE_IMPACT + HELD;
        return 1.0F - HemoEasing.smoothstep(after, after + RECOVER, since);
    }

    private void gesture(int gesture, float since, Vec3 target) {
        float gather = HemoEasing.smoothstep(0.0F, GATHERED, since);
        // The tremble of a held weight at the top of the gather, before it falls.
        float tension = HemoEasing.smoothstep(GATHERED - 3.0F, GATHERED, since)
                * (1.0F - HemoEasing.smoothstep(CircusFacultyEntity.GESTURE_IMPACT - DROP, CircusFacultyEntity.GESTURE_IMPACT, since));
        float tremble = Mth.sin(since * 3.1F) * tension;
        float down = drop(since);
        float weight = HemoEasing.smoothstep(0.0F, 6.0F, since) * held(since);
        // The blow lands hard: the body pitches into it, then settles.
        float slam = down * held(since);
        switch (gesture) {
            case CircusFacultyEntity.GESTURE_DROP -> {
                // A fist hauled up over the mark, trembling, then dropped on it like the weight it is.
                lift(0, weight, gather, down, tremble);
                ghost(0, target.add(0.0D, Mth.lerp(down, 5.5D + .5D * gather + .15D * tremble, .4D), 0.0D), weight,
                        .5F + 1.0F * Math.max(gather, down));
                chest.xRot += -.12F * gather * (1.0F - down) + .28F * slam;
                head.xRot += .1F * slam;
            }
            case CircusFacultyEntity.GESTURE_SLAM -> {
                // Both fists over the rim nearest him, hauled up, then brought down together to start the wave.
                Vec3 toward = new Vec3(at.x - target.x, 0.0D, at.z - target.z).normalize();
                for (int i = 0; i < 2; i++) {
                    lift(i, weight, gather, down, tremble);
                    Vec3 rim = target.add(toward.yRot((i == 0 ? 1 : -1) * .4F).scale(OrdealMoves.HEAVE_START));
                    ghost(i, rim.add(0.0D, Mth.lerp(down, 6.0D + .5D * gather + .15D * tremble, .4D), 0.0D), weight,
                            .6F + .9F * Math.max(gather, down));
                }
                chest.xRot += -.16F * gather * (1.0F - down) + .34F * slam;
                head.xRot += .14F * slam;
            }
            case CircusFacultyEntity.GESTURE_SWEEP -> {
                // A hand pressed down on the bar where it will start, then hauled across the ring with it.
                float sweep = Mth.clamp((since - CircusFacultyEntity.GESTURE_IMPACT) / OrdealMoves.SWEEP_TICKS, 0.0F, 1.0F);
                float hold = HemoEasing.smoothstep(0.0F, 6.0F, since) * (1.0F - HemoEasing.smoothstep(
                        CircusFacultyEntity.GESTURE_IMPACT + OrdealMoves.SWEEP_TICKS, CircusFacultyEntity.GESTURE_IMPACT
                                + OrdealMoves.SWEEP_TICKS + 12.0F, since));
                // The sweep starts slow under the bar's weight and whips through the far side.
                float swept = sweep * sweep * (3.0F - 2.0F * sweep);
                uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, -1.35F + .1F * tremble);
                uppers[1].yRot = Mth.lerp(hold, 0.0F, Mth.lerp(swept, 1.0F, -1.0F));
                fores[1].xRot = Mth.lerp(hold, fores[1].xRot, -.25F);
                curl(1, Mth.lerp(hold, .35F, 1.3F));
                chest.yRot += hold * Mth.lerp(swept, .3F, -.3F);
                float along = -(float) OrdealMoves.SWEEP_REACH + 2.0F * (float) OrdealMoves.SWEEP_REACH * swept;
                double side = Mth.clamp(at.z - target.z, -8.0D, 8.0D);
                ghost(1, target.add(along, Mth.lerp(gather, 2.5D, .8D), side), hold, 1.3F);
            }
            case CircusFacultyEntity.GESTURE_PULL -> {
                // Both hands shoot out for the fighter, close on them at the strike, and haul in hard jerks.
                float out = HemoEasing.smoothstep(0.0F, 8.0F, since);
                float grip = HemoEasing.smoothstep(CircusFacultyEntity.GESTURE_IMPACT - 2.0F, CircusFacultyEntity.GESTURE_IMPACT, since);
                float hold = out * (1.0F - HemoEasing.smoothstep(CircusFacultyEntity.GESTURE_IMPACT + 60.0F,
                        CircusFacultyEntity.GESTURE_IMPACT + 72.0F, since));
                // Each haul a jerk: a sharp pull, then a breath.
                float jerk = Math.max(0.0F, Mth.sin(since * .55F)) * grip;
                Vec3 hauled = target.lerp(at, (.15F + .2F * jerk) * grip).add(0.0D, 1.2D, 0.0D);
                for (int i = 0; i < 2; i++) {
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, Mth.lerp(grip, -1.5F, -.9F - .25F * jerk));
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, Mth.lerp(grip, -.15F, -1.6F - .2F * jerk));
                    curl(i, Mth.lerp(hold, .35F, .2F + 1.3F * grip));
                    Vec3 offset = new Vec3(i == 0 ? -1.2D : 1.2D, 0.0D, 0.0D).yRot(-bodyYaw * Mth.DEG_TO_RAD);
                    ghost(i, hauled.add(offset), hold, .2F + 1.3F * grip);
                }
                chest.xRot += .12F * out * (1.0F - grip) - .14F * jerk * hold;
            }
            default -> {
            }
        }
    }

    /** His own arm hauled up overhead through the gather, trembling at the top, brought down hard at the drop. */
    private void lift(int i, float weight, float gather, float down, float tremble) {
        float up = Mth.lerp(gather, -.6F, -2.9F + .05F * tremble);
        uppers[i].xRot = Mth.lerp(weight, uppers[i].xRot, Mth.lerp(down, up, -1.0F));
        uppers[i].zRot *= 1.0F - .6F * weight;
        fores[i].xRot = Mth.lerp(weight, fores[i].xRot, Mth.lerp(down, Mth.lerp(gather, -1.0F, -1.3F), -.25F));
        curl(i, Mth.lerp(weight, .35F, .4F + 1.1F * Math.max(gather, down)));
    }

    private void ghost(int i, Vec3 world, float weight, float fist) {
        ghostWeight[i] = weight;
        ghostTarget[i] = model(world);
        ghostFist[i] = fist;
    }

    /** The spectral arm's size against his own, and its length then (upper arm, forearm and hand), in pixels. */
    private static final float GHOST_SIZE = 1.6F, GHOST_LENGTH = (ARM + HAND) * GHOST_SIZE;

    /**
     * Draws the spectral arms: a copy of his own arm, larger, manifesting in the air over the ring and driving its fist
     * onto this frame's target from above and from his side, faded in with the gesture. His real arms stay where they are.
     */
    public void renderGhosts(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int i = 0; i < 2; i++) {
            float weight = ghostWeight[i];
            if (weight <= .01F) continue;
            Vec3 target = ghostTarget[i];
            Vec3 toward = new Vec3(-target.x, 0.0D, -target.z);
            toward = toward.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : toward.normalize();
            // From above and from his side of the ring: in model space up is -y.
            Vec3 dir = toward.scale(-.5D).add(0.0D, .85D, 0.0D).normalize();
            Vec3 origin = target.subtract(dir.scale(GHOST_LENGTH));
            PartPose upper = uppers[i].storePose(), fore = fores[i].storePose(), hand = hands[i].storePose();
            float scale = uppers[i].xScale;
            uppers[i].setPos((float) origin.x, (float) origin.y, (float) origin.z);
            uppers[i].setRotation((float) Math.asin(Mth.clamp(dir.z, -1.0D, 1.0D)), 0.0F, (float) Math.atan2(-dir.x, dir.y));
            uppers[i].xScale = uppers[i].yScale = uppers[i].zScale = GHOST_SIZE;
            fores[i].setRotation(0.0F, 0.0F, 0.0F);
            hands[i].setRotation(0.0F, hands[i].yRot, 0.0F);
            float[] curled = {fingers[i][0].zRot};
            curl(i, ghostFist[i]);
            int alpha = (int) (Mth.clamp(weight, 0.0F, 1.0F) * ((color >>> 24) & 255));
            uppers[i].render(pose, consumer, light, overlay, (alpha << 24) | (color & 0xFFFFFF));
            uppers[i].loadPose(upper);
            fores[i].loadPose(fore);
            hands[i].loadPose(hand);
            uppers[i].xScale = uppers[i].yScale = uppers[i].zScale = scale;
            curl(i, Math.abs(curled[0]));
        }
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / OrdealFlight.SCALE);
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
