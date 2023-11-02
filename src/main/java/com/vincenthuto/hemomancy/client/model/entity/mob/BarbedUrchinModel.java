package com.vincenthuto.hemomancy.client.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.BarbedUrchinEntity;

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
import net.minecraft.world.entity.Entity;

public class BarbedUrchinModel<T extends Entity> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modelbarbedurchin"),
			"main");
	private final ModelPart whole;

	public BarbedUrchinModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition base = whole.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 22).addBox(-5.0F,
				-2.0F, -5.0F, 10.0F, 2.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition base2 = whole.addOrReplaceChild("base2",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-6.0F, -2.25F, -6.0F, 12.0F, 10.0F, 12.0F, new CubeDeformation(0.0F)).texOffs(2, 2)
						.addBox(-5.0F, -3.25F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -9.75F, 0.0F));

		PartDefinition bone3 = base2.addOrReplaceChild("bone3",
				CubeListBuilder.create().texOffs(32, 56)
						.addBox(-8.0F, -4.25F, 4.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(32, 34)
						.addBox(-8.0F, -3.25F, -4.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition bone2 = base2.addOrReplaceChild("bone2",
				CubeListBuilder.create().texOffs(0, 56)
						.addBox(-8.0F, -4.5F, 3.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(48, 11)
						.addBox(-8.0F, -5.5F, 2.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 45)
						.addBox(-8.0F, -5.5F, 0.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(36, 0)
						.addBox(-8.0F, -6.5F, -2.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 34)
						.addBox(-8.0F, -5.5F, -3.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 1.25F, 0.0F));

		PartDefinition bone6 = bone2.addOrReplaceChild("bone6",
				CubeListBuilder.create().texOffs(40, 22)
						.addBox(-8.0F, -5.0F, -1.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(32, 45)
						.addBox(-8.0F, -6.0F, 1.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition bone = base2.addOrReplaceChild("bone",
				CubeListBuilder.create().texOffs(72, 22)
						.addBox(-8.0F, -5.5F, 3.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(68, 0)
						.addBox(-8.0F, -5.5F, 2.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(0, 67)
						.addBox(-8.0F, -5.5F, 0.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(64, 55)
						.addBox(-8.0F, -5.5F, -2.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(64, 33)
						.addBox(-8.0F, -5.5F, -3.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.25F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition bone5 = bone.addOrReplaceChild("bone5",
				CubeListBuilder.create().texOffs(64, 66)
						.addBox(-8.0F, -15.0F, -1.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(32, 67)
						.addBox(-8.0F, -15.0F, 1.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 8.5F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition bone4 = bone.addOrReplaceChild("bone4",
				CubeListBuilder.create().texOffs(64, 77)
						.addBox(-8.0F, -5.5F, 4.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)).texOffs(64, 44)
						.addBox(-8.0F, -5.5F, -4.0F, 16.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public ModelPart root() {
		return this.whole;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		entity.level().random.nextFloat();
		this.animate(((BarbedUrchinEntity)entity).idleAnimationState, BarbedUrchinModel.IDLE, ageInTicks );

	}

	public static final AnimationDefinition IDLE = AnimationDefinition.Builder.withLength(2f)
			.looping()
			.addAnimation("base2",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8343334f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0416767f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.2916767f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone3",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8343334f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0416767f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.2916767f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone2",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8343334f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0416767f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.2916767f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.addAnimation("bone",
					new AnimationChannel(AnimationChannel.Targets.SCALE,
							new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(0.8343334f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.0416767f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.2916767f, KeyframeAnimations.scaleVec(0.95f, 0.95f, 0.95f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM),
							new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
									AnimationChannel.Interpolations.CATMULLROM)))
			.build();

}