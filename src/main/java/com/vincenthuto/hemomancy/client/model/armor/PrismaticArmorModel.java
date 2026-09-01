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

public class PrismaticArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation PRISMATIC_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("prismatic_helmet"), "main");
	public static final ModelLayerLocation PRISMATIC_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("prismatic_chest"), "main");
	public static final ModelLayerLocation PRISMATIC_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("prismatic_leggings"), "main");
	public static final ModelLayerLocation PRISMATIC_FEET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("prismatic_boots"), "main");

	public static final Lazy<PrismaticArmorModel<LivingEntity>> helmet = Lazy.of(() -> new PrismaticArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(PRISMATIC_HELMET_LAYER)));
	public static final Lazy<PrismaticArmorModel<LivingEntity>> chest = Lazy.of(() -> new PrismaticArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(PRISMATIC_CHEST_LAYER)));
	public static final Lazy<PrismaticArmorModel<LivingEntity>> legs = Lazy.of(() -> new PrismaticArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(PRISMATIC_LEGS_LAYER)));
	public static final Lazy<PrismaticArmorModel<LivingEntity>> boots = Lazy.of(() -> new PrismaticArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(PRISMATIC_FEET_LAYER)));

	public PrismaticArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);
	}

	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition root = mesh.getRoot();
		if (slot == EquipmentSlot.CHEST) {
			root.addOrReplaceChild("body",
					CubeListBuilder.create().texOffs(16, 16)
							.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.18F))
							.texOffs(16, 52)
							.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.42F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("right_arm",
					CubeListBuilder.create().texOffs(64, 32)
							.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.16F))
							.texOffs(72, 16)
							.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F))
							.texOffs(59, 48)
							.addBox(-4.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
							.texOffs(80, 32)
							.addBox(-4.05F, -2.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.03F))
							.texOffs(80, 32)
							.addBox(-2.55F, -3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.03F))
							.texOffs(82, 36)
							.addBox(-2.0F, -2.0F, 2.05F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.03F))
							.texOffs(82, 36)
							.addBox(-2.0F, -2.0F, -2.95F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.03F)),
					PartPose.offset(-5.0F, 2.0F, 0.0F));
			root.addOrReplaceChild("left_arm",
					CubeListBuilder.create().texOffs(40, 16)
							.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.16F))
							.texOffs(56, 16)
							.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.5F))
							.texOffs(59, 48)
							.addBox(-2.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
							.texOffs(80, 32)
							.addBox(3.05F, -2.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.03F))
							.texOffs(80, 32)
							.addBox(-0.45F, -3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.03F))
							.texOffs(82, 36)
							.addBox(0.0F, -2.0F, 2.05F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.03F))
							.texOffs(82, 36)
							.addBox(0.0F, -2.0F, -2.95F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.03F)),
					PartPose.offset(5.0F, 2.0F, 0.0F));
		} else if (slot == EquipmentSlot.LEGS) {
			root.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(16, 32)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.12F))
							.texOffs(48, 32)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.48F))
							.texOffs(59, 48)
							.addBox(-3.1F, 6.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));
			root.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(16, 32)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.12F))
							.texOffs(32, 32)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.48F))
							.texOffs(59, 48)
							.addBox(-2.9F, 6.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));
		} else if (slot == EquipmentSlot.FEET) {
			PartDefinition rightLeg = root.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(16, 42)
							.addBox(-2.0F, 6.3F, -2.0F, 4.0F, 5.85F, 4.0F, new CubeDeformation(0.0F))
							.texOffs(48, 42)
							.addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.15F, 4.0F, new CubeDeformation(0.32F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));
			rightLeg.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(59, 48)
							.addBox(-3.1F, 1.9F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, 4.15F, 0.0F));
			PartDefinition leftLeg = root.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(0, 42)
							.addBox(-2.0F, 6.3F, -2.0F, 4.0F, 5.85F, 4.0F, new CubeDeformation(0.0F))
							.texOffs(32, 42)
							.addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.15F, 4.0F, new CubeDeformation(0.32F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));
			leftLeg.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(59, 48)
							.addBox(-2.9F, 1.9F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, 4.15F, 0.0F));
		}
		return LayerDefinition.create(mesh, 128, 128);
	}

	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition root = mesh.getRoot();
		if (slot == EquipmentSlot.HEAD) {
			PartDefinition head = root.addOrReplaceChild("head",
					CubeListBuilder.create().texOffs(0, 0)
							.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.18F))
							.texOffs(32, 0)
							.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.52F))
							.texOffs(32, 60)
							.addBox(-5.0F, 0.55F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)),
					PartPose.ZERO);
			PartDefinition headTentacles = head.addOrReplaceChild("HeadTentacles",
					CubeListBuilder.create().texOffs(36, 75)
							.addBox(1.7F, -2.3811F, -2.7983F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.08F))
							.texOffs(36, 75)
							.addBox(-6.3F, -2.3811F, -2.7983F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.08F)),
					PartPose.offsetAndRotation(0.3F, -6.5128F, 0.2125F, 0.3927F, 0.0F, 0.0F));
			PartDefinition headTentacles2 = headTentacles.addOrReplaceChild("HeadTentacles2",
					CubeListBuilder.create().texOffs(36, 75)
							.addBox(2.2333F, -1.4087F, -1.1171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.32F))
							.texOffs(36, 75)
							.addBox(-5.7667F, -1.4087F, -1.1171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.32F)),
					PartPose.offsetAndRotation(-0.5333F, -0.9766F, 4.1993F, -0.6109F, 0.0F, 0.0F));
			PartDefinition headTentacles3 = headTentacles2.addOrReplaceChild("HeadTentacles3",
					CubeListBuilder.create().texOffs(36, 75)
							.addBox(2.2333F, -2.0335F, -1.3046F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.92F))
							.texOffs(36, 75)
							.addBox(-5.7667F, -2.0335F, -1.3046F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.92F)),
					PartPose.offsetAndRotation(0.0F, 0.0266F, 4.6844F, -0.7418F, 0.0F, 0.0F));
			headTentacles3.addOrReplaceChild("jingles",
					CubeListBuilder.create().texOffs(64, 59)
							.addBox(2.75F, 0.1399F, -4.8554F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.72F))
							.texOffs(64, 59)
							.addBox(-5.25F, 0.1399F, -4.8554F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.72F)),
					PartPose.offsetAndRotation(-0.4667F, 2.8219F, 8.0353F, -1.5708F, 0.0F, 0.0F));
		}
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		body.render(poseStack, buffer, packedLight, packedOverlay);
		leftArm.render(poseStack, buffer, packedLight, packedOverlay);
		rightArm.render(poseStack, buffer, packedLight, packedOverlay);
		rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
	}
}
