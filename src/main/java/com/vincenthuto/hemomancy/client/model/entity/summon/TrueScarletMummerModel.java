package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.ScarletMummerOrdealMoves;
import com.vincenthuto.hemomancy.common.encounter.ordeal.StiltWalkerMoves;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_true_scarlet_mummer.py (the Mummer's true self, 2026-11-17); regenerate together.
/**
 * The Scarlet Mummer's true self in the Stilt Walker's Chamber: long-limbed, a cracked mask grinning, a train of
 * eyespot fans folded down its back like a cloak. It prances (high knees, a bob, a twitch of the head, arms flourishing)
 * and its ordeal moves ({@link ScarletMummerOrdealMoves}) are each a slow gather, a fast blow and a recovery: the
 * dazzle throws every fan open at once, the feint steps aside and lunges fingers first, the whirl spins twice with its
 * ribbons out, the entrance crouches, leaps tucked and lands spread.
 */
public class TrueScarletMummerModel extends EntityModel<ScarletMummerEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("true_scarlet_mummer"), "main");
    private final ModelPart root, pelvis, torso, chest, neck, head, hat, train;
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], uppers = new ModelPart[2],
            fores = new ModelPart[2], hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2],
            fans = new ModelPart[2], outers = new ModelPart[2], fanTips = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];
    private final ModelPart[] ribbons = new ModelPart[5], ribbonTips = new ModelPart[5];

    public TrueScarletMummerModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        torso = pelvis.getChild("torso");
        chest = torso.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        hat = head.getChild("hat");
        train = chest.getChild("train");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            thumbTips[i] = thumbs[i].getChild(side + "_thumb_tip");
            fans[i] = chest.getChild(side + "_fan");
            outers[i] = fans[i].getChild(side + "_fan_outer");
            fanTips[i] = outers[i].getChild(side + "_fan_tip");
            for (int f = 0; f < 4; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
        }
        for (int k = 0; k < 5; k++) {
            ribbons[k] = pelvis.getChild("ribbon_" + k);
            ribbonTips[k] = ribbons[k].getChild("ribbon_" + k + "_tip");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -1.0F, -1.5F, 5.0F, 3.0F, 3.0F), PartPose.offset(0.0F, -13.0F, 0.0F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(17, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(-1.5F, 2.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(26, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(35, 0).addBox(-1.0F, 4.0F, -3.0F, 2.0F, 1.0F, 4.0F).texOffs(48, 0).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(53, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(1.5F, 2.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(62, 0).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(71, 0).addBox(-1.0F, 4.0F, -3.0F, 2.0F, 1.0F, 4.0F).texOffs(84, 0).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition ribbon_0 = pelvis.addOrReplaceChild("ribbon_0", CubeListBuilder.create().texOffs(89, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(-2.0F, 1.5F, -1.25F, 0.0F, 0.0F, -0.20943951023931956F));
        PartDefinition ribbon_0_tip = ribbon_0.addOrReplaceChild("ribbon_0_tip", CubeListBuilder.create().texOffs(94, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition ribbon_1 = pelvis.addOrReplaceChild("ribbon_1", CubeListBuilder.create().texOffs(99, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(-1.0F, 1.5F, 1.25F, 0.0F, 0.0F, -0.10471975511965978F));
        PartDefinition ribbon_1_tip = ribbon_1.addOrReplaceChild("ribbon_1_tip", CubeListBuilder.create().texOffs(104, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition ribbon_2 = pelvis.addOrReplaceChild("ribbon_2", CubeListBuilder.create().texOffs(109, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offset(0.0F, 1.5F, -1.25F));
        PartDefinition ribbon_2_tip = ribbon_2.addOrReplaceChild("ribbon_2_tip", CubeListBuilder.create().texOffs(114, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition ribbon_3 = pelvis.addOrReplaceChild("ribbon_3", CubeListBuilder.create().texOffs(119, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(1.0F, 1.5F, 1.25F, 0.0F, 0.0F, 0.10471975511965978F));
        PartDefinition ribbon_3_tip = ribbon_3.addOrReplaceChild("ribbon_3_tip", CubeListBuilder.create().texOffs(124, 0).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition ribbon_4 = pelvis.addOrReplaceChild("ribbon_4", CubeListBuilder.create().texOffs(0, 9).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 6.0F, 0.5F), PartPose.offsetAndRotation(2.0F, 1.5F, -1.25F, 0.0F, 0.0F, 0.20943951023931956F));
        PartDefinition ribbon_4_tip = ribbon_4.addOrReplaceChild("ribbon_4_tip", CubeListBuilder.create().texOffs(5, 9).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition torso = pelvis.addOrReplaceChild("torso", CubeListBuilder.create().texOffs(10, 9).addBox(-1.5F, -5.0F, -1.0F, 3.0F, 5.0F, 2.0F), PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition chest = torso.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(21, 9).addBox(-2.5F, -6.0F, -1.5F, 5.0F, 6.0F, 3.0F).texOffs(38, 9).addBox(-0.5F, -5.5F, -1.75F, 1.0F, 5.0F, 0.25F).texOffs(43, 9).addBox(-3.5F, -7.0F, -2.5F, 7.0F, 1.5F, 5.0F), PartPose.offset(0.0F, -5.0F, 0.0F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(68, 9).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(73, 9).addBox(-2.5F, -7.0F, -2.5F, 5.0F, 7.0F, 5.0F), PartPose.offset(0.0F, -1.5F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(94, 9).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offset(0.0F, -7.0F, 0.0F));
        PartDefinition horn_centre = hat.addOrReplaceChild("horn_centre", CubeListBuilder.create().texOffs(119, 9).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition horn_centre_tip = horn_centre.addOrReplaceChild("horn_centre_tip", CubeListBuilder.create().texOffs(0, 22).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(5, 22).addBox(-0.75F, -4.5F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition horn_right = hat.addOrReplaceChild("horn_right", CubeListBuilder.create().texOffs(14, 22).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(-2.0F, -1.0F, 0.0F, 0.0F, 0.0F, -0.6981317007977318F));
        PartDefinition horn_right_tip = horn_right.addOrReplaceChild("horn_right_tip", CubeListBuilder.create().texOffs(23, 22).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(28, 22).addBox(-0.75F, -4.5F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, -0.6981317007977318F));
        PartDefinition horn_left = hat.addOrReplaceChild("horn_left", CubeListBuilder.create().texOffs(37, 22).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F), PartPose.offsetAndRotation(2.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.6981317007977318F));
        PartDefinition horn_left_tip = horn_left.addOrReplaceChild("horn_left_tip", CubeListBuilder.create().texOffs(46, 22).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(51, 22).addBox(-0.75F, -4.5F, -0.75F, 1.5F, 1.5F, 1.5F), PartPose.offsetAndRotation(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.6981317007977318F));
        PartDefinition right_upper = chest.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(60, 22).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.5F, 3.0F).texOffs(73, 22).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 6.0F, 1.5F), PartPose.offset(-3.0F, -5.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(82, 22).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 6.0F, 1.5F).texOffs(91, 22).addBox(-1.25F, 4.5F, -1.25F, 2.5F, 1.5F, 2.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(104, 22).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(111, 22).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.75F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(116, 22).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(121, 22).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.25F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(0, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(5, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.25F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(10, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(15, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.75F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(20, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(25, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.5F, -1.0F, -0.4363323129985824F, 0.0F, 0.3490658503988659F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(30, 31).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition right_fan = chest.addOrReplaceChild("right_fan", CubeListBuilder.create().texOffs(35, 31).addBox(-6.0F, -12.0F, 0.0F, 6.0F, 12.0F, 1.0F), PartPose.offset(-1.0F, -4.0F, 1.5F));
        PartDefinition right_fan_outer = right_fan.addOrReplaceChild("right_fan_outer", CubeListBuilder.create().texOffs(50, 31).addBox(-5.0F, -9.0F, 0.0F, 5.0F, 10.0F, 1.0F), PartPose.offset(-6.0F, -8.0F, 0.0F));
        PartDefinition right_fan_tip = right_fan_outer.addOrReplaceChild("right_fan_tip", CubeListBuilder.create().texOffs(63, 31).addBox(-4.0F, -6.0F, 0.0F, 4.0F, 8.0F, 1.0F), PartPose.offset(-5.0F, -5.0F, 0.0F));
        PartDefinition left_upper = chest.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(74, 31).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.5F, 3.0F).texOffs(87, 31).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 6.0F, 1.5F), PartPose.offset(3.0F, -5.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(96, 31).addBox(-0.75F, 0.0F, -0.75F, 1.5F, 6.0F, 1.5F).texOffs(105, 31).addBox(-1.25F, 4.5F, -1.25F, 2.5F, 1.5F, 2.5F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(118, 31).addBox(-0.5F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(0, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.75F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(5, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(10, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, -0.25F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(15, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(20, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.25F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(25, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(30, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 3.0F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.75F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(35, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offset(0.0F, 3.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(40, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 0.5F, -1.0F, -0.4363323129985824F, 0.0F, -0.3490658503988659F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(45, 45).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.25F, 0.5F), PartPose.offset(0.0F, 2.0F, 0.0F));
        PartDefinition left_fan = chest.addOrReplaceChild("left_fan", CubeListBuilder.create().texOffs(50, 45).addBox(0.0F, -12.0F, 0.0F, 6.0F, 12.0F, 1.0F), PartPose.offset(1.0F, -4.0F, 1.5F));
        PartDefinition left_fan_outer = left_fan.addOrReplaceChild("left_fan_outer", CubeListBuilder.create().texOffs(65, 45).addBox(0.0F, -9.0F, 0.0F, 5.0F, 10.0F, 1.0F), PartPose.offset(6.0F, -8.0F, 0.0F));
        PartDefinition left_fan_tip = left_fan_outer.addOrReplaceChild("left_fan_tip", CubeListBuilder.create().texOffs(78, 45).addBox(0.0F, -6.0F, 0.0F, 4.0F, 8.0F, 1.0F), PartPose.offset(5.0F, -5.0F, 0.0F));
        PartDefinition train = chest.addOrReplaceChild("train", CubeListBuilder.create().texOffs(89, 45).addBox(-2.5F, -14.0F, 0.0F, 5.0F, 14.0F, 1.0F), PartPose.offset(0.0F, -4.0F, 2.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(ScarletMummerEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        root.getAllParts().forEach(ModelPart::resetPose);
        float move = Mth.clamp(limbSwingAmount, 0.0F, 1.0F);
        prance(limbSwing, move, age);
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD;
        head.xRot += Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD;
        float flare = entity.isPerforming() ? .55F + .08F * Mth.sin(age * .3F) : .0F;
        fanned(flare, age);
        swipe(attackTime);
        if (entity.ordealMove() >= 0) move(entity.ordealMove(), entity.ordealMoveAge(partialTick), age);
    }

    /** A prance: knees high, a bob on every step, the arms flourishing, a twitch of the head now and then. */
    private void prance(float limbSwing, float move, float age) {
        float stride = limbSwing * .6662F;
        root.y -= Math.abs(Mth.sin(stride)) * 1.2F * move + Mth.sin(age * .09F) * .3F;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot = swing * .9F * move - Math.max(0.0F, -swing) * .5F * move;
            shins[i].xRot = Math.max(0.0F, -swing) * 1.3F * move + .05F;
            uppers[i].xRot = -swing * .5F * move - .2F;
            uppers[i].zRot = -sign * (.35F + .15F * Mth.sin(age * .11F + i * 2.0F));
            fores[i].xRot = -.6F - .25F * Mth.sin(age * .13F + i);
            hands[i].zRot = sign * .3F * Mth.sin(age * .17F + i);
            curl(i, .25F + .2F * Mth.sin(age * .07F + i));
        }
        chest.yRot += Mth.cos(stride) * .15F * move;
        chest.xRot += .1F;
        float twitch = Mth.sin(age * .13F) > .93F ? .4F : 0.0F;
        head.zRot += Mth.sin(age * .9F) * .04F + twitch;
        for (int k = 0; k < 5; k++) {
            ribbons[k].xRot += .1F * move + .06F * Mth.sin(age * .1F + k);
            ribbonTips[k].xRot += .12F * Mth.sin(age * .12F + k * 1.3F) + .2F * move;
        }
    }

    /** The fans: folded down along its back at 0, every eye open behind its head at 1, flung past open above 1. */
    private void fanned(float open, float age) {
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            fans[i].xRot = Mth.lerp(Math.min(open, 1.0F), -1.35F, -.15F);
            fans[i].zRot = sign * Mth.lerp(Math.min(open, 1.0F), -.1F, .45F + .25F * Math.max(0.0F, open - 1.0F));
            fans[i].yRot = sign * Mth.lerp(Math.min(open, 1.0F), -.5F, .1F);
            outers[i].zRot = sign * Mth.lerp(Math.min(open, 1.0F), -.5F, .35F) + sign * .03F * Mth.sin(age * .2F + i);
            fanTips[i].zRot = sign * Mth.lerp(Math.min(open, 1.0F), -.6F, .35F);
        }
        train.xRot = Mth.lerp(Math.min(open, 1.0F), -1.2F, -.2F);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .08F);
            tips[i][f].zRot = sign * amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .5F;
    }

    /** Its ordinary blow: the right hand raked across in front of it, claws first. */
    private void swipe(float attack) {
        if (attack <= 0.0F) return;
        float rake = Mth.sin(attack * Mth.PI);
        uppers[0].xRot = Mth.lerp(rake, uppers[0].xRot, -1.7F);
        uppers[0].yRot += rake * .9F - attack * 1.2F;
        fores[0].xRot = Mth.lerp(rake, fores[0].xRot, -.2F);
        curl(0, Mth.lerp(rake, .25F, .05F));
        chest.yRot += -rake * .4F;
    }

    /** 0 until {@code length} ticks before {@code at}, then 1 at {@code at}, accelerating into the blow. */
    private static float snap(float since, float at, float length) {
        float t = Mth.clamp((since - (at - length)) / length, 0.0F, 1.0F);
        return t * t;
    }

    private void move(int move, float since, float age) {
        switch (move) {
            case ScarletMummerOrdealMoves.DAZZLE -> {
                // The fans rise slowly, every eye opening, tremble held at the top, then fling open past full at the blow
                // with its arms thrown wide and its back arched; they settle as it recovers.
                float w = StiltWalkerMoves.DAZZLE_WINDUP;
                float rise = HemoEasing.smoothstep(0.0F, w - 3.0F, since), open = snap(since, w, 2.0F);
                float hold = 1.0F - HemoEasing.smoothstep(w + 5.0F, w + 16.0F, since);
                float tremble = Mth.sin(since * 3.3F) * HemoEasing.smoothstep(w - 6.0F, w - 2.0F, since) * (1.0F - open);
                fanned((.85F * rise + .5F * open + .02F * tremble) * hold, age);
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(hold, uppers[i].zRot, -sign * Mth.lerp(open, .6F * rise, 1.8F));
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, -.3F);
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, -.2F);
                    curl(i, Mth.lerp(hold * open, .25F, 0.0F));
                }
                chest.xRot += hold * (.2F * rise * (1.0F - open) - .35F * open);
                head.xRot += hold * (.15F * rise * (1.0F - open) - .3F * open);
            }
            case ScarletMummerOrdealMoves.FEINT -> {
                // The quick step aside, a coil low, then the lunge: right arm thrust out straight, fingers first.
                float w = StiltWalkerMoves.FEINT_WINDUP;
                float step = HemoEasing.smoothstep(0.0F, 4.0F, since) * (1.0F - HemoEasing.smoothstep(4.0F, 7.0F, since));
                float coil = HemoEasing.smoothstep(3.0F, w - 1.0F, since), lunge = snap(since, w + 1.0F, 2.0F);
                float hold = 1.0F - HemoEasing.smoothstep(w + StiltWalkerMoves.FEINT_DASH + 3.0F, w + StiltWalkerMoves.FEINT_DASH + 14.0F, since);
                pelvis.zRot += .35F * step;
                thighs[0].zRot += .5F * step;
                thighs[1].zRot -= .5F * step;
                float low = coil * (1.0F - lunge) * hold;
                root.y += 2.5F * low;
                thighs[0].xRot = Mth.lerp(hold, thighs[0].xRot, Mth.lerp(lunge, -.9F * coil, -1.2F));
                shins[0].xRot = Mth.lerp(hold, shins[0].xRot, Mth.lerp(lunge, 1.2F * coil, .3F));
                thighs[1].xRot = Mth.lerp(hold, thighs[1].xRot, Mth.lerp(lunge, .3F * coil, .9F));
                uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, Mth.lerp(lunge, .6F * coil, -1.65F));
                uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, Mth.lerp(lunge, .2F, -.05F));
                fores[0].xRot = Mth.lerp(hold, fores[0].xRot, Mth.lerp(lunge, -1.4F * coil, 0.0F));
                curl(0, Mth.lerp(hold * lunge, .25F, 0.0F));
                uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, .7F * lunge);
                uppers[1].zRot = Mth.lerp(hold, uppers[1].zRot, .9F * lunge);
                chest.xRot += hold * (.3F * coil * (1.0F - lunge) + .45F * lunge);
                chest.yRot += hold * (-.4F * coil * (1.0F - lunge) + .3F * lunge);
            }
            case ScarletMummerOrdealMoves.WHIRL -> {
                // A crouch with its arms gathered in, then two whole turns with arms, fans and ribbons out like blades.
                float w = StiltWalkerMoves.WHIRL_WINDUP;
                float gather = HemoEasing.smoothstep(0.0F, w - 2.0F, since);
                float spin = HemoEasing.clamp01((since - w + 1.0F) / 9.0F);
                float out = HemoEasing.smoothstep(w - 2.0F, w, since) * (1.0F - HemoEasing.smoothstep(w + 9.0F, w + 16.0F, since));
                float hold = 1.0F - HemoEasing.smoothstep(w + 9.0F, w + 16.0F, since);
                root.y += 2.0F * gather * (1.0F - out) * hold;
                root.yRot += (-.6F * gather * (1.0F - spin) + Mth.TWO_PI * 2.0F * HemoEasing.smoothstep(spin)) * hold;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    uppers[i].zRot = Mth.lerp(hold, uppers[i].zRot, -sign * Mth.lerp(out, .2F, 1.55F));
                    uppers[i].xRot = Mth.lerp(hold, uppers[i].xRot, Mth.lerp(out, .5F * gather, 0.0F));
                    fores[i].xRot = Mth.lerp(hold, fores[i].xRot, Mth.lerp(out, -1.6F * gather, 0.0F));
                    curl(i, Mth.lerp(out, .5F, 0.0F));
                }
                for (int k = 0; k < 5; k++) {
                    ribbons[k].zRot += (k - 2) * .45F * out;
                    ribbons[k].xRot += -1.2F * out * ((k % 2) * 2 - 1);
                }
                fanned(Mth.lerp(out, 0.0F, .8F), age);
            }
            case ScarletMummerOrdealMoves.ENTRANCE -> {
                // The grand entrance: a deep crouch with the fans half open, a tucked leap, a landing spread wide.
                float w = StiltWalkerMoves.ENTRANCE_WINDUP, f = StiltWalkerMoves.ENTRANCE_FLIGHT;
                float crouch = HemoEasing.smoothstep(0.0F, w - 2.0F, since) * (1.0F - HemoEasing.smoothstep(w - 1.0F, w + 1.0F, since));
                float air = HemoEasing.smoothstep(w - 1.0F, w + 2.0F, since) * (1.0F - HemoEasing.smoothstep(w + f - 3.0F, w + f, since));
                float land = snap(since, w + f, 2.0F) * (1.0F - HemoEasing.smoothstep(w + f + 3.0F, w + f + 14.0F, since));
                root.y += 3.0F * crouch + 2.5F * land;
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    thighs[i].xRot += -1.1F * crouch - 1.4F * air - .8F * land;
                    shins[i].xRot += 1.6F * crouch + 1.8F * air + 1.0F * land;
                    uppers[i].xRot += -2.6F * air + .4F * crouch;
                    uppers[i].zRot += -sign * (1.4F * land + .3F * air);
                }
                chest.xRot += .4F * crouch - .2F * air + .45F * land;
                fanned(.5F * crouch + .3F * air + 1.2F * land, age);
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
