package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MarrowSpitterEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class MarrowSpitterModel extends EntityModel<MarrowSpitterEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("marrow_spitter"), "main");

	private final ModelPart root;
	private final ModelPart nozzle;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart rearLeg;
	private final ModelPart tubing;

	public MarrowSpitterModel(ModelPart root) {
		this.root = root.getChild("root");
		this.nozzle = this.root.getChild("nozzle");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.rearLeg = this.root.getChild("rear_leg");
		this.tubing = this.root.getChild("tubing");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-3.0F, -9.0F, -2.5F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.1F))
						.texOffs(24, 0).addBox(-2.0F, -6.5F, -3.0F, 4.0F, 5.0F, 1.0F, new CubeDeformation(0.2F))
						.texOffs(36, 0).addBox(-4.5F, -8.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(44, 0).addBox(3.5F, -8.0F, -1.0F, 1.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		root.addOrReplaceChild("nozzle", CubeListBuilder.create()
						.texOffs(0, 20).addBox(-1.5F, -1.5F, -9.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
						.texOffs(26, 20).addBox(-2.0F, -2.0F, -11.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, -5.5F, -2.5F));

		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(10, 38).addBox(-1.0F, 7.0F, -4.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, -1.0F, -1.0F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(24, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(34, 38).addBox(-1.0F, 7.0F, -4.0F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, -1.0F, -1.0F));

		root.addOrReplaceChild("rear_leg", CubeListBuilder.create()
						.texOffs(48, 38).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(58, 38).addBox(-2.0F, 6.0F, 0.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.0F));

		root.addOrReplaceChild("tubing", CubeListBuilder.create()
						.texOffs(52, 0).addBox(-4.0F, -10.0F, 1.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(52, 4).addBox(-0.5F, -10.0F, -4.5F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
						.texOffs(52, 12).addBox(-5.0F, -5.0F, 1.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(MarrowSpitterEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		this.root.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.25F;
		this.nozzle.xRot = headPitch * Mth.DEG_TO_RAD * 0.45F + Mth.sin(ageInTicks * 0.2F) * 0.025F;
		this.tubing.xScale = 1.0F + Mth.sin(ageInTicks * 0.18F) * 0.04F;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.65F * limbSwingAmount;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 0.65F * limbSwingAmount;
		this.rearLeg.xRot = Mth.sin(limbSwing * 0.6662F) * 0.35F * limbSwingAmount;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
