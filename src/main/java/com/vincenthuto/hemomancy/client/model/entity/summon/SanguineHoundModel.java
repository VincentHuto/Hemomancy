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

public class SanguineHoundModel extends EntityModel<SanguineHoundEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("sanguine_hound"), "main");

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
		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.ZERO);

		root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-5.0F, -4.0F, -7.0F, 10.0F, 8.0F, 14.0F, new CubeDeformation(0.35F))
				.texOffs(0, 0).addBox(-5.8F, -4.8F, -7.2F, 11.6F, 9.6F, 7.0F, new CubeDeformation(0.45F))
				.texOffs(0, 0).addBox(-4.7F, -3.7F, 2.0F, 9.4F, 7.4F, 7.2F, new CubeDeformation(0.3F))
				.texOffs(32, 0).addBox(-5.3F, -3.2F, -5.8F, 10.6F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(32, 4).addBox(-5.1F, -3.0F, -1.7F, 10.2F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(32, 8).addBox(-4.8F, -2.8F, 2.5F, 9.6F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(36, 12).addBox(-0.65F, -5.0F, -5.0F, 1.3F, 2.0F, 12.5F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-5.5F, 2.4F, -4.4F, 11.0F, 1.2F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 15.5F, 1.5F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.7F, -3.5F, -4.6F, 7.4F, 6.5F, 6.3F, new CubeDeformation(0.25F))
				.texOffs(0, 16).addBox(-3.2F, -1.5F, -8.3F, 6.4F, 4.0F, 4.5F, new CubeDeformation(0.12F))
				.texOffs(24, 16).addBox(-3.5F, -2.7F, -8.7F, 7.0F, 1.0F, 5.2F, new CubeDeformation(0.03F))
				.texOffs(24, 20).addBox(-3.55F, 1.6F, -8.1F, 7.1F, 0.9F, 4.2F, new CubeDeformation(0.03F))
				.texOffs(44, 16).addBox(-3.8F, -3.3F, -3.9F, 1.2F, 5.6F, 2.0F, new CubeDeformation(0.02F))
				.texOffs(44, 16).mirror().addBox(2.6F, -3.3F, -3.9F, 1.2F, 5.6F, 2.0F, new CubeDeformation(0.02F))
				.texOffs(52, 0).addBox(-3.3F, -5.8F, -1.8F, 2.4F, 3.2F, 1.3F, new CubeDeformation(0.05F))
				.texOffs(52, 0).mirror().addBox(0.9F, -5.8F, -1.8F, 2.4F, 3.2F, 1.3F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, 14.0F, -5.7F));
		head.addOrReplaceChild("left_fang", CubeListBuilder.create()
				.texOffs(58, 8).addBox(-0.35F, -0.2F, -0.4F, 0.7F, 2.4F, 0.7F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.15F, 1.2F, -8.1F, -0.35F, 0.0F, 0.0F));
		head.addOrReplaceChild("right_fang", CubeListBuilder.create()
				.texOffs(58, 8).mirror().addBox(-0.35F, -0.2F, -0.4F, 0.7F, 2.4F, 0.7F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.15F, 1.2F, -8.1F, -0.35F, 0.0F, 0.0F));

		root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-1.7F, -1.0F, -1.7F, 3.4F, 7.0F, 3.4F, new CubeDeformation(0.2F))
				.texOffs(14, 18).addBox(-1.9F, 4.5F, -3.3F, 3.8F, 2.3F, 4.6F, new CubeDeformation(0.08F))
				.texOffs(30, 26).addBox(-1.5F, 5.6F, -4.7F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F))
				.texOffs(30, 26).addBox(0.8F, 5.6F, -4.7F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offset(3.5F, 18.0F, -3.5F));
		root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
				.texOffs(0, 18).mirror().addBox(-1.7F, -1.0F, -1.7F, 3.4F, 7.0F, 3.4F, new CubeDeformation(0.2F))
				.texOffs(14, 18).mirror().addBox(-1.9F, 4.5F, -3.3F, 3.8F, 2.3F, 4.6F, new CubeDeformation(0.08F))
				.texOffs(30, 26).mirror().addBox(-1.5F, 5.6F, -4.7F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F))
				.texOffs(30, 26).mirror().addBox(0.8F, 5.6F, -4.7F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.5F, 18.0F, -3.5F));
		root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
				.texOffs(0, 18).addBox(-1.9F, -1.2F, -1.9F, 3.8F, 7.2F, 3.8F, new CubeDeformation(0.25F))
				.texOffs(14, 18).addBox(-2.0F, 4.5F, -3.0F, 4.0F, 2.3F, 4.3F, new CubeDeformation(0.08F))
				.texOffs(30, 26).addBox(-1.55F, 5.6F, -4.4F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F))
				.texOffs(30, 26).addBox(0.85F, 5.6F, -4.4F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offset(3.3F, 18.0F, 6.0F));
		root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
				.texOffs(0, 18).mirror().addBox(-1.9F, -1.2F, -1.9F, 3.8F, 7.2F, 3.8F, new CubeDeformation(0.25F))
				.texOffs(14, 18).mirror().addBox(-2.0F, 4.5F, -3.0F, 4.0F, 2.3F, 4.3F, new CubeDeformation(0.08F))
				.texOffs(30, 26).mirror().addBox(-1.55F, 5.6F, -4.4F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F))
				.texOffs(30, 26).mirror().addBox(0.85F, 5.6F, -4.4F, 0.7F, 0.8F, 1.8F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.3F, 18.0F, 6.0F));

		PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(32, 12).addBox(-1.25F, -1.2F, 0.0F, 2.5F, 2.4F, 6.5F, new CubeDeformation(0.15F))
				.texOffs(52, 12).addBox(-1.65F, -1.55F, 2.0F, 3.3F, 3.1F, 1.4F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 13.7F, 8.2F, -0.55F, 0.0F, 0.0F));
		tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
				.texOffs(32, 12).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 5.0F, new CubeDeformation(0.1F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 5.8F, -0.2F, 0.0F, 0.0F));

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
