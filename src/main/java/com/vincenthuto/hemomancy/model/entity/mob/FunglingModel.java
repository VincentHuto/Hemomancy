package com.vincenthuto.hemomancy.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

// Made with Blockbench 4.1.1
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports


public class FunglingModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modelfungling"), "main");
	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition full = partdefinition.addOrReplaceChild("full", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, 0.0F));

		PartDefinition core = full.addOrReplaceChild("core", CubeListBuilder.create(), PartPose.offset(-1.0F, -6.0F, -1.0F));

		PartDefinition base = core.addOrReplaceChild("base", CubeListBuilder.create().texOffs(16, 16).addBox(-2.0F, -1.0F, -2.0F, 4.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(13, 21).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 6.0F, 2.0F));

		PartDefinition cap = core.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(0, 23).addBox(1.0F, -9.3F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(23, 23).addBox(1.0F, -9.3F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -9.3F, 1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(-3.0F, -9.3F, -3.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -14.3F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.0F, -13.3F, 4.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(11, 6).addBox(-3.0F, -13.3F, -5.0F, 6.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 8).addBox(-3.0F, -15.3F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(11, 2).addBox(4.0F, -13.3F, -3.0F, 1.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(14, 2).addBox(-5.0F, -13.3F, -3.0F, 1.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 5.05F, 2.0F));

		PartDefinition mid = core.addOrReplaceChild("mid", CubeListBuilder.create().texOffs(8, 25).addBox(-1.5F, -2.1667F, -0.75F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-2.0F, -6.1667F, -1.25F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 15).addBox(-1.5F, -5.6667F, -2.0F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 5.1667F, 1.25F));

		PartDefinition eye = mid.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(16, 25).addBox(-1.0F, -1.0F, -0.475F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-0.5F, -0.5F, -0.525F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.1667F, -1.825F));

		PartDefinition tent1 = full.addOrReplaceChild("tent1", CubeListBuilder.create().texOffs(29, 21).addBox(0.75F, -1.25F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.75F, 1.25F, 0.0F));

		PartDefinition tent11 = tent1.addOrReplaceChild("tent11", CubeListBuilder.create().texOffs(28, 29).addBox(0.0F, -0.5F, -0.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.75F, -0.25F, -0.75F));

		PartDefinition tent12 = tent11.addOrReplaceChild("tent12", CubeListBuilder.create().texOffs(28, 18).addBox(0.0F, -0.25F, -0.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.25F, -0.5F));

		PartDefinition tent13 = tent12.addOrReplaceChild("tent13", CubeListBuilder.create().texOffs(7, 30).addBox(0.0F, -0.25F, -0.25F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.0F, 0.5F, 0.0F));

		PartDefinition tent2 = full.addOrReplaceChild("tent2", CubeListBuilder.create().texOffs(24, 29).addBox(-1.75F, -1.25F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.75F, 1.25F, 2.5F));

		PartDefinition tent21 = tent2.addOrReplaceChild("tent21", CubeListBuilder.create().texOffs(24, 6).addBox(-2.0F, -0.5F, -0.25F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.75F, -0.25F, 0.5F));

		PartDefinition tent22 = tent21.addOrReplaceChild("tent22", CubeListBuilder.create().texOffs(6, 23).addBox(-2.0F, -0.25F, -0.25F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.0F, 0.25F, -0.5F));

		PartDefinition tent23 = tent22.addOrReplaceChild("tent23", CubeListBuilder.create().texOffs(22, 29).addBox(-0.8F, -0.25F, -0.75F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.1F, 0.5F, 1.0F));

		PartDefinition tent3 = full.addOrReplaceChild("tent3", CubeListBuilder.create().texOffs(22, 21).addBox(-1.125F, -0.75F, 0.125F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.375F, 0.75F, 2.625F));

		PartDefinition tent31 = tent3.addOrReplaceChild("tent31", CubeListBuilder.create().texOffs(18, 29).addBox(-0.25F, -0.5F, 0.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.625F, 0.25F, 0.875F));

		PartDefinition tent32 = tent31.addOrReplaceChild("tent32", CubeListBuilder.create().texOffs(0, 29).addBox(0.125F, -0.75F, 0.125F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.125F, 0.75F, 2.125F));

		PartDefinition tent33 = tent32.addOrReplaceChild("tent33", CubeListBuilder.create().texOffs(4, 29).addBox(0.0F, -0.5F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.125F, 0.25F, 2.125F));

		PartDefinition tent4 = full.addOrReplaceChild("tent4", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -0.25F, -1.25F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.25F, 0.25F, -0.5F));

		PartDefinition tent41 = tent4.addOrReplaceChild("tent41", CubeListBuilder.create().texOffs(28, 15).addBox(-0.75F, -0.5F, -2.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.25F, 0.75F, -1.25F));

		PartDefinition tent42 = tent41.addOrReplaceChild("tent42", CubeListBuilder.create().texOffs(14, 28).addBox(-0.875F, -0.75F, -1.875F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.375F, 0.75F, -2.125F));

		PartDefinition tent43 = tent42.addOrReplaceChild("tent43", CubeListBuilder.create().texOffs(0, 17).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.875F, 0.25F, -2.375F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	private final ModelPart full;
	public FunglingModel(ModelPart root) {
		this.full = root.getChild("full");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		full.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}
}