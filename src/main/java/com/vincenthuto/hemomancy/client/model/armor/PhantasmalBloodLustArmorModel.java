package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;

public class PhantasmalBloodLustArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
    public static final ModelLayerLocation PHANTASMAL_BLOOD_LUST_HELMET_LAYER = new ModelLayerLocation(
            Hemomancy.rloc("phantasmal_blood_lust_helmet"), "main");
    public static final ModelLayerLocation PHANTASMAL_BLOOD_LUST_CHEST_LAYER = new ModelLayerLocation(
            Hemomancy.rloc("phantasmal_blood_lust_chest"), "main");
    public static final ModelLayerLocation PHANTASMAL_BLOOD_LUST_LEGS_LAYER = new ModelLayerLocation(
            Hemomancy.rloc("phantasmal_blood_lust_leggings"), "main");
    public static final ModelLayerLocation PHANTASMAL_BLOOD_LUST_FEET_LAYER = new ModelLayerLocation(
            Hemomancy.rloc("phantasmal_blood_lust_boots"), "main");

    public static final Lazy<PhantasmalBloodLustArmorModel<LivingEntity>> helmet = Lazy.of(() -> new PhantasmalBloodLustArmorModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(PHANTASMAL_BLOOD_LUST_HELMET_LAYER)));
    public static final Lazy<PhantasmalBloodLustArmorModel<LivingEntity>> chest = Lazy.of(() -> new PhantasmalBloodLustArmorModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(PHANTASMAL_BLOOD_LUST_CHEST_LAYER)));
    public static final Lazy<PhantasmalBloodLustArmorModel<LivingEntity>> legs = Lazy.of(() -> new PhantasmalBloodLustArmorModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(PHANTASMAL_BLOOD_LUST_LEGS_LAYER)));
    public static final Lazy<PhantasmalBloodLustArmorModel<LivingEntity>> boots = Lazy.of(() -> new PhantasmalBloodLustArmorModel<>(
            Minecraft.getInstance().getEntityModels().bakeLayer(PHANTASMAL_BLOOD_LUST_FEET_LAYER)));

    public PhantasmalBloodLustArmorModel(ModelPart root) {
        super(root, RenderType::entityTranslucent);
    }

    public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();
        if (slot == EquipmentSlot.HEAD) {
            PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.18F))
                    .texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.8F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition crownJewel = head.addOrReplaceChild("crownJewel", CubeListBuilder.create().texOffs(113, 45).addBox(-1.0F, -1.25F, -0.625F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
                    .texOffs(107, 56).addBox(-2.0F, -2.25F, -0.325F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.75F, -4.525F, -0.3491F, 0.0F, 0.0F));

            PartDefinition HeadTentacles = head.addOrReplaceChild("HeadTentacles", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.3F, -6.5128F, 0.2125F, 0.3927F, 0.0F, 0.0F));

            PartDefinition lTentacle = HeadTentacles.addOrReplaceChild("lTentacle", CubeListBuilder.create().texOffs(36, 75).mirror().addBox(-2.25F, -2.0942F, -1.7947F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.08F)).mirror(false)
                    .texOffs(38, 85).mirror().addBox(-0.25F, -4.0942F, -1.7947F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.55F, -0.0559F, -0.0993F, 0.0F, 0.8727F, 0.0F));

            PartDefinition lTentacle1 = lTentacle.addOrReplaceChild("lTentacle1", CubeListBuilder.create().texOffs(12, 110).mirror().addBox(-2.4F, -2.6617F, -1.1222F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.32F)).mirror(false)
                    .texOffs(39, 115).mirror().addBox(-0.4F, -3.6617F, -1.1222F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.15F, 0.0942F, 4.2947F, -0.7418F, 0.0F, 0.0F));

            PartDefinition lTentacle2 = lTentacle1.addOrReplaceChild("lTentacle2", CubeListBuilder.create().texOffs(59, 113).mirror().addBox(-2.35F, -2.7263F, -1.4171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.9F)).mirror(false)
                    .texOffs(45, 110).mirror().addBox(-0.35F, -3.2263F, -0.4171F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -0.5336F, 4.7918F, -0.6981F, 0.0F, 0.0F));

            PartDefinition lTentacleBell = lTentacle2.addOrReplaceChild("lTentacleBell", CubeListBuilder.create().texOffs(75, 69).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.72F)).mirror(false)
                    .texOffs(64, 59).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)).mirror(false), PartPose.offset(-0.3F, -0.731F, 4.0674F));

            PartDefinition lJingleChain = lTentacleBell.addOrReplaceChild("lJingleChain", CubeListBuilder.create().texOffs(90, 55).addBox(0.0F, -1.5F, 1.75F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, 0.0F, -0.3193F, 0.2909F, -0.0945F));

            PartDefinition lTentacleBell2 = lJingleChain.addOrReplaceChild("lTentacleBell2", CubeListBuilder.create().texOffs(64, 59).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.0F));

            PartDefinition rTentacle = HeadTentacles.addOrReplaceChild("rTentacle", CubeListBuilder.create().texOffs(36, 75).addBox(-1.75F, -2.0942F, -1.7947F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.08F))
                    .texOffs(38, 85).addBox(0.25F, -4.0942F, -1.7947F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.95F, -0.0559F, -0.0993F, 0.0F, -0.8727F, 0.0F));

            PartDefinition rTentacle1 = rTentacle.addOrReplaceChild("rTentacle1", CubeListBuilder.create().texOffs(12, 110).addBox(-1.6F, -2.6617F, -1.1222F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.32F))
                    .texOffs(39, 115).addBox(0.4F, -3.6617F, -1.1222F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.15F, 0.0942F, 4.2947F, -0.7418F, 0.0F, 0.0F));

            PartDefinition rTentacle2 = rTentacle1.addOrReplaceChild("rTentacle2", CubeListBuilder.create().texOffs(59, 113).addBox(-1.65F, -2.7263F, -1.4171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.9F))
                    .texOffs(45, 110).addBox(0.35F, -3.2263F, -0.4171F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.05F, -0.5336F, 4.7918F, -0.6981F, 0.0F, 0.0F));

            PartDefinition rTentacleBell = rTentacle2.addOrReplaceChild("rTentacleBell", CubeListBuilder.create().texOffs(75, 69).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.72F))
                    .texOffs(64, 59).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)), PartPose.offset(0.3F, -0.731F, 4.0674F));

            PartDefinition rJingleChain = rTentacleBell.addOrReplaceChild("rJingleChain", CubeListBuilder.create().texOffs(90, 55).mirror().addBox(0.0F, -1.5F, 1.75F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, -0.5F, 0.0F, -0.3193F, -0.2909F, 0.0945F));

            PartDefinition rTentacleBell2 = rJingleChain.addOrReplaceChild("rTentacleBell2", CubeListBuilder.create().texOffs(64, 59).addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)), PartPose.offset(0.0F, 0.0F, 5.0F));

            PartDefinition mTentacle = HeadTentacles.addOrReplaceChild("mTentacle", CubeListBuilder.create().texOffs(36, 75).mirror().addBox(-2.25F, -2.0942F, -1.7947F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.08F)).mirror(false)
                    .texOffs(38, 85).mirror().addBox(-0.25F, -4.0942F, -1.7947F, 0.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.55F, -0.0559F, -0.0993F, 0.4363F, 0.0F, 0.0F));

            PartDefinition mTentacle1 = mTentacle.addOrReplaceChild("mTentacle1", CubeListBuilder.create().texOffs(12, 110).mirror().addBox(-2.4F, -2.6617F, -1.1222F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.32F)).mirror(false)
                    .texOffs(39, 115).mirror().addBox(-0.4F, -3.6617F, -1.1222F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.15F, 0.0942F, 4.2947F, -0.7418F, 0.0F, 0.0F));

            PartDefinition mTentacle2 = mTentacle1.addOrReplaceChild("mTentacle2", CubeListBuilder.create().texOffs(59, 113).mirror().addBox(-2.35F, -2.7263F, -1.4171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.9F)).mirror(false)
                    .texOffs(45, 110).mirror().addBox(-0.35F, -3.2263F, -0.4171F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-0.05F, -0.5336F, 4.7918F, -0.6981F, 0.0F, 0.0F));

            PartDefinition mTentacleBell = mTentacle2.addOrReplaceChild("mTentacleBell", CubeListBuilder.create().texOffs(75, 69).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.72F)).mirror(false)
                    .texOffs(64, 59).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)).mirror(false), PartPose.offset(-0.3F, -0.731F, 4.0674F));

            PartDefinition mJingleChain = mTentacleBell.addOrReplaceChild("mJingleChain", CubeListBuilder.create().texOffs(90, 55).addBox(0.0F, -1.5F, 1.75F, 0.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, -0.6545F, 0.0F, 0.0F));

            PartDefinition mTentacleBell2 = mJingleChain.addOrReplaceChild("mTentacleBell2", CubeListBuilder.create().texOffs(64, 59).mirror().addBox(-2.0F, -2.0F, -1.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)).mirror(false), PartPose.offset(0.0F, 0.0F, 5.0F));
        }
        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();

        if (slot == EquipmentSlot.CHEST) {
            PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.18F))
                    .texOffs(84, 100).addBox(-4.0F, 1.0F, -2.0F, 8.0F, 11.0F, 4.0F, new CubeDeformation(0.35F))
                    .texOffs(45, 99).addBox(-4.0F, 0.25F, -2.0F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.55F))
                    .texOffs(99, 117).addBox(-4.0F, 5.25F, -2.0F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(0.0F, 0.0F, 0.0F));

            PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(64, 32).addBox(-0.95F, -1.75F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.16F)), PartPose.offset(5.0F, 2.0F, 0.0F));

            PartDefinition lPaul1 = left_arm.addOrReplaceChild("lPaul1", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.25F, -1.0F, 0.0F, 0.0F, 0.0F, -0.2182F));

            PartDefinition right_arm_72_16_850af74a_r1 = lPaul1.addOrReplaceChild("right_arm_72_16_850af74a_r1", CubeListBuilder.create().texOffs(104, 1).addBox(-2.5F, 1.5F, -5.0F, 7.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 1.25F, 0.5F, 0.0F, 0.0F, -0.1745F));

            PartDefinition right_arm_72_16_850af74a_r2 = lPaul1.addOrReplaceChild("right_arm_72_16_850af74a_r2", CubeListBuilder.create().texOffs(73, 16).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.7F)), PartPose.offsetAndRotation(1.5F, 0.5F, 0.0F, 0.0F, 0.0F, -0.1745F));

            PartDefinition lPaul2 = left_arm.addOrReplaceChild("lPaul2", CubeListBuilder.create().texOffs(91, 27).addBox(0.0F, -1.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.65F))
                    .texOffs(104, 21).addBox(-1.0F, 2.75F, -4.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 0.3F, 0.0F, 0.0F, 0.0F, -0.2182F));

            PartDefinition lPaul3 = left_arm.addOrReplaceChild("lPaul3", CubeListBuilder.create().texOffs(89, 3).addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.5F))
                    .texOffs(106, 30).addBox(-2.0F, 2.0F, -4.0F, 5.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.25F, 3.0F, 0.0F, 0.0F, 0.0F, -0.0873F));

            PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(64, 32).mirror().addBox(-3.05F, -1.75F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.16F)).mirror(false), PartPose.offset(-5.0F, 2.0F, 0.0F));

            PartDefinition rPaul1 = right_arm.addOrReplaceChild("rPaul1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.25F, -1.0F, 0.0F, 0.0F, 0.0F, 0.2182F));

            PartDefinition left_arm_72_16_850af74a_r1 = rPaul1.addOrReplaceChild("left_arm_72_16_850af74a_r1", CubeListBuilder.create().texOffs(104, 1).mirror().addBox(-4.5F, 1.5F, -5.0F, 7.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.0F, 1.25F, 0.5F, 0.0F, 0.0F, 0.1745F));

            PartDefinition left_arm_72_16_850af74a_r2 = rPaul1.addOrReplaceChild("left_arm_72_16_850af74a_r2", CubeListBuilder.create().texOffs(73, 16).mirror().addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.7F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 0.5F, 0.0F, 0.0F, 0.0F, 0.1745F));

            PartDefinition rPaul2 = right_arm.addOrReplaceChild("rPaul2", CubeListBuilder.create().texOffs(91, 27).mirror().addBox(-3.0F, -1.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.65F)).mirror(false)
                    .texOffs(104, 21).mirror().addBox(-5.0F, 2.75F, -4.0F, 6.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.25F, 0.3F, 0.0F, 0.0F, 0.0F, 0.2182F));

            PartDefinition rPaul3 = right_arm.addOrReplaceChild("rPaul3", CubeListBuilder.create().texOffs(89, 3).mirror().addBox(-1.5F, -1.5F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false)
                    .texOffs(106, 30).mirror().addBox(-3.0F, 2.0F, -4.0F, 5.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.25F, 3.0F, 0.0F, 0.0F, 0.0F, 0.0873F));
        } else if (slot == EquipmentSlot.LEGS) {
            PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                    .texOffs(3, 69).addBox(-2.0F, 0.7F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(1.9F, 12.0F, 0.0F));

            PartDefinition left_leg1 = left_leg.addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(0, 57).addBox(-0.5F, -1.75F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.7F, 0.0F));

            PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
                    .texOffs(3, 69).mirror().addBox(-2.0F, 0.7F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.35F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

            PartDefinition right_leg1 = right_leg.addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(0, 57).mirror().addBox(-2.5F, -1.75F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offset(0.0F, 0.7F, 0.0F));
        } else if (slot == EquipmentSlot.FEET) {
            PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

            PartDefinition left_leg1 = left_leg.addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(12, 91).addBox(-2.0F, -5.5F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.4F)), PartPose.offsetAndRotation(0.0F, 6.5F, -0.5F, 0.3491F, 0.0F, 0.0F));

            PartDefinition left_leg2 = left_leg1.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(12, 91).addBox(-2.0F, -5.5F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

            PartDefinition left_leg3 = left_leg.addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(0, 81).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.4F))
                    .texOffs(12, 82).addBox(-2.0F, 3.4F, -4.25F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.5F, 0.0F));

            PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 42).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-1.9F, 12.0F, 0.0F));

            PartDefinition right_leg1 = right_leg.addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(12, 91).mirror().addBox(-2.0F, -5.5F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.4F)).mirror(false), PartPose.offsetAndRotation(0.0F, 6.5F, -0.5F, 0.3491F, 0.0F, 0.0F));

            PartDefinition right_leg2 = right_leg1.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(12, 91).mirror().addBox(-2.0F, -5.5F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F)).mirror(false), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

            PartDefinition right_leg3 = right_leg.addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(0, 81).mirror().addBox(-2.0F, -4.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.4F)).mirror(false)
                    .texOffs(12, 82).mirror().addBox(-3.0F, 3.4F, -4.25F, 5.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 8.5F, 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
        head.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        leftArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        rightArm.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        rightLeg.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
        leftLeg.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
    }
}
