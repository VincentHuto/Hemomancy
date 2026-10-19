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
	private final ModelPart rForearm;
	private final ModelPart lForearm;
	private final Vector3f animationVectorCache = new Vector3f();
	private final float[] soleCache = new float[2];
	private final float[] floorCache = new float[2];
	private java.util.List<ModelPart> parts;
	private final VesperEveningStarMoves.Rig rig;
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
		this.body = this.whole.getChild("body");
		this.head = this.body.getChild("head");
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
		this.rightArm = this.body.getChild("rightArm");
		this.rShoulder = this.rightArm.getChild("rShoulder");
		this.rElbow = this.rShoulder.getChild("rElbow");
		this.leftArm = this.body.getChild("leftArm");
		this.rShoulder2 = this.leftArm.getChild("rShoulder2");
		this.rElbow2 = this.rShoulder2.getChild("rElbow2");
		this.lShoulder = this.rShoulder2;
		this.lElbow = this.rElbow2;
		this.rForearm = this.rElbow.getChild("rForearm");
		this.lForearm = this.rElbow2.getChild("lForearm");
		this.leftLeg = this.whole.getChild("leftLeg");
		this.leftLeg2 = this.leftLeg.getChild("leftLeg2");
		this.leftBoot = this.leftLeg2.getChild("leftBoot");
		this.lShin = this.leftLeg2.getChild("lShin");
		this.lShin3 = this.leftLeg2.getChild("lShin3");
		this.rightLeg = this.whole.getChild("rightLeg");
		this.rightLeg2 = this.rightLeg.getChild("rightLeg2");
		this.rightBoot = this.rightLeg2.getChild("rightBoot");
		this.lrhin = this.rightLeg2.getChild("lrhin");
		this.rshin2 = this.rightLeg2.getChild("rshin2");
		this.rig = new VesperEveningStarMoves.Rig(this.whole, this.body, this.head, this.rightArm, this.rForearm,
				this.leftArm, this.lForearm, this.rightLeg, this.rightLeg2, this.leftLeg, this.leftLeg2);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(), PartPose.offset(0.1469F, -9.4663F, -0.5994F));

		PartDefinition body = whole.addOrReplaceChild("body", CubeListBuilder.create().texOffs(51, 21).addBox(-6.5062F, -18.8670F, -2.0284F, 12.0F, 20.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(88, 18).addBox(4.0184F, -18.3968F, -2.7784F, 2.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(0, 92).addBox(-7.4559F, -18.3968F, -2.7784F, 2.0F, 20.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(129, 143).addBox(-6.6563F, -17.6170F, -3.9034F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(67, 155).addBox(2.6437F, -17.6170F, -3.9034F, 3.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(54, 130).addBox(-6.3562F, -17.6170F, 3.7466F, 12.0F, 12.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 48).addBox(-6.6563F, -18.1170F, 3.0716F, 12.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.3594F, 10.0F, -0.3722F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 181).addBox(-6.125F, -9.075F, -5.625F, 12.0F, 12.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.3813F, -19.942F, 1.3466F));

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


		PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(), PartPose.offset(19.4937F, 1.583F, 7.5716F));

		PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(), PartPose.offset(6.0F, 0.0F, -1.5F));

		PartDefinition ClothBackR1 = ClothBack1.addOrReplaceChild("ClothBackR1", CubeListBuilder.create().texOffs(29, 149).addBox(-26.0F, 0.0F, -3.0F, 6.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBackL1 = ClothBack1.addOrReplaceChild("ClothBackL1", CubeListBuilder.create().texOffs(149, 91).addBox(-26.0F, 0.0F, -3.0F, 6.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));

		PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(), PartPose.offsetAndRotation(-3.0F, 12.0F, 0.0F, 0.3054F, 0.0F, 0.0F));

		PartDefinition ClothBackR2 = ClothBack2.addOrReplaceChild("ClothBackR2", CubeListBuilder.create().texOffs(98, 151).addBox(-21.95F, 11.0283F, -4.2151F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackR3 = ClothBack2.addOrReplaceChild("ClothBackR3", CubeListBuilder.create().texOffs(140, 157).addBox(-26.25F, 10.5283F, -4.3151F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL2 = ClothBack2.addOrReplaceChild("ClothBackL2", CubeListBuilder.create().texOffs(163, 38).addBox(-26.05F, 11.0283F, -4.2151F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition ClothBackL3 = ClothBack2.addOrReplaceChild("ClothBackL3", CubeListBuilder.create().texOffs(155, 157).addBox(-24.75F, 10.5283F, -4.3151F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -12.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(), PartPose.offset(5.4438F, 1.508F, 0.9716F));

		PartDefinition SideclothL1 = SideclothL.addOrReplaceChild("SideclothL1", CubeListBuilder.create().texOffs(141, 123).addBox(-20.4375F, -0.3964F, -3.75F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(19.0F, -2.0F, 0.0F, 0.0F, 0.0F, -0.1222F));

		PartDefinition SideclothL2 = SideclothL1.addOrReplaceChild("SideclothL2", CubeListBuilder.create().texOffs(142, 32).addBox(-20.6678F, -6.9576F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3614F, 8.2145F, 0.0F, 0.0F, 0.0F, -0.2967F));

		PartDefinition SideclothL3 = SideclothL2.addOrReplaceChild("SideclothL3", CubeListBuilder.create().texOffs(108, 143).addBox(-14.4319F, -14.7607F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.789F, 3.6096F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition SideclothL4 = body.addOrReplaceChild("SideclothL4", CubeListBuilder.create(), PartPose.offset(-6.9562F, 1.508F, 0.9716F));

		PartDefinition SideclothL5 = SideclothL4.addOrReplaceChild("SideclothL5", CubeListBuilder.create().texOffs(141, 140).addBox(18.4375F, -0.3964F, -3.75F, 2.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-19.0F, -2.0F, 0.0F, 0.0F, 0.0F, 0.1222F));

		PartDefinition SideclothL6 = SideclothL5.addOrReplaceChild("SideclothL6", CubeListBuilder.create().texOffs(145, 106).addBox(18.6678F, -6.9576F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.3614F, 8.2145F, 0.0F, 0.0F, 0.0F, 0.2967F));

		PartDefinition SideclothL7 = SideclothL6.addOrReplaceChild("SideclothL7", CubeListBuilder.create().texOffs(147, 0).addBox(12.4319F, -14.7607F, -3.75F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.789F, 3.6096F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.5063F, -16.3656F, 6.8371F, 0.2182F, 0.0F, 0.0F));

		PartDefinition CloakTL = cloak.addOrReplaceChild("CloakTL", CubeListBuilder.create().texOffs(33, 63).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(6.25F, -0.9813F, -2.8584F, 0.1396F, 0.0F, 0.0F));

		PartDefinition CloakTR = cloak.addOrReplaceChild("CloakTR", CubeListBuilder.create().texOffs(98, 157).addBox(-1.5F, -1.0F, -2.5F, 3.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-4.75F, -0.9813F, -2.8584F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak1 = cloak.addOrReplaceChild("Cloak1", CubeListBuilder.create().texOffs(68, 77).addBox(-27.25F, 3.0F, 1.5F, 14.0F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(21.0F, -2.8227F, -4.127F, 0.1396F, 0.0F, 0.0F));

		PartDefinition Cloak2 = Cloak1.addOrReplaceChild("Cloak2", CubeListBuilder.create().texOffs(113, 58).addBox(-27.25F, -0.2252F, -0.5443F, 14.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 21.0502F, 2.0869F, 0.3069F, 0.0F, 0.0F));

		PartDefinition Cloak3 = Cloak2.addOrReplaceChild("Cloak3", CubeListBuilder.create().texOffs(130, 67).addBox(-27.25F, -0.6727F, -0.2867F, 14.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.2578F, 0.0048F, 0.4466F, 0.0F, 0.0F));

		PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(130, 86).addBox(-6.0F, -20.0F, -4.5F, 12.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(33, 71).addBox(-3.0F, -18.5F, -4.1F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(84, 71).addBox(-3.0F, -21.5F, -4.1F, 6.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5062F, 20.3163F, 1.2216F));

		PartDefinition rightArm = body.addOrReplaceChild("rightArm", CubeListBuilder.create(), PartPose.offset(-7.2563F, -13.867F, 0.9716F));

		PartDefinition rShoulder = rightArm.addOrReplaceChild("rShoulder", CubeListBuilder.create().texOffs(116, 91).addBox(-4.9113F, 1.7156F, -3.8312F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.125F)), PartPose.offset(-2.175F, 0.0F, 0.0F));

		PartDefinition ShoulderL_17_45_0eedefc7_r1 = rShoulder.addOrReplaceChild("ShoulderL_17_45_0eedefc7_r1", CubeListBuilder.create().texOffs(0, 129).addBox(-3.2098F, -2.7968F, -3.7687F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.1875F)), PartPose.offsetAndRotation(-0.9469F, 0.7347F, -0.0938F, 0.0F, 0.0F, 0.4363F));

		PartDefinition ShoulderL_17_45_0eedefc7_r2 = rShoulder.addOrReplaceChild("ShoulderL_17_45_0eedefc7_r2", CubeListBuilder.create().texOffs(54, 115).addBox(-4.0155F, -3.4911F, -3.875F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.25F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1345F));

		PartDefinition rElbow = rShoulder.addOrReplaceChild("rElbow", CubeListBuilder.create().texOffs(0, 161).addBox(-2.3125F, 1.5F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(113, 38).addBox(-3.0625F, 0.0F, -7.5F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0125F, 7.75F, 3.75F));

		rElbow.addOrReplaceChild("rForearm", CubeListBuilder.create().texOffs(113, 43).addBox(-3.0625F, 0.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(79, 151).addBox(-3.0625F, 0.0F, 3.5F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
		.texOffs(46, 155).addBox(-2.5625F, 6.0F, -3.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 5.0F, -3.5F));

		PartDefinition leftArm = body.addOrReplaceChild("leftArm", CubeListBuilder.create(), PartPose.offset(6.2437F, -13.867F, 0.9716F));

		PartDefinition rShoulder2 = leftArm.addOrReplaceChild("rShoulder2", CubeListBuilder.create().texOffs(116, 91).mirror().addBox(-3.0887F, 1.7156F, -3.8312F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.125F)).mirror(false), PartPose.offset(2.175F, 0.0F, 0.0F));

		PartDefinition ShoulderL_18_45_0eedefc8_r1 = rShoulder2.addOrReplaceChild("ShoulderL_18_45_0eedefc8_r1", CubeListBuilder.create().texOffs(0, 129).mirror().addBox(-2.7902F, -2.7968F, -3.7687F, 6.0F, 7.0F, 8.0F, new CubeDeformation(0.1875F)).mirror(false), PartPose.offsetAndRotation(0.9469F, 0.7347F, -0.0938F, 0.0F, 0.0F, -0.4363F));

		PartDefinition ShoulderL_18_45_0eedefc8_r2 = rShoulder2.addOrReplaceChild("ShoulderL_18_45_0eedefc8_r2", CubeListBuilder.create().texOffs(54, 115).mirror().addBox(-3.9845F, -3.4911F, -3.875F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.25F)).mirror(false), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1345F));

		PartDefinition rElbow2 = rShoulder2.addOrReplaceChild("rElbow2", CubeListBuilder.create().texOffs(0, 161).mirror().addBox(-2.6875F, 1.5F, 0.0F, 5.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(113, 38).mirror().addBox(-2.9375F, 0.0F, -7.5F, 6.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(1.0125F, 7.75F, 3.75F));

		rElbow2.addOrReplaceChild("lForearm", CubeListBuilder.create().texOffs(113, 43).mirror().addBox(-2.9375F, 0.0F, -4.0F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(79, 151).mirror().addBox(-2.9375F, 0.0F, 3.5F, 6.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(46, 155).mirror().addBox(-2.4375F, 6.0F, -3.0F, 5.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(0.0F, 5.0F, -3.5F));

		PartDefinition leftLeg = whole.addOrReplaceChild("leftLeg", CubeListBuilder.create().texOffs(83, 134).addBox(-5.0125F, 0.2917F, -1.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(4.9656F, 10.8413F, -0.9006F));

		PartDefinition leftLeg2 = leftLeg.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(116, 123).addBox(-3.025F, -0.4167F, 0.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9875F, 10.7083F, -1.5F));

		PartDefinition leftBoot = leftLeg2.addOrReplaceChild("leftBoot", CubeListBuilder.create().texOffs(142, 46).addBox(-2.8750F, -1.5833F, -3.0000F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.2F))
		.texOffs(162, 150).addBox(-1.3250F, -1.4135F, -4.2852F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(46, 163).addBox(0.0500F, -1.4135F, -3.9102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(55, 163).addBox(-2.2000F, -1.4135F, -3.9102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.6F, 2.9F));

		PartDefinition lShin = leftLeg2.addOrReplaceChild("lShin", CubeListBuilder.create().texOffs(19, 151).addBox(-23.125F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(115, 157).addBox(-21.125F, -4.25F, -1.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(78, 161).addBox(-19.0F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.05F, 5.2917F, 0.025F, -0.0873F, 0.0F, 0.0F));

		PartDefinition lShin3 = leftLeg2.addOrReplaceChild("lShin3", CubeListBuilder.create().texOffs(162, 141).addBox(-23.075F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(161, 81).addBox(-21.075F, -3.5F, -1.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 14).addBox(-18.95F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(20.05F, 9.0417F, 6.025F, 0.0873F, 0.0F, 0.0F));

		PartDefinition rightLeg = whole.addOrReplaceChild("rightLeg", CubeListBuilder.create().texOffs(138, 15).addBox(-0.9875F, 0.2917F, -1.5F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.2594F, 10.8413F, -0.9006F));

		PartDefinition rightLeg2 = rightLeg.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(29, 129).addBox(-2.975F, -0.4167F, 0.0F, 6.0F, 13.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9875F, 10.7083F, -1.5F));

		PartDefinition rightBoot = rightLeg2.addOrReplaceChild("rightBoot", CubeListBuilder.create().texOffs(54, 145).addBox(-3.1250F, -1.5833F, -3.0000F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.2F))
		.texOffs(163, 32).addBox(-1.6750F, -1.4135F, -4.2852F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 62).addBox(-2.0500F, -1.4135F, -3.9102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 68).addBox(0.2000F, -1.4135F, -3.9102F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 10.6F, 2.9F));

		PartDefinition lrhin = rightLeg2.addOrReplaceChild("lrhin", CubeListBuilder.create().texOffs(87, 161).addBox(21.125F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(126, 158).addBox(18.125F, -4.25F, -1.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 120).addBox(17.0F, -4.625F, -0.625F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.05F, 5.2917F, 0.025F, -0.0873F, 0.0F, 0.0F));

		PartDefinition rshin2 = rightLeg2.addOrReplaceChild("rshin2", CubeListBuilder.create().texOffs(15, 163).addBox(21.075F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(162, 132).addBox(18.075F, -3.5F, -1.0F, 3.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(163, 23).addBox(16.95F, -2.75F, -0.625F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-20.05F, 9.0417F, 6.025F, 0.0873F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	public void translateToWeapon(PoseStack poseStack) {
		whole.translateAndRotate(poseStack);
		body.translateAndRotate(poseStack);
		rightArm.translateAndRotate(poseStack);
		rShoulder.translateAndRotate(poseStack);
		rElbow.translateAndRotate(poseStack);
		rForearm.translateAndRotate(poseStack);
		poseStack.translate(0.0D, 0.4675D, -0.18125D);

	}

	public void translateToLeftWeapon(PoseStack poseStack) {
		whole.translateAndRotate(poseStack);
		body.translateAndRotate(poseStack);
		leftArm.translateAndRotate(poseStack);
		lShoulder.translateAndRotate(poseStack);
		lElbow.translateAndRotate(poseStack);
		lForearm.translateAndRotate(poseStack);
		poseStack.translate(0.05D, 0.1075D, 0.18875D);
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
		VesperEveningStarAnimState state = VesperEveningStarAnimState.of(entity);
		state.update(entity, partialTick);
		resetCombatPose();
		this.hood.visible = VesperEveningStarPresentationRules.isHoodVisible(
				entity.isHoodRemoved(), entity.getHoodRemovalTick());
		this.hair.visible = entity.isHoodRemoved();

		if (entity.isAwaitingAbsorption()) {
			applyAuthoredAnimation(VesperTheEveningStarAnimations.DEFEAT,
					entity.getDownedTicks() + partialTick);
			finishPose(entity, state);
			return;
		}
		if (entity.isHoodRemovalActive()) {
			applyAuthoredAnimation(VesperTheEveningStarAnimations.IDLE, frame);
			applyAuthoredAnimation(VesperTheEveningStarAnimations.REMOVE_HOOD,
					entity.getHoodRemovalTick() + partialTick);
			applyHeadTracking(netHeadYaw, headPitch);
			finishPose(entity, state);
			return;
		}

		// The guard his weapon is carried in, then whatever move is under way keyed over it on the move's own clock.
		VesperWeaponAction action = entity.getWeaponAction();
		float[] pose = VesperEveningStarMoves.stance(entity.getActiveTendency(), entity.isRaging(), frame,
				state.stanceTime());
		VesperEveningStarMoves.act(action, entity.getActionVariant(), state.actionTime(), pose);
		VesperEveningStarMoves.apply(this.rig, pose);
		applyGait(state, action == VesperWeaponAction.NONE);
		applySecondaryClothMotion(frame);
		if (entity.hurtTime > 0) {
			float hitTick = Math.max(0.0F, entity.hurtDuration - entity.hurtTime + partialTick);
			applyAuthoredAnimation(VesperTheEveningStarAnimations.HIT, hitTick);
		}
		applyHeadTracking(netHeadYaw, headPitch);
		finishPose(entity, state);
    }

	/** Eases the pose out of the last frame, then sets the boots on the ground. */
	private void finishPose(VesperTheEveningStarEntity entity, VesperEveningStarAnimState state) {
		if (this.parts == null) this.parts = this.whole.getAllParts().toList();
		state.blend(this.parts);
		plantFeet(entity, state);
	}

	/**
	 * Walks the legs by the distance actually travelled, in the direction actually travelled: forward and back swing
	 * the thighs, a strafe swings them out to the side, and the knee lifts through each swing. The stance's set feet
	 * give way to the stride as he speeds up. The body height is not bobbed here; {@link #plantFeet} finds it from
	 * whichever boot is down.
	 */
	private void applyGait(VesperEveningStarAnimState state, boolean armsFree) {
		float stride = state.stride();
		if (stride < 0.002F) return;
		float settle = 1.0F - 0.75F * stride;
		this.rightLeg.xRot *= settle;
		this.leftLeg.xRot *= settle;
		this.rightLeg.yRot *= settle;
		this.leftLeg.yRot *= settle;
		this.rightLeg2.xRot *= settle;
		this.leftLeg2.xRot *= settle;
		float swing = VesperEveningStarPose.STEP_SWING * stride;
		float ahead = state.driveForward();
		float side = state.driveLateral();
		float phase = state.gaitPhase();
		stepLeg(this.rightLeg, this.rightLeg2, phase, swing, stride, ahead, side);
		stepLeg(this.leftLeg, this.leftLeg2, phase + 0.5F, swing, stride, ahead, side);
		float sway = VesperEveningStarPose.legSwing(phase);
		this.body.xRot += 0.09F * stride * ahead;
		this.body.zRot += 0.06F * stride * side;
		this.body.yRot += 0.07F * stride * sway * ahead;
		if (armsFree) {
			this.rightArm.xRot += 0.12F * stride * sway * ahead;
			this.leftArm.xRot -= 0.12F * stride * sway * ahead;
		}
		this.ClothBack.xRot += 0.3F * stride * ahead;
		this.ClothBack2.xRot += 0.18F * stride * Math.abs(ahead) + 0.08F * stride * sway;
		this.Cloak1.xRot += 0.22F * stride * ahead;
		this.Cloak2.xRot += 0.12F * stride * Math.abs(ahead);
	}

	private static void stepLeg(ModelPart thigh, ModelPart shin, float phase, float swing, float stride,
			float ahead, float side) {
		float reach = VesperEveningStarPose.legSwing(phase);
		float lift = VesperEveningStarPose.legLift(phase) * stride;
		thigh.xRot += -swing * reach * ahead - lift * 0.32F;
		thigh.zRot += -swing * reach * side * 0.75F;
		shin.xRot += lift * 0.95F;
	}

	/**
	 * Sets the body on its boots: the hips settle until the lowest boot meets the floor under it, sink a little into
	 * uneven ground so the lower boot can reach, and any leg that would push into a step folds at the knee instead.
	 * Planted boots lie flat on the ground they stand on.
	 */
	private void plantFeet(VesperTheEveningStarEntity entity, VesperEveningStarAnimState state) {
		float unitsPerBlock = 16.0F / VesperEveningStarAnimState.renderScale(entity);
		ModelPart[] thighs = { this.leftLeg, this.rightLeg };
		ModelPart[] shins = { this.leftLeg2, this.rightLeg2 };
		ModelPart[] boots = { this.leftBoot, this.rightBoot };
		for (int i = 0; i < 2; i++) {
			ModelPart thigh = thighs[i];
			ModelPart shin = shins[i];
			soleCache[i] = this.whole.y + thigh.y + VesperEveningStarPose.soleDrop(thigh.xRot, shin.xRot, thigh.zRot);
			floorCache[i] = VesperEveningStarPose.FLOOR_Y - state.groundUnder(i) * unitsPerBlock;
			state.setFoot(i, this.whole.x + thigh.x + shin.x,
					this.whole.z + thigh.z + VesperEveningStarPose.soleReach(thigh.xRot, shin.xRot));
		}
		float weight = state.groundWeight();
		if (weight <= 0.001F) return;
		float shift = VesperEveningStarPose.bodyShift(soleCache, floorCache) * weight;
		this.whole.y += shift;
		for (int i = 0; i < 2; i++) {
			ModelPart thigh = thighs[i];
			ModelPart shin = shins[i];
			float fold = VesperEveningStarPose.foldFor(thigh.xRot, shin.xRot, thigh.zRot,
					soleCache[i] + shift - floorCache[i]) * weight;
			thigh.xRot -= fold;
			shin.xRot += 2.0F * fold;
			float sole = this.whole.y + thigh.y + VesperEveningStarPose.soleDrop(thigh.xRot, shin.xRot, thigh.zRot);
			float contact = 1.0F - Mth.clamp((floorCache[i] - sole) / 3.0F, 0.0F, 1.0F);
			boots[i].xRot = Mth.lerp(contact * weight * 0.85F, boots[i].xRot, -(thigh.xRot + shin.xRot));
		}
	}

	/**
	 * The cloth hangs from a torso that leans and twists, so it swings back toward plumb against the torso's lean,
	 * then sways a little on its own.
	 */
	private void applySecondaryClothMotion(float frame) {
		float lean = this.body.xRot;
		this.ClothBack.xRot -= lean * 0.75F;
		this.Cloak1.xRot -= lean * 0.6F;
		this.ClothBack2.xRot += lean * 0.15F;
		this.SideclothL1.zRot -= this.body.zRot * 0.6F;
		this.SideclothL5.zRot -= this.body.zRot * 0.6F;
        this.ClothBack.xRot += Mth.sin(frame * 0.3F) * 0.05F;
        this.ClothBack1.xRot += Mth.sin(frame * 0.5F) * 0.08F;
        this.ClothBack2.xRot += Mth.sin(frame * 0.7F) * 0.12F;
        this.ClothBackR2.xRot += Mth.sin(frame * 0.75F) * 0.10F;
        this.ClothBackL2.xRot += Mth.sin(frame * 0.75F + Mth.PI) * 0.10F;
        this.ClothBackL3.xRot += Mth.sin(frame * 0.8F) * 0.16F;
	}

	/** The head keeps on its mark whatever the torso is doing under it. */
	private void applyHeadTracking(float netHeadYaw, float headPitch) {
		this.head.yRot += Mth.wrapDegrees(netHeadYaw) * Mth.DEG_TO_RAD * 0.85F - this.body.yRot;
		this.head.xRot += headPitch * Mth.DEG_TO_RAD * 0.75F - this.body.xRot * 0.8F;
		this.head.zRot -= this.body.zRot * 0.6F;
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

	private static float pose(float current, float target, float blend) {
		return Mth.lerp(blend, current, target);
	}
}
