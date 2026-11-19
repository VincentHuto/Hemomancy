package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.client.render.AnimationTime;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules;
import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusRingmasterMoves.Move;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

// Authored by tools/circus/build_true_ringmaster.py; regenerate the model, atlas and BBModel together.
/**
 * The Ringmaster's true self, worn by his finale copy from the reveal on: the showman in his top hat and tailcoat, a
 * bone mask grinning under the brim, the coat open on his lit ribs, his own red strings running up into the dark. He
 * keeps fighting in it. At rest he stands leaning on the staff planted beside him, the free hand's fingers working;
 * walking, he strides with knees and ankles, the tails swinging and the staff swung like a cane. Each move replays the
 * server's synced move tick ({@link CircusRingmasterEntity#moveTime}) like his physical model's, but with the staff's
 * weight: the topper hauled back over the shoulder and cracked down in front of him. He presents himself through the
 * reveal (staff high, the free arm thrown wide), and at the kneel sinks onto one knee, the staff planted, the head bowed.
 */
public final class CircusTrueRingmasterModel extends EntityModel<CircusRingmasterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_ringmaster"), "main");
    /** The staff's own turn on the fist through a blow: the topper leads, behind the head raised, ahead of him landed. */
    private static final float STAFF_BLOW = 1.1F;
    /** How far the kneel lowers his hips, in pixels: the near thigh upright, its shin along the floor. */
    private static final float KNEEL_DROP = 8.5F;
    private final ModelPart root, pelvis, chest, neck, head, strings, staff, topper, core;
    private final ModelPart[] shoulders = new ModelPart[2], uppers = new ModelPart[2], fores = new ModelPart[2],
            hands = new ModelPart[2], thumbs = new ModelPart[2], thumbTips = new ModelPart[2], thighs = new ModelPart[2],
            shins = new ModelPart[2], feet = new ModelPart[2], tails = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][4], tips = new ModelPart[2][4];

    public CircusTrueRingmasterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        strings = chest.getChild("strings");
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
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
            tails[i] = pelvis.getChild(side + "_tail");
        }
        staff = hands[0].getChild("staff");
        topper = staff.getChild("topper");
        core = topper.getChild("core");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -2.5F, 9.0F, 4.0F, 5.0F), PartPose.offset(0.0F, -18.0F, 0.0F));
        PartDefinition buckle = pelvis.addOrReplaceChild("buckle", CubeListBuilder.create().texOffs(29, 0).addBox(-1.0F, -0.5F, -0.25F, 2.0F, 1.5F, 0.5F), PartPose.offset(0.0F, -2.5F, -2.75F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(36, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(-2.25F, 0.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(53, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F).texOffs(66, 0).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 5.0F, 4.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(83, 0).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 2.0F, 6.0F).texOffs(104, 0).addBox(-1.5F, 0.75F, -6.0F, 3.0F, 1.25F, 1.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition right_tail = pelvis.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(115, 0).addBox(-2.25F, 0.0F, -0.5F, 4.5F, 14.0F, 1.0F), PartPose.offsetAndRotation(-2.25F, 0.0F, 2.5F, 0.10471975511965978F, 0.0F, 0.05235987755982989F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(128, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F), PartPose.offset(2.25F, 0.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(145, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 8.0F, 3.0F).texOffs(158, 0).addBox(-2.0F, 3.0F, -2.0F, 4.0F, 5.0F, 4.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(175, 0).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 2.0F, 6.0F).texOffs(196, 0).addBox(-1.5F, 0.75F, -6.0F, 3.0F, 1.25F, 1.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition left_tail = pelvis.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(207, 0).addBox(-2.25F, 0.0F, -0.5F, 4.5F, 14.0F, 1.0F), PartPose.offsetAndRotation(2.25F, 0.0F, 2.5F, 0.10471975511965978F, 0.0F, -0.05235987755982989F));
        PartDefinition chest = pelvis.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(220, 0).addBox(-4.5F, -10.0F, -2.5F, 9.0F, 10.0F, 5.0F).texOffs(0, 16).addBox(-5.0F, -10.0F, -2.75F, 10.0F, 3.0F, 5.5F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition sternum = chest.addOrReplaceChild("sternum", CubeListBuilder.create().texOffs(33, 16).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 5.5F, 0.5F), PartPose.offset(0.0F, -7.0F, -2.5F));
        PartDefinition right_rib_0 = chest.addOrReplaceChild("right_rib_0", CubeListBuilder.create().texOffs(38, 16).addBox(-2.5F, 0.0F, -0.5F, 2.5F, 0.5F, 0.5F), PartPose.offsetAndRotation(-0.5F, -6.5F, -2.5F, 0.0F, 0.0F, -0.24434609527920614F));
        PartDefinition left_rib_0 = chest.addOrReplaceChild("left_rib_0", CubeListBuilder.create().texOffs(47, 16).addBox(0.0F, 0.0F, -0.5F, 2.5F, 0.5F, 0.5F), PartPose.offsetAndRotation(0.5F, -6.5F, -2.5F, 0.0F, 0.0F, 0.24434609527920614F));
        PartDefinition right_rib_1 = chest.addOrReplaceChild("right_rib_1", CubeListBuilder.create().texOffs(56, 16).addBox(-2.5F, 0.0F, -0.5F, 2.5F, 0.5F, 0.5F), PartPose.offsetAndRotation(-0.5F, -5.0F, -2.5F, 0.0F, 0.0F, -0.24434609527920614F));
        PartDefinition left_rib_1 = chest.addOrReplaceChild("left_rib_1", CubeListBuilder.create().texOffs(65, 16).addBox(0.0F, 0.0F, -0.5F, 2.5F, 0.5F, 0.5F), PartPose.offsetAndRotation(0.5F, -5.0F, -2.5F, 0.0F, 0.0F, 0.24434609527920614F));
        PartDefinition right_rib_2 = chest.addOrReplaceChild("right_rib_2", CubeListBuilder.create().texOffs(74, 16).addBox(-2.5F, 0.0F, -0.5F, 2.5F, 0.5F, 0.5F), PartPose.offsetAndRotation(-0.5F, -3.5F, -2.5F, 0.0F, 0.0F, -0.24434609527920614F));
        PartDefinition left_rib_2 = chest.addOrReplaceChild("left_rib_2", CubeListBuilder.create().texOffs(83, 16).addBox(0.0F, 0.0F, -0.5F, 2.5F, 0.5F, 0.5F), PartPose.offsetAndRotation(0.5F, -3.5F, -2.5F, 0.0F, 0.0F, 0.24434609527920614F));
        PartDefinition right_lapel = chest.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(92, 16).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 7.0F, 0.5F), PartPose.offsetAndRotation(-3.0F, -7.0F, -2.75F, 0.0F, 0.0F, 0.13962634015954636F));
        PartDefinition left_lapel = chest.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(99, 16).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 7.0F, 0.5F), PartPose.offsetAndRotation(3.0F, -7.0F, -2.75F, 0.0F, 0.0F, -0.13962634015954636F));
        PartDefinition collar = chest.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(106, 16).addBox(-5.0F, -5.0F, -0.5F, 10.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -10.0F, 1.75F, -0.24434609527920614F, 0.0F, 0.0F));
        PartDefinition right_collar = chest.addOrReplaceChild("right_collar", CubeListBuilder.create().texOffs(129, 16).addBox(-0.5F, -4.0F, -2.0F, 1.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(-4.75F, -10.0F, 0.0F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition left_collar = chest.addOrReplaceChild("left_collar", CubeListBuilder.create().texOffs(140, 16).addBox(-0.5F, -4.0F, -2.0F, 1.0F, 4.0F, 4.0F), PartPose.offsetAndRotation(4.75F, -10.0F, 0.0F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(151, 16).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 4.0F), PartPose.offset(0.0F, -10.0F, -0.5F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(168, 16).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -1.5F, 0.0F));
        PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(201, 16).addBox(-6.0F, -1.0F, -6.0F, 12.0F, 1.0F, 12.0F).texOffs(0, 33).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 9.0F, 8.0F).texOffs(33, 33).addBox(-4.5F, -11.5F, -4.5F, 9.0F, 1.5F, 9.0F).texOffs(70, 33).addBox(-4.25F, -3.5F, -4.25F, 8.5F, 2.0F, 8.5F), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, -0.06981317007977318F, 0.0F, -0.08726646259971647F));
        PartDefinition brooch = hat.addOrReplaceChild("brooch", CubeListBuilder.create().texOffs(107, 33).addBox(-1.0F, -1.0F, -0.5F, 2.0F, 2.0F, 0.5F), PartPose.offset(0.0F, -2.5F, -4.25F));
        PartDefinition strings = chest.addOrReplaceChild("strings", CubeListBuilder.create().texOffs(114, 33).addBox(-4.0F, -56.0F, -0.25F, 0.5F, 56.0F, 0.5F).texOffs(119, 33).addBox(3.5F, -56.0F, -0.25F, 0.5F, 56.0F, 0.5F).texOffs(124, 33).addBox(-0.25F, -60.0F, 2.0F, 0.5F, 58.0F, 0.5F), PartPose.offset(0.0F, -9.0F, 0.0F));
        PartDefinition right_shoulder = chest.addOrReplaceChild("right_shoulder", CubeListBuilder.create().texOffs(129, 33).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 1.5F, 5.0F).texOffs(150, 33).addBox(-2.75F, 0.0F, -2.75F, 5.5F, 1.5F, 5.5F), PartPose.offset(-4.5F, -9.0F, 0.0F));
        PartDefinition right_upper = right_shoulder.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(175, 33).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.5F, 3.0F), PartPose.offset(-0.75F, 0.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(188, 33).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 5.5F, 2.5F).texOffs(201, 33).addBox(-1.75F, 3.5F, -1.75F, 3.5F, 2.0F, 3.5F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(218, 33).addBox(-0.75F, 0.0F, -2.0F, 1.5F, 2.5F, 4.0F), PartPose.offset(0.0F, 5.5F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(231, 33).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 2.5F, -1.5F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(236, 33).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(241, 33).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.75F, 1.0F), PartPose.offset(0.0F, 2.5F, -0.5F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(246, 33).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(251, 33).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.75F, 1.0F), PartPose.offset(0.0F, 2.5F, 0.5F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(0, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(5, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 2.5F, 1.5F));
        PartDefinition right_finger_3_tip = right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create().texOffs(10, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(15, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.75F, 0.5F, -1.75F, -0.4363323129985824F, 0.0F, 0.3490658503988659F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(20, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_shoulder = chest.addOrReplaceChild("left_shoulder", CubeListBuilder.create().texOffs(25, 93).addBox(-2.5F, -1.5F, -2.5F, 5.0F, 1.5F, 5.0F).texOffs(46, 93).addBox(-2.75F, 0.0F, -2.75F, 5.5F, 1.5F, 5.5F), PartPose.offset(4.5F, -9.0F, 0.0F));
        PartDefinition left_upper = left_shoulder.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(71, 93).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.5F, 3.0F), PartPose.offset(0.75F, 0.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(84, 93).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 5.5F, 2.5F).texOffs(97, 93).addBox(-1.75F, 3.5F, -1.75F, 3.5F, 2.0F, 3.5F), PartPose.offset(0.0F, 6.5F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(114, 93).addBox(-0.75F, 0.0F, -2.0F, 1.5F, 2.5F, 4.0F), PartPose.offset(0.0F, 5.5F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(127, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 2.5F, -1.5F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(132, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(137, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.75F, 1.0F), PartPose.offset(0.0F, 2.5F, -0.5F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(142, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(147, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.75F, 1.0F), PartPose.offset(0.0F, 2.5F, 0.5F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(152, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.75F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(157, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offset(0.0F, 2.5F, 1.5F));
        PartDefinition left_finger_3_tip = left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create().texOffs(162, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(167, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offsetAndRotation(-0.75F, 0.5F, -1.75F, -0.4363323129985824F, 0.0F, -0.3490658503988659F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(172, 93).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.25F, 1.0F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition staff = right_hand.addOrReplaceChild("staff", CubeListBuilder.create().texOffs(177, 93).addBox(-0.5F, -0.5F, -8.0F, 1.0F, 1.0F, 30.0F).texOffs(240, 93).addBox(-0.75F, -0.75F, -2.5F, 1.5F, 1.5F, 5.0F).texOffs(0, 125).addBox(-0.75F, -0.75F, 21.0F, 1.5F, 1.5F, 1.5F), PartPose.offset(1.25F, 1.75F, 0.0F));
        PartDefinition topper = staff.addOrReplaceChild("topper", CubeListBuilder.create().texOffs(9, 125).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 1.5F).texOffs(18, 125).addBox(-3.0F, -0.5F, -8.0F, 6.0F, 1.0F, 1.0F).texOffs(33, 125).addBox(-3.0F, -0.5F, -2.0F, 6.0F, 1.0F, 1.0F).texOffs(48, 125).addBox(-3.0F, -0.5F, -7.0F, 1.0F, 1.0F, 5.0F).texOffs(61, 125).addBox(2.0F, -0.5F, -7.0F, 1.0F, 1.0F, 5.0F), PartPose.offset(0.0F, 0.0F, -8.0F));
        PartDefinition core = topper.addOrReplaceChild("core", CubeListBuilder.create().texOffs(74, 125).addBox(-1.25F, -0.75F, -1.25F, 2.5F, 1.5F, 2.5F), PartPose.offsetAndRotation(0.0F, 0.0F, -4.5F, 0.0F, 0.7853981633974483F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusRingmasterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = AnimationTime.partialTick();
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.move(), entity.moveTime(partialTick),
                entity.finalePhase(), entity.finalePhaseTime(partialTick), entity.hasTopper());
    }

    /**
     * The whole pose: {@code moveTime} is the ticks into the running move, {@code phaseTime} the ticks into the
     * finale's phase.
     */
    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, Move move, float moveTime,
            FinaleRules.FinalePhase phase, float phaseTime, boolean hasTopper) {
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        topper.visible = hasTopper;
        float breath = Mth.sin(age * .05F);
        // A looming stoop, the chest rising and falling, the head cocked a little too far and drifting back.
        chest.xRot += .07F + breath * .015F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .3F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -35, 35) * Mth.DEG_TO_RAD;
        head.zRot += .08F + Mth.sin(age * .031F) * .05F;
        // The staff arm: forearm out before him, the fist closed on the shaft. The free hand hangs, its fingers working.
        uppers[0].xRot = -.25F + breath * .02F;
        uppers[0].zRot = .3F;
        fores[0].xRot = -1.15F;
        curl(0, 1.45F);
        uppers[1].xRot = .05F;
        uppers[1].zRot = -.14F;
        fores[1].xRot = -.35F;
        curl(1, .45F + .25F * Mth.sin(age * .09F) * Mth.sin(age * .023F));
        for (int i = 0; i < 2; i++) tails[i].xRot += Mth.sin(age * .05F + i) * .04F;
        // Walking: a long stride bending at the knee and ankle, the hips dipping, the tails swinging behind.
        float step = Mth.clamp(movement * 4.0F, 0.0F, 1.0F);
        float stride = limbSwing * .6662F;
        pelvis.y += Math.abs(Mth.sin(stride)) * .7F * step;
        for (int i = 0; i < 2; i++) {
            float swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot += swing * .6F * step;
            shins[i].xRot += Math.max(0.0F, Mth.sin(stride + i * Mth.PI)) * .9F * step;
            feet[i].xRot = -(thighs[i].xRot + shins[i].xRot) * .6F;
            tails[i].xRot += Math.max(0.0F, -swing) * .45F * step + .15F * step;
        }
        uppers[1].xRot += Mth.cos(stride) * .4F * step;
        float cane = -Mth.cos(stride) * .35F * step;
        pelvis.yRot += Mth.cos(stride) * .06F * step;
        float blow = 0.0F;
        BossMoveTimeline timeline = move.timeline();
        if (timeline != null) {
            float commit = timeline.commitTick(), total = timeline.total();
            switch (move) {
                case KNIFE_THROW -> throwKnife(moveTime, commit, total);
                case STAFF_STRIKE -> blow = staffStrike(moveTime, commit, total);
                case SPOTLIGHT -> spotlight(moveTime, commit, total, age);
                case CUE -> blow = cue(moveTime, commit, total);
                case STRINGS_CUT -> pullStrings(moveTime, commit, total);
                default -> {
                }
            }
        }
        if (phase == FinaleRules.FinalePhase.REVEAL) present(phaseTime, age);
        if (phase == FinaleRules.FinalePhase.KNEEL || phase == FinaleRules.FinalePhase.ENDED) {
            blow = 0.0F;
            kneel(HemoEasing.smoothstep(0.0F, FinaleRules.KNEEL_SETTLE_TICKS, phaseTime), breath);
        }
        // The staff stands plumb in the fist (planted, or swung like a cane) except through a blow, when it rides the arm.
        Vector3f plumb = local(new Vector3f(0.0F, Mth.cos(cane), -Mth.sin(cane)), pelvis, chest, shoulders[0], uppers[0],
                fores[0], hands[0]);
        staff.xRot = Mth.lerp(blow, (float) -Math.asin(Mth.clamp(plumb.y, -1.0F, 1.0F)), STAFF_BLOW);
        staff.yRot = Mth.lerp(blow, (float) Mth.atan2(plumb.x, plumb.z), 0.0F);
        float pulse = 1.0F + Mth.sin(age * .11F) * .06F;
        core.xScale = core.yScale = core.zScale = pulse;
        // His own strings hang plumb from the rigging whatever his body does, swaying a little.
        Vector3f down = local(new Vector3f(Mth.sin(age * .02F) * .04F, 1.0F, Mth.cos(age * .017F) * .04F).normalize(),
                pelvis, chest);
        strings.xRot = (float) Math.acos(Mth.clamp(down.y, -1.0F, 1.0F));
        strings.yRot = (float) Mth.atan2(down.x, down.z);
    }

    /** A model-space direction in the frame of the last of {@code chain} (root first), undoing each joint's turn. */
    private static Vector3f local(Vector3f direction, ModelPart... chain) {
        Quaternionf turn = new Quaternionf();
        for (ModelPart part : chain) turn.mul(new Quaternionf().rotationZYX(part.zRot, part.yRot, part.xRot));
        return turn.conjugate().transform(direction);
    }

    private void curl(int i, float amount) {
        float sign = i == 0 ? -1 : 1;
        for (int f = 0; f < 4; f++) {
            fingers[i][f].zRot = sign * amount * (1.0F + f * .06F);
            tips[i][f].zRot = sign * amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .4F;
        thumbTips[i].zRot = sign * amount * .6F;
    }

    /** 0 until {@code lead} ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float drop(float since, float at, float lead) {
        float t = Mth.clamp((since - (at - lead)) / lead, 0.0F, 1.0F);
        return t * t;
    }

    /**
     * The staff strike: the topper hauled back over his shoulder through the wind-up, the body rearing, a tremble at the
     * top, then cracked down in front of him on the commit tick, the body pitched into it, a hold, and the recovery.
     * Returns how fully the staff rides the arm.
     */
    private float staffStrike(float ticks, float commit, float total) {
        float raise = HemoEasing.smoothstep(0.0F, commit - 1.0F, ticks);
        float down = drop(ticks, commit, 2.0F);
        float tension = HemoEasing.smoothstep(commit - 4.0F, commit - 2.0F, ticks) * (1.0F - down);
        float tremble = Mth.sin(ticks * 3.1F) * tension;
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, Mth.lerp(raise, -1.0F, -3.0F) + .04F * tremble, -.7F));
        uppers[0].zRot = Mth.lerp(weight, uppers[0].zRot, -.1F);
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, -.4F, -.15F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(down, -.9F * raise, .4F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(down, -.5F, -.25F));
        chest.xRot += weight * Mth.lerp(down, -.22F * raise, .32F);
        head.xRot += weight * Mth.lerp(down, -.2F * raise, .15F);
        // He steps into the blow: the right foot forward, the left knee bending.
        thighs[0].xRot += weight * Mth.lerp(down, .1F, -.5F);
        shins[0].xRot += weight * down * .3F;
        thighs[1].xRot += weight * down * .25F;
        shins[1].xRot += weight * down * .35F;
        for (int i = 0; i < 2; i++) feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
        return weight;
    }

    /** The free arm drawn back behind the shoulder through the wind-up, then whipped out and down as the knife leaves. */
    private void throwKnife(float ticks, float commit, float total) {
        float wind = HemoEasing.smoothstep(0.0F, commit * .6F, ticks);
        float whip = drop(ticks, commit, 2.0F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(whip, Mth.lerp(wind, -.4F, -2.8F), -.8F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(whip, -.35F, .2F));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(whip, -1.3F * wind, -.1F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(whip, 1.2F, .05F)));
        chest.yRot += weight * Mth.lerp(whip, .35F * wind, -.3F);
        head.yRot -= weight * Mth.lerp(whip, .2F * wind, -.15F);
    }

    /** The staff lifted high and held for the whole of the light, the free hand pointing down at the lit pool. */
    private void spotlight(float ticks, float commit, float total, float age) {
        float hold = HemoEasing.smoothstep(0.0F, commit * .6F, ticks) * (1.0F - HemoEasing.smoothstep(total - 6.0F, total, ticks));
        float sway = Mth.sin(age * .15F) * .05F;
        uppers[0].xRot = Mth.lerp(hold, uppers[0].xRot, -2.8F + sway);
        uppers[0].zRot = Mth.lerp(hold, uppers[0].zRot, .2F);
        fores[0].xRot = Mth.lerp(hold, fores[0].xRot, -.2F);
        uppers[1].xRot = Mth.lerp(hold, uppers[1].xRot, -1.2F);
        uppers[1].yRot = Mth.lerp(hold, uppers[1].yRot, -.3F);
        fores[1].xRot = Mth.lerp(hold, fores[1].xRot, -.1F);
        curl(1, Mth.lerp(hold, .45F, .1F));
        head.xRot += .25F * hold;
    }

    /** The Cue: the staff raised like a baton through the count, the free arm high, then brought down hard on the beat. */
    private float cue(float ticks, float commit, float total) {
        float count = HemoEasing.smoothstep(0.0F, commit - 1.0F, ticks);
        float down = drop(ticks, commit, 1.5F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 2.0F, total, ticks));
        uppers[0].xRot = Mth.lerp(weight, uppers[0].xRot, Mth.lerp(down, Mth.lerp(count, -1.0F, -3.0F), -.5F));
        fores[0].xRot = Mth.lerp(weight, fores[0].xRot, Mth.lerp(down, -.3F, -.2F));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(down, -2.4F * count, -.6F));
        uppers[1].zRot = Mth.lerp(weight, uppers[1].zRot, Mth.lerp(down, -.9F, -.3F));
        curl(1, Mth.lerp(weight, .45F, .05F));
        head.xRot += weight * Mth.lerp(down, -.3F * count, .1F);
        chest.xRot += weight * down * .28F;
        return weight;
    }

    /** Strings Cut: both fists close and haul back on a thread through the wind-up, then the borrowed act lands. */
    private void pullStrings(float ticks, float commit, float total) {
        float haul = HemoEasing.smoothstep(0.0F, commit, ticks);
        float release = drop(ticks, commit + 1.5F, 1.5F);
        float weight = HemoEasing.smoothstep(0.0F, 3.0F, ticks) * (1.0F - HemoEasing.smoothstep(commit + 3.0F, total, ticks));
        uppers[1].xRot = Mth.lerp(weight, uppers[1].xRot, Mth.lerp(release, -1.5F + .4F * haul, -1.9F));
        uppers[1].yRot = Mth.lerp(weight, uppers[1].yRot, -.5F * (1.0F - release));
        fores[1].xRot = Mth.lerp(weight, fores[1].xRot, Mth.lerp(release, -1.2F * haul, -.1F));
        curl(1, Mth.lerp(weight, .45F, Mth.lerp(release, 1.5F, .1F)));
        chest.xRot += weight * Mth.lerp(release, -.25F * haul, .3F);
    }

    /** The reveal: the staff raised high, the free arm thrown wide to the stands, the head back. */
    private void present(float ticks, float age) {
        float on = HemoEasing.smoothstep(0.0F, 14.0F, ticks)
                * (1.0F - HemoEasing.smoothstep(FinaleRules.REVEAL_TICKS - 12.0F, FinaleRules.REVEAL_TICKS, ticks));
        float sway = Mth.sin(age * .07F);
        uppers[0].xRot = Mth.lerp(on, uppers[0].xRot, -2.6F + .05F * sway);
        uppers[0].zRot = Mth.lerp(on, uppers[0].zRot, .25F);
        fores[0].xRot = Mth.lerp(on, fores[0].xRot, -.3F);
        uppers[1].xRot = Mth.lerp(on, uppers[1].xRot, -.4F);
        uppers[1].zRot = Mth.lerp(on, uppers[1].zRot, -1.25F + .06F * sway);
        fores[1].xRot = Mth.lerp(on, fores[1].xRot, -.15F);
        curl(1, Mth.lerp(on, .45F, .1F));
        chest.xRot -= .15F * on;
        head.xRot -= .3F * on;
    }

    /** The kneel: onto the right knee, the left foot planted before him, the staff planted, the head bowed over it. */
    private void kneel(float k, float breath) {
        pelvis.y += KNEEL_DROP * k;
        pelvis.yRot *= 1.0F - k;
        thighs[0].xRot = Mth.lerp(k, thighs[0].xRot, .12F);
        shins[0].xRot = Mth.lerp(k, shins[0].xRot, 1.45F);
        feet[0].xRot = Mth.lerp(k, feet[0].xRot, -.25F);
        thighs[1].xRot = Mth.lerp(k, thighs[1].xRot, -1.45F);
        thighs[1].zRot = Mth.lerp(k, thighs[1].zRot, -.08F);
        shins[1].xRot = Mth.lerp(k, shins[1].xRot, 1.42F);
        feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
        for (int i = 0; i < 2; i++) tails[i].xRot = Mth.lerp(k, tails[i].xRot, .55F);
        chest.xRot = Mth.lerp(k, chest.xRot, .32F + breath * .03F);
        chest.yRot *= 1.0F - k;
        head.xRot = Mth.lerp(k, head.xRot, .65F);
        head.yRot *= 1.0F - k;
        neck.yRot *= 1.0F - k;
        uppers[0].xRot = Mth.lerp(k, uppers[0].xRot, -.55F);
        uppers[0].zRot = Mth.lerp(k, uppers[0].zRot, .05F);
        fores[0].xRot = Mth.lerp(k, fores[0].xRot, -.95F);
        // The free hand on the raised knee.
        uppers[1].xRot = Mth.lerp(k, uppers[1].xRot, -.75F);
        uppers[1].zRot = Mth.lerp(k, uppers[1].zRot, -.05F);
        uppers[1].yRot = Mth.lerp(k, uppers[1].yRot, 0.0F);
        fores[1].xRot = Mth.lerp(k, fores[1].xRot, -.2F);
        curl(1, Mth.lerp(k, .45F, .9F));
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
