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


public class ModelDrudge<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Hemomancy.MOD_ID, "modeldrudgenew"), "main");
	private final ModelPart tent;
	private final ModelPart tent2;
	private final ModelPart tent3;
	private final ModelPart tent4;
	private final ModelPart tent5;
	private final ModelPart tent6;
	private final ModelPart tent7;
	private final ModelPart tent8;
	private final ModelPart brain;

	public ModelDrudge(ModelPart root) {
		this.tent = root.getChild("tent");
		this.tent2 = root.getChild("tent2");
		this.tent3 = root.getChild("tent3");
		this.tent4 = root.getChild("tent4");
		this.tent5 = root.getChild("tent5");
		this.tent6 = root.getChild("tent6");
		this.tent7 = root.getChild("tent7");
		this.tent8 = root.getChild("tent8");
		this.brain = root.getChild("brain");
	}
	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition tent = partdefinition.addOrReplaceChild("tent", CubeListBuilder.create().texOffs(0, 20).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.0F, 5.0F, 2.25F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(19, 16).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(19, 14).addBox(-1.0F, -1.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.1F, 17.0F, -2.0F));

		PartDefinition tent2 = partdefinition.addOrReplaceChild("tent2", CubeListBuilder.create().texOffs(19, 19).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 18).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 18).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 16.0F, -2.5F));

		PartDefinition tent3 = partdefinition.addOrReplaceChild("tent3", CubeListBuilder.create().texOffs(6, 6).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(19, 18).addBox(-1.0F, 6.0F, 1.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(2, 18).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 18).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 17.25F, 1.0F));

		PartDefinition tent4 = partdefinition.addOrReplaceChild("tent4", CubeListBuilder.create().texOffs(2, 20).addBox(-1.0F, 5.0F, 1.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 1).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 3).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 16).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.1F, 17.0F, -1.0F));

		PartDefinition tent5 = partdefinition.addOrReplaceChild("tent5", CubeListBuilder.create().texOffs(2, 16).addBox(-1.0F, 3.0F, 2.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(2, 16).addBox(-1.0F, 5.0F, 2.3F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 16).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 14).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 16.0F, -2.25F));

		PartDefinition tent6 = partdefinition.addOrReplaceChild("tent6", CubeListBuilder.create().texOffs(4, 14).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(2, 14).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 14).addBox(-1.0F, -1.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 16.25F, -0.25F));

		PartDefinition tent7 = partdefinition.addOrReplaceChild("tent7", CubeListBuilder.create().texOffs(8, 7).addBox(-1.0F, 3.0F, 1.75F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 5).addBox(-1.0F, 5.0F, 2.25F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 5).addBox(-1.0F, 1.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(8, 3).addBox(-1.0F, -1.0F, 0.75F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 17.0F, 1.0F));

		PartDefinition tent8 = partdefinition.addOrReplaceChild("tent8", CubeListBuilder.create().texOffs(8, 1).addBox(-1.0F, 3.0F, 1.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 7).addBox(-1.0F, 5.0F, 1.75F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 7).addBox(-1.0F, 1.0F, 0.5F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(2, 7).addBox(-1.0F, -1.0F, 0.0F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.5F, 16.0F, 0.5F));

		PartDefinition brain = partdefinition.addOrReplaceChild("brain", CubeListBuilder.create().texOffs(19, 15).addBox(-3.5F, 0.0F, -3.5F, 5.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(22, 0).addBox(-2.5F, 1.0F, 0.55F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.5F, 0.0F, 2.7F, 1.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-1.5F, 6.0F, 3.9F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(4, 0).addBox(-2.0F, 1.0F, -0.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-3.95F, -4.9F, -5.25F, 6.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(11, 25).addBox(-3.45F, -4.0F, -5.75F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(30, 24).addBox(-3.45F, -3.5F, 4.25F, 5.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 28).addBox(1.55F, -4.0F, -4.75F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(19, 24).addBox(-4.45F, -4.0F, -4.75F, 1.0F, 4.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-3.45F, -5.5F, -4.75F, 5.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 15.0F, -1.0F));

		PartDefinition device = brain.addOrReplaceChild("device", CubeListBuilder.create().texOffs(1, 43).addBox(-3.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 42).addBox(-5.5F, -1.25F, -1.5F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(3, 44).addBox(-5.0F, -1.0F, 1.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(1, 43).addBox(-6.0F, -1.0F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(3, 44).addBox(-5.0F, -1.0F, -2.0F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.05F, -5.0F, -1.25F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		tent.render(poseStack, buffer, packedLight, packedOverlay);
		tent2.render(poseStack, buffer, packedLight, packedOverlay);
		tent3.render(poseStack, buffer, packedLight, packedOverlay);
		tent4.render(poseStack, buffer, packedLight, packedOverlay);
		tent5.render(poseStack, buffer, packedLight, packedOverlay);
		tent6.render(poseStack, buffer, packedLight, packedOverlay);
		tent7.render(poseStack, buffer, packedLight, packedOverlay);
		tent8.render(poseStack, buffer, packedLight, packedOverlay);
		brain.render(poseStack, buffer, packedLight, packedOverlay);
	}
}