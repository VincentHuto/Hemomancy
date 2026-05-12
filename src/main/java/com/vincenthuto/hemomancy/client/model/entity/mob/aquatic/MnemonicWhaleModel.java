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

	public MnemonicWhaleModel(ModelPart root) {
		this.body = root.getChild("body");
		this.rearBody = this.body.getChild("rear_body");
		this.tail = this.rearBody.getChild("tail");
		this.dorsal = this.rearBody.getChild("dorsal");
		this.leftFin = this.body.getChild("left_fin");
		this.rightFin = this.body.getChild("right_fin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-10.0F, -6.0F, -28.0F, MnemonicWhaleTuning.MODEL_HEAD_WIDTH_UNITS, 12.0F, 18.0F)
						.texOffs(0, 32).addBox(-8.0F, -5.0F, -10.0F, MnemonicWhaleTuning.MODEL_MID_BODY_WIDTH_UNITS, 10.0F, 28.0F),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		PartDefinition rearBody = body.addOrReplaceChild("rear_body", CubeListBuilder.create()
						.texOffs(0, 70).addBox(-7.0F, -4.5F, 0.0F, MnemonicWhaleTuning.MODEL_REAR_BODY_WIDTH_UNITS, 9.0F, 18.0F),
				PartPose.offset(0.0F, 0.0F, 18.0F));
		rearBody.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(52, 70).addBox(-4.0F, -2.5F, 0.0F, 8.0F, 5.0F, 18.0F)
						.texOffs(52, 94).addBox(-11.0F, -1.0F, 15.0F, 22.0F, 2.0F, 7.0F),
				PartPose.offset(0.0F, 0.0F, 18.0F));
		rearBody.addOrReplaceChild("dorsal", CubeListBuilder.create()
						.texOffs(72, 0).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 6.0F, 7.0F),
				PartPose.offset(0.0F, -4.5F, 4.0F));
		body.addOrReplaceChild("left_fin", CubeListBuilder.create()
						.texOffs(0, 100).addBox(0.0F, 0.0F, -5.0F, 12.0F, 1.0F, 10.0F),
				PartPose.offset(8.0F, 2.0F, -8.0F));
		body.addOrReplaceChild("right_fin", CubeListBuilder.create()
						.texOffs(0, 100).mirror().addBox(-12.0F, 0.0F, -5.0F, 12.0F, 1.0F, 10.0F).mirror(false),
				PartPose.offset(-8.0F, 2.0F, -8.0F));

		return LayerDefinition.create(mesh, 128, 128);
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
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		this.body.render(poseStack, buffer, packedLight, packedOverlay);
	}
}

