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
			PartDefinition body = root.addOrReplaceChild("body",
					CubeListBuilder.create().texOffs(16, 16)
							.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.22F))
							.texOffs(0, 16)
							.addBox(-3.0F, 1.0F, -2.65F, 6.0F, 4.0F, 1.0F, new CubeDeformation(0.08F))
							.texOffs(0, 22)
							.addBox(-2.0F, 5.0F, -2.75F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.04F)),
					PartPose.ZERO);
			body.addOrReplaceChild("back_display_panel",
					CubeListBuilder.create().texOffs(24, 0)
							.addBox(-2.5F, 1.5F, 2.45F, 5.0F, 7.0F, 1.0F, new CubeDeformation(0.02F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));
			root.addOrReplaceChild("right_arm",
					CubeListBuilder.create().texOffs(40, 16)
							.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.18F))
							.texOffs(40, 0)
							.addBox(-4.25F, -2.2F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-5.0F, 2.0F, 0.0F));
			root.addOrReplaceChild("left_arm",
					CubeListBuilder.create().texOffs(40, 16).mirror()
							.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.18F))
							.mirror(false).texOffs(48, 0)
							.addBox(2.25F, -2.2F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
					PartPose.offset(5.0F, 2.0F, 0.0F));
		} else if (slot == EquipmentSlot.LEGS) {
			root.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(0, 16)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.16F))
							.texOffs(0, 0)
							.addBox(-2.35F, 2.0F, -2.45F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
							.texOffs(8, 0)
							.addBox(-1.0F, 7.0F, 1.8F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));
			root.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(0, 16).mirror()
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.16F))
							.mirror(false).texOffs(0, 0)
							.addBox(-0.65F, 2.0F, -2.45F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
							.texOffs(8, 0)
							.addBox(-2.0F, 7.0F, 1.8F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));
		} else if (slot == EquipmentSlot.FEET) {
			root.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(0, 16)
							.addBox(-2.15F, 9.0F, -2.25F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.08F))
							.texOffs(16, 0)
							.addBox(-2.75F, 10.0F, -3.3F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));
			root.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(0, 16).mirror()
							.addBox(-1.85F, 9.0F, -2.25F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.08F))
							.mirror(false).texOffs(16, 0)
							.addBox(1.75F, 10.0F, -3.3F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));
		}
		return LayerDefinition.create(mesh, 64, 32);
	}

	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition root = mesh.getRoot();
		if (slot == EquipmentSlot.HEAD) {
			PartDefinition head = root.addOrReplaceChild("head",
					CubeListBuilder.create().texOffs(0, 0)
							.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.55F))
							.texOffs(32, 0)
							.addBox(-2.5F, -9.0F, -4.6F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
							.texOffs(44, 0)
							.addBox(-1.5F, -6.25F, -5.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.ZERO);
			head.addOrReplaceChild("right_display_fin",
					CubeListBuilder.create().texOffs(52, 0)
							.addBox(-5.0F, -7.0F, -1.5F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.ZERO);
			head.addOrReplaceChild("left_display_fin",
					CubeListBuilder.create().texOffs(52, 0).mirror()
							.addBox(4.0F, -7.0F, -1.5F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
							.mirror(false),
					PartPose.ZERO);
		}
		return LayerDefinition.create(mesh, 64, 32);
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
