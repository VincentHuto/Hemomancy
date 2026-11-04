package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import net.minecraft.util.Mth;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 4.1.3
// Exported for Minecraft version 1.17 with Mojang mappings


public class LeechModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modelleech"), "main");
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(5, 0).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(5, 0).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 24.0F, -2.0F));

		PartDefinition headTop = partdefinition.addOrReplaceChild("headTop", CubeListBuilder.create().texOffs(11, 11).addBox(-0.5F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 24.0F, -2.0F));

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(0, 4).addBox(-0.5F, -1.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 4).addBox(-1.5F, 0.0F, -2.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 24.0F, 0.0F));

		PartDefinition bodyTop = partdefinition.addOrReplaceChild("bodyTop", CubeListBuilder.create().texOffs(8, 11).addBox(-0.5F, -2.0F, -2.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 24.0F, 0.0F));

		PartDefinition Tail = partdefinition.addOrReplaceChild("Tail", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -0.5F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.5F, 0.5F, 0.0F, 3.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 23.5F, 1.0F));

		PartDefinition tailTop = partdefinition.addOrReplaceChild("tailTop", CubeListBuilder.create().texOffs(10, 13).addBox(-0.5F, -1.5F, 0.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 23.5F, 1.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}
	private final ModelPart Head;
	private final ModelPart headTop;
	private final ModelPart Body;
	private final ModelPart bodyTop;
	private final ModelPart Tail;

	private final ModelPart tailTop;
	public LeechModel(ModelPart root) {
		this.Head = root.getChild("Head");
		this.headTop = root.getChild("headTop");
		this.Body = root.getChild("Body");
		this.bodyTop = root.getChild("bodyTop");
		this.Tail = root.getChild("Tail");
		this.tailTop = root.getChild("tailTop");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		Head.render(poseStack, buffer, packedLight, packedOverlay);
		headTop.render(poseStack, buffer, packedLight, packedOverlay);
		Body.render(poseStack, buffer, packedLight, packedOverlay);
		bodyTop.render(poseStack, buffer, packedLight, packedOverlay);
		Tail.render(poseStack, buffer, packedLight, packedOverlay);
		tailTop.render(poseStack, buffer, packedLight, packedOverlay);
	}

	/**
	 * Three segments that are not parented, so the inchworm is built from each one: the middle arches up and the
	 * head and tail pitch toward it on the stride, and at rest the head probes side to side.
	 */
	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		for (ModelPart part : new ModelPart[] {Head, headTop, Body, bodyTop, Tail, tailTop}) part.resetPose();
		float stride = Math.min(1.0F, limbSwingAmount * 3.0F);
		float arch = Mth.abs(Mth.sin(limbSwing * 1.3F)) * stride;
		float probe = Mth.sin(ageInTicks * 0.07F) * 0.18F * (1.0F - stride);
		Body.y -= 1.1F * arch;
		bodyTop.y -= 1.1F * arch;
		Head.xRot = 0.45F * arch;
		headTop.xRot = 0.45F * arch;
		Tail.xRot = -0.45F * arch;
		tailTop.xRot = -0.45F * arch;
		Head.yRot = probe + netHeadYaw * Mth.DEG_TO_RAD * 0.4F;
		headTop.yRot = Head.yRot;
	}
}
