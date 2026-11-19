package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import com.vincenthuto.hemomancy.common.encounter.ordeal.OrdealHulkMoves;
import com.vincenthuto.hemomancy.common.util.HemoEasing;

// Authored by tools/circus/build_true_gorebound_hulk.py (the Hulk's true self, 2026-11-17); regenerate the model, atlas and BBModel together.
public class TrueGoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
    public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
            Hemomancy.rloc("true_gorebound_hulk"), "main");
    private final ModelPart root, chest, head, jaw;
    private final ModelPart[] arms = new ModelPart[2], forearms = new ModelPart[2], hands = new ModelPart[2];
    private final ModelPart[] thighs = new ModelPart[2], shins = new ModelPart[2], feet = new ModelPart[2];

    public TrueGoreboundHulkModel(ModelPart bakedRoot) {
        root = bakedRoot.getChild("root");
        ModelPart pelvis = root.getChild("pelvis");
        chest = pelvis.getChild("chest");
        head = chest.getChild("neck").getChild("head");
        jaw = head.getChild("jaw");
        for (int i = 0; i < 2; i++) {
            String side = i == 0 ? "left" : "right";
            arms[i] = chest.getChild(side + "_arm");
            forearms[i] = arms[i].getChild(side + "_forearm");
            hands[i] = forearms[i].getChild(side + "_hand");
            thighs[i] = pelvis.getChild(side + "_thigh");
            shins[i] = thighs[i].getChild(side + "_shin");
            feet[i] = shins[i].getChild(side + "_foot");
        }
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot().addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));
        PartDefinition pelvis = root.addOrReplaceChild("pelvis", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.0F, -3.0F, 10.0F, 4.0F, 6.0F).texOffs(33, 0).addBox(-4.5F, -9.0F, -3.0F, 9.0F, 7.0F, 6.0F), PartPose.offset(0.0F, -12.0F, 0.0F));
        PartDefinition belt = pelvis.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(64, 0).addBox(-5.5F, -1.5F, -3.5F, 11.0F, 3.0F, 7.0F), PartPose.offset(0.0F, -2.5F, 0.0F));
        PartDefinition buckle = belt.addOrReplaceChild("buckle", CubeListBuilder.create().texOffs(101, 0).addBox(-2.0F, -1.5F, -0.75F, 4.0F, 3.0F, 1.0F).texOffs(112, 0).addBox(-1.0F, -1.0F, -1.25F, 2.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 0.0F, -3.5F));
        PartDefinition chest = pelvis.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(119, 0).addBox(-7.0F, -11.0F, -4.0F, 14.0F, 11.0F, 8.0F).texOffs(164, 0).addBox(-6.0F, -13.0F, -1.0F, 12.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(0.0F, -8.0F, 0.0F, 0.4886921905584123F, 0.0F, 0.0F));
        PartDefinition left_pectoral = chest.addOrReplaceChild("left_pectoral", CubeListBuilder.create().texOffs(201, 0).addBox(-3.0F, -2.5F, -1.0F, 6.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(-3.5F, -8.0F, -4.0F, 0.0F, 0.0F, 0.10471975511965978F));
        PartDefinition left_lat = chest.addOrReplaceChild("left_lat", CubeListBuilder.create().texOffs(218, 0).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(-6.5F, -5.0F, 1.0F, 0.0F, 0.0F, -0.20943951023931956F));
        PartDefinition left_strap = chest.addOrReplaceChild("left_strap", CubeListBuilder.create().texOffs(233, 0).addBox(-1.0F, -5.0F, -0.5F, 2.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(-4.0F, -6.0F, -5.0F, 0.0F, 0.0F, 0.5585053606381855F));
        PartDefinition left_trap = chest.addOrReplaceChild("left_trap", CubeListBuilder.create().texOffs(0, 20).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(-3.5F, -11.0F, 0.0F, 0.0F, 0.0F, -0.3490658503988659F));
        PartDefinition right_pectoral = chest.addOrReplaceChild("right_pectoral", CubeListBuilder.create().texOffs(25, 20).addBox(-3.0F, -2.5F, -1.0F, 6.0F, 5.0F, 2.0F), PartPose.offsetAndRotation(3.5F, -8.0F, -4.0F, 0.0F, 0.0F, -0.10471975511965978F));
        PartDefinition right_lat = chest.addOrReplaceChild("right_lat", CubeListBuilder.create().texOffs(42, 20).addBox(-1.5F, -4.0F, -2.0F, 3.0F, 8.0F, 4.0F), PartPose.offsetAndRotation(6.5F, -5.0F, 1.0F, 0.0F, 0.0F, 0.20943951023931956F));
        PartDefinition right_strap = chest.addOrReplaceChild("right_strap", CubeListBuilder.create().texOffs(57, 20).addBox(-1.0F, -5.0F, -0.5F, 2.0F, 5.0F, 1.0F), PartPose.offsetAndRotation(4.0F, -6.0F, -5.0F, 0.0F, 0.0F, -0.5585053606381855F));
        PartDefinition right_trap = chest.addOrReplaceChild("right_trap", CubeListBuilder.create().texOffs(64, 20).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F), PartPose.offsetAndRotation(3.5F, -11.0F, 0.0F, 0.0F, 0.0F, 0.3490658503988659F));
        PartDefinition neck = chest.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(89, 20).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 3.0F, 4.0F), PartPose.offset(0.0F, -12.0F, -2.5F));
        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(106, 20).addBox(-2.5F, -5.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(127, 20).addBox(-3.0F, -4.0F, -3.5F, 6.0F, 1.0F, 1.0F).texOffs(142, 20).addBox(-3.5F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(147, 20).addBox(2.5F, -3.0F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(152, 20).addBox(-2.5F, -1.5F, -3.25F, 5.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -2.0F, -0.5F, -0.3839724354387525F, 0.0F, 0.0F));
        PartDefinition jaw = head.addOrReplaceChild("jaw", CubeListBuilder.create().texOffs(165, 20).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 4.0F).texOffs(182, 20).addBox(-2.0F, -1.0F, -3.5F, 1.0F, 1.0F, 1.0F).texOffs(187, 20).addBox(1.0F, -1.0F, -3.5F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 0.0F, -0.5F, 0.06981317007977318F, 0.0F, 0.0F));
        PartDefinition left_arm = chest.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(192, 20).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(213, 20).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F).texOffs(238, 20).addBox(-2.0F, 1.0F, -3.5F, 4.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-8.0F, -9.0F, 0.0F, -0.20943951023931956F, 0.0F, 0.17453292519943295F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(0, 33).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F).texOffs(29, 33).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.3839724354387525F, 0.0F, -0.06981317007977318F));
        PartDefinition left_binding = left_forearm.addOrReplaceChild("left_binding", CubeListBuilder.create().texOffs(54, 33).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition left_hand = left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create().texOffs(87, 33).addBox(-4.0F, 0.0F, -3.5F, 8.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition left_finger_0 = left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create().texOffs(118, 33).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_finger_1 = left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create().texOffs(127, 33).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_finger_2 = left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create().texOffs(136, 33).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_finger_3 = left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create().texOffs(145, 33).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition left_thumb = left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create().texOffs(154, 33).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(4.0F, 1.0F, -2.0F, 0.3490658503988659F, 0.0F, -0.4363323129985824F));
        PartDefinition left_thigh = pelvis.addOrReplaceChild("left_thigh", CubeListBuilder.create().texOffs(163, 33).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(184, 33).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(-3.0F, 0.0F, 0.0F));
        PartDefinition left_shin = left_thigh.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(209, 33).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F).texOffs(226, 33).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition left_foot = left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 48).addBox(-2.5F, -1.0F, -4.0F, 5.0F, 3.0F, 6.0F).texOffs(23, 48).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_arm = chest.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(34, 48).addBox(-2.5F, -1.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(55, 48).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 3.0F, 6.0F).texOffs(80, 48).addBox(-2.0F, 1.0F, -3.5F, 4.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(8.0F, -9.0F, 0.0F, -0.20943951023931956F, 0.0F, -0.17453292519943295F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(91, 48).addBox(-3.5F, 0.0F, -3.5F, 7.0F, 7.0F, 7.0F).texOffs(120, 48).addBox(-3.0F, -1.0F, -3.0F, 6.0F, 1.0F, 6.0F), PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, -0.3839724354387525F, 0.0F, 0.06981317007977318F));
        PartDefinition right_binding = right_forearm.addOrReplaceChild("right_binding", CubeListBuilder.create().texOffs(145, 48).addBox(-4.0F, 0.0F, -4.0F, 8.0F, 2.0F, 8.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_hand = right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create().texOffs(178, 48).addBox(-4.0F, 0.0F, -3.5F, 8.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, 0.17453292519943295F, 0.0F, 0.0F));
        PartDefinition right_finger_0 = right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create().texOffs(209, 48).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_finger_1 = right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create().texOffs(218, 48).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_finger_2 = right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create().texOffs(227, 48).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(1.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_finger_3 = right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create().texOffs(236, 48).addBox(-1.0F, 0.0F, -1.5F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(3.0F, 3.0F, -3.5F, 0.5235987755982988F, 0.0F, 0.0F));
        PartDefinition right_thumb = right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create().texOffs(245, 48).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(-4.0F, 1.0F, -2.0F, 0.3490658503988659F, 0.0F, 0.4363323129985824F));
        PartDefinition right_thigh = pelvis.addOrReplaceChild("right_thigh", CubeListBuilder.create().texOffs(0, 63).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 5.0F, 5.0F).texOffs(21, 63).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 2.0F, 6.0F), PartPose.offset(3.0F, 0.0F, 0.0F));
        PartDefinition right_shin = right_thigh.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(46, 63).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 5.0F, 4.0F).texOffs(63, 63).addBox(-1.5F, -0.5F, -2.5F, 3.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition right_foot = right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(72, 63).addBox(-2.5F, -1.0F, -4.0F, 5.0F, 3.0F, 6.0F).texOffs(95, 63).addBox(-2.0F, 0.0F, -4.5F, 4.0F, 2.0F, 1.0F), PartPose.offset(0.0F, 5.0F, 0.0F));
        PartDefinition hump = chest.addOrReplaceChild("hump", CubeListBuilder.create().texOffs(106, 63).addBox(-6.0F, -4.0F, -2.0F, 12.0F, 5.0F, 6.0F).texOffs(143, 63).addBox(-4.0F, -6.0F, -1.0F, 8.0F, 2.0F, 4.0F), PartPose.offset(0.0F, -11.0F, 2.0F));
        PartDefinition left_shoulder_mass = chest.addOrReplaceChild("left_shoulder_mass", CubeListBuilder.create().texOffs(168, 63).addBox(-3.0F, -3.0F, -3.5F, 6.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(-7.0F, -11.0F, 0.0F, 0.0F, 0.0F, -0.24434609527920614F));
        PartDefinition left_spur = left_forearm.addOrReplaceChild("left_spur", CubeListBuilder.create().texOffs(195, 63).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(-3.5F, 2.0F, 0.0F, 0.0F, 0.0F, -0.6108652381980153F));
        PartDefinition left_chain = left_binding.addOrReplaceChild("left_chain", CubeListBuilder.create().texOffs(200, 63).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(205, 63).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.0F, 1.5F).texOffs(214, 63).addBox(-0.5F, 4.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 2.0F, -3.5F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition right_shoulder_mass = chest.addOrReplaceChild("right_shoulder_mass", CubeListBuilder.create().texOffs(219, 63).addBox(-3.0F, -3.0F, -3.5F, 6.0F, 4.0F, 7.0F), PartPose.offsetAndRotation(7.0F, -11.0F, 0.0F, 0.0F, 0.0F, 0.24434609527920614F));
        PartDefinition right_spur = right_forearm.addOrReplaceChild("right_spur", CubeListBuilder.create().texOffs(246, 63).addBox(-0.5F, -3.0F, -0.5F, 1.0F, 4.0F, 1.0F), PartPose.offsetAndRotation(3.5F, 2.0F, 0.0F, 0.0F, 0.0F, 0.6108652381980153F));
        PartDefinition right_chain = right_binding.addOrReplaceChild("right_chain", CubeListBuilder.create().texOffs(251, 63).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(0, 75).addBox(-0.75F, 3.0F, -0.75F, 1.5F, 1.0F, 1.5F).texOffs(9, 75).addBox(-0.5F, 4.0F, -0.5F, 1.0F, 3.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 2.0F, -3.5F, 0.3490658503988659F, 0.0F, 0.0F));
        PartDefinition vent_0 = chest.addOrReplaceChild("vent_0", CubeListBuilder.create().texOffs(14, 75).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.5F), PartPose.offset(0.0F, -9.5F, 4.0F));
        PartDefinition vent_1 = chest.addOrReplaceChild("vent_1", CubeListBuilder.create().texOffs(25, 75).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.5F), PartPose.offset(0.0F, -6.5F, 4.0F));
        PartDefinition vent_2 = chest.addOrReplaceChild("vent_2", CubeListBuilder.create().texOffs(36, 75).addBox(-1.5F, -1.0F, -0.5F, 3.0F, 2.0F, 1.5F), PartPose.offset(0.0F, -3.5F, 4.0F));
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public void setupAnim(GoreboundHulkEntity entity, float limbSwing, float limbSwingAmount,
            float age, float yaw, float pitch) {
        float partialTick = Mth.clamp(age - entity.tickCount, 0, 1);
        float windup = entity.getStrikeWindup();
        setupPose(limbSwing, limbSwingAmount, age, yaw, pitch,
                windup > 0 ? windup + partialTick : 0, attackTime);
        ordealPose(limbSwing, limbSwingAmount, entity.ordealMove(), entity.ordealMoveAge(partialTick));
    }

    /** Shared runtime and authoring pose; windup is the existing ten-tick strike counter. */
    public void setupPose(float limbSwing, float movement, float age, float yaw, float pitch,
            float windup, float strike) {
        root.getAllParts().forEach(ModelPart::resetPose);
        movement = Mth.clamp(movement, 0, 1);
        float lift = Mth.clamp(windup / 8, 0, 1);
        lift = lift * lift * (3 - 2 * lift);
        float recovery = Mth.clamp(strike * 2.5F, 0, 1);
        float impact = Mth.sin(Mth.clamp(strike, 0, 1) * Mth.PI);
        chest.xRot += -lift * .18F + impact * .14F;
        head.yRot += Mth.clamp(yaw, -45, 45) * Mth.DEG_TO_RAD * .7F;
        head.xRot += Mth.clamp(pitch, -30, 30) * Mth.DEG_TO_RAD * .5F;
        jaw.xRot += Mth.sin(age * .09F) * .025F + lift * .05F;
        for (int i = 0; i < 2; i++) {
            float side = i == 0 ? -1 : 1;
            float gait = Mth.cos(limbSwing * .6662F + i * Mth.PI) * movement;
            thighs[i].xRot = gait * .45F;
            shins[i].xRot = Math.max(0, -gait) * .55F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            arms[i].xRot -= lift * 1.1F;
            forearms[i].xRot -= lift * .65F;
            if (windup <= 0 && strike > 0) {
                arms[i].xRot = Mth.lerp(recovery, -1.25F, arms[i].xRot);
                forearms[i].xRot = Mth.lerp(recovery, -.85F, forearms[i].xRot);
            }
            arms[i].xRot -= gait * .12F * (1 - lift);
            arms[i].zRot -= side * lift * .12F;
            hands[i].xRot -= lift * .12F;
        }
    }

    /**
     * The true self's weight: a heavy gait (deeper strides, the body dropping on each footfall and rolling over the
     * planted leg) and its ordeal moves (OrdealHulkMoves), each a slow gather, a fast blow and a heavy recovery.
     */
    private void ordealPose(float limbSwing, float movement, int move, float since) {
        movement = Mth.clamp(movement, 0, 1);
        float stride = limbSwing * .6662F;
        root.y += Math.abs(Mth.sin(stride)) * 1.2F * movement;
        chest.zRot += Mth.cos(stride) * .09F * movement;
        chest.yRot += Mth.cos(stride) * .07F * movement;
        for (int i = 0; i < 2; i++) {
            thighs[i].xRot *= 1.5F;
            shins[i].xRot *= 1.4F;
            feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
            arms[i].xRot -= Mth.cos(stride + i * Mth.PI) * .3F * movement;
        }
        if (move < 0) return;
        switch (move) {
            case OrdealHulkMoves.HAMMER -> {
                float w = OrdealHulkMoves.HAMMER_WINDUP;
                float gather = HemoEasing.smoothstep(0, w - 3, since), down = fall(since, w), held = 1 - HemoEasing.smoothstep(w + 5, w + 16, since);
                float tremble = Mth.sin(since * 3.1F) * HemoEasing.smoothstep(w - 5, w - 2, since) * (1 - down);
                for (int i = 0; i < 2; i++) {
                    arms[i].xRot = Mth.lerp(held, arms[i].xRot, Mth.lerp(down, -2.9F + .06F * tremble, -.85F));
                    arms[i].zRot *= 1 - .7F * held;
                    forearms[i].xRot = Mth.lerp(held, forearms[i].xRot, Mth.lerp(down, -1.0F, -.15F));
                    thighs[i].xRot += -.35F * down * held;
                    shins[i].xRot += .6F * down * held;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                chest.xRot += held * (-.32F * gather * (1 - down) + .5F * down);
                head.xRot += .2F * down * held;
                jaw.xRot += .3F * gather * (1 - down) * held;
            }
            case OrdealHulkMoves.CHARGE -> {
                float w = OrdealHulkMoves.CHARGE_WINDUP, run = OrdealHulkMoves.CHARGE_RUN;
                float crouch = HemoEasing.smoothstep(0, w - 4, since) * (1 - HemoEasing.smoothstep(w + run, w + run + 14, since));
                float running = HemoEasing.smoothstep(w - 1, w + 1, since) * (1 - HemoEasing.smoothstep(w + run, w + run + 4, since));
                // Pawing the floor with its right foot through the wind-up, then a lowered-head rush.
                float paw = Mth.sin(since * .9F) * .35F * crouch * (1 - running);
                thighs[1].xRot += paw;
                chest.xRot += .45F * crouch + .3F * running;
                head.xRot += -.35F * crouch * (1 - running) + .2F * running;
                jaw.xRot += .25F * crouch;
                for (int i = 0; i < 2; i++) {
                    arms[i].xRot = Mth.lerp(crouch, arms[i].xRot, Mth.lerp(running, .6F, -.7F));
                    forearms[i].xRot = Mth.lerp(crouch, forearms[i].xRot, -.4F);
                    float sprint = Mth.cos(since * 1.4F + i * Mth.PI) * running;
                    thighs[i].xRot += sprint * .9F;
                    shins[i].xRot += Math.max(0, -sprint) * .9F;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                // It stumbles out of the rush.
                float stumble = HemoEasing.smoothstep(w + run, w + run + 3, since) * (1 - HemoEasing.smoothstep(w + run + 3, w + run + 14, since));
                chest.zRot += Mth.sin(since * 1.3F) * .15F * stumble;
            }
            case OrdealHulkMoves.GRAB -> {
                float w = OrdealHulkMoves.GRAB_WINDUP, hold = OrdealHulkMoves.GRAB_HOLD;
                float reach = HemoEasing.smoothstep(0, w - 2, since), clamp = HemoEasing.smoothstep(w - 1, w + 1, since);
                float lift = HemoEasing.smoothstep(w + 1, w + 5, since) * (1 - HemoEasing.smoothstep(w + hold - 1, w + hold + 1, since));
                float thrown = HemoEasing.smoothstep(w + hold - 1, w + hold + 1, since) * (1 - HemoEasing.smoothstep(w + hold + 4, w + hold + 14, since));
                float any = reach * (1 - HemoEasing.smoothstep(w + hold + 4, w + hold + 14, since));
                for (int i = 0; i < 2; i++) {
                    float side = i == 0 ? -1 : 1;
                    float x = Mth.lerp(lift, -1.6F, -2.9F);
                    x = Mth.lerp(thrown, x, -.9F);
                    arms[i].xRot = Mth.lerp(any, arms[i].xRot, x);
                    arms[i].zRot = Mth.lerp(any, arms[i].zRot, Mth.lerp(clamp, -side * .65F, -side * .12F));
                    forearms[i].xRot = Mth.lerp(any, forearms[i].xRot, Mth.lerp(lift, -.1F, -.45F));
                }
                chest.xRot += .25F * reach * (1 - lift) - .25F * lift + .45F * thrown;
                jaw.xRot += .35F * lift;
            }
            case OrdealHulkMoves.STOMP -> {
                float w = OrdealHulkMoves.STOMP_WINDUP;
                float raise = HemoEasing.smoothstep(0, w - 3, since), down = fall(since, w), held = 1 - HemoEasing.smoothstep(w + 4, w + 16, since);
                float up = raise * (1 - down) * held;
                thighs[1].xRot = Mth.lerp(up, thighs[1].xRot, -1.45F);
                shins[1].xRot = Mth.lerp(up, shins[1].xRot, 1.3F);
                feet[1].xRot = -thighs[1].xRot - shins[1].xRot;
                for (int i = 0; i < 2; i++) {
                    float side = i == 0 ? -1 : 1;
                    arms[i].zRot += -side * .9F * up;
                    arms[i].xRot += -.4F * up + .5F * down * held;
                    thighs[i].xRot += -.3F * down * held;
                    shins[i].xRot += .5F * down * held;
                    feet[i].xRot = -thighs[i].xRot - shins[i].xRot;
                }
                chest.xRot += -.2F * up + .4F * down * held;
                head.xRot += .25F * down * held;
            }
            default -> {
            }
        }
    }

    /** 0 until two ticks before {@code at}, then 1 at {@code at}, accelerating like a falling weight. */
    private static float fall(float since, float at) {
        float t = Mth.clamp((since - (at - 2.0F)) / 2.0F, 0.0F, 1.0F);
        return t * t;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer consumer, int light, int overlay, int color) {
        root.render(pose, consumer, light, overlay, color);
    }
}
