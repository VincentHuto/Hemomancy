package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hutoslib.client.HLClientUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.Lazy;
import org.jetbrains.annotations.Nullable;

public class SilentArchonArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation SILENT_ARCHON_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_helmet"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> helmet = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_HELMET_LAYER), EquipmentSlot.HEAD));

	public static final ModelLayerLocation SILENT_ARCHON_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_chest"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> chest = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_CHEST_LAYER), EquipmentSlot.CHEST));

	public static final ModelLayerLocation SILENT_ARCHON_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_legs"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> legs = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_LEGS_LAYER), EquipmentSlot.LEGS));

	public static final ModelLayerLocation SILENT_ARCHON_BOOTS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("silent_archon_robe_boots"), "main");
	public static final Lazy<SilentArchonArmorModel<LivingEntity>> boots = Lazy.of(() -> new SilentArchonArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(SILENT_ARCHON_BOOTS_LAYER), EquipmentSlot.FEET));

	private final EquipmentSlot renderSlot;
	private final ModelPart rightBoot;
	private final ModelPart leftBoot;
	private final ModelPart cloak;
	private final ModelPart Cloak1;
	private final ModelPart Cloak2;
	private final ModelPart Cloak3;
	private final ModelPart ClothBackR1;
	private final ModelPart ClothBackR2;
	private final ModelPart ClothBackR3;
	private final ModelPart ClothBackL1;
	private final ModelPart ClothBackL2;
	private final ModelPart ClothBackL3;
	private final ModelPart SideclothL;
	private final ModelPart SideclothR;
	private final ModelPart SideclothR1;
	private final ModelPart SideclothR2;
	private final ModelPart SideclothR3;
	private final ModelPart SideclothR4;
	private final ModelPart SideclothR5;
	private final ModelPart SideclothR6;

	public SilentArchonArmorModel(ModelPart root, EquipmentSlot renderSlot) {
		super(root, RenderType::entityTranslucent);
		this.renderSlot = renderSlot;
		this.rightBoot = this.rightLeg.getChild("right_boot");
		this.leftBoot = this.leftLeg.getChild("left_boot");
		this.cloak = this.body.getChild("cloak");
		this.Cloak1 = this.cloak.getChild("Cloak1");
		this.Cloak2 = this.Cloak1.getChild("Cloak2");
		this.Cloak3 = this.Cloak2.getChild("Cloak3");
		ModelPart clothBack = this.body.getChild("ClothBack");
		ModelPart clothBack1 = clothBack.getChild("ClothBack1");
		ModelPart clothBack2 = clothBack1.getChild("ClothBack2");
		this.ClothBackR1 = clothBack1.getChild("ClothBackR1");
		this.ClothBackR2 = clothBack2.getChild("ClothBackR2");
		this.ClothBackR3 = clothBack2.getChild("ClothBackR3");
		this.ClothBackL1 = clothBack1.getChild("ClothBackL1");
		this.ClothBackL2 = clothBack2.getChild("ClothBackL2");
		this.ClothBackL3 = clothBack2.getChild("ClothBackL3");
		this.SideclothL = this.body.getChild("SideclothL");
		this.SideclothR = this.body.getChild("SideclothR");
		this.SideclothR1 = this.SideclothL.getChild("SideclothR1");
		this.SideclothR2 = this.SideclothR1.getChild("SideclothR2");
		this.SideclothR3 = this.SideclothR2.getChild("SideclothR3");
		this.SideclothR4 = this.SideclothR.getChild("SideclothR4");
		this.SideclothR5 = this.SideclothR4.getChild("SideclothR5");
		this.SideclothR6 = this.SideclothR5.getChild("SideclothR6");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
		PartDefinition partdefinition = meshdefinition.getRoot();
		partdefinition.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = partdefinition.addOrReplaceChild("head",
				CubeListBuilder.create().texOffs(78, 2)
						.addBox(-4.0F, -8.1F, -3.5F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition Hood1 = head.addOrReplaceChild("Hood1", CubeListBuilder.create().texOffs(0, 0)
				.addBox(-4.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(8, 0).addBox(3.5F, -9.0F, -4.6F, 1.0F, 9.0F, 9.0F,
						new CubeDeformation(0.0F))
				.texOffs(24, 106).addBox(-3.5F, -9.0F, -4.6F, 7.0F, 1.0F, 9.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition Hood2 = Hood1.addOrReplaceChild("Hood2",
				CubeListBuilder.create().texOffs(25, 19).addBox(-4.0F, -9.7F, 2.0F, 8.0F, 9.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2269F, 0.0F, 0.0F));
		PartDefinition Hood3 = Hood2.addOrReplaceChild("Hood3", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.3491F, 0.0F, 0.0F));
		Hood3.addOrReplaceChild("Hood3_52_14_697af612_r1",
				CubeListBuilder.create().texOffs(42, 45).addBox(-3.5F, -10.0F, 3.5F, 7.0F, 8.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2182F, 0.0F, 0.0F));
		PartDefinition Hood4 = Hood3.addOrReplaceChild("Hood4", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.576F, 0.0F, 0.0F));
		Hood4.addOrReplaceChild("Hood4_53_15_2a9435b8_r1",
				CubeListBuilder.create().texOffs(38, 57).addBox(-3.0F, -10.7F, 3.5F, 6.0F, 7.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6109F, 0.0F, 0.0F));

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 73)
				.addBox(-4.0F, -1.0F, -2.0F, 8.0F, 13.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(3.3497F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F,
						new CubeDeformation(0.0F))
				.texOffs(0, 18).addBox(-4.2998F, -0.6865F, -2.5F, 1.0F, 13.0F, 5.0F,
						new CubeDeformation(0.0F))
				.texOffs(17, 36).addBox(-4.1F, -0.5F, -3.25F, 2.0F, 8.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(37, 68).addBox(2.1F, -0.5F, -3.25F, 2.0F, 8.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(4, 93).addBox(-3.9F, -0.5F, 1.85F, 8.0F, 8.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(38, 85).addBox(-4.1F, -0.5F, 1.4F, 8.0F, 13.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ClothBack = body.addOrReplaceChild("ClothBack", CubeListBuilder.create(),
				PartPose.offset(0.0F, 12.3F, 4.4F));
		PartDefinition ClothBack1 = ClothBack.addOrReplaceChild("ClothBack1", CubeListBuilder.create(),
				PartPose.offset(4.0F, 0.0F, -1.0F));
		ClothBack1.addOrReplaceChild("ClothBackR1",
				CubeListBuilder.create().texOffs(26, 60).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));
		ClothBack1.addOrReplaceChild("ClothBackL1",
				CubeListBuilder.create().texOffs(63, 42).addBox(-4.0F, 0.0F, -2.0F, 4.0F, 8.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-4.0F, 0.0F, 0.0F, 0.1047F, 0.0F, 0.0F));
		PartDefinition ClothBack2 = ClothBack1.addOrReplaceChild("ClothBack2", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-2.0F, 8.0F, 0.0F, 0.3054F, 0.0F, 0.0F));
		ClothBack2.addOrReplaceChild("ClothBackR2",
				CubeListBuilder.create().texOffs(58, 68).addBox(-1.0F, 7.3522F, -2.8768F, 1.0F, 2.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
		ClothBack2.addOrReplaceChild("ClothBackR3",
				CubeListBuilder.create().texOffs(37, 13).addBox(-4.0F, 7.3522F, -2.8768F, 3.0F, 3.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
		ClothBack2.addOrReplaceChild("ClothBackL2",
				CubeListBuilder.create().texOffs(63, 68).addBox(-4.0F, 7.3522F, -2.8768F, 1.0F, 2.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));
		ClothBack2.addOrReplaceChild("ClothBackL3",
				CubeListBuilder.create().texOffs(63, 52).addBox(-3.0F, 7.3522F, -2.8768F, 3.0F, 3.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-2.0F, -8.0F, 0.0F, 0.2269F, 0.0F, 0.0F));

		PartDefinition SideclothL = body.addOrReplaceChild("SideclothL", CubeListBuilder.create(),
				PartPose.offset(3.8F, 12.25F, 0.0F));
		PartDefinition SideclothR1 = SideclothL.addOrReplaceChild("SideclothR1",
				CubeListBuilder.create().texOffs(57, 57).addBox(0.0417F, 0.0691F, -2.5F, 1.0F, 5.0F,
						5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.5F, 0.0F, 0.0F, 0.0F, 0.0F, -0.1222F));
		PartDefinition SideclothR2 = SideclothR1.addOrReplaceChild("SideclothR2",
				CubeListBuilder.create().texOffs(63, 24).addBox(-0.709F, -0.6426F, -2.5F, 1.0F, 3.0F,
						5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, -0.2967F));
		SideclothR2.addOrReplaceChild("SideclothR3",
				CubeListBuilder.create().texOffs(63, 33).addBox(-0.134F, -0.134F, -2.5F, 1.0F, 3.0F,
						5.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition SideclothR = body.addOrReplaceChild("SideclothR", CubeListBuilder.create(),
				PartPose.offset(-3.8F, 12.25F, 0.0F));
		PartDefinition SideclothR4 = SideclothR.addOrReplaceChild("SideclothR4",
				CubeListBuilder.create().texOffs(57, 57).mirror().addBox(-1.0416F, 0.0691F, -2.5F, 1.0F,
						5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.1222F));
		PartDefinition SideclothR5 = SideclothR4.addOrReplaceChild("SideclothR5",
				CubeListBuilder.create().texOffs(63, 24).mirror().addBox(-0.291F, -0.6426F, -2.5F, 1.0F,
						3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-0.9076F, 5.4763F, 0.0F, 0.0F, 0.0F, 0.2967F));
		SideclothR5.addOrReplaceChild("SideclothR6",
				CubeListBuilder.create().texOffs(63, 33).mirror().addBox(-0.866F, -0.134F, -2.5F, 1.0F,
						3.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.526F, 2.4064F, 0.0F, 0.0F, 0.0F, 0.5236F));

		PartDefinition cloak = body.addOrReplaceChild("cloak", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0.0F, 0.0F, 1.2F, -0.0436F, 0.0F, 0.0F));
		cloak.addOrReplaceChild("CloakTL",
				CubeListBuilder.create().texOffs(98, 72).addBox(-4.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
		cloak.addOrReplaceChild("CloakTR",
				CubeListBuilder.create().texOffs(94, 62).addBox(2.5F, 1.0F, -1.0F, 2.0F, 1.0F, 3.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
		PartDefinition Cloak1 = cloak.addOrReplaceChild("Cloak1",
				CubeListBuilder.create().texOffs(107, 54).addBox(-4.5F, 2.0F, 1.0F, 9.0F, 12.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.1396F, 0.0F, 0.0F));
		PartDefinition Cloak2 = Cloak1.addOrReplaceChild("Cloak2",
				CubeListBuilder.create().texOffs(104, 83).addBox(-4.5F, -0.1501F, -0.3628F, 9.0F, 4.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 14.0335F, 1.3912F, 0.3069F, 0.0F, 0.0F));
		Cloak2.addOrReplaceChild("Cloak3",
				CubeListBuilder.create().texOffs(103, 98).addBox(-4.5F, -0.4485F, -0.1911F, 9.0F, 4.0F,
						1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 4.1718F, 0.0032F, 0.4466F, 0.0F, 0.0F));
		body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(77, 59)
				.addBox(-4.0F, -13.0F, -3.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(77, 64).addBox(-2.0F, -12.0F, -3.0F, 4.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(81, 54).addBox(-2.0F, -14.0F, -3.0F, 4.0F, 1.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 23.85F, 0.25F));

		PartDefinition leftArm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(38, 0)
				.addBox(-0.5F, 3.5F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(48, 24).addBox(-0.5F, 7.5F, 2.5F, 4.0F, 3.0F, 2.0F,
						new CubeDeformation(0.0F))
				.texOffs(58, 9).addBox(0.0F, 5.5F, 2.5F, 3.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(102, 36).addBox(-0.9841F, 1.1021F, -2.5125F, 5.0F, 4.0F, 5.0F,
						new CubeDeformation(0.125F)),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r1",
				CubeListBuilder.create().texOffs(79, 36).addBox(-2.4063F, -2.0774F, -2.5F, 5.0F, 3.0F,
						5.0F, new CubeDeformation(0.25F)),
				PartPose.offsetAndRotation(0.45F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1345F));
		leftArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r2",
				CubeListBuilder.create().texOffs(100, 28).addBox(-1.9226F, -1.5937F, -2.45F, 4.0F, 3.0F,
						5.0F, new CubeDeformation(0.1875F)),
				PartPose.offsetAndRotation(1.4146F, 0.4898F, -0.0625F, 0.0F, 0.0F, -0.4363F));
		leftArm.addOrReplaceChild("body2", CubeListBuilder.create(), PartPose.offset(-5.0F, -2.0F, 1.0F));

		PartDefinition rightArm = partdefinition.addOrReplaceChild("right_arm",
				CubeListBuilder.create().texOffs(42, 32)
						.addBox(-3.5F, 3.5F, -2.5F, 4.0F, 7.0F, 5.0F, new CubeDeformation(0.0F))
						.texOffs(0, 64).addBox(-3.5F, 7.5F, 2.5F, 4.0F, 3.0F, 2.0F,
								new CubeDeformation(0.0F))
						.texOffs(67, 9).addBox(-3.0F, 5.5F, 2.5F, 3.0F, 2.0F, 1.0F,
								new CubeDeformation(0.0F))
						.texOffs(79, 27).mirror().addBox(-4.0159F, 1.1021F, -2.5125F, 5.0F, 4.0F,
								5.0F, new CubeDeformation(0.125F)).mirror(false),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r3",
				CubeListBuilder.create().texOffs(100, 20).mirror().addBox(-2.5F, -2.5F, -2.45F, 4.0F,
						3.0F, 5.0F, new CubeDeformation(0.1875F)).mirror(false),
				PartPose.offsetAndRotation(-1.4146F, 1.4898F, -0.0625F, 0.0F, 0.0F, 0.4363F));
		rightArm.addOrReplaceChild("ShoulderR_16_45_0eedefc6_r4",
				CubeListBuilder.create().texOffs(79, 19).mirror().addBox(-3.5F, -2.5F, -2.5F, 5.0F,
						3.0F, 5.0F, new CubeDeformation(0.25F)).mirror(false),
				PartPose.offsetAndRotation(-0.45F, 1.0F, 0.0F, 0.0F, 0.0F, 1.1345F));

		PartDefinition leftLeg = partdefinition.addOrReplaceChild("left_leg",
				CubeListBuilder.create().texOffs(25, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition rightLeg = partdefinition.addOrReplaceChild("right_leg",
				CubeListBuilder.create().texOffs(0, 36).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));

		addBoots(rightLeg, leftLeg);
		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	private static void addBoots(PartDefinition rightLeg, PartDefinition leftLeg) {
		PartDefinition rightBoot = rightLeg.addOrReplaceChild("right_boot", CubeListBuilder.create().texOffs(77, 116)
				.addBox(-2.0F, 10.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2F))
				.texOffs(97, 122).addBox(-1.1F, 10.1799F, -2.9235F, 2.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(97, 122).addBox(-1.35F, 10.1799F, -2.6735F, 1.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F))
				.texOffs(97, 122).addBox(0.15F, 10.1799F, -2.6735F, 1.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		rightBoot.addOrReplaceChild("rShin",
				CubeListBuilder.create().texOffs(93, 106).addBox(0.9167F, -3.0833F, -0.4167F, 1.0F, 6.0F,
						1.0F, new CubeDeformation(0.0F))
						.texOffs(97, 113).addBox(-1.0833F, -2.8333F, -0.6667F, 2.0F, 6.0F, 1.0F,
								new CubeDeformation(0.0F))
						.texOffs(87, 106).addBox(-1.8333F, -3.0833F, -0.4167F, 1.0F, 6.0F, 1.0F,
								new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0167F, 7.5833F, -1.9833F, -0.0873F, 0.0F, 0.0F));
		rightBoot.addOrReplaceChild("rShin2",
				CubeListBuilder.create().texOffs(105, 123).addBox(0.9167F, -1.8333F, -0.4167F, 1.0F, 4.0F,
						1.0F, new CubeDeformation(0.0F))
						.texOffs(105, 113).addBox(-1.0833F, -2.3333F, -0.6667F, 2.0F, 4.0F, 1.0F,
								new CubeDeformation(0.0F))
						.texOffs(105, 105).addBox(-1.8333F, -1.8333F, -0.4167F, 1.0F, 4.0F, 1.0F,
								new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-0.0167F, 10.0833F, 2.0167F, 0.0873F, 0.0F, 0.0F));

		PartDefinition leftBoot = leftLeg.addOrReplaceChild("left_boot", CubeListBuilder.create().texOffs(77, 116)
				.mirror().addBox(-2.0F, 10.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2F))
				.mirror(false)
				.texOffs(97, 122).mirror().addBox(-0.9F, 10.1799F, -2.9235F, 2.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F)).mirror(false)
				.texOffs(97, 122).mirror().addBox(0.35F, 10.1799F, -2.6735F, 1.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F)).mirror(false)
				.texOffs(97, 122).mirror().addBox(-1.15F, 10.1799F, -2.6735F, 1.0F, 2.0F, 1.0F,
						new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(0.0F, 0.0F, 0.0F));
		leftBoot.addOrReplaceChild("rShin3",
				CubeListBuilder.create().texOffs(93, 106).mirror().addBox(-1.9167F, -3.0833F, -0.4167F,
						1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(97, 113).mirror().addBox(-0.9167F, -2.8333F, -0.6667F, 2.0F, 6.0F,
								1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(87, 106).mirror().addBox(0.8333F, -3.0833F, -0.4167F, 1.0F, 6.0F,
								1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0167F, 7.5833F, -1.9833F, -0.0873F, 0.0F, 0.0F));
		leftBoot.addOrReplaceChild("rShin4",
				CubeListBuilder.create().texOffs(105, 123).mirror().addBox(-1.9167F, -1.8333F, -0.4167F,
						1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(105, 113).mirror().addBox(-0.9167F, -2.3333F, -0.6667F, 2.0F, 4.0F,
								1.0F, new CubeDeformation(0.0F)).mirror(false)
						.texOffs(105, 105).mirror().addBox(0.8333F, -1.8333F, -0.4167F, 1.0F, 4.0F,
								1.0F, new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(0.0167F, 10.0833F, 2.0167F, 0.0873F, 0.0F, 0.0F));
	}

	@Override
	public void renderToBuffer(@Nullable PoseStack poseStack, @Nullable VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		switch (this.renderSlot) {
			case HEAD -> {
				renderPart(this.head, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.hat, poseStack, buffer, packedLight, packedOverlay, packedColor);
			}
			case CHEST -> {
				renderPart(this.body, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.rightArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
				renderPart(this.leftArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
			}
			case LEGS -> renderLegsWithoutBoots(poseStack, buffer, packedLight, packedOverlay, packedColor);
			case FEET -> renderBoots(poseStack, buffer, packedLight, packedOverlay, packedColor);
			default -> {
			}
		}
	}

	private void renderLegsWithoutBoots(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		boolean rightVisible = this.rightBoot.visible;
		boolean leftVisible = this.leftBoot.visible;
		this.rightBoot.visible = false;
		this.leftBoot.visible = false;
		renderPart(this.rightLeg, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.leftLeg, poseStack, buffer, packedLight, packedOverlay, packedColor);
		this.rightBoot.visible = rightVisible;
		this.leftBoot.visible = leftVisible;
	}

	private void renderBoots(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		renderChildWithParentTransform(this.rightLeg, this.rightBoot, poseStack, buffer, packedLight, packedOverlay,
				packedColor);
		renderChildWithParentTransform(this.leftLeg, this.leftBoot, poseStack, buffer, packedLight, packedOverlay,
				packedColor);
	}

	private static void renderChildWithParentTransform(ModelPart parent, ModelPart child, PoseStack poseStack,
			VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		if (parent.visible && child.visible) {
			poseStack.pushPose();
			parent.translateAndRotate(poseStack);
			child.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
			poseStack.popPose();
		}
	}

	private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		if (part.visible) {
			part.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		}
	}

	@Override
	public void setupAnim(@Nullable T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float frame = entity.tickCount + HLClientUtils.getPartialTicks();
		float idleWave = Mth.sin(frame * 0.067F);
		float walkWave = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount;

		// Cape - wave animation cascading down each segment
		this.cloak.xRot = 0.08F + idleWave * 0.03F;
		this.Cloak1.xRot = 0.12F + Mth.sin(frame * 0.067F + 0.5F) * 0.04F;
		this.Cloak2.xRot = 0.10F + Mth.sin(frame * 0.067F + 1.0F) * 0.04F;
		this.Cloak3.xRot = 0.08F + Mth.sin(frame * 0.067F + 1.5F) * 0.03F;

		// Back cloth panels - x axis flutter
		this.ClothBackR1.xRot = 0.1047F + idleWave * 0.03F + walkWave * 0.07F;
		this.ClothBackR2.xRot = 0.2269F + Mth.sin(frame * 0.067F + 0.6F) * 0.035F + walkWave * 0.08F;
		this.ClothBackR3.xRot = 0.2269F + Mth.sin(frame * 0.067F + 1.0F) * 0.04F + walkWave * 0.10F;
		this.ClothBackL1.xRot = 0.1047F + Mth.sin(frame * 0.067F + 0.3F) * 0.03F + walkWave * 0.07F;
		this.ClothBackL2.xRot = 0.2269F + Mth.sin(frame * 0.067F + 0.9F) * 0.035F + walkWave * 0.08F;
		this.ClothBackL3.xRot = 0.2269F + Mth.sin(frame * 0.067F + 1.3F) * 0.04F + walkWave * 0.10F;

		// Side cloth strips - z axis sway
		float sideSway = Mth.sin(frame * 0.09F) * 0.05F + walkWave * 0.05F;
		this.SideclothL.zRot = -0.08F + sideSway;
		this.SideclothR.zRot = 0.08F - sideSway;

		this.SideclothR1.zRot = -0.1222F + Mth.sin(frame * 0.067F + 0.2F) * 0.03F + walkWave * 0.04F;
		this.SideclothR2.zRot = -0.2967F + Mth.sin(frame * 0.067F + 0.8F) * 0.035F + walkWave * 0.05F;
		this.SideclothR3.zRot = -0.5236F + Mth.sin(frame * 0.067F + 1.2F) * 0.04F + walkWave * 0.06F;
		this.SideclothR4.zRot = 0.1222F + Mth.sin(frame * 0.067F + 0.5F) * 0.03F + walkWave * 0.04F;
		this.SideclothR5.zRot = 0.2967F + Mth.sin(frame * 0.067F + 1.0F) * 0.035F + walkWave * 0.05F;
		this.SideclothR6.zRot = 0.5236F + Mth.sin(frame * 0.067F + 1.4F) * 0.04F + walkWave * 0.06F;


	}
}
