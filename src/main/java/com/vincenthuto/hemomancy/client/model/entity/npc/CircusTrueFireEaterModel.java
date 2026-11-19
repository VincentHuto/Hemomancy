package com.vincenthuto.hemomancy.client.model.entity.npc;

import org.joml.Vector4f;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterChoreography;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterOrdealGeometry;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.util.HemoEasing;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

// Authored by tools/circus/build_true_fire_eater.py; regenerate the model, atlas and BBModel together.
/**
 * The Fire Breather's true self: the masked performer made a flue, standing on the pulpit past the ring and breathing
 * into it. At rest they stand square to the ring with the torch held up by the mouth, breathing slowly, the flues
 * smoking, the head following the fighter. Each intervention is a gesture ({@link CircusFireEaterEntity#gesture}) timed
 * from its wind-up, every one a slow fill, a fast loosing and a heavy recovery: the long breath (head back, chest
 * filled, then thrown forward over the torch), the spat embers (three sharp snaps of the head), the ring of fire (arms
 * raised wide, flung down and out as it lights, then drawn in as it closes) and the cinder rain (a crouch, then flung up
 * at the sky). The breath itself has a body ({@link #renderBreath}): eighteen flame parts laid along its path from the
 * mouth, drawn as light by the renderer.
 */
public final class CircusTrueFireEaterModel extends EntityModel<CircusFireEaterEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_true_fire_eater"), "main");
    private static final int FLAMES = 18;
    /** A blow's hit-stop and recovery, in ticks. */
    private static final float HELD = 6.0F, RECOVER = 18.0F;
    private final ModelPart root, pelvis, chest, neck, head, jaw, torch;
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], tails = new ModelPart[2], skirts = new ModelPart[2], flues = new ModelPart[2],
            uppers = new ModelPart[2], fores = new ModelPart[2], hands = new ModelPart[2], thumbs = new ModelPart[2];
    private final ModelPart[][] fingers = new ModelPart[2][3], tips = new ModelPart[2][3];
    private final ModelPart[] flames = new ModelPart[FLAMES];
    /** This frame's body (to bring world points into model space) and its breath: where each flame is, and how big. */
    private Vec3 at = Vec3.ZERO;
    private float bodyYaw, age;
    private final Vec3[] flameAt = new Vec3[FLAMES];
    private final float[] flameSize = new float[FLAMES];

    public CircusTrueFireEaterModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "right" : "left";
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            tails[i] = pelvis.getChild(side + "_tail");
            skirts[i] = pelvis.getChild(side + "_skirt");
            flues[i] = head.getChild(side + "_flue");
            uppers[i] = chest.getChild(side + "_upper");
            fores[i] = uppers[i].getChild(side + "_fore");
            hands[i] = fores[i].getChild(side + "_hand");
            thumbs[i] = hands[i].getChild(side + "_thumb");
            for (int f = 0; f < 3; f++) {
                fingers[i][f] = hands[i].getChild(side + "_finger_" + f);
                tips[i][f] = fingers[i][f].getChild(side + "_finger_" + f + "_tip");
            }
        }
        torch = hands[0].getChild("torch");
        for (int k = 0; k < FLAMES; k++) {
            flames[k] = bakedRoot.getChild("flame_" + k);
            flameAt[k] = Vec3.ZERO;
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -2.5F, 9.0F, 4.0F, 5.0F).texOffs(29, 0).addBox(-4.75F, -1.0F, -2.75F, 9.5F, 1.5F, 5.5F), PartPose.offset(0.0F, -20.0F, 0.0F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(62, 0).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 10.0F, 3.5F), PartPose.offset(-2.25F, 1.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(79, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F).texOffs(92, 0).addBox(-1.75F, 5.0F, -1.75F, 3.5F, 4.0F, 3.5F).texOffs(109, 0).addBox(-1.75F, 8.0F, -3.25F, 3.5F, 1.0F, 1.5F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition right_tail = pelvis.addOrReplaceChild("right_tail", CubeListBuilder.create().texOffs(122, 0).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 15.0F, 1.0F), PartPose.offsetAndRotation(-2.25F, 0.0F, 2.5F, 0.13962634015954636F, 0.0F, 0.06981317007977318F));
        PartDefinition right_skirt = pelvis.addOrReplaceChild("right_skirt", CubeListBuilder.create().texOffs(133, 0).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(-3.0F, 0.0F, -2.5F, -0.10471975511965978F, 0.0F, 0.08726646259971647F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(142, 0).addBox(-1.75F, 0.0F, -1.75F, 3.5F, 10.0F, 3.5F), PartPose.offset(2.25F, 1.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(159, 0).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 6.0F, 3.0F).texOffs(172, 0).addBox(-1.75F, 5.0F, -1.75F, 3.5F, 4.0F, 3.5F).texOffs(189, 0).addBox(-1.75F, 8.0F, -3.25F, 3.5F, 1.0F, 1.5F), PartPose.offset(0.0F, 10.0F, 0.0F));
        PartDefinition left_tail = pelvis.addOrReplaceChild("left_tail", CubeListBuilder.create().texOffs(202, 0).addBox(-2.0F, 0.0F, -0.5F, 4.0F, 15.0F, 1.0F), PartPose.offsetAndRotation(2.25F, 0.0F, 2.5F, 0.13962634015954636F, 0.0F, -0.06981317007977318F));
        PartDefinition left_skirt = pelvis.addOrReplaceChild("left_skirt", CubeListBuilder.create().texOffs(213, 0).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 11.0F, 1.0F), PartPose.offsetAndRotation(3.0F, 0.0F, -2.5F, -0.10471975511965978F, 0.0F, -0.08726646259971647F));
        PartDefinition chest = pelvis.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(222, 0).addBox(-4.5F, -12.0F, -2.75F, 9.0F, 12.0F, 5.5F).texOffs(0, 19).addBox(-3.0F, -11.0F, -3.25F, 6.0F, 8.0F, 0.5F).texOffs(15, 19).addBox(-2.5F, -10.5F, -3.5F, 5.0F, 7.0F, 0.5F).texOffs(28, 19).addBox(-5.5F, -12.5F, -3.0F, 11.0F, 2.0F, 6.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(63, 19).addBox(-1.5F, -3.0F, -1.5F, 3.0F, 3.0F, 3.0F), PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition throat_frame = neck.addOrReplaceChild("throat_frame", CubeListBuilder.create().texOffs(76, 19).addBox(-2.25F, -0.5F, -2.25F, 4.5F, 1.0F, 4.5F), PartPose.offset(0.0F, -1.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(97, 19).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, -3.0F, 0.0F));
        PartDefinition band = head.addOrReplaceChild("band", CubeListBuilder.create().texOffs(130, 19).addBox(-4.25F, 0.0F, -4.25F, 8.5F, 1.0F, 8.5F), PartPose.offset(0.0F, -6.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(167, 19).addBox(-2.5F, 0.0F, -4.25F, 5.0F, 1.5F, 4.0F), PartPose.offset(0.0F, -1.5F, 0.0F));
        PartDefinition right_flue = head.addOrReplaceChild("right_flue", CubeListBuilder.create().texOffs(186, 19).addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 6.0F).texOffs(203, 19).addBox(-1.0F, -4.5F, 5.0F, 2.0F, 4.5F, 2.0F).texOffs(212, 19).addBox(-1.25F, -6.0F, 4.75F, 2.5F, 1.5F, 2.5F), PartPose.offsetAndRotation(-2.75F, -7.0F, 0.0F, 0.3141592653589793F, -0.24434609527920614F, 0.0F));
        PartDefinition right_upper = chest.addOrReplaceChild("right_upper", CubeListBuilder.create().texOffs(225, 19).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 9.0F, 2.5F).texOffs(238, 19).addBox(-1.75F, -1.0F, -1.75F, 3.5F, 2.5F, 3.5F), PartPose.offset(-5.5F, -11.0F, 0.0F));
        PartDefinition right_fore = right_upper.addOrReplaceChild("right_fore", CubeListBuilder.create().texOffs(0, 36).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 8.0F, 2.5F).texOffs(13, 36).addBox(-1.5F, 4.5F, -1.5F, 3.0F, 3.5F, 3.0F), PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition right_hand = right_fore.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(26, 36).addBox(-1.0F, 0.0F, -1.25F, 2.0F, 2.5F, 2.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(37, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.5F), PartPose.offset(0.0F, 2.5F, -0.75F));
        PartDefinition right_finger_0_tip = right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create().texOffs(42, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.25F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(47, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition right_finger_1_tip = right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create().texOffs(52, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.25F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(57, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.75F));
        PartDefinition right_finger_2_tip = right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create().texOffs(62, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.25F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(67, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(0.75F, 0.5F, -1.25F, -0.3490658503988659F, 0.0F, 0.4363323129985824F));
        PartDefinition right_thumb_tip = right_thumb.addOrReplaceChild("right_thumb_tip", CubeListBuilder.create().texOffs(72, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_flue = head.addOrReplaceChild("left_flue", CubeListBuilder.create().texOffs(77, 36).addBox(-0.75F, -0.75F, 0.0F, 1.5F, 1.5F, 6.0F).texOffs(94, 36).addBox(-1.0F, -4.5F, 5.0F, 2.0F, 4.5F, 2.0F).texOffs(103, 36).addBox(-1.25F, -6.0F, 4.75F, 2.5F, 1.5F, 2.5F), PartPose.offsetAndRotation(2.75F, -7.0F, 0.0F, 0.3141592653589793F, 0.24434609527920614F, 0.0F));
        PartDefinition left_upper = chest.addOrReplaceChild("left_upper", CubeListBuilder.create().texOffs(116, 36).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 9.0F, 2.5F).texOffs(129, 36).addBox(-1.75F, -1.0F, -1.75F, 3.5F, 2.5F, 3.5F), PartPose.offset(5.5F, -11.0F, 0.0F));
        PartDefinition left_fore = left_upper.addOrReplaceChild("left_fore", CubeListBuilder.create().texOffs(146, 36).addBox(-1.25F, 0.0F, -1.25F, 2.5F, 8.0F, 2.5F).texOffs(159, 36).addBox(-1.5F, 4.5F, -1.5F, 3.0F, 3.5F, 3.0F), PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition left_hand = left_fore.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(172, 36).addBox(-1.0F, 0.0F, -1.25F, 2.0F, 2.5F, 2.5F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(183, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.5F), PartPose.offset(0.0F, 2.5F, -0.75F));
        PartDefinition left_finger_0_tip = left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create().texOffs(188, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.25F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(193, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.0F));
        PartDefinition left_finger_1_tip = left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create().texOffs(198, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.25F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(203, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.5F, 0.5F), PartPose.offset(0.0F, 2.5F, 0.75F));
        PartDefinition left_finger_2_tip = left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create().texOffs(208, 36).addBox(-0.5F, 0.0F, -0.25F, 1.0F, 1.25F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(213, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(-0.75F, 0.5F, -1.25F, -0.3490658503988659F, 0.0F, -0.4363323129985824F));
        PartDefinition left_thumb_tip = left_thumb.addOrReplaceChild("left_thumb_tip", CubeListBuilder.create().texOffs(218, 36).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.0F, 0.5F), PartPose.offset(0.0F, 1.5F, 0.0F));
        PartDefinition torch = right_hand.addOrReplaceChild("torch", CubeListBuilder.create().texOffs(223, 36).addBox(-0.5F, -6.0F, -0.5F, 1.0F, 9.0F, 1.0F).texOffs(228, 36).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 1.0F, 2.0F).texOffs(237, 36).addBox(-1.5F, -9.5F, -1.5F, 3.0F, 2.5F, 3.0F).texOffs(0, 49).addBox(-1.0F, -11.5F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(9, 49).addBox(-0.5F, -13.0F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 2.0F, -0.25F, 1.5707963267948966F, 0.0F, 0.0F));
        PartDefinition flame_0 = mesh.getRoot().addOrReplaceChild("flame_0", CubeListBuilder.create().texOffs(14, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_1 = mesh.getRoot().addOrReplaceChild("flame_1", CubeListBuilder.create().texOffs(39, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_2 = mesh.getRoot().addOrReplaceChild("flame_2", CubeListBuilder.create().texOffs(64, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_3 = mesh.getRoot().addOrReplaceChild("flame_3", CubeListBuilder.create().texOffs(89, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_4 = mesh.getRoot().addOrReplaceChild("flame_4", CubeListBuilder.create().texOffs(114, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_5 = mesh.getRoot().addOrReplaceChild("flame_5", CubeListBuilder.create().texOffs(139, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_6 = mesh.getRoot().addOrReplaceChild("flame_6", CubeListBuilder.create().texOffs(164, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_7 = mesh.getRoot().addOrReplaceChild("flame_7", CubeListBuilder.create().texOffs(189, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_8 = mesh.getRoot().addOrReplaceChild("flame_8", CubeListBuilder.create().texOffs(214, 49).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_9 = mesh.getRoot().addOrReplaceChild("flame_9", CubeListBuilder.create().texOffs(0, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_10 = mesh.getRoot().addOrReplaceChild("flame_10", CubeListBuilder.create().texOffs(25, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_11 = mesh.getRoot().addOrReplaceChild("flame_11", CubeListBuilder.create().texOffs(50, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_12 = mesh.getRoot().addOrReplaceChild("flame_12", CubeListBuilder.create().texOffs(75, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_13 = mesh.getRoot().addOrReplaceChild("flame_13", CubeListBuilder.create().texOffs(100, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_14 = mesh.getRoot().addOrReplaceChild("flame_14", CubeListBuilder.create().texOffs(125, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_15 = mesh.getRoot().addOrReplaceChild("flame_15", CubeListBuilder.create().texOffs(150, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_16 = mesh.getRoot().addOrReplaceChild("flame_16", CubeListBuilder.create().texOffs(175, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition flame_17 = mesh.getRoot().addOrReplaceChild("flame_17", CubeListBuilder.create().texOffs(200, 62).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CircusFireEaterEntity entity, float limbSwing, float limbSwingAmount, float age, float yaw,
            float pitch) {
        float partialTick = age - entity.tickCount;
        at = entity.getPosition(partialTick);
        bodyYaw = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        pose(limbSwing, limbSwingAmount, age, yaw, pitch, entity.gesture(), entity.gestureAge(partialTick), entity.gestureAt());
    }

    /** The whole pose; {@code gesture} is -1 at rest, {@code since} the ticks since its wind-up began. */
    public void pose(float limbSwing, float movement, float age, float yaw, float pitch, int gesture, float since, Vec3 target) {
        this.age = age;
        root.getAllParts().forEach(part -> {
            part.resetPose();
            part.xScale = part.yScale = part.zScale = 1.0F;
        });
        java.util.Arrays.fill(flameSize, 0.0F);
        float breath = Mth.sin(age * .06F);
        float walk = Mth.clamp(movement, 0.0F, 1.0F), stride = limbSwing * .6662F;
        chest.xRot += .06F + breath * .025F;
        neck.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .25F;
        head.yRot += Mth.clamp(yaw, -70, 70) * Mth.DEG_TO_RAD * .75F;
        head.xRot += Mth.clamp(pitch, -40, 40) * Mth.DEG_TO_RAD;
        jaw.xRot += .06F + Math.max(0.0F, breath) * .05F;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1, swing = Mth.cos(stride + i * Mth.PI);
            thighs[i].xRot = swing * .6F * walk;
            shins[i].xRot = Math.max(0.0F, -swing) * .9F * walk;
            tails[i].xRot += Mth.sin(age * .07F + i * 1.3F) * .05F + walk * .25F;
            skirts[i].xRot += Math.min(0.0F, thighs[i].xRot) * .8F + Mth.sin(age * .08F + i) * .03F;
            flues[i].xRot += Mth.sin(age * .09F + i * 2.0F) * .04F;
            uppers[i].xRot = i == 0 ? -.55F : -.12F - swing * .25F * walk;
            uppers[i].zRot = -sign * (i == 0 ? .05F : .12F);
            fores[i].xRot = i == 0 ? -1.35F : -.3F;
            curl(i, i == 0 ? 1.35F : .45F);
        }
        if (gesture >= 0) gesture(gesture, since, target);
    }

    private void curl(int i, float amount) {
        for (int f = 0; f < 3; f++) {
            fingers[i][f].xRot = -amount * (1.0F + f * .05F);
            tips[i][f].xRot = -amount * 1.1F;
        }
        thumbs[i].xRot = thumbs[i].getInitialPose().xRot - amount * .5F;
    }

    /** 0 until two and a half ticks before {@code at}, then 1 at {@code at}, accelerating like a thrown thing. */
    private static float loose(float since, float at) {
        float t = Mth.clamp((since - (at - 2.5F)) / 2.5F, 0.0F, 1.0F);
        return t * t;
    }

    /** 1 until {@code end} and the hit-stop after it, falling to 0 over the recovery. */
    private static float held(float since, float end) {
        return 1.0F - HemoEasing.smoothstep(end + HELD, end + HELD + RECOVER, since);
    }

    private void gesture(int gesture, float since, Vec3 target) {
        switch (gesture) {
            case CircusFireEaterEntity.GESTURE_BREATH -> longBreath(since, target);
            case CircusFireEaterEntity.GESTURE_SPIT -> spit(since);
            case CircusFireEaterEntity.GESTURE_RING -> ring(since, target);
            case CircusFireEaterEntity.GESTURE_RAIN -> rain(since);
            default -> {
            }
        }
    }

    /** Head back and chest filled over the torch, a tremble at the top, then thrown forward into the breath. */
    private void longBreath(float since, Vec3 target) {
        float w = FireEaterChoreography.BREATH_IMPACT;
        float run = (float) Math.ceil(FireEaterOrdealGeometry.BREATH_LENGTH / FireEaterOrdealGeometry.BREATH_SPEED);
        float gather = HemoEasing.smoothstep(0.0F, w - 4.0F, since), down = loose(since, w), hold = held(since, w + run);
        float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - down);
        float fill = gather * (1.0F - down);
        chest.xRot += hold * (-.38F * fill + .5F * down);
        chest.xScale = chest.zScale = 1.0F + .1F * fill;
        neck.xRot += hold * (-.25F * fill + .15F * down);
        head.xRot += hold * (-.55F * fill + .02F * tremble + .3F * down);
        jaw.xRot += hold * (.12F * fill + .95F * down);
        // The torch brought up before the mouth; the free arm thrown back for balance as the breath goes.
        uppers[0].xRot = Mth.lerp(hold * gather, uppers[0].xRot, Mth.lerp(down, -2.2F, -1.55F));
        fores[0].xRot = Mth.lerp(hold * gather, fores[0].xRot, Mth.lerp(down, -.9F, -.35F));
        uppers[1].xRot = Mth.lerp(hold * gather, uppers[1].xRot, Mth.lerp(down, -.35F, .7F));
        uppers[1].zRot = Mth.lerp(hold * gather, uppers[1].zRot, -.5F);
        for (int i = 0; i < 2; i++) {
            thighs[i].xRot += -.3F * down * hold;
            shins[i].xRot += .45F * down * hold;
        }
        Vec3 mouth = mouth();
        if (since < w) {
            // The breath held in the mouth, swelling.
            held(mouth, .35F + .7F * gather);
            return;
        }
        float show = 1.0F - HemoEasing.smoothstep(w + run + 2.0F, w + run + 8.0F, since);
        if (show <= 0.0F) return;
        Vec3 start = model(target);
        Vec3 direction = target.subtract(at).multiply(1.0D, 0.0D, 1.0D).normalize();
        double front = FireEaterOrdealGeometry.breathFront((int) (since - w) + 1);
        Vec3 tip = model(target.add(direction.scale(front)).add(0.0D, .5D, 0.0D));
        lay(mouth, start, tip, .5F, 1.8F, show);
    }

    /** Head drawn back, then three sharp snaps of the head and jaw, one for each ember spat. */
    private void spit(float since) {
        float gather = HemoEasing.smoothstep(0.0F, 12.0F, since);
        float last = FireEaterChoreography.SPIT_IMPACT + 2 * FireEaterChoreography.SPIT_GAP;
        float hold = held(since, last);
        float snap = 0.0F;
        for (int i = 0; i < 3; i++) {
            float leaves = FireEaterChoreography.SPIT_IMPACT + i * FireEaterChoreography.SPIT_GAP - FireEaterChoreography.SPIT_FLIGHT;
            snap = Math.max(snap, loose(since, leaves) * (1.0F - HemoEasing.smoothstep(leaves + 1.0F, leaves + 5.0F, since)));
        }
        head.xRot += hold * (-.35F * gather * (1.0F - snap) + .45F * snap);
        neck.xRot += hold * .2F * snap;
        jaw.xRot += hold * (.2F * gather + .75F * snap);
        chest.xRot += hold * (-.1F * gather + .18F * snap);
        uppers[0].xRot = Mth.lerp(hold * gather, uppers[0].xRot, -1.95F);
        fores[0].xRot = Mth.lerp(hold * gather, fores[0].xRot, -.85F);
        if (snap > .05F) held(mouth(), .5F * snap);
    }

    /** Arms raised wide, flung down and out as the ring lights, then drawn in as it closes, and held. */
    private void ring(float since, Vec3 target) {
        float w = FireEaterChoreography.RING_IMPACT, close = FireEaterOrdealGeometry.RING_CLOSE_TICKS;
        float raise = HemoEasing.smoothstep(0.0F, w - 6.0F, since), down = loose(since, w);
        float hold = held(since, w + close + FireEaterChoreography.RING_HOLD);
        float closing = HemoEasing.smoothstep(w + 4.0F, w + close, since);
        float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - down);
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            float z = Mth.lerp(down, 2.7F + .05F * tremble, Mth.lerp(closing, 1.35F, .45F));
            float x = Mth.lerp(down, -.25F, Mth.lerp(closing, -.25F, -1.35F));
            uppers[i].zRot = Mth.lerp(hold * raise, uppers[i].zRot, -sign * z);
            uppers[i].xRot = Mth.lerp(hold * raise, uppers[i].xRot, x);
            fores[i].xRot = Mth.lerp(hold * raise, fores[i].xRot, Mth.lerp(closing, -.15F, -.75F));
            curl(i, i == 0 ? 1.35F : Mth.lerp(hold * raise, .45F, Mth.lerp(down, .1F, .9F)));
        }
        chest.xRot += hold * (-.22F * raise * (1.0F - down) + .3F * down);
        head.xRot += hold * (-.3F * raise * (1.0F - down) + .35F * down);
        jaw.xRot += hold * (.95F * down * (1.0F - HemoEasing.smoothstep(w + 6.0F, w + 14.0F, since)) + .25F * down);
        Vec3 mouth = mouth();
        if (since < w) {
            held(mouth, .25F + .45F * raise);
            return;
        }
        float show = 1.0F - HemoEasing.smoothstep(w + 6.0F, w + 10.0F, since);
        if (show <= 0.0F) return;
        Vec3 toward = at.subtract(target).multiply(1.0D, 0.0D, 1.0D).normalize();
        Vec3 rim = model(target.add(toward.scale(FireEaterOrdealGeometry.RING_OPEN)).add(0.0D, .4D, 0.0D));
        lay(mouth, rim, rim, .6F, 2.2F, show);
    }

    /** A crouch with the head bowed and the arms hugged in, then flung up at the sky as the cinders go up. */
    private void rain(float since) {
        float w = FireEaterChoreography.RAIN_IMPACT;
        float down = loose(since, w), hold = held(since, w + 10.0F);
        float crouch = HemoEasing.smoothstep(0.0F, w - 5.0F, since) * (1.0F - down) * hold;
        float flung = down * hold;
        float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 7.0F, w - 3.0F, since) * (1.0F - down);
        pelvis.y += 4.0F * crouch;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            thighs[i].xRot += -.7F * crouch;
            shins[i].xRot += 1.3F * crouch;
            uppers[i].xRot = Mth.lerp(Math.max(crouch, flung), uppers[i].xRot, Mth.lerp(down, -.7F + .04F * tremble, -2.9F));
            uppers[i].zRot = Mth.lerp(Math.max(crouch, flung), uppers[i].zRot, Mth.lerp(down, sign * .35F, -sign * .55F));
            fores[i].xRot = Mth.lerp(Math.max(crouch, flung), fores[i].xRot, Mth.lerp(down, -1.6F, -.2F));
        }
        chest.xRot += .45F * crouch - .45F * flung;
        head.xRot += .4F * crouch - 1.0F * flung;
        jaw.xRot += 1.05F * flung;
        Vec3 mouth = mouth();
        if (since < w) {
            held(mouth, .3F + .6F * crouch);
            return;
        }
        float show = 1.0F - HemoEasing.smoothstep(w + 6.0F, w + 10.0F, since);
        if (show <= 0.0F) return;
        // Up: model space's -y, as high as the cinders go before they fall.
        Vec3 sky = mouth.add(0.0D, -10.0D / FireEaterChoreography.SCALE * 16.0D, 0.0D);
        lay(mouth, mouth.lerp(sky, .5D), sky, .8F, 2.8F, show);
    }

    /** The breath held in the mouth: two flames turning there, swelling with the fill. */
    private void held(Vec3 mouth, float size) {
        flameAt[0] = mouth;
        flameAt[1] = mouth.add(0.0D, -.5D, -.5D);
        flameSize[0] = size;
        flameSize[1] = size * .6F;
    }

    /**
     * Lays the flames from the mouth along a sagging arc to {@code bend} (where the breath meets the floor) and on to
     * {@code tip}, growing from {@code from} to {@code to} in size, all at {@code show}.
     */
    private void lay(Vec3 mouth, Vec3 bend, Vec3 tip, float from, float to, float show) {
        // In model space up is -y: the arc rises a little out of the mouth before it falls.
        Vec3 control = mouth.lerp(bend, .5D).add(0.0D, -8.0D, 0.0D);
        double arc = mouth.distanceTo(control) + control.distanceTo(bend), run = bend.distanceTo(tip), total = arc + run;
        if (total < 1.0E-3D) return;
        for (int k = 0; k < FLAMES; k++) {
            double along = (k + .5D) / FLAMES * total;
            Vec3 point;
            if (along <= arc) {
                double t = along / arc;
                point = mouth.scale((1 - t) * (1 - t)).add(control.scale(2 * t * (1 - t))).add(bend.scale(t * t));
            } else {
                point = bend.lerp(tip, (along - arc) / Math.max(1.0E-3D, run));
            }
            float flicker = 1.0F + .15F * Mth.sin(age * 1.7F + k * 2.3F);
            flameAt[k] = point;
            flameSize[k] = Mth.lerp(k / (float) (FLAMES - 1), from, to) * flicker * show;
        }
    }

    /** The mouth, in model pixels: the jaw's front through this frame's pose. */
    private Vec3 mouth() {
        PoseStack stack = new PoseStack();
        root.translateAndRotate(stack);
        pelvis.translateAndRotate(stack);
        chest.translateAndRotate(stack);
        neck.translateAndRotate(stack);
        head.translateAndRotate(stack);
        jaw.translateAndRotate(stack);
        Vector4f point = new Vector4f(0.0F, .05F, -.3F, 1.0F).mul(stack.last().pose());
        return new Vec3(point.x * 16.0F, point.y * 16.0F, point.z * 16.0F);
    }

    /** A world point in this model's pixels: undoes the renderer's turn, flip, lift and the true self's scale. */
    private Vec3 model(Vec3 world) {
        Vec3 d = world.subtract(at).scale(1.0D / FireEaterChoreography.SCALE);
        double turn = -(180.0F - bodyYaw) * Mth.DEG_TO_RAD;
        double cos = Math.cos(turn), sin = Math.sin(turn);
        double x = d.x * cos + d.z * sin, z = -d.x * sin + d.z * cos;
        return new Vec3(-x * 16.0D, (-d.y + 1.501D) * 16.0D, z * 16.0D);
    }

    /** Draws this frame's breath: each flame turning where it lies, at its size; nothing when no breath shows. */
    public void renderBreath(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        for (int k = 0; k < FLAMES; k++) {
            float size = flameSize[k];
            if (size <= .01F) continue;
            ModelPart flame = flames[k];
            flame.setPos((float) flameAt[k].x, (float) flameAt[k].y, (float) flameAt[k].z);
            flame.setRotation(age * .35F + k * 1.7F, age * .5F + k, k * .9F);
            flame.xScale = flame.yScale = flame.zScale = size;
            flame.render(pose, consumer, light, overlay, color);
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
