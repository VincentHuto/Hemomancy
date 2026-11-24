package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleEntity;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleTuning;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MnemonicWhaleModel extends EntityModel<MnemonicWhaleEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("mnemonic_whale"), "main");

	private final ModelPart body;
	private final ModelPart rearBody;
	private final ModelPart tail;
	private final ModelPart dorsal;
	private final ModelPart leftFin;
	private final ModelPart rightFin;
	private final ModelPart leftWisp;
	private final ModelPart rightWisp;

	public MnemonicWhaleModel(ModelPart root) {
		this.body = root.getChild("body");
		this.rearBody = this.body.getChild("rear_body");
		this.tail = this.rearBody.getChild("tail");
		this.dorsal = this.rearBody.getChild("dorsal");
		this.leftFin = this.body.getChild("left_fin");
		this.rightFin = this.body.getChild("right_fin");
		this.leftWisp = this.tail.getChild("left_wisp");
		this.rightWisp = this.tail.getChild("right_wisp");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(1, 68).addBox(-6.0F, -11.0F, -36.0F, 12.0F, 14.0F, 2.0F)
				.texOffs(57, 41).addBox(-8.0F, -13.0F, -34.0F, 16.0F, 18.0F, 5.0F)
				.texOffs(1, 1).addBox(-10.0F, -13.0F, -29.0F, 20.0F, 19.0F, 19.0F)
				.texOffs(193, 41).addBox(-7.0F, -16.0F, -31.0F, 14.0F, 3.0F, 14.0F)
				.texOffs(69, 86).addBox(-5.0F, -14.0F, -17.0F, 10.0F, 3.0F, 9.0F)
				.texOffs(157, 1).addBox(-6.0F, 5.0F, -32.0F, 12.0F, 3.0F, 23.0F)
				.texOffs(1, 41).addBox(-9.0F, -9.0F, -10.0F, 18.0F, 16.0F, 9.0F)
				.texOffs(81, 1).addBox(-8.0F, -7.0F, -1.0F, 16.0F, 13.0F, 21.0F), PartPose.offset(0.0F, 18.0F, -8.0F));

		PartDefinition rear_body = body.addOrReplaceChild("rear_body", CubeListBuilder.create()
				.texOffs(101, 41).addBox(-7.0F, -6.0F, -1.0F, 14.0F, 11.0F, 12.0F)
				.texOffs(31, 68).addBox(-5.0F, -4.0F, 11.0F, 10.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 18.0F));

		PartDefinition tail = rear_body.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(155, 41).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 12.0F)
				.texOffs(231, 102).addBox(-2.0F, -1.0F, 10.0F, 4.0F, 3.0F, 5.0F)
				.texOffs(183, 102).addBox(-6.0F, -1.0F, 8.0F, 4.0F, 2.0F, 7.0F)
				.texOffs(197, 86).addBox(-10.0F, -1.0F, 8.0F, 4.0F, 2.0F, 8.0F)
				.texOffs(229, 1).addBox(-14.0F, -1.0F, 9.0F, 4.0F, 2.0F, 9.0F)
				.texOffs(223, 86).addBox(-18.0F, 0.0F, 10.0F, 4.0F, 1.0F, 9.0F)
				.texOffs(1, 102).addBox(-21.0F, 0.0F, 12.0F, 3.0F, 1.0F, 9.0F)
				.texOffs(45, 114).addBox(-23.0F, 0.0F, 15.0F, 2.0F, 1.0F, 7.0F)
				.texOffs(207, 102).addBox(2.0F, -1.0F, 8.0F, 4.0F, 2.0F, 7.0F)
				.texOffs(55, 102).addBox(6.0F, -1.0F, 8.0F, 4.0F, 2.0F, 8.0F)
				.texOffs(169, 86).addBox(10.0F, -1.0F, 9.0F, 4.0F, 2.0F, 9.0F)
				.texOffs(81, 102).addBox(14.0F, 0.0F, 10.0F, 4.0F, 1.0F, 9.0F)
				.texOffs(109, 102).addBox(18.0F, 0.0F, 12.0F, 3.0F, 1.0F, 9.0F)
				.texOffs(65, 114).addBox(21.0F, 0.0F, 15.0F, 2.0F, 1.0F, 7.0F), PartPose.offset(0.0F, 0.0F, 17.0F));

		rear_body.addOrReplaceChild("dorsal", CubeListBuilder.create()
				.texOffs(69, 68).addBox(-2.0F, -3.0F, -6.0F, 4.0F, 3.0F, 13.0F)
				.texOffs(163, 102).addBox(-1.0F, -5.0F, -1.0F, 2.0F, 2.0F, 7.0F), PartPose.offset(0.0F, -5.0F, 3.0F));

		tail.addOrReplaceChild("left_wisp", CubeListBuilder.create()
				.texOffs(27, 102).addBox(-3.0F, -1.0F, -1.0F, 4.0F, 1.0F, 9.0F)
				.texOffs(85, 114).addBox(-4.0F, 0.0F, 6.0F, 3.0F, 1.0F, 6.0F)
				.texOffs(125, 114).addBox(-5.0F, 0.0F, 10.0F, 2.0F, 1.0F, 5.0F)
				.texOffs(245, 68).addBox(-5.0F, 1.0F, 13.0F, 1.0F, 1.0F, 4.0F), PartPose.offset(-6.0F, 1.0F, 12.0F));

		tail.addOrReplaceChild("right_wisp", CubeListBuilder.create()
				.texOffs(135, 102).addBox(-1.0F, -1.0F, -1.0F, 4.0F, 1.0F, 9.0F)
				.texOffs(105, 114).addBox(1.0F, 0.0F, 6.0F, 3.0F, 1.0F, 6.0F)
				.texOffs(141, 114).addBox(3.0F, 0.0F, 10.0F, 2.0F, 1.0F, 5.0F)
				.texOffs(157, 114).addBox(4.0F, 1.0F, 13.0F, 1.0F, 1.0F, 4.0F), PartPose.offset(6.0F, 1.0F, 12.0F));

		body.addOrReplaceChild("left_fin", CubeListBuilder.create()
				.texOffs(177, 68).addBox(-4.0F, 0.0F, -3.0F, 4.0F, 2.0F, 12.0F)
				.texOffs(105, 68).addBox(-8.0F, 0.0F, -2.0F, 4.0F, 2.0F, 13.0F)
				.texOffs(211, 68).addBox(-12.0F, 1.0F, 1.0F, 4.0F, 2.0F, 12.0F)
				.texOffs(109, 86).addBox(-16.0F, 1.0F, 5.0F, 4.0F, 2.0F, 10.0F)
				.texOffs(1, 114).addBox(-19.0F, 2.0F, 9.0F, 3.0F, 1.0F, 7.0F)
				.texOffs(169, 114).addBox(-21.0F, 2.0F, 14.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(-7.0F, 2.0F, -7.0F));

		body.addOrReplaceChild("right_fin", CubeListBuilder.create()
				.texOffs(1, 86).addBox(0.0F, 0.0F, -3.0F, 4.0F, 2.0F, 12.0F)
				.texOffs(141, 68).addBox(4.0F, 0.0F, -2.0F, 4.0F, 2.0F, 13.0F)
				.texOffs(35, 86).addBox(8.0F, 1.0F, 1.0F, 4.0F, 2.0F, 12.0F)
				.texOffs(139, 86).addBox(12.0F, 1.0F, 5.0F, 4.0F, 2.0F, 10.0F)
				.texOffs(23, 114).addBox(16.0F, 2.0F, 9.0F, 3.0F, 1.0F, 7.0F)
				.texOffs(181, 114).addBox(19.0F, 2.0F, 14.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(7.0F, 2.0F, -7.0F));

		return LayerDefinition.create(mesh, 256, 128);
	}


	@Override
	public void setupAnim(MnemonicWhaleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float swim = Mth.sin(ageInTicks * 0.12F);
		boolean moving = entity.getDeltaMovement().horizontalDistanceSqr() > 1.0E-7D;
		this.body.xRot = headPitch * ((float) Math.PI / 180.0F);
		this.body.yRot = netHeadYaw * ((float) Math.PI / 180.0F);
		this.rearBody.yRot = 0.0F;
		this.tail.yRot = 0.0F;
		if (moving) {
			this.body.xRot += -0.04F - 0.03F * Mth.cos(ageInTicks * 0.18F);
			this.rearBody.xRot = swim * MnemonicWhaleTuning.REAR_BODY_VERTICAL_SWING_RADIANS;
			this.tail.xRot = swim * MnemonicWhaleTuning.TAIL_FLUKE_VERTICAL_SWING_RADIANS;
		} else {
			this.rearBody.xRot = 0.0F;
			this.tail.xRot = 0.0F;
		}
		this.dorsal.zRot = swim * 0.05F;
		this.leftFin.zRot = -0.2F + Mth.sin(ageInTicks * 0.06F) * 0.08F;
		this.rightFin.zRot = 0.2F - Mth.sin(ageInTicks * 0.06F) * 0.08F;
		this.leftWisp.yRot = Mth.sin(ageInTicks * 0.09F) * 0.16F;
		this.rightWisp.yRot = Mth.sin(ageInTicks * 0.09F + 1.8F) * 0.16F;
		this.leftWisp.xRot = swim * 0.08F;
		this.rightWisp.xRot = Mth.sin(ageInTicks * 0.12F + 1.8F) * 0.08F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		this.body.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}

