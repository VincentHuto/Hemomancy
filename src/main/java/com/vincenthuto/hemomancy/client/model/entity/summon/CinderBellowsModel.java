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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_cinder_bellows.py; regenerate the model, atlas and BBModel together.
public class CinderBellowsModel extends EntityModel<CinderBellowsEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "cinder_bellows"), "main");
    private final ModelPart root, pelvis, abdomen, chest, neck, head, jaw, tailTip;
    private final ModelPart[] tail = new ModelPart[10];
    private final ModelPart[] lungs, arms, forearms, hands;

    public CinderBellowsModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        pelvis = root.getChild("pelvis");
        abdomen = pelvis.getChild("abdomen");
        chest = abdomen.getChild("chest");
        neck = chest.getChild("neck");
        head = neck.getChild("head");
        jaw = head.getChild("jaw");
        ModelPart parent = root.getChild("coil");
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
        PartDefinition tail_1 = tail_0.addOrReplaceChild("tail_1", CubeListBuilder.create().texOffs(100, 0).addBox(-3.5F, -5.0F, -2.0F, 7.0F, 5.0F, 6.0F).texOffs(0, 13).addBox(-2.5F, -6.0F, -1.5F, 5.0F, 6.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_2 = tail_1.addOrReplaceChild("tail_2", CubeListBuilder.create().texOffs(21, 13).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 4.0F, 6.0F).texOffs(46, 13).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_3 = tail_2.addOrReplaceChild("tail_3", CubeListBuilder.create().texOffs(65, 13).addBox(-3.0F, -4.0F, -2.0F, 6.0F, 4.0F, 6.0F).texOffs(90, 13).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 5.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_4 = tail_3.addOrReplaceChild("tail_4", CubeListBuilder.create().texOffs(0, 25).addBox(-2.5F, -3.0F, -2.0F, 5.0F, 3.0F, 6.0F).texOffs(23, 25).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_5 = tail_4.addOrReplaceChild("tail_5", CubeListBuilder.create().texOffs(40, 25).addBox(-2.5F, -3.0F, -2.0F, 5.0F, 3.0F, 6.0F).texOffs(63, 25).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 4.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_6 = tail_5.addOrReplaceChild("tail_6", CubeListBuilder.create().texOffs(80, 25).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 2.0F, 6.0F).texOffs(101, 25).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 3.0F, 5.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_7 = tail_6.addOrReplaceChild("tail_7", CubeListBuilder.create().texOffs(0, 35).addBox(-1.5F, -3.0F, -2.0F, 3.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.47123889803846897F, 0.0F));
        PartDefinition tail_8 = tail_7.addOrReplaceChild("tail_8", CubeListBuilder.create().texOffs(19, 35).addBox(-1.5F, -2.0F, -2.0F, 3.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.4188790204786391F, 0.0F));
        PartDefinition tail_9 = tail_8.addOrReplaceChild("tail_9", CubeListBuilder.create().texOffs(38, 35).addBox(-1.0F, -2.0F, -2.0F, 2.0F, 2.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 2.5F, 0.0F, -0.3141592653589793F, 0.0F));
        PartDefinition tail_tip = tail_9.addOrReplaceChild("tail_tip", CubeListBuilder.create().texOffs(55, 35).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 1.0F, 3.0F).texOffs(64, 35).addBox(-0.5F, -2.0F, 2.0F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 3.5F, 0.6108652381980153F, 0.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(69, 35).addBox(-3.5F, -7.0F, -3.0F, 7.0F, 8.0F, 6.0F).texOffs(96, 35).addBox(-2.5F, -6.0F, -3.5F, 5.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, -3.0F, -0.13962634015954636F, 0.0F, 0.0F));
        PartDefinition abdomen = pelvis.addOrReplaceChild("abdomen", CubeListBuilder.create().texOffs(0, 50).addBox(-3.0F, -7.0F, -2.5F, 6.0F, 7.0F, 5.0F).texOffs(23, 50).addBox(-2.0F, -6.0F, -3.0F, 4.0F, 6.0F, 1.0F).texOffs(34, 50).addBox(-0.5F, -6.0F, 2.0F, 1.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.20943951023931956F, 0.0F, 0.0F));
        PartDefinition chest = abdomen.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(39, 50).addBox(-3.0F, -8.0F, 0.5F, 6.0F, 7.0F, 2.0F).texOffs(56, 50).addBox(-4.0F, -10.0F, -1.5F, 8.0F, 2.0F, 3.0F).texOffs(79, 50).addBox(-0.5F, -9.0F, 2.5F, 1.0F, 9.0F, 1.0F).texOffs(84, 50).addBox(-0.5F, -8.0F, -3.5F, 1.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -6.0F, 0.0F, 0.08726646259971647F, 0.0F, 0.0F));
        PartDefinition ruff = chest.addOrReplaceChild("ruff", CubeListBuilder.create().texOffs(89, 50).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offset(0.0F, -10.0F, 0.0F));
        PartDefinition ruff_fold = ruff.addOrReplaceChild("ruff_fold", CubeListBuilder.create().texOffs(0, 63).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.7853981633974483F, 0.0F));
        PartDefinition left_lung = chest.addOrReplaceChild("left_lung", CubeListBuilder.create().texOffs(25, 63).addBox(-1.5F, -3.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(40, 63).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(51, 63).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(66, 63).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(77, 63).addBox(-1.5F, 0.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(92, 63).addBox(-1.0F, 1.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(103, 63).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.0F, 4.0F), PartPose.offset(-1.75F, -4.0F, -0.5F));
        PartDefinition left_clavicle = chest.addOrReplaceChild("left_clavicle", CubeListBuilder.create().texOffs(118, 63).addBox(-4.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-0.5F, -9.5F, -2.0F, 0.0F, 0.0F, -0.17453292519943295F));
        PartDefinition left_rib_0 = chest.addOrReplaceChild("left_rib_0", CubeListBuilder.create().texOffs(0, 71).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(11, 71).addBox(0.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -7.25F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_rib_1 = chest.addOrReplaceChild("left_rib_1", CubeListBuilder.create().texOffs(18, 71).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(29, 71).addBox(0.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -5.5F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_rib_2 = chest.addOrReplaceChild("left_rib_2", CubeListBuilder.create().texOffs(36, 71).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(47, 71).addBox(0.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -3.75F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_rib_3 = chest.addOrReplaceChild("left_rib_3", CubeListBuilder.create().texOffs(54, 71).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(65, 71).addBox(0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, -2.0F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(70, 71).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(79, 71).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(-4.5F, -8.5F, 0.5F, 0.2617993877991494F, 0.0F, 0.20943951023931956F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(92, 71).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(101, 71).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -1.3089969389957472F, 0.0F, -0.10471975511965978F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(114, 71).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition left_fingers = left_hand.addOrReplaceChild("left_fingers", CubeListBuilder.create().texOffs(121, 71).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F).texOffs(0, 80).addBox(-0.25F, 0.0F, -0.5F, 0.5F, 2.5F, 0.5F).texOffs(5, 80).addBox(0.5F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(10, 80).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(1.0F, 0.5F, -0.25F, 0.0F, 0.0F, 0.4363323129985824F));
        PartDefinition right_lung = chest.addOrReplaceChild("right_lung", CubeListBuilder.create().texOffs(15, 80).addBox(-1.5F, -3.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(30, 80).addBox(-1.0F, -2.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(41, 80).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(56, 80).addBox(-1.0F, -0.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(67, 80).addBox(-1.5F, 0.5F, -2.0F, 3.0F, 1.0F, 4.0F).texOffs(82, 80).addBox(-1.0F, 1.5F, -1.5F, 2.0F, 1.0F, 3.0F).texOffs(93, 80).addBox(-1.5F, 2.5F, -2.0F, 3.0F, 1.0F, 4.0F), PartPose.offset(1.75F, -4.0F, -0.5F));
        PartDefinition right_clavicle = chest.addOrReplaceChild("right_clavicle", CubeListBuilder.create().texOffs(108, 80).addBox(0.0F, -0.5F, -0.5F, 4.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.5F, -9.5F, -2.0F, 0.0F, 0.0F, 0.17453292519943295F));
        PartDefinition right_rib_0 = chest.addOrReplaceChild("right_rib_0", CubeListBuilder.create().texOffs(0, 86).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(11, 86).addBox(-2.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -7.25F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_rib_1 = chest.addOrReplaceChild("right_rib_1", CubeListBuilder.create().texOffs(18, 86).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(29, 86).addBox(-2.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -5.5F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_rib_2 = chest.addOrReplaceChild("right_rib_2", CubeListBuilder.create().texOffs(36, 86).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(47, 86).addBox(-2.5F, -0.5F, -4.0F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -3.75F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_rib_3 = chest.addOrReplaceChild("right_rib_3", CubeListBuilder.create().texOffs(54, 86).addBox(-0.5F, -0.5F, -4.0F, 1.0F, 1.0F, 4.0F).texOffs(65, 86).addBox(-1.5F, -0.5F, -4.0F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(3.5F, -2.0F, 1.0F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(70, 86).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(79, 86).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 2.0F, 3.0F), PartPose.offsetAndRotation(4.5F, -8.5F, 0.5F, 0.2617993877991494F, 0.0F, -0.20943951023931956F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(92, 86).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(101, 86).addBox(-1.5F, 4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -1.3089969389957472F, 0.0F, 0.10471975511965978F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(114, 86).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_fingers = right_hand.addOrReplaceChild("right_fingers", CubeListBuilder.create().texOffs(121, 86).addBox(-1.0F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F).texOffs(0, 95).addBox(-0.25F, 0.0F, -0.5F, 0.5F, 2.5F, 0.5F).texOffs(5, 95).addBox(0.5F, 0.0F, -0.5F, 0.5F, 2.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(10, 95).addBox(-0.25F, 0.0F, -0.25F, 0.5F, 1.5F, 0.5F), PartPose.offsetAndRotation(-1.0F, 0.5F, -0.25F, 0.0F, 0.0F, -0.4363323129985824F));
        PartDefinition torch = left_hand.addOrReplaceChild("torch", CubeListBuilder.create().texOffs(15, 95).addBox(-0.5F, -5.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(20, 95).addBox(-1.0F, -7.0F, -1.0F, 2.0F, 2.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 1.5F, -1.0F, 0.6283185307179586F, 0.0F, -0.3490658503988659F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(29, 95).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(38, 95).addBox(-0.5F, -5.0F, -1.75F, 1.0F, 5.0F, 1.0F).texOffs(43, 95).addBox(-1.5F, -2.0F, -1.5F, 3.0F, 1.0F, 3.0F).texOffs(56, 95).addBox(-1.5F, -4.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -10.0F, -0.5F, 0.3141592653589793F, 0.0F, 0.0F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(69, 95).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(90, 95).addBox(-1.5F, -2.0F, -5.5F, 3.0F, 2.0F, 3.0F).texOffs(103, 95).addBox(-2.5F, -5.0F, -3.5F, 5.0F, 1.0F, 1.0F).texOffs(116, 95).addBox(-3.0F, -2.5F, -2.5F, 1.0F, 1.0F, 2.0F).texOffs(0, 106).addBox(2.0F, -2.5F, -2.5F, 1.0F, 1.0F, 2.0F).texOffs(7, 106).addBox(-0.5F, -6.0F, -1.5F, 1.0F, 1.0F, 4.0F), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, -0.4363323129985824F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(18, 106).addBox(-1.5F, 0.0F, -7.0F, 3.0F, 1.0F, 6.0F).texOffs(37, 106).addBox(-1.0F, -1.0F, -6.75F, 2.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, 1.5F, 0.10471975511965978F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void setupAnim(CinderBellowsEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        int cycle = entity.getBreathCycle();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                cycle == 0 ? 0 : Math.min(100, cycle + Mth.frac(age)));
    }

    /** Ember heat for the glow layer: banked at rest, rising through the inhale, full during the breath. */
    public static float emberHeat(float cycle) {
        return Math.max(inhale(cycle) * .55F, exhale(cycle));
    }

    private static float inhale(float cycle) {
        return smooth(cycle <= 20 ? cycle / 20 : cycle <= 60 ? 1 : (100 - cycle) / 40);
    }

    private static float exhale(float cycle) {
        return smooth((cycle - 20) / 5) * (1 - smooth((cycle - 60) / 12));
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

    private static float smooth(float value) {
        float t = Mth.clamp(value, 0, 1);
        return t * t * (3 - 2 * t);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
