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
import net.minecraft.resources.ResourceLocation;

public class GoreboundHulkModel extends EntityModel<GoreboundHulkEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "gorebound_hulk"), "main");

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
				.texOffs(0, 0).addBox(-6F, -12F, -3.5F, 12F, 10F, 7F)
				.texOffs(38, 13).addBox(-4F, -3F, -3F, 8F, 3F, 6F),
				PartPose.offsetAndRotation(0F, 18F, 0F, 0F, 0F, 0F));

		PartDefinition yoke = root.addOrReplaceChild("yoke", CubeListBuilder.create()
				.texOffs(22, 22).addBox(-7F, -1F, -3F, 14F, 2F, 6F)
				.texOffs(110, 0).addBox(-1F, -1.5F, -3.5F, 2F, 3F, 7F),
				PartPose.offsetAndRotation(0F, -12F, 0F, 0F, 0F, 0F));

		PartDefinition rightChestStrap = root.addOrReplaceChild("right_chest_strap", CubeListBuilder.create()
				.texOffs(88, 0).addBox(-0.5F, 0F, 0F, 1F, 10F, 1F),
				PartPose.offsetAndRotation(-3F, -11F, -3.7F, 0F, 0F, 0.17F));

		PartDefinition leftChestStrap = root.addOrReplaceChild("left_chest_strap", CubeListBuilder.create()
				.texOffs(88, 0).addBox(-0.5F, 0F, 0F, 1F, 10F, 1F),
				PartPose.offsetAndRotation(3F, -11F, -3.7F, 0F, 0F, -0.17F));

		PartDefinition waistBinding = root.addOrReplaceChild("waist_binding", CubeListBuilder.create()
				.texOffs(20, 30).addBox(-6F, 0F, 0F, 12F, 2F, 1F)
				.texOffs(120, 10).addBox(-1F, -0.5F, -0.3F, 2F, 3F, 1F),
				PartPose.offsetAndRotation(0F, -3F, -3.6F, 0F, 0F, 0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(86, 22).addBox(-2F, -2F, -3F, 4F, 4F, 3F)
				.texOffs(120, 14).addBox(-1.5F, -1.5F, -3.5F, 3F, 3F, 1F)
				.texOffs(22, 17).addBox(-2F, -2F, -3.5F, 4F, 1F, 1F),
				PartPose.offsetAndRotation(0F, -10F, -3.5F, 0F, 0F, 0F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(68, 0).addBox(-5F, -1F, -2.5F, 5F, 7F, 5F)
				.texOffs(100, 22).addBox(-5.5F, 3F, -3F, 6F, 1F, 6F),
				PartPose.offsetAndRotation(-6F, -10F, 0F, 0F, 0F, 0.06F));

		PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-4.5F, 0F, -2.5F, 5F, 2F, 5F)
				.texOffs(92, 0).addBox(-4F, 1F, -2.5F, 4F, 6F, 5F)
				.texOffs(88, 11).addBox(-6F, 5F, -3.5F, 7F, 4F, 7F),
				PartPose.offsetAndRotation(-1F, 5.5F, 0F, 0.08F, 0F, -0.08F));

		PartDefinition rightKnuckle_0 = rightForearm.addOrReplaceChild("right_knuckle_0", CubeListBuilder.create()
				.texOffs(84, 12).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(-0.5F, 7F, -3.5F, 0F, 0F, 0F));

		PartDefinition rightKnuckle_1 = rightForearm.addOrReplaceChild("right_knuckle_1", CubeListBuilder.create()
				.texOffs(84, 12).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(-2.5F, 7F, -3.5F, 0F, 0F, 0F));

		PartDefinition rightKnuckle_2 = rightForearm.addOrReplaceChild("right_knuckle_2", CubeListBuilder.create()
				.texOffs(84, 12).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(-4.5F, 7F, -3.5F, 0F, 0F, 0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(68, 12).addBox(-2F, 0F, -2F, 4F, 5F, 4F)
				.texOffs(86, 29).addBox(-2.5F, 1F, -2.5F, 5F, 1F, 5F)
				.texOffs(0, 17).addBox(-2.5F, 4F, -3.5F, 5F, 3F, 6F),
				PartPose.offsetAndRotation(-3F, -1F, 0F, 0F, 0F, 0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(68, 0).addBox(0F, -1F, -2.5F, 5F, 7F, 5F)
				.texOffs(100, 22).addBox(-0.5F, 3F, -3F, 6F, 1F, 6F),
				PartPose.offsetAndRotation(6F, -10F, 0F, 0F, 0F, -0.06F));

		PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
				.texOffs(0, 26).addBox(-0.5F, 0F, -2.5F, 5F, 2F, 5F)
				.texOffs(92, 0).addBox(0F, 1F, -2.5F, 4F, 6F, 5F)
				.texOffs(88, 11).addBox(-1F, 5F, -3.5F, 7F, 4F, 7F),
				PartPose.offsetAndRotation(1F, 5.5F, 0F, 0.08F, 0F, 0.08F));

		PartDefinition leftKnuckle_0 = leftForearm.addOrReplaceChild("left_knuckle_0", CubeListBuilder.create()
				.texOffs(84, 12).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(0.5F, 7F, -3.5F, 0F, 0F, 0F));

		PartDefinition leftKnuckle_1 = leftForearm.addOrReplaceChild("left_knuckle_1", CubeListBuilder.create()
				.texOffs(84, 12).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(2.5F, 7F, -3.5F, 0F, 0F, 0F));

		PartDefinition leftKnuckle_2 = leftForearm.addOrReplaceChild("left_knuckle_2", CubeListBuilder.create()
				.texOffs(84, 12).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(4.5F, 7F, -3.5F, 0F, 0F, 0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(68, 12).addBox(-2F, 0F, -2F, 4F, 5F, 4F)
				.texOffs(86, 29).addBox(-2.5F, 1F, -2.5F, 5F, 1F, 5F)
				.texOffs(0, 17).addBox(-2.5F, 4F, -3.5F, 5F, 3F, 6F),
				PartPose.offsetAndRotation(3F, -1F, 0F, 0F, 0F, 0F));

		PartDefinition backMass = root.addOrReplaceChild("back_mass", CubeListBuilder.create()
				.texOffs(38, 0).addBox(-5F, 0F, 0F, 10F, 8F, 5F)
				.texOffs(62, 22).addBox(-4F, -3F, 0F, 8F, 4F, 4F)
				.texOffs(46, 30).addBox(-5.5F, 1F, 4.4F, 11F, 1F, 1F)
				.texOffs(46, 30).addBox(-5.5F, 5F, 4.4F, 11F, 1F, 1F)
				.texOffs(116, 10).addBox(-0.5F, -2F, 4.4F, 1F, 9F, 1F),
				PartPose.offsetAndRotation(0F, -10F, 3F, 0F, 0F, 0F));

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
