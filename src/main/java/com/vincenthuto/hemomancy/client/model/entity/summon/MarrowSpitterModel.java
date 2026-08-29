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
						.texOffs(0, 0).addBox(-3.2F, -10.5F, -2.7F, 6.4F, 11.0F, 5.4F, new CubeDeformation(0.15F))
						.texOffs(26, 0).addBox(-2.7F, -12.0F, -2.2F, 5.4F, 1.8F, 4.4F, new CubeDeformation(0.1F))
						.texOffs(48, 0).addBox(-3.7F, -11.4F, -1.2F, 7.4F, 1.0F, 2.4F, new CubeDeformation(0.0F))
						.texOffs(48, 5).addBox(-3.8F, -7.8F, -1.25F, 7.6F, 1.1F, 2.5F, new CubeDeformation(0.0F))
						.texOffs(48, 10).addBox(-3.8F, -3.9F, -1.25F, 7.6F, 1.1F, 2.5F, new CubeDeformation(0.0F))
						.texOffs(48, 15).addBox(-3.5F, -0.7F, -2.4F, 7.0F, 1.5F, 4.8F, new CubeDeformation(0.05F))
						.texOffs(76, 0).addBox(-4.45F, -10.2F, -1.0F, 1.2F, 9.7F, 2.0F, new CubeDeformation(0.03F))
						.texOffs(76, 0).mirror().addBox(3.25F, -10.2F, -1.0F, 1.2F, 9.7F, 2.0F, new CubeDeformation(0.03F))
						.texOffs(84, 0).addBox(-2.0F, -9.2F, -3.15F, 4.0F, 6.4F, 0.8F, new CubeDeformation(0.08F))
						.texOffs(102, 0).addBox(-0.65F, -11.8F, 2.2F, 1.3F, 11.5F, 1.2F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		PartDefinition nozzle = root.addOrReplaceChild("nozzle", CubeListBuilder.create()
						.texOffs(0, 24).addBox(-2.4F, -2.3F, -2.2F, 4.8F, 4.6F, 3.2F, new CubeDeformation(0.1F))
						.texOffs(20, 24).addBox(-1.65F, -1.65F, -8.6F, 3.3F, 3.3F, 6.8F, new CubeDeformation(0.03F))
						.texOffs(44, 24).addBox(-2.15F, -2.1F, -5.0F, 4.3F, 4.2F, 1.3F, new CubeDeformation(0.0F))
						.texOffs(58, 24).addBox(-2.25F, -2.2F, -9.4F, 4.5F, 4.4F, 1.2F, new CubeDeformation(0.05F))
						.texOffs(74, 24).addBox(-2.8F, -0.45F, -7.8F, 5.6F, 0.9F, 1.6F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -5.5F, -2.5F));
		nozzle.addOrReplaceChild("upper_muzzle", CubeListBuilder.create()
						.texOffs(0, 36).addBox(-1.8F, -0.9F, -3.8F, 3.6F, 1.4F, 4.0F, new CubeDeformation(0.02F))
						.texOffs(16, 36).addBox(-2.2F, -1.25F, -1.0F, 4.4F, 0.8F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -0.65F, -9.0F, -0.22F, 0.0F, 0.0F));
		nozzle.addOrReplaceChild("lower_muzzle", CubeListBuilder.create()
						.texOffs(0, 42).addBox(-1.8F, -0.5F, -3.8F, 3.6F, 1.4F, 4.0F, new CubeDeformation(0.02F))
						.texOffs(16, 42).addBox(-2.2F, 0.45F, -1.0F, 4.4F, 0.8F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.65F, -9.0F, 0.22F, 0.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(36, 38).addBox(-1.0F, -0.6F, -1.0F, 2.0F, 4.8F, 2.0F, new CubeDeformation(0.05F))
						.texOffs(46, 38).addBox(-1.45F, 2.8F, -1.45F, 2.9F, 1.4F, 2.9F, new CubeDeformation(0.0F)),
				PartPose.offset(3.0F, -1.0F, -1.0F));
		leftLeg.addOrReplaceChild("stilt", CubeListBuilder.create()
						.texOffs(60, 38).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 5.2F, 1.4F, new CubeDeformation(0.0F))
						.texOffs(68, 38).addBox(-1.0F, 4.5F, -3.7F, 2.0F, 0.9F, 4.2F, new CubeDeformation(0.0F))
						.texOffs(84, 38).addBox(-0.35F, 4.7F, -5.2F, 0.7F, 0.7F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 3.7F, 0.0F, 0.08F, 0.0F, 0.05F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(36, 38).mirror().addBox(-1.0F, -0.6F, -1.0F, 2.0F, 4.8F, 2.0F, new CubeDeformation(0.05F))
						.texOffs(46, 38).mirror().addBox(-1.45F, 2.8F, -1.45F, 2.9F, 1.4F, 2.9F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.0F, -1.0F, -1.0F));
		rightLeg.addOrReplaceChild("stilt", CubeListBuilder.create()
						.texOffs(60, 38).mirror().addBox(-0.7F, 0.0F, -0.7F, 1.4F, 5.2F, 1.4F, new CubeDeformation(0.0F))
						.texOffs(68, 38).mirror().addBox(-1.0F, 4.5F, -3.7F, 2.0F, 0.9F, 4.2F, new CubeDeformation(0.0F))
						.texOffs(84, 38).mirror().addBox(-0.35F, 4.7F, -5.2F, 0.7F, 0.7F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 3.7F, 0.0F, 0.08F, 0.0F, -0.05F));

		PartDefinition rearLeg = root.addOrReplaceChild("rear_leg", CubeListBuilder.create()
						.texOffs(92, 38).addBox(-1.1F, -0.4F, -1.1F, 2.2F, 4.5F, 2.2F, new CubeDeformation(0.05F))
						.texOffs(102, 38).addBox(-1.55F, 2.7F, -1.55F, 3.1F, 1.4F, 3.1F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 3.0F));
		rearLeg.addOrReplaceChild("stilt", CubeListBuilder.create()
						.texOffs(92, 48).addBox(-0.7F, 0.0F, -0.7F, 1.4F, 4.8F, 1.4F, new CubeDeformation(0.0F))
						.texOffs(100, 48).addBox(-2.0F, 4.1F, -0.5F, 4.0F, 0.9F, 3.8F, new CubeDeformation(0.0F))
						.texOffs(118, 48).addBox(-0.35F, 4.3F, 2.8F, 0.7F, 0.7F, 1.7F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 3.7F, 0.0F, -0.1F, 0.0F, 0.0F));

		root.addOrReplaceChild("tubing", CubeListBuilder.create()
						.texOffs(0, 52).addBox(-4.3F, -11.2F, 2.2F, 8.6F, 0.8F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(0, 56).addBox(-4.7F, -10.8F, 1.9F, 0.8F, 7.8F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(0, 56).mirror().addBox(3.9F, -10.8F, 1.9F, 0.8F, 7.8F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(10, 56).addBox(-4.3F, -3.4F, 1.9F, 8.6F, 0.8F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(30, 52).addBox(-0.4F, -11.0F, -5.7F, 0.8F, 0.8F, 8.2F, new CubeDeformation(0.0F))
						.texOffs(52, 52).addBox(-4.15F, -7.0F, -3.7F, 0.8F, 0.8F, 5.9F, new CubeDeformation(0.0F))
						.texOffs(52, 52).mirror().addBox(3.35F, -7.0F, -3.7F, 0.8F, 0.8F, 5.9F, new CubeDeformation(0.0F)),
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
