package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.GoreboundHulkEntity;
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
						.texOffs(0, 0).addBox(-6.0F, -13.0F, -4.0F, 12.0F, 13.0F, 8.0F, new CubeDeformation(0.45F))
						.texOffs(42, 0).addBox(-4.0F, -15.0F, -3.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.25F))
						.texOffs(72, 0).addBox(-5.0F, -9.0F, -4.5F, 10.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 18.0F, 0.0F));

		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 26).addBox(-3.5F, -4.0F, -5.0F, 7.0F, 5.0F, 5.0F, new CubeDeformation(0.1F))
						.texOffs(26, 26).addBox(-1.0F, -1.0F, -7.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -13.0F, -3.5F));

		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
						.texOffs(0, 42).addBox(-1.0F, -1.0F, -2.0F, 5.0F, 15.0F, 5.0F, new CubeDeformation(0.35F))
						.texOffs(22, 42).addBox(0.0F, 12.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)),
				PartPose.offset(6.0F, -11.0F, 0.0F));

		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
						.texOffs(50, 42).addBox(-4.0F, -1.0F, -2.0F, 5.0F, 15.0F, 5.0F, new CubeDeformation(0.35F))
						.texOffs(72, 42).addBox(-6.0F, 12.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)),
				PartPose.offset(-6.0F, -11.0F, 0.0F));

		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
						.texOffs(0, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.2F))
						.texOffs(18, 70).addBox(-2.5F, 6.0F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.2F, -1.0F, 0.0F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
						.texOffs(42, 70).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.2F))
						.texOffs(60, 70).addBox(-2.5F, 6.0F, -4.0F, 5.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.2F, -1.0F, 0.0F));

		root.addOrReplaceChild("back_mass", CubeListBuilder.create()
						.texOffs(84, 0).addBox(-4.5F, -10.0F, 3.0F, 9.0F, 11.0F, 5.0F, new CubeDeformation(0.35F))
						.texOffs(84, 18).addBox(-2.5F, -13.0F, 5.0F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.2F)),
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
