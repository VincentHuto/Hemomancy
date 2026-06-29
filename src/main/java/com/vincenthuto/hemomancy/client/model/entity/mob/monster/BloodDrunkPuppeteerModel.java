package com.vincenthuto.hemomancy.client.model.entity.mob.monster;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.BloodDrunkPuppeteerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.harbinger.HarbingerAlchemistEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.Map;

public class BloodDrunkPuppeteerModel<T extends BloodDrunkPuppeteerEntity>  extends EntityModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("modelblooddrunkpuppeteer"), "main");

	public final Map<String, ModelPart> parts;
	private final ModelPart head;
	private final ModelPart HeadTentacles;
	private final ModelPart HeadTentacles2;
	private final ModelPart HeadTentacles3;
	private final ModelPart jingles;
	private final ModelPart body;
	private final ModelPart right_arm;
	private final ModelPart left_arm;
	private final ModelPart left_leg;
	private final ModelPart left_leg2;
	private final ModelPart right_leg;
	private final ModelPart right_leg2;

	public BloodDrunkPuppeteerModel(ModelPart root) {
		this.head = root.getChild("head");
		this.HeadTentacles = this.head.getChild("HeadTentacles");
		this.HeadTentacles2 = this.HeadTentacles.getChild("HeadTentacles2");
		this.HeadTentacles3 = this.HeadTentacles2.getChild("HeadTentacles3");
		this.jingles = this.HeadTentacles3.getChild("jingles");
		this.body = root.getChild("body");
		this.right_arm = root.getChild("right_arm");
		this.left_arm = root.getChild("left_arm");
		this.left_leg = root.getChild("left_leg");
		this.left_leg2 = this.left_leg.getChild("left_leg2");
		this.right_leg = root.getChild("right_leg");
		this.right_leg2 = this.right_leg.getChild("right_leg2");
		parts = new ImmutableMap.Builder<String, ModelPart>().put("body", body).put("head", head).build();
	}


	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-0.0F, -0.0F, -0.0F,
				0.0F, 0.0F, 0.0F, new CubeDeformation(0)), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.075F, -4.5093F, -4.0531F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(32, 60).addBox(-5.075F, 4.0407F, -5.0531F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(32, 0).addBox(-4.075F, -4.5093F, -4.0531F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.3F)), PartPose.offset(0.075F, -3.4907F, 0.0531F));

		PartDefinition HeadTentacles = head.addOrReplaceChild("HeadTentacles", CubeListBuilder.create().texOffs(36, 75).addBox(1.7F, -2.3811F, -2.7983F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(36, 75).addBox(-6.3F, -2.3811F, -2.7983F, 4.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.225F, -3.0221F, 0.1594F, 0.3927F, 0.0F, 0.0F));

		PartDefinition HeadTentacles2 = HeadTentacles.addOrReplaceChild("HeadTentacles2", CubeListBuilder.create().texOffs(36, 75).addBox(2.2333F, -1.4087F, -1.1171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F))
				.texOffs(36, 75).addBox(-5.7667F, -1.4087F, -1.1171F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(-0.5333F, -0.9766F, 4.1993F, -0.6109F, 0.0F, 0.0F));

		PartDefinition HeadTentacles3 = HeadTentacles2.addOrReplaceChild("HeadTentacles3", CubeListBuilder.create().texOffs(36, 75).addBox(2.2333F, -2.0335F, -1.3046F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-1.0F))
				.texOffs(36, 75).addBox(-5.7667F, -2.0335F, -1.3046F, 4.0F, 4.0F, 7.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 0.0266F, 4.6844F, -0.7418F, 0.0F, 0.0F));

		PartDefinition jingles = HeadTentacles3.addOrReplaceChild("jingles", CubeListBuilder.create().texOffs(64, 59).addBox(2.75F, 0.1399F, -4.8554F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F))
				.texOffs(64, 59).addBox(-5.25F, 0.1399F, -4.8554F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-0.8F)), PartPose.offsetAndRotation(-0.4667F, 2.8219F, 8.0353F, -1.5708F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(16, 52).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(64, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(72, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F))
				.texOffs(59, 48).addBox(-4.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(80, 32).addBox(-4.05F, -2.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(80, 32).addBox(-2.55F, -3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(82, 36).addBox(-2.0F, -2.0F, 2.05F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(82, 36).addBox(-2.0F, -2.0F, -2.95F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));

		PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(82, 36).addBox(0.0F, -2.0F, 2.05F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(82, 36).addBox(0.0F, -2.0F, -2.95F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(80, 32).addBox(-0.45F, -3.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(56, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F))
				.texOffs(80, 32).addBox(3.05F, -2.0F, -1.0F, 1.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(59, 48).addBox(-2.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));

		PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(32, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F))
				.texOffs(59, 48).addBox(-2.9F, 6.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition left_leg2 = left_leg.addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(0, 42).addBox(-2.0F, 2.15F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.1F))
				.texOffs(32, 42).addBox(-2.0F, 1.85F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 4.15F, 0.0F));

		PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(48, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.35F))
				.texOffs(59, 48).addBox(-3.1F, 6.4F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition right_leg2 = right_leg.addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(16, 42).addBox(-2.0F, 2.15F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.1F))
				.texOffs(48, 42).addBox(-2.0F, 1.85F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 4.15F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}


	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		body.render(poseStack, buffer, packedLight, packedOverlay);
		right_arm.render(poseStack, buffer, packedLight, packedOverlay);
		left_arm.render(poseStack, buffer, packedLight, packedOverlay);
		left_leg.render(poseStack, buffer, packedLight, packedOverlay);
		right_leg.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(BloodDrunkPuppeteerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.body.yRot = 0.0F;
		this.right_arm.z = 0.0F;
		this.right_arm.x = -5.0F;
		this.left_arm.z = 0.0F;
		this.left_arm.x = 5.0F;
		float f = 1.0F;
		this.right_arm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 2.0F * limbSwingAmount * 0.5F / f;
		this.left_arm.xRot = Mth.cos(limbSwing * 0.6662F) * 2.0F * limbSwingAmount * 0.5F / f;
		this.right_arm.zRot = 0.0F;
		this.left_arm.zRot = 0.0F;
		this.right_leg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		this.left_leg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount / f;
		this.right_leg.yRot = 0.005F;
		this.left_leg.yRot = -0.005F;
		this.right_leg.zRot = 0.005F;
		this.left_leg.zRot = -0.005F;

	}


}