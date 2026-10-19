package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings


public class VesperTheCrownedRefusalModel extends HierarchicalModel<VesperTheCrownedRefusalEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "vesper_crowned_refusal"), "main");
	private final ModelPart whole;
	private final ModelPart vesper;
	private final ModelPart head2;
	private final ModelPart crown;
	private final ModelPart bone6;
	private final ModelPart hat3;
	private final ModelPart hat4;
	private final ModelPart hat2;
	private final ModelPart hat5;
	private final ModelPart hat;
	private final ModelPart hood1;
	private final ModelPart hood2;
	private final ModelPart hood3;
	private final ModelPart hood4;
	private final ModelPart body;
	private final ModelPart ClothBack;
	private final ModelPart ClothBack1;
	private final ModelPart ClothBackR1;
	private final ModelPart ClothBackL1;
	private final ModelPart ClothBack2;
	private final ModelPart ClothBackR2;
	private final ModelPart ClothBackR3;
	private final ModelPart ClothBackL2;
	private final ModelPart ClothBackL3;
	private final ModelPart cloak;
	private final ModelPart CloakTL;
	private final ModelPart CloakTR;
	private final ModelPart belt;
	private final ModelPart SideclothL;
	private final ModelPart SideclothL1;
	private final ModelPart SideclothL2;
	private final ModelPart SideclothL3;
	private final ModelPart SideclothL4;
	private final ModelPart SideclothL5;
	private final ModelPart SideclothL6;
	private final ModelPart SideclothL7;
	private final ModelPart leftArm;
	private final ModelPart lShoulder;
	private final ModelPart lElbow;
	private final ModelPart rightArm;
	private final ModelPart rShoulder;
	private final ModelPart rElbow;
	private final ModelPart leftLeg;
	private final ModelPart leftLeg2;
	private final ModelPart leftBoot;
	private final ModelPart lShin;
	private final ModelPart lShin3;
	private final ModelPart rightLeg;
	private final ModelPart rightLeg2;
	private final ModelPart rightBoot;
	private final ModelPart lrhin;
	private final ModelPart rshin2;

	public VesperTheCrownedRefusalModel(ModelPart root) {
		this.whole = root.getChild("whole");
		this.vesper = this.whole.getChild("vesper");
		this.head2 = this.vesper.getChild("head2");
		this.crown = this.head2.getChild("crown");
		this.bone6 = this.crown.getChild("bone6");
		this.hat3 = this.crown.getChild("hat3");
		this.hat4 = this.crown.getChild("hat4");
		this.hat2 = this.crown.getChild("hat2");
		this.hat5 = this.crown.getChild("hat5");
		this.hat = this.crown.getChild("hat");
		this.hood1 = this.head2.getChild("hood1");
		this.hood2 = this.hood1.getChild("hood2");
		this.hood3 = this.hood2.getChild("hood3");
		this.hood4 = this.hood3.getChild("hood4");
		this.body = this.vesper.getChild("body");
		this.ClothBack = this.body.getChild("ClothBack");
		this.ClothBack1 = this.ClothBack.getChild("ClothBack1");
		this.ClothBackR1 = this.ClothBack1.getChild("ClothBackR1");
		this.ClothBackL1 = this.ClothBack1.getChild("ClothBackL1");
		this.ClothBack2 = this.ClothBack1.getChild("ClothBack2");
		this.ClothBackR2 = this.ClothBack2.getChild("ClothBackR2");
		this.ClothBackR3 = this.ClothBack2.getChild("ClothBackR3");
		this.ClothBackL2 = this.ClothBack2.getChild("ClothBackL2");
		this.ClothBackL3 = this.ClothBack2.getChild("ClothBackL3");
		this.cloak = this.body.getChild("cloak");
		this.CloakTL = this.cloak.getChild("CloakTL");
		this.CloakTR = this.cloak.getChild("CloakTR");
		this.belt = this.body.getChild("belt");
		this.SideclothL = this.body.getChild("SideclothL");
		this.SideclothL1 = this.SideclothL.getChild("SideclothL1");
		this.SideclothL2 = this.SideclothL1.getChild("SideclothL2");
		this.SideclothL3 = this.SideclothL2.getChild("SideclothL3");
		this.SideclothL4 = this.body.getChild("SideclothL4");
		this.SideclothL5 = this.SideclothL4.getChild("SideclothL5");
		this.SideclothL6 = this.SideclothL5.getChild("SideclothL6");
		this.SideclothL7 = this.SideclothL6.getChild("SideclothL7");
		this.leftArm = this.vesper.getChild("leftArm");
		this.lShoulder = this.leftArm.getChild("lShoulder");
		this.lElbow = this.lShoulder.getChild("lElbow");
		this.rightArm = this.vesper.getChild("rightArm");
		this.rShoulder = this.rightArm.getChild("rShoulder");
		this.rElbow = this.rShoulder.getChild("rElbow");
		this.leftLeg = this.vesper.getChild("leftLeg");
		this.leftLeg2 = this.leftLeg.getChild("leftLeg2");
		this.leftBoot = this.leftLeg2.getChild("leftBoot");
		this.lShin = this.leftBoot.getChild("lShin");
		this.lShin3 = this.leftBoot.getChild("lShin3");
		this.rightLeg = this.vesper.getChild("rightLeg");
		this.rightLeg2 = this.rightLeg.getChild("rightLeg2");
		this.rightBoot = this.rightLeg2.getChild("rightBoot");
		this.lrhin = this.rightBoot.getChild("lrhin");
		this.rshin2 = this.rightBoot.getChild("rshin2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(-1.25F, -11.0F, -17.95F));

		PartDefinition vesper = whole.addOrReplaceChild("vesper", CubeListBuilder.create(), PartPose.offset(1.5143F, -38.7953F, 10.6518F));

		PartDefinition head2 = vesper.addOrReplaceChild("head2", CubeListBuilder.create().texOffs(78, 813).addBox(-4.0F, -9.075F, -5.625F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0143F, -11.4387F, 0.2109F, 0.48F, 0.0F, 0.0F));

		PartDefinition crown = head2.addOrReplaceChild("crown", CubeListBuilder.create(), PartPose.offset(21.5F, -7.0993F, 0.0873F));

		PartDefinition bone6 = crown.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(78, 792).addBox(-30.25F, 2.625F, -9.75F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.35F, -0.775F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat3 = crown.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(179, 863).addBox(-27.5242F, -0.2323F, -6.7318F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.35F, -0.775F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat4 = crown.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(224, 848).addBox(-26.3154F, -0.6148F, -7.5242F, 13.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.35F, -0.775F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat2 = crown.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(187, 810).addBox(-11.7258F, -0.7677F, -6.7682F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.75F, -13.35F, -0.775F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat5 = crown.addOrReplaceChild("hat5", CubeListBuilder.create().texOffs(78, 937).addBox(-27.1846F, -0.7602F, 7.5242F, 14.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.35F, -0.775F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat = crown.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(99, 889).addBox(-28.7742F, 1.2677F, -6.7318F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(132, 890).addBox(-13.7258F, 0.7323F, -6.7682F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(208, 873).addBox(-26.3154F, 1.2602F, -8.2742F, 13.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(208, 868).addBox(-27.1846F, 0.7398F, 6.7742F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -13.35F, -0.775F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hood1 = head2.addOrReplaceChild("hood1", CubeListBuilder.create().texOffs(129, 840).addBox(-27.25F, -14.0F, -6.9F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(78, 855).addBox(-15.25F, -14.0F, -6.9F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(78, 838).addBox(-25.75F, -14.0F, -6.9F, 11.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(22.0F, 3.075F, -0.375F));

		PartDefinition hood2 = hood1.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(111, 869).addBox(-26.0F, -15.05F, 3.0F, 12.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

		PartDefinition hood3 = hood2.addOrReplaceChild("hood3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition Hood3_52_14_697af612_r1 = hood3.addOrReplaceChild("Hood3_52_14_697af612_r1", CubeListBuilder.create().texOffs(159, 792).addBox(-5.75F, -15.0F, 5.25F, 11.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.0F, 0.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition hood4 = hood3.addOrReplaceChild("hood4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

		PartDefinition Hood4_53_15_2a9435b8_r1 = hood4.addOrReplaceChild("Hood4_53_15_2a9435b8_r1", CubeListBuilder.create().texOffs(194, 898).addBox(-4.5F, -16.55F, 5.25F, 9.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.0F, 0.0F, 1.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition body = vesper.addOrReplaceChild("body", CubeListBuilder.create().texOffs(129, 813).addBox(-26.0F, -2.0F, -3.0F, 12.0F, 20.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(166, 810).addBox(-15.4754F, -1.5298F, -3.75F, 2.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(78, 884).addBox(-26.9496F, -1.5298F, -3.75F, 2.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(207, 935).addBox(-26.15F, -0.75F, -4.875F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(145, 947).addBox(-16.85F, -0.75F, -4.875F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(132, 922).addBox(-25.85F, -0.75F, 2.775F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 840).addBox(-26.15F, -1.25F, 2.1F, 12.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(19.9857F, -8.538F, 1.2982F));

		PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(0.0F, 18.45F, 6.6F));

		PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, -1.5F));

		PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(107, 941).addBox(-26.0F, 0.0F, -3.0F, 6.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(227, 883).addBox(-26.0F, 0.0F, -3.0F, 6.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 12.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

		PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(176, 943).addBox(-22.05F, 11.0283F, -4.3152F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(218, 949).addBox(-26.25F, 10.5283F, -4.3152F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(241, 830).addBox(-25.95F, 11.0283F, -4.3152F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(233, 949).addBox(-24.75F, 10.5283F, -4.3152F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offsetAndRotation(-21.0F, 0.5014F, 5.8654F, 0.2182F, 0.0F, 0.0F));

		PartDefinition CloakTL = cloak.addOrReplaceChild("CloakTL", CubeListBuilder.create().texOffs(111, 855).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.25F, -0.9813F, -2.8584F, 0.1396F, 0.0F, 0.0F));

		PartDefinition CloakTR = cloak.addOrReplaceChild("CloakTR", CubeListBuilder.create().texOffs(176, 949).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, -0.9813F, -2.8584F, 0.1396F, 0.0F, 0.0F));

		PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(208, 878).addBox(-6.0F, -20.0F, -4.5F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(111, 863).addBox(-3.0F, -18.5F, -4.5F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 863).addBox(-3.0F, -21.5F, -4.25F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.0F, 37.1833F, 0.25F));

		PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(-14.05F, 15.375F, 0.0F));

		PartDefinition SideclothL1 = SideclothL.addOrReplaceChild("SideclothL1", CubeListBuilder.create().texOffs(219, 915).addBox(-20.4375F, -0.3964F, -3.75F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(19.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

		PartDefinition SideclothL2 = SideclothL1.addOrReplaceChild("SideclothL2", CubeListBuilder.create().texOffs(220, 824).addBox(-20.6678F, -6.9576F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3614F, 8.2145F, 0.0F, 0.0F, 0.0F, -0.2967F));

		PartDefinition SideclothL3 = SideclothL2.addOrReplaceChild("SideclothL3", CubeListBuilder.create().texOffs(186, 935).addBox(-14.4319F, -14.7607F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.789F, 3.6096F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition SideclothL4 = body.addOrReplaceChild("SideclothL4", CubeListBuilder.create(), PartPose.offset(-26.45F, 15.375F, 0.0F));

		PartDefinition SideclothL5 = SideclothL4.addOrReplaceChild("SideclothL5", CubeListBuilder.create().texOffs(219, 932).addBox(18.4375F, -0.3964F, -3.75F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition SideclothL6 = SideclothL5.addOrReplaceChild("SideclothL6", CubeListBuilder.create().texOffs(223, 898).addBox(18.6678F, -6.9576F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3614F, 8.2145F, 0.0F, 0.0F, 0.0F, 0.2967F));

		PartDefinition SideclothL7 = SideclothL6.addOrReplaceChild("SideclothL7", CubeListBuilder.create().texOffs(225, 792).addBox(12.4319F, -14.7607F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.789F, 3.6096F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition leftArm = vesper.addOrReplaceChild("leftArm", CubeListBuilder.create(), PartPose.offset(6.4857F, -5.538F, 1.2982F));

		PartDefinition lShoulder = leftArm.addOrReplaceChild("lShoulder", CubeListBuilder.create().texOffs(192, 792).addBox(-3.0887F, 1.7157F, -3.8313F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(2.175F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition ShoulderL_16_45_0eedefc6_r1 = lShoulder.addOrReplaceChild("ShoulderL_16_45_0eedefc6_r1", CubeListBuilder.create().texOffs(165, 910).addBox(-2.7902F, -2.7968F, -3.7688F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.1875F)), PartPose.offsetAndRotation(0.9469F, 0.7347F, -0.0938F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderL_16_45_0eedefc6_r2 = lShoulder.addOrReplaceChild("ShoulderL_16_45_0eedefc6_r2", CubeListBuilder.create().texOffs(99, 906).addBox(-3.9845F, -3.4911F, -3.875F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1345F));

		PartDefinition lElbow = lShoulder.addOrReplaceChild("lElbow", CubeListBuilder.create().texOffs(179, 883).addBox(-2.6875F, 1.5F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(78, 943).addBox(-2.9375F, 5.0F, 0.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(165, 890).addBox(-2.9375F, 0.0F, -7.5F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(78, 913).addBox(-2.4375F, 11.0F, -6.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0125F, 7.75F, 3.75F, -0.4363F, 0.0F, 0.0F));

		PartDefinition rightArm = vesper.addOrReplaceChild("rightArm", CubeListBuilder.create(), PartPose.offset(-7.0143F, -5.538F, 1.2982F));

		PartDefinition rShoulder = rightArm.addOrReplaceChild("rShoulder", CubeListBuilder.create().texOffs(194, 883).addBox(-4.9113F, 1.7157F, -3.8313F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.125F)), PartPose.offsetAndRotation(-2.175F, 0.0F, 0.0F, -0.1309F, 0.0F, 0.0F));

		PartDefinition ShoulderL_17_45_0eedefc7_r1 = rShoulder.addOrReplaceChild("ShoulderL_17_45_0eedefc7_r1", CubeListBuilder.create().texOffs(78, 921).addBox(-3.2098F, -2.7968F, -3.7688F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.1875F)), PartPose.offsetAndRotation(-0.9469F, 0.7347F, -0.0938F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderL_17_45_0eedefc7_r2 = rShoulder.addOrReplaceChild("ShoulderL_17_45_0eedefc7_r2", CubeListBuilder.create().texOffs(132, 907).addBox(-4.0155F, -3.4911F, -3.875F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1345F));

		PartDefinition rElbow = rShoulder.addOrReplaceChild("rElbow", CubeListBuilder.create().texOffs(78, 953).addBox(-2.3125F, 1.5F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(157, 943).addBox(-3.0625F, 5.0F, 0.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(191, 830).addBox(-3.0625F, 0.0F, -7.5F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(124, 947).addBox(-2.5625F, 11.0F, -6.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0125F, 7.75F, 3.75F, -0.829F, 0.0F, 0.0F));

		PartDefinition leftLeg = vesper.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(161, 926).addBox(-5.0125F, 0.2917F, -1.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.8482F, 9.1703F, -0.2018F, -1.1345F, 0.0F, 0.0F));

		PartDefinition leftLeg2 = leftLeg.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(194, 915).addBox(-3.025F, -0.4167F, 0.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.9875F, 10.7083F, -1.5F, 0.9599F, 0.0F, 0.0F));

		PartDefinition leftBoot = leftLeg2.addOrReplaceChild("leftBoot", CubeListBuilder.create().texOffs(220, 838).addBox(-22.9F, 15.1F, -3.1F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.2F))
		.texOffs(240, 942).addBox(-21.35F, 15.2698F, -4.3852F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(124, 955).addBox(-19.975F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(133, 955).addBox(-22.225F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(20.025F, -6.0833F, 3.0F));

		PartDefinition lShin = leftBoot.addOrReplaceChild("lShin", CubeListBuilder.create().texOffs(97, 943).addBox(-23.125F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(193, 949).addBox(-21.125F, -4.25F, -1.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(156, 953).addBox(-19.0F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.025F, 11.375F, -2.975F, -0.0873F, 0.0F, 0.0F));

		PartDefinition lShin3 = leftBoot.addOrReplaceChild("lShin3", CubeListBuilder.create().texOffs(240, 933).addBox(-23.075F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(239, 873).addBox(-21.075F, -3.5F, -1.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(241, 806).addBox(-18.95F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.025F, 15.125F, 3.025F, 0.0873F, 0.0F, 0.0F));

		PartDefinition rightLeg = vesper.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(216, 807).addBox(-0.9875F, 0.2917F, -1.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.3768F, 9.1703F, -0.2018F, -1.0472F, 0.0F, 0.0F));

		PartDefinition rightLeg2 = rightLeg.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(107, 921).addBox(-2.975F, -0.4167F, 0.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.9875F, 10.7083F, -1.5F, 0.9599F, 0.0F, 0.0F));

		PartDefinition rightBoot = rightLeg2.addOrReplaceChild("rightBoot", CubeListBuilder.create().texOffs(132, 937).addBox(16.9F, 15.1F, -3.1F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.2F))
		.texOffs(241, 824).addBox(18.35F, 15.2698F, -4.3852F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(241, 854).addBox(17.975F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(241, 860).addBox(20.225F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.025F, -6.0833F, 3.0F));

		PartDefinition lrhin = rightBoot.addOrReplaceChild("lrhin", CubeListBuilder.create().texOffs(165, 953).addBox(21.125F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(204, 950).addBox(18.125F, -4.25F, -1.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(240, 912).addBox(17.0F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.025F, 11.375F, -2.975F, -0.0873F, 0.0F, 0.0F));

		PartDefinition rshin2 = rightBoot.addOrReplaceChild("rshin2", CubeListBuilder.create().texOffs(93, 955).addBox(21.075F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(240, 924).addBox(18.075F, -3.5F, -1.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(241, 815).addBox(16.95F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.025F, 15.125F, 3.025F, 0.0873F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 1024, 1024);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int packedColor) {
		renderVesperOnly(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
	}

	public void translateToRiderWeapon(PoseStack poseStack) {
		whole.translateAndRotate(poseStack);
		vesper.translateAndRotate(poseStack);
		rightArm.translateAndRotate(poseStack);
		rShoulder.translateAndRotate(poseStack);
		rElbow.translateAndRotate(poseStack);
		poseStack.translate(0.0D, 0.78D, -0.4D);
	}

	public void translateToRiderLeftHand(PoseStack poseStack) {
		whole.translateAndRotate(poseStack);
		vesper.translateAndRotate(poseStack);
		leftArm.translateAndRotate(poseStack);
		lShoulder.translateAndRotate(poseStack);
		lElbow.translateAndRotate(poseStack);
		poseStack.translate(0.05D, 0.42D, -0.53D);
	}

	/** Vesper alone; the mount he rides is the separately generated {@link VesperMountModel}. */
	public void renderVesperOnly(PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int packedColor) {
		poseStack.pushPose();
		whole.translateAndRotate(poseStack);
		vesper.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		poseStack.popPose();
	}

	@Override
	public void setupAnim(VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		whole.getAllParts().forEach(ModelPart::resetPose);
		if (entity.getTransitionTick() > 0) {
			this.animate(entity.transformationAnimationState,
					VesperTheCrownedRefusalAnimations.TRANSFORMATION, ageInTicks);
			return;
		}
		// The mount model has already advanced this entity's motion state this frame; ride the same clocks.
		VesperMountAnimState motion = VesperMountAnimState.of(entity);
		VesperRiderMotion.State state = new VesperRiderMotion.State(entity.getAttack(),
				motion.attackTime(), motion.staggerTime(),
				entity.getActiveAnchor() >= 0, entity.getFlightMode(), entity.isBurrowing());
		seated(ageInTicks);
		neckGripTuck(motion.riderGrip());
		crossbarRaise(VesperRiderMotion.weight(VesperRiderMotion.Pose.CROSSBAR_RAISE, state), ageInTicks);
		woundRecoil(motion.riderRecoil(), ageInTicks);
		flightLean(motion.riderFlight(), motion.bank(), motion.flightPitch());
		stagger(VesperRiderMotion.weight(VesperRiderMotion.Pose.STAGGER, state), ageInTicks);
		if (entity.getActiveAnchor() < 0 && !entity.isBurrowing()) {
			this.head2.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
			this.head2.yRot += Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD * 0.5F;
		}
	}

	// Rider poses, each added on top of the Blockbench rest pose and scaled by its weight.

	private void seated(float age) {
		float breath = Mth.sin(age * 0.07F);
		this.body.xRot += breath * 0.02F;
		this.head2.xRot -= breath * 0.015F;
		this.rightArm.xRot += Mth.sin(age * 0.05F) * 0.03F;
		this.leftArm.xRot += Mth.sin(age * 0.05F + 1.1F) * 0.03F;
		this.ClothBack.xRot += Mth.sin(age * 0.24F) * 0.035F;
		this.ClothBack1.xRot += Mth.sin(age * 0.31F) * 0.055F;
		this.ClothBack2.xRot += Mth.sin(age * 0.39F) * 0.075F;
		this.ClothBackR2.xRot += Mth.sin(age * 0.43F) * 0.06F;
		this.ClothBackL2.xRot += Mth.sin(age * 0.43F + Mth.PI) * 0.06F;
	}

	/** He leans forward, tucks low and takes the mount's nape in his free (left) hand. */
	private void neckGripTuck(float w) {
		if (w <= 0.0F) return;
		this.vesper.xRot += 0.55F * w;
		this.vesper.y += 4.0F * w;
		this.vesper.z -= 5.0F * w;
		this.head2.xRot -= 0.35F * w;
		this.leftArm.xRot -= 1.35F * w;
		this.leftArm.zRot += 0.15F * w;
		this.lElbow.xRot -= 0.4F * w;
		this.rightArm.xRot -= 0.45F * w;
		this.rightArm.zRot -= 0.35F * w;
		this.ClothBack.xRot -= 0.5F * w;
		this.ClothBack1.xRot -= 0.35F * w;
	}

	private void crossbarRaise(float w, float age) {
		if (w <= 0.0F) return;
		this.leftArm.xRot -= 2.4F * w;
		this.leftArm.zRot -= 0.25F * w;
		this.lElbow.xRot += 0.35F * w;
		this.leftArm.yRot += Mth.sin(age * 0.35F) * 0.08F * w;
		this.body.yRot += 0.15F * w;
		this.head2.xRot -= 0.25F * w;
	}

	private void woundRecoil(float w, float age) {
		if (w <= 0.0F) return;
		float tremor = Mth.sin(age * 1.9F) * 0.03F;
		this.vesper.xRot -= 0.22F * w;
		this.body.xRot -= 0.12F * w + tremor * w;
		this.rightArm.zRot -= 0.45F * w;
		this.leftArm.zRot += 0.45F * w;
		this.head2.xRot += 0.3F * w;
	}

	private void flightLean(float w, float bank, float pitch) {
		if (w <= 0.0F) return;
		this.vesper.xRot += (0.25F - pitch * 0.01F) * w;
		this.vesper.zRot -= bank * Mth.DEG_TO_RAD * 0.35F * w;
		this.ClothBack.xRot -= 0.7F * w;
		this.ClothBack1.xRot -= 0.45F * w;
		this.ClothBack2.xRot -= 0.3F * w;
	}

	private void stagger(float w, float age) {
		if (w <= 0.0F) return;
		this.vesper.xRot += 0.35F * w;
		this.vesper.zRot += Mth.sin(age * 0.8F) * 0.12F * w;
		this.head2.xRot += 0.4F * w;
		this.rightArm.xRot += 0.4F * w;
	}

	@Override
	public ModelPart root() {
		return whole;
	}
}
