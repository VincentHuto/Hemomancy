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
import net.minecraftforge.common.util.Lazy;

public class UnstainedArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation UNSTAINED_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_helmet"), "main");
	public static final ModelLayerLocation UNSTAINED_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_chest"), "main");
	public static final ModelLayerLocation UNSTAINED_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_leggings"), "main");
	public static final ModelLayerLocation UNSTAINED_FEET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("unstained_boots"), "main");

	public static final Lazy<UnstainedArmorModel<LivingEntity>> helmet = Lazy.of(() -> new UnstainedArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_HELMET_LAYER)));
	public static final Lazy<UnstainedArmorModel<LivingEntity>> chest = Lazy.of(() -> new UnstainedArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_CHEST_LAYER)));
	public static final Lazy<UnstainedArmorModel<LivingEntity>> legs = Lazy.of(
			() -> new UnstainedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_LEGS_LAYER)));
	public static final Lazy<UnstainedArmorModel<LivingEntity>> boots = Lazy.of(
			() -> new UnstainedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(UNSTAINED_FEET_LAYER)));

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot.equals(EquipmentSlot.CHEST)) {

			PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(29, 105)
					.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition scale = body.addOrReplaceChild("scale",
					CubeListBuilder.create().texOffs(59, 28)
							.addBox(-4.0F, -24.0F, -3.75F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(60, 7)
							.addBox(-3.0F, -24.0F, -4.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(39, 29)
							.addBox(-4.0F, -24.0F, -2.5F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)).texOffs(0, 77)
							.addBox(-3.0F, -21.0F, -3.25F, 6.0F, 7.0F, 6.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 24.0F, 0.0F));

			PartDefinition tank3 = body.addOrReplaceChild("tank3",
					CubeListBuilder.create().texOffs(18, 57)
							.addBox(8.0F, -24.5F, -3.75F, 3.0F, 13.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(62, 66)
							.addBox(7.5F, -22.5F, -1.75F, 4.0F, 9.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(38, 62)
							.addBox(8.0F, -22.5F, -1.75F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(0, 0)
							.addBox(8.0F, -16.5F, -1.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(29, 41)
							.addBox(8.5F, -23.5F, -1.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(48, 56)
							.addBox(7.0F, -24.0F, -4.0F, 5.0F, 11.0F, 2.0F, new CubeDeformation(0.25F)),
					PartPose.offset(-6.25F, 22.0F, 7.0F));

			PartDefinition tank2 = body.addOrReplaceChild("tank2",
					CubeListBuilder.create().texOffs(32, 12)
							.addBox(2.75F, -20.0F, -4.25F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(18, 57)
							.addBox(1.0F, -24.5F, -3.75F, 3.0F, 13.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(62, 66)
							.addBox(0.5F, -22.5F, -1.75F, 4.0F, 9.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(38, 62)
							.addBox(1.0F, -22.5F, -1.75F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(0, 0)
							.addBox(1.0F, -16.5F, -1.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(29, 41)
							.addBox(1.5F, -23.5F, -1.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(48, 56)
							.addBox(0.0F, -24.0F, -4.0F, 5.0F, 11.0F, 2.0F, new CubeDeformation(0.25F)),
					PartPose.offset(-5.75F, 22.0F, 7.0F));

			PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(),
					PartPose.offset(5.0F, 2.0F, 0.0F));

			PartDefinition rArm2 = left_arm.addOrReplaceChild("rArm2",
					CubeListBuilder.create().texOffs(47, 7)
							.addBox(4.1F, -24.25F, -2.5F, 4.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(67, 42)
							.addBox(4.6F, -25.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-5.0F, 22.0F, 0.0F));

			PartDefinition rArmBackShing2 = rArm2.addOrReplaceChild("rArmBackShing2", CubeListBuilder.create()
					.texOffs(0, 72).addBox(-2.0F, -1.0F, -1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(5.85F, -22.5F, 3.75F));

			PartDefinition rArmFrontShing2 = rArm2.addOrReplaceChild("rArmFrontShing2", CubeListBuilder.create()
					.texOffs(70, 58).addBox(-2.0F, -1.0F, 0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(5.85F, -22.5F, -3.75F));

			PartDefinition shingle2 = rArm2.addOrReplaceChild("shingle2",
					CubeListBuilder.create().texOffs(45, 47)
							.addBox(-4.5F, -3.5F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(69, 48)
							.addBox(-3.5F, -4.5F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(62, 58)
							.addBox(-1.25F, -0.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(8.6F, -21.75F, 0.0F));

			PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(),
					PartPose.offset(-5.0F, 2.0F, 0.0F));

			PartDefinition right_arm3 = right_arm.addOrReplaceChild("right_arm3",
					CubeListBuilder.create().texOffs(16, 41)
							.addBox(-8.1F, -24.25F, -2.5F, 4.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(66, 33)
							.addBox(-7.6F, -25.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(5.0F, 22.0F, 0.0F));

			PartDefinition rArmBackShing = right_arm3.addOrReplaceChild("rArmBackShing", CubeListBuilder.create()
					.texOffs(24, 0).addBox(-2.0F, -1.0F, -1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-5.85F, -22.5F, 3.75F));

			PartDefinition rArmFrontShing = right_arm3.addOrReplaceChild("rArmFrontShing",
					CubeListBuilder.create().texOffs(70, 58).mirror()
							.addBox(-2.0F, -1.0F, 0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offset(-5.85F, -22.5F, -3.75F));

			PartDefinition shingle = right_arm3.addOrReplaceChild("shingle",
					CubeListBuilder.create().texOffs(28, 53)
							.addBox(-0.5F, -3.5F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(68, 23)
							.addBox(0.5F, -4.5F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(61, 50)
							.addBox(0.25F, -0.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-8.6F, -21.75F, 0.0F));

		}
		if (slot.equals(EquipmentSlot.LEGS)) {
			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(99, 116)
							.addBox(-5.9F, 0.0F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.6F)).texOffs(25, 167)
							.addBox(-5.9F, -1.0F, -2.0F, 8.0F, 0.0F, 4.0F, new CubeDeformation(0.8F)).texOffs(0, 32)
							.addBox(-3.0F, 0.0F, -2.0F, 5.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(51, 37)
							.addBox(-2.0F, 0.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.2F)).texOffs(19, 29)
							.addBox(-2.0F, 3.8F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(35, 37)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 50)
							.addBox(-3.0F, 0.0F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.2F)).texOffs(32, 0)
							.addBox(-3.0F, 3.8F, -2.5F, 5.0F, 7.0F, 5.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

		}
		if (slot.equals(EquipmentSlot.FEET)) {
			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(58, 0)
							.addBox(-1.9F, 10.1F, -2.75F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(58, 0)
							.addBox(-1.9F, 10.1F, -2.75F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(101, 102)
							.addBox(-1.9F, 8.85F, 2.25F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(48, 21)
							.addBox(-3.1F, 10.1F, -2.75F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(91, 123)
							.addBox(-3.1F, 8.85F, 2.25F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

		}
		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	@SuppressWarnings("unused")
	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot.equals(EquipmentSlot.HEAD)) {
			PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
					.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));

		}

		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	public UnstainedArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		body.render(poseStack, buffer, packedLight, packedOverlay);
		leftArm.render(poseStack, buffer, packedLight, packedOverlay);
		rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rightArm.render(poseStack, buffer, packedLight, packedOverlay);

	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}
}
