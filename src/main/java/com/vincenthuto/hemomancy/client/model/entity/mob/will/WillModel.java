package com.vincenthuto.hemomancy.client.model.entity.mob.will;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.monster.will.WillEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class WillModel extends EntityModel<WillEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("will"), "main");
	private static final int WILL_TRANSLUCENT_COLOR = 0x22FFFFFF;

	private final ModelPart whole;
	private final ModelPart head;
	private final ModelPart body;
	private final ModelPart ClothBackR1;
	private final ModelPart ClothBackL1;
	private final ModelPart ClothBackR2;
	private final ModelPart ClothBackR3;
	private final ModelPart ClothBackL2;
	private final ModelPart ClothBackL3;
	private final ModelPart SideclothR;
	private final ModelPart SideclothR4;
	private final ModelPart SideclothR5;
	private final ModelPart SideclothR6;
	private final ModelPart SideclothL;
	private final ModelPart SideclothR1;
	private final ModelPart SideclothR2;
	private final ModelPart SideclothR3;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;

	public WillModel(ModelPart root) {
		this.whole = root.getChild("whole");
		this.head = this.whole.getChild("head");
		this.body = this.whole.getChild("body");
		ModelPart clothBack = this.body.getChild("ClothBack");
		ModelPart clothBack1 = clothBack.getChild("ClothBack1");
		this.ClothBackR1 = clothBack1.getChild("ClothBackR1");
		this.ClothBackL1 = clothBack1.getChild("ClothBackL1");
		ModelPart clothBack2 = clothBack1.getChild("ClothBack2");
		this.ClothBackR2 = clothBack2.getChild("ClothBackR2");
		this.ClothBackR3 = clothBack2.getChild("ClothBackR3");
		this.ClothBackL2 = clothBack2.getChild("ClothBackL2");
		this.ClothBackL3 = clothBack2.getChild("ClothBackL3");
		this.SideclothR = this.body.getChild("SideclothR");
		this.SideclothR4 = this.SideclothR.getChild("SideclothR4");
		this.SideclothR5 = this.SideclothR4.getChild("SideclothR5");
		this.SideclothR6 = this.SideclothR5.getChild("SideclothR6");
		this.SideclothL = this.body.getChild("SideclothL");
		this.SideclothR1 = this.SideclothL.getChild("SideclothR1");
		this.SideclothR2 = this.SideclothR1.getChild("SideclothR2");
		this.SideclothR3 = this.SideclothR2.getChild("SideclothR3");
		this.leftArm = this.whole.getChild("leftArm");
		this.rightArm = this.whole.getChild("rightArm");
		this.leftLeg = this.whole.getChild("leftLeg");
		this.rightLeg = this.whole.getChild("rightLeg");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		head.addOrReplaceChild("Hood1",
				CubeListBuilder.create().texOffs(0, 0)
						.addBox(-4.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
						.texOffs(2, 2)
						.addBox(-3.5F, -8.0F, -3.6F, 7.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(8, 1)
						.addBox(3.5F, -8.0F, -4.6F, 1.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
						.texOffs(0, 0)
						.addBox(-4.5F, -9.0F, -4.6F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		head.addOrReplaceChild("Hood2",
				CubeListBuilder.create().texOffs(25, 19)
						.addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

		head.addOrReplaceChild("Hood3",
				CubeListBuilder.create().texOffs(42, 45)
						.addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

		head.addOrReplaceChild("Hood4",
				CubeListBuilder.create().texOffs(38, 57)
						.addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

		PartDefinition body = whole.addOrReplaceChild("body",
				CubeListBuilder.create().texOffs(0, 73)
						.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(0, 18)
						.addBox(3.3498F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(0, 18)
						.addBox(-4.2998F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(4, 93)
						.addBox(-3.9F, -0.5F, 1.6F, 8.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(38, 85)
						.addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		body.addOrReplaceChild("ClothchestR_108_38_cf815bb5_r1",
				CubeListBuilder.create().texOffs(37, 68)
						.addBox(2.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(17, 36)
						.addBox(-4.1F, 0.5F, -3.5F, 2.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.0F, 0.25F, 0.0F, 0.0F, 0.0F));

		PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(),
				PartPose.offset(0.0F, 12.3F, 4.4F));

		PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(),
				PartPose.offset(4.0F, 0.0F, -1.0F));

		ClothBack1.addOrReplaceChild("ClothBackR1",
				CubeListBuilder.create().texOffs(26, 60)
						.addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		ClothBack1.addOrReplaceChild("ClothBackL1",
				CubeListBuilder.create().texOffs(63, 42)
						.addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-2.0F, 8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

		ClothBack2.addOrReplaceChild("ClothBackR2",
				CubeListBuilder.create().texOffs(58, 68)
						.addBox(-1.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		ClothBack2.addOrReplaceChild("ClothBackR3",
				CubeListBuilder.create().texOffs(37, 13)
						.addBox(-4.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		ClothBack2.addOrReplaceChild("ClothBackL2",
				CubeListBuilder.create().texOffs(63, 68)
						.addBox(-4.0F, 7.3522F, -2.8768F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		ClothBack2.addOrReplaceChild("ClothBackL3",
				CubeListBuilder.create().texOffs(63, 52)
						.addBox(-3.0F, 7.3522F, -2.8768F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(),
				PartPose.offset(-3.8F, 12.25F, 0.0F));

		PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4",
				CubeListBuilder.create().texOffs(57, 57).mirror()
						.addBox(-1.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
						.mirror(false),
				PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5",
				CubeListBuilder.create().texOffs(63, 24).mirror()
						.addBox(-0.291F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
						.mirror(false),
				PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));

		SideclothR5.addOrReplaceChild("SideclothR6",
				CubeListBuilder.create().texOffs(63, 33).mirror()
						.addBox(-0.866F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
						.mirror(false),
				PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(),
				PartPose.offset(3.8F, 12.25F, 0.0F));

		PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1",
				CubeListBuilder.create().texOffs(57, 57)
						.addBox(0.0416F, 0.0691F, -2.5F, 1.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

		PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2",
				CubeListBuilder.create().texOffs(63, 24)
						.addBox(-0.709F, -0.6426F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));

		SideclothR2.addOrReplaceChild("SideclothR3",
				CubeListBuilder.create().texOffs(63, 33)
						.addBox(-0.134F, -0.134F, -2.5F, 1.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition leftArm = whole.addOrReplaceChild("leftArm",
				CubeListBuilder.create().texOffs(38, 0)
						.addBox(-0.5F, 2.5F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(48, 24)
						.addBox(-0.5F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(58, 9)
						.addBox(-0.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r1",
				CubeListBuilder.create().texOffs(48, 13)
						.addBox(-1.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.125F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		leftArm.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(-5.0F, -2.0F, 1.0F));

		PartDefinition rightArm = whole.addOrReplaceChild("rightArm",
				CubeListBuilder.create().texOffs(42, 32)
						.addBox(-3.5F, 2.5F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(0, 64)
						.addBox(-3.5F, 5.5F, 2.5F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(67, 9)
						.addBox(-2.5F, 3.5F, 2.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));

		rightArm.addOrReplaceChild("ShoulderL_16_45_e24b0f90_r1",
				CubeListBuilder.create().texOffs(17, 49)
						.addBox(-3.5F, -2.5F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.125F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F));

		whole.addOrReplaceChild("leftLeg",
				CubeListBuilder.create().texOffs(25, 32)
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));

		whole.addOrReplaceChild("rightLeg",
				CubeListBuilder.create().texOffs(0, 36)
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(WillEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * ((float) Math.PI / 180F);
		this.head.xRot = headPitch * ((float) Math.PI / 180F);
		this.body.yRot = 0.0F;
		this.rightArm.z = 0.0F;
		this.rightArm.x = -5.0F;
		this.leftArm.z = 0.0F;
		this.leftArm.x = 5.0F;

		this.rightArm.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * limbSwingAmount;
		this.leftArm.xRot = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount;
		this.rightArm.zRot = 0.0F;
		this.leftArm.zRot = 0.0F;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float) Math.PI) * 1.4F * limbSwingAmount;
		this.rightLeg.yRot = 0.005F;
		this.leftLeg.yRot = -0.005F;
		this.rightLeg.zRot = 0.005F;
		this.leftLeg.zRot = -0.005F;

		float idleWave = Mth.sin(ageInTicks * 0.067F);
		float walkWave = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;

		this.ClothBackR1.xRot = 0.1047F + idleWave * 0.03F + walkWave * 0.07F;
		this.ClothBackR2.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 0.6F) * 0.035F + walkWave * 0.08F;
		this.ClothBackR3.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 1.0F) * 0.04F + walkWave * 0.10F;
		this.ClothBackL1.xRot = 0.1047F + Mth.sin(ageInTicks * 0.067F + 0.3F) * 0.03F + walkWave * 0.07F;
		this.ClothBackL2.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 0.9F) * 0.035F + walkWave * 0.08F;
		this.ClothBackL3.xRot = 0.2269F + Mth.sin(ageInTicks * 0.067F + 1.3F) * 0.04F + walkWave * 0.10F;

		float sideSway = Mth.sin(ageInTicks * 0.09F) * 0.05F + walkWave * 0.05F;
		this.SideclothL.zRot = -0.08F + sideSway;
		this.SideclothR.zRot = 0.08F - sideSway;
		this.SideclothR1.zRot = -0.1222F + Mth.sin(ageInTicks * 0.067F + 0.2F) * 0.03F + walkWave * 0.04F;
		this.SideclothR2.zRot = -0.2967F + Mth.sin(ageInTicks * 0.067F + 0.8F) * 0.035F + walkWave * 0.05F;
		this.SideclothR3.zRot = -0.5236F + Mth.sin(ageInTicks * 0.067F + 1.2F) * 0.04F + walkWave * 0.06F;
		this.SideclothR4.zRot = 0.1222F + Mth.sin(ageInTicks * 0.067F + 0.5F) * 0.03F + walkWave * 0.04F;
		this.SideclothR5.zRot = 0.2967F + Mth.sin(ageInTicks * 0.067F + 1.0F) * 0.035F + walkWave * 0.05F;
		this.SideclothR6.zRot = 0.5236F + Mth.sin(ageInTicks * 0.067F + 1.4F) * 0.04F + walkWave * 0.06F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		renderWithColor(poseStack, buffer, packedLight, packedOverlay, WILL_TRANSLUCENT_COLOR);
	}

	public void renderWithColor(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		whole.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
