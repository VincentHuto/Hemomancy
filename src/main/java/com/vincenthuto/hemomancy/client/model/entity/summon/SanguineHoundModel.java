package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.SanguineHoundEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class SanguineHoundModel extends EntityModel<SanguineHoundEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "sanguine_hound"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart frontLeftLeg;
	private final ModelPart frontRightLeg;
	private final ModelPart hindLeftLeg;
	private final ModelPart hindRightLeg;
	private final ModelPart tail;
	private int color = -1;

	public SanguineHoundModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.frontLeftLeg = this.root.getChild("front_left_leg");
		this.frontRightLeg = this.root.getChild("front_right_leg");
		this.hindLeftLeg = this.root.getChild("hind_left_leg");
		this.hindRightLeg = this.root.getChild("hind_right_leg");
		this.tail = this.root.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3F, -3F, -5F, 6F, 6F, 10F)
				.texOffs(32, 0).addBox(-3F, -3.5F, -5.5F, 6F, 6F, 4F)
				.texOffs(32, 10).addBox(-0.5F, -4F, -4F, 1F, 1F, 8F),
				PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-2F, -2F, -3F, 4F, 4F, 4F)
				.texOffs(50, 12).addBox(-1.5F, 0F, -6F, 3F, 2F, 3F)
				.texOffs(16, 16).addBox(-2F, -0.5F, -6F, 4F, 1F, 4F)
				.texOffs(24, 21).addBox(-1.5F, 0.5F, -6.2F, 3F, 1F, 1F)
				.texOffs(60, 0).addBox(-2.5F, -2F, -2F, 1F, 4F, 1F)
				.texOffs(60, 0).addBox(1.5F, -2F, -2F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(0F, 14F, -5F, 0F, 0F, 0F));

		PartDefinition rightEar = head.addOrReplaceChild("right_ear", CubeListBuilder.create()
				.texOffs(40, 19).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(-1.5F, -2F, 0F, 0.2F, 0F, -0.2F));

		PartDefinition rightFang = head.addOrReplaceChild("right_fang", CubeListBuilder.create()
				.texOffs(44, 19).addBox(-0.5F, 0F, -0.5F, 1F, 1F, 1F),
				PartPose.offsetAndRotation(-1F, 1F, -5.5F, 0F, 0F, 0F));

		PartDefinition frontRightLeg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
				.texOffs(52, 0).addBox(-1F, -1F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(-2F, 17F, -3.5F, 0F, 0F, 0F));

		PartDefinition frontRightShin = frontRightLeg.addOrReplaceChild("front_right_shin", CubeListBuilder.create()
				.texOffs(16, 21).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(50, 17).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F)
				.texOffs(54, 17).addBox(-1F, 3F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3F, 0F, 0F, 0F, 0F));

		PartDefinition hindRightLeg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
				.texOffs(52, 0).addBox(-1F, -1F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(-2F, 17F, 3.5F, 0F, 0F, 0F));

		PartDefinition hindRightShin = hindRightLeg.addOrReplaceChild("hind_right_shin", CubeListBuilder.create()
				.texOffs(16, 21).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(50, 17).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F)
				.texOffs(54, 17).addBox(-1F, 3F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3F, 0F, 0F, 0F, 0F));

		PartDefinition leftEar = head.addOrReplaceChild("left_ear", CubeListBuilder.create()
				.texOffs(40, 19).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(1.5F, -2F, 0F, 0.2F, 0F, 0.2F));

		PartDefinition leftFang = head.addOrReplaceChild("left_fang", CubeListBuilder.create()
				.texOffs(44, 19).addBox(-0.5F, 0F, -0.5F, 1F, 1F, 1F),
				PartPose.offsetAndRotation(1F, 1F, -5.5F, 0F, 0F, 0F));

		PartDefinition frontLeftLeg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
				.texOffs(52, 0).addBox(-1F, -1F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(2F, 17F, -3.5F, 0F, 0F, 0F));

		PartDefinition frontLeftShin = frontLeftLeg.addOrReplaceChild("front_left_shin", CubeListBuilder.create()
				.texOffs(16, 21).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(50, 17).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F)
				.texOffs(54, 17).addBox(-1F, 3F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3F, 0F, 0F, 0F, 0F));

		PartDefinition hindLeftLeg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
				.texOffs(52, 0).addBox(-1F, -1F, -1F, 2F, 4F, 2F),
				PartPose.offsetAndRotation(2F, 17F, 3.5F, 0F, 0F, 0F));

		PartDefinition hindLeftShin = hindLeftLeg.addOrReplaceChild("hind_left_shin", CubeListBuilder.create()
				.texOffs(16, 21).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F)
				.texOffs(50, 17).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F)
				.texOffs(54, 17).addBox(-1F, 3F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 3F, 0F, 0F, 0F, 0F));

		PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(52, 6).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 5F),
				PartPose.offsetAndRotation(0F, 13F, 4.5F, -0.4F, 0F, 0F));

		PartDefinition tailTip = tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
				.texOffs(32, 19).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.25F, 0F, 0F));

		return LayerDefinition.create(mesh, 64, 32);
	}

	@Override
	public void setupAnim(SanguineHoundEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD;
		this.frontLeftLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.15F * limbSwingAmount;
		this.frontRightLeg.xRot = Mth.cos(limbSwing * 0.6662F + Mth.PI) * 1.15F * limbSwingAmount;
		this.hindLeftLeg.xRot = this.frontRightLeg.xRot;
		this.hindRightLeg.xRot = this.frontLeftLeg.xRot;
		this.tail.yRot = Mth.sin(ageInTicks * 0.12F) * 0.24F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, color == -1 ? packedColor : color);
	}

	public void setColor(int color) {
		this.color = color;
	}
}
