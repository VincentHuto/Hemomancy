package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.util.Lazy;

public class EdaciousBloodLustArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation EDACIOUS_BLOOD_LUST_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("edacious_blood_lust_helmet"), "main");
	public static final ModelLayerLocation EDACIOUS_BLOOD_LUST_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("edacious_blood_lust_chest"), "main");
	public static final ModelLayerLocation EDACIOUS_BLOOD_LUST_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("edacious_blood_lust_leggings"), "main");
	public static final ModelLayerLocation EDACIOUS_BLOOD_LUST_FEET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("edacious_blood_lust_boots"), "main");

	public static final Lazy<EdaciousBloodLustArmorModel<LivingEntity>> helmet = Lazy
			.of(() -> new EdaciousBloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EDACIOUS_BLOOD_LUST_HELMET_LAYER)));
	public static final Lazy<EdaciousBloodLustArmorModel<LivingEntity>> chest = Lazy
			.of(() -> new EdaciousBloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EDACIOUS_BLOOD_LUST_CHEST_LAYER)));
	public static final Lazy<EdaciousBloodLustArmorModel<LivingEntity>> legs = Lazy
			.of(() -> new EdaciousBloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EDACIOUS_BLOOD_LUST_LEGS_LAYER)));
	public static final Lazy<EdaciousBloodLustArmorModel<LivingEntity>> boots = Lazy
			.of(() -> new EdaciousBloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(EDACIOUS_BLOOD_LUST_FEET_LAYER)));

	private final ModelPart rightUpperWing;
	private final ModelPart rightMiddleWing;
	private final ModelPart rightLowerWing;
	private final ModelPart leftUpperWing;
	private final ModelPart leftMiddleWing;
	private final ModelPart leftLowerWing;
	private final ModelPart wings;
	private final ModelPart rightEye;
	private final ModelPart leftEye;
	private final ModelPart rightFeeler;
	private final ModelPart rightFeelerTip;
	private final ModelPart leftFeeler;
	private final ModelPart leftFeelerTip;
	private boolean renderWingsInMainPass = true;

	private static final float EYE_BASE_X = 0.2182F;
	private static final float RIGHT_EYE_BASE_Y = -0.2182F;
	private static final float LEFT_EYE_BASE_Y = 0.2182F;
	private static final float LEFT_FEELER_BASE_X = 0.6563F;
	private static final float LEFT_FEELER_BASE_Y = -0.0692F;
	private static final float LEFT_FEELER_BASE_Z = -0.0532F;
	private static final float RIGHT_FEELER_BASE_X = 0.6563F;
	private static final float RIGHT_FEELER_BASE_Y = 0.0692F;
	private static final float RIGHT_FEELER_BASE_Z = 0.0532F;
	private static final float LEFT_FEELER_TIP_BASE_X = 0.125F;
	private static final float LEFT_FEELER_TIP_BASE_Y = -0.2988F;
	private static final float LEFT_FEELER_TIP_BASE_Z = -0.4147F;
	private static final float RIGHT_FEELER_TIP_BASE_X = 0.125F;
	private static final float RIGHT_FEELER_TIP_BASE_Y = 0.2988F;
	private static final float RIGHT_FEELER_TIP_BASE_Z = 0.4147F;

	public EdaciousBloodLustArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);
		this.wings = childOrNull(this.body, "wings");
		ModelPart rightWings = childOrNull(this.wings, "right_wings");
		ModelPart leftWings = childOrNull(this.wings, "left_wings");
		this.rightUpperWing = childOrNull(rightWings, "right_upper_wing");
		this.rightMiddleWing = childOrNull(rightWings, "right_middle_wing");
		this.rightLowerWing = childOrNull(rightWings, "right_lower_wing");
		this.leftUpperWing = childOrNull(leftWings, "left_upper_wing");
		this.leftMiddleWing = childOrNull(leftWings, "left_middle_wing");
		this.leftLowerWing = childOrNull(leftWings, "left_lower_wing");
		ModelPart compoundEyes = childOrNull(this.head, "compound_eyes");
		this.rightEye = childOrNull(compoundEyes, "right_large_red_compound_eye");
		this.leftEye = childOrNull(compoundEyes, "left_large_red_compound_eye");
		ModelPart proboscis = childOrNull(this.head, "proboscis");
		this.rightFeeler = childOrNull(proboscis, "right_feeler");
		this.rightFeelerTip = childOrNull(this.rightFeeler, "right_feeler2");
		this.leftFeeler = childOrNull(proboscis, "left_feeler");
		this.leftFeelerTip = childOrNull(this.leftFeeler, "left_feeler2");
	}

	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		if (slot.equals(EquipmentSlot.HEAD)) {
PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 20).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.35F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition mosquito_helmet = head.addOrReplaceChild("mosquito_helmet", CubeListBuilder.create().texOffs(58, 55).addBox(-2.5F, -3.8F, -5.1F, 5.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -5.15F, -3.7F, 6.0F, 10.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition compound_eyes = head.addOrReplaceChild("compound_eyes", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -2.0F));

		PartDefinition right_large_red_compound_eye = compound_eyes.addOrReplaceChild("right_large_red_compound_eye", CubeListBuilder.create().texOffs(116, 36).addBox(-1.5F, -1.5F, -1.25F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(-3.675F, -1.25F, 0.2F, 0.2182F, -0.2182F, 0.0F));

		PartDefinition left_large_red_compound_eye = compound_eyes.addOrReplaceChild("left_large_red_compound_eye", CubeListBuilder.create().texOffs(116, 43).addBox(-1.5F, -1.5F, -1.25F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.35F)), PartPose.offsetAndRotation(3.675F, -1.25F, 0.2F, 0.2182F, 0.2182F, 0.0F));

		PartDefinition proboscis = head.addOrReplaceChild("proboscis", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -2.0842F, -5.9108F, -0.2618F, 0.0F, 0.0F));

		PartDefinition proboscis_tip_r1 = proboscis.addOrReplaceChild("proboscis_tip_r1", CubeListBuilder.create().texOffs(103, 116).addBox(-1.5F, -0.85F, -0.8F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.3766F, 0.1443F, 0.7854F, 0.0F, 0.0F));

		PartDefinition proboscis_lower_curve_r1 = proboscis.addOrReplaceChild("proboscis_lower_curve_r1", CubeListBuilder.create().texOffs(116, 120).addBox(-1.5F, -0.95F, -1.45F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.9658F, 0.6108F, 0.6109F, 0.0F, 0.0F));

		PartDefinition proboscis_upper_needle_r1 = proboscis.addOrReplaceChild("proboscis_upper_needle_r1", CubeListBuilder.create().texOffs(66, 111).addBox(-0.5F, -0.4F, -5.6F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.3342F, -0.0892F, 1.0036F, 0.0F, 0.0F));

		PartDefinition left_feeler = proboscis.addOrReplaceChild("left_feeler", CubeListBuilder.create(), PartPose.offsetAndRotation(1.0F, -2.0613F, 0.9653F, 0.6563F, -0.0692F, -0.0532F));

		PartDefinition cube_r1 = left_feeler.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(51, 83).addBox(0.0F, -4.0F, -0.5F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5941F, 0.1195F, 0.0918F, 0.7543F, -0.2443F, 0.2519F));

		PartDefinition left_feeler2 = left_feeler.addOrReplaceChild("left_feeler2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.4518F, -2.3539F, -1.9662F, 0.125F, -0.2988F, -0.4147F));

		PartDefinition cube_r2 = left_feeler2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(72, 119).addBox(0.0F, -4.0F, -3.5F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0131F, 2.585F, 1.9535F, 0.7366F, -0.148F, 0.3424F));

		PartDefinition right_feeler = proboscis.addOrReplaceChild("right_feeler", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.0F, -2.0613F, 0.9653F, 0.6563F, 0.0692F, 0.0532F));

		PartDefinition cube_r3 = right_feeler.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(51, 83).mirror().addBox(0.0F, -4.0F, -0.5F, 0.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5941F, 0.1195F, 0.0918F, 0.7543F, 0.2443F, -0.2519F));

		PartDefinition right_feeler2 = right_feeler.addOrReplaceChild("right_feeler2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.4518F, -2.3539F, -1.9662F, 0.125F, 0.2988F, 0.4147F));

		PartDefinition cube_r4 = right_feeler2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(72, 119).mirror().addBox(0.0F, -4.0F, -3.5F, 0.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(1.0131F, 2.585F, 1.9535F, 0.7366F, 0.148F, -0.3424F));

		PartDefinition right_antennae = head.addOrReplaceChild("right_antennae", CubeListBuilder.create().texOffs(46, 93).addBox(-0.5833F, 3.2667F, -5.2667F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(74, 43).addBox(-0.8333F, 0.6167F, -2.3667F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(94, 91).addBox(-0.0833F, -1.3833F, -3.3667F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(0, 55).addBox(1.9167F, -3.3833F, -3.3667F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4167F, -6.2667F, 1.1667F, 0.4498F, -0.2368F, -0.1128F));

		PartDefinition left_antennae = head.addOrReplaceChild("left_antennae", CubeListBuilder.create().texOffs(0, 95).addBox(-0.4167F, 3.2667F, -5.2667F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(54, 81).addBox(-0.1667F, 0.6167F, -2.3667F, 1.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(95, 16).addBox(-0.9167F, -1.3833F, -3.3667F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(29, 55).addBox(-2.9167F, -3.3833F, -3.3667F, 1.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4167F, -6.2667F, 1.1667F, 0.4498F, 0.2368F, 0.1128F));


		}

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		if (slot.equals(EquipmentSlot.CHEST)) {
		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(69, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_shell = body.addOrReplaceChild("chest_shell", CubeListBuilder.create().texOffs(30, 144).addBox(-4.5F, -4.25F, -2.8F, 9.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(29, 70).addBox(-2.0F, -4.25F, -3.75F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(123, 6).addBox(-1.0F, -2.9F, -3.3F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition mid_diagonal_rib_plate_r1 = chest_shell.addOrReplaceChild("mid_diagonal_rib_plate_r1", CubeListBuilder.create().texOffs(118, 145).addBox(1.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -2.5F, 5.1F, 0.48F, 0.0F, 0.0F));

		PartDefinition mid_diagonal_rib_plate_r2 = chest_shell.addOrReplaceChild("mid_diagonal_rib_plate_r2", CubeListBuilder.create().texOffs(118, 145).addBox(1.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(118, 145).addBox(6.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.5F, -4.5F, 5.1F, 0.48F, 0.0F, 0.0F));

		PartDefinition left_diagonal_rib_plate_r1 = chest_shell.addOrReplaceChild("left_diagonal_rib_plate_r1", CubeListBuilder.create().texOffs(70, 30).addBox(0.5F, -1.75F, -3.85F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.6935F, -2.1489F, 0.0F, 0.0F, 0.0F, -0.384F));

		PartDefinition right_diagonal_rib_plate_r1 = chest_shell.addOrReplaceChild("right_diagonal_rib_plate_r1", CubeListBuilder.create().texOffs(70, 17).addBox(-4.5F, -1.75F, -3.85F, 4.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.6935F, -2.1489F, 0.0F, 0.0F, 0.0F, 0.384F));

		PartDefinition back_spikes = chest_shell.addOrReplaceChild("back_spikes", CubeListBuilder.create(), PartPose.offset(-3.0F, 1.5F, 5.1F));

		PartDefinition mid_diagonal_rib_plate_r3 = back_spikes.addOrReplaceChild("mid_diagonal_rib_plate_r3", CubeListBuilder.create().texOffs(118, 145).addBox(1.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(118, 145).addBox(-0.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -4.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition mid_diagonal_rib_plate_r4 = back_spikes.addOrReplaceChild("mid_diagonal_rib_plate_r4", CubeListBuilder.create().texOffs(118, 145).addBox(1.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(118, 145).addBox(-3.5F, 0.25F, -3.85F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -6.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition mid_diagonal_rib_plate_r5 = back_spikes.addOrReplaceChild("mid_diagonal_rib_plate_r5", CubeListBuilder.create().texOffs(120, 147).addBox(1.5F, 0.25F, -3.85F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(120, 147).addBox(6.5F, 0.25F, -3.85F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, -2.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition mid_diagonal_rib_plate_r6 = back_spikes.addOrReplaceChild("mid_diagonal_rib_plate_r6", CubeListBuilder.create().texOffs(120, 147).addBox(1.5F, 0.25F, -3.85F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(120, 147).addBox(-0.5F, 0.25F, -3.85F, 1.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.48F, 0.0F, 0.0F));

		PartDefinition segmented_abdomen = body.addOrReplaceChild("segmented_abdomen", CubeListBuilder.create().texOffs(54, 71).addBox(-3.5F, -3.1F, -2.9F, 7.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(94, 82).addBox(-3.0F, -0.65F, -2.3F, 6.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(102, 51).addBox(-2.5F, 2.05F, -2.35F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 9.0F, -0.1F));

		PartDefinition wing_roots_and_spine = body.addOrReplaceChild("wing_roots_and_spine", CubeListBuilder.create().texOffs(13, 112).addBox(-1.0F, -4.8F, -2.1F, 2.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 37).addBox(-2.5F, 5.0F, -2.5F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 3.5F));

		PartDefinition wings = body.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 4.0F));

		PartDefinition right_wings = wings.addOrReplaceChild("right_wings", CubeListBuilder.create(), PartPose.offset(-3.7F, -2.5F, -0.4F));

		PartDefinition right_upper_wing = right_wings.addOrReplaceChild("right_upper_wing", CubeListBuilder.create().texOffs(31, 0).addBox(-16.15F, -1.8F, 0.51F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(115, 99).addBox(-4.4F, 0.1F, 0.05F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(104, 77).addBox(-12.4F, -0.9F, 0.05F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(123, 76).addBox(-15.4F, 0.1F, 0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(23, 94).addBox(-16.4F, 1.1F, 0.05F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8742F, 0.6258F, 1.1812F));

		PartDefinition right_middle_wing = right_wings.addOrReplaceChild("right_middle_wing", CubeListBuilder.create().texOffs(33, 11).addBox(-16.15F, -1.8F, 0.51F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(116, 24).addBox(-4.4F, 0.1F, 0.05F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(110, 13).addBox(-12.4F, -0.9F, 0.05F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(99, 125).addBox(-15.4F, 0.1F, 0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(21, 97).addBox(-16.4F, 1.1F, 0.05F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1399F, 0.9916F, 0.1419F));

		PartDefinition right_lower_wing = right_wings.addOrReplaceChild("right_lower_wing", CubeListBuilder.create().texOffs(33, 22).addBox(-16.15F, -1.8F, 0.51F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(117, 87).addBox(-4.4F, 0.1F, 0.05F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(112, 110).addBox(-12.4F, -0.9F, 0.05F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(9, 126).addBox(-15.4F, 0.1F, 0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(97, 51).addBox(-16.4F, 1.1F, 0.05F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7049F, 0.6012F, -0.976F));

		PartDefinition left_wings = wings.addOrReplaceChild("left_wings", CubeListBuilder.create(), PartPose.offset(3.7F, -2.5F, -0.4F));

		PartDefinition left_upper_wing = left_wings.addOrReplaceChild("left_upper_wing", CubeListBuilder.create().texOffs(31, 0).mirror().addBox(-1.85F, -1.8F, 0.51F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(115, 99).mirror().addBox(-0.6F, 0.1F, 0.05F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(104, 77).mirror().addBox(4.4F, -0.9F, 0.05F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(123, 76).mirror().addBox(12.4F, 0.1F, 0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(23, 94).mirror().addBox(15.4F, 1.1F, 0.05F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.8742F, -0.6258F, -1.1812F));

		PartDefinition left_middle_wing = left_wings.addOrReplaceChild("left_middle_wing", CubeListBuilder.create().texOffs(33, 11).mirror().addBox(-1.85F, -1.8F, 0.51F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(116, 24).mirror().addBox(-0.6F, 0.1F, 0.05F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(110, 13).mirror().addBox(4.4F, -0.9F, 0.05F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(99, 125).mirror().addBox(12.4F, 0.1F, 0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(21, 97).mirror().addBox(15.4F, 1.1F, 0.05F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1399F, -0.9916F, -0.1419F));

		PartDefinition left_lower_wing = left_wings.addOrReplaceChild("left_lower_wing", CubeListBuilder.create().texOffs(33, 22).mirror().addBox(-1.85F, -1.8F, 0.51F, 18.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(117, 87).mirror().addBox(-0.6F, 0.1F, 0.05F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(112, 110).mirror().addBox(4.4F, -0.9F, 0.05F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(9, 126).mirror().addBox(12.4F, 0.1F, 0.05F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(97, 51).mirror().addBox(15.4F, 1.1F, 0.05F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.7049F, -0.6012F, 0.976F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(85, 55).addBox(-3.0F, -0.2F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition r_jagged_shoulder = right_arm.addOrReplaceChild("r_jagged_shoulder", CubeListBuilder.create().texOffs(81, 71).addBox(-3.55F, -2.45F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition right_shoulder_knife_spike = r_jagged_shoulder.addOrReplaceChild("right_shoulder_knife_spike", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition right_shoulder_knife_spike_r1 = right_shoulder_knife_spike.addOrReplaceChild("right_shoulder_knife_spike_r1", CubeListBuilder.create().texOffs(37, 117).addBox(-6.05F, -2.35F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition right_shoulder_knife_spike2 = r_jagged_shoulder.addOrReplaceChild("right_shoulder_knife_spike2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition right_shoulder_knife_spike_r2 = right_shoulder_knife_spike2.addOrReplaceChild("right_shoulder_knife_spike_r2", CubeListBuilder.create().texOffs(0, 119).addBox(-6.05F, -2.35F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition r_forearm_spur = right_arm.addOrReplaceChild("r_forearm_spur", CubeListBuilder.create().texOffs(102, 59).addBox(-1.3F, 2.15F, -2.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(53, 104).addBox(-2.8F, -3.6F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(97, 39).addBox(-1.8F, -3.85F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 5.0F, 0.0F));

		PartDefinition right_hooked_forearm_spur_r1 = r_forearm_spur.addOrReplaceChild("right_hooked_forearm_spur_r1", CubeListBuilder.create().texOffs(127, 102).addBox(-2.45F, -2.4F, 0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(126, 126).addBox(-2.45F, -2.4F, -2.15F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -2.0F, 0.5F, 0.0F, 0.0F, -0.3578F));

		PartDefinition right_hooked_forearm_spur_r2 = r_forearm_spur.addOrReplaceChild("right_hooked_forearm_spur_r2", CubeListBuilder.create().texOffs(99, 128).addBox(-2.45F, -2.4F, 0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.0F, -1.75F, 0.1745F, 0.0F, -0.3578F));

		PartDefinition right_hooked_forearm_spur_r3 = r_forearm_spur.addOrReplaceChild("right_hooked_forearm_spur_r3", CubeListBuilder.create().texOffs(128, 90).addBox(-2.45F, -2.4F, 0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 2.0F, 0.5F, -0.1745F, 0.0F, -0.3578F));

		PartDefinition r_claw_hand = right_arm.addOrReplaceChild("r_claw_hand", CubeListBuilder.create().texOffs(22, 112).addBox(-1.65F, -1.1F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(86, 102).addBox(-1.95F, 0.45F, -3.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 9.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(93, 0).addBox(-0.75F, -0.25F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.35F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition l_jagged_shoulder = left_arm.addOrReplaceChild("l_jagged_shoulder", CubeListBuilder.create().texOffs(0, 84).addBox(-1.45F, -2.45F, -3.0F, 5.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition left_shoulder_knife_spike = l_jagged_shoulder.addOrReplaceChild("left_shoulder_knife_spike", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition left_shoulder_knife_spike_r1 = left_shoulder_knife_spike.addOrReplaceChild("left_shoulder_knife_spike_r1", CubeListBuilder.create().texOffs(22, 120).addBox(2.05F, -2.35F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition left_shoulder_knife_spike2 = l_jagged_shoulder.addOrReplaceChild("left_shoulder_knife_spike2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 3.5F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition left_shoulder_knife_spike_r2 = left_shoulder_knife_spike2.addOrReplaceChild("left_shoulder_knife_spike_r2", CubeListBuilder.create().texOffs(103, 120).addBox(2.05F, -2.35F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition l_forearm_spur = left_arm.addOrReplaceChild("l_forearm_spur", CubeListBuilder.create().texOffs(104, 68).addBox(-1.7F, 2.15F, -2.5F, 3.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(0, 106).addBox(-0.2F, -3.6F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(21, 100).addBox(-2.2F, -3.85F, -2.5F, 4.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 5.0F, 0.0F));

		PartDefinition left_hooked_forearm_spur_r1 = l_forearm_spur.addOrReplaceChild("left_hooked_forearm_spur_r1", CubeListBuilder.create().texOffs(128, 79).addBox(1.45F, -2.4F, 0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(127, 113).addBox(1.45F, -2.4F, -2.15F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -2.0F, 0.5F, 0.0F, 0.0F, 0.3578F));

		PartDefinition left_hooked_forearm_spur_r2 = l_forearm_spur.addOrReplaceChild("left_hooked_forearm_spur_r2", CubeListBuilder.create().texOffs(5, 129).addBox(1.45F, -2.4F, 0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 2.0F, -1.75F, 0.1745F, 0.0F, 0.3578F));

		PartDefinition left_hooked_forearm_spur_r3 = l_forearm_spur.addOrReplaceChild("left_hooked_forearm_spur_r3", CubeListBuilder.create().texOffs(0, 129).addBox(1.45F, -2.4F, 0.1F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 2.0F, 0.5F, -0.1745F, 0.0F, 0.3578F));

		PartDefinition l_claw_hand = left_arm.addOrReplaceChild("l_claw_hand", CubeListBuilder.create().texOffs(112, 102).addBox(-1.35F, -1.1F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(99, 102).addBox(1.95F, 0.45F, -3.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 9.0F, 0.0F));


		} else if (slot.equals(EquipmentSlot.LEGS)) {
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(77, 82).addBox(-2.1F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition r_thigh_chitin = right_leg.addOrReplaceChild("r_thigh_chitin", CubeListBuilder.create().texOffs(115, 91).addBox(-1.5F, -3.1F, -2.5F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(70, 152).addBox(-2.8F, 2.9F, -1.35F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(81, 116).addBox(-2.8F, -3.1F, -1.35F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition right_outer_thigh_blade_r1 = r_thigh_chitin.addOrReplaceChild("right_outer_thigh_blade_r1", CubeListBuilder.create().texOffs(108, 159).addBox(-3.3F, -3.1F, -0.35F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.25F, 5.0F, 0.0F, 0.0F, 0.0F, -0.3054F));

		PartDefinition right_thigh_front_chitin_r1 = r_thigh_chitin.addOrReplaceChild("right_thigh_front_chitin_r1", CubeListBuilder.create().texOffs(95, 150).addBox(0.0F, -3.1F, -2.75F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.0F, -0.75F, 0.3927F, 0.0F, 0.0F));

		PartDefinition r_shin_chitin = right_leg.addOrReplaceChild("r_shin_chitin", CubeListBuilder.create().texOffs(121, 66).addBox(-1.55F, -3.3F, -2.7F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(50, 117).addBox(-1.55F, -0.4F, -2.9F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(123, 50).addBox(-0.75F, -2.05F, 1.6F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition right_waist = right_leg.addOrReplaceChild("right_waist", CubeListBuilder.create(), PartPose.offset(1.9F, 2.0F, 1.25F));

		PartDefinition front_right_tattered_strip_r1 = right_waist.addOrReplaceChild("front_right_tattered_strip_r1", CubeListBuilder.create().texOffs(55, 124).addBox(-3.1F, -2.1F, -2.35F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.0F, 0.0F, -0.0698F));

		PartDefinition back_right_long_blood_tendril_r1 = right_waist.addOrReplaceChild("back_right_long_blood_tendril_r1", CubeListBuilder.create().texOffs(46, 83).addBox(-2.65F, -2.25F, 1.15F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(77, 82).mirror().addBox(-1.9F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.1F)).mirror(false), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition l_thigh_chitin = left_leg.addOrReplaceChild("l_thigh_chitin", CubeListBuilder.create().texOffs(115, 91).mirror().addBox(-1.5F, -3.1F, -2.5F, 3.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(70, 152).mirror().addBox(0.8F, 2.9F, -1.35F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(81, 116).mirror().addBox(0.8F, -3.1F, -1.35F, 2.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 3.0F, 0.0F));

		PartDefinition left_outer_thigh_blade_r1 = l_thigh_chitin.addOrReplaceChild("left_outer_thigh_blade_r1", CubeListBuilder.create().texOffs(108, 159).mirror().addBox(2.3F, -3.1F, -0.35F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.25F, 5.0F, 0.0F, 0.0F, 0.0F, 0.3054F));

		PartDefinition left_thigh_front_chitin_r1 = l_thigh_chitin.addOrReplaceChild("left_thigh_front_chitin_r1", CubeListBuilder.create().texOffs(95, 150).mirror().addBox(-1.0F, -3.1F, -2.75F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.5F, 0.0F, -0.75F, 0.3927F, 0.0F, 0.0F));

		PartDefinition l_shin_chitin = left_leg.addOrReplaceChild("l_shin_chitin", CubeListBuilder.create().texOffs(121, 66).mirror().addBox(-1.45F, -3.3F, -2.7F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(50, 117).mirror().addBox(-1.45F, -0.4F, -2.9F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(123, 50).mirror().addBox(-1.25F, -2.05F, 1.6F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition left_waist = left_leg.addOrReplaceChild("left_waist", CubeListBuilder.create(), PartPose.offset(-1.9F, 2.0F, 1.25F));

		PartDefinition front_left_tattered_strip_r1 = left_waist.addOrReplaceChild("front_left_tattered_strip_r1", CubeListBuilder.create().texOffs(55, 124).mirror().addBox(2.1F, -2.1F, -2.35F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -1.5F, 0.0F, 0.0F, 0.0698F));

		PartDefinition back_left_long_blood_tendril_r1 = left_waist.addOrReplaceChild("back_left_long_blood_tendril_r1", CubeListBuilder.create().texOffs(46, 83).mirror().addBox(1.65F, -2.25F, 1.15F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.1396F));


		} else if (slot.equals(EquipmentSlot.FEET)) {
		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition r_hooked_foot = right_leg.addOrReplaceChild("r_hooked_foot", CubeListBuilder.create().texOffs(15, 37).addBox(-1.5F, -0.75F, -2.55F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(117, 80).addBox(-0.5F, -0.85F, -6.4F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(90, 125).addBox(-1.75F, 0.15F, -5.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(81, 125).addBox(0.75F, 0.15F, -5.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1F, 11.0F, 0.0F));

		PartDefinition right_rear_heel_spur_r1 = r_hooked_foot.addOrReplaceChild("right_rear_heel_spur_r1", CubeListBuilder.create().texOffs(15, 135).addBox(-2.15F, -0.35F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition right_rear_heel_spur_r2 = r_hooked_foot.addOrReplaceChild("right_rear_heel_spur_r2", CubeListBuilder.create().texOffs(9, 147).addBox(-2.15F, -0.35F, 1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.25F, -0.25F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition right_rear_heel_spur_r3 = r_hooked_foot.addOrReplaceChild("right_rear_heel_spur_r3", CubeListBuilder.create().texOffs(8, 161).addBox(-2.15F, -0.35F, 1.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition l_hooked_foot = left_leg.addOrReplaceChild("l_hooked_foot", CubeListBuilder.create().texOffs(15, 37).mirror().addBox(-1.5F, -0.75F, -2.55F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(117, 80).mirror().addBox(-0.5F, -0.85F, -6.4F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(90, 125).mirror().addBox(0.75F, 0.15F, -5.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(81, 125).mirror().addBox(-1.75F, 0.15F, -5.4F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.1F, 11.0F, 0.0F));

		PartDefinition left_rear_heel_spur_r1 = l_hooked_foot.addOrReplaceChild("left_rear_heel_spur_r1", CubeListBuilder.create().texOffs(15, 135).mirror().addBox(1.15F, -0.35F, 1.0F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.0F, -3.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition left_rear_heel_spur_r2 = l_hooked_foot.addOrReplaceChild("left_rear_heel_spur_r2", CubeListBuilder.create().texOffs(9, 147).mirror().addBox(1.15F, -0.35F, 1.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.25F, -0.25F, 0.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition left_rear_heel_spur_r3 = l_hooked_foot.addOrReplaceChild("left_rear_heel_spur_r3", CubeListBuilder.create().texOffs(8, 161).mirror().addBox(1.15F, -0.35F, 1.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.3491F, 0.0F, 0.0F));

		}

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		animateArmorDetails(entity, ageInTicks);
	}

	public void animateArmorDetails(LivingEntity entity, float ageInTicks) {
		animateHeadDetails(ageInTicks);
		animateWings(entity, ageInTicks);
	}

	public void setRenderWingsInMainPass(boolean renderWingsInMainPass) {
		this.renderWingsInMainPass = renderWingsInMainPass;
	}

	public void renderWingsToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int packedColor) {
		if (this.wings == null) {
			return;
		}

		poseStack.pushPose();
		this.body.translateAndRotate(poseStack);
		this.wings.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		poseStack.popPose();
	}

	private void animateHeadDetails(float ageInTicks) {
		if (this.rightEye != null) {
			this.rightEye.xRot = EYE_BASE_X + Mth.sin(ageInTicks * 0.11F + 1.7F) * 0.08F;
			this.rightEye.yRot = RIGHT_EYE_BASE_Y + Mth.sin(ageInTicks * 0.17F) * 0.18F;
			this.rightEye.zRot = Mth.sin(ageInTicks * 0.07F + 0.8F) * 0.035F;
		}
		if (this.leftEye != null) {
			this.leftEye.xRot = EYE_BASE_X + Mth.sin(ageInTicks * 0.13F + 3.1F) * 0.08F;
			this.leftEye.yRot = LEFT_EYE_BASE_Y + Mth.sin(ageInTicks * 0.19F + 2.4F) * 0.18F;
			this.leftEye.zRot = Mth.sin(ageInTicks * 0.09F + 2.2F) * 0.035F;
		}
		if (this.rightFeeler != null) {
			this.rightFeeler.xRot = RIGHT_FEELER_BASE_X + Mth.sin(ageInTicks * 0.16F + 0.5F) * 0.045F;
			this.rightFeeler.yRot = RIGHT_FEELER_BASE_Y + Mth.sin(ageInTicks * 0.21F + 1.1F) * 0.11F;
			this.rightFeeler.zRot = RIGHT_FEELER_BASE_Z + Mth.cos(ageInTicks * 0.18F + 0.4F) * 0.075F;
		}
		if (this.rightFeelerTip != null) {
			this.rightFeelerTip.xRot = RIGHT_FEELER_TIP_BASE_X + Mth.sin(ageInTicks * 0.22F + 2.3F) * 0.08F;
			this.rightFeelerTip.yRot = RIGHT_FEELER_TIP_BASE_Y + Mth.cos(ageInTicks * 0.18F + 0.7F) * 0.09F;
			this.rightFeelerTip.zRot = RIGHT_FEELER_TIP_BASE_Z + Mth.sin(ageInTicks * 0.24F + 1.6F) * 0.08F;
		}
		if (this.leftFeeler != null) {
			this.leftFeeler.xRot = LEFT_FEELER_BASE_X + Mth.sin(ageInTicks * 0.14F + 2.0F) * 0.045F;
			this.leftFeeler.yRot = LEFT_FEELER_BASE_Y + Mth.sin(ageInTicks * 0.20F + 3.0F) * 0.11F;
			this.leftFeeler.zRot = LEFT_FEELER_BASE_Z + Mth.cos(ageInTicks * 0.17F + 2.1F) * 0.075F;
		}
		if (this.leftFeelerTip != null) {
			this.leftFeelerTip.xRot = LEFT_FEELER_TIP_BASE_X + Mth.sin(ageInTicks * 0.23F + 0.9F) * 0.08F;
			this.leftFeelerTip.yRot = LEFT_FEELER_TIP_BASE_Y + Mth.cos(ageInTicks * 0.19F + 2.8F) * 0.09F;
			this.leftFeelerTip.zRot = LEFT_FEELER_TIP_BASE_Z + Mth.sin(ageInTicks * 0.25F + 3.4F) * 0.08F;
		}
	}

	public void animateWings(LivingEntity entity, float ageInTicks) {
		if (this.rightUpperWing == null || this.rightMiddleWing == null || this.rightLowerWing == null
				|| this.leftUpperWing == null || this.leftMiddleWing == null || this.leftLowerWing == null) {
			return;
		}

		boolean flying = entity.isFallFlying() || entity instanceof Player player && player.getAbilities().flying;
		if (flying) {
			float phase = ageInTicks * 1.15F;
			float upperBeat = Mth.sin(phase - 0.18F) * 0.55F;
			float middleBeat = Mth.sin(phase + 0.08F) * 0.4F;
			float lowerBeat = Mth.sin(phase + 0.26F) * 0.3F;
			float upperLift = Mth.cos(phase - 0.18F) * 0.23F;
			float middleLift = Mth.cos(phase + 0.08F) * 0.20F;
			float lowerLift = Mth.cos(phase + 0.26F) * 0.028F;
			setWingPose(this.rightUpperWing, 0.74F + upperLift, 0.64F, 1.10F + upperBeat);
			setWingPose(this.rightMiddleWing, 0.20F + middleLift, 0.88F, 0.18F + middleBeat);
			setWingPose(this.rightLowerWing, -0.78F + lowerLift, 0.62F, -0.82F + lowerBeat);
			setWingPose(this.leftUpperWing, 0.74F + upperLift, -0.64F, -1.10F - upperBeat);
			setWingPose(this.leftMiddleWing, 0.20F + middleLift, -0.88F, -0.18F - middleBeat);
			setWingPose(this.leftLowerWing, -0.78F + lowerLift, -0.62F, 0.82F - lowerBeat);
		} else {
			setWingPose(this.rightUpperWing, 1.35F, 2.35F, 1.55F);
			setWingPose(this.rightMiddleWing, 1.48F, 2.45F, 1.35F);
			setWingPose(this.rightLowerWing, 1.62F, 2.55F, 1.15F);
			setWingPose(this.leftUpperWing, 1.35F, -2.35F, -1.55F);
			setWingPose(this.leftMiddleWing, 1.48F, -2.45F, -1.35F);
			setWingPose(this.leftLowerWing, 1.62F, -2.55F, -1.15F);
		}
	}

	private static ModelPart childOrNull(ModelPart parent, String childName) {
		return parent != null && parent.hasChild(childName) ? parent.getChild(childName) : null;
	}

	private static void setWingPose(ModelPart wing, float xRot, float yRot, float zRot) {
		wing.xRot = xRot;
		wing.yRot = yRot;
		wing.zRot = zRot;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
		head.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		if (this.wings != null && !this.renderWingsInMainPass) {
			this.wings.visible = false;
			body.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
			this.wings.visible = true;
		} else {
			body.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		}
		leftArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		rightArm.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		rightLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		leftLeg.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
