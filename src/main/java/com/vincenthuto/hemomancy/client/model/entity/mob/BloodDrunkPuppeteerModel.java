package com.vincenthuto.hemomancy.client.model.entity.mob;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

public class BloodDrunkPuppeteerModel<T extends LivingEntity> extends HumanoidModel<T> {


	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelblooddrunkpuppeteer"), "main");

	@SuppressWarnings("unused")
	public static LayerDefinition createbodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-0.0F, -0.0F, -0.0F,
				0.0F, 0.0F, 0.0F, new CubeDeformation(0)), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(32, 60)
						.addBox(-5.0F, 0.55F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(32, 0)
						.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.3F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition HeadTentacles = head.addOrReplaceChild("HeadTentacles",
				CubeListBuilder.create().texOffs(36, 75)
						.addBox(3.0F, -2.0F, -5.5F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(36, 75)
						.addBox(-6.0F, -2.0F, -5.5F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -8.0F, 2.5F, 0.4363F, 0.0F, 0.0F));

		PartDefinition HeadTentacles2 = HeadTentacles.addOrReplaceChild("HeadTentacles2",
				CubeListBuilder.create().texOffs(36, 75)
						.addBox(2.5F, -2.5F, -4.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)).texOffs(36, 75)
						.addBox(-6.5F, -2.5F, -4.0F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(0.5F, 1.952F, 3.2332F, -0.6109F, 0.0F, 0.0F));

		PartDefinition HeadTentacles3 = HeadTentacles.addOrReplaceChild("HeadTentacles3",
				CubeListBuilder.create().texOffs(36, 75)
						.addBox(2.4F, -0.6352F, -3.6215F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-1.0F)).texOffs(36, 75)
						.addBox(-6.6F, -0.6352F, -3.6215F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-1.0F)),
				PartPose.offsetAndRotation(0.5F, 4.0726F, 7.8146F, -1.2217F, 0.0F, 0.0F));

		PartDefinition jingles = HeadTentacles.addOrReplaceChild("jingles",
				CubeListBuilder.create().texOffs(64, 59)
						.addBox(2.35F, 0.2054F, -5.7692F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)).texOffs(64, 59)
						.addBox(-6.65F, 0.2054F, -5.7692F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)),
				PartPose.offsetAndRotation(0.5F, 10.6726F, 11.1646F, -1.2217F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(16, 16)
						.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(16, 52)
						.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm",
				CubeListBuilder.create().texOffs(64, 32)
						.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(72, 16)
						.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(59, 48)
						.addBox(-4.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(80, 32)
						.addBox(-4.05F, -2.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(80, 32)
						.addBox(-2.55F, -3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(82, 36)
						.addBox(-2.0F, -2.0F, 2.05F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(82, 36)
						.addBox(-2.0F, -2.0F, -2.95F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm",
				CubeListBuilder.create().texOffs(40, 16)
						.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(82, 36)
						.addBox(0.0F, -2.0F, 2.05F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(82, 36)
						.addBox(0.0F, -2.0F, -2.95F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(80, 32)
						.addBox(-0.45F, -3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(56, 16)
						.addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(80, 32)
						.addBox(3.05F, -2.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(59, 48)
						.addBox(-2.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
				CubeListBuilder.create().texOffs(16, 32)
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(32, 32)
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(59, 48)
						.addBox(-2.9F, 6.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition left_leg2 = partdefinition.addOrReplaceChild("left_leg2",
				CubeListBuilder.create().texOffs(0, 42)
						.addBox(-2.0F, 6.3F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.1F)).texOffs(32, 42)
						.addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
				CubeListBuilder.create().texOffs(16, 32)
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(48, 32)
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F)).texOffs(59, 48)
						.addBox(-3.1F, 6.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition right_leg2 = partdefinition.addOrReplaceChild("right_leg2",
				CubeListBuilder.create().texOffs(16, 42)
						.addBox(-2.0F, 6.3F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.1F)).texOffs(48, 42)
						.addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}
	public final Map<String, ModelPart> parts;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart left_leg;
	private final ModelPart left_leg2;
	private final ModelPart right_leg;

	private final ModelPart right_leg2;

	public BloodDrunkPuppeteerModel(ModelPart root) {
		super(root);
		this.head = root.getChild("head");
		this.body = root.getChild("body");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.left_leg = root.getChild("left_leg");
		this.left_leg2 = root.getChild("left_leg2");
		this.right_leg = root.getChild("right_leg");
		this.right_leg2 = root.getChild("right_leg2");

		parts = new ImmutableMap.Builder<String, ModelPart>().put("body", body).put("head", head).build();
	}

	@Override
	protected ModelPart getArm(HumanoidArm pSide) {
		return pSide == HumanoidArm.LEFT ? this.left_arm : this.right_arm;
	}

	@Override
	public ModelPart getHead() {
		return this.head;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		body.render(poseStack, buffer, packedLight, packedOverlay);
		right_arm.render(poseStack, buffer, packedLight, packedOverlay);
		left_arm.render(poseStack, buffer, packedLight, packedOverlay);
		left_leg.render(poseStack, buffer, packedLight, packedOverlay);
		left_leg2.render(poseStack, buffer, packedLight, packedOverlay);
		right_leg.render(poseStack, buffer, packedLight, packedOverlay);
		right_leg2.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw,
			float pHeadPitch) {
		super.setupAnim(pEntity, pLimbSwing, pLimbSwingAmount, pAgeInTicks, pNetHeadYaw, pHeadPitch);
		this.right_leg2.copyFrom(right_leg);
		this.left_leg2.copyFrom(left_leg);

//		// loads the original position of the entity
//		// ticks the animation. this can be setup to take whatever parameter is desired,
//		// ageInTicks is just an easy one for looping animations
		//maybe use some form of attack timer for other attacks
//		DANCE.tick(parts, pAgeInTicks);

	}

	@Override
	public void translateToHand(HumanoidArm pSide, PoseStack pPoseStack) {
		this.getArm(pSide).translateAndRotate(pPoseStack);
	}

}