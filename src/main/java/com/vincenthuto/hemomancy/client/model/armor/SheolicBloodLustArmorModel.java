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

public class SheolicBloodLustArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation SHEOLIC_BLOOD_LUST_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("sheolic_blood_lust_helmet"), "main");
	public static final ModelLayerLocation SHEOLIC_BLOOD_LUST_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("sheolic_blood_lust_chest"), "main");
	public static final ModelLayerLocation SHEOLIC_BLOOD_LUST_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("sheolic_blood_lust_leggings"), "main");
	public static final ModelLayerLocation SHEOLIC_BLOOD_LUST_FEET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("sheolic_blood_lust_boots"), "main");

	public static final Lazy<SheolicBloodLustArmorModel<LivingEntity>> helmet = Lazy.of(() -> new SheolicBloodLustArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SHEOLIC_BLOOD_LUST_HELMET_LAYER)));
	public static final Lazy<SheolicBloodLustArmorModel<LivingEntity>> chest = Lazy.of(() -> new SheolicBloodLustArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SHEOLIC_BLOOD_LUST_CHEST_LAYER)));
	public static final Lazy<SheolicBloodLustArmorModel<LivingEntity>> legs = Lazy.of(
			() -> new SheolicBloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(SHEOLIC_BLOOD_LUST_LEGS_LAYER)));
	public static final Lazy<SheolicBloodLustArmorModel<LivingEntity>> boots = Lazy.of(
			() -> new SheolicBloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(SHEOLIC_BLOOD_LUST_FEET_LAYER)));

	public SheolicBloodLustArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);
	}

	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot == EquipmentSlot.HEAD) {
		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 42).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 5.0F, 8.0F, new CubeDeformation(1.0F))
		.texOffs(37, 26).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.8F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head_core_r1 = head.addOrReplaceChild("head_core_r1", CubeListBuilder.create().texOffs(93, 110).addBox(-3.0F, -1.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -8.25F, 2.1F, 0.3054F, 0.0F, 0.0F));

		PartDefinition head_core_r2 = head.addOrReplaceChild("head_core_r2", CubeListBuilder.create().texOffs(93, 110).addBox(-3.0F, -1.0F, -4.0F, 6.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.25F, 0.1F, 0.3054F, 0.0F, 0.0F));

		PartDefinition left_horns = head.addOrReplaceChild("left_horns", CubeListBuilder.create().texOffs(85, 0).addBox(0.0F, -1.0F, -2.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, -6.25F, -1.5F, 0.5236F, 0.0F, 0.0F));

		PartDefinition left_horns1 = left_horns.addOrReplaceChild("left_horns1", CubeListBuilder.create().texOffs(81, 44).addBox(0.0F, -2.8874F, 1.2895F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.5F, -0.829F, 0.0F, 0.0F));

		PartDefinition right_horns = head.addOrReplaceChild("right_horns", CubeListBuilder.create().texOffs(63, 85).addBox(-1.0F, -1.0F, -2.5F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -6.25F, -1.5F, 0.5236F, 0.0F, 0.0F));

		PartDefinition right_horns1 = right_horns.addOrReplaceChild("right_horns1", CubeListBuilder.create().texOffs(82, 73).addBox(-1.0F, -2.8874F, 1.2895F, 1.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.5F, -0.829F, 0.0F, 0.0F));

			PartDefinition headscutes = head.addOrReplaceChild("headscutes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition cube_r1 = headscutes.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 13).addBox(-5.0F, -2.0F, -3.0F, 11.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -2.75F, -1.0F, -0.3054F, 0.0F, 0.0F));

			PartDefinition cube_r2 = headscutes.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -2.0F, -3.0F, 11.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -0.5F, -1.0F, -0.3054F, 0.0F, 0.0F));

		}
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot == EquipmentSlot.CHEST) {
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(43, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition thorax_shell = body.addOrReplaceChild("thorax_shell", CubeListBuilder.create().texOffs(0, 26).addBox(-5.0F, -5.0F, -3.8F, 10.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(70, 29).addBox(-4.5F, -4.5F, -4.6F, 9.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(73, 67).addBox(-4.0F, 0.0F, -3.7F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition abdomen_segments = body.addOrReplaceChild("abdomen_segments", CubeListBuilder.create().texOffs(27, 56).addBox(-4.0F, -2.2F, -3.2F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition back_scutes = body.addOrReplaceChild("back_scutes", CubeListBuilder.create().texOffs(44, 73).addBox(-4.0F, -0.5F, -0.1F, 8.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(13, 91).addBox(-2.55F, 3.8F, -0.35F, 5.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(98, 98).addBox(-3.75F, -5.2F, 1.2F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(98, 98).addBox(-2.75F, -0.2F, 0.7F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 99).addBox(1.75F, -5.2F, 1.2F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 99).addBox(0.5F, -0.2F, 0.7F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 6.0F, 2.0F));

		PartDefinition tassets = body.addOrReplaceChild("tassets", CubeListBuilder.create().texOffs(94, 63).addBox(-2.0F, -2.25F, -2.7F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(44, 66).addBox(-4.2F, -1.4F, -2.6F, 3.0F, 2.8F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(70, 36).addBox(1.2F, -1.4F, -2.6F, 3.0F, 2.8F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(99, 69).addBox(-4.25F, 0.4F, -2.4F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(99, 75).addBox(1.25F, 0.4F, -2.4F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 11.0F, -0.75F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 56).addBox(-3.25F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition r_pauldron = right_arm.addOrReplaceChild("r_pauldron", CubeListBuilder.create().texOffs(33, 43).addBox(-4.8F, -3.0F, -3.6F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(73, 55).addBox(-5.4F, -3.6F, -2.8F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 96).addBox(-3.85F, -5.2F, -2.7F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(17, 76).addBox(-2.55F, -3.7F, 0.7F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition r_pauldron_spur_r1 = r_pauldron.addOrReplaceChild("r_pauldron_spur_r1", CubeListBuilder.create().texOffs(0, 96).addBox(-5.6F, -5.2F, -1.2F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.75F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition r_forearm_shell = right_arm.addOrReplaceChild("r_forearm_shell", CubeListBuilder.create().texOffs(60, 43).addBox(-2.2F, -1.2F, -2.8F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(17, 83).addBox(-2.0F, -2.0F, -2.4F, 4.0F, 1.8F, 4.8F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 4.0F, 0.0F));

		PartDefinition r_bracer_blade = r_forearm_shell.addOrReplaceChild("r_bracer_blade", CubeListBuilder.create().texOffs(78, 91).addBox(-0.55F, -3.4F, -1.55F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.45F, 2.4F, 0.1F, 0.0F, 0.0F, -0.1309F));

		PartDefinition r_hand_claw = right_arm.addOrReplaceChild("r_hand_claw", CubeListBuilder.create().texOffs(100, 0).addBox(-2.7F, -1.05F, -1.2F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 9.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 69).addBox(-0.75F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition l_pauldron = left_arm.addOrReplaceChild("l_pauldron", CubeListBuilder.create().texOffs(0, 56).addBox(-1.2F, -3.0F, -3.6F, 6.0F, 5.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(65, 73).addBox(3.4F, -3.6F, -2.8F, 2.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(11, 97).addBox(1.85F, -5.2F, -2.7F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(89, 98).addBox(0.55F, -3.7F, 0.7F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition l_pauldron_spur_r1 = l_pauldron.addOrReplaceChild("l_pauldron_spur_r1", CubeListBuilder.create().texOffs(0, 96).mirror().addBox(3.6F, -5.2F, -1.2F, 2.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.75F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition l_forearm_shell = left_arm.addOrReplaceChild("l_forearm_shell", CubeListBuilder.create().texOffs(70, 17).addBox(-1.8F, -1.2F, -2.8F, 4.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(82, 83).addBox(-2.0F, -2.0F, -2.4F, 4.0F, 1.8F, 4.8F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 4.0F, 0.0F));

		PartDefinition l_bracer_blade = l_forearm_shell.addOrReplaceChild("l_bracer_blade", CubeListBuilder.create().texOffs(62, 94).addBox(-1.45F, -3.4F, -1.55F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.45F, 2.4F, 0.1F, 0.0F, 0.0F, 0.1309F));

		PartDefinition l_hand_claw = left_arm.addOrReplaceChild("l_hand_claw", CubeListBuilder.create().texOffs(73, 100).addBox(0.7F, -1.05F, -1.2F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 9.0F, 0.0F));
		} else if (slot == EquipmentSlot.LEGS) {
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(27, 66).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition r_thigh_shell = right_leg.addOrReplaceChild("r_thigh_shell", CubeListBuilder.create().texOffs(85, 9).addBox(-2.1F, -3.4F, -2.8F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 97).addBox(-1.45F, 2.0F, -2.6F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition r_thigh_outer_shell = r_thigh_shell.addOrReplaceChild("r_thigh_outer_shell", CubeListBuilder.create().texOffs(0, 86).addBox(-0.65F, -2.3F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.15F, -0.7F, 0.2F, 0.0F, 0.0F, 0.3054F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(68, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition l_thigh_shell = left_leg.addOrReplaceChild("l_thigh_shell", CubeListBuilder.create().texOffs(91, 17).addBox(-1.9F, -3.4F, -2.8F, 4.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(98, 44).addBox(-1.55F, 2.0F, -2.6F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition l_thigh_outer_shell = l_thigh_shell.addOrReplaceChild("l_thigh_outer_shell", CubeListBuilder.create().texOffs(36, 89).addBox(-1.35F, -2.3F, -2.0F, 2.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.15F, -0.7F, 0.2F, 0.0F, 0.0F, -0.3054F));
		} else if (slot == EquipmentSlot.FEET) {
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition r_shin_shell = right_leg.addOrReplaceChild("r_shin_shell", CubeListBuilder.create().texOffs(49, 89).addBox(-2.8F, -1.2F, -1.95F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(2, 108).addBox(-1.6F, -2.4F, 1.3F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(79, 113).addBox(-3.0F, -3.4F, -0.9F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition r_calf_back_shell_r1 = r_shin_shell.addOrReplaceChild("r_calf_back_shell_r1", CubeListBuilder.create().texOffs(2, 108).addBox(-1.6F, -2.4F, 1.8F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.75F, -1.0F, -0.1745F, 0.0F, -3.1416F));

		PartDefinition r_calf_back_shell_r2 = r_shin_shell.addOrReplaceChild("r_calf_back_shell_r2", CubeListBuilder.create().texOffs(2, 108).addBox(-1.6F, -2.4F, 1.8F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.25F, -0.75F, 1.0908F, 0.0F, 0.0F));

		PartDefinition r_shin_front_shell = right_leg.addOrReplaceChild("r_shin_front_shell", CubeListBuilder.create().texOffs(89, 91).addBox(-1.35F, -2.1F, -0.9F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1F, 8.6F, -1.8F, 0.2618F, 0.0F, 0.0F));

		PartDefinition r_boot_shell = right_leg.addOrReplaceChild("r_boot_shell", CubeListBuilder.create().texOffs(81, 36).addBox(-2.0F, -1.0F, -2.8F, 4.0F, 2.2F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(100, 32).addBox(-0.45F, -0.8F, -4.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1F, 11.0F, 0.0F));

		PartDefinition r_heel_spur = r_boot_shell.addOrReplaceChild("r_heel_spur", CubeListBuilder.create().texOffs(79, 113).addBox(-2.65F, -0.9F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition r_heel_spur_r1 = r_heel_spur.addOrReplaceChild("r_heel_spur_r1", CubeListBuilder.create().texOffs(98, 9).addBox(-2.25F, -1.3F, -0.9F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.05F, 0.1F, -0.1F, -0.0122F, 0.0175F, 0.8316F));

		PartDefinition r_toe_claw = r_boot_shell.addOrReplaceChild("r_toe_claw", CubeListBuilder.create().texOffs(93, 32).addBox(-2.4F, 0.1F, -4.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.75F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition l_shin_shell = left_leg.addOrReplaceChild("l_shin_shell", CubeListBuilder.create().texOffs(90, 54).addBox(0.8F, -1.45F, -1.95F, 2.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(49, 113).addBox(-1.4F, -2.4F, 1.3F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(22, 109).addBox(0.9F, -3.4F, -0.9F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition l_calf_back_shell_r1 = l_shin_shell.addOrReplaceChild("l_calf_back_shell_r1", CubeListBuilder.create().texOffs(49, 113).addBox(-5.2F, -2.4F, 1.8F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.8F, -4.75F, -1.0F, -0.1745F, 0.0F, -3.1416F));

		PartDefinition l_calf_back_shell_r2 = l_shin_shell.addOrReplaceChild("l_calf_back_shell_r2", CubeListBuilder.create().texOffs(49, 113).addBox(-1.4F, -2.4F, 1.8F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.25F, -0.75F, 1.0908F, 0.0F, 0.0F));

		PartDefinition l_shin_front_shell = left_leg.addOrReplaceChild("l_shin_front_shell", CubeListBuilder.create().texOffs(93, 25).addBox(-1.65F, -2.1F, -0.9F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.1F, 8.6F, -1.8F, 0.2618F, 0.0F, 0.0F));

		PartDefinition l_boot_shell = left_leg.addOrReplaceChild("l_boot_shell", CubeListBuilder.create().texOffs(44, 81).addBox(-2.0F, -1.0F, -2.8F, 4.0F, 2.2F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(100, 37).addBox(-1.55F, -0.8F, -4.8F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1F, 11.0F, 0.0F));

		PartDefinition l_heel_spur = l_boot_shell.addOrReplaceChild("l_heel_spur", CubeListBuilder.create().texOffs(79, 113).mirror().addBox(0.65F, -0.9F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition l_heel_spur_r1 = l_heel_spur.addOrReplaceChild("l_heel_spur_r1", CubeListBuilder.create().texOffs(98, 49).addBox(-0.75F, -1.3F, -0.9F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.05F, 0.1F, -0.1F, -0.0122F, -0.0175F, -0.8316F));

		PartDefinition l_toe_claw = l_boot_shell.addOrReplaceChild("l_toe_claw", CubeListBuilder.create().texOffs(42, 99).addBox(1.4F, 0.1F, -4.4F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.75F, 0.0F, 0.0F));
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
