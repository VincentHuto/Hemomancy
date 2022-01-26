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


public class ModelChitinite<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Hemomancy.MOD_ID, "modelchitinite"), "main");
	private final ModelPart whole;

	public ModelChitinite(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition shell = whole.addOrReplaceChild("shell", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition plate4 = shell.addOrReplaceChild("plate4", CubeListBuilder.create().texOffs(21, 12).addBox(-2.5F, 0.849F, -0.3726F, 5.0F, 2.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 5).addBox(-4.0F, -0.3232F, -1.0217F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 27).addBox(-2.0F, 0.7951F, -0.1097F, 4.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(24, 30).addBox(3.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 30).addBox(-4.25F, 0.0673F, -0.9351F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.7637F, 2.3841F, -0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg4 = plate4.addOrReplaceChild("rLeg4", CubeListBuilder.create().texOffs(2, 2).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 5).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.375F, 1.2069F, 2.4821F, 0.2182F, 0.0F, 0.0F));

		PartDefinition rLeg = plate4.addOrReplaceChild("rLeg", CubeListBuilder.create().texOffs(0, 12).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(16, 16).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.875F, 1.5309F, 0.7202F, 0.2182F, 0.0F, 0.0F));

		PartDefinition lLeg5 = plate4.addOrReplaceChild("lLeg5", CubeListBuilder.create().texOffs(2, 12).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 0).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.625F, 1.2069F, 2.4821F, 0.2182F, 0.0F, 0.0F));

		PartDefinition lLeg4 = plate4.addOrReplaceChild("lLeg4", CubeListBuilder.create().texOffs(16, 14).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 6).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.125F, 1.5797F, 0.731F, 0.2182F, 0.0F, 0.0F));

		PartDefinition plate5 = plate4.addOrReplaceChild("plate5", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, -0.1149F, -0.0043F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(20, 1).addBox(-3.0F, -0.3367F, -0.8838F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(10, 24).addBox(-1.0F, 0.256F, -0.0315F, 2.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(32, 22).addBox(2.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(32, 17).addBox(-3.1F, -0.2371F, -0.8751F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5851F, 1.8947F, -0.3054F, 0.0F, 0.0F));

		PartDefinition plate3 = shell.addOrReplaceChild("plate3", CubeListBuilder.create().texOffs(0, 15).addBox(-3.0F, -1.6226F, -1.9775F, 6.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(12, 35).addBox(3.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(30, 31).addBox(-4.5F, -1.9955F, -2.0353F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-4.0F, -2.2445F, -2.0571F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(18, 27).addBox(-4.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(3.1F, -1.2483F, -1.9699F, 1.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.4226F, -0.0225F, -0.0436F, 0.0F, 0.0F));

		PartDefinition rLeg3 = plate3.addOrReplaceChild("rLeg3", CubeListBuilder.create().texOffs(0, 7).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 10).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.625F, 0.3774F, 0.5225F, 0.0436F, 0.0F, 0.0F));

		PartDefinition lLeg3 = plate3.addOrReplaceChild("lLeg3", CubeListBuilder.create().texOffs(0, 17).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 11).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.875F, 0.3774F, 0.5225F, 0.0436F, 0.0F, 0.0F));

		PartDefinition plate2 = shell.addOrReplaceChild("plate2", CubeListBuilder.create().texOffs(0, 10).addBox(-4.0F, -0.5197F, -3.1395F, 8.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(16, 18).addBox(-3.0F, 0.2803F, -3.1395F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 32).addBox(3.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(6, 29).addBox(-4.25F, -0.2735F, -3.1829F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.7759F, -2.2109F, 0.1745F, 0.0F, 0.0F));

		PartDefinition lLeg2 = plate2.addOrReplaceChild("lLeg2", CubeListBuilder.create().texOffs(2, 17).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.125F, 1.625F, 0.2997F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg5 = plate2.addOrReplaceChild("rLeg5", CubeListBuilder.create().texOffs(0, 2).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.375F, 1.2803F, -1.6395F, -0.1745F, 0.0F, 0.0F));

		PartDefinition rLeg2 = plate2.addOrReplaceChild("rLeg2", CubeListBuilder.create().texOffs(2, 7).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.875F, 1.625F, 0.2997F, -0.1745F, 0.0F, 0.0F));

		PartDefinition lLeg = plate2.addOrReplaceChild("lLeg", CubeListBuilder.create().texOffs(18, 14).addBox(0.625F, 1.25F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(6, 26).addBox(-0.125F, -0.75F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.625F, 1.2803F, -1.6395F, -0.1745F, 0.0F, 0.0F));

		PartDefinition plate = plate2.addOrReplaceChild("plate", CubeListBuilder.create().texOffs(20, 6).addBox(-2.5F, -0.7866F, -2.7514F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(6, 34).addBox(-2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(18, 33).addBox(2.6F, -0.5987F, -2.8198F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, 0.727F, -2.5342F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head = plate.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 24).addBox(-2.5F, -0.2667F, -1.8333F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 0.2301F, -0.668F));

		PartDefinition leftEye = head.addOrReplaceChild("leftEye", CubeListBuilder.create().texOffs(10, 26).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(2.75F, 0.0333F, -1.0833F));

		PartDefinition rightEye = head.addOrReplaceChild("rightEye", CubeListBuilder.create().texOffs(11, 22).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.75F, 0.0333F, -1.0833F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		whole.render(poseStack, buffer, packedLight, packedOverlay);
	}
}