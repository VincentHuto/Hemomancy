package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseOneAttack;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperPhaseTransitionRules;
import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperTheCrownedRefusalEntity;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Vector3f;

// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


public class VesperTheCrownedRefusalModel extends HierarchicalModel<VesperTheCrownedRefusalEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			ResourceLocation.fromNamespaceAndPath("hemomancy", "vesper_crowned_refusal"), "main");
	private final ModelPart whole;
	private boolean transitioning;
	private float mountOpacity = 1.0F;
	private float mountScale = 1.0F;
	private final Vector3f animationVectorCache = new Vector3f();
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
	private final ModelPart lowerBody;
	private final ModelPart backAbdomen;
	private final ModelPart tail;
	private final ModelPart tail2;
	private final ModelPart tail3;
	private final ModelPart tail4;
	private final ModelPart tail5;
	private final ModelPart stinger;
	private final ModelPart prongL;
	private final ModelPart prongR;
	private final ModelPart fLeftArm;
	private final ModelPart fLShoulder;
	private final ModelPart fLBicep;
	private final ModelPart fLForearm;
	private final ModelPart fLWrist;
	private final ModelPart flHand;
	private final ModelPart flThumb;
	private final ModelPart fLTopFingers;
	private final ModelPart ffFinger5;
	private final ModelPart ffFinger6;
	private final ModelPart ffFinger1;
	private final ModelPart ffFinger2;
	private final ModelPart ffFinger3;
	private final ModelPart ffFinger4;
	private final ModelPart fRightArm;
	private final ModelPart fLShoulder2;
	private final ModelPart fLBicep2;
	private final ModelPart fLForearm2;
	private final ModelPart fLWrist2;
	private final ModelPart flHand2;
	private final ModelPart flThumb2;
	private final ModelPart fLTopFingers2;
	private final ModelPart ffFinger7;
	private final ModelPart ffFinger8;
	private final ModelPart ffFinger9;
	private final ModelPart ffFinger10;
	private final ModelPart ffFinger11;
	private final ModelPart ffFinger12;
	private final ModelPart lLegs;
	private final ModelPart fLeg;
	private final ModelPart fLHip2;
	private final ModelPart fLFemur2;
	private final ModelPart fLTibia2;
	private final ModelPart fLTibia4;
	private final ModelPart flFoot2;
	private final ModelPart fLeg2;
	private final ModelPart fLHip3;
	private final ModelPart fLFemur3;
	private final ModelPart fLTibia3;
	private final ModelPart fLTibia5;
	private final ModelPart flFoot3;
	private final ModelPart fLeg3;
	private final ModelPart fLHip4;
	private final ModelPart fLFemur4;
	private final ModelPart fLTibia6;
	private final ModelPart fLTibia7;
	private final ModelPart flFoot4;
	private final ModelPart lLegs2;
	private final ModelPart fLeg4;
	private final ModelPart fLHip5;
	private final ModelPart fLFemur5;
	private final ModelPart fLTibia8;
	private final ModelPart fLTibia9;
	private final ModelPart flFoot5;
	private final ModelPart fLeg5;
	private final ModelPart fLHip6;
	private final ModelPart fLFemur6;
	private final ModelPart fLTibia10;
	private final ModelPart fLTibia11;
	private final ModelPart flFoot6;
	private final ModelPart fLeg6;
	private final ModelPart fLHip7;
	private final ModelPart fLFemur7;
	private final ModelPart fLTibia12;
	private final ModelPart fLTibia13;
	private final ModelPart flFoot7;
	private final ModelPart head;
	private final ModelPart upperJaw;
	private final ModelPart lowerJaw;
	private final ModelPart lFang;
	private final ModelPart lFang2;
	private final ModelPart bone;
	private final ModelPart cube_r1;
	private final ModelPart cube_r3;
	private final ModelPart throne;
	private final ModelPart gourd;
	private final ModelPart rope;
	private final ModelPart bone2;
	private final ModelPart bone3;
	private final ModelPart morphJar;
	private final ModelPart rope2;
	private final ModelPart bone4;
	private final ModelPart bone5;
	private final ModelPart backing;

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
		this.lowerBody = this.whole.getChild("lowerBody");
		this.backAbdomen = this.lowerBody.getChild("backAbdomen");
		this.tail = this.backAbdomen.getChild("tail");
		this.tail2 = this.tail.getChild("tail2");
		this.tail3 = this.tail2.getChild("tail3");
		this.tail4 = this.tail3.getChild("tail4");
		this.tail5 = this.tail4.getChild("tail5");
		this.stinger = this.tail5.getChild("stinger");
		this.prongL = this.stinger.getChild("prongL");
		this.prongR = this.stinger.getChild("prongR");
		this.fLeftArm = this.lowerBody.getChild("fLeftArm");
		this.fLShoulder = this.fLeftArm.getChild("fLShoulder");
		this.fLBicep = this.fLShoulder.getChild("fLBicep");
		this.fLForearm = this.fLBicep.getChild("fLForearm");
		this.fLWrist = this.fLForearm.getChild("fLWrist");
		this.flHand = this.fLWrist.getChild("flHand");
		this.flThumb = this.flHand.getChild("flThumb");
		this.fLTopFingers = this.flHand.getChild("fLTopFingers");
		this.ffFinger5 = this.fLTopFingers.getChild("ffFinger5");
		this.ffFinger6 = this.ffFinger5.getChild("ffFinger6");
		this.ffFinger1 = this.fLTopFingers.getChild("ffFinger1");
		this.ffFinger2 = this.ffFinger1.getChild("ffFinger2");
		this.ffFinger3 = this.fLTopFingers.getChild("ffFinger3");
		this.ffFinger4 = this.ffFinger3.getChild("ffFinger4");
		this.fRightArm = this.lowerBody.getChild("fRightArm");
		this.fLShoulder2 = this.fRightArm.getChild("fLShoulder2");
		this.fLBicep2 = this.fLShoulder2.getChild("fLBicep2");
		this.fLForearm2 = this.fLBicep2.getChild("fLForearm2");
		this.fLWrist2 = this.fLForearm2.getChild("fLWrist2");
		this.flHand2 = this.fLWrist2.getChild("flHand2");
		this.flThumb2 = this.flHand2.getChild("flThumb2");
		this.fLTopFingers2 = this.flHand2.getChild("fLTopFingers2");
		this.ffFinger7 = this.fLTopFingers2.getChild("ffFinger7");
		this.ffFinger8 = this.ffFinger7.getChild("ffFinger8");
		this.ffFinger9 = this.fLTopFingers2.getChild("ffFinger9");
		this.ffFinger10 = this.ffFinger9.getChild("ffFinger10");
		this.ffFinger11 = this.fLTopFingers2.getChild("ffFinger11");
		this.ffFinger12 = this.ffFinger11.getChild("ffFinger12");
		this.lLegs = this.lowerBody.getChild("lLegs");
		this.fLeg = this.lLegs.getChild("fLeg");
		this.fLHip2 = this.fLeg.getChild("fLHip2");
		this.fLFemur2 = this.fLHip2.getChild("fLFemur2");
		this.fLTibia2 = this.fLFemur2.getChild("fLTibia2");
		this.fLTibia4 = this.fLTibia2.getChild("fLTibia4");
		this.flFoot2 = this.fLTibia4.getChild("flFoot2");
		this.fLeg2 = this.lLegs.getChild("fLeg2");
		this.fLHip3 = this.fLeg2.getChild("fLHip3");
		this.fLFemur3 = this.fLHip3.getChild("fLFemur3");
		this.fLTibia3 = this.fLFemur3.getChild("fLTibia3");
		this.fLTibia5 = this.fLTibia3.getChild("fLTibia5");
		this.flFoot3 = this.fLTibia5.getChild("flFoot3");
		this.fLeg3 = this.lLegs.getChild("fLeg3");
		this.fLHip4 = this.fLeg3.getChild("fLHip4");
		this.fLFemur4 = this.fLHip4.getChild("fLFemur4");
		this.fLTibia6 = this.fLFemur4.getChild("fLTibia6");
		this.fLTibia7 = this.fLTibia6.getChild("fLTibia7");
		this.flFoot4 = this.fLTibia7.getChild("flFoot4");
		this.lLegs2 = this.lowerBody.getChild("lLegs2");
		this.fLeg4 = this.lLegs2.getChild("fLeg4");
		this.fLHip5 = this.fLeg4.getChild("fLHip5");
		this.fLFemur5 = this.fLHip5.getChild("fLFemur5");
		this.fLTibia8 = this.fLFemur5.getChild("fLTibia8");
		this.fLTibia9 = this.fLTibia8.getChild("fLTibia9");
		this.flFoot5 = this.fLTibia9.getChild("flFoot5");
		this.fLeg5 = this.lLegs2.getChild("fLeg5");
		this.fLHip6 = this.fLeg5.getChild("fLHip6");
		this.fLFemur6 = this.fLHip6.getChild("fLFemur6");
		this.fLTibia10 = this.fLFemur6.getChild("fLTibia10");
		this.fLTibia11 = this.fLTibia10.getChild("fLTibia11");
		this.flFoot6 = this.fLTibia11.getChild("flFoot6");
		this.fLeg6 = this.lLegs2.getChild("fLeg6");
		this.fLHip7 = this.fLeg6.getChild("fLHip7");
		this.fLFemur7 = this.fLHip7.getChild("fLFemur7");
		this.fLTibia12 = this.fLFemur7.getChild("fLTibia12");
		this.fLTibia13 = this.fLTibia12.getChild("fLTibia13");
		this.flFoot7 = this.fLTibia13.getChild("flFoot7");
		this.head = this.lowerBody.getChild("head");
		this.upperJaw = this.head.getChild("upperJaw");
		this.lowerJaw = this.head.getChild("lowerJaw");
		this.lFang = this.lowerJaw.getChild("lFang");
		this.lFang2 = this.lowerJaw.getChild("lFang2");
		this.bone = this.lowerBody.getChild("bone");
		this.cube_r1 = this.bone.getChild("cube_r1");
		this.cube_r3 = this.bone.getChild("cube_r3");
		this.throne = this.lowerBody.getChild("throne");
		this.gourd = this.throne.getChild("gourd");
		this.rope = this.gourd.getChild("rope");
		this.bone2 = this.rope.getChild("bone2");
		this.bone3 = this.rope.getChild("bone3");
		this.morphJar = this.throne.getChild("morphJar");
		this.rope2 = this.morphJar.getChild("rope2");
		this.bone4 = this.rope2.getChild("bone4");
		this.bone5 = this.rope2.getChild("bone5");
		this.backing = this.throne.getChild("backing");
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

		PartDefinition lowerBody = whole.addOrReplaceChild("lowerBody", CubeListBuilder.create().texOffs(294, 0).addBox(-9.25F, -20.6271F, -20.9647F, 21.0F, 8.0F, 41.0F, new CubeDeformation(0.0F))
		.texOffs(166, 84).addBox(-9.25F, -9.0F, -21.0F, 21.0F, 24.0F, 41.0F, new CubeDeformation(0.0F))
		.texOffs(0, 156).addBox(-12.25F, -19.0F, -22.0F, 28.0F, 16.0F, 51.0F, new CubeDeformation(0.0F))
		.texOffs(158, 156).addBox(-13.25F, -3.0F, -22.0F, 28.0F, 15.0F, 43.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 4.0F, 17.2F));

		PartDefinition lowerBody_56_151_29420832_r1 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420832_r1", CubeListBuilder.create().texOffs(194, 390).addBox(3.75F, -16.0F, 18.0F, 8.0F, 28.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.5407F, -5.5901F, 6.5692F, -0.0436F, 0.0F, 0.7418F));

		PartDefinition lowerBody_56_151_29420832_r2 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420832_r2", CubeListBuilder.create().texOffs(100, 357).addBox(-7.75F, -20.0F, -21.0F, 0.0F, 32.0F, 47.0F, new CubeDeformation(0.0F))
		.texOffs(0, 223).addBox(-11.75F, -16.0F, -21.0F, 8.0F, 28.0F, 47.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -5.0F, 0.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition lowerBody_56_151_29420831_r1 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420831_r1", CubeListBuilder.create().texOffs(220, 350).addBox(7.75F, -21.0F, -21.0F, 0.0F, 33.0F, 47.0F, new CubeDeformation(0.0F))
		.texOffs(274, 214).addBox(3.75F, -16.0F, -21.0F, 8.0F, 28.0F, 47.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition lowerBody_56_151_29420831_r2 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420831_r2", CubeListBuilder.create().texOffs(338, 289).addBox(4.0F, -19.0F, -21.5F, 0.0F, 26.0F, 50.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.9254F, -3.882F, -0.5F, 0.0F, 0.0F, 0.3927F));

		PartDefinition lowerBody_56_151_29420830_r1 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420830_r1", CubeListBuilder.create().texOffs(0, 298).addBox(-4.0F, -19.0F, -21.5F, 0.0F, 26.0F, 50.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.4254F, -3.882F, -0.5F, 0.0F, 0.0F, -0.3927F));

		PartDefinition lowerBody_56_151_29420829_r1 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420829_r1", CubeListBuilder.create().texOffs(158, 214).addBox(-4.0F, -14.0F, -21.5F, 8.0F, 21.0F, 50.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(12.9254F, -3.882F, -0.5F, 0.0F, 0.0F, 0.3927F));

		PartDefinition lowerBody_56_151_29420831_r3 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420831_r3", CubeListBuilder.create().texOffs(550, 0).addBox(3.75F, -16.0F, 19.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.7189F, -9.1497F, 5.9873F, -0.2182F, 0.0F, 0.3927F));

		PartDefinition lowerBody_56_151_29420828_r1 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420828_r1", CubeListBuilder.create().texOffs(178, 0).addBox(-4.0F, -14.0F, -21.5F, 8.0F, 21.0F, 50.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-10.4254F, -3.882F, -0.5F, 0.0F, 0.0F, -0.3927F));

		PartDefinition lowerBody_56_151_29420830_r2 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420830_r2", CubeListBuilder.create().texOffs(110, 285).addBox(-11.75F, -16.0F, -20.0F, 8.0F, 25.0F, 47.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -2.0F, 0.0F, 0.0F, 0.0F, -1.0036F));

		PartDefinition lowerBody_56_151_29420828_r2 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420828_r2", CubeListBuilder.create().texOffs(0, 485).addBox(-37.75F, -19.0F, 19.0F, 28.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(25.5F, -13.1843F, 2.7669F, -0.5672F, 0.0F, 0.0F));

		PartDefinition lowerBody_56_151_29420830_r3 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420830_r3", CubeListBuilder.create().texOffs(374, 547).addBox(-11.75F, -16.0F, 19.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.2189F, -9.1497F, 5.9873F, -0.2182F, 0.0F, -0.3927F));

		PartDefinition lowerBody_56_151_29420831_r4 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420831_r4", CubeListBuilder.create().texOffs(194, 357).addBox(-11.75F, -16.0F, 18.0F, 8.0F, 28.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0407F, -5.5901F, 6.5692F, -0.0436F, 0.0F, -0.7418F));

		PartDefinition lowerBody_56_151_29420832_r3 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420832_r3", CubeListBuilder.create().texOffs(68, 448).addBox(-11.75F, -16.0F, 17.0F, 8.0F, 25.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4869F, -3.2658F, 8.057F, -0.1309F, 0.0F, -1.0036F));

		PartDefinition lowerBody_56_151_29420831_r5 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420831_r5", CubeListBuilder.create().texOffs(352, 547).addBox(3.75F, -16.0F, 17.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9869F, -3.2658F, 5.057F, -0.1309F, 0.0F, 1.0036F));

		PartDefinition lowerBody_56_151_29420831_r6 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420831_r6", CubeListBuilder.create().texOffs(330, 547).addBox(-11.75F, -16.0F, -20.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4869F, -3.2658F, -1.057F, 0.1309F, 0.0F, -1.0036F));

		PartDefinition lowerBody_56_151_29420830_r4 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420830_r4", CubeListBuilder.create().texOffs(486, 523).addBox(-11.75F, -16.0F, -22.0F, 8.0F, 28.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6521F, -11.7138F, 2.0754F, 0.4363F, 0.0F, -0.7418F));

		PartDefinition lowerBody_56_151_29420829_r2 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420829_r2", CubeListBuilder.create().texOffs(308, 547).addBox(-11.75F, -16.0F, -22.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.1465F, -13.8035F, 1.9817F, 0.4363F, 0.0F, -0.3927F));

		PartDefinition lowerBody_56_151_29420830_r5 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420830_r5", CubeListBuilder.create().texOffs(202, 546).addBox(3.75F, -16.0F, -20.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.9869F, -3.2658F, -1.057F, 0.1309F, 0.0F, 1.0036F));

		PartDefinition lowerBody_56_151_29420829_r3 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420829_r3", CubeListBuilder.create().texOffs(290, 71).addBox(3.75F, -16.0F, -20.0F, 8.0F, 28.0F, 44.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -2.0F, 0.0F, 0.0F, 0.0F, 1.0036F));

		PartDefinition lowerBody_56_151_29420829_r4 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420829_r4", CubeListBuilder.create().texOffs(380, 479).addBox(3.75F, -16.0F, -22.0F, 8.0F, 28.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(11.1521F, -11.7138F, 2.0754F, 0.4363F, 0.0F, 0.7418F));

		PartDefinition lowerBody_56_151_29420828_r3 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420828_r3", CubeListBuilder.create().texOffs(56, 546).addBox(3.75F, -16.0F, -22.0F, 8.0F, 28.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6465F, -13.8035F, 1.9817F, 0.4363F, 0.0F, 0.3927F));

		PartDefinition lowerBody_56_151_29420826_r1 = lowerBody.addOrReplaceChild("lowerBody_56_151_29420826_r1", CubeListBuilder.create().texOffs(484, 101).addBox(9.75F, -19.0F, -23.0F, 28.0F, 16.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.0F, -13.1843F, 4.2331F, 0.5672F, 0.0F, 0.0F));

		PartDefinition upperBody_176_103_bcdf491f_r1 = lowerBody.addOrReplaceChild("upperBody_176_103_bcdf491f_r1", CubeListBuilder.create().texOffs(408, 125).addBox(-11.0F, -3.0F, -3.5F, 21.0F, 6.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.75F, 11.4378F, 22.9259F, 0.1745F, 0.0F, 0.0F));

		PartDefinition upperBody_176_103_bcdf491f_r2 = lowerBody.addOrReplaceChild("upperBody_176_103_bcdf491f_r2", CubeListBuilder.create().texOffs(394, 94).addBox(-10.0F, -3.0F, -3.5F, 20.0F, 6.0F, 25.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, -17.065F, 22.9611F, -0.1745F, 0.0F, 0.0F));

		PartDefinition backAbdomen = lowerBody.addOrReplaceChild("backAbdomen", CubeListBuilder.create().texOffs(0, 0).addBox(-14.25F, -15.0F, -19.0F, 30.0F, 25.0F, 59.0F, new CubeDeformation(0.0F))
		.texOffs(484, 405).addBox(-11.25F, -14.0F, 40.0F, 24.0F, 22.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(194, 430).addBox(-13.25F, -18.0F, 21.0F, 28.0F, 30.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail = backAbdomen.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(408, 155).addBox(-7.25F, -11.1F, 2.7836F, 16.0F, 16.0F, 22.0F, new CubeDeformation(0.0F))
		.texOffs(464, 193).addBox(-5.25F, -12.0F, 3.7836F, 12.0F, 18.0F, 21.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 5.0F, 32.0F, 0.2618F, 0.0F, 0.0F));

		PartDefinition tail2 = tail.addOrReplaceChild("tail2", CubeListBuilder.create().texOffs(406, 474).addBox(0.75F, -14.0F, 1.1736F, 0.0F, 24.0F, 26.0F, new CubeDeformation(0.0F))
		.texOffs(394, 49).addBox(-3.0152F, -10.0F, -2.8264F, 8.0F, 15.0F, 30.0F, new CubeDeformation(0.0F))
		.texOffs(408, 365).addBox(-6.25F, -9.0F, 1.1736F, 14.0F, 14.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 24.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create().texOffs(224, 475).addBox(1.1563F, -10.8978F, 0.6462F, 0.0F, 19.0F, 26.0F, new CubeDeformation(0.0F))
		.texOffs(94, 436).addBox(-3.25F, -7.8978F, -1.3538F, 8.0F, 12.0F, 28.0F, new CubeDeformation(0.0F))
		.texOffs(418, 0).addBox(-5.25F, -6.8978F, 0.6462F, 12.0F, 11.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 26.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create().texOffs(0, 448).addBox(-3.25F, -5.93F, 0.6541F, 8.0F, 11.0F, 26.0F, new CubeDeformation(0.0F))
		.texOffs(276, 475).addBox(0.75F, -8.68F, 0.6541F, 0.0F, 17.0F, 26.0F, new CubeDeformation(0.0F))
		.texOffs(268, 439).addBox(-3.3408F, -4.68F, -1.3459F, 8.0F, 7.0F, 28.0F, new CubeDeformation(0.0F))
		.texOffs(438, 282).addBox(-5.25F, -4.68F, 0.6541F, 12.0F, 9.0F, 26.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 26.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition tail5 = tail4.addOrReplaceChild("tail5", CubeListBuilder.create().texOffs(340, 439).addBox(-1.25F, -5.0357F, 0.9381F, 4.0F, 11.0F, 29.0F, new CubeDeformation(0.0F))
		.texOffs(166, 469).addBox(0.75F, -7.0357F, 0.9381F, 0.0F, 15.0F, 29.0F, new CubeDeformation(0.0F))
		.texOffs(494, 0).addBox(-3.25F, -4.0357F, 0.9381F, 8.0F, 9.0F, 19.0F, new CubeDeformation(0.0F))
		.texOffs(484, 155).addBox(-1.5893F, -3.0357F, -2.0619F, 6.0F, 7.0F, 22.0F, new CubeDeformation(0.0F))
		.texOffs(498, 142).addBox(-3.25F, -3.0357F, 18.9381F, 8.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 25.5F, 0.5672F, 0.0F, 0.0F));

		PartDefinition stinger = tail5.addOrReplaceChild("stinger", CubeListBuilder.create().texOffs(514, 305).addBox(-3.25F, -2.0357F, 24.9381F, 8.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(576, 159).addBox(-3.25F, -3.0357F, 29.9381F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition prongL = stinger.addOrReplaceChild("prongL", CubeListBuilder.create().texOffs(478, 573).addBox(0.0F, -5.0F, -2.75F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(268, 71).addBox(0.0F, -2.0F, 5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(458, 496).addBox(0.0F, -3.0F, 2.25F, 3.0F, 3.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(2.4107F, 0.9643F, 32.6881F));

		PartDefinition prongR = stinger.addOrReplaceChild("prongR", CubeListBuilder.create().texOffs(396, 573).addBox(-6.0F, -5.0F, -2.75F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(590, 46).addBox(-3.0F, -2.0F, 5.25F, 3.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(64, 503).addBox(-3.0F, -3.0F, 2.25F, 3.0F, 3.0F, 24.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.1607F, 0.9643F, 32.6881F));

		PartDefinition fLeftArm = lowerBody.addOrReplaceChild("fLeftArm", CubeListBuilder.create().texOffs(118, 503).addBox(-6.9167F, -15.9167F, -6.55F, 11.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.25F, 3.0F, -23.85F, -1.0551F, -0.0697F, -1.3281F));

		PartDefinition fLShoulder = fLeftArm.addOrReplaceChild("fLShoulder", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0833F, -0.9167F, -1.55F, -0.6109F, -0.829F, 0.8727F));

		PartDefinition fLBicep = fLShoulder.addOrReplaceChild("fLBicep", CubeListBuilder.create().texOffs(118, 529).addBox(-0.4318F, -3.9571F, -4.2077F, 6.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0398F, 4.0897F, 1.1723F, 0.4486F, 0.5545F, 0.2169F));

		PartDefinition cube_r2 = fLBicep.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(132, 579).addBox(-2.0F, -5.0F, -5.0F, 4.0F, 22.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.6339F, 1.0429F, 1.1771F, 0.0F, 0.3927F, 0.0F));

		PartDefinition cube_r4 = fLBicep.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(136, 476).addBox(-4.0F, -5.0F, -1.0F, 6.0F, 22.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.8685F, 1.0429F, 0.2553F, 0.0F, -0.3927F, 0.0F));

		PartDefinition fLForearm = fLBicep.addOrReplaceChild("fLForearm", CubeListBuilder.create().texOffs(542, 306).addBox(-2.7719F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.34F, 13.9482F, -0.1328F, 0.0F, 0.0F, 0.2618F));

		PartDefinition fLWrist = fLForearm.addOrReplaceChild("fLWrist", CubeListBuilder.create().texOffs(560, 306).addBox(-7.9027F, -2.1621F, -2.8579F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(548, 438).addBox(-8.9027F, -1.1621F, -3.8579F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.1308F, 9.2568F, -0.217F, -0.48F, 0.0F, 0.0F));

		PartDefinition cube_r5 = fLWrist.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(522, 557).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.4923F, -5.1621F, 1.9586F, 0.0F, 0.6109F, 0.0F));

		PartDefinition cube_r6 = fLWrist.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(260, 582).addBox(-5.0F, 4.0F, -1.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6394F, -5.1621F, -0.036F, 0.0F, -0.6109F, 0.0F));

		PartDefinition flHand = fLWrist.addOrReplaceChild("flHand", CubeListBuilder.create(), PartPose.offset(14.641F, -19.3515F, 10.2917F));

		PartDefinition flThumb = flHand.addOrReplaceChild("flThumb", CubeListBuilder.create().texOffs(394, 125).addBox(-0.75F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(94, 374).addBox(-1.75F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-22.5437F, 30.6894F, -10.1496F, 0.0F, 0.0F, -0.0436F));

		PartDefinition fLTopFingers = flHand.addOrReplaceChild("fLTopFingers", CubeListBuilder.create(), PartPose.offsetAndRotation(-17.0437F, 28.6894F, -8.1496F, 0.0F, 0.0F, -0.3491F));

		PartDefinition ffFinger5 = fLTopFingers.addOrReplaceChild("ffFinger5", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 3.25F, 0.0F, 0.0F, -0.1745F));

		PartDefinition cube_r7 = ffFinger5.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(242, 149).addBox(-1.5F, 0.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(372, 194).addBox(-1.5F, 0.5F, -4.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition ffFinger6 = ffFinger5.addOrReplaceChild("ffFinger6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition cube_r8 = ffFinger6.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(258, 149).addBox(-1.5F, -2.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0427F, 7.9571F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition cube_r9 = ffFinger6.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(250, 149).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0578F, 8.3041F, -0.5F, 0.0F, 0.0F, 0.1309F));

		PartDefinition ffFinger1 = fLTopFingers.addOrReplaceChild("ffFinger1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition cube_r10 = ffFinger1.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(194, 149).addBox(-1.5F, 0.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(100, 334).addBox(-1.5F, 0.5F, -4.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition ffFinger2 = ffFinger1.addOrReplaceChild("ffFinger2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition cube_r11 = ffFinger2.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(210, 149).addBox(-1.5F, -2.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0427F, 7.9571F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition cube_r12 = ffFinger2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(202, 149).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0578F, 8.3041F, -0.5F, 0.0F, 0.0F, 0.1309F));

		PartDefinition ffFinger3 = fLTopFingers.addOrReplaceChild("ffFinger3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -3.25F, 0.0F, 0.0F, -0.1745F));

		PartDefinition cube_r13 = ffFinger3.addOrReplaceChild("cube_r13", CubeListBuilder.create().texOffs(218, 149).addBox(-1.5F, 0.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(100, 342).addBox(-1.5F, 0.5F, -4.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition ffFinger4 = ffFinger3.addOrReplaceChild("ffFinger4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition cube_r14 = ffFinger4.addOrReplaceChild("cube_r14", CubeListBuilder.create().texOffs(234, 149).addBox(-1.5F, -2.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0427F, 7.9571F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition cube_r15 = ffFinger4.addOrReplaceChild("cube_r15", CubeListBuilder.create().texOffs(226, 149).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0578F, 8.3041F, -0.5F, 0.0F, 0.0F, 0.1309F));

		PartDefinition fRightArm = lowerBody.addOrReplaceChild("fRightArm", CubeListBuilder.create().texOffs(360, 521).addBox(-4.0833F, -15.9167F, -6.55F, 11.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.75F, 3.0F, -23.85F, -1.0551F, 0.0697F, 1.3281F));

		PartDefinition fLShoulder2 = fRightArm.addOrReplaceChild("fLShoulder2", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.0833F, -0.9167F, -1.55F, -0.6109F, 0.829F, -0.8727F));

		PartDefinition fLBicep2 = fLShoulder2.addOrReplaceChild("fLBicep2", CubeListBuilder.create().texOffs(538, 352).addBox(-5.5682F, -3.9571F, -4.2077F, 6.0F, 22.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0398F, 4.0897F, 1.1723F, 0.4486F, -0.5545F, -0.2169F));

		PartDefinition cube_r16 = fLBicep2.addOrReplaceChild("cube_r16", CubeListBuilder.create().texOffs(580, 61).addBox(-2.0F, -5.0F, -5.0F, 4.0F, 22.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.6339F, 1.0429F, 1.1771F, 0.0F, -0.3927F, 0.0F));

		PartDefinition cube_r17 = fLBicep2.addOrReplaceChild("cube_r17", CubeListBuilder.create().texOffs(32, 558).addBox(-2.0F, -5.0F, -1.0F, 6.0F, 22.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.8685F, 1.0429F, 0.2553F, 0.0F, 0.3927F, 0.0F));

		PartDefinition fLForearm2 = fLBicep2.addOrReplaceChild("fLForearm2", CubeListBuilder.create().texOffs(530, 595).addBox(-1.2281F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.34F, 13.9482F, -0.1328F, 0.0F, 0.0F, -0.2618F));

		PartDefinition fLWrist2 = fLForearm2.addOrReplaceChild("fLWrist2", CubeListBuilder.create().texOffs(572, 556).addBox(1.9027F, -2.1621F, -2.8579F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(118, 559).addBox(0.9027F, -1.1621F, -3.8579F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.1308F, 9.2568F, -0.217F, -0.48F, 0.0F, 0.0F));

		PartDefinition cube_r18 = fLWrist2.addOrReplaceChild("cube_r18", CubeListBuilder.create().texOffs(494, 592).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.4923F, -5.1621F, 1.9586F, 0.0F, -0.6109F, 0.0F));

		PartDefinition cube_r19 = fLWrist2.addOrReplaceChild("cube_r19", CubeListBuilder.create().texOffs(284, 582).addBox(-3.0F, 4.0F, -1.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6394F, -5.1621F, -0.036F, 0.0F, 0.6109F, 0.0F));

		PartDefinition flHand2 = fLWrist2.addOrReplaceChild("flHand2", CubeListBuilder.create(), PartPose.offset(-14.641F, -19.3515F, 10.2917F));

		PartDefinition flThumb2 = flHand2.addOrReplaceChild("flThumb2", CubeListBuilder.create().texOffs(394, 133).addBox(-1.25F, -0.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(94, 381).addBox(-1.25F, -0.5F, 0.0F, 3.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(22.5437F, 30.6894F, -10.1496F, 0.0F, 0.0F, 0.0436F));

		PartDefinition fLTopFingers2 = flHand2.addOrReplaceChild("fLTopFingers2", CubeListBuilder.create(), PartPose.offsetAndRotation(17.0437F, 28.6894F, -8.1496F, 0.0F, 0.0F, 0.3491F));

		PartDefinition ffFinger7 = fLTopFingers2.addOrReplaceChild("ffFinger7", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 3.25F, 0.0F, 0.0F, 0.1745F));

		PartDefinition cube_r20 = ffFinger7.addOrReplaceChild("cube_r20", CubeListBuilder.create().texOffs(266, 149).addBox(-2.5F, 0.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(372, 202).addBox(-1.5F, 0.5F, -4.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition ffFinger8 = ffFinger7.addOrReplaceChild("ffFinger8", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r21 = ffFinger8.addOrReplaceChild("cube_r21", CubeListBuilder.create().texOffs(148, 278).addBox(-2.5F, -2.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0427F, 7.9571F, 1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition cube_r22 = ffFinger8.addOrReplaceChild("cube_r22", CubeListBuilder.create().texOffs(274, 149).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0578F, 8.3041F, -0.5F, 0.0F, 0.0F, -0.1309F));

		PartDefinition ffFinger9 = fLTopFingers2.addOrReplaceChild("ffFinger9", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition cube_r23 = ffFinger9.addOrReplaceChild("cube_r23", CubeListBuilder.create().texOffs(282, 149).addBox(-2.5F, 0.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(384, 49).addBox(-1.5F, 0.5F, -4.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition ffFinger10 = ffFinger9.addOrReplaceChild("ffFinger10", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r24 = ffFinger10.addOrReplaceChild("cube_r24", CubeListBuilder.create().texOffs(384, 65).addBox(-2.5F, -2.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0427F, 7.9571F, 1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition cube_r25 = ffFinger10.addOrReplaceChild("cube_r25", CubeListBuilder.create().texOffs(100, 350).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0578F, 8.3041F, -0.5F, 0.0F, 0.0F, -0.1309F));

		PartDefinition ffFinger11 = fLTopFingers2.addOrReplaceChild("ffFinger11", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, -3.25F, 0.0F, 0.0F, 0.1745F));

		PartDefinition cube_r26 = ffFinger11.addOrReplaceChild("cube_r26", CubeListBuilder.create().texOffs(384, 282).addBox(-2.5F, 0.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(384, 57).addBox(-1.5F, 0.5F, -4.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition ffFinger12 = ffFinger11.addOrReplaceChild("ffFinger12", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition cube_r27 = ffFinger12.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(400, 282).addBox(-2.5F, -2.5F, -3.0F, 4.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0427F, 7.9571F, 1.0F, 0.0F, 0.0F, -0.1309F));

		PartDefinition cube_r28 = ffFinger12.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(392, 282).addBox(-1.5F, -2.5F, -2.0F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0578F, 8.3041F, -0.5F, 0.0F, 0.0F, -0.1309F));

		PartDefinition lLegs = lowerBody.addOrReplaceChild("lLegs", CubeListBuilder.create(), PartPose.offset(13.25F, 7.75F, -4.35F));

		PartDefinition fLeg = lLegs.addOrReplaceChild("fLeg", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.6109F, 0.0F));

		PartDefinition fLHip2 = fLeg.addOrReplaceChild("fLHip2", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0833F, -7.9167F, 8.45F, 0.0F, 0.0F, 0.4363F));

		PartDefinition fLHip_158_476_8040e7b4_r1 = fLHip2.addOrReplaceChild("fLHip_158_476_8040e7b4_r1", CubeListBuilder.create().texOffs(56, 530).addBox(-3.0833F, -4.1821F, -5.1F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(548, 91).addBox(12.9167F, -4.1821F, -5.1F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(178, 71).addBox(-6.0833F, -3.1821F, -4.1F, 39.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -7.0F, 0.0F, 0.0F, -0.6981F));

		PartDefinition fLHip_168_676_8060e7b6_r1 = fLHip2.addOrReplaceChild("fLHip_168_676_8060e7b6_r1", CubeListBuilder.create().texOffs(324, 61).addBox(12.9167F, -4.1821F, -4.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0971F, 4.6925F, -7.5889F, -0.7418F, 0.0F, -0.6981F));

		PartDefinition fLHip_158_576_8050e7b5_r1 = fLHip2.addOrReplaceChild("fLHip_158_576_8050e7b5_r1", CubeListBuilder.create().texOffs(294, 61).addBox(12.9167F, -4.1821F, -5.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1054F, 2.2991F, -5.5366F, 0.7854F, 0.0F, -0.6981F));

		PartDefinition fLFemur2 = fLHip2.addOrReplaceChild("fLFemur2", CubeListBuilder.create().texOffs(540, 159).addBox(-1.4318F, 9.0429F, -5.2077F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(512, 496).addBox(-1.4318F, -7.9571F, -6.2077F, 9.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(244, 520).addBox(-0.4318F, -8.9571F, -4.2077F, 6.0F, 27.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(244, 555).addBox(0.5682F, -13.9571F, -2.2077F, 4.0F, 32.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.9602F, -12.9103F, -7.8277F, 0.0F, 0.0F, -1.1781F));

		PartDefinition cube_r29 = fLFemur2.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(506, 573).addBox(-3.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4469F, -4.9571F, -1.9858F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r30 = fLFemur2.addOrReplaceChild("cube_r30", CubeListBuilder.create().texOffs(178, 573).addBox(-5.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.154F, -4.9571F, 2.2776F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r31 = fLFemur2.addOrReplaceChild("cube_r31", CubeListBuilder.create().texOffs(332, 578).addBox(-5.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4469F, 1.0429F, 2.671F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r32 = fLFemur2.addOrReplaceChild("cube_r32", CubeListBuilder.create().texOffs(576, 538).addBox(-5.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6895F, 1.0429F, 1.1563F, 0.0F, 0.7854F, 0.0F));

		PartDefinition fLTibia2 = fLFemur2.addOrReplaceChild("fLTibia2", CubeListBuilder.create().texOffs(372, 592).addBox(-2.7719F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.34F, 13.9482F, -0.1329F, 0.0F, 0.0F, 0.2618F));

		PartDefinition fLTibia4 = fLTibia2.addOrReplaceChild("fLTibia4", CubeListBuilder.create().texOffs(260, 562).addBox(-22.5437F, 17.1894F, -13.1496F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(554, 483).addBox(-23.5437F, 18.1894F, -14.1496F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(18.7719F, -10.0947F, 10.0748F));

		PartDefinition cube_r33 = fLTibia4.addOrReplaceChild("cube_r33", CubeListBuilder.create().texOffs(586, 123).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.665F, 14.1894F, -8.2709F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r34 = fLTibia4.addOrReplaceChild("cube_r34", CubeListBuilder.create().texOffs(586, 29).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.1295F, 14.1894F, -8.4927F, 0.0F, 0.7854F, 0.0F));

		PartDefinition flFoot2 = fLTibia4.addOrReplaceChild("flFoot2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r35 = flFoot2.addOrReplaceChild("cube_r35", CubeListBuilder.create().texOffs(512, 592).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.6897F, 43.4642F, -10.1496F, 0.0F, 0.0F, 0.48F));

		PartDefinition cube_r36 = flFoot2.addOrReplaceChild("cube_r36", CubeListBuilder.create().texOffs(586, 327).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0437F, 31.6894F, -10.1496F, 0.0F, 0.0F, -0.3054F));

		PartDefinition cube_r37 = flFoot2.addOrReplaceChild("cube_r37", CubeListBuilder.create().texOffs(148, 254).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.2937F, 32.1894F, -9.6496F, 0.0F, 0.0F, 0.6109F));

		PartDefinition fLeg2 = lLegs.addOrReplaceChild("fLeg2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition fLHip3 = fLeg2.addOrReplaceChild("fLHip3", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0833F, -7.9167F, 17.45F, 0.0F, 0.0F, 0.4363F));

		PartDefinition fLHip_158_576_8050e7b5_r2 = fLHip3.addOrReplaceChild("fLHip_158_576_8050e7b5_r2", CubeListBuilder.create().texOffs(530, 202).addBox(-3.0833F, -4.1821F, -5.1F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(550, 31).addBox(12.9167F, -4.1821F, -5.1F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(294, 49).addBox(-6.0833F, -3.1821F, -4.1F, 39.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -7.0F, 0.0F, 0.0F, -0.6981F));

		PartDefinition fLHip_178_777_8070e7b7_r1 = fLHip3.addOrReplaceChild("fLHip_178_777_8070e7b7_r1", CubeListBuilder.create().texOffs(268, 430).addBox(12.9167F, -4.1821F, -4.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0971F, 4.6925F, -7.5889F, -0.7418F, 0.0F, -0.6981F));

		PartDefinition fLHip_168_676_8060e7b6_r2 = fLHip3.addOrReplaceChild("fLHip_168_676_8060e7b6_r2", CubeListBuilder.create().texOffs(354, 61).addBox(12.9167F, -4.1821F, -5.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1054F, 2.2991F, -5.5366F, 0.7854F, 0.0F, -0.6981F));

		PartDefinition fLFemur3 = fLHip3.addOrReplaceChild("fLFemur3", CubeListBuilder.create().texOffs(540, 538).addBox(-1.4318F, 9.0429F, -5.2077F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(160, 513).addBox(-1.4318F, -7.9571F, -6.2077F, 9.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(98, 530).addBox(-0.4318F, -11.9571F, -2.2077F, 6.0F, 30.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 523).addBox(-0.4318F, -8.9571F, -4.2077F, 6.0F, 27.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.9602F, -12.9103F, -7.8276F, 0.0F, 0.0F, -1.1781F));

		PartDefinition cube_r38 = fLFemur3.addOrReplaceChild("cube_r38", CubeListBuilder.create().texOffs(574, 370).addBox(-3.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4469F, -4.9571F, -1.9858F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r39 = fLFemur3.addOrReplaceChild("cube_r39", CubeListBuilder.create().texOffs(574, 234).addBox(-5.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.154F, -4.9571F, 2.2776F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r40 = fLFemur3.addOrReplaceChild("cube_r40", CubeListBuilder.create().texOffs(578, 389).addBox(-5.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4469F, 1.0429F, 2.671F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r41 = fLFemur3.addOrReplaceChild("cube_r41", CubeListBuilder.create().texOffs(360, 578).addBox(-5.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6895F, 1.0429F, 1.1563F, 0.0F, 0.7854F, 0.0F));

		PartDefinition fLTibia3 = fLFemur3.addOrReplaceChild("fLTibia3", CubeListBuilder.create().texOffs(214, 593).addBox(-2.7719F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.34F, 13.9482F, -0.1329F, 0.0F, 0.0F, 0.2618F));

		PartDefinition fLTibia5 = fLTibia3.addOrReplaceChild("fLTibia5", CubeListBuilder.create().texOffs(284, 562).addBox(-22.5437F, 17.1894F, -13.1496F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(556, 266).addBox(-23.5437F, 18.1894F, -14.1496F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(18.7719F, -10.0947F, 10.0748F));

		PartDefinition cube_r42 = fLTibia5.addOrReplaceChild("cube_r42", CubeListBuilder.create().texOffs(590, 347).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.665F, 14.1894F, -8.2709F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r43 = fLTibia5.addOrReplaceChild("cube_r43", CubeListBuilder.create().texOffs(474, 588).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.1295F, 14.1894F, -8.4928F, 0.0F, 0.7854F, 0.0F));

		PartDefinition flFoot3 = fLTibia5.addOrReplaceChild("flFoot3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r44 = flFoot3.addOrReplaceChild("cube_r44", CubeListBuilder.create().texOffs(76, 593).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.6897F, 43.4642F, -10.1496F, 0.0F, 0.0F, 0.48F));

		PartDefinition cube_r45 = flFoot3.addOrReplaceChild("cube_r45", CubeListBuilder.create().texOffs(586, 477).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0437F, 31.6894F, -10.1496F, 0.0F, 0.0F, -0.3054F));

		PartDefinition cube_r46 = flFoot3.addOrReplaceChild("cube_r46", CubeListBuilder.create().texOffs(148, 266).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.2937F, 32.1894F, -9.6496F, 0.0F, 0.0F, 0.6109F));

		PartDefinition fLeg3 = lLegs.addOrReplaceChild("fLeg3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 18.0F, 0.0F, -0.6109F, 0.0F));

		PartDefinition fLHip4 = fLeg3.addOrReplaceChild("fLHip4", CubeListBuilder.create(), PartPose.offsetAndRotation(3.0833F, -7.9167F, 8.45F, 0.0F, 0.0F, 0.4363F));

		PartDefinition fLHip_168_676_8060e7b6_r3 = fLHip4.addOrReplaceChild("fLHip_168_676_8060e7b6_r3", CubeListBuilder.create().texOffs(532, 218).addBox(-3.0833F, -4.1821F, -5.1F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(550, 334).addBox(12.9167F, -4.1821F, -5.1F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(418, 37).addBox(-6.0833F, -3.1821F, -4.1F, 39.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -7.0F, 0.0F, 0.0F, -0.6981F));

		PartDefinition fLHip_188_888_8080e8b8_r1 = fLHip4.addOrReplaceChild("fLHip_188_888_8080e8b8_r1", CubeListBuilder.create().texOffs(484, 432).addBox(12.9167F, -4.1821F, -4.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0971F, 4.6925F, -7.5889F, -0.7418F, 0.0F, -0.6981F));

		PartDefinition fLHip_178_777_8070e7b7_r2 = fLHip4.addOrReplaceChild("fLHip_178_777_8070e7b7_r2", CubeListBuilder.create().texOffs(484, 184).addBox(12.9167F, -4.1821F, -5.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.1054F, 2.2991F, -5.5366F, 0.7854F, 0.0F, -0.6981F));

		PartDefinition fLFemur4 = fLHip4.addOrReplaceChild("fLFemur4", CubeListBuilder.create().texOffs(542, 402).addBox(-1.4318F, 9.0429F, -5.2076F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(514, 279).addBox(-1.4318F, -7.9571F, -6.2076F, 9.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(512, 522).addBox(-0.4318F, -8.9571F, -4.2076F, 6.0F, 27.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(78, 546).addBox(-0.4318F, -10.9571F, -2.2076F, 6.0F, 29.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.9602F, -12.9103F, -7.8277F, 0.0F, 0.0F, -1.1781F));

		PartDefinition cube_r47 = fLFemur4.addOrReplaceChild("cube_r47", CubeListBuilder.create().texOffs(576, 458).addBox(-3.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.4469F, -4.9571F, -1.9858F, 0.0F, -2.3562F, 0.0F));

		PartDefinition cube_r48 = fLFemur4.addOrReplaceChild("cube_r48", CubeListBuilder.create().texOffs(150, 576).addBox(-5.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.154F, -4.9571F, 2.2776F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r49 = fLFemur4.addOrReplaceChild("cube_r49", CubeListBuilder.create().texOffs(578, 417).addBox(-5.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.4469F, 1.0429F, 2.671F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r50 = fLFemur4.addOrReplaceChild("cube_r50", CubeListBuilder.create().texOffs(578, 403).addBox(-5.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.6895F, 1.0429F, 1.1563F, 0.0F, 0.7854F, 0.0F));

		PartDefinition fLTibia6 = fLFemur4.addOrReplaceChild("fLTibia6", CubeListBuilder.create().texOffs(594, 105).addBox(-2.7719F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.34F, 13.9482F, -0.1328F, 0.0F, 0.0F, 0.2618F));

		PartDefinition fLTibia7 = fLTibia6.addOrReplaceChild("fLTibia7", CubeListBuilder.create().texOffs(566, 350).addBox(-22.5437F, 17.1894F, -13.1496F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(556, 286).addBox(-23.5437F, 18.1894F, -14.1496F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(18.7719F, -10.0947F, 10.0748F));

		PartDefinition cube_r51 = fLTibia7.addOrReplaceChild("cube_r51", CubeListBuilder.create().texOffs(578, 590).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.665F, 14.1894F, -8.2709F, 0.0F, -0.7854F, 0.0F));

		PartDefinition cube_r52 = fLTibia7.addOrReplaceChild("cube_r52", CubeListBuilder.create().texOffs(590, 497).addBox(-2.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-14.1295F, 14.1894F, -8.4928F, 0.0F, 0.7854F, 0.0F));

		PartDefinition flFoot4 = fLTibia7.addOrReplaceChild("flFoot4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r53 = flFoot4.addOrReplaceChild("cube_r53", CubeListBuilder.create().texOffs(534, 483).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.6897F, 43.4642F, -10.1496F, 0.0F, 0.0F, 0.48F));

		PartDefinition cube_r54 = flFoot4.addOrReplaceChild("cube_r54", CubeListBuilder.create().texOffs(588, 262).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-18.0437F, 31.6894F, -10.1496F, 0.0F, 0.0F, -0.3054F));

		PartDefinition cube_r55 = flFoot4.addOrReplaceChild("cube_r55", CubeListBuilder.create().texOffs(290, 143).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-24.2937F, 32.1894F, -9.6496F, 0.0F, 0.0F, 0.6109F));

		PartDefinition lLegs2 = lowerBody.addOrReplaceChild("lLegs2", CubeListBuilder.create(), PartPose.offset(-10.75F, 7.75F, -4.35F));

		PartDefinition fLeg4 = lLegs2.addOrReplaceChild("fLeg4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.6109F, 0.0F));

		PartDefinition fLHip5 = fLeg4.addOrReplaceChild("fLHip5", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0833F, -7.9167F, 8.45F, 0.0F, 0.0F, -0.4363F));

		PartDefinition fLHip_158_576_8050e7b5_r3 = fLHip5.addOrReplaceChild("fLHip_158_576_8050e7b5_r3", CubeListBuilder.create().texOffs(532, 234).addBox(-9.9167F, -4.1821F, -5.1F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(554, 503).addBox(-22.9167F, -4.1821F, -5.1F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(464, 267).addBox(-32.9167F, -3.1821F, -4.1F, 39.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -7.0F, 0.0F, 0.0F, 0.6981F));

		PartDefinition fLHip_178_777_8070e7b7_r3 = fLHip5.addOrReplaceChild("fLHip_178_777_8070e7b7_r3", CubeListBuilder.create().texOffs(574, 253).addBox(-22.9167F, -4.1821F, -4.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0971F, 4.6925F, -7.5889F, -0.7418F, 0.0F, 0.6981F));

		PartDefinition fLHip_168_676_8060e7b6_r4 = fLHip5.addOrReplaceChild("fLHip_168_676_8060e7b6_r4", CubeListBuilder.create().texOffs(572, 20).addBox(-22.9167F, -4.1821F, -5.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1054F, 2.2991F, -5.5366F, 0.7854F, 0.0F, 0.6981F));

		PartDefinition fLFemur5 = fLHip5.addOrReplaceChild("fLFemur5", CubeListBuilder.create().texOffs(542, 420).addBox(-6.5682F, 9.0429F, -5.2077F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(276, 518).addBox(-7.5682F, -7.9571F, -6.2077F, 9.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(28, 523).addBox(-5.5682F, -8.9571F, -4.2077F, 6.0F, 27.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(430, 555).addBox(-4.5682F, -13.9571F, -2.2077F, 4.0F, 32.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.9602F, -12.9103F, -7.8277F, 0.0F, 0.0F, 1.1781F));

		PartDefinition cube_r56 = fLFemur5.addOrReplaceChild("cube_r56", CubeListBuilder.create().texOffs(554, 576).addBox(-4.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4469F, -4.9571F, -1.9858F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r57 = fLFemur5.addOrReplaceChild("cube_r57", CubeListBuilder.create().texOffs(530, 576).addBox(-2.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.154F, -4.9571F, 2.2776F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r58 = fLFemur5.addOrReplaceChild("cube_r58", CubeListBuilder.create().texOffs(578, 519).addBox(-3.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4469F, 1.0429F, 2.671F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r59 = fLFemur5.addOrReplaceChild("cube_r59", CubeListBuilder.create().texOffs(446, 578).addBox(-3.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6895F, 1.0429F, 1.1563F, 0.0F, -0.7854F, 0.0F));

		PartDefinition fLTibia8 = fLFemur5.addOrReplaceChild("fLTibia8", CubeListBuilder.create().texOffs(594, 173).addBox(-1.2281F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.34F, 13.9482F, -0.1329F, 0.0F, 0.0F, -0.2618F));

		PartDefinition fLTibia9 = fLTibia8.addOrReplaceChild("fLTibia9", CubeListBuilder.create().texOffs(570, 177).addBox(16.5437F, 17.1894F, -13.1496F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(540, 556).addBox(15.5437F, 18.1894F, -14.1496F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.7719F, -10.0947F, 10.0748F));

		PartDefinition cube_r60 = fLTibia9.addOrReplaceChild("cube_r60", CubeListBuilder.create().texOffs(424, 591).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.665F, 14.1894F, -8.2709F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r61 = fLTibia9.addOrReplaceChild("cube_r61", CubeListBuilder.create().texOffs(230, 591).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.1295F, 14.1894F, -8.4927F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flFoot5 = fLTibia9.addOrReplaceChild("flFoot5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r62 = flFoot5.addOrReplaceChild("cube_r62", CubeListBuilder.create().texOffs(94, 593).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.6897F, 43.4642F, -10.1496F, 0.0F, 0.0F, -0.48F));

		PartDefinition cube_r63 = flFoot5.addOrReplaceChild("cube_r63", CubeListBuilder.create().texOffs(588, 282).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0437F, 31.6894F, -10.1496F, 0.0F, 0.0F, 0.3054F));

		PartDefinition cube_r64 = flFoot5.addOrReplaceChild("cube_r64", CubeListBuilder.create().texOffs(100, 298).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(24.2937F, 32.1894F, -9.6496F, 0.0F, 0.0F, -0.6109F));

		PartDefinition fLeg5 = lLegs2.addOrReplaceChild("fLeg5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition fLHip6 = fLeg5.addOrReplaceChild("fLHip6", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0833F, -7.9167F, 17.45F, 0.0F, 0.0F, -0.4363F));

		PartDefinition fLHip_168_676_8060e7b6_r5 = fLHip6.addOrReplaceChild("fLHip_168_676_8060e7b6_r5", CubeListBuilder.create().texOffs(532, 250).addBox(-9.9167F, -4.1821F, -5.1F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(486, 557).addBox(-22.9167F, -4.1821F, -5.1F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(470, 49).addBox(-32.9167F, -3.1821F, -4.1F, 39.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -7.0F, 0.0F, 0.0F, 0.6981F));

		PartDefinition fLHip_188_888_8080e8b8_r2 = fLHip6.addOrReplaceChild("fLHip_188_888_8080e8b8_r2", CubeListBuilder.create().texOffs(584, 87).addBox(-22.9167F, -4.1821F, -4.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0971F, 4.6925F, -7.5889F, -0.7418F, 0.0F, 0.6981F));

		PartDefinition fLHip_178_777_8070e7b7_r4 = fLHip6.addOrReplaceChild("fLHip_178_777_8070e7b7_r4", CubeListBuilder.create().texOffs(580, 449).addBox(-22.9167F, -4.1821F, -5.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1054F, 2.2991F, -5.5366F, 0.7854F, 0.0F, 0.6981F));

		PartDefinition fLFemur6 = fLHip6.addOrReplaceChild("fLFemur6", CubeListBuilder.create().texOffs(544, 73).addBox(-6.5682F, 9.0429F, -5.2077F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(202, 520).addBox(-7.5682F, -7.9571F, -6.2077F, 9.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(182, 539).addBox(-5.5682F, -11.9571F, -2.2077F, 6.0F, 30.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(458, 523).addBox(-5.5682F, -8.9571F, -4.2077F, 6.0F, 27.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.9602F, -12.9103F, -7.8276F, 0.0F, 0.0F, 1.1781F));

		PartDefinition cube_r65 = fLFemur6.addOrReplaceChild("cube_r65", CubeListBuilder.create().texOffs(0, 578).addBox(-4.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4469F, -4.9571F, -1.9858F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r66 = fLFemur6.addOrReplaceChild("cube_r66", CubeListBuilder.create().texOffs(52, 577).addBox(-2.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.154F, -4.9571F, 2.2776F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r67 = fLFemur6.addOrReplaceChild("cube_r67", CubeListBuilder.create().texOffs(76, 579).addBox(-3.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4469F, 1.0429F, 2.671F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r68 = fLFemur6.addOrReplaceChild("cube_r68", CubeListBuilder.create().texOffs(578, 576).addBox(-3.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6895F, 1.0429F, 1.1563F, 0.0F, -0.7854F, 0.0F));

		PartDefinition fLTibia10 = fLFemur6.addOrReplaceChild("fLTibia10", CubeListBuilder.create().texOffs(594, 183).addBox(-1.2281F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.34F, 13.9482F, -0.1329F, 0.0F, 0.0F, -0.2618F));

		PartDefinition fLTibia11 = fLTibia10.addOrReplaceChild("fLTibia11", CubeListBuilder.create().texOffs(572, 0).addBox(16.5437F, 17.1894F, -13.1496F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 558).addBox(15.5437F, 18.1894F, -14.1496F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.7719F, -10.0947F, 10.0748F));

		PartDefinition cube_r69 = fLTibia11.addOrReplaceChild("cube_r69", CubeListBuilder.create().texOffs(332, 592).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.665F, 14.1894F, -8.2709F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r70 = fLTibia11.addOrReplaceChild("cube_r70", CubeListBuilder.create().texOffs(174, 592).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.1295F, 14.1894F, -8.4928F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flFoot6 = fLTibia11.addOrReplaceChild("flFoot6", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r71 = flFoot6.addOrReplaceChild("cube_r71", CubeListBuilder.create().texOffs(112, 593).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.6897F, 43.4642F, -10.1496F, 0.0F, 0.0F, -0.48F));

		PartDefinition cube_r72 = flFoot6.addOrReplaceChild("cube_r72", CubeListBuilder.create().texOffs(388, 588).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0437F, 31.6894F, -10.1496F, 0.0F, 0.0F, 0.3054F));

		PartDefinition cube_r73 = flFoot6.addOrReplaceChild("cube_r73", CubeListBuilder.create().texOffs(100, 310).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(24.2937F, 32.1894F, -9.6496F, 0.0F, 0.0F, -0.6109F));

		PartDefinition fLeg6 = lLegs2.addOrReplaceChild("fLeg6", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 18.0F, 0.0F, 0.6109F, 0.0F));

		PartDefinition fLHip7 = fLeg6.addOrReplaceChild("fLHip7", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0833F, -7.9167F, 8.45F, 0.0F, 0.0F, -0.4363F));

		PartDefinition fLHip_178_777_8070e7b7_r5 = fLHip7.addOrReplaceChild("fLHip_178_777_8070e7b7_r5", CubeListBuilder.create().texOffs(534, 467).addBox(-9.9167F, -4.1821F, -5.1F, 13.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(558, 107).addBox(-22.9167F, -4.1821F, -5.1F, 10.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(470, 61).addBox(-32.9167F, -3.1821F, -4.1F, 39.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -7.0F, 0.0F, 0.0F, 0.6981F));

		PartDefinition fLHip_199_999_9090e9b9_r1 = fLHip7.addOrReplaceChild("fLHip_199_999_9090e9b9_r1", CubeListBuilder.create().texOffs(584, 306).addBox(-22.9167F, -4.1821F, -4.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0971F, 4.6925F, -7.5889F, -0.7418F, 0.0F, 0.6981F));

		PartDefinition fLHip_188_888_8080e8b8_r3 = fLHip7.addOrReplaceChild("fLHip_188_888_8080e8b8_r3", CubeListBuilder.create().texOffs(584, 96).addBox(-22.9167F, -4.1821F, -5.1F, 10.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.1054F, 2.2991F, -5.5366F, 0.7854F, 0.0F, 0.6981F));

		PartDefinition fLFemur7 = fLHip7.addOrReplaceChild("fLFemur7", CubeListBuilder.create().texOffs(272, 544).addBox(-6.5682F, 9.0429F, -5.2076F, 8.0F, 8.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(318, 521).addBox(-7.5682F, -7.9571F, -6.2076F, 9.0F, 14.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(402, 524).addBox(-5.5682F, -8.9571F, -4.2076F, 6.0F, 27.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(224, 546).addBox(-5.5682F, -10.9571F, -2.2076F, 6.0F, 29.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-17.9602F, -12.9103F, -7.8277F, 0.0F, 0.0F, 1.1781F));

		PartDefinition cube_r74 = fLFemur7.addOrReplaceChild("cube_r74", CubeListBuilder.create().texOffs(308, 578).addBox(-4.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-5.4469F, -4.9571F, -1.9858F, 0.0F, 2.3562F, 0.0F));

		PartDefinition cube_r75 = fLFemur7.addOrReplaceChild("cube_r75", CubeListBuilder.create().texOffs(578, 140).addBox(-2.0F, -3.0F, -7.0F, 7.0F, 14.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.154F, -4.9571F, 2.2776F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r76 = fLFemur7.addOrReplaceChild("cube_r76", CubeListBuilder.create().texOffs(202, 579).addBox(-3.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.4469F, 1.0429F, 2.671F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r77 = fLFemur7.addOrReplaceChild("cube_r77", CubeListBuilder.create().texOffs(104, 579).addBox(-3.0F, 8.0F, -6.0F, 8.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.6895F, 1.0429F, 1.1563F, 0.0F, -0.7854F, 0.0F));

		PartDefinition fLTibia12 = fLFemur7.addOrReplaceChild("fLTibia12", CubeListBuilder.create().texOffs(148, 595).addBox(-1.2281F, 3.0947F, -2.0748F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.34F, 13.9482F, -0.1328F, 0.0F, 0.0F, -0.2618F));

		PartDefinition fLTibia13 = fLTibia12.addOrReplaceChild("fLTibia13", CubeListBuilder.create().texOffs(572, 197).addBox(16.5437F, 17.1894F, -13.1496F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(446, 558).addBox(15.5437F, 18.1894F, -14.1496F, 8.0F, 12.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-18.7719F, -10.0947F, 10.0748F));

		PartDefinition cube_r78 = fLTibia13.addOrReplaceChild("cube_r78", CubeListBuilder.create().texOffs(444, 592).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(17.665F, 14.1894F, -8.2709F, 0.0F, 0.7854F, 0.0F));

		PartDefinition cube_r79 = fLTibia13.addOrReplaceChild("cube_r79", CubeListBuilder.create().texOffs(352, 592).addBox(-3.0F, 4.0F, -5.0F, 5.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.1295F, 14.1894F, -8.4928F, 0.0F, -0.7854F, 0.0F));

		PartDefinition flFoot7 = fLTibia13.addOrReplaceChild("flFoot7", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r80 = flFoot7.addOrReplaceChild("cube_r80", CubeListBuilder.create().texOffs(98, 564).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 7.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.6897F, 43.4642F, -10.1496F, 0.0F, 0.0F, -0.48F));

		PartDefinition cube_r81 = flFoot7.addOrReplaceChild("cube_r81", CubeListBuilder.create().texOffs(406, 588).addBox(-1.5F, -6.5F, -3.0F, 3.0F, 14.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(18.0437F, 31.6894F, -10.1496F, 0.0F, 0.0F, 0.3054F));

		PartDefinition cube_r82 = flFoot7.addOrReplaceChild("cube_r82", CubeListBuilder.create().texOffs(100, 322).addBox(-1.0F, -3.0F, -1.5F, 2.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(24.2937F, 32.1894F, -9.6496F, 0.0F, 0.0F, -0.6109F));

		PartDefinition head = lowerBody.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offsetAndRotation(0.75F, -6.6271F, -26.9647F, 0.3491F, 0.0F, 0.0F));

		PartDefinition upperJaw = head.addOrReplaceChild("upperJaw", CubeListBuilder.create().texOffs(540, 522).addBox(-7.0F, -2.5767F, -16.6589F, 14.0F, 11.0F, 5.0F, new CubeDeformation(0.0F))
		.texOffs(580, 431).addBox(-2.0F, -2.4344F, -18.7873F, 4.0F, 11.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(484, 73).addBox(-8.0F, -6.5767F, -11.6589F, 16.0F, 14.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(166, 436).addBox(-6.5F, -6.5767F, -11.6589F, 0.0F, 17.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(574, 217).addBox(-6.5F, -6.5767F, -11.6589F, 13.0F, 17.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(110, 254).addBox(-2.0F, -7.5767F, -12.6589F, 4.0F, 16.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.4233F, 0.6589F));

		PartDefinition upperBody_48_195_64112220_r1 = upperJaw.addOrReplaceChild("upperBody_48_195_64112220_r1", CubeListBuilder.create().texOffs(314, 350).addBox(-2.0F, -6.5F, -3.5F, 4.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.2951F, -11.928F, 0.6981F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112221_r1 = upperJaw.addOrReplaceChild("upperBody_48_195_64112221_r1", CubeListBuilder.create().texOffs(300, 194).addBox(-9.0F, -5.5F, -2.5F, 18.0F, 2.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.1475F, -10.9018F, 0.48F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112220_r2 = upperJaw.addOrReplaceChild("upperBody_48_195_64112220_r2", CubeListBuilder.create().texOffs(458, 474).addBox(-9.0F, -5.5F, -2.5F, 18.0F, 2.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.1475F, -11.9018F, 0.48F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112219_r1 = upperJaw.addOrReplaceChild("upperBody_48_195_64112219_r1", CubeListBuilder.create().texOffs(488, 392).addBox(-7.0F, -5.5F, -2.5F, 14.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.1475F, -11.9018F, 0.48F, 0.0F, 0.0F));

		PartDefinition lowerJaw = head.addOrReplaceChild("lowerJaw", CubeListBuilder.create().texOffs(0, 505).addBox(-7.0F, -2.0F, -12.5F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(166, 149).addBox(-7.0F, -5.0F, -12.5F, 14.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(558, 123).addBox(-6.7F, -5.0F, -12.5F, 0.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(150, 559).addBox(6.8F, -5.0F, -12.5F, 0.0F, 3.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(530, 184).addBox(-3.0F, -0.25F, -12.5F, 6.0F, 4.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -1.5F, 0.6981F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112220_r3 = lowerJaw.addOrReplaceChild("upperBody_48_195_64112220_r3", CubeListBuilder.create().texOffs(540, 142).addBox(-3.0F, -2.0F, -6.5F, 6.0F, 4.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.5005F, -5.7038F, -0.2618F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112219_r2 = lowerJaw.addOrReplaceChild("upperBody_48_195_64112219_r2", CubeListBuilder.create().texOffs(506, 317).addBox(-7.0F, -2.0F, -6.5F, 14.0F, 4.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.7505F, -5.7038F, -0.2618F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112220_r4 = lowerJaw.addOrReplaceChild("upperBody_48_195_64112220_r4", CubeListBuilder.create().texOffs(194, 593).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.3422F, 0.0F, -13.4674F, 0.0F, 0.4363F, 0.0F));

		PartDefinition upperBody_48_195_64112219_r3 = lowerJaw.addOrReplaceChild("upperBody_48_195_64112219_r3", CubeListBuilder.create().texOffs(560, 61).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.3422F, 0.0F, -13.4674F, 0.0F, -0.4363F, 0.0F));

		PartDefinition lFang = lowerJaw.addOrReplaceChild("lFang", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112221_r2 = lFang.addOrReplaceChild("upperBody_48_195_64112221_r2", CubeListBuilder.create().texOffs(546, 595).addBox(-7.0F, -2.0F, 0.5F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 0.4582F, -20.4165F, 0.1745F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112220_r5 = lFang.addOrReplaceChild("upperBody_48_195_64112220_r5", CubeListBuilder.create().texOffs(538, 382).addBox(-7.0F, -2.0F, -9.5F, 2.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 1.7505F, -5.7038F, -0.2618F, 0.0F, 0.0F));

		PartDefinition lFang2 = lowerJaw.addOrReplaceChild("lFang2", CubeListBuilder.create(), PartPose.offset(-16.0F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112222_r1 = lFang2.addOrReplaceChild("upperBody_48_195_64112222_r1", CubeListBuilder.create().texOffs(562, 595).addBox(-7.0F, -2.0F, 0.5F, 2.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 0.4582F, -20.4165F, 0.1745F, 0.0F, 0.0F));

		PartDefinition upperBody_48_195_64112221_r3 = lFang2.addOrReplaceChild("upperBody_48_195_64112221_r3", CubeListBuilder.create().texOffs(146, 539).addBox(-7.0F, -2.0F, -9.5F, 2.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(14.0F, 1.7505F, -5.7038F, -0.2618F, 0.0F, 0.0F));

		PartDefinition bone = lowerBody.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(-0.4F, 9.5F, -24.8F, 0.6109F, 0.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(480, 442).addBox(-8.85F, -5.8834F, -1.0068F, 21.0F, 12.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.1015F, 0.3548F, 0.0873F, 0.0F, 0.0F));

		PartDefinition cube_r1_180_130_f47dbc18_r1 = cube_r1.addOrReplaceChild("cube_r1_180_130_f47dbc18_r1", CubeListBuilder.create().texOffs(0, 84).addBox(-14.85F, -16.8834F, 3.9932F, 33.0F, 22.0F, 50.0F, new CubeDeformation(0.0F))
		.texOffs(406, 442).addBox(-11.5F, -15.8834F, 1.9932F, 27.0F, 22.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(464, 232).addBox(-8.35F, -15.8834F, -1.0068F, 21.0F, 22.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5F, -9.9218F, 3.6961F, -0.6981F, 0.0F, 0.0F));

		PartDefinition cube_r1_180_130_f47dbc14_r1 = cube_r1.addOrReplaceChild("cube_r1_180_130_f47dbc14_r1", CubeListBuilder.create().texOffs(300, 143).addBox(-5.85F, -5.8834F, -1.0068F, 15.0F, 12.0F, 39.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0782F, 3.6961F, -0.6981F, 0.0F, 0.0F));

		PartDefinition cube_r3 = bone.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(498, 121).addBox(-8.85F, 29.0656F, 8.6007F, 21.0F, 12.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -49.8985F, 0.3548F, 0.0873F, 0.0F, 0.0F));

		PartDefinition throne = lowerBody.addOrReplaceChild("throne", CubeListBuilder.create().texOffs(396, 559).addBox(8.25F, -2.5F, -9.3F, 5.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(68, 476).addBox(-9.5F, -4.5F, -3.3F, 22.0F, 15.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(508, 28).addBox(-6.5F, 0.5F, -8.3F, 15.0F, 10.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(488, 359).addBox(-7.5F, -15.5F, 1.3F, 18.0F, 26.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(380, 511).addBox(8.25F, -4.5F, -6.3F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(514, 432).addBox(-10.0F, -4.5F, -6.3F, 5.0F, 2.0F, 7.0F, new CubeDeformation(0.0F))
		.texOffs(560, 47).addBox(-10.0F, -2.5F, -9.3F, 5.0F, 4.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(24, 584).addBox(-9.1F, 1.5F, -8.3F, 4.0F, 3.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(584, 315).addBox(8.35F, 1.5F, -8.3F, 4.0F, 3.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offset(0.1F, -30.5F, 0.05F));

		PartDefinition gourd = throne.addOrReplaceChild("gourd", CubeListBuilder.create().texOffs(25, 660).addBox(-4.25F, -7.3055F, -3.75F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 667).addBox(-4.25F, -1.8055F, -3.75F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(34, 640).addBox(-5.3375F, -1.2431F, -3.1875F, 10.0F, 9.0F, 7.0F, new CubeDeformation(-0.375F))
		.texOffs(17, 649).addBox(-4.3125F, -1.2431F, -3.1875F, 1.0F, 9.0F, 6.0F, new CubeDeformation(-0.375F))
		.texOffs(5, 650).addBox(-2.8125F, -1.2431F, 3.3125F, 6.0F, 9.0F, 1.0F, new CubeDeformation(-0.375F))
		.texOffs(2, 694).addBox(-3.5625F, -1.2431F, -4.7875F, 7.0F, 9.0F, 10.0F, new CubeDeformation(-0.375F))
		.texOffs(4, 633).addBox(-3.75F, -9.6805F, -3.0F, 7.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
		.texOffs(2, 658).addBox(-3.0F, 5.3194F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F))
		.texOffs(26, 633).addBox(-3.0F, -5.3055F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(17.4F, -0.9278F, 0.9F, 0.1903F, -0.0247F, -0.2417F));

		PartDefinition rope = gourd.addOrReplaceChild("rope", CubeListBuilder.create().texOffs(28, 621).addBox(3.575F, -1.7583F, -2.6583F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.05F)), PartPose.offset(-0.25F, -3.0347F, 0.0833F));

		PartDefinition bone2 = rope.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(32, 631).addBox(-10.6079F, -1.675F, -0.1625F, 11.0F, 3.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(3.25F, -0.0833F, 2.6667F, 0.0F, 0.3927F, 0.0F));

		PartDefinition bone3 = rope.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(24, 622).addBox(-9.7557F, -1.692F, -0.2386F, 10.0F, 3.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(3.25F, -0.0833F, -2.5833F, 0.0554F, 0.0064F, 0.0646F));

		PartDefinition morphJar = throne.addOrReplaceChild("morphJar", CubeListBuilder.create().texOffs(339, 701).addBox(-6.375F, -11.4583F, -5.625F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(314, 708).addBox(-6.375F, -2.7083F, -5.625F, 12.0F, 15.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(331, 690).addBox(-6.1563F, -1.5521F, -4.5938F, 1.0F, 13.0F, 9.0F, new CubeDeformation(-0.375F))
		.texOffs(319, 691).addBox(-4.4063F, -1.5521F, 5.1563F, 9.0F, 13.0F, 1.0F, new CubeDeformation(-0.375F))
		.texOffs(340, 674).addBox(-4.75F, -8.2083F, -4.25F, 9.0F, 6.0F, 9.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(3.4F, -8.9278F, 16.9F, -1.783F, -1.453F, 1.5399F));

		PartDefinition rope2 = morphJar.addOrReplaceChild("rope2", CubeListBuilder.create().texOffs(342, 662).addBox(5.3875F, -3.1125F, -4.0125F, 0.0F, 5.0F, 8.0F, new CubeDeformation(0.05F)), PartPose.offset(-0.375F, -4.5521F, 0.125F));

		PartDefinition bone4 = rope2.addOrReplaceChild("bone4", CubeListBuilder.create().texOffs(346, 672).addBox(-16.5447F, -2.9875F, 0.1999F, 17.0F, 5.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(4.875F, -0.125F, 4.0F, 0.0F, 0.3927F, 0.0F));

		PartDefinition bone5 = rope2.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(338, 663).addBox(-14.6085F, -3.013F, -0.3829F, 15.0F, 5.0F, 0.0F, new CubeDeformation(0.05F)), PartPose.offsetAndRotation(4.875F, -0.125F, -3.875F, 0.0554F, 0.0064F, 0.0646F));

		PartDefinition backing = throne.addOrReplaceChild("backing", CubeListBuilder.create().texOffs(110, 223).addBox(-8.0F, -30.2569F, -4.1284F, 19.0F, 27.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(328, 479).addBox(-8.5F, -26.5F, -0.5F, 20.0F, 36.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 1024, 1024);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight,
			int packedOverlay, int packedColor) {
		if (!transitioning) {
			whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
			return;
		}
		renderVesperOnly(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		if (mountOpacity > 0.01F) {
			int baseAlpha = packedColor >>> 24;
			int mountColor = (Mth.clamp(Math.round(baseAlpha * mountOpacity), 0, 255) << 24)
					| (packedColor & 0x00FFFFFF);
			renderMountAssembly(poseStack, vertexConsumer, packedLight, packedOverlay, mountColor);
		}
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

	public void renderVesperOnly(PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int packedColor) {
		poseStack.pushPose();
		whole.translateAndRotate(poseStack);
		vesper.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		poseStack.popPose();
	}

	public void renderMountAssembly(PoseStack poseStack, VertexConsumer vertexConsumer,
			int packedLight, int packedOverlay, int packedColor) {
		boolean riderVisible = vesper.visible;
		vesper.visible = false;
		poseStack.pushPose();
		whole.translateAndRotate(poseStack);
		poseStack.scale(mountScale, Math.max(0.02F, mountScale * 0.72F), mountScale);
		lowerBody.render(poseStack, vertexConsumer, packedLight, packedOverlay, packedColor);
		poseStack.popPose();
		vesper.visible = riderVisible;
	}

	@Override
	public void setupAnim(VesperTheCrownedRefusalEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		whole.getAllParts().forEach(ModelPart::resetPose);
		float partialTick = HLClientUtils.getPartialTicks();
		float frame = entity.tickCount + partialTick;
		this.transitioning = entity.getTransitionTick() > 0;
		this.mountOpacity = 1.0F;
		this.mountScale = 1.0F;

		if (transitioning) {
			this.animate(entity.transformationAnimationState,
					VesperTheCrownedRefusalAnimations.TRANSFORMATION, ageInTicks);
			float transitionFrame = entity.transformationAnimationState.getAccumulatedTime() / 50.0F;
			float absorption = smooth(VesperPhaseTransitionRules.absorptionProgress(transitionFrame));
			this.mountOpacity = 1.0F - absorption;
			this.mountScale = Math.max(0.03F, 1.0F - absorption * 0.97F);
			return;
		}

		float attackFrame = entity.getAttackTick() + partialTick;
		AnimationDefinition animation = switch (entity.getAttack()) {
			case ROYAL_SCUTTLE -> VesperTheCrownedRefusalAnimations.ROYAL_SCUTTLE;
			case PINCER_VICE -> VesperTheCrownedRefusalAnimations.PINCER_VICE;
			case STINGER_SCRIPT -> VesperTheCrownedRefusalAnimations.STINGER_SCRIPT;
			case BROOD_TRAMPLE -> VesperTheCrownedRefusalAnimations.BROOD_TRAMPLE;
			case PUPPET_MUSTER -> VesperTheCrownedRefusalAnimations.PUPPET_MUSTER;
			case IDLE -> entity.getActiveAnchor() >= 0
					? VesperTheCrownedRefusalAnimations.VULNERABLE
					: limbSwingAmount > 0.02F
							? VesperTheCrownedRefusalAnimations.WALK
							: VesperTheCrownedRefusalAnimations.IDLE;
		};
		applyAuthoredAnimation(animation, entity.getAttack() == VesperPhaseOneAttack.IDLE ? frame : attackFrame);
		this.head2.xRot += headPitch * Mth.DEG_TO_RAD * 0.5F;
		this.head2.yRot += Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD * 0.5F;
	}

	@Override
	public ModelPart root() {
		return whole;
	}

	private void applyAuthoredAnimation(AnimationDefinition animation, float ticks) {
		KeyframeAnimations.animate(this, animation, (long) (ticks * 50.0F), 1.0F, animationVectorCache);
	}

	private static void animateLeg(ModelPart leg, ModelPart femur, ModelPart tibia,
			float limbSwing, float limbSwingAmount, float phase) {
		float stride = Mth.cos(limbSwing * 0.6662F + phase) * 0.72F * limbSwingAmount;
		leg.xRot += stride * 0.35F;
		femur.xRot += stride;
		tibia.xRot += Math.abs(Mth.sin(limbSwing * 0.6662F + phase)) * 0.72F * limbSwingAmount;
	}

	private void animateRiderCloth(float frame) {
		this.ClothBack.xRot += Mth.sin(frame * 0.24F) * 0.035F;
		this.ClothBack1.xRot += Mth.sin(frame * 0.31F) * 0.055F;
		this.ClothBack2.xRot += Mth.sin(frame * 0.39F) * 0.075F;
		this.ClothBackR2.xRot += Mth.sin(frame * 0.43F) * 0.06F;
		this.ClothBackL2.xRot += Mth.sin(frame * 0.43F + Mth.PI) * 0.06F;
	}

	private void applyTransitionPose(VesperTheCrownedRefusalEntity entity, float frame) {
		float transitionFrame = entity.getTransitionTick() + HLClientUtils.getPartialTicks();
		float dismount = smooth(VesperPhaseTransitionRules.dismountProgress(transitionFrame));
		float collapse = VesperPhaseTransitionRules.collapseProgress(transitionFrame);
		float absorption = smooth(VesperPhaseTransitionRules.absorptionProgress(transitionFrame));

		this.vesper.y += 18.0F * dismount - 20.0F * VesperPhaseTransitionRules.jumpArc(transitionFrame);
		this.vesper.z -= 24.0F * dismount;
		this.vesper.xRot -= 0.16F * Mth.sin(dismount * Mth.PI);
		this.vesper.zRot += 0.08F * Mth.sin(dismount * Mth.PI);
		this.body.xRot += 0.12F * absorption;
		this.leftArm.xRot = Mth.lerp(absorption, this.leftArm.xRot, -1.05F);
		this.leftArm.yRot = Mth.lerp(absorption, this.leftArm.yRot, 0.28F);
		this.rightArm.xRot = Mth.lerp(absorption, this.rightArm.xRot, -1.05F);
		this.rightArm.yRot = Mth.lerp(absorption, this.rightArm.yRot, -0.28F);
		this.head2.xRot = Mth.lerp(absorption, this.head2.xRot, -0.18F);
		this.head2.yRot *= 1.0F - absorption;

		this.lowerBody.y += 10.0F * collapse;
		this.lowerBody.xRot += 0.18F * collapse;
		this.lowerBody.zRot += 0.10F * collapse;
		this.lLegs.zRot += 0.42F * collapse;
		this.lLegs2.zRot -= 0.42F * collapse;
		this.head.xRot += 0.24F * collapse;
		this.head.yRot -= 0.16F * collapse;
		this.head.zRot += 0.42F * collapse;
		this.upperJaw.xRot = Mth.lerp(collapse, this.upperJaw.xRot, -0.18F);
		this.lowerJaw.xRot = Mth.lerp(collapse, this.lowerJaw.xRot, 0.34F);
		this.tail.xRot += 0.34F * collapse;
		this.tail2.xRot += 0.40F * collapse;
		this.tail3.xRot += 0.46F * collapse;
		this.tail4.xRot += 0.52F * collapse;
		this.tail5.xRot = Mth.lerp(collapse, this.tail5.xRot, 1.12F);

		this.mountOpacity = 1.0F - absorption;
		this.mountScale = Math.max(0.03F, 1.0F - absorption * 0.97F);
		this.vesper.y += 8.0F * absorption;
		this.vesper.z -= 10.0F * absorption;
		this.vesper.zRot += Mth.sin(frame * 0.2F) * 0.02F * absorption;
	}

	private static float smooth(float progress) {
		return progress * progress * (3.0F - 2.0F * progress);
	}

}
