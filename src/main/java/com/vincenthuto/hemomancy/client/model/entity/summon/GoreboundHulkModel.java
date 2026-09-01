package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class GoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("gorebound_hulk"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftArm;
	private final ModelPart rightArm;
	private final ModelPart leftLeg;
	private final ModelPart rightLeg;
	private final ModelPart backMass;

	public GoreboundHulkModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftArm = this.root.getChild("left_arm");
		this.rightArm = this.root.getChild("right_arm");
		this.leftLeg = this.root.getChild("left_leg");
		this.rightLeg = this.root.getChild("right_leg");
		this.backMass = this.root.getChild("back_mass");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-6.5F, -12.7F, -4.3F, 13.0F, 12.8F, 8.6F, new CubeDeformation(0.45F))
						.texOffs(46, 0).addBox(-7.4F, -13.5F, -3.5F, 5.0F, 6.5F, 7.0F, new CubeDeformation(0.55F))
						.texOffs(46, 0).mirror().addBox(2.4F, -13.5F, -3.5F, 5.0F, 6.5F, 7.0F, new CubeDeformation(0.55F))
						.texOffs(78, 0).addBox(-4.2F, -15.3F, -2.8F, 8.4F, 4.0F, 6.2F, new CubeDeformation(0.35F))
						.texOffs(0, 24).addBox(-5.5F, -10.5F, -4.9F, 11.0F, 1.2F, 1.0F, new CubeDeformation(0.03F))
						.texOffs(26, 24).addBox(-4.8F, -6.6F, -4.85F, 9.6F, 1.1F, 0.9F, new CubeDeformation(0.03F))
						.texOffs(50, 24).addBox(-0.8F, -11.8F, -5.0F, 1.6F, 9.7F, 0.8F, new CubeDeformation(0.0F))
						.texOffs(58, 24).addBox(-5.8F, -3.5F, -4.8F, 11.6F, 2.0F, 1.0F, new CubeDeformation(0.05F))
						.texOffs(86, 24).addBox(-2.1F, -9.0F, -5.25F, 4.2F, 2.5F, 0.7F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 32).addBox(-2.6F, -3.2F, -4.2F, 5.2F, 4.4F, 4.2F, new CubeDeformation(0.08F))
						.texOffs(22, 32).addBox(-2.15F, -2.6F, -4.8F, 4.3F, 2.0F, 1.0F, new CubeDeformation(0.03F))
						.texOffs(36, 32).addBox(-1.6F, -0.5F, -5.5F, 3.2F, 1.8F, 1.8F, new CubeDeformation(0.0F))
						.texOffs(50, 32).addBox(-2.3F, 0.7F, -4.7F, 4.6F, 1.0F, 2.2F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -11.3F, -4.0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(0, 42).addBox(-0.8F, -1.0F, -2.4F, 5.8F, 8.8F, 5.4F, new CubeDeformation(0.4F))
						.texOffs(24, 42).addBox(-1.1F, 4.6F, -2.8F, 6.2F, 2.0F, 6.2F, new CubeDeformation(0.05F))
						.texOffs(50, 42).addBox(0.0F, 1.2F, -3.0F, 4.4F, 1.0F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offset(6.2F, -11.0F, 0.0F));
		leftArm.addOrReplaceChild("forearm", CubeListBuilder.create()
						.texOffs(0, 60).addBox(-0.2F, -0.4F, -2.6F, 5.7F, 8.0F, 5.7F, new CubeDeformation(0.5F))
						.texOffs(26, 60).addBox(-0.8F, 5.1F, -3.4F, 7.2F, 5.3F, 7.0F, new CubeDeformation(0.25F))
						.texOffs(56, 60).addBox(-0.2F, 9.1F, -4.1F, 2.0F, 2.1F, 3.0F, new CubeDeformation(0.1F))
						.texOffs(56, 60).addBox(1.8F, 9.3F, -4.3F, 2.0F, 2.1F, 3.2F, new CubeDeformation(0.1F))
						.texOffs(56, 60).addBox(3.8F, 9.1F, -4.1F, 2.0F, 2.1F, 3.0F, new CubeDeformation(0.1F))
						.texOffs(72, 60).addBox(-0.5F, 2.2F, -3.25F, 6.3F, 1.0F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.06F, 0.0F, -0.06F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(0, 42).mirror().addBox(-5.0F, -1.0F, -2.4F, 5.8F, 8.8F, 5.4F, new CubeDeformation(0.4F))
						.texOffs(24, 42).mirror().addBox(-5.1F, 4.6F, -2.8F, 6.2F, 2.0F, 6.2F, new CubeDeformation(0.05F))
						.texOffs(50, 42).mirror().addBox(-4.4F, 1.2F, -3.0F, 4.4F, 1.0F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offset(-6.2F, -11.0F, 0.0F));
		rightArm.addOrReplaceChild("forearm", CubeListBuilder.create()
						.texOffs(0, 60).mirror().addBox(-5.5F, -0.4F, -2.6F, 5.7F, 8.0F, 5.7F, new CubeDeformation(0.5F))
						.texOffs(26, 60).mirror().addBox(-6.4F, 5.1F, -3.4F, 7.2F, 5.3F, 7.0F, new CubeDeformation(0.25F))
						.texOffs(56, 60).mirror().addBox(-1.8F, 9.1F, -4.1F, 2.0F, 2.1F, 3.0F, new CubeDeformation(0.1F))
						.texOffs(56, 60).mirror().addBox(-3.8F, 9.3F, -4.3F, 2.0F, 2.1F, 3.2F, new CubeDeformation(0.1F))
						.texOffs(56, 60).mirror().addBox(-5.8F, 9.1F, -4.1F, 2.0F, 2.1F, 3.0F, new CubeDeformation(0.1F))
						.texOffs(72, 60).mirror().addBox(-5.8F, 2.2F, -3.25F, 6.3F, 1.0F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 7.0F, 0.0F, -0.06F, 0.0F, 0.06F));

		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 84).addBox(-2.3F, -0.4F, -2.4F, 4.6F, 6.8F, 4.8F, new CubeDeformation(0.25F))
						.texOffs(22, 84).addBox(-2.7F, 2.2F, -2.85F, 5.4F, 1.4F, 5.7F, new CubeDeformation(0.0F))
						.texOffs(46, 84).addBox(-2.8F, 5.6F, -4.5F, 5.6F, 2.5F, 6.5F, new CubeDeformation(0.08F)),
				PartPose.offset(3.2F, -1.0F, 0.0F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(0, 84).mirror().addBox(-2.3F, -0.4F, -2.4F, 4.6F, 6.8F, 4.8F, new CubeDeformation(0.25F))
						.texOffs(22, 84).mirror().addBox(-2.7F, 2.2F, -2.85F, 5.4F, 1.4F, 5.7F, new CubeDeformation(0.0F))
						.texOffs(46, 84).mirror().addBox(-2.8F, 5.6F, -4.5F, 5.6F, 2.5F, 6.5F, new CubeDeformation(0.08F)),
				PartPose.offset(-3.2F, -1.0F, 0.0F));

		root.addOrReplaceChild("back_mass", CubeListBuilder.create()
						.texOffs(78, 32).addBox(-5.4F, -12.8F, 3.0F, 10.8F, 12.5F, 6.0F, new CubeDeformation(0.55F))
						.texOffs(78, 52).addBox(-4.2F, -15.2F, 4.0F, 8.4F, 5.0F, 5.4F, new CubeDeformation(0.45F))
						.texOffs(78, 68).addBox(-6.0F, -9.0F, 4.3F, 5.0F, 8.2F, 5.3F, new CubeDeformation(0.4F))
						.texOffs(78, 68).mirror().addBox(1.0F, -9.0F, 4.3F, 5.0F, 8.2F, 5.3F, new CubeDeformation(0.4F))
						.texOffs(78, 86).addBox(-4.8F, -11.0F, 9.0F, 9.6F, 1.2F, 0.9F, new CubeDeformation(0.0F))
						.texOffs(78, 91).addBox(-4.8F, -5.7F, 9.2F, 9.6F, 1.2F, 0.9F, new CubeDeformation(0.0F))
						.texOffs(112, 84).addBox(-0.65F, -14.5F, 9.0F, 1.3F, 9.7F, 0.8F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(GoreboundHulkEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float breath = 1.0F + Mth.sin(ageInTicks * 0.11F) * 0.025F;
		this.root.xScale = breath;
		this.root.zScale = breath;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.45F;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.35F;
		this.leftArm.xRot = Mth.cos(limbSwing * 0.45F + Mth.PI) * 0.85F * limbSwingAmount - 0.08F;
		this.rightArm.xRot = Mth.cos(limbSwing * 0.45F) * 0.85F * limbSwingAmount - 0.08F;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.45F) * 0.45F * limbSwingAmount;
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.45F + Mth.PI) * 0.45F * limbSwingAmount;
		this.backMass.xRot = Mth.sin(ageInTicks * 0.09F) * 0.025F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
