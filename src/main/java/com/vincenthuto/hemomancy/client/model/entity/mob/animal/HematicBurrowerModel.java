package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.HematicBurrowerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class HematicBurrowerModel extends HierarchicalModel<HematicBurrowerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("hematic_burrower"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart head;
	private final ModelPart leftForeClaw;
	private final ModelPart rightForeClaw;
	private final ModelPart leftHindClaw;
	private final ModelPart rightHindClaw;
	private final ModelPart whiskers;

	public HematicBurrowerModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.head = this.root.getChild("head");
		this.leftForeClaw = this.root.getChild("left_fore_claw");
		this.rightForeClaw = this.root.getChild("right_fore_claw");
		this.leftHindClaw = this.root.getChild("left_hind_claw");
		this.rightHindClaw = this.root.getChild("right_hind_claw");
		this.whiskers = this.head.getChild("whiskers");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition rootPart = mesh.getRoot();
		PartDefinition root = rootPart.addOrReplaceChild("root", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		root.addOrReplaceChild("body", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-4.5F, -4.0F, -6.0F, 9.0F, 5.0F, 12.0F, new CubeDeformation(0.1F))
						.texOffs(0, 18).addBox(-3.5F, -5.0F, -4.0F, 7.0F, 2.0F, 8.0F, new CubeDeformation(0.0F))
						.texOffs(38, 0).addBox(-2.5F, -5.8F, -2.0F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.0F, 1.0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 30).addBox(-3.5F, -3.0F, -5.0F, 7.0F, 4.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(24, 30).addBox(-1.5F, -1.5F, -7.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(38, 8).addBox(-0.5F, -1.0F, -8.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.5F, -5.5F));
		head.addOrReplaceChild("whiskers", CubeListBuilder.create()
						.texOffs(44, 18).addBox(-4.5F, -1.0F, -6.0F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		root.addOrReplaceChild("left_fore_claw", CubeListBuilder.create()
						.texOffs(0, 42).addBox(0.0F, -0.75F, -1.0F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(16, 42).addBox(3.5F, -1.0F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.2F, -0.7F, -4.2F));
		root.addOrReplaceChild("right_fore_claw", CubeListBuilder.create()
						.texOffs(0, 48).addBox(-5.0F, -0.75F, -1.0F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(16, 48).addBox(-5.5F, -1.0F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.2F, -0.7F, -4.2F));
		root.addOrReplaceChild("left_hind_claw", CubeListBuilder.create()
						.texOffs(32, 42).addBox(0.0F, -0.5F, -1.0F, 3.5F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(3.5F, -0.2F, 4.2F));
		root.addOrReplaceChild("right_hind_claw", CubeListBuilder.create()
						.texOffs(32, 46).addBox(-3.5F, -0.5F, -1.0F, 3.5F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-3.5F, -0.2F, 4.2F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public ModelPart root() {
		return this.root;
	}

	@Override
	public void setupAnim(HematicBurrowerEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		float scurry = Mth.clamp(limbSwingAmount * 2.0F, 0.0F, 1.0F);
		float swing = limbSwing * 1.6F;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD * 0.35F;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.25F;
		this.body.y += Mth.sin(ageInTicks * 0.25F) * 0.08F;
		this.leftForeClaw.zRot = -0.25F + Mth.sin(swing) * 0.5F * scurry;
		this.rightForeClaw.zRot = 0.25F - Mth.sin(swing) * 0.5F * scurry;
		this.leftHindClaw.zRot = Mth.sin(swing + Mth.PI) * 0.25F * scurry;
		this.rightHindClaw.zRot = -Mth.sin(swing + Mth.PI) * 0.25F * scurry;
		this.whiskers.yRot = Mth.sin(ageInTicks * 0.18F) * 0.08F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
