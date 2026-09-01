package com.vincenthuto.hemomancy.client.model.entity.mob.arthropod;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.arthropod.LanternTickEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class LanternTickModel extends EntityModel<LanternTickEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("lantern_tick"), "main");
	private static final float LEG_OUTWARD_Z_ROT = 0.68F;

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart abdomenGlow;
	private final ModelPart leftLegs;
	private final ModelPart leftLeg0;
	private final ModelPart leftLeg1;
	private final ModelPart leftLeg2;
	private final ModelPart leftLeg3;
	private final ModelPart rightLegs;
	private final ModelPart rightLeg0;
	private final ModelPart rightLeg1;
	private final ModelPart rightLeg2;
	private final ModelPart rightLeg3;

	public LanternTickModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.abdomenGlow = this.root.getChild("abdomen_glow");
		this.leftLegs = this.root.getChild("left_legs");
		this.leftLeg0 = this.leftLegs.getChild("left_leg_0");
		this.leftLeg1 = this.leftLegs.getChild("left_leg_1");
		this.leftLeg2 = this.leftLegs.getChild("left_leg_2");
		this.leftLeg3 = this.leftLegs.getChild("left_leg_3");
		this.rightLegs = this.root.getChild("right_legs");
		this.rightLeg0 = this.rightLegs.getChild("right_leg_0");
		this.rightLeg1 = this.rightLegs.getChild("right_leg_1");
		this.rightLeg2 = this.rightLegs.getChild("right_leg_2");
		this.rightLeg3 = this.rightLegs.getChild("right_leg_3");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 20.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -3.0F, -4.5F, 9.0F, 5.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 19).addBox(-3.5F, -4.0F, -3.5F, 7.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(-3, 52).addBox(-3.5F, 1.0F, -9.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(32, 33).addBox(-2.5F, -2.0F, -7.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition abdomen_glow = root.addOrReplaceChild("abdomen_glow", CubeListBuilder.create().texOffs(0, 33).addBox(-3.25F, -4.05F, -2.25F, 6.5F, 0.25F, 9.0F, new CubeDeformation(0.02F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_legs = root.addOrReplaceChild("left_legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_leg_0 = left_legs.addOrReplaceChild("left_leg_0", CubeListBuilder.create().texOffs(38, 19).addBox(-5.0F, -0.5F, -2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.25F, 0.95F, -1.8F, 0.0F, 0.0F, -0.68F));

		PartDefinition left_leg_1 = left_legs.addOrReplaceChild("left_leg_1", CubeListBuilder.create().texOffs(38, 21).addBox(-5.0F, -0.5F, -2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.25F, 0.95F, 0.55F, 0.0F, 0.0F, -0.68F));

		PartDefinition left_leg_2 = left_legs.addOrReplaceChild("left_leg_2", CubeListBuilder.create().texOffs(38, 23).addBox(-5.0F, -0.5F, -2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.25F, 0.95F, 2.9F, 0.0F, 0.0F, -0.68F));

		PartDefinition left_leg_3 = left_legs.addOrReplaceChild("left_leg_3", CubeListBuilder.create().texOffs(38, 25).addBox(-5.0F, -0.5F, -2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.25F, 0.95F, 5.25F, 0.0F, 0.0F, -0.68F));

		PartDefinition right_legs = root.addOrReplaceChild("right_legs", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition right_leg_0 = right_legs.addOrReplaceChild("right_leg_0", CubeListBuilder.create().texOffs(38, 27).addBox(0.0F, -0.5F, -2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, 0.95F, -1.8F, 0.0F, 0.0F, 0.68F));

		PartDefinition right_leg_1 = right_legs.addOrReplaceChild("right_leg_1", CubeListBuilder.create().texOffs(38, 29).addBox(0.0F, -0.5F, -2.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, 0.95F, 0.55F, 0.0F, 0.0F, 0.68F));

		PartDefinition right_leg_2 = right_legs.addOrReplaceChild("right_leg_2", CubeListBuilder.create().texOffs(38, 31).addBox(0.0F, -0.5F, -4.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, 0.95F, 4.9F, 0.0F, 0.0F, 0.68F));

		PartDefinition right_leg_3 = right_legs.addOrReplaceChild("right_leg_3", CubeListBuilder.create().texOffs(32, 41).addBox(0.0F, -0.5F, -4.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.25F, 0.95F, 7.25F, 0.0F, 0.0F, 0.68F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	private static void addAngledLeg(PartDefinition parent, String name, int texX, int texY, float x, float z,
			float boxZ, float zRot) {
		boolean rightSide = x > 0.0F;
		parent.addOrReplaceChild(name, CubeListBuilder.create()
					.texOffs(texX, texY).addBox(rightSide ? 0.0F : -5.0F, -0.5F, boxZ,
								5.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(x, 0.95F, z, 0.0F, 0.0F, zRot));
	}

	@Override
	public void setupAnim(LanternTickEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		resetPose();
		switch (entity.getLanternTickAnimationState()) {
			case LanternTickEntity.ANIMATION_STALKING -> {
				applyWalkCycle(limbSwing, limbSwingAmount);
				applyStalkingAnimation(limbSwing, limbSwingAmount, ageInTicks);
			}
			case LanternTickEntity.ANIMATION_LEAPING -> applyLeapingAnimation(ageInTicks);
			case LanternTickEntity.ANIMATION_LATCHED -> applyLatchedAnimation(ageInTicks);
			default -> {
				applyWalkCycle(limbSwing, limbSwingAmount * 0.45F);
				applyLureAnimation(ageInTicks);
			}
		}
		applyBitePulse(entity.getBiteAnimationTicks());
	}

	private void resetPose() {
		this.root.xRot = 0.0F;
		this.root.yRot = 0.0F;
		this.root.zRot = 0.0F;
		this.root.y = 20.0F;
		this.body.xRot = 0.0F;
		this.body.yRot = 0.0F;
		this.body.zRot = 0.0F;
		this.abdomenGlow.xRot = 0.0F;
		this.abdomenGlow.yRot = 0.0F;
		this.abdomenGlow.zRot = 0.0F;
		this.abdomenGlow.y = 0.0F;
		this.leftLegs.xRot = 0.0F;
		this.rightLegs.xRot = 0.0F;
		this.leftLegs.yRot = 0.0F;
		this.rightLegs.yRot = 0.0F;
		this.leftLegs.zRot = 0.0F;
		this.rightLegs.zRot = 0.0F;
		resetLeg(this.leftLeg0, -LEG_OUTWARD_Z_ROT);
		resetLeg(this.leftLeg1, -LEG_OUTWARD_Z_ROT);
		resetLeg(this.leftLeg2, -LEG_OUTWARD_Z_ROT);
		resetLeg(this.leftLeg3, -LEG_OUTWARD_Z_ROT);
		resetLeg(this.rightLeg0, LEG_OUTWARD_Z_ROT);
		resetLeg(this.rightLeg1, LEG_OUTWARD_Z_ROT);
		resetLeg(this.rightLeg2, LEG_OUTWARD_Z_ROT);
		resetLeg(this.rightLeg3, LEG_OUTWARD_Z_ROT);
	}

	private static void resetLeg(ModelPart leg, float baseZRot) {
		leg.xRot = 0.0F;
		leg.yRot = 0.0F;
		leg.zRot = baseZRot;
	}

	private void applyWalkCycle(float limbSwing, float limbSwingAmount) {
		if (limbSwingAmount <= 0.01F) {
			return;
		}
		float gaitSpeed = 1.9F;
		float stride = (Mth.cos(limbSwing * gaitSpeed) * 0.34F * limbSwingAmount*5)*2;
		float counterStride = Mth.cos(limbSwing * gaitSpeed + Mth.PI) * 0.34F * limbSwingAmount;
		float reach = Mth.sin(limbSwing * gaitSpeed) * 0.18F * limbSwingAmount;

		poseLeftLeg(this.leftLeg0, stride, -reach * 0.90F);
		poseLeftLeg(this.leftLeg1, counterStride, reach * 0.55F);
		poseLeftLeg(this.leftLeg2, stride, -reach * 0.55F);
		poseLeftLeg(this.leftLeg3, counterStride, reach * 0.90F);

		poseRightLeg(this.rightLeg0, stride, reach * 0.90F);
		poseRightLeg(this.rightLeg1, counterStride, -reach * 0.55F);
		poseRightLeg(this.rightLeg2, stride, reach * 0.55F);
		poseRightLeg(this.rightLeg3, counterStride, -reach * 0.90F);
	}

	private static void poseLeftLeg(ModelPart leg, float swing, float reach) {
		leg.zRot = -LEG_OUTWARD_Z_ROT - swing;
		leg.yRot = reach;
	}

	private static void poseRightLeg(ModelPart leg, float swing, float reach) {
		leg.zRot = LEG_OUTWARD_Z_ROT + swing;
		leg.yRot = reach;
	}

	private void applyLureAnimation(float ageInTicks) {
		this.root.y += Mth.sin(ageInTicks * 0.08F) * 0.18F;
		this.body.xRot = 0.08F;
		this.abdomenGlow.y = -0.08F + Mth.sin(ageInTicks * 0.18F) * 0.03F;
	}

	private void applyStalkingAnimation(float limbSwing, float limbSwingAmount, float ageInTicks) {
		float skitter = Mth.sin(limbSwing * 3.2F) * 0.42F * Math.max(0.35F, limbSwingAmount);
		this.root.xRot = 0.16F;
		this.root.y += Mth.sin(ageInTicks * 0.45F) * 0.12F;
		this.leftLegs.zRot = skitter;
		this.rightLegs.zRot = -skitter;
		this.abdomenGlow.y = -0.04F + Mth.sin(ageInTicks * 0.25F) * 0.02F;
	}

	private void applyLeapingAnimation(float ageInTicks) {
		this.root.xRot = -0.52F;
		this.root.y += Mth.sin(ageInTicks * 0.7F) * 0.16F;
		this.leftLegs.zRot = -0.62F;
		this.rightLegs.zRot = 0.62F;
		this.leftLegs.yRot = -0.18F;
		this.rightLegs.yRot = 0.18F;
		this.abdomenGlow.y = -0.03F;
	}

	private void applyLatchedAnimation(float ageInTicks) {
		this.root.xRot = 0.36F;
		this.root.y += 0.18F + Mth.sin(ageInTicks * 0.2F) * 0.08F;
		this.body.xRot = -0.12F;
		this.leftLegs.zRot = -0.82F;
		this.rightLegs.zRot = 0.82F;
		this.leftLegs.xRot = 0.18F;
		this.rightLegs.xRot = 0.18F;
		this.abdomenGlow.y = -0.02F;
	}

	private void applyBitePulse(int biteTicks) {
		if (biteTicks <= 0) {
			return;
		}
		float pulse = biteTicks / (float) LanternTickEntity.BITE_ANIMATION_TICKS;
		float squeeze = Mth.sin(pulse * Mth.PI) * 0.22F;
		this.body.xRot -= squeeze;
		this.abdomenGlow.zRot += squeeze * 0.45F;
		this.leftLegs.zRot -= squeeze * 0.8F;
		this.rightLegs.zRot += squeeze * 0.8F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		this.root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
