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

public class CovenantLeaderArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation COVENANT_LEADER_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("covenant_leader_chest"), "main");
	public static final Lazy<CovenantLeaderArmorModel<LivingEntity>> chest = Lazy.of(() -> new CovenantLeaderArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(COVENANT_LEADER_CHEST_LAYER), EquipmentSlot.CHEST));

	private final EquipmentSlot renderSlot;


	public CovenantLeaderArmorModel(ModelPart root, EquipmentSlot renderSlot) {
		super(root, RenderType::entityTranslucent);
		this.renderSlot = renderSlot;
	}

public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Helmet = head.addOrReplaceChild("Helmet", CubeListBuilder.create().texOffs(41, 8).addBox(-4.5F, -9.0F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition CollarF = body.addOrReplaceChild("CollarF", CubeListBuilder.create().texOffs(17, 31).addBox(-4.5F, -1.5F, -3.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition CollarB = body.addOrReplaceChild("CollarB", CubeListBuilder.create().texOffs(17, 26).addBox(-4.5F, -1.5F, 7.0F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition CollarR = body.addOrReplaceChild("CollarR", CubeListBuilder.create().texOffs(17, 11).addBox(4.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition CollarL = body.addOrReplaceChild("CollarL", CubeListBuilder.create().texOffs(17, 11).addBox(-5.5F, -1.5F, -3.0F, 1.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -2.5F, 0.2269F, 0.0F, 0.0F));

		PartDefinition BeltR = body.addOrReplaceChild("BeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Mbelt = body.addOrReplaceChild("Mbelt", CubeListBuilder.create().texOffs(56, 55).addBox(-4.0F, 8.0F, -3.0F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltL = body.addOrReplaceChild("MbeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition MbeltR = body.addOrReplaceChild("MbeltR", CubeListBuilder.create().texOffs(76, 44).addBox(4.0F, 8.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition BeltL = body.addOrReplaceChild("BeltL", CubeListBuilder.create().texOffs(76, 44).addBox(-5.0F, 4.0F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition CloakTL = body.addOrReplaceChild("CloakTL", CubeListBuilder.create().texOffs(0, 43).addBox(-4.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak3 = body.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 17.0F, -3.7F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.4466F, 0.0F, 0.0F));

		PartDefinition CloakTR = body.addOrReplaceChild("CloakTR", CubeListBuilder.create().texOffs(0, 43).addBox(2.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak1 = body.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(0, 47).addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak2 = body.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(0, 59).addBox(-4.5F, 14.0F, -1.3F, 9.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 3.0F, 0.3069F, 0.0F, 0.0F));

		PartDefinition Chestplate = body.addOrReplaceChild("Chestplate", CubeListBuilder.create().texOffs(56, 45).addBox(-4.0F, 1.0F, -3.8F, 8.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ChestOrnament = body.addOrReplaceChild("ChestOrnament", CubeListBuilder.create().texOffs(76, 53).addBox(-2.5F, 3.0F, -4.8F, 5.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ChestClothL = body.addOrReplaceChild("ChestClothL", CubeListBuilder.create().texOffs(20, 47).mirror().addBox(-4.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663F, 0.0F, 0.0F));

		PartDefinition ChestClothR = body.addOrReplaceChild("ChestClothR", CubeListBuilder.create().texOffs(20, 47).addBox(1.5F, 1.2F, -4.5F, 3.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0663F, 0.0F, 0.0F));

		PartDefinition Backplate = body.addOrReplaceChild("Backplate", CubeListBuilder.create().texOffs(36, 45).addBox(-4.0F, 1.0F, 2.0F, 8.0F, 11.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition LegClothR = body.addOrReplaceChild("LegClothR", CubeListBuilder.create().texOffs(20, 55).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.5F, 10.4F, -3.9F, -0.0349F, 0.0F, 0.0F));

		PartDefinition LegClothL = body.addOrReplaceChild("LegClothL", CubeListBuilder.create().texOffs(20, 55).mirror().addBox(-3.0F, 0.0F, 0.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-1.5F, 10.4F, -3.9F, -0.0349F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition GauntletR = right_arm.addOrReplaceChild("GauntletR", CubeListBuilder.create().texOffs(100, 26).addBox(1.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR1 = right_arm.addOrReplaceChild("GauntletstrapR1", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapR2 = right_arm.addOrReplaceChild("GauntletstrapR2", CubeListBuilder.create().texOffs(84, 31).addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletR2 = right_arm.addOrReplaceChild("GauntletR2", CubeListBuilder.create().texOffs(102, 37).addBox(4.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1676F));

		PartDefinition ShoulderR = right_arm.addOrReplaceChild("ShoulderR", CubeListBuilder.create().texOffs(56, 35).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderR1 = right_arm.addOrReplaceChild("ShoulderR1", CubeListBuilder.create().texOffs(0, 0).addBox(1.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR2 = right_arm.addOrReplaceChild("ShoulderR2", CubeListBuilder.create().texOffs(0, 19).addBox(2.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR5 = right_arm.addOrReplaceChild("ShoulderR5", CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR3 = right_arm.addOrReplaceChild("ShoulderR3", CubeListBuilder.create().texOffs(0, 11).addBox(1.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition ShoulderR4 = right_arm.addOrReplaceChild("ShoulderR4", CubeListBuilder.create().texOffs(18, 4).addBox(1.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition GauntletL = left_arm.addOrReplaceChild("GauntletL", CubeListBuilder.create().texOffs(114, 26).addBox(-3.5F, 3.5F, -2.5F, 2.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL1 = left_arm.addOrReplaceChild("GauntletstrapL1", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 3.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletstrapL2 = left_arm.addOrReplaceChild("GauntletstrapL2", CubeListBuilder.create().texOffs(84, 31).mirror().addBox(-1.5F, 6.5F, -2.5F, 3.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition GauntletL2 = left_arm.addOrReplaceChild("GauntletL2", CubeListBuilder.create().texOffs(102, 37).addBox(-5.0F, 3.5F, -2.0F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1676F));

		PartDefinition ShoulderL = left_arm.addOrReplaceChild("ShoulderL", CubeListBuilder.create().texOffs(56, 35).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ShoulderL1 = left_arm.addOrReplaceChild("ShoulderL1", CubeListBuilder.create().texOffs(0, 0).addBox(-4.3F, -1.5F, -3.0F, 3.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL2 = left_arm.addOrReplaceChild("ShoulderL2", CubeListBuilder.create().texOffs(0, 19).mirror().addBox(-3.3F, 3.5F, -2.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL3 = left_arm.addOrReplaceChild("ShoulderL3", CubeListBuilder.create().texOffs(0, 11).addBox(-2.3F, 3.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL5 = left_arm.addOrReplaceChild("ShoulderL5", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, 3.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition ShoulderL4 = left_arm.addOrReplaceChild("ShoulderL4", CubeListBuilder.create().texOffs(18, 4).addBox(-2.3F, -1.5F, -4.0F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition BackpanelR1 = right_leg.addOrReplaceChild("BackpanelR1", CubeListBuilder.create().texOffs(0, 25).addBox(-2.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

		PartDefinition BackpanelR2 = right_leg.addOrReplaceChild("BackpanelR2", CubeListBuilder.create().texOffs(96, 14).addBox(-2.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition BackpanelR3 = right_leg.addOrReplaceChild("BackpanelR3", CubeListBuilder.create().texOffs(116, 13).addBox(2.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1396F));

		PartDefinition BackpanelR4 = right_leg.addOrReplaceChild("BackpanelR4", CubeListBuilder.create().texOffs(0, 25).mirror().addBox(-2.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition BackpanelL1 = left_leg.addOrReplaceChild("BackpanelL1", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, 2.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0698F, 0.0F, 0.0F));

		PartDefinition BackpanelL4 = left_leg.addOrReplaceChild("BackpanelL4", CubeListBuilder.create().texOffs(0, 25).addBox(-3.0F, -0.5F, -3.5F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0349F, 0.0F, 0.0F));

		PartDefinition BackpanelL2 = left_leg.addOrReplaceChild("BackpanelL2", CubeListBuilder.create().texOffs(96, 14).addBox(-3.0F, -0.5F, -2.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

		PartDefinition BackpanelL3 = left_leg.addOrReplaceChild("BackpanelL3", CubeListBuilder.create().texOffs(116, 13).addBox(-3.0F, 2.5F, -2.5F, 1.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1396F));

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
