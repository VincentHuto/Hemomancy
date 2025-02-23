package com.vincenthuto.hemomancy.client.model.entity.mob;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.ErythromyceliumEruptusEntity;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
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

public class ErythromyceliumEruptusModel extends HierarchicalModel<ErythromyceliumEruptusEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in
	// the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("modelerupted"),
			"main");
	private final ModelPart whole;

	public ErythromyceliumEruptusModel(ModelPart root) {
		this.whole = root.getChild("whole");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition whole = partdefinition.addOrReplaceChild("whole", CubeListBuilder.create(),
				PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition MainForm = whole.addOrReplaceChild("MainForm", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, 0.0F, 5.0F, -1.5708F, 3.1416F, 0.0F));

		PartDefinition LeftLeg = MainForm.addOrReplaceChild("LeftLeg", CubeListBuilder.create().texOffs(16, 48)
				.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 48)
				.addBox(-2.0F, 5.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).texOffs(0, 32).mirror()
				.addBox(-2.0F, -16.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offset(1.9F, 8.0F, 0.0F));

		PartDefinition RightLeg = MainForm.addOrReplaceChild("RightLeg",
				CubeListBuilder.create().texOffs(16, 48).mirror()
						.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(0, 32).addBox(-2.0F, -16.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
						.texOffs(0, 48).mirror()
						.addBox(-2.0F, 5.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offset(-1.9F, 8.0F, 0.0F));

		PartDefinition LeftArm = MainForm.addOrReplaceChild("LeftArm", CubeListBuilder.create().texOffs(32, 48)
				.addBox(0.0F, -1.5F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(48, 48)
				.addBox(0.0F, 3.5F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).texOffs(40, 32).mirror()
				.addBox(0.0F, -17.5F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offset(4.0F, -2.5F, 0.0F));

		PartDefinition Body = MainForm.addOrReplaceChild("Body", CubeListBuilder.create().texOffs(16, 16).mirror()
				.addBox(-4.0F, 2.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(16, 32)
				.mirror().addBox(-4.0F, -14.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition RightArm = MainForm.addOrReplaceChild("RightArm",
				CubeListBuilder.create().texOffs(32, 48).mirror()
						.addBox(-4.0F, -1.5F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(40, 32).addBox(-4.0F, -17.5F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
						.texOffs(48, 48).mirror()
						.addBox(-4.0F, 3.5F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offset(-4.0F, -2.5F, 0.0F));

		PartDefinition Head = MainForm.addOrReplaceChild("Head", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(32, 0)
				.addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).texOffs(0, 0).mirror()
				.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)).mirror(false).texOffs(32, 0)
				.mirror().addBox(-4.0F, -24.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.5F)).mirror(false),
				PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition Eruption = whole.addOrReplaceChild("Eruption", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Center = Eruption.addOrReplaceChild("Center", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Legs = Center.addOrReplaceChild("Legs", CubeListBuilder.create(),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Leg4 = Legs.addOrReplaceChild("Leg4",
				CubeListBuilder.create().texOffs(47, 126).mirror()
						.addBox(-5.0F, 0.0F, -1.5F, 7.0F, 2.5F, 2.5F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-4.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition Seg10 = Leg4.addOrReplaceChild("Seg10",
				CubeListBuilder.create().texOffs(48, 127).mirror()
						.addBox(-7.0F, 0.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-5.0F, 0.0F, -0.25F, 0.0F, 0.0F, -1.1781F));

		PartDefinition Seg11 = Seg10.addOrReplaceChild("Seg11",
				CubeListBuilder.create().texOffs(47, 126).mirror()
						.addBox(-12.0F, -0.25F, -1.25F, 13.0F, 2.5F, 2.5F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition Seg12 = Seg11.addOrReplaceChild("Seg12", CubeListBuilder.create().texOffs(48, 127).mirror()
				.addBox(-5.0F, -0.75F, -0.75F, 6.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(49, 128).mirror().addBox(-15.0F, -0.75F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.mirror(false).texOffs(84, 171).mirror()
				.addBox(-6.0F, 0.0F, -1.75F, 7.0F, 0.0F, 3.5F, new CubeDeformation(0.0F)).mirror(false).texOffs(87, 171)
				.mirror().addBox(-6.0F, -1.75F, 0.0F, 7.0F, 3.5F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-12.0F, 1.0F, 0.0F, 0.0F, 0.0F, -2.4435F));

		PartDefinition Leg3 = Legs.addOrReplaceChild("Leg3",
				CubeListBuilder.create().texOffs(47, 126).mirror()
						.addBox(-5.0F, 0.0F, -1.5F, 7.0F, 2.5F, 2.5F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-4.0F, -5.0F, 7.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition Seg7 = Leg3.addOrReplaceChild("Seg7",
				CubeListBuilder.create().texOffs(48, 127).mirror()
						.addBox(-7.0F, 0.0F, -1.0F, 7.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-5.0F, 0.0F, -0.25F, 0.0F, 0.0F, -1.1781F));

		PartDefinition Seg8 = Seg7.addOrReplaceChild("Seg8",
				CubeListBuilder.create().texOffs(47, 126).mirror()
						.addBox(-12.0F, -0.25F, -1.25F, 13.0F, 2.5F, 2.5F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-7.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.7418F));

		PartDefinition Seg9 = Seg8.addOrReplaceChild("Seg9", CubeListBuilder.create().texOffs(48, 127).mirror()
				.addBox(-5.0F, -0.75F, -0.75F, 6.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)).mirror(false)
				.texOffs(49, 128).mirror().addBox(-15.0F, -0.75F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.mirror(false).texOffs(83, 167).mirror()
				.addBox(-6.0F, 0.0F, -1.75F, 7.0F, 0.0F, 3.5F, new CubeDeformation(0.0F)).mirror(false).texOffs(87, 171)
				.mirror().addBox(-6.0F, -1.75F, 0.0F, 7.0F, 3.5F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-12.0F, 1.0F, 0.0F, 0.0F, 0.0F, -2.4435F));

		PartDefinition Leg2 = Legs
				.addOrReplaceChild("Leg2",
						CubeListBuilder.create().texOffs(47, 126).addBox(-2.0F, 0.0F, -1.5F, 7.0F, 2.5F, 2.5F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(4.0F, -5.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition Seg4 = Leg2.addOrReplaceChild("Seg4",
				CubeListBuilder.create().texOffs(48, 127).addBox(0.0F, 0.0F, -1.0F, 7.0F, 2.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.0F, 0.0F, -0.25F, 0.0F, 0.0F, 1.1781F));

		PartDefinition Seg5 = Seg4
				.addOrReplaceChild("Seg5",
						CubeListBuilder.create().texOffs(47, 126).addBox(-1.0F, -0.25F, -1.25F, 13.0F, 2.5F, 2.5F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition Seg6 = Seg5.addOrReplaceChild("Seg6",
				CubeListBuilder.create().texOffs(48, 127)
						.addBox(-1.0F, -0.75F, -0.75F, 6.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)).texOffs(49, 128)
						.addBox(5.0F, -0.75F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(83, 167)
						.addBox(-1.0F, 0.0F, -1.75F, 7.0F, 0.0F, 3.5F, new CubeDeformation(0.0F)).texOffs(87, 171)
						.addBox(-1.0F, -1.75F, 0.0F, 7.0F, 3.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(12.0F, 1.0F, 0.0F, 0.0F, 0.0F, 2.4435F));

		PartDefinition Leg1 = Legs
				.addOrReplaceChild("Leg1",
						CubeListBuilder.create().texOffs(47, 126).addBox(-2.0F, 0.0F, -1.5F, 7.0F, 2.5F, 2.5F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(4.0F, -5.0F, 7.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition Seg1 = Leg1.addOrReplaceChild("Seg1",
				CubeListBuilder.create().texOffs(48, 127).addBox(0.0F, 0.0F, -1.0F, 7.0F, 2.0F, 2.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(5.0F, 0.0F, -0.25F, 0.0F, 0.0F, 1.1781F));

		PartDefinition Seg2 = Seg1
				.addOrReplaceChild("Seg2",
						CubeListBuilder.create().texOffs(47, 126).addBox(-1.0F, -0.25F, -1.25F, 13.0F, 2.5F, 2.5F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(7.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.7418F));

		PartDefinition Seg3 = Seg2.addOrReplaceChild("Seg3",
				CubeListBuilder.create().texOffs(48, 127)
						.addBox(-1.0F, -0.75F, -0.75F, 6.0F, 1.5F, 1.5F, new CubeDeformation(0.0F)).texOffs(49, 128)
						.addBox(5.0F, -0.75F, -0.5F, 10.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(83, 167)
						.addBox(-1.0F, 0.0F, -1.75F, 7.0F, 0.0F, 3.5F, new CubeDeformation(0.0F)).texOffs(99, 170)
						.addBox(-1.0F, -1.75F, 0.0F, 7.0F, 3.5F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(12.0F, 1.0F, 0.0F, 0.0F, 0.0F, 2.4435F));

		PartDefinition EruptionBody = Center.addOrReplaceChild("EruptionBody",
				CubeListBuilder.create().texOffs(2, 198)
						.addBox(-2.0F, -2.0F, -5.5F, 4.0F, 4.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(9, 205)
						.mirror().addBox(-2.5F, -2.5F, 1.5F, 5.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0F, -9.5F, 3.0F, -1.5708F, 0.0F, 0.0F));

		PartDefinition EruptionCap = EruptionBody.addOrReplaceChild("EruptionCap", CubeListBuilder.create()
				.texOffs(6, 107).addBox(-4.0F, -4.0F, -4.5F, 8.0F, 8.0F, 9.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, -9.0F));

		PartDefinition EruptionCap2 = EruptionCap.addOrReplaceChild("EruptionCap2",
				CubeListBuilder.create().texOffs(10, 111)
						.addBox(-5.0F, -6.0F, -5.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(10, 111)
						.addBox(-4.75F, 1.0F, 0.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(8, 109)
						.addBox(0.0F, -6.0F, -1.5F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition EruptionStem = EruptionBody.addOrReplaceChild("EruptionStem",
				CubeListBuilder.create().texOffs(9, 205).addBox(0.0F, -1.0F, -2.5F, 2.0F, 2.0F, 4.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, -0.75F, -3.0F, 0.0F, -0.6109F, 0.0F));

		PartDefinition EruptionStem2 = EruptionBody.addOrReplaceChild("EruptionStem2",
				CubeListBuilder.create().texOffs(7, 203).addBox(-0.25F, -1.0F, -2.5F, 2.0F, 2.0F, 6.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, 3.25F, -3.0F, 0.6545F, 0.0F, 0.0F));

		PartDefinition Hypahe = EruptionBody.addOrReplaceChild("Hypahe", CubeListBuilder.create().texOffs(60, 191)
				.addBox(-8.0F, -5.0F, 0.0F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -1.0F, 6.0F));

		PartDefinition hFlap = Hypahe.addOrReplaceChild("hFlap",
				CubeListBuilder.create().texOffs(60, 191).mirror()
						.addBox(-0.2172F, -5.0F, -0.6543F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(7.5886F, 0.0F, 0.5532F));

		PartDefinition hFlap2 = Hypahe.addOrReplaceChild("hFlap2", CubeListBuilder.create().texOffs(60, 191)
				.addBox(-16.5886F, -5.0F, -0.5532F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-7.4114F, 0.0F, 0.5532F));

		PartDefinition Hypahe2 = EruptionBody
				.addOrReplaceChild("Hypahe2",
						CubeListBuilder.create().texOffs(60, 191).addBox(-8.0F, -5.0F, 0.0F, 16.0F, 10.0F, 0.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, -1.0F, 6.0F, 0.0F, 0.0F, 0.6545F));

		PartDefinition hFlap3 = Hypahe2.addOrReplaceChild("hFlap3", CubeListBuilder.create().texOffs(60, 191)
				.addBox(-0.2172F, -5.0F, -0.6543F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(7.5886F, 0.0F, 0.5532F));

		PartDefinition hFlap4 = Hypahe2.addOrReplaceChild("hFlap4", CubeListBuilder.create().texOffs(60, 191).mirror()
				.addBox(-16.5886F, -5.0F, -0.5532F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-7.4114F, 0.0F, 0.5532F));

		PartDefinition Hypahe3 = EruptionBody.addOrReplaceChild("Hypahe3",
				CubeListBuilder.create().texOffs(60, 191).addBox(-8.0F, -5.0F, 0.0F, 16.0F, 10.0F, 0.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.0F, 6.0F, 0.0F, 0.0F, -0.6545F));

		PartDefinition hFlap5 = Hypahe3.addOrReplaceChild("hFlap5", CubeListBuilder.create().texOffs(60, 191)
				.addBox(-0.2172F, -5.0F, -0.6543F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)),
				PartPose.offset(7.5886F, 0.0F, 0.5532F));

		PartDefinition hFlap6 = Hypahe3.addOrReplaceChild("hFlap6", CubeListBuilder.create().texOffs(60, 191).mirror()
				.addBox(-16.5886F, -5.0F, -0.5532F, 16.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(-7.4114F, 0.0F, 0.5532F));

		PartDefinition EruptionMouth = Center.addOrReplaceChild("EruptionMouth",
				CubeListBuilder.create().texOffs(9, 110)
						.addBox(-3.0F, 0.5F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(12, 113)
						.addBox(-1.5F, 3.5F, -1.5F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(10, 111)
						.addBox(-2.5F, 5.5F, -2.5F, 5.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(11, 112)
						.addBox(-2.0F, -1.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -3.0F, 2.5F));

		PartDefinition EruptionMouth2 = EruptionMouth.addOrReplaceChild("EruptionMouth2",
				CubeListBuilder.create().texOffs(11, 112)
						.addBox(-2.0F, 0.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(13, 114)
						.addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(9, 110)
						.addBox(0.0F, 2.5F, -3.0F, 0.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(87, 139)
						.addBox(0.0F, 2.5F, -2.0F, 0.0F, 17.0F, 4.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 10.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@Override
	public void setupAnim(ErythromyceliumEruptusEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		this.root().getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.disguisedAnimationState, ErythomyceliumEruptusAnimations.MODELERUPTED_DISGUISED,
				ageInTicks);
		this.animate(entity.eruptAnimationState, ErythomyceliumEruptusAnimations.MODELERUPTED_ERUPT, ageInTicks);
		this.animate(entity.idleAnimationState, ErythomyceliumEruptusAnimations.MODELERUPTED_IDLE, ageInTicks);
		this.animate(entity.walkAnimationState, ErythomyceliumEruptusAnimations.MODELERUPTED_WALK, ageInTicks);

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		whole.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return this.whole;
	}

	public static class ErythomyceliumEruptusAnimations {

		public static final AnimationDefinition MODELERUPTED_DISGUISED = AnimationDefinition.Builder.withLength(3f)
				.looping()
				.addAnimation("LeftLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.6766666f, KeyframeAnimations.degreeVec(10f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(2.125f, KeyframeAnimations.degreeVec(-7.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.20834334f, KeyframeAnimations.degreeVec(-1f, 0.52f, 0.23f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0834333f, KeyframeAnimations.degreeVec(-7.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.3433333f, KeyframeAnimations.degreeVec(-13.57f, -34.13f, -15.08f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Body",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.20834334f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.3433333f, KeyframeAnimations.scaleVec(1.05f, 1.05f, 1.05f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5f, KeyframeAnimations.scaleVec(1.05f, 1.05f, 1.05f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5834333f, KeyframeAnimations.scaleVec(1.05f, 1.05f, 1.05f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.75f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.875f, KeyframeAnimations.scaleVec(1.05f, 1.05f, 1.05f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.9583433f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(3f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.25f, KeyframeAnimations.degreeVec(0f, 0f, 22.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.20834334f, KeyframeAnimations.degreeVec(-14.99f, 27.57f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(0f, 52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.875f, KeyframeAnimations.degreeVec(0f, 52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.875f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.9583433f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(2.2916765f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(3f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Eruption",
						new AnimationChannel(AnimationChannel.Targets.SCALE, new Keyframe(0f,
								KeyframeAnimations.scaleVec(0f, 0f, 0f), AnimationChannel.Interpolations.CATMULLROM)))
				.build();
		public static final AnimationDefinition MODELERUPTED_ERUPT = AnimationDefinition.Builder.withLength(1.625f)
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(-15f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 25f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-42.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(50f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -25f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-55.83f, -20.18f, -34.17f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(50f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 12.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-22.13f, -6.01f, 33.86f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(50f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-22.15f, -13.74f, -30.46f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(-12.95f, -10.9f, -23.04f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.2083433f, KeyframeAnimations.degreeVec(50f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5834333f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("whole",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.3433333f, KeyframeAnimations.posVec(0f, 3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.posVec(0f, 18f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Eruption",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionBody",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(0f, 0f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(19.54f, -26.61f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg1",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg1",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg2",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg3",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(0f, 0f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(0f, 0f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.degreeVec(0f, 0f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(19.54f, 26.61f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg7",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg7",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg8",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg8",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg9",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg9",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg11",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(1.5f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-42.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9583434f, KeyframeAnimations.degreeVec(-37.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0834333f, KeyframeAnimations.degreeVec(-42.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(-37.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-32.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9583434f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0834333f, KeyframeAnimations.degreeVec(-32.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.5f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap5",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap6",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.build();
		public static final AnimationDefinition MODELERUPTED_IDLE = AnimationDefinition.Builder.withLength(1.625f)
				.looping()
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(46.81f, -9.18f, 8.52f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(44.75f, 5.3f, -5.32f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(47.39f, -3.68f, 3.38f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0416767f, KeyframeAnimations.degreeVec(33.05f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(29.91f, 2.5f, -4.33f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0416767f, KeyframeAnimations.degreeVec(76.78f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("whole",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 18f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Eruption",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionBody",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, -4f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(19.54f, -26.61f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(19.54f, -26.61f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg1",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -56.58f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg1",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 19f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg2",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -115f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg3",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -60f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0.22f, 5f, 12.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(19.54f, 26.61f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(19.54f, 26.61f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg7",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 56.58f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg7",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg8",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -19f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg8",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg9",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 115f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg9",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 60f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg11",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg11",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(-0.22f, -5f, -12.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-37.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(-26.72f, 6.7f, 13.05f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.6766666f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0416767f, KeyframeAnimations.degreeVec(-25.62f, -10.33f, -20.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("MainForm",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, 52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, 52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap5",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap6",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(0f, 52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionCap",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7083434f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.875f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0834333f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.build();
		public static final AnimationDefinition MODELERUPTED_WALK = AnimationDefinition.Builder.withLength(1.625f)
				.looping()
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5834334f, KeyframeAnimations.degreeVec(-22.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-30f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Head",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(36.33f, -10.39f, 14.62f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(40.84f, -11.31f, 16.6f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftArm",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(38.22f, 6.9f, -9.05f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(43.8f, 10.61f, -13.93f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("RightLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(44.75f, -5.3f, 5.32f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(36.65f, -3.18f, 3.11f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("LeftLeg",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.4167667f, KeyframeAnimations.degreeVec(39.9f, 2.87f, -4.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7083434f, KeyframeAnimations.degreeVec(34.35f, 4.02f, -5.8f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(35f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("whole",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 18f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Eruption",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionBody",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, -4f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(19.54f, -26.61f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.375f, KeyframeAnimations.degreeVec(27.48f, -35.12f, -51.28f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(34.15f, -37.04f, -56.72f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(19.54f, -26.61f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.25f, KeyframeAnimations.degreeVec(5.39f, 14.99f, -29.87f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(19.54f, -26.61f, -37.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg1",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg1",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -50.85f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg1",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 18.81f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg2",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -115f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg3",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.375f, KeyframeAnimations.degreeVec(-2.82f, 6.23f, -46.58f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(-5.54f, -1.94f, -34.22f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.25f, KeyframeAnimations.degreeVec(-16.62f, 50.81f, -37.35f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-18.06f, 27.77f, -38.4f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg2",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -60f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg4",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0.22f, 5f, 12.49f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.25f, KeyframeAnimations.degreeVec(-0.11f, 2.63f, -5.54f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -15f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg5",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, -110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg6",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.375f, KeyframeAnimations.degreeVec(-27.32f, -53.29f, 52.26f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.5f, KeyframeAnimations.degreeVec(-20.25f, -58.09f, 43.51f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.25f, KeyframeAnimations.degreeVec(10.93f, -15.83f, 34.4f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-18.06f, -27.77f, 38.4f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg4",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(19.54f, 26.61f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.375f, KeyframeAnimations.degreeVec(-3.27f, 9.41f, 36.46f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(19.54f, 26.61f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.25f, KeyframeAnimations.degreeVec(22.6f, 34.6f, 40.96f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(19.54f, 26.61f, 37.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Leg3",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg7",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 50.85f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg7",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg8",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, -18.81f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.25f, KeyframeAnimations.degreeVec(0f, 0f, 0.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg8",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg9",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 115f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg9",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 60f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 32.5f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg10",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg11",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg11",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.375f, KeyframeAnimations.degreeVec(-0.53f, -2.41f, 11.45f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(-0.22f, -5f, -12.49f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 15f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.375f, KeyframeAnimations.degreeVec(0f, 0f, 115f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.8343334f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 0f, 110f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("Seg12",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-37.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionMouth2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.20834334f, KeyframeAnimations.degreeVec(-24.79f, 12.31f, 24.79f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.2916767f, KeyframeAnimations.degreeVec(-24.92f, -9.46f, 32.45f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.625f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(-26.11f, 15.35f, -24.64f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.3433333f, KeyframeAnimations.degreeVec(-24.41f, -9.15f, -18.93f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(-27.5f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("MainForm",
						new AnimationChannel(AnimationChannel.Targets.POSITION,
								new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.posVec(0f, -3f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.posVec(0f, 0f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap2",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap3",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap4",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap5",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, -52.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, -42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("hFlap6",
						new AnimationChannel(AnimationChannel.Targets.ROTATION,
								new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.9167666f, KeyframeAnimations.degreeVec(0f, 55f, 0f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.degreeVec(0f, 42.5f, 0f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.addAnimation("EruptionCap",
						new AnimationChannel(AnimationChannel.Targets.SCALE,
								new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7083434f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.7916766f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(0.875f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.0834333f, KeyframeAnimations.scaleVec(1.1f, 1.1f, 1.1f),
										AnimationChannel.Interpolations.CATMULLROM),
								new Keyframe(1.625f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
										AnimationChannel.Interpolations.CATMULLROM)))
				.build();
	}
}