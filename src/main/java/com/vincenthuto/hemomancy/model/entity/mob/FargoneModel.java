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


public class FargoneModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Hemomancy.MOD_ID, "modelfargone"), "main");
	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(22, 39).addBox(-3.5F, -6.3992F, 0.5124F, 7.0F, 11.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(40, 39).addBox(-3.5F, -4.3381F, -2.7903F, 7.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(24, 0).addBox(-2.5F, 2.6619F, -1.7903F, 5.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.5627F, 0.9718F, 0.2618F, 0.0F, 0.0F));

		PartDefinition Head = Body.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -5.597F, -1.7562F, 0.3491F, 0.0F, 0.0F));

		PartDefinition head2 = Head.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.169F, -6.5408F, 8.0F, 7.0F, 8.0F, new CubeDeformation(-1.0F))
		.texOffs(34, 21).addBox(0.0F, -6.7928F, -7.0782F, 5.0F, 5.0F, 6.0F, new CubeDeformation(-1.0F))
		.texOffs(0, 33).addBox(-5.0F, -6.7928F, -7.0782F, 5.0F, 5.0F, 6.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, -0.7104F, 1.2249F, -0.2618F, 0.0F, 0.0F));

		PartDefinition lowerJaw = head2.addOrReplaceChild("lowerJaw", CubeListBuilder.create().texOffs(29, 8).addBox(-3.0F, -3.6991F, -5.3355F, 6.0F, 3.0F, 7.0F, new CubeDeformation(-1.0F))
		.texOffs(14, 15).addBox(-1.0F, -3.0613F, -13.6828F, 2.0F, 1.0F, 11.0F, new CubeDeformation(-1.0F))
		.texOffs(0, 15).addBox(-0.5F, -2.5771F, -15.0622F, 1.0F, 1.0F, 12.0F, new CubeDeformation(-1.0F))
		.texOffs(17, 27).addBox(-1.5F, -4.6068F, -11.2687F, 3.0F, 3.0F, 9.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(0.0F, 2.5312F, -0.9696F, -0.0873F, 0.0F, 0.0F));

		PartDefinition bone = lowerJaw.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(29, 18).addBox(-1.5138F, -0.7585F, -0.6527F, 1.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(-1.2981F, -4.7485F, -6.1487F, 0.0F, 0.0F, -0.6981F));

		PartDefinition bone2 = lowerJaw.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(0.0542F, -0.3728F, -0.6527F, 1.0F, 4.0F, 3.0F, new CubeDeformation(-1.0F)), PartPose.offsetAndRotation(1.7019F, -4.7485F, -6.1487F, 0.0F, 0.0F, 0.6981F));

		PartDefinition neck = Head.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(41, 32).addBox(-1.5F, -27.5795F, 1.3225F, 3.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, -2.0F));

		PartDefinition rightWing = Body.addOrReplaceChild("rightWing", CubeListBuilder.create().texOffs(14, 19).addBox(-0.6006F, -1.1582F, -0.2803F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(8, 40).addBox(-0.6006F, -0.1582F, 0.2197F, 0.0F, 15.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-0.6006F, 14.8418F, 0.7197F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -5.4353F, 2.0799F, 0.0F, 0.48F, 0.0F));

		PartDefinition leftWing = Body.addOrReplaceChild("leftWing", CubeListBuilder.create().texOffs(14, 18).addBox(0.0F, -1.3523F, -0.7844F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(0, 40).addBox(0.0F, -0.3523F, -0.2844F, 0.0F, 15.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 4).addBox(0.0F, 14.6477F, 0.2156F, 0.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.3F, -5.2412F, 2.8043F, 0.0F, -0.48F, 0.0F));

		PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create().texOffs(24, 52).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(14, 15).addBox(-1.3F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, 2.0F, 0.0F));

		PartDefinition rightFore = RightArm.addOrReplaceChild("rightFore", CubeListBuilder.create().texOffs(14, 23).addBox(-0.5F, 5.1667F, 0.3333F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(5, 23).addBox(-0.5F, 5.1667F, -1.4167F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(24, 52).addBox(-1.0F, 0.1667F, -0.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.8333F, 0.6667F));

		PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create().texOffs(48, 50).addBox(-2.1F, 3.4F, 0.3F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(38, 50).addBox(-2.0F, 0.0F, 0.8F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));

		PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(48, 0).addBox(-1.0F, 0.0F, 0.8F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(0, 15).addBox(-0.9F, 3.4F, 0.3F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));

		PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(16, 50).addBox(-1.0F, -1.0F, -0.25F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(16, 28).addBox(-0.7F, 3.0F, -0.75F, 2.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(4.5F, 2.0F, 0.0F));

		PartDefinition leftFore = LeftArm.addOrReplaceChild("leftFore", CubeListBuilder.create().texOffs(16, 50).addBox(-1.0F, -0.8333F, -0.9167F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(21, 15).addBox(-0.5F, 4.1667F, 0.3333F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(20, 21).addBox(-0.5F, 4.1667F, -1.4167F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.8333F, 0.6667F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
	private final ModelPart Body;
	private final ModelPart RightArm;
	private final ModelPart RightLeg;
	private final ModelPart LeftLeg;

	private final ModelPart LeftArm;
	public FargoneModel(ModelPart root) {
		this.Body = root.getChild("Body");
		this.RightArm = root.getChild("RightArm");
		this.RightLeg = root.getChild("RightLeg");
		this.LeftLeg = root.getChild("LeftLeg");
		this.LeftArm = root.getChild("LeftArm");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		Body.render(poseStack, buffer, packedLight, packedOverlay);
		RightArm.render(poseStack, buffer, packedLight, packedOverlay);
		RightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		LeftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		LeftArm.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}
}