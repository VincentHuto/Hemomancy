package com.vincenthuto.hemomancy.client.model.entity.mob.aquatic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.aquatic.MnemonicWhaleEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class MnemonicWhaleModel extends EntityModel<MnemonicWhaleEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("mnemonic_whale"), "main");

	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart dorsal;
	private final ModelPart leftFin;
	private final ModelPart rightFin;

	public MnemonicWhaleModel(ModelPart root) {
		this.body = root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.dorsal = this.body.getChild("dorsal");
		this.leftFin = this.body.getChild("left_fin");
		this.rightFin = this.body.getChild("right_fin");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-8.0F, -5.0F, -16.0F, 16.0F, 10.0F, 30.0F),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		body.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(0, 40).addBox(-4.0F, -3.0F, 0.0F, 8.0F, 6.0F, 14.0F)
						.texOffs(44, 40).addBox(-9.0F, -1.0F, 12.0F, 18.0F, 2.0F, 6.0F),
				PartPose.offset(0.0F, 0.0F, 14.0F));
		body.addOrReplaceChild("dorsal", CubeListBuilder.create()
						.texOffs(64, 0).addBox(-1.0F, -6.0F, -2.0F, 2.0F, 6.0F, 6.0F),
				PartPose.offset(0.0F, -5.0F, -2.0F));
		body.addOrReplaceChild("left_fin", CubeListBuilder.create()
						.texOffs(0, 60).addBox(0.0F, 0.0F, -4.0F, 10.0F, 1.0F, 8.0F),
				PartPose.offset(8.0F, 2.0F, -6.0F));
		body.addOrReplaceChild("right_fin", CubeListBuilder.create()
						.texOffs(0, 60).mirror().addBox(-10.0F, 0.0F, -4.0F, 10.0F, 1.0F, 8.0F).mirror(false),
				PartPose.offset(-8.0F, 2.0F, -6.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(MnemonicWhaleEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		float swim = Mth.sin(ageInTicks * 0.08F) * 0.2F;
		this.body.yRot = swim * 0.25F;
		this.tail.yRot = swim * 0.9F;
		this.dorsal.zRot = swim * 0.05F;
		this.leftFin.zRot = -0.2F + Mth.sin(ageInTicks * 0.06F) * 0.08F;
		this.rightFin.zRot = 0.2F - Mth.sin(ageInTicks * 0.06F) * 0.08F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		this.body.render(poseStack, buffer, packedLight, packedOverlay);
	}
}

