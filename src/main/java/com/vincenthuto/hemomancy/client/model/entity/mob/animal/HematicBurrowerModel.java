package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.HematicBurrowerEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class HematicBurrowerModel extends HierarchicalModel<HematicBurrowerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("hematic_burrower"), "main");

	private final ModelPart root;
	private final ModelPart body;
	private final ModelPart tail;
	private final ModelPart head;
	private final ModelPart whiskers;
	private final ModelPart left_fore_claw;
	private final ModelPart right_fore_claw;
	private final ModelPart left_hind_claw;
	private final ModelPart right_hind_claw;


	public HematicBurrowerModel(ModelPart root) {
		this.root = root.getChild("root");
		this.body = this.root.getChild("body");
		this.tail = this.body.getChild("tail");
		this.head = this.root.getChild("head");
		this.whiskers = this.head.getChild("whiskers");
		this.left_fore_claw = this.root.getChild("left_fore_claw");
		this.right_fore_claw = this.root.getChild("right_fore_claw");
		this.left_hind_claw = this.root.getChild("left_hind_claw");
		this.right_hind_claw = this.root.getChild("right_hind_claw");

	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition root = partdefinition.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -2.0F, -6.0F, 9.0F, 3.0F, 12.0F, new CubeDeformation(0.1F))
				.texOffs(0, 16).addBox(-3.5F, -3.0F, -4.0F, 7.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 1.0F));

		PartDefinition tail = body.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(19, 3).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 6.0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 26).addBox(-3.5F, -1.0F, -5.0F, 7.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, -4.5F));

		PartDefinition whiskers = head.addOrReplaceChild("whiskers", CubeListBuilder.create().texOffs(25, 26).addBox(-4.5F, 0.0F, -6.0F, 9.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition left_fore_claw = root.addOrReplaceChild("left_fore_claw", CubeListBuilder.create().texOffs(0, 34).addBox(-5.0F, -0.75F, -1.0F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(31, 16).addBox(-8.5F, 0.0F, -2.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.2F, -0.7F, -4.2F));

		PartDefinition right_fore_claw = root.addOrReplaceChild("right_fore_claw", CubeListBuilder.create().texOffs(17, 37).addBox(0.0F, -0.75F, -1.0F, 5.0F, 1.5F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(25, 31).addBox(3.5F, 0.0F, -2.0F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.2F, -0.7F, -4.2F));

		PartDefinition left_hind_claw = root.addOrReplaceChild("left_hind_claw", CubeListBuilder.create().texOffs(31, 22).addBox(-3.5F, 0.25F, 0.0F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -0.2F, 4.2F));

		PartDefinition right_hind_claw = root.addOrReplaceChild("right_hind_claw", CubeListBuilder.create().texOffs(33, 37).addBox(-0.5F, 0.25F, 0.0F, 4.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -0.2F, 4.2F));

		return LayerDefinition.create(meshdefinition, 64, 64);
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
		this.left_fore_claw.zRot = -0.25F + Mth.sin(swing) * 0.5F * scurry;
		this.right_fore_claw.zRot = 0.25F - Mth.sin(swing) * 0.5F * scurry;
		this.left_hind_claw.zRot = Mth.sin(swing + Mth.PI) * 0.25F * scurry;
		this.right_hind_claw.zRot = -Mth.sin(swing + Mth.PI) * 0.25F * scurry;
		this.whiskers.yRot = Mth.sin(ageInTicks * 0.18F) * 0.08F;

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			int packedColor) {
		this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
