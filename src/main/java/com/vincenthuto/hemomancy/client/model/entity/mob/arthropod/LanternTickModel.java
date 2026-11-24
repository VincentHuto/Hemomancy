package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.LanternTickEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class LanternTickModel extends EntityModel<LanternTickEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("lantern_tick"), "main");
	public static final ResourceLocation TEXTURE = Hemomancy.rloc("textures/entity/lantern_tick/model_lantern_tick.png");
	private static final RenderType LANTERN_GLOW = RenderType.entityTranslucentEmissive(
			Hemomancy.rloc("textures/entity/lantern_tick/model_lantern_tick_glow.png"));
	private static final int LEGS_PER_SIDE = 4;
	private static final float LATCHED_Y_OFFSET = 5.5F;

	private final ModelPart root;
	private final ModelPart feedingHead;
	private final ModelPart abdomen;
	private final ModelPart[] hips = new ModelPart[8];
	private final ModelPart[] innerLegs = new ModelPart[8];
	private final ModelPart[] outerLegs = new ModelPart[8];

	public LanternTickModel(ModelPart root) {
		this.root = root.getChild("lantern_tick");
		ModelPart body = this.root.getChild("body");
		this.feedingHead = body.getChild("feeding_head");
		this.abdomen = body.getChild("abdomen");
		for (int i = 0; i < hips.length; i++) {
			String side = i < LEGS_PER_SIDE ? "left" : "right";
			hips[i] = body.getChild(side + "_leg_" + (i % LEGS_PER_SIDE + 1));
			innerLegs[i] = hips[i].getChild("inner_leg");
			outerLegs[i] = innerLegs[i].getChild("outer_leg");
		}
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition tick = mesh.getRoot().addOrReplaceChild("lantern_tick", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));
		PartDefinition body = tick.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(1, 1).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 3.0F, 17.0F)
				.texOffs(56, 1).addBox(-5.0F, -3.0F, -6.0F, 10.0F, 2.0F, 4.0F),
				PartPose.offset(0.0F, -4.0F, -3.0F));
		body.addOrReplaceChild("feeding_head", CubeListBuilder.create()
				.texOffs(85, 1).addBox(-2.0F, -1.0F, -3.0F, 4.0F, 3.0F, 3.0F)
				.texOffs(100, 1).addBox(-1.0F, 1.0F, -5.0F, 2.0F, 1.0F, 3.0F)
				.texOffs(111, 1).addBox(-3.0F, 0.0F, -4.0F, 1.0F, 2.0F, 3.0F)
				.texOffs(1, 22).addBox(2.0F, 0.0F, -4.0F, 1.0F, 2.0F, 3.0F)
				.texOffs(10, 22).addBox(-4.0F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F)
				.texOffs(15, 22).addBox(3.0F, -2.0F, 0.0F, 1.0F, 1.0F, 1.0F),
				PartPose.offset(0.0F, -1.0F, -4.0F));
		body.addOrReplaceChild("abdomen", CubeListBuilder.create()
				.texOffs(20, 22).addBox(-6.0F, -1.0F, -5.0F, 12.0F, 2.0F, 17.0F)
				.texOffs(1, 42).addBox(-6.0F, -5.0F, -5.0F, 12.0F, 4.0F, 18.0F)
				.texOffs(62, 42).addBox(-7.0F, -4.0F, -2.0F, 1.0F, 3.0F, 13.0F)
				.texOffs(91, 42).addBox(6.0F, -4.0F, -2.0F, 1.0F, 3.0F, 13.0F)
				.texOffs(1, 65).addBox(-5.0F, -6.0F, -4.0F, 10.0F, 1.0F, 16.0F)
				.texOffs(54, 65).addBox(-5.0F, -4.0F, 13.0F, 10.0F, 3.0F, 1.0F),
				PartPose.offset(0.0F, -1.0F, 2.0F));
		PartDefinition left_leg_1 = body.addOrReplaceChild("left_leg_1", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4.0F, -1.0F, -4.0F, 0.0F, 0.80285146F, 0.0F));
		PartDefinition left_leg_1_hinge = left_leg_1.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(77, 65).addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.26179939F));
		left_leg_1_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(94, 65).addBox(0.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9250245F));
		PartDefinition left_leg_2 = body.addOrReplaceChild("left_leg_2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4.0F, -1.0F, -2.0F, 0.0F, 0.33161256F, 0.0F));
		PartDefinition left_leg_2_hinge = left_leg_2.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(107, 65).addBox(0.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.26179939F));
		left_leg_2_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(1, 83).addBox(0.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9250245F));
		PartDefinition left_leg_3 = body.addOrReplaceChild("left_leg_3", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4.0F, -1.0F, 0.0F, 0.0F, -0.20943951F, 0.0F));
		PartDefinition left_leg_3_hinge = left_leg_3.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(14, 83).addBox(0.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.26179939F));
		left_leg_3_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(33, 83).addBox(0.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(6.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9250245F));
		PartDefinition left_leg_4 = body.addOrReplaceChild("left_leg_4", CubeListBuilder.create(),
				PartPose.offsetAndRotation(4.0F, -1.0F, 2.0F, 0.0F, -0.66322512F, 0.0F));
		PartDefinition left_leg_4_hinge = left_leg_4.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(46, 83).addBox(0.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.26179939F));
		left_leg_4_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(63, 83).addBox(0.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(5.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.9250245F));
		PartDefinition right_leg_1 = body.addOrReplaceChild("right_leg_1", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4.0F, -1.0F, -4.0F, 0.0F, -0.80285146F, 0.0F));
		PartDefinition right_leg_1_hinge = right_leg_1.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(76, 83).addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.26179939F));
		right_leg_1_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(93, 83).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9250245F));
		PartDefinition right_leg_2 = body.addOrReplaceChild("right_leg_2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4.0F, -1.0F, -2.0F, 0.0F, -0.33161256F, 0.0F));
		PartDefinition right_leg_2_hinge = right_leg_2.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(106, 83).addBox(-7.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.26179939F));
		right_leg_2_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(1, 88).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9250245F));
		PartDefinition right_leg_3 = body.addOrReplaceChild("right_leg_3", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4.0F, -1.0F, 0.0F, 0.0F, 0.20943951F, 0.0F));
		PartDefinition right_leg_3_hinge = right_leg_3.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(14, 88).addBox(-7.0F, -1.0F, -1.0F, 7.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.26179939F));
		right_leg_3_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(33, 88).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9250245F));
		PartDefinition right_leg_4 = body.addOrReplaceChild("right_leg_4", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-4.0F, -1.0F, 2.0F, 0.0F, 0.66322512F, 0.0F));
		PartDefinition right_leg_4_hinge = right_leg_4.addOrReplaceChild("inner_leg", CubeListBuilder.create()
				.texOffs(46, 88).addBox(-6.0F, -1.0F, -1.0F, 6.0F, 2.0F, 2.0F),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.26179939F));
		right_leg_4_hinge.addOrReplaceChild("outer_leg", CubeListBuilder.create()
				.texOffs(63, 88).addBox(-5.0F, -1.0F, 0.0F, 5.0F, 1.0F, 1.0F),
				PartPose.offsetAndRotation(-5.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.9250245F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(LanternTickEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		resetPose();
		switch (entity.getLanternTickAnimationState()) {
			case LanternTickEntity.ANIMATION_STALKING -> {
				applyWalkCycle(limbSwing, limbSwingAmount);
				applyStalkingAnimation(limbSwing, limbSwingAmount);
			}
			case LanternTickEntity.ANIMATION_LEAPING -> applyLeapingAnimation();
			case LanternTickEntity.ANIMATION_LATCHED -> applyLatchedAnimation(ageInTicks);
			default -> {
				applyWalkCycle(limbSwing, limbSwingAmount * 0.45F);
				applyLureAnimation(ageInTicks);
			}
		}
		applyBitePulse(entity.getBiteAnimationTicks());
	}

	public void setupHelmetPose(float ageInTicks) {
		resetPose();
		applyGrip(0.18F, 0.48F);
		abdomen.xRot = Mth.sin(ageInTicks * 0.15F) * 0.02F;
	}

	private void resetPose() {
		root.getAllParts().forEach(ModelPart::resetPose);
	}

	private void applyWalkCycle(float limbSwing, float amount) {
		for (int i = 0; i < hips.length; i++) {
			int side = i < LEGS_PER_SIDE ? 1 : -1;
			int pair = i % LEGS_PER_SIDE;
			float phase = limbSwing * 1.9F + ((pair + i / LEGS_PER_SIDE) % 2) * Mth.PI;
			float lift = Math.max(0, Mth.sin(phase)) * amount;
			hips[i].yRot += side * Mth.cos(phase) * 0.26F * amount;
			innerLegs[i].zRot -= side * lift * 0.25F;
			outerLegs[i].zRot += side * lift * 0.20F;
		}
	}

	private void applyLureAnimation(float ageInTicks) {
		abdomen.xRot = Mth.sin(ageInTicks * 0.12F) * 0.025F;
		feedingHead.xRot = Mth.sin(ageInTicks * 0.18F) * 0.06F;
	}

	private void applyStalkingAnimation(float limbSwing, float amount) {
		root.y += 0.12F + Mth.sin(limbSwing * 3.8F) * amount * 0.16F;
		feedingHead.xRot = 0.12F;
	}

	private void applyLeapingAnimation() {
		root.xRot = -0.35F;
		applyGrip(-0.32F, -0.38F);
	}

	private void applyLatchedAnimation(float ageInTicks) {
		root.xRot = 0.16F;
		feedingHead.xRot = 0.18F;
		applyGrip(0.30F, 0.65F);
		abdomen.xRot = Mth.sin(ageInTicks * 0.20F) * 0.025F;
	}

	private void applyGrip(float inner, float outer) {
		for (int i = 0; i < hips.length; i++) {
			int side = i < LEGS_PER_SIDE ? 1 : -1;
			innerLegs[i].zRot += side * inner;
			outerLegs[i].zRot += side * outer;
		}
	}

	private void applyBitePulse(int biteTicks) {
		if (biteTicks > 0) {
			float pulse = Mth.sin(biteTicks * Mth.PI / LanternTickEntity.BITE_ANIMATION_TICKS);
			feedingHead.xRot += pulse * 0.32F;
			abdomen.xRot -= pulse * 0.06F;
			applyGrip(pulse * 0.08F, pulse * 0.16F);
		}
	}

	public void renderLantern(PoseStack pose, MultiBufferSource buffers) {
		root.render(pose, buffers.getBuffer(LANTERN_GLOW), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
