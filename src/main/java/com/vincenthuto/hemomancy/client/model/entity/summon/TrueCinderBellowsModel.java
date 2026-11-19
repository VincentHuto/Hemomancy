package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.CinderBellowsEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import com.vincenthuto.hemomancy.common.encounter.ordeal.CinderBellowsOrdealMoves;
import com.vincenthuto.hemomancy.common.encounter.ordeal.FireEaterOrdealGeometry;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

// Authored by tools/circus/build_true_cinder_bellows.py (the Bellows' true self, 2026-11-17); regenerate the model, atlas and BBModel together.
public class TrueCinderBellowsModel extends EntityModel<CinderBellowsEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("true_cinder_bellows"), "main");
    private final ModelPart root, pelvis, abdomen, chest, neck, head, jaw, tailTip;
    private final ModelPart[] tail = new ModelPart[10];
    private final ModelPart[] lungs, arms, forearms, hands, hoods, ribs;
    private final ModelPart coil;

    public TrueCinderBellowsModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        abdomen = pelvis.getChild("abdomen");
        chest = abdomen.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        coil = root.getChild("coil");
        hoods = new ModelPart[]{neck.getChild("left_hood"), neck.getChild("right_hood")};
        ribs = new ModelPart[]{chest.getChild("left_rib_0"), chest.getChild("left_rib_1"), chest.getChild("left_rib_2"),
                chest.getChild("right_rib_0"), chest.getChild("right_rib_1"), chest.getChild("right_rib_2")};
        ModelPart parent = coil;
        for (int i = 0; i < tail.length; i++) {
            tail[i] = parent.getChild("tail_" + i);
            parent = tail[i];
        }
        tailTip = tail[9].getChild("tail_tip");
        lungs = new ModelPart[]{chest.getChild("left_lung"), chest.getChild("right_lung")};
        arms = new ModelPart[]{chest.getChild("left_arm"), chest.getChild("right_arm")};
        forearms = new ModelPart[]{arms[0].getChild("left_forearm"), arms[1].getChild("right_forearm")};
        hands = new ModelPart[]{forearms[0].getChild("left_hand"), forearms[1].getChild("right_hand")};
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition coil = root.addOrReplaceChild("coil", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -2.0F, 8.0F, 6.0F, 6.0F).texOffs(29, 0).addBox(-3.0F, -7.0F, -1.5F, 6.0F, 7.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -3.5F, 0.0F, 1.5707963267948966F, 0.0F));
        PartDefinition tail_0 = coil.addOrReplaceChild("tail_0", CubeListBuilder.create().texOffs(52, 0).addBox(-3.5F, -5.0F, -2.0F, 7.0F, 5.0F, 6.0F).texOffs(79, 0).addBox(-2.5F, -6.0F, -1.5F, 5.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_1 = tail_0.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(100, 0).addBox(-3.5F, -5.0F, -2.0F, 7.0F, 5.0F, 6.0F).texOffs(127, 0).addBox(-2.5F, -6.0F, -1.5F, 5.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(148, 0).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 4.0F, 6.0F).texOffs(173, 0).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(192, 0).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 4.0F, 6.0F).texOffs(217, 0).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_4 = tail_3.addOrReplaceChild("tail_4", CubeListBuilder.create().texOffs(0, 13).addBox(-2.5F, -3.0F, -2.0F, 5.0F, 3.0F, 6.0F).texOffs(23, 13).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_5 = tail_4.addOrReplaceChild("tail_5", CubeListBuilder.create().texOffs(40, 13).addBox(-2.5F, -3.0F, -2.0F, 5.0F, 3.0F, 6.0F).texOffs(63, 13).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_6 = tail_5.addOrReplaceChild("tail_6", CubeListBuilder.create().texOffs(80, 13).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 6.0F).texOffs(101, 13).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_7 = tail_6.addOrReplaceChild("tail_7", CubeListBuilder.create().texOffs(116, 13).addBox(-1.5F, -3.0F, -2.0F, 3.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_8 = tail_7.addOrReplaceChild("tail_8", CubeListBuilder.create().texOffs(135, 13).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.4188790204786391F, 0.0F));
        PartDefinition tail_9 = tail_8.addOrReplaceChild("tail_9", CubeListBuilder.create().texOffs(154, 13).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.3141592653589793F, 0.0F));
        PartDefinition tail_tip = tail_9.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(171, 13).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 3.0F).texOffs(180, 13).addBox(-0.5F, -2.0F, 2.0F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.5F, 0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(185, 13).addBox(-3.5F, -7.0F, -3.0F, 7.0F, 8.0F, 6.0F).texOffs(212, 13).addBox(-2.5F, -6.0F, -3.5F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, -3.0F, -0.24434609527920614F, 0.0F, 0.0F));
        PartDefinition abdomen = pelvis.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(225, 13).addBox(-3.0F, -7.0F, -2.5F, 6.0F, 7.0F, 5.0F).texOffs(0, 28).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 6.0F, 1.0F).texOffs(11, 28).addBox(-0.5F, -6.0F, 2.0F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.10471975511965978F, 0.0F, 0.0F));
        PartDefinition chest = abdomen.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(16, 28).addBox(-3.0F, -8.0F, 0.5F, 6.0F, 7.0F, 2.0F).texOffs(33, 28).addBox(-4.0F, -10.0F, -1.5F, 8.0F, 2.0F, 3.0F).texOffs(56, 28).addBox(-0.5F, -9.0F, 2.5F, 1.0F, 9.0F, 1.0F).texOffs(61, 28).addBox(-0.5F, -8.0F, -3.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.08726646259971647F, 0.0F, 0.0F));
        PartDefinition ruff = chest.addOrReplaceChild("ruff", CubeListBuilder.create().texOffs(66, 28).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offset(0.0F, -10.0F, 0.0F));
        PartDefinition ruff_fold = ruff.addOrReplaceChild("ruff_fold", CubeListBuilder.create().texOffs(91, 28).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.7853981633974483F, 0.0F));
        PartDefinition left_lung = chest.addOrReplaceChild("left_lung", CubeListBuilder.create().texOffs(116, 28).addBox(-1.5F, -3.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(131, 28).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(142, 28).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(157, 28).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(168, 28).addBox(-1.5F, 0.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(183, 28).addBox(-1.0F, 1.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(194, 28).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.0F, 4.0F), PartPose.offset(-1.75F, -4.0F, -0.5F));
        PartDefinition left_clavicle = chest.addOrReplaceChild("left_clavicle", CubeListBuilder.create().texOffs(209, 28).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-0.5F, -9.5F, -2.0F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition left_rib_0 = chest.addOrReplaceChild("left_rib_0", CubeListBuilder.create().texOffs(220, 28).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(231, 28).addBox(0.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -7.25F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_rib_1 = chest.addOrReplaceChild("left_rib_1", CubeListBuilder.create().texOffs(238, 28).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(249, 28).addBox(0.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -5.5F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_rib_2 = chest.addOrReplaceChild("left_rib_2", CubeListBuilder.create().texOffs(0, 39).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(11, 39).addBox(0.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -3.75F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_rib_3 = chest.addOrReplaceChild("left_rib_3", CubeListBuilder.create().texOffs(18, 39).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(29, 39).addBox(0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -2.0F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(34, 39).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(43, 39).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(-4.5F, -8.5F, 0.5F, 0.2617993877991494F, 0.0F, 0.20943951023931956F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(56, 39).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(65, 39).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -1.3089969389957472F, 0.0F, -0.10471975511965978F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(78, 39).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition left_fingers = left_hand.addOrReplaceChild("left_fingers", CubeListBuilder.create().texOffs(85, 39).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F).texOffs(90, 39).addBox(-0.25F, 0.0F, -0.5F, 0.5F, 2.5F, 0.5F).texOffs(95, 39).addBox(0.5F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(100, 39).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, 0.0F, 0.0F, 0.4363323129985824F));
        PartDefinition right_lung = chest.addOrReplaceChild("right_lung", CubeListBuilder.create().texOffs(105, 39).addBox(-1.5F, -3.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(120, 39).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(131, 39).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(146, 39).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(157, 39).addBox(-1.5F, 0.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(172, 39).addBox(-1.0F, 1.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(183, 39).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.0F, 4.0F), PartPose.offset(1.75F, -4.0F, -0.5F));
        PartDefinition right_clavicle = chest.addOrReplaceChild("right_clavicle", CubeListBuilder.create().texOffs(198, 39).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.5F, -9.5F, -2.0F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition right_rib_0 = chest.addOrReplaceChild("right_rib_0", CubeListBuilder.create().texOffs(209, 39).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(220, 39).addBox(-2.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -7.25F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_rib_1 = chest.addOrReplaceChild("right_rib_1", CubeListBuilder.create().texOffs(227, 39).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(238, 39).addBox(-2.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -5.5F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_rib_2 = chest.addOrReplaceChild("right_rib_2", CubeListBuilder.create().texOffs(245, 39).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(0, 48).addBox(-2.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -3.75F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_rib_3 = chest.addOrReplaceChild("right_rib_3", CubeListBuilder.create().texOffs(7, 48).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(18, 48).addBox(-1.5F, -0.5F, -4.0F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -2.0F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(23, 48).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(32, 48).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(4.5F, -8.5F, 0.5F, 0.2617993877991494F, 0.0F, -0.20943951023931956F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(45, 48).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(54, 48).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -1.3089969389957472F, 0.0F, 0.10471975511965978F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(67, 48).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_fingers = right_hand.addOrReplaceChild("right_fingers", CubeListBuilder.create().texOffs(74, 48).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F).texOffs(79, 48).addBox(-0.25F, 0.0F, -0.5F, 0.5F, 2.5F, 0.5F).texOffs(84, 48).addBox(0.5F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(89, 48).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, 0.0F, 0.0F, -0.4363323129985824F));
        PartDefinition torch = left_hand.addOrReplaceChild("torch", CubeListBuilder.create().texOffs(94, 48).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(99, 48).addBox(-1.5F, -8.0F, -1.5F, 3.0F, 3.0F, 3.0F).texOffs(112, 48).addBox(-1.0F, -10.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(121, 48).addBox(-0.5F, -11.5F, -0.5F, 1.0F, 1.5F, 1.0F), PartPose.offsetAndRotation(0.0F, 1.5F, -1.0F, 0.6283185307179586F, 0.0F, -0.3490658503988659F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(126, 48).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(135, 48).addBox(-0.5F, -5.0F, -1.75F, 1.0F, 5.0F, 1.0F).texOffs(140, 48).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 1.0F, 3.0F).texOffs(153, 48).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -10.0F, -0.5F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(166, 48).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(187, 48).addBox(-1.5F, -2.0F, -5.5F, 3.0F, 2.0F, 3.0F).texOffs(200, 48).addBox(-2.5F, -5.0F, -3.5F, 5.0F, 1.0F, 1.0F).texOffs(213, 48).addBox(-3.0F, -2.5F, -2.5F, 1.0F, 1.0F, 2.0F).texOffs(220, 48).addBox(2.0F, -2.5F, -2.5F, 1.0F, 1.0F, 2.0F).texOffs(227, 48).addBox(-0.5F, -6.0F, -1.5F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.2617993877991494F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(238, 48).addBox(-1.5F, 0.0F, -7.0F, 3.0F, 1.0F, 6.0F).texOffs(0, 59).addBox(-1.0F, -1.0F, -6.75F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.10471975511965978F, 0.0F, 0.0F));
        PartDefinition left_hood = neck.addOrReplaceChild("left_hood", CubeListBuilder.create().texOffs(7, 59).addBox(-6.0F, -7.0F, -0.5F, 6.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(-1.0F, -4.0F, 0.5F, 0.0F, 0.6108652381980153F, 0.20943951023931956F));
        PartDefinition left_horn = head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(22, 59).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(-2.0F, -4.5F, 1.0F, 0.3490658503988659F, -0.3490658503988659F, 0.0F));
        PartDefinition left_horn_tip = left_horn.addOrReplaceChild("left_horn_tip", CubeListBuilder.create().texOffs(33, 59).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_fang = head.addOrReplaceChild("left_fang", CubeListBuilder.create().texOffs(38, 59).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offset(-1.0F, -0.5F, -5.0F));
        PartDefinition right_hood = neck.addOrReplaceChild("right_hood", CubeListBuilder.create().texOffs(43, 59).addBox(0.0F, -7.0F, -0.5F, 6.0F, 9.0F, 1.0F), PartPose.offsetAndRotation(1.0F, -4.0F, 0.5F, 0.0F, -0.6108652381980153F, -0.20943951023931956F));
        PartDefinition right_horn = head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(58, 59).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(2.0F, -4.5F, 1.0F, 0.3490658503988659F, 0.3490658503988659F, 0.0F));
        PartDefinition right_horn_tip = right_horn.addOrReplaceChild("right_horn_tip", CubeListBuilder.create().texOffs(69, 59).addBox(-0.5F, -2.5F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, -0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_fang = head.addOrReplaceChild("right_fang", CubeListBuilder.create().texOffs(74, 59).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offset(1.0F, -0.5F, -5.0F));
        PartDefinition vent_0 = chest.addOrReplaceChild("vent_0", CubeListBuilder.create().texOffs(79, 59).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.5F, 1.5F), PartPose.offset(0.0F, -8.0F, 2.5F));
        PartDefinition vent_1 = chest.addOrReplaceChild("vent_1", CubeListBuilder.create().texOffs(90, 59).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.5F, 1.5F), PartPose.offset(0.0F, -5.5F, 2.5F));
        PartDefinition vent_2 = chest.addOrReplaceChild("vent_2", CubeListBuilder.create().texOffs(101, 59).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.5F, 1.5F), PartPose.offset(0.0F, -3.0F, 2.5F));
        PartDefinition vent_3 = abdomen.addOrReplaceChild("vent_3", CubeListBuilder.create().texOffs(112, 59).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 1.5F, 1.5F), PartPose.offset(0.0F, -4.0F, 2.5F));
        PartDefinition spine_0 = coil.addOrReplaceChild("spine_0", CubeListBuilder.create().texOffs(123, 59).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -7.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition spine_1 = tail_0.addOrReplaceChild("spine_1", CubeListBuilder.create().texOffs(128, 59).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition spine_2 = tail_1.addOrReplaceChild("spine_2", CubeListBuilder.create().texOffs(133, 59).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition spine_3 = tail_2.addOrReplaceChild("spine_3", CubeListBuilder.create().texOffs(138, 59).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -5.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition spine_4 = tail_3.addOrReplaceChild("spine_4", CubeListBuilder.create().texOffs(143, 59).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -5.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition spine_5 = tail_4.addOrReplaceChild("spine_5", CubeListBuilder.create().texOffs(148, 59).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -4.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition spine_6 = tail_5.addOrReplaceChild("spine_6", CubeListBuilder.create().texOffs(153, 59).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -4.0F, 1.0F, -0.4363323129985824F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(CinderBellowsEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        int cycle = entity.getBreathCycle();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
        ordealPose(limbSwing, limbSwingAmount, age, entity.ordealMove(), entity.ordealMoveAge(Mth.frac(age)));
    }

    /** Ember heat for the glow layer: banked at rest, rising through the inhale, full during the breath. */
    public static float emberHeat(float cycle) {
        return Math.max(inhale(cycle) * .55F, exhale(cycle));
    }

    private static float inhale(float cycle) {
        return HemoEasing.smoothstep(cycle <= 20 ? cycle / 20 : cycle <= 60 ? 1 : (100 - cycle) / 40);
    }

    private static float exhale(float cycle) {
        return HemoEasing.smoothstep((cycle - 20) / 5) * (1 - HemoEasing.smoothstep((cycle - 60) / 12));
    }

    /** Shared posing entry point for runtime rendering and authoring previews. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch, float cycle) {
        root.getAllParts().forEach(ModelPart::resetPose);
        float inhale = inhale(cycle);
        float exhale = exhale(cycle);
        // Rear back while drawing breath, then lunge the neck into the cone.
        float draw = inhale * (1 - exhale);
        float travel = Mth.clamp(movement, 0, 1);
        float wave = age * .075F + limbSwing * .6F;
        for (int i = 0; i < tail.length; i++) {
            // Local curvature propagates through the parents, as on Scarlet Serpent.
            tail[i].yRot += Mth.sin(wave - i * .65F) * (.035F + travel * .12F)
                    * (1 - exhale * .65F);
        }
        tailTip.xRot += Mth.sin(age * .11F) * .12F + inhale * .3F;
        pelvis.zRot += Mth.sin(wave) * travel * .035F;
        abdomen.xRot -= draw * .12F;
        chest.xRot += exhale * .12F - draw * .16F;
        chest.y -= inhale * .25F;
        for (ModelPart lung : lungs) {
            lung.xScale = 1 + inhale * .12F;
            lung.yScale = 1 + inhale * .06F;
            lung.zScale = 1 + inhale * .22F;
        }
        neck.xRot += exhale * .32F - draw * .18F;
        head.yRot += Mth.clamp(yaw, -35, 35) * Mth.DEG_TO_RAD;
        // The head counters the neck lunge so the open jaw stays aimed down the cone.
        head.xRot += Mth.clamp(pitch, -25, 25) * Mth.DEG_TO_RAD - exhale * .3F + draw * .1F;
        jaw.xRot += exhale * .9F + inhale * .03F;
        for (int i = 0; i < arms.length; i++) {
            float sign = i == 0 ? -1 : 1;
            // Elbows haul back like bellows handles on the draw, then pump forward on the breath.
            arms[i].zRot -= sign * draw * .25F;
            arms[i].xRot += Mth.sin(age * .045F + i * Mth.PI) * .025F + draw * .35F - exhale * .45F;
            forearms[i].xRot -= exhale * .3F;
            hands[i].xRot += Mth.sin(age * .05F + i * Mth.PI) * .035F;
        }
    }

    /**
     * The true self's weight: a slither (the coil undulating harder, the trunk swaying over it) and its ordeal moves
     * (CinderBellowsOrdealMoves), each a slow gather, a fast blow and a heavy recovery.
     */
    private void ordealPose(float limbSwing, float movement, float age, int move, float since) {
        float travel = Mth.clamp(movement, 0, 1);
        float wave = age * .11F + limbSwing * .9F;
        for (int i = 0; i < tail.length; i++) tail[i].yRot += Mth.sin(wave - i * .7F) * .12F * travel;
        pelvis.yRot += Mth.sin(wave + .6F) * .12F * travel;
        root.y -= Math.abs(Mth.sin(wave)) * .6F * travel;
        for (int i = 0; i < 2; i++) {
            float sign = i == 0 ? -1 : 1;
            hoods[i].yRot += sign * Mth.sin(age * .09F) * .05F;
        }
        if (move < 0) return;
        switch (move) {
            case CinderBellowsOrdealMoves.BLAST -> {
                float w = CinderBellowsOrdealMoves.BLAST_WINDUP, burn = CinderBellowsOrdealMoves.BLAST_BURN;
                float inhale = HemoEasing.smoothstep(0, w - 3, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + burn + 2, w + burn + 18, since);
                float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 6, w - 2, since) * (1 - down);
                float fill = inhale * (1 - down) * hold, blow = down * hold;
                // Rearing back to fill, the bellows swelling and the hood spreading; then thrown into the blast.
                abdomen.xRot += -.25F * fill + .2F * blow;
                chest.xRot += -.3F * fill + .3F * blow;
                neck.xRot += -.4F * fill + .02F * tremble + .55F * blow;
                head.xRot += .25F * fill - .4F * blow;
                jaw.xRot += .1F * fill + 1.0F * blow;
                for (ModelPart lung : lungs) {
                    float swell = 1 + .5F * fill - .15F * blow;
                    lung.xScale *= swell;
                    lung.zScale *= swell;
                }
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    hoods[i].yRot += sign * -.5F * Math.max(fill, blow);
                    arms[i].xRot += .6F * fill - .9F * blow;
                    arms[i].zRot += -sign * .3F * fill;
                    forearms[i].xRot += -.3F * blow;
                }
            }
            case CinderBellowsOrdealMoves.VENT -> {
                float w = CinderBellowsOrdealMoves.VENT_WINDUP;
                float stoke = HemoEasing.smoothstep(0, w - 2, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + 4, w + 18, since);
                float hunch = stoke * (1 - down) * hold, burst = down * hold;
                // Hunched over the furnace in its ribs, arms crossed in; then thrown open, ribs and hood flared.
                chest.xRot += .35F * hunch - .35F * burst;
                neck.xRot += .3F * hunch - .3F * burst;
                head.xRot += -.2F * hunch + .25F * burst;
                jaw.xRot += .9F * burst;
                for (ModelPart lung : lungs) {
                    float swell = 1 + .35F * hunch + .25F * burst;
                    lung.xScale *= swell;
                    lung.zScale *= swell;
                }
                for (int i = 0; i < 2; i++) {
                    float sign = i == 0 ? -1 : 1;
                    arms[i].zRot += sign * .5F * hunch - sign * 1.2F * burst;
                    arms[i].xRot += -.5F * hunch - .3F * burst;
                    forearms[i].xRot += -.6F * hunch + .5F * burst;
                    hoods[i].yRot += sign * -.7F * burst;
                }
                for (ModelPart rib : ribs) rib.xRot += -.5F * burst;
            }
            case CinderBellowsOrdealMoves.LASH -> {
                float w = CinderBellowsOrdealMoves.LASH_WINDUP, spin = FireEaterOrdealGeometry.LASH_TICKS;
                float draw = HemoEasing.smoothstep(0, w - 2, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + spin + 2, w + spin + 14, since);
                // The coil drawn tight, then whipped all the way round under it.
                float whip = HemoEasing.smoothstep(w - 1, w + spin, since);
                for (int i = 0; i < tail.length; i++) tail[i].yRot += (.14F * draw * (1 - down) - .08F * down) * hold;
                coil.yRot += -Mth.TWO_PI * whip * hold;
                pelvis.zRot += (.15F * draw * (1 - down) - .2F * down) * hold;
                chest.xRot += -.15F * draw * hold;
                jaw.xRot += .5F * down * hold;
                for (int i = 0; i < 2; i++) arms[i].zRot += (i == 0 ? 1 : -1) * .6F * down * hold;
            }
            case CinderBellowsOrdealMoves.LUNGE -> {
                float w = CinderBellowsOrdealMoves.LUNGE_WINDUP, run = CinderBellowsOrdealMoves.LUNGE_RUN;
                float coilUp = HemoEasing.smoothstep(0, w - 2, since), down = fall(since, w);
                float hold = 1 - HemoEasing.smoothstep(w + run + 2, w + run + 16, since);
                float drawn = coilUp * (1 - down) * hold, strike = down * hold;
                // Drawn back in an S like a striking snake, then all of it thrown forward jaws first.
                abdomen.xRot += .35F * drawn + .45F * strike;
                chest.xRot += -.45F * drawn + .35F * strike;
                neck.xRot += -.55F * drawn + .45F * strike;
                head.xRot += .4F * drawn - .35F * strike;
                jaw.xRot += .2F * drawn + 1.1F * strike;
                for (int i = 0; i < 2; i++) {
                    arms[i].xRot += .5F * drawn - 1.4F * strike;
                    forearms[i].xRot += .4F * strike;
                    hoods[i].yRot += (i == 0 ? 1 : -1) * -.5F * strike;
                }
            }
            default -> {
            }
        }
    }

    /** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating like a thrown thing. */
    private static float fall(float since, float at) {
        float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
        return t * t;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
