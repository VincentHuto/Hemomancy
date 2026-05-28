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

public class ChalybeateFortressArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation CHALYBEATE_FORTRESS_BOOTS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("chalybeate_fortress_boots"), "main");
	public static final Lazy<ChalybeateFortressArmorModel<LivingEntity>> boots = Lazy.of(() -> new ChalybeateFortressArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(CHALYBEATE_FORTRESS_BOOTS_LAYER), EquipmentSlot.FEET));

	private final EquipmentSlot renderSlot;


	public ChalybeateFortressArmorModel(ModelPart root, EquipmentSlot renderSlot) {
		super(root, RenderType::entityTranslucent);
		this.renderSlot = renderSlot;
	}

public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mask_0 = head.addOrReplaceChild("Mask_0", CubeListBuilder.create().texOffs(52, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mask_1 = head.addOrReplaceChild("Mask_1", CubeListBuilder.create().texOffs(76, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mask_2 = head.addOrReplaceChild("Mask_2", CubeListBuilder.create().texOffs(100, 2).addBox(-4.5F, -5.0F, -4.6F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Goggles = head.addOrReplaceChild("Goggles", CubeListBuilder.create().texOffs(100, 18).addBox(-4.5F, -5.0F, -4.25F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition OrnamentL = head.addOrReplaceChild("OrnamentL", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(-3.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition OrnamentL2 = head.addOrReplaceChild("OrnamentL2", CubeListBuilder.create().texOffs(78, 8).mirror().addBox(-4.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition OrnamentR = head.addOrReplaceChild("OrnamentR", CubeListBuilder.create().texOffs(78, 8).addBox(1.5F, -9.0F, -6.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition OrnamentR2 = head.addOrReplaceChild("OrnamentR2", CubeListBuilder.create().texOffs(78, 8).addBox(3.5F, -10.0F, -6.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition Helmet = head.addOrReplaceChild("Helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 4.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition HelmetR = head.addOrReplaceChild("HelmetR", CubeListBuilder.create().texOffs(21, 13).addBox(5.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition HelmetL = head.addOrReplaceChild("HelmetL", CubeListBuilder.create().texOffs(21, 13).mirror().addBox(-6.5F, -3.0F, -4.5F, 1.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition HelmetB = head.addOrReplaceChild("HelmetB", CubeListBuilder.create().texOffs(41, 21).addBox(-4.5F, -3.0F, 5.5F, 9.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

		PartDefinition capsthingy = head.addOrReplaceChild("capsthingy", CubeListBuilder.create().texOffs(21, 0).addBox(-4.5F, -6.0F, -6.5F, 9.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition flapR = head.addOrReplaceChild("flapR", CubeListBuilder.create().texOffs(59, 10).addBox(7.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.5236F, -0.5236F));

		PartDefinition flapL = head.addOrReplaceChild("flapL", CubeListBuilder.create().texOffs(59, 10).mirror().addBox(-10.0F, -2.0F, -1.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.5236F, 0.5236F));

		PartDefinition Gemornament = head.addOrReplaceChild("Gemornament", CubeListBuilder.create().texOffs(68, 11).addBox(-1.5F, -9.0F, -7.0F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition Gem = head.addOrReplaceChild("Gem", CubeListBuilder.create().texOffs(72, 8).addBox(-1.0F, -8.5F, -7.5F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1396F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltR = body.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = body.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -4.0F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Scroll = body.addOrReplaceChild("Scroll", CubeListBuilder.create().texOffs(34, 27).addBox(-6.0F, 9.5F, 4.0F, 8.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.192F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Book = body.addOrReplaceChild("Book", CubeListBuilder.create().texOffs(100, 8).addBox(-6.0F, -0.3F, 4.0F, 5.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7679F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletR = right_arm.addOrReplaceChild("GauntletR", CubeListBuilder.create().texOffs(100, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR1 = right_arm.addOrReplaceChild("GauntletstrapR1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR2 = right_arm.addOrReplaceChild("GauntletstrapR2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderplateRtop = right_arm.addOrReplaceChild("ShoulderplateRtop", CubeListBuilder.create().texOffs(110, 37).addBox(3.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR1 = right_arm.addOrReplaceChild("ShoulderplateR1", CubeListBuilder.create().texOffs(110, 45).addBox(3.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR2 = right_arm.addOrReplaceChild("ShoulderplateR2", CubeListBuilder.create().texOffs(94, 45).addBox(2.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderplateR3 = right_arm.addOrReplaceChild("ShoulderplateR3", CubeListBuilder.create().texOffs(94, 45).addBox(1.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletL = left_arm.addOrReplaceChild("GauntletL", CubeListBuilder.create().texOffs(114, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Gauntletstrapl1 = left_arm.addOrReplaceChild("Gauntletstrapl1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL2 = left_arm.addOrReplaceChild("GauntletstrapL2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderplateLtop = left_arm.addOrReplaceChild("ShoulderplateLtop", CubeListBuilder.create().texOffs(110, 37).mirror().addBox(-5.5F, -2.5F, -3.5F, 2.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL1 = left_arm.addOrReplaceChild("ShoulderplateL1", CubeListBuilder.create().texOffs(110, 45).mirror().addBox(-4.5F, -1.5F, -3.5F, 1.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL2 = left_arm.addOrReplaceChild("ShoulderplateL2", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(-3.5F, 1.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderplateL3 = left_arm.addOrReplaceChild("ShoulderplateL3", CubeListBuilder.create().texOffs(94, 45).mirror().addBox(-2.5F, 3.5F, -3.5F, 1.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition LegpanelR1 = right_leg.addOrReplaceChild("LegpanelR1", CubeListBuilder.create().texOffs(0, 51).addBox(-2.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR2 = right_leg.addOrReplaceChild("LegpanelR2", CubeListBuilder.create().texOffs(8, 51).addBox(-2.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR3 = right_leg.addOrReplaceChild("LegpanelR3", CubeListBuilder.create().texOffs(0, 56).addBox(-2.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR4 = right_leg.addOrReplaceChild("LegpanelR4", CubeListBuilder.create().texOffs(0, 43).addBox(1.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR5 = right_leg.addOrReplaceChild("LegpanelR5", CubeListBuilder.create().texOffs(0, 47).addBox(1.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelR6 = right_leg.addOrReplaceChild("LegpanelR6", CubeListBuilder.create().texOffs(6, 43).addBox(1.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition SidepanelR1 = right_leg.addOrReplaceChild("SidepanelR1", CubeListBuilder.create().texOffs(0, 22).addBox(1.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition SidepanelR2 = right_leg.addOrReplaceChild("SidepanelR2", CubeListBuilder.create().texOffs(0, 31).addBox(0.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition SidepanelR3 = right_leg.addOrReplaceChild("SidepanelR3", CubeListBuilder.create().texOffs(12, 31).addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition BackpanelR1 = right_leg.addOrReplaceChild("BackpanelR1", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition BackpanelR2 = right_leg.addOrReplaceChild("BackpanelR2", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition BackpanelR3 = right_leg.addOrReplaceChild("BackpanelR3", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition BackpanelL3 = left_leg.addOrReplaceChild("BackpanelL3", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 4.5F, 0.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL1 = left_leg.addOrReplaceChild("LegpanelL1", CubeListBuilder.create().texOffs(0, 51).mirror().addBox(-1.0F, 0.5F, -3.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL2 = left_leg.addOrReplaceChild("LegpanelL2", CubeListBuilder.create().texOffs(8, 51).mirror().addBox(-1.0F, 3.5F, -2.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL3 = left_leg.addOrReplaceChild("LegpanelL3", CubeListBuilder.create().texOffs(0, 56).mirror().addBox(-1.0F, 6.5F, -1.5F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL4 = left_leg.addOrReplaceChild("LegpanelL4", CubeListBuilder.create().texOffs(0, 43).mirror().addBox(-3.0F, 0.5F, -3.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL5 = left_leg.addOrReplaceChild("LegpanelL5", CubeListBuilder.create().texOffs(0, 47).mirror().addBox(-3.0F, 2.5F, -2.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition LegpanelL6 = left_leg.addOrReplaceChild("LegpanelL6", CubeListBuilder.create().texOffs(6, 43).mirror().addBox(-3.0F, 4.5F, -1.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition SidepanelL1 = left_leg.addOrReplaceChild("SidepanelL1", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2.5F, 0.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition SidepanelL2 = left_leg.addOrReplaceChild("SidepanelL2", CubeListBuilder.create().texOffs(0, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition SidepanelL3 = left_leg.addOrReplaceChild("SidepanelL3", CubeListBuilder.create().texOffs(12, 31).mirror().addBox(-0.5F, 5.5F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition BackpanelL1 = left_leg.addOrReplaceChild("BackpanelL1", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 0.5F, 2.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition BackpanelL2 = left_leg.addOrReplaceChild("BackpanelL2", CubeListBuilder.create().texOffs(0, 18).mirror().addBox(-3.0F, 2.5F, 1.5F, 5.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

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
