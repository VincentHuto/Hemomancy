package com.vincenthuto.hemomancy.client.model.tile.crafting;// Made with Blockbench 5.1.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.fml.earlydisplay.ElementShader;

public class HematicArmatureModel extends Model {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("hematic_armature"), "main");
	private final ModelPart platform;
	private final ModelPart left_pillar;
	private final ModelPart right_pillar;
	private final ModelPart arch;
	private final ModelPart monolithic_cornerstone;
	private final ModelPart left_vial;
	private final ModelPart right_vial;
	private final ModelPart chains;
	private final ModelPart bowls;
	private final ModelPart altar_outer_left;
	private final ModelPart altar_outer_right;
	private final ModelPart altar_inner_left;
	private final ModelPart altar_inner_right;
	private final ModelPart vicarkit_upgrade;
	private final ModelPart candles;
	private final ModelPart banners;
	private final ModelPart bannerL;
	private final ModelPart bannerR;

	public HematicArmatureModel(ModelPart root) {
		super(RenderType::entityTranslucent);
		this.platform = root.getChild("platform");
		this.left_pillar = root.getChild("left_pillar");
		this.right_pillar = root.getChild("right_pillar");
		this.arch = root.getChild("arch");
		this.monolithic_cornerstone = root.getChild("monolithic_cornerstone");
		this.left_vial = root.getChild("left_vial");
		this.right_vial = root.getChild("right_vial");
		this.chains = root.getChild("chains");
		this.bowls = root.getChild("bowls");
		this.altar_outer_left = this.bowls.getChild("altar_outer_left");
		this.altar_outer_right = this.bowls.getChild("altar_outer_right");
		this.altar_inner_left = this.bowls.getChild("altar_inner_left");
		this.altar_inner_right = this.bowls.getChild("altar_inner_right");
		this.vicarkit_upgrade = root.getChild("vicarkit_upgrade");
		this.candles = this.vicarkit_upgrade.getChild("candles");
		this.banners = this.vicarkit_upgrade.getChild("banners");
		this.bannerL = this.banners.getChild("bannerL");
		this.bannerR = this.banners.getChild("bannerR");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition platform = partdefinition.addOrReplaceChild("platform", CubeListBuilder.create().texOffs(0, 0).addBox(-22.0F, -2.0F, -22.0F, 44.0F, 2.0F, 44.0F, new CubeDeformation(0.0F))
				.texOffs(0, 47).addBox(-12.0F, -5.0F, -10.0F, 24.0F, 4.0F, 20.0F, new CubeDeformation(0.0F))
				.texOffs(92, 142).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition left_pillar = partdefinition.addOrReplaceChild("left_pillar", CubeListBuilder.create().texOffs(0, 157).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 91).addBox(-1.0F, -54.0F, -2.0F, 4.0F, 46.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(169, 167).addBox(-3.0F, -57.7F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(159, 61).addBox(0.0F, -20.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(159, 61).addBox(0.0F, -20.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(138, 61).addBox(0.0F, -42.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(189, 110).addBox(0.0F, -50.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(23.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition right_pillar = partdefinition.addOrReplaceChild("right_pillar", CubeListBuilder.create().texOffs(113, 157).addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(17, 91).addBox(-2.0F, -54.0F, -2.0F, 4.0F, 46.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 172).addBox(-4.0F, -57.7F, -3.0F, 8.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(167, 187).addBox(-7.0F, -20.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(167, 187).addBox(-7.0F, -34.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(186, 187).addBox(-7.0F, -32.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(179, 73).addBox(-8.0F, -42.0F, -1.0F, 8.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(189, 115).addBox(-6.0F, -50.0F, -1.0F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-23.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition arch = partdefinition.addOrReplaceChild("arch", CubeListBuilder.create().texOffs(41, 118).addBox(0.0F, -2.0F, -2.0F, 23.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(96, 118).addBox(-23.0F, -2.0F, -2.0F, 23.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(175, 155).addBox(17.0F, 2.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(177, 26).addBox(-23.0F, 2.0F, -2.0F, 6.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -34.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition monolithic_cornerstone = partdefinition.addOrReplaceChild("monolithic_cornerstone", CubeListBuilder.create().texOffs(51, 142).addBox(-6.0F, -2.0F, -4.0F, 12.0F, 12.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(177, 0).addBox(5.9F, -2.0F, -1.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(177, 13).addBox(-13.9F, -2.0F, -1.0F, 8.0F, 10.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(177, 13).addBox(-3.9F, 10.0F, -1.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(19, 185).addBox(-2.0F, -5.9F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(177, 35).addBox(7.9F, -4.0F, -2.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(169, 178).addBox(-15.9F, -4.0F, -2.0F, 8.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(189, 120).addBox(1.0F, -7.9F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(190, 178).addBox(-5.0F, -7.9F, -2.0F, 4.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -50.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition left_vial = partdefinition.addOrReplaceChild("left_vial", CubeListBuilder.create().texOffs(110, 183).addBox(-2.0F, -18.0F, -1.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(110, 183).addBox(-2.0F, -32.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(110, 183).addBox(-2.0F, -34.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(110, 183).addBox(-2.0F, -37.0F, -1.0F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(72, 163).addBox(3.0F, -30.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(192, 101).addBox(-1.0F, -36.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition right_vial = partdefinition.addOrReplaceChild("right_vial", CubeListBuilder.create().texOffs(51, 184).addBox(-3.0F, -18.0F, -1.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(51, 184).addBox(-3.0F, -37.0F, -1.0F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(172, 110).addBox(-6.0F, -30.0F, -2.0F, 4.0F, 11.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-21.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition chains = partdefinition.addOrReplaceChild("chains", CubeListBuilder.create().texOffs(29, 157).addBox(14.1F, -58.0F, -0.5F, 1.0F, 13.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(34, 169).addBox(-14.0F, -58.0F, -0.5F, 1.0F, 26.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(41, 127).addBox(8.0F, -59.0F, -0.5F, 1.0F, 47.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(46, 127).addBox(-9.0F, -59.0F, -0.5F, 1.0F, 43.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(34, 110).addBox(-1.0F, -59.2F, -0.5F, 2.0F, 23.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition bowls = partdefinition.addOrReplaceChild("bowls", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 24.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition altar_outer_left = bowls.addOrReplaceChild("altar_outer_left", CubeListBuilder.create().texOffs(89, 47).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(124, 91).addBox(-5.0F, -18.0F, -5.0F, 10.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 72).addBox(-8.0F, -20.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(130, 66).addBox(-6.0F, -21.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(89, 66).addBox(-6.0F, -23.0F, -7.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(41, 110).addBox(-6.0F, -23.0F, 5.5F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(165, 80).addBox(6.0F, -23.0F, -6.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(165, 95).addBox(-6.5F, -23.0F, -6.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(42.0F, 0.0F, 4.0F));

		PartDefinition altar_outer_right = bowls.addOrReplaceChild("altar_outer_right", CubeListBuilder.create().texOffs(34, 91).addBox(-6.0F, -6.0F, -6.0F, 12.0F, 6.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(83, 91).addBox(-5.0F, -18.0F, -5.0F, 10.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(65, 72).addBox(-8.0F, -20.0F, -8.0F, 16.0F, 2.0F, 16.0F, new CubeDeformation(0.0F))
				.texOffs(138, 47).addBox(-6.0F, -21.0F, -6.0F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(41, 114).addBox(-6.0F, -23.0F, -7.0F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(124, 114).addBox(-6.0F, -23.0F, 5.5F, 12.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(166, 140).addBox(6.0F, -23.0F, -6.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(142, 167).addBox(-6.5F, -23.0F, -6.0F, 1.0F, 2.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-42.0F, 0.0F, 4.0F));

		PartDefinition altar_inner_left = bowls.addOrReplaceChild("altar_inner_left", CubeListBuilder.create().texOffs(0, 142).addBox(-4.0F, 4.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(113, 172).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(51, 127).addBox(-6.0F, -2.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(130, 80).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(175, 164).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(68, 192).addBox(-4.0F, -4.0F, 3.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(72, 182).addBox(4.0F, -4.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(91, 182).addBox(-4.5F, -4.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(29.0F, -10.0F, 17.0F));

		PartDefinition altar_inner_right = bowls.addOrReplaceChild("altar_inner_right", CubeListBuilder.create().texOffs(133, 142).addBox(-4.0F, 4.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(172, 129).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(100, 127).addBox(-6.0F, -2.0F, -6.0F, 12.0F, 2.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(142, 157).addBox(-4.0F, -3.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(192, 78).addBox(-4.0F, -4.0F, -5.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(192, 81).addBox(-4.0F, -4.0F, 3.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(138, 182).addBox(4.0F, -4.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(0, 183).addBox(-4.5F, -4.0F, -4.0F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-29.0F, -10.0F, 17.0F));

		PartDefinition vicarkit_upgrade = partdefinition.addOrReplaceChild("vicarkit_upgrade", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition candles = vicarkit_upgrade.addOrReplaceChild("candles", CubeListBuilder.create().texOffs(192, 93).addBox(-18.0F, -2.0F, 17.3333F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(39, 186).addBox(-16.0F, -5.0F, -8.6667F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(96, 192).addBox(-9.0F, -1.0F, -11.6667F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(192, 84).addBox(7.0F, -3.0F, -11.6667F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(127, 183).addBox(14.0F, -5.0F, -8.6667F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(87, 192).addBox(16.0F, -2.0F, 17.3333F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 6.6667F, 0.0F, 3.1416F, 0.0F));

		PartDefinition banners = vicarkit_upgrade.addOrReplaceChild("banners", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition bannerL = banners.addOrReplaceChild("bannerL", CubeListBuilder.create().texOffs(179, 61).addBox(-4.5F, -2.0F, -0.5F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(151, 114).addBox(-4.5F, 2.0F, -0.5F, 9.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(187, 44).addBox(-4.5F, 26.0F, -0.5F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(28.5F, -48.0F, 0.0F));

		PartDefinition bannerR = banners.addOrReplaceChild("bannerR", CubeListBuilder.create().texOffs(179, 67).addBox(-5.5F, -2.0F, -0.5F, 10.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(92, 156).addBox(-4.5F, 2.0F, -0.5F, 9.0F, 24.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(187, 50).addBox(-3.5F, 26.0F, -0.5F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-28.5F, -48.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	public void setupBannerAnimation(float ageInTicks) {
		bannerL.resetPose();
		bannerR.resetPose();
		float sway = Mth.sin(ageInTicks * 0.08F) * 0.045F;
		float flutter = Mth.sin(ageInTicks * 0.11F + 0.8F) * 0.018F;
		bannerL.zRot = sway;
		bannerR.zRot = -sway;
		bannerL.xRot = flutter;
		bannerR.xRot = flutter;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int packedColor) {
		platform.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		left_pillar.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		right_pillar.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		arch.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		monolithic_cornerstone.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		left_vial.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		right_vial.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		chains.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		bowls.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		vicarkit_upgrade.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}
}
