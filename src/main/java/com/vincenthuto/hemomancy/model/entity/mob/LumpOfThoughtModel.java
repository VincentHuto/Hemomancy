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


public class LumpOfThoughtModel<T extends Entity> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modellumpofthought"), "main");
	@SuppressWarnings("unused")

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition arm = bone.addOrReplaceChild("arm", CubeListBuilder.create().texOffs(44, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-7.5F, -10.0F, -0.5F, 0.0F, 0.0F, -0.6109F));

		PartDefinition arm2 = arm.addOrReplaceChild("arm2", CubeListBuilder.create().texOffs(0, 7).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 55).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 41).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition arm3 = arm2.addOrReplaceChild("arm3", CubeListBuilder.create().texOffs(90, 0).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 55).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(50, 94).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.025F, -3.975F, 0.575F));

		PartDefinition arm4 = arm3.addOrReplaceChild("arm4", CubeListBuilder.create().texOffs(65, 88).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(55, 25).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -4.025F, -0.575F));

		PartDefinition arm5 = arm4.addOrReplaceChild("arm5", CubeListBuilder.create().texOffs(93, 71).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(16, 93).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(56, 21).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition arm6 = bone.addOrReplaceChild("arm6", CubeListBuilder.create().texOffs(35, 23).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -10.0F, 7.5F, -0.6981F, 0.0F, 0.5672F));

		PartDefinition arm7 = arm6.addOrReplaceChild("arm7", CubeListBuilder.create().texOffs(0, 6).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(54, 0).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(60, 36).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition arm8 = arm7.addOrReplaceChild("arm8", CubeListBuilder.create().texOffs(57, 88).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 16).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(44, 94).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.025F, -3.975F, 0.575F));

		PartDefinition arm9 = arm8.addOrReplaceChild("arm9", CubeListBuilder.create().texOffs(49, 88).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(53, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(73, 92).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -4.025F, -0.575F));

		PartDefinition arm10 = arm9.addOrReplaceChild("arm10", CubeListBuilder.create().texOffs(8, 93).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 93).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(51, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition arm21 = bone.addOrReplaceChild("arm21", CubeListBuilder.create().texOffs(0, 6).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -11.0F, -0.5F, -0.7854F, 0.0F, -0.5236F));

		PartDefinition arm22 = arm21.addOrReplaceChild("arm22", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 20).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 12).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition arm23 = arm22.addOrReplaceChild("arm23", CubeListBuilder.create().texOffs(48, 62).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 26).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(66, 54).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.025F, -3.975F, 0.575F));

		PartDefinition arm24 = arm23.addOrReplaceChild("arm24", CubeListBuilder.create().texOffs(24, 55).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(26, 26).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -4.025F, -0.575F));

		PartDefinition arm25 = arm24.addOrReplaceChild("arm25", CubeListBuilder.create().texOffs(90, 66).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(90, 45).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(12, 10).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition arm11 = bone.addOrReplaceChild("arm11", CubeListBuilder.create().texOffs(26, 20).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.5F, -9.0F, -7.5F, 1.1345F, 0.0F, -0.48F));

		PartDefinition arm12 = arm11.addOrReplaceChild("arm12", CubeListBuilder.create().texOffs(0, 2).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(34, 46).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 5).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition arm13 = arm12.addOrReplaceChild("arm13", CubeListBuilder.create().texOffs(20, 87).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(47, 26).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(73, 88).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.025F, -3.975F, 0.575F));

		PartDefinition arm14 = arm13.addOrReplaceChild("arm14", CubeListBuilder.create().texOffs(12, 87).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(4, 44).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -4.025F, -0.575F));

		PartDefinition arm15 = arm14.addOrReplaceChild("arm15", CubeListBuilder.create().texOffs(36, 92).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(91, 50).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(0, 44).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition arm16 = bone.addOrReplaceChild("arm16", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.5F, -9.0F, -0.5F, 0.3054F, 0.0F, 0.5672F));

		PartDefinition arm17 = arm16.addOrReplaceChild("arm17", CubeListBuilder.create().texOffs(0, 1).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 37).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(12, 0).addBox(-1.5F, -3.75F, 0.0F, 3.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition arm18 = arm17.addOrReplaceChild("arm18", CubeListBuilder.create().texOffs(41, 86).addBox(-0.975F, -4.025F, -1.575F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(41, 20).addBox(-0.475F, -0.025F, -1.075F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(72, 54).addBox(-1.475F, -3.775F, -0.575F, 3.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.025F, -3.975F, 0.575F));

		PartDefinition arm19 = arm18.addOrReplaceChild("arm19", CubeListBuilder.create().texOffs(0, 71).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(38, 21).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.025F, -4.025F, -0.575F));

		PartDefinition arm20 = arm19.addOrReplaceChild("arm20", CubeListBuilder.create().texOffs(91, 31).addBox(-2.0F, -4.75F, 0.0F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(24, 97).addBox(-1.0F, -5.75F, 0.0F, 2.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(28, 91).addBox(-1.5F, -4.0F, -0.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
		.texOffs(35, 20).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(0, 0).addBox(-9.0F, -1.0F, -9.0F, 18.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.25F, -0.25F));

		PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(0, 71).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, -6.0F, -2.15F));

		PartDefinition bone4 = bone.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(34, 46).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(2.9F, -8.0F, -3.4F));

		PartDefinition bone5 = bone.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(80, 80).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.15F, -8.0F, 4.4F));

		PartDefinition bone6 = bone.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(0, 38).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.4F, -6.0F, 5.35F));

		PartDefinition bone7 = bone.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(24, 63).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.1F, -6.0F, 4.6F));

		PartDefinition bone8 = bone.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(26, 29).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.6F, -6.5F, -1.75F));

		PartDefinition bone9 = bone.addOrReplaceChild("bone9", CubeListBuilder.create().texOffs(58, 38).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-2.1F, -5.25F, -4.15F));

		PartDefinition bone10 = bone.addOrReplaceChild("bone10", CubeListBuilder.create().texOffs(58, 58).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-6.1F, -5.75F, 2.55F));

		PartDefinition bone11 = bone.addOrReplaceChild("bone11", CubeListBuilder.create().texOffs(0, 55).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(3.9F, -5.0F, -4.35F));

		PartDefinition bone12 = bone.addOrReplaceChild("bone12", CubeListBuilder.create().texOffs(0, 20).addBox(-4.5F, -4.5F, -4.0F, 9.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(5.4F, -5.0F, 3.35F));

		PartDefinition bone13 = bone.addOrReplaceChild("bone13", CubeListBuilder.create().texOffs(54, 0).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.1F, -4.35F, -6.15F));

		PartDefinition bone16 = bone.addOrReplaceChild("bone16", CubeListBuilder.create().texOffs(48, 74).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.85F, -4.5F, -5.4F));

		PartDefinition bone15 = bone.addOrReplaceChild("bone15", CubeListBuilder.create().texOffs(52, 20).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.9F, -5.5F, 6.6F));

		PartDefinition eye = bone.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 87).addBox(2.5F, -3.5F, 2.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -10.0F, -5.5F));

		PartDefinition eye2 = bone.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(85, 25).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-7.5F, -10.0F, 4.5F));

		PartDefinition eye3 = bone.addOrReplaceChild("eye3", CubeListBuilder.create().texOffs(29, 85).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.75F, -11.0F, 4.5F));

		PartDefinition eye7 = bone.addOrReplaceChild("eye7", CubeListBuilder.create().texOffs(76, 22).addBox(-8.5F, 0.5F, -0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(2.25F, -7.0F, 9.5F));

		PartDefinition eye12 = bone.addOrReplaceChild("eye12", CubeListBuilder.create().texOffs(72, 74).addBox(-8.5F, 0.5F, -0.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(12.25F, -10.25F, 8.5F));

		PartDefinition eye8 = bone.addOrReplaceChild("eye8", CubeListBuilder.create().texOffs(82, 39).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.75F, -3.0F, -10.5F));

		PartDefinition eye9 = bone.addOrReplaceChild("eye9", CubeListBuilder.create().texOffs(82, 33).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -3.0F, -6.5F));

		PartDefinition eye13 = bone.addOrReplaceChild("eye13", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(10.0F, -5.0F, 5.5F));

		PartDefinition eye10 = bone.addOrReplaceChild("eye10", CubeListBuilder.create().texOffs(32, 79).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.75F, -5.0F, -5.5F));

		PartDefinition eye11 = bone.addOrReplaceChild("eye11", CubeListBuilder.create().texOffs(78, 0).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-9.75F, -3.0F, 8.5F));

		PartDefinition eye4 = bone.addOrReplaceChild("eye4", CubeListBuilder.create().texOffs(84, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.25F, -10.0F, 4.5F));

		PartDefinition eye5 = bone.addOrReplaceChild("eye5", CubeListBuilder.create().texOffs(82, 60).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(7.25F, -10.0F, -4.5F));

		PartDefinition eye6 = bone.addOrReplaceChild("eye6", CubeListBuilder.create().texOffs(82, 54).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.75F, -10.0F, -7.5F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	private final ModelPart bone;
	public LumpOfThoughtModel(ModelPart root) {
		this.bone = root.getChild("bone");
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, buffer, packedLight, packedOverlay);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}
}