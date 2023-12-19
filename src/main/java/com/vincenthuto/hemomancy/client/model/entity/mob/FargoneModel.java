package com.vincenthuto.hemomancy.client.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.FargoneEntity;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class FargoneModel extends HierarchicalModel<FargoneEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("fargonemodel"),
			"main");
	private final ModelPart whole;

	public FargoneModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, -14.9328F, 2.4651F, 0.2618F, 0.0F, 0.0F));

		PartDefinition rightArm = body.addOrReplaceChild("rightArm",
				CubeListBuilder.create().texOffs(24, 52)
						.addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(14, 15)
						.addBox(-1.05F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.5F, -7.0672F, -3.4651F, -0.2618F, 0.0F, 0.0F));

		PartDefinition rightFore = rightArm.addOrReplaceChild("rightFore",
				CubeListBuilder.create().texOffs(16, 44)
						.addBox(0.0F, 5.1667F, -1.4167F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(24, 52)
						.addBox(-1.0F, 0.1667F, -0.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 3.8333F, 0.6667F));

		PartDefinition rightArm2 = body.addOrReplaceChild("rightArm2",
				CubeListBuilder.create().texOffs(24, 52)
						.addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(14, 15)
						.addBox(-1.05F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.5F, -3.0672F, -1.4651F, -0.2618F, 0.0F, 0.0F));

		PartDefinition rightFore2 = rightArm2.addOrReplaceChild("rightFore2",
				CubeListBuilder.create().texOffs(16, 44)
						.addBox(0.0F, 5.1667F, -1.4167F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(24, 52)
						.addBox(-1.0F, 0.1667F, -0.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 3.8333F, 0.6667F));

		PartDefinition leftArm = body.addOrReplaceChild("leftArm",
				CubeListBuilder.create().texOffs(16, 50)
						.addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(16, 28)
						.addBox(-0.95F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.5F, -7.0672F, -3.4651F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leftFore = leftArm.addOrReplaceChild("leftFore",
				CubeListBuilder.create().texOffs(16, 50)
						.addBox(-1.0F, -0.8333F, -0.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(16, 44)
						.addBox(0.0F, 4.1667F, -1.4167F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 4.8333F, 0.6667F));

		PartDefinition leftArm2 = body.addOrReplaceChild("leftArm2",
				CubeListBuilder.create().texOffs(16, 50)
						.addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(16, 28)
						.addBox(-0.95F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(4.5F, -3.0672F, -1.4651F, -0.2618F, 0.0F, 0.0F));

		PartDefinition leftFore2 = leftArm2.addOrReplaceChild("leftFore2",
				CubeListBuilder.create().texOffs(16, 50)
						.addBox(-1.0F, -0.8333F, -0.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(16, 44)
						.addBox(0.0F, 4.1667F, -1.4167F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 4.8333F, 0.6667F));

		PartDefinition leftLeg = body.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(16, 44)
				.addBox(0.0333F, 8.1799F, -1.9123F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(67, 55)
				.addBox(-1.5667F, -3.8201F, -1.3623F, 3.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 15)
				.addBox(-1.4667F, 0.5799F, -1.8623F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.4667F, 3.2846F, -1.5888F, -0.2618F, 0.0F, 0.0F));

		PartDefinition rightLeg = body.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(16, 44)
				.addBox(-0.0333F, 8.1799F, -1.9123F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(48, 50)
				.addBox(-1.5333F, 0.5799F, -1.8623F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(34, 81)
				.addBox(-1.4333F, -3.8201F, -1.3623F, 3.0F, 13.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.4667F, 3.2846F, -1.5888F, -0.2618F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, -9.3686F, -2.2916F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head2 = head.addOrReplaceChild("head2",
				CubeListBuilder.create().texOffs(1, 1)
						.addBox(-3.0F, -0.9883F, -4.3984F, 6.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(60, 106)
						.addBox(-1.0F, -6.9883F, -6.3984F, 6.0F, 7.0F, 11.0F, new CubeDeformation(-1.0F)).texOffs(0, 96)
						.addBox(-5.0F, -6.9883F, -6.3984F, 6.0F, 7.0F, 11.0F, new CubeDeformation(-1.0F)),
				PartPose.offsetAndRotation(0.0F, -0.7104F, 0.2249F, -0.2618F, 0.0F, 0.0F));

		PartDefinition lowerJaw = head2.addOrReplaceChild("lowerJaw", CubeListBuilder.create().texOffs(31, 9)
				.addBox(-2.0F, -3.2607F, -3.3947F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(14, 15)
				.addBox(-1.0F, -3.2607F, -13.3947F, 2.0F, 1.0F, 11.0F, new CubeDeformation(-1.0F)).texOffs(33, 64)
				.addBox(-0.5F, -2.2607F, -15.3947F, 1.0F, 1.0F, 13.0F, new CubeDeformation(-1.0F)).texOffs(17, 27)
				.addBox(-1.5F, -4.2607F, -11.3947F, 3.0F, 3.0F, 9.0F, new CubeDeformation(-1.0F)),
				PartPose.offsetAndRotation(0.0F, 2.2724F, -0.0037F, -0.0873F, 0.0F, 0.0F));

		PartDefinition bone = lowerJaw.addOrReplaceChild("bone", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-0.7767F, -3.1458F, -5.3014F, -0.7854F, 0.0F, -0.6981F));

		PartDefinition bone2 = lowerJaw.addOrReplaceChild("bone2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(1.0805F, -3.1458F, -5.3014F, -0.7854F, 0.0F, 0.6981F));

		PartDefinition neck = head.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(42, 33).addBox(-1.5F,
				-27.6987F, 1.3265F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, -2.0F));

		PartDefinition leftWing = body.addOrReplaceChild("leftWing",
				CubeListBuilder.create().texOffs(89, 19).addBox(-0.0581F, 0.0F, -6.0419F, 0.0F, 16.0F, 6.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.0F, -9.1708F, 1.977F, 0.0F, -1.5708F, 0.0F));

		PartDefinition rightWing = body.addOrReplaceChild("rightWing",
				CubeListBuilder.create().texOffs(92, -4).addBox(-0.0581F, 0.0F, -0.0419F, 0.0F, 16.0F, 6.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-3.0F, -9.1708F, 1.977F, 0.0F, -1.5708F, 0.0F));

		PartDefinition abdomen = body.addOrReplaceChild("abdomen",
				CubeListBuilder.create().texOffs(40, 39)
						.addBox(-3.5F, -4.6525F, -3.0465F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 65)
						.addBox(-3.0F, -5.6525F, 0.4535F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(24, 0)
						.addBox(-2.5F, 2.305F, -1.907F, 5.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -3.4147F, -0.4186F));

		PartDefinition tail = body.addOrReplaceChild("tail",
				CubeListBuilder.create().texOffs(59, 0).addBox(-2.5F, -3.5F, -0.5F, 5.0F, 14.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 3.3903F, 0.1743F, 0.3054F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(FargoneEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks,
			float pNetHeadYaw, float pHeadPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(pEntity.idleAnimationState, FargoneModel.IDLE, pAgeInTicks);
		if(pEntity.isMovingOnLand()) {
			this.animate(pEntity.idleAnimationState, FargoneModel.WALK, pAgeInTicks);
		}

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.whole;
	}

	public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2f).looping()
			.addAnimation("body",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightArm",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightFore",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightArm2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightFore2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftArm",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftFore",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftArm2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.4167667f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftFore2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head", new AnimationChannel(AnimationChannel.Targets.POSITION,
					new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.posVec(0f, 0f, 0f), AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.posVec(0f, 0f, 0f),
							AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head2",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(-3.54f, 5.93f, -21.82f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(-3.54f, 5.93f, 21.82f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("lowerJaw",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftWing",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.125f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.375f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.625f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.875f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.125f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.375f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.625f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.875f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightWing",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.125f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.375f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.625f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.875f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.125f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.375f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.625f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.75f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.875f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("tail",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.5f, KeyframeAnimations.degreeVec(10f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.degreeVec(-10f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("abdomen",
					new AnimationChannel(AnimationChannel.Targets.ROTATION,
							new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1f, KeyframeAnimations.degreeVec(-7.5f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.build();  
	
	
	public static final AnimationDefinition WALK = AnimationDefinition.Builder.withLength(2f).looping()
			.addAnimation("body",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(17.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightArm",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightFore",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightArm2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightFore2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftArm",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftFore",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftArm2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(12.48f, 1.15f, 2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4167667f, KeyframeAnimations.degreeVec(-12.48f, -1.15f, -2.22f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftFore2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head",
				new AnimationChannel(AnimationChannel.Targets.POSITION, 
					new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM), 
					new Keyframe(1f, KeyframeAnimations.posVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM), 
					new Keyframe(2f, KeyframeAnimations.posVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head",
				new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("head2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(-15f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(-27.97f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("lowerJaw",
				new AnimationChannel(AnimationChannel.Targets.SCALE,
					new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftWing",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.041676664f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.16766666f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5416766f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6766666f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583434f, KeyframeAnimations.degreeVec(-12.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0416767f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.125f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1676667f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2916767f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.375f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4167667f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5416767f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6766667f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7083433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7916767f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.8343333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9167667f, KeyframeAnimations.degreeVec(-90f, -15f, 72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9583433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightWing",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.041676664f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.125f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.16766666f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.375f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5416766f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.6766666f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.9583434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.0416767f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.125f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.1676667f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.25f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.2916767f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.375f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.4167667f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5416767f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.6766667f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7083433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.7916767f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.8343333f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9167667f, KeyframeAnimations.degreeVec(90f, 15f, -72.5f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.9583433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("tail",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(10f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(-10f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("abdomen",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1f, KeyframeAnimations.degreeVec(-7.5f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftLeg",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(30.45f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightLeg",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(30.45f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("rightLeg2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(30.45f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(40.45f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("leftLeg2",
				new AnimationChannel(AnimationChannel.Targets.ROTATION,
					new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(0.5f, KeyframeAnimations.degreeVec(40.45f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(1.5f, KeyframeAnimations.degreeVec(30.45f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM),
					new Keyframe(2f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
						AnimationChannel.Interpolations.CATMULLROM))).build();

}