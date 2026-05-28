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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;

public class SilentArchonArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation SILENT_ARCHON_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_helmet"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> helmet = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_HELMET_LAYER), EquipmentSlot.HEAD));

	public static final ModelLayerLocation SILENT_ARCHON_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_chest"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> chest = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_CHEST_LAYER), EquipmentSlot.CHEST));

	public static final ModelLayerLocation SILENT_ARCHON_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_legs"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> legs = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_LEGS_LAYER), EquipmentSlot.LEGS));

	public static final ModelLayerLocation SILENT_ARCHON_BOOTS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_boots"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> boots = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_BOOTS_LAYER), EquipmentSlot.FEET));

	private final EquipmentSlot renderSlot;
	private final ModelPart beltR;
	private final ModelPart beltL;

	public SilentArchonArmorModel(ModelPart root, EquipmentSlot renderSlot) {
		super(root, RenderType::entityTranslucent);
		this.renderSlot = renderSlot;
		this.beltR = root.getChild("BeltR");
		this.beltL = root.getChild("BeltL");
	}

public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition BeltR = partdefinition.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = partdefinition.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Hood1 = head.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(16, 7).addBox(-4.5F, -9.0F, -4.6F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Hood2 = head.addOrReplaceChild("Hood2", CubeListBuilder.create().texOffs(52, 13).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

		PartDefinition Hood3 = head.addOrReplaceChild("Hood3", CubeListBuilder.create().texOffs(52, 14).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition Hood4 = head.addOrReplaceChild("Hood4", CubeListBuilder.create().texOffs(53, 15).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Chestthing = body.addOrReplaceChild("Chestthing", CubeListBuilder.create().texOffs(56, 50).addBox(-2.5F, 1.0F, -4.0F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -3.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltB = body.addOrReplaceChild("MbeltB", CubeListBuilder.create().texOffs(16, 55).addBox(-4.0F, 7.0F, -4.0F, 8.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -3.1416F, 0.0F));

		PartDefinition ClothchestL = body.addOrReplaceChild("ClothchestL", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ClothchestR = body.addOrReplaceChild("ClothchestR", CubeListBuilder.create().texOffs(108, 38).addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Book = body.addOrReplaceChild("Book", CubeListBuilder.create().texOffs(81, 16).addBox(-6.0F, 0.0F, 4.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7679F));

		PartDefinition Scroll = body.addOrReplaceChild("Scroll", CubeListBuilder.create().texOffs(78, 25).addBox(-6.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.192F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 1.9F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(16, 36).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(16, 36).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(16, 25).addBox(-4.0F, 1.0F, -3.0F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition FrontclothR1 = body.addOrReplaceChild("FrontclothR1", CubeListBuilder.create().texOffs(108, 38).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -2.9F, -0.1047F, 0.0F, 0.0F));

		PartDefinition FrontclothR2 = body.addOrReplaceChild("FrontclothR2", CubeListBuilder.create().texOffs(108, 47).addBox(-3.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, 11.0F, -2.9F, -0.3316F, 0.0F, 0.0F));

		PartDefinition FrontclothL1 = body.addOrReplaceChild("FrontclothL1", CubeListBuilder.create().texOffs(108, 38).mirror().addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.1047F, 0.0F, 0.0F));

		PartDefinition FrontclothL2 = body.addOrReplaceChild("FrontclothL2", CubeListBuilder.create().texOffs(108, 47).mirror().addBox(-3.0F, 7.5F, 1.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.0F, -2.9F, -0.3316F, 0.0F, 0.0F));

		PartDefinition ClothBackR1 = body.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(118, 16).mirror().addBox(-4.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 11.5F, 2.9F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackR2 = body.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(123, 9).addBox(-1.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackR3 = body.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(120, 12).mirror().addBox(-4.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(4.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL1 = body.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(118, 16).addBox(-4.0F, 0.0F, 0.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackL2 = body.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(123, 9).mirror().addBox(-4.0F, 7.8F, -0.9F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL3 = body.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(120, 12).addBox(-3.0F, 7.8F, -0.9F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 11.5F, 2.9F, 0.2269F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition SideclothL2 = left_leg.addOrReplaceChild("SideclothL2", CubeListBuilder.create().texOffs(116, 34).addBox(-1.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.2967F));

		PartDefinition SideclothL3 = left_leg.addOrReplaceChild("SideclothL3", CubeListBuilder.create().texOffs(116, 1).addBox(0.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition Focipouch = left_leg.addOrReplaceChild("Focipouch", CubeListBuilder.create().texOffs(100, 20).addBox(-6.5F, 0.5F, -2.5F, 3.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition SideclothL1 = left_leg.addOrReplaceChild("SideclothL1", CubeListBuilder.create().texOffs(116, 42).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition LegpanelL4 = left_leg.addOrReplaceChild("LegpanelL4", CubeListBuilder.create().texOffs(76, 38).mirror().addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL5 = left_leg.addOrReplaceChild("LegpanelL5", CubeListBuilder.create().texOffs(76, 42).mirror().addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL6 = left_leg.addOrReplaceChild("LegpanelL6", CubeListBuilder.create().texOffs(82, 38).mirror().addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition SidepanelL1 = left_leg.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(116, 25).addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderplateR1 = right_arm.addOrReplaceChild("ShoulderplateR1", CubeListBuilder.create().texOffs(56, 33).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR2 = right_arm.addOrReplaceChild("ShoulderplateR2", CubeListBuilder.create().texOffs(40, 33).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR3 = right_arm.addOrReplaceChild("ShoulderplateR3", CubeListBuilder.create().texOffs(40, 33).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateTopR = right_arm.addOrReplaceChild("ShoulderplateTopR", CubeListBuilder.create().texOffs(56, 25).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition RArm1 = right_arm.addOrReplaceChild("RArm1", CubeListBuilder.create().texOffs(88, 39).addBox(-1.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RArm2 = right_arm.addOrReplaceChild("RArm2", CubeListBuilder.create().texOffs(76, 32).addBox(-1.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition RArm3 = right_arm.addOrReplaceChild("RArm3", CubeListBuilder.create().texOffs(88, 32).addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderplateL1 = left_arm.addOrReplaceChild("ShoulderplateL1", CubeListBuilder.create().texOffs(56, 33).addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL2 = left_arm.addOrReplaceChild("ShoulderplateL2", CubeListBuilder.create().texOffs(40, 33).addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL3 = left_arm.addOrReplaceChild("ShoulderplateL3", CubeListBuilder.create().texOffs(40, 33).addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateTopL = left_arm.addOrReplaceChild("ShoulderplateTopL", CubeListBuilder.create().texOffs(56, 25).addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition LArm1 = left_arm.addOrReplaceChild("LArm1", CubeListBuilder.create().texOffs(88, 39).mirror().addBox(-3.5F, 2.5F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LArm2 = left_arm.addOrReplaceChild("LArm2", CubeListBuilder.create().texOffs(76, 32).addBox(-3.0F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LArm3 = left_arm.addOrReplaceChild("LArm3", CubeListBuilder.create().texOffs(88, 32).addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(16, 45).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition SideclothR1 = right_leg.addOrReplaceChild("SideclothR1", CubeListBuilder.create().texOffs(116, 42).addBox(1.5F, 0.5F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

		PartDefinition SideclothR2 = right_leg.addOrReplaceChild("SideclothR2", CubeListBuilder.create().texOffs(116, 34).addBox(0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.2967F));

		PartDefinition SideclothR3 = right_leg.addOrReplaceChild("SideclothR3", CubeListBuilder.create().texOffs(116, 1).addBox(-1.4F, 8.4F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition SidepanelR1 = right_leg.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(116, 25).mirror().addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition LegpanelR6 = right_leg.addOrReplaceChild("LegpanelR6", CubeListBuilder.create().texOffs(82, 38).addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR5 = right_leg.addOrReplaceChild("LegpanelR5", CubeListBuilder.create().texOffs(76, 42).addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR4 = right_leg.addOrReplaceChild("LegpanelR4", CubeListBuilder.create().texOffs(76, 38).addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 64);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		switch (this.renderSlot) {
			case HEAD -> {
				renderPart(this.head, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.hat, poseStack, buffer, packedLight, packedOverlay, packedColor);
			}
			case CHEST -> {
				renderPart(this.body, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.rightArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.leftArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
				if (this.body.visible) {
					poseStack.pushPose();
					this.body.translateAndRotate(poseStack);
					renderPart(this.beltR, poseStack, buffer, packedLight, packedOverlay, packedColor);
					renderPart(this.beltL, poseStack, buffer, packedLight, packedOverlay, packedColor);
					poseStack.popPose();
				}
			}
			case LEGS, FEET -> {
				renderPart(this.rightLeg, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.leftLeg, poseStack, buffer, packedLight, packedOverlay, packedColor);
			}
			default -> {
			}
		}
	}

	private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		if (part.visible) {
			part.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		}
	}
}
