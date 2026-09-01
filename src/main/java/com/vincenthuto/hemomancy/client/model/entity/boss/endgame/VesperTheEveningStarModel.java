package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.*;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings


public class VesperTheEveningStarModel extends HierarchicalModel<VesperTheEveningStarEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "vesper_evening_star"), "main");
	private final ModelPart whole;
	private final ModelPart head;
	private final ModelPart crown;
	private final ModelPart bone5;
	private final ModelPart hat3;
	private final ModelPart hat4;
	private final ModelPart hat2;
	private final ModelPart hat5;
	private final ModelPart hat;
	private final ModelPart hood;
	private final ModelPart hood2;
	private final ModelPart hood3;
	private final ModelPart hood4;
	private final ModelPart hair;
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
	private final ModelPart SideclothL;
	private final ModelPart SideclothL1;
	private final ModelPart SideclothL2;
	private final ModelPart SideclothL3;
	private final ModelPart SideclothL4;
	private final ModelPart SideclothL5;
	private final ModelPart SideclothL6;
	private final ModelPart SideclothL7;
	private final ModelPart cloak;
	private final ModelPart CloakTL;
	private final ModelPart CloakTR;
	private final ModelPart Cloak1;
	private final ModelPart Cloak2;
	private final ModelPart Cloak3;
	private final ModelPart belt;
	private final ModelPart rightArm;
	private final ModelPart rShoulder;
	private final ModelPart rElbow;
	private final ModelPart leftArm;
	private final ModelPart rShoulder2;
	private final ModelPart rElbow2;
	private final ModelPart lShoulder;
	private final ModelPart lElbow;
	private final Vector3f animationVectorCache = new Vector3f();
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

	public VesperTheEveningStarModel(ModelPart root) {
		this.whole = root.getChild("whole");
		this.head = this.whole.getChild("head");
		this.crown = this.head.getChild("crown");
		this.bone5 = this.crown.getChild("bone5");
		this.hat3 = this.crown.getChild("hat3");
		this.hat4 = this.crown.getChild("hat4");
		this.hat2 = this.crown.getChild("hat2");
		this.hat5 = this.crown.getChild("hat5");
		this.hat = this.crown.getChild("hat");
		this.hood = this.head.getChild("hood");
		this.hood2 = this.hood.getChild("hood2");
		this.hood3 = this.hood2.getChild("hood3");
		this.hood4 = this.hood3.getChild("hood4");
		this.hair = this.head.getChild("hair");
		this.body = this.whole.getChild("body");
		this.ClothBack = this.body.getChild("ClothBack");
		this.ClothBack1 = this.ClothBack.getChild("ClothBack1");
		this.ClothBackR1 = this.ClothBack1.getChild("ClothBackR1");
		this.ClothBackL1 = this.ClothBack1.getChild("ClothBackL1");
		this.ClothBack2 = this.ClothBack1.getChild("ClothBack2");
		this.ClothBackR2 = this.ClothBack2.getChild("ClothBackR2");
		this.ClothBackR3 = this.ClothBack2.getChild("ClothBackR3");
		this.ClothBackL2 = this.ClothBack2.getChild("ClothBackL2");
		this.ClothBackL3 = this.ClothBack2.getChild("ClothBackL3");
		this.SideclothL = this.body.getChild("SideclothL");
		this.SideclothL1 = this.SideclothL.getChild("SideclothL1");
		this.SideclothL2 = this.SideclothL1.getChild("SideclothL2");
		this.SideclothL3 = this.SideclothL2.getChild("SideclothL3");
		this.SideclothL4 = this.body.getChild("SideclothL4");
		this.SideclothL5 = this.SideclothL4.getChild("SideclothL5");
		this.SideclothL6 = this.SideclothL5.getChild("SideclothL6");
		this.SideclothL7 = this.SideclothL6.getChild("SideclothL7");
		this.cloak = this.body.getChild("cloak");
		this.CloakTL = this.cloak.getChild("CloakTL");
		this.CloakTR = this.cloak.getChild("CloakTR");
		this.Cloak1 = this.cloak.getChild("Cloak1");
		this.Cloak2 = this.Cloak1.getChild("Cloak2");
		this.Cloak3 = this.Cloak2.getChild("Cloak3");
		this.belt = this.body.getChild("belt");
		this.rightArm = this.whole.getChild("rightArm");
		this.rShoulder = this.rightArm.getChild("rShoulder");
		this.rElbow = this.rShoulder.getChild("rElbow");
		this.leftArm = this.whole.getChild("leftArm");
		this.rShoulder2 = this.leftArm.getChild("rShoulder2");
		this.rElbow2 = this.rShoulder2.getChild("rElbow2");
		this.lShoulder = this.rShoulder2;
		this.lElbow = this.rElbow2;
		this.leftLeg = this.whole.getChild("leftLeg");
		this.leftLeg2 = this.leftLeg.getChild("leftLeg2");
		this.leftBoot = this.leftLeg2.getChild("leftBoot");
		this.lShin = this.leftBoot.getChild("lShin");
		this.lShin3 = this.leftBoot.getChild("lShin3");
		this.rightLeg = this.whole.getChild("rightLeg");
		this.rightLeg2 = this.rightLeg.getChild("rightLeg2");
		this.rightBoot = this.rightLeg2.getChild("rightBoot");
		this.lrhin = this.rightBoot.getChild("lrhin");
		this.rshin2 = this.rightBoot.getChild("rshin2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.1469F, -9.4663F, -0.5994F));

		PartDefinition head = whole.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 181).addBox(-6.125F, -9.075F, -5.625F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.0219F, -9.942F, 0.9744F));

		PartDefinition crown = head.addOrReplaceChild("crown", CubeListBuilder.create(), PartPose.offset(-0.5F, -16.275F, -2.15F));

		PartDefinition bone5 = crown.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(0, 0).addBox(-30.25F, 2.625F, -9.75F, 20.0F, 0.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.125F, -6.0F, 0.0F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat3 = crown.addOrReplaceChild("hat3", CubeListBuilder.create().texOffs(101, 71).addBox(-27.5242F, -0.2323F, -6.7318F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.125F, -6.0F, 0.0F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat4 = crown.addOrReplaceChild("hat4", CubeListBuilder.create().texOffs(146, 56).addBox(-26.8074F, -0.704F, -7.5303F, 13.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.125F, -6.0F, 0.0F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat2 = crown.addOrReplaceChild("hat2", CubeListBuilder.create().texOffs(109, 18).addBox(-11.7258F, -0.7677F, -6.7682F, 0.0F, 5.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(19.375F, -6.0F, 0.0F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat5 = crown.addOrReplaceChild("hat5", CubeListBuilder.create().texOffs(0, 145).addBox(-27.1846F, -0.7602F, 7.5242F, 14.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.125F, -6.0F, 0.0F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hat = crown.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(21, 97).addBox(-28.7742F, 1.2677F, -6.7318F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(54, 98).addBox(-13.7258F, 0.7323F, -6.7682F, 2.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(130, 81).addBox(-26.7074F, 1.171F, -8.2803F, 13.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(130, 76).addBox(-27.1846F, 0.7398F, 6.7742F, 14.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.125F, -6.0F, 0.0F, 0.1745F, 0.0436F, -0.1745F));

		PartDefinition hood = head.addOrReplaceChild("hood", CubeListBuilder.create().texOffs(51, 48).addBox(-27.25F, -14.0F, -6.9F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 63).addBox(-15.25F, -14.0F, -6.9F, 2.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(0, 46).addBox(-25.75F, -14.0F, -6.9F, 11.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(11, 32).addBox(-26.25F, -12.15F, -6.25F, 12.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(20.125F, 3.075F, -0.375F));

		PartDefinition hood2 = hood.addOrReplaceChild("hood2", CubeListBuilder.create().texOffs(33, 77).addBox(-26.25F, -15.05F, 3.0F, 12.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));

		PartDefinition hood3 = hood2.addOrReplaceChild("hood3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));

		PartDefinition Hood3_52_14_697af612_r1 = hood3.addOrReplaceChild("Hood3_52_14_697af612_r1", CubeListBuilder.create().texOffs(81, 0).addBox(-5.75F, -15.0F, 5.25F, 11.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.0F, 0.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

		PartDefinition hood4 = hood3.addOrReplaceChild("hood4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));

		PartDefinition Hood4_53_15_2a9435b8_r1 = hood4.addOrReplaceChild("Hood4_53_15_2a9435b8_r1", CubeListBuilder.create().texOffs(116, 106).addBox(-4.75F, -16.55F, 5.25F, 9.0F, 11.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.0F, 0.0F, 1.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create()
				.texOffs(51, 48).addBox(-6.3F, -9.35F, -5.8F, 12.6F, 2.6F, 12.6F,
						new CubeDeformation(0.0F))
				.texOffs(51, 48).addBox(-6.3F, -9.25F, -5.85F, 12.6F, 4.8F, 0.7F,
						new CubeDeformation(0.0F)), PartPose.ZERO);

		PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(51, 21).addBox(-6.5062F, -4.8604F, -2.0284F, 12.0F, 20.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(88, 18).addBox(4.0184F, -4.3902F, -2.7784F, 2.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 92).addBox(-7.4559F, -4.3902F, -2.7784F, 2.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(129, 143).addBox(-6.6563F, -3.6104F, -3.9034F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(67, 155).addBox(2.6437F, -3.6104F, -3.9034F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 130).addBox(-6.3562F, -3.6104F, 3.7466F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(-6.6563F, -4.1104F, 3.0716F, 12.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.3594F, -4.0066F, -0.3722F));

		PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(19.4937F, 15.5896F, 7.5716F));

		PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, -1.5F));

		PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(29, 149).addBox(-26.0F, 0.0F, -3.0F, 6.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(149, 91).addBox(-26.0F, 0.0F, -3.0F, 6.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 12.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

		PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(98, 151).addBox(-21.95F, 11.0283F, -4.2151F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(140, 157).addBox(-26.25F, 10.5283F, -4.3151F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(163, 38).addBox(-26.05F, 11.0283F, -4.2151F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(155, 157).addBox(-24.75F, 10.5283F, -4.3151F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(5.4438F, 15.5146F, 0.9716F));

		PartDefinition SideclothL1 = SideclothL.addOrReplaceChild("SideclothL1", CubeListBuilder.create().texOffs(141, 123).addBox(-20.4375F, -0.3964F, -3.75F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(19.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

		PartDefinition SideclothL2 = SideclothL1.addOrReplaceChild("SideclothL2", CubeListBuilder.create().texOffs(142, 32).addBox(-20.6678F, -6.9576F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3614F, 8.2145F, 0.0F, 0.0F, 0.0F, -0.2967F));

		PartDefinition SideclothL3 = SideclothL2.addOrReplaceChild("SideclothL3", CubeListBuilder.create().texOffs(108, 143).addBox(-14.4319F, -14.7607F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.789F, 3.6096F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition SideclothL4 = body.addOrReplaceChild("SideclothL4", CubeListBuilder.create(), PartPose.offset(-6.9562F, 15.5146F, 0.9716F));

		PartDefinition SideclothL5 = SideclothL4.addOrReplaceChild("SideclothL5", CubeListBuilder.create().texOffs(141, 140).addBox(18.4375F, -0.3964F, -3.75F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition SideclothL6 = SideclothL5.addOrReplaceChild("SideclothL6", CubeListBuilder.create().texOffs(145, 106).addBox(18.6678F, -6.9576F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3614F, 8.2145F, 0.0F, 0.0F, 0.0F, 0.2967F));

		PartDefinition SideclothL7 = SideclothL6.addOrReplaceChild("SideclothL7", CubeListBuilder.create().texOffs(147, 0).addBox(12.4319F, -14.7607F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.789F, 3.6096F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5063F, -2.359F, 6.8371F, 0.2182F, 0.0F, 0.0F));

		PartDefinition CloakTL = cloak.addOrReplaceChild("CloakTL", CubeListBuilder.create().texOffs(33, 63).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.25F, -0.9813F, -2.8584F, 0.1396F, 0.0F, 0.0F));

		PartDefinition CloakTR = cloak.addOrReplaceChild("CloakTR", CubeListBuilder.create().texOffs(98, 157).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, -0.9813F, -2.8584F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak1 = cloak.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(68, 77).addBox(-27.25F, 3.0F, 1.5F, 14.0F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -2.8227F, -4.127F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak2 = Cloak1.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(113, 58).addBox(-27.25F, -0.2252F, -0.5443F, 14.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 21.0502F, 2.0869F, 0.3069F, 0.0F, 0.0F));

		PartDefinition Cloak3 = Cloak2.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(130, 67).addBox(-27.25F, -0.6727F, -0.2867F, 14.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.2578F, 0.0048F, 0.4466F, 0.0F, 0.0F));

		PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(130, 86).addBox(-6.0F, -20.0F, -4.5F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 71).addBox(-3.0F, -18.5F, -4.1F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 71).addBox(-3.0F, -21.5F, -4.1F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5062F, 34.3229F, 1.2216F));

		PartDefinition rightArm = whole.addOrReplaceChild("rightArm", CubeListBuilder.create(), PartPose.offset(-6.8969F, -3.867F, 0.5994F));

		PartDefinition rShoulder = rightArm.addOrReplaceChild("rShoulder", CubeListBuilder.create().texOffs(116, 91).addBox(-4.9113F, 1.7156F, -3.8312F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.125F)), PartPose.offset(-2.175F, 0.0F, 0.0F));

		PartDefinition ShoulderL_17_45_0eedefc7_r1 = rShoulder.addOrReplaceChild("ShoulderL_17_45_0eedefc7_r1", CubeListBuilder.create().texOffs(0, 129).addBox(-3.2098F, -2.7968F, -3.7687F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.1875F)), PartPose.offsetAndRotation(-0.9469F, 0.7347F, -0.0938F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderL_17_45_0eedefc7_r2 = rShoulder.addOrReplaceChild("ShoulderL_17_45_0eedefc7_r2", CubeListBuilder.create().texOffs(54, 115).addBox(-4.0155F, -3.4911F, -3.875F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1345F));

		PartDefinition rElbow = rShoulder.addOrReplaceChild("rElbow", CubeListBuilder.create().texOffs(0, 161).addBox(-2.3125F, 1.5F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(79, 151).addBox(-3.0625F, 5.0F, 0.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(113, 38).addBox(-3.0625F, 0.0F, -7.5F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(46, 155).addBox(-2.5625F, 11.0F, -6.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0125F, 7.75F, 3.75F));

		PartDefinition leftArm = whole.addOrReplaceChild("leftArm", CubeListBuilder.create(), PartPose.offset(6.6031F, -3.867F, 0.5994F));

		PartDefinition rShoulder2 = leftArm.addOrReplaceChild("rShoulder2", CubeListBuilder.create().texOffs(116, 91).mirror().addBox(-3.0887F, 1.7156F, -3.8312F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.125F)).mirror(false), PartPose.offset(2.175F, 0.0F, 0.0F));

		PartDefinition ShoulderL_18_45_0eedefc8_r1 = rShoulder2.addOrReplaceChild("ShoulderL_18_45_0eedefc8_r1", CubeListBuilder.create().texOffs(0, 129).mirror().addBox(-2.7902F, -2.7968F, -3.7687F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.1875F)).mirror(false), PartPose.offsetAndRotation(0.9469F, 0.7347F, -0.0938F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderL_18_45_0eedefc8_r2 = rShoulder2.addOrReplaceChild("ShoulderL_18_45_0eedefc8_r2", CubeListBuilder.create().texOffs(54, 115).mirror().addBox(-3.9845F, -3.4911F, -3.875F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1345F));

		PartDefinition rElbow2 = rShoulder2.addOrReplaceChild("rElbow2", CubeListBuilder.create().texOffs(0, 161).mirror().addBox(-2.6875F, 1.5F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(79, 151).mirror().addBox(-2.9375F, 5.0F, 0.0F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(113, 38).mirror().addBox(-2.9375F, 0.0F, -7.5F, 6.0F, 11.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(46, 155).mirror().addBox(-2.4375F, 11.0F, -6.5F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.0125F, 7.75F, 3.75F));

		PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(83, 134).addBox(-5.0125F, 0.2917F, -1.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.9656F, 10.8413F, -0.9006F));

		PartDefinition leftLeg2 = leftLeg.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(116, 123).addBox(-3.025F, -0.4167F, 0.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9875F, 10.7083F, -1.5F));

		PartDefinition leftBoot = leftLeg2.addOrReplaceChild("leftBoot", CubeListBuilder.create().texOffs(142, 46).addBox(-22.9F, 15.1F, -3.1F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.2F))
		.texOffs(162, 150).addBox(-21.35F, 15.2698F, -4.3852F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 163).addBox(-19.975F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(55, 163).addBox(-22.225F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(20.025F, -6.0833F, 3.0F));

		PartDefinition lShin = leftBoot.addOrReplaceChild("lShin", CubeListBuilder.create().texOffs(19, 151).addBox(-23.125F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(115, 157).addBox(-21.125F, -4.25F, -1.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(78, 161).addBox(-19.0F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.025F, 11.375F, -2.975F, -0.0873F, 0.0F, 0.0F));

		PartDefinition lShin3 = leftBoot.addOrReplaceChild("lShin3", CubeListBuilder.create().texOffs(162, 141).addBox(-23.075F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(161, 81).addBox(-21.075F, -3.5F, -1.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 14).addBox(-18.95F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.025F, 15.125F, 3.025F, 0.0873F, 0.0F, 0.0F));

		PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(138, 15).addBox(-0.9875F, 0.2917F, -1.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.2594F, 10.8413F, -0.9006F));

		PartDefinition rightLeg2 = rightLeg.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(29, 129).addBox(-2.975F, -0.4167F, 0.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9875F, 10.7083F, -1.5F));

		PartDefinition rightBoot = rightLeg2.addOrReplaceChild("rightBoot", CubeListBuilder.create().texOffs(54, 145).addBox(16.9F, 15.1F, -3.1F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.2F))
		.texOffs(163, 32).addBox(18.35F, 15.2698F, -4.3852F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 62).addBox(17.975F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 68).addBox(20.225F, 15.2698F, -4.0102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-20.025F, -6.0833F, 3.0F));

		PartDefinition lrhin = rightBoot.addOrReplaceChild("lrhin", CubeListBuilder.create().texOffs(87, 161).addBox(21.125F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(126, 158).addBox(18.125F, -4.25F, -1.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 120).addBox(17.0F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.025F, 11.375F, -2.975F, -0.0873F, 0.0F, 0.0F));

		PartDefinition rshin2 = rightBoot.addOrReplaceChild("rshin2", CubeListBuilder.create().texOffs(15, 163).addBox(21.075F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 132).addBox(18.075F, -3.5F, -1.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 23).addBox(16.95F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.025F, 15.125F, 3.025F, 0.0873F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	public void translateToWeapon(PoseStack poseStack) {
		whole.translateAndRotate(poseStack);
		rightArm.translateAndRotate(poseStack);
		rShoulder.translateAndRotate(poseStack);
		rElbow.translateAndRotate(poseStack);
		poseStack.translate(0.0D, 0.78D, -0.4D);

	}

	public void translateToLeftWeapon(PoseStack poseStack) {
		whole.translateAndRotate(poseStack);
		leftArm.translateAndRotate(poseStack);
		lShoulder.translateAndRotate(poseStack);
		lElbow.translateAndRotate(poseStack);
		poseStack.translate(0.05D, 0.42D, -0.03D);
	}

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
                               int packedColor) {
        whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
    }

    @Override
    public void setupAnim(VesperTheEveningStarEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
                          float headPitch) {
		float partialTick = HLClientUtils.getPartialTicks();
        float frame = entity.tickCount + partialTick;
		resetCombatPose();
		this.hood.visible = VesperEveningStarPresentationRules.isHoodVisible(
				entity.isHoodRemoved(), entity.getHoodRemovalTick());
		this.hair.visible = entity.isHoodRemoved();

		if (entity.isAwaitingAbsorption()) {
			applyAuthoredAnimation(VesperTheEveningStarAnimations.DEFEAT,
					entity.getDownedTicks() + partialTick);
			return;
		}
		if (entity.isHoodRemovalActive()) {
			applyAuthoredAnimation(VesperTheEveningStarAnimations.IDLE, frame);
			applyAuthoredAnimation(VesperTheEveningStarAnimations.REMOVE_HOOD,
					entity.getHoodRemovalTick() + partialTick);
			return;
		}

		VesperWeaponAction action = entity.getWeaponAction();
		boolean authoredAction = action != VesperWeaponAction.NONE
				&& applyAuthoredAction(entity, action, partialTick);

		if (!authoredAction && action == VesperWeaponAction.NONE) {
			if (entity.isRaging()) {
				applyAuthoredAnimation(VesperTheEveningStarAnimations.RAGE_IDLE, frame);
			} else if (entity.getActiveTendency() == EnumBloodTendency.TENEBRIS) {
				float stanceTick = entity.getStanceTick() + partialTick;
				applyAuthoredAnimation(stanceTick < VesperEveningStarPresentationRules.HOOD_REMOVAL_TICKS
						? VesperTheEveningStarAnimations.STANCE_TENEBRIS
						: VesperTheEveningStarAnimations.IDLE_TENEBRIS,
						stanceTick < VesperEveningStarPresentationRules.HOOD_REMOVAL_TICKS ? stanceTick : frame);
			} else {
				applyAuthoredAnimation(limbSwingAmount > 0.02F
						? VesperTheEveningStarAnimations.WALK
						: VesperTheEveningStarAnimations.IDLE, frame);
				applyWeaponIdle(entity.getActiveTendency(), frame);
			}
		} else if (!authoredAction) {
			applyWeaponActionPose(entity, frame);
		}

		if (!authoredAction) {
			applySecondaryClothMotion(frame);
			float stanceTick = entity.getStanceTick() + partialTick;
			if (action == VesperWeaponAction.NONE && !entity.isRaging() && stanceTick < 30.0F) {
				float morph = VesperWeaponAnimationRules.stanceBlend(stanceTick);
				this.rightArm.xRot -= morph * 0.8F;
				this.rightArm.zRot += morph * 0.25F;
				if (entity.getActiveTendency() == EnumBloodTendency.ANIMUS
						|| entity.getActiveTendency() == EnumBloodTendency.MORTEM) {
					applyTwoHandedGrip(entity.getActiveTendency(), 0.0F, morph);
				}
			}
		}
		if (entity.hurtTime > 0) {
			float hitTick = Math.max(0.0F, entity.hurtDuration - entity.hurtTime + partialTick);
			applyAuthoredAnimation(VesperTheEveningStarAnimations.HIT, hitTick);
		}
		applyHeadTracking(netHeadYaw, headPitch);
    }

	private void applySecondaryClothMotion(float frame) {
        this.ClothBack.xRot += Mth.sin(frame * 0.3F) * 0.05F;
        this.ClothBack1.xRot += Mth.sin(frame * 0.5F) * 0.08F;
        this.ClothBack2.xRot += Mth.sin(frame * 0.7F) * 0.12F;
        this.ClothBackR2.xRot += Mth.sin(frame * 0.75F) * 0.10F;
        this.ClothBackL2.xRot += Mth.sin(frame * 0.75F + Mth.PI) * 0.10F;
        this.ClothBackL3.xRot += Mth.sin(frame * 0.8F) * 0.16F;
	}

	private void applyHeadTracking(float netHeadYaw, float headPitch) {
		this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.75F;
		this.head.yRot += Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD * 0.75F;
	}

	private boolean applyAuthoredAction(VesperTheEveningStarEntity entity, VesperWeaponAction action,
			float partialTick) {
		boolean alternate = (entity.getActionVariant() & 1) != 0;
		AnimationDefinition animation = switch (action) {
			case SKY_LANCE -> alternate ? VesperTheEveningStarAnimations.SKY_LANCE_ALTERNATE
					: VesperTheEveningStarAnimations.SKY_LANCE;
			case SICKLE_CYCLONE -> alternate ? VesperTheEveningStarAnimations.SICKLE_CYCLONE_ALTERNATE
					: VesperTheEveningStarAnimations.SICKLE_CYCLONE;
			case SICKLE_POUNCE -> VesperTheEveningStarAnimations.SICKLE_POUNCE;
			case SICKLE_CROSS_REND -> alternate ? VesperTheEveningStarAnimations.SICKLE_CROSS_REND_ALTERNATE
					: VesperTheEveningStarAnimations.SICKLE_CROSS_REND;
			case SICKLE_HOOK -> VesperTheEveningStarAnimations.SICKLE_HOOK;
			case TWIN_REND -> alternate ? VesperTheEveningStarAnimations.TWIN_REND_ALTERNATE
					: VesperTheEveningStarAnimations.TWIN_REND;
			case PREDATOR_POUNCE -> alternate ? VesperTheEveningStarAnimations.PREDATOR_POUNCE_ALTERNATE
					: VesperTheEveningStarAnimations.PREDATOR_POUNCE;
			case SANGUINE_CRESCENTS -> alternate ? VesperTheEveningStarAnimations.SANGUINE_CRESCENTS_ALTERNATE
					: VesperTheEveningStarAnimations.SANGUINE_CRESCENTS;
			default -> null;
		};
		if (animation == null) return false;
		applyAuthoredAnimation(animation, entity.getActionTick() + partialTick);
		return true;
	}

	@Override
	public ModelPart root() {
		return whole;
	}

	private void applyAuthoredAnimation(AnimationDefinition animation, float ticks) {
		KeyframeAnimations.animate(this, animation, (long) (ticks * 50.0F), 1.0F, animationVectorCache);
	}

	private void applyDefeatPose(float downedFrame) {
		float recoil = VesperCombatRules.defeatRecoilProgress(downedFrame);
		float kneel = VesperCombatRules.defeatKneelProgress(downedFrame);
		this.body.xRot -= recoil * 0.22F;
		this.head.xRot -= recoil * 0.16F;
		this.rightArm.xRot += recoil * 0.28F;
		this.leftArm.xRot += recoil * 0.28F;

		this.whole.y = Mth.lerp(kneel, this.whole.y, -8.0F);
		this.head.xRot = pose(this.head.xRot, 0.48F, kneel);
		this.head.yRot = pose(this.head.yRot, 0.0F, kneel);
		this.body.xRot = pose(this.body.xRot, 0.24F, kneel);
		this.body.yRot = pose(this.body.yRot, 0.0F, kneel);
		this.rightArm.xRot = pose(this.rightArm.xRot, -0.38F, kneel);
		this.rightArm.yRot = pose(this.rightArm.yRot, 0.0F, kneel);
		this.rightArm.zRot = pose(this.rightArm.zRot, 0.18F, kneel);
		this.rElbow.xRot = pose(this.rElbow.xRot, -0.72F, kneel);
		this.leftArm.xRot = pose(this.leftArm.xRot, -0.18F, kneel);
		this.leftArm.yRot = pose(this.leftArm.yRot, 0.0F, kneel);
		this.leftArm.zRot = pose(this.leftArm.zRot, -0.14F, kneel);
		this.lElbow.xRot = pose(this.lElbow.xRot, -0.66F, kneel);
		this.rightLeg.xRot = pose(this.rightLeg.xRot, -1.18F, kneel);
		this.rightLeg2.xRot = pose(this.rightLeg2.xRot, 1.82F, kneel);
		this.leftLeg.xRot = pose(this.leftLeg.xRot, 0.18F, kneel);
		this.leftLeg2.xRot = pose(this.leftLeg2.xRot, 0.55F, kneel);
		this.ClothBack.xRot = pose(this.ClothBack.xRot, 0.82F, kneel);
		this.ClothBack1.xRot = pose(this.ClothBack1.xRot, 0.24F, kneel);
		this.ClothBack2.xRot = pose(this.ClothBack2.xRot, 0.18F, kneel);
		this.ClothBackL3.xRot = pose(this.ClothBackL3.xRot, 0.12F, kneel);
	}

	private void resetCombatPose() {
		this.whole.getAllParts().forEach(ModelPart::resetPose);
	}

	private void applyWeaponActionPose(VesperTheEveningStarEntity entity, float frame) {
		VesperWeaponAction action = entity.getWeaponAction();
		float tick = entity.getActionTick() + HLClientUtils.getPartialTicks();
		if (action == VesperWeaponAction.NONE) {
			if (entity.isRaging()) applyRageIdle(frame);
			else applyWeaponIdle(entity.getActiveTendency(), frame);
			return;
		}
		if (entity.isRaging()) applyRageIdle(frame);
		else applyWeaponIdle(entity.getActiveTendency(), frame);
		float blend = VesperWeaponAnimationRules.actionBlend(action, tick);
		float arc = VesperWeaponAnimationRules.swingArc(action, tick);
		float contact = VesperWeaponAnimationRules.contactMotion(action, tick);
		float variantSign = (entity.getActionVariant() & 1) == 0 ? 1.0F : -1.0F;
		switch (action) {
			case ICHIMONJI -> {
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.2F + arc * 1.28F, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.52F + Math.max(0.0F, arc) * 0.22F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, -arc * 0.16F * variantSign, blend);
				this.body.xRot = pose(this.body.xRot, arc * 0.24F, blend);
				this.body.yRot = pose(this.body.yRot, -arc * 0.1F * variantSign, blend);
				applyTwoHandedGrip(action, arc, blend);
			}
			case CROSSCUT -> {
				float slash = contact * variantSign;
				this.body.yRot = pose(this.body.yRot, slash * 0.78F, blend);
				this.body.xRot = pose(this.body.xRot, 0.1F + Math.abs(slash) * 0.08F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.32F + Math.abs(slash) * 0.12F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, slash * 1.08F, blend);
				this.rElbow.zRot = pose(this.rElbow.zRot, slash * 0.18F, blend);
				applyTwoHandedGrip(action, slash, blend);
			}
			case LEAPING_CLEAVE -> {
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.28F + arc * 1.18F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 0.2F + arc * 0.12F * variantSign, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.48F + Math.max(0.0F, arc) * 0.2F, blend);
				this.body.xRot = pose(this.body.xRot, arc * 0.32F, blend);
				this.body.yRot = pose(this.body.yRot, -arc * 0.1F * variantSign, blend);
				applyTwoHandedGrip(action, arc, blend);
			}
			case REAPER_SWEEP -> {
				float sweep = arc * variantSign;
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.38F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, sweep * 1.12F, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.58F + Math.abs(sweep) * 0.18F, blend);
				this.body.xRot = pose(this.body.xRot, 0.08F + Math.abs(sweep) * 0.1F, blend);
				this.body.yRot = pose(this.body.yRot, -sweep * 0.76F, blend);
				applyTwoHandedGrip(action, sweep, blend);
			}
			case SKY_LANCE -> {
				float drive = Math.max(0.0F, arc);
				float brace = Math.max(0.0F, -arc);
				this.body.xRot = pose(this.body.xRot, -brace * 0.22F + drive * 1.02F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.52F - brace * 0.22F + drive * 0.18F, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.32F + drive * 0.14F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.18F + drive * 0.12F, blend);
				this.rightLeg.xRot = pose(this.rightLeg.xRot, 0.62F, blend);
				this.leftLeg.xRot = pose(this.leftLeg.xRot, 0.45F, blend);
				this.ClothBack.xRot = pose(this.ClothBack.xRot, 1.35F, blend);
				this.ClothBack2.xRot = pose(this.ClothBack2.xRot, 0.95F, blend);
			}
			case LANCE_FLURRY, BRANDING_THRUSTS, UPDRAFT_IMPALEMENT -> {
				float thrust = contact * variantSign;
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.3F - thrust * 0.58F, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.42F + Math.max(0.0F, thrust) * 0.2F, blend);
				this.rightArm.yRot = pose(this.rightArm.yRot, thrust * 0.16F, blend);
				this.body.yRot = pose(this.body.yRot, thrust * 0.25F, blend);
				this.body.xRot = pose(this.body.xRot, 0.08F + Math.abs(thrust) * 0.08F, blend);
			}
			case FLAMMEUS_CONCENTRATION -> {
				float windup = Mth.clamp(tick / VesperFlammeusBreathRules.WINDUP_TICKS, 0.0F, 1.0F);
				float breath = tick < VesperFlammeusBreathRules.DURATION_TICKS - 8
						? windup : Mth.clamp((VesperFlammeusBreathRules.DURATION_TICKS - tick) / 8.0F, 0.0F, 1.0F);
				this.body.xRot = pose(this.body.xRot, 0.18F * breath, blend);
				this.head.xRot = pose(this.head.xRot, -0.12F * breath, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.78F * breath, blend);
				this.rightArm.yRot = pose(this.rightArm.yRot, -0.48F * breath, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 0.15F * breath, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.52F * breath, blend);
				this.leftArm.yRot = pose(this.leftArm.yRot, 0.12F * breath, blend);
			}
			case TWIN_REND, PREDATOR_POUNCE -> {
				float slash = contact * variantSign;
				this.body.xRot = pose(this.body.xRot, 0.34F + Math.abs(slash) * 0.16F, blend);
				this.body.yRot = pose(this.body.yRot, slash * 0.2F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.08F + slash * 0.72F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.08F - slash * 0.72F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 0.72F - slash * 0.16F, blend);
				this.leftArm.zRot = pose(this.leftArm.zRot, -0.72F - slash * 0.16F, blend);
			}
			case CONDUCTIVE_VOLLEY, STORM_LOCK -> {
				float surge = 0.5F + 0.5F * Mth.sin(tick * 0.28F);
				this.body.xRot = pose(this.body.xRot, 0.08F + surge * 0.05F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.35F - surge * 0.08F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.35F - surge * 0.08F, blend);
				this.rightArm.yRot = pose(this.rightArm.yRot, -0.42F, blend);
				this.leftArm.yRot = pose(this.leftArm.yRot, 0.62F, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.35F, blend);
				this.lElbow.xRot = pose(this.lElbow.xRot, -0.35F, blend);
			}
			case CHAIN_SWEEP, HOOK_AND_CRUSH -> {
				float swing = VesperWeaponAnimationRules.flailArmMotion(action, tick) * variantSign;
				float follow = VesperWeaponAnimationRules.flailFollowMotion(action, tick) * variantSign;
				this.body.yRot = pose(this.body.yRot, swing * 0.68F, blend);
				this.body.xRot = pose(this.body.xRot, 0.12F + Math.abs(swing) * 0.08F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.32F + Math.abs(swing) * 0.1F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, swing * 0.94F, blend);
				this.rElbow.yRot = pose(this.rElbow.yRot, -follow * 0.32F, blend);
				this.rElbow.zRot = pose(this.rElbow.zRot, follow * 0.1F, blend);
			}
			case MAGNETIC_AXIS, IRON_RETORT -> {
				float load = Math.max(0.0F, -arc);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.55F - load * 0.18F, blend);
				this.rElbow.xRot = pose(this.rElbow.xRot, -0.34F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -0.72F - load * 0.12F, blend);
				this.lElbow.xRot = pose(this.lElbow.xRot, -0.55F, blend);
				this.body.xRot = pose(this.body.xRot, 0.12F + load * 0.08F, blend);
			}
			case SICKLE_CYCLONE -> {
				float spin = VesperWeaponAnimationRules.cycloneSpin(action, tick, variantSign);
				this.body.xRot = pose(this.body.xRot, 0.32F, blend);
				this.body.yRot = spin;
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.18F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.18F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 1.28F, blend);
				this.leftArm.zRot = pose(this.leftArm.zRot, -1.28F, blend);
				this.rightLeg.xRot = pose(this.rightLeg.xRot, 0.48F, blend);
				this.leftLeg.xRot = pose(this.leftLeg.xRot, -0.35F, blend);
			}
			case SICKLE_POUNCE -> {
				float cross = arc * 1.18F;
				this.body.xRot = pose(this.body.xRot, 0.48F + Math.max(0.0F, arc) * 0.42F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.08F + cross * 0.58F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.08F + cross * 0.58F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 1.05F - cross, blend);
				this.leftArm.zRot = pose(this.leftArm.zRot, -1.05F + cross, blend);
				this.rightLeg.xRot = pose(this.rightLeg.xRot, 0.72F, blend);
				this.leftLeg.xRot = pose(this.leftLeg.xRot, 0.62F, blend);
			}
			case SICKLE_CROSS_REND -> {
				float rend = contact * variantSign;
				this.body.xRot = pose(this.body.xRot, 0.28F + Math.abs(rend) * 0.1F, blend);
				this.body.yRot = pose(this.body.yRot, rend * 0.92F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.0F - Math.abs(rend) * 0.62F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.0F - Math.abs(rend) * 0.62F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 0.9F - rend * 1.35F, blend);
				this.leftArm.zRot = pose(this.leftArm.zRot, -0.9F - rend * 1.35F, blend);
			}
			case SICKLE_HOOK -> {
				float draw = Mth.clamp(tick / 14.0F, 0.0F, 1.0F);
				float release = Mth.clamp((tick - 14.0F) / 8.0F, 0.0F, 1.0F);
				this.body.xRot = pose(this.body.xRot, 0.24F - draw * 0.12F, blend);
				this.body.yRot = pose(this.body.yRot, -0.48F * draw + 0.28F * release, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.05F - draw * 0.72F + release * 0.85F, blend);
				this.rightArm.yRot = pose(this.rightArm.yRot, -0.75F * draw + release * 0.9F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 0.82F - release * 0.64F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.18F, blend);
				this.leftArm.zRot = pose(this.leftArm.zRot, -0.88F, blend);
			}
			case SANGUINE_CRESCENTS -> {
				float slash = contact * variantSign;
				this.body.xRot = pose(this.body.xRot, 0.22F + Math.abs(slash) * 0.08F, blend);
				this.body.yRot = pose(this.body.yRot, slash * 0.52F, blend);
				this.rightArm.xRot = pose(this.rightArm.xRot, -1.42F, blend);
				this.leftArm.xRot = pose(this.leftArm.xRot, -1.42F, blend);
				this.rightArm.yRot = pose(this.rightArm.yRot, -0.42F - slash * 0.45F, blend);
				this.leftArm.yRot = pose(this.leftArm.yRot, 0.42F + slash * 0.45F, blend);
				this.rightArm.zRot = pose(this.rightArm.zRot, 0.8F - slash * 1.18F, blend);
				this.leftArm.zRot = pose(this.leftArm.zRot, -0.8F - slash * 1.18F, blend);
			}
			default -> { }
		}
	}

	private void applyRageIdle(float frame) {
		float twitch = Mth.sin(frame * 0.62F) * 0.14F;
		this.body.xRot = 0.38F;
		this.body.yRot = twitch * 0.35F;
		this.rightArm.xRot = -0.95F + twitch;
		this.leftArm.xRot = -0.95F - twitch;
		this.rightArm.zRot = 0.78F;
		this.leftArm.zRot = -0.78F;
		this.rightLeg.xRot += 0.22F;
		this.leftLeg.xRot -= 0.16F;
	}

	private static float pose(float current, float target, float blend) {
		return Mth.lerp(blend, current, target);
	}

	private void applyTwoHandedGrip(EnumBloodTendency tendency, float motion, float blend) {
		VesperWeaponAnimationRules.OffhandGrip grip = VesperWeaponAnimationRules.twoHandedGrip(tendency, motion);
		applyTwoHandedGrip(grip, blend);
	}

	private void applyTwoHandedGrip(VesperWeaponAction action, float motion, float blend) {
		VesperWeaponAnimationRules.OffhandGrip grip = VesperWeaponAnimationRules.twoHandedGrip(action, motion);
		applyTwoHandedGrip(grip, blend);
	}

	private void applyTwoHandedGrip(VesperWeaponAnimationRules.OffhandGrip grip, float blend) {
		this.leftArm.xRot = pose(this.leftArm.xRot, grip.armX(), blend);
		this.leftArm.yRot = pose(this.leftArm.yRot, grip.armY(), blend);
		this.leftArm.zRot = pose(this.leftArm.zRot, grip.armZ(), blend);
		this.lElbow.xRot = pose(this.lElbow.xRot, grip.elbowX(), blend);
		this.lElbow.yRot = pose(this.lElbow.yRot, grip.elbowY(), blend);
		this.lElbow.zRot = pose(this.lElbow.zRot, grip.elbowZ(), blend);
	}

	private void applyWeaponIdle(EnumBloodTendency tendency, float frame) {
		float breathe = Mth.sin(frame * 0.12F) * 0.08F;
		switch (tendency) {
			case ANIMUS -> {
				this.rightArm.xRot -= 0.55F;
				this.body.yRot = breathe;
				applyTwoHandedGrip(tendency, 0.0F, 1.0F);
			}
			case MORTEM -> {
				this.rightArm.xRot -= 0.85F;
				this.body.xRot = 0.12F;
				applyTwoHandedGrip(tendency, 0.0F, 1.0F);
			}
			case LUX -> { this.rightArm.xRot = -1.12F; this.leftArm.xRot = -0.32F; }
			case TENEBRIS -> { this.rightArm.xRot = -0.8F; this.leftArm.xRot = -0.8F; }
			case DUCTILIS -> { this.rightArm.xRot = -1.15F; this.leftArm.xRot = -1.05F; }
			case FLAMMEUS -> { this.rightArm.xRot = -1.05F; this.body.yRot = 0.14F; }
			case CONGEATIO -> { this.rightArm.xRot = -0.85F; this.body.yRot = breathe * 1.8F; }
			case FERRIC -> { this.rightArm.xRot = -1.42F; this.leftArm.xRot = -0.55F; }
		}
	}

}
