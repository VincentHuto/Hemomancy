package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

public class BloodLustArmorModel<T extends LivingEntity> extends HumanoidModel<T> {


	public static enum EnumBloodLustMaskTypes {
		NONE, HORNED, TENGU
	}
	public static final ModelLayerLocation BLOOD_LUST_HEAD_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("blood_lust_helmet"), "main");
	public static final ModelLayerLocation BLOOD_LUST_HEAD_TENGU_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("blood_lust_helmet_tengu"), "main");
	public static final ModelLayerLocation BLOOD_LUST_HEAD_HORNED_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("blood_lust_helmet_horned"), "main");
	public static final ModelLayerLocation BLOOD_LUST_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("blood_lust_chest"), "main");
	public static final ModelLayerLocation BLOOD_LUST_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("blood_lust_legs"), "main");

    public static final ModelLayerLocation BLOOD_LUST_BOOTS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("blood_lust_boots"), "main");
    public static final Lazy<BloodLustArmorModel<LivingEntity>> helmet = Lazy.of(() -> new BloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BLOOD_LUST_HEAD_LAYER)));
    public static final Lazy<BloodLustArmorModel<LivingEntity>> tengu = Lazy.of(() -> new BloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BLOOD_LUST_HEAD_TENGU_LAYER)));
    public static final Lazy<BloodLustArmorModel<LivingEntity>> horned = Lazy.of(() -> new BloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BLOOD_LUST_HEAD_HORNED_LAYER)));
    public static final Lazy<BloodLustArmorModel<LivingEntity>> chest = Lazy.of(() -> new BloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BLOOD_LUST_CHEST_LAYER)));
    public static final Lazy<BloodLustArmorModel<LivingEntity>> legs = Lazy.of(() -> new BloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BLOOD_LUST_LEGS_LAYER)));


	public static final Lazy<BloodLustArmorModel<LivingEntity>> boots = Lazy.of(() -> new BloodLustArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BLOOD_LUST_BOOTS_LAYER)));

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		// Chest
		if (slot.equals(EquipmentSlot.CHEST)) {

			PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create(),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition belt = body.addOrReplaceChild("belt",
					CubeListBuilder.create().texOffs(5, 75)
							.addBox(-4.0F, -13.0F, -3.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(39, 131)
							.addBox(-5.0F, -13.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(21, 97)
							.addBox(-4.0F, -13.0F, 2.0F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(16, 119)
							.addBox(4.0F, -13.0F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(41, 67)
							.addBox(-2.0F, -12.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(41, 67)
							.addBox(-2.0F, -14.0F, -3.0F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, 23.85F, 0.0F));

			PartDefinition lLivingSide = body.addOrReplaceChild("lLivingSide",
					CubeListBuilder.create().texOffs(122, 85)
							.addBox(0.9F, 0.75F, -3.5F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(84, 86)
							.addBox(0.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.125F)),
					PartPose.offset(0.1F, 0.0F, 0.0F));

			PartDefinition spike3 = lLivingSide.addOrReplaceChild("spike3",
					CubeListBuilder.create().texOffs(94, 109)
							.addBox(2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(9, 70)
							.addBox(1.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(-0.75F, 5.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone3 = spike3.addOrReplaceChild("bone3",
					CubeListBuilder.create().texOffs(125, 120).addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F,
							new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition spike4 = lLivingSide.addOrReplaceChild("spike4",
					CubeListBuilder.create().texOffs(94, 109)
							.addBox(2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(9, 70)
							.addBox(1.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(0.25F, 0.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone4 = spike4.addOrReplaceChild("bone4",
					CubeListBuilder.create().texOffs(125, 120).addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F,
							new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition rLivingSide = body.addOrReplaceChild("rLivingSide",
					CubeListBuilder.create().texOffs(122, 85).mirror()
							.addBox(-3.9F, 0.75F, -3.5F, 3.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(84, 86).mirror()
							.addBox(-4.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offset(-0.1F, 0.0F, 0.0F));

			PartDefinition spike7 = rLivingSide.addOrReplaceChild("spike7",
					CubeListBuilder.create().texOffs(94, 109).mirror()
							.addBox(-2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(9, 70).mirror()
							.addBox(-2.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(0.75F, 5.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone7 = spike7.addOrReplaceChild("bone7", CubeListBuilder.create().texOffs(125, 120).mirror()
					.addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition spike8 = rLivingSide.addOrReplaceChild("spike8",
					CubeListBuilder.create().texOffs(94, 109).mirror()
							.addBox(-2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(9, 70).mirror()
							.addBox(-2.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-0.25F, 0.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone8 = spike8.addOrReplaceChild("bone8", CubeListBuilder.create().texOffs(125, 120).mirror()
					.addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition rIronSide = body.addOrReplaceChild("rIronSide", CubeListBuilder.create().texOffs(131, 40)
					.addBox(-2.6F, -19.25F, -3.25F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(119, 60)
					.addBox(-4.1F, -14.0F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(120, 52)
					.addBox(-3.6F, -23.25F, -3.25F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(121, 128)
					.addBox(-4.1F, -24.0F, -3.5F, 5.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(121, 128)
					.addBox(-5.1F, -25.25F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(119, 60)
					.addBox(-4.1F, -23.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(121, 45)
					.addBox(-3.1F, -19.25F, 2.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(120, 32)
					.addBox(-4.1F, -23.25F, 2.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-0.9F, 23.8F, 0.0F));

			PartDefinition lIronSide = body.addOrReplaceChild("lIronSide", CubeListBuilder.create().texOffs(131, 40)
					.mirror().addBox(-0.4F, -19.25F, -3.25F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(119, 60).mirror().addBox(-0.9F, -14.0F, -2.5F, 5.0F, 1.0F, 5.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(120, 52).mirror()
					.addBox(-0.4F, -23.25F, -3.25F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(121, 128).mirror()
					.addBox(-0.9F, -24.0F, -3.5F, 5.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(121, 128).mirror()
					.addBox(3.1F, -25.25F, -2.5F, 2.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(119, 60).mirror().addBox(-0.9F, -23.0F, -2.5F, 5.0F, 9.0F, 5.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(121, 45).mirror()
					.addBox(0.1F, -19.25F, 2.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(120, 32).mirror().addBox(0.1F, -23.25F, 2.75F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false), PartPose.offset(0.9F, 23.8F, 0.0F));

			PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(),
					PartPose.offset(-5.0F, 2.0F, 0.0F));

			PartDefinition rArm = right_arm.addOrReplaceChild("rArm", CubeListBuilder.create().texOffs(78, 116)
					.addBox(-8.1F, -24.25F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(102, 104)
					.addBox(-7.6F, -25.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(5.0F, 22.0F, 0.0F));

			PartDefinition rArmBackShing = rArm.addOrReplaceChild("rArmBackShing",
					CubeListBuilder.create().texOffs(89, 104)
							.addBox(-2.0F, -3.0F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-1.0F, -4.0F, -1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, -1.0F, -1.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, 1.0F, -2.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-1.0F, 5.0F, -1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-5.85F, -22.5F, 3.75F, 0.3054F, 0.0F, 0.0F));

			PartDefinition rArmFrontShing = rArm.addOrReplaceChild("rArmFrontShing",
					CubeListBuilder.create().texOffs(89, 104)
							.addBox(-2.0F, -3.0F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-1.0F, -4.0F, 0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, -1.0F, 0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, 1.0F, 1.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-1.0F, 5.0F, 0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-5.85F, -22.5F, -3.75F, -0.3054F, 0.0F, 0.0F));

			PartDefinition shingle = rArm.addOrReplaceChild("shingle",
					CubeListBuilder.create().texOffs(80, 70)
							.addBox(-0.5F, -2.5F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(80, 70)
							.addBox(-1.5F, -3.5F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(80, 70)
							.addBox(-0.5F, -4.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(80, 70)
							.addBox(-0.5F, 3.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(80, 70)
							.addBox(0.5F, -0.5F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-8.6F, -21.75F, 0.0F, 0.0F, 0.0F, 0.4363F));

			PartDefinition left_arm2 = right_arm.addOrReplaceChild("left_arm2", CubeListBuilder.create(),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition lArm2 = left_arm2.addOrReplaceChild("lArm2",
					CubeListBuilder.create().texOffs(137, 83).mirror()
							.addBox(-8.65F, -22.25F, -1.25F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(102, 67).mirror()
							.addBox(-8.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offset(5.0F, 22.0F, 0.0F));

			PartDefinition spike5 = lArm2.addOrReplaceChild("spike5",
					CubeListBuilder.create().texOffs(94, 109).mirror()
							.addBox(-2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(9, 70).mirror()
							.addBox(-2.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-5.25F, -19.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone5 = spike5.addOrReplaceChild("bone5", CubeListBuilder.create().texOffs(125, 120).mirror()
					.addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition spike6 = lArm2.addOrReplaceChild("spike6",
					CubeListBuilder.create().texOffs(94, 109).mirror()
							.addBox(-2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(9, 70).mirror()
							.addBox(-2.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-3.25F, -24.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone6 = spike6.addOrReplaceChild("bone6", CubeListBuilder.create().texOffs(125, 120).mirror()
					.addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.125F)).mirror(false),
					PartPose.offsetAndRotation(-2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(),
					PartPose.offset(5.0F, 2.0F, 0.0F));

			PartDefinition lArm = left_arm.addOrReplaceChild("lArm", CubeListBuilder.create().texOffs(137, 83)
					.addBox(7.65F, -22.25F, -1.25F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(102, 67)
					.addBox(4.0F, -24.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.125F)),
					PartPose.offset(-5.0F, 22.0F, 0.0F));

			PartDefinition spike = lArm.addOrReplaceChild("spike",
					CubeListBuilder.create().texOffs(94, 109)
							.addBox(2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(9, 70)
							.addBox(1.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(5.25F, -19.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone = spike.addOrReplaceChild("bone",
					CubeListBuilder.create().texOffs(125, 120).addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F,
							new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition spike2 = lArm.addOrReplaceChild("spike2",
					CubeListBuilder.create().texOffs(94, 109)
							.addBox(2.25F, 0.5F, -3.35F, 0.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(9, 70)
							.addBox(1.5F, 0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(3.25F, -24.5F, 3.0F, 0.4363F, 0.0F, 0.0F));

			PartDefinition bone2 = spike2.addOrReplaceChild("bone2",
					CubeListBuilder.create().texOffs(125, 120).addBox(-0.5F, -0.2498F, -1.36F, 1.0F, 1.0F, 1.0F,
							new CubeDeformation(0.125F)),
					PartPose.offsetAndRotation(2.0F, 0.0F, 2.7F, 0.6981F, 0.0F, 0.0F));

			PartDefinition right_arm2 = left_arm.addOrReplaceChild("right_arm2", CubeListBuilder.create(),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition rArm2 = right_arm2.addOrReplaceChild("rArm2",
					CubeListBuilder.create().texOffs(78, 116).mirror()
							.addBox(4.1F, -24.25F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(102, 104).mirror()
							.addBox(4.6F, -25.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offset(-5.0F, 22.0F, 0.0F));

			PartDefinition rArmBackShing2 = rArm2.addOrReplaceChild("rArmBackShing2", CubeListBuilder.create()
					.texOffs(89, 104).mirror().addBox(-2.0F, -3.0F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-1.0F, -4.0F, -1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(89, 104).mirror().addBox(-2.0F, -1.0F, -1.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-2.0F, 1.0F, -2.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(89, 104).mirror().addBox(-1.0F, 5.0F, -1.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false), PartPose.offsetAndRotation(5.85F, -22.5F, 3.75F, 0.3054F, 0.0F, 0.0F));

			PartDefinition rArmFrontShing2 = rArm2.addOrReplaceChild("rArmFrontShing2", CubeListBuilder.create()
					.texOffs(89, 104).mirror().addBox(-2.0F, -3.0F, -0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-1.0F, -4.0F, 0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(89, 104).mirror().addBox(-2.0F, -1.0F, 0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-2.0F, 1.0F, 1.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(89, 104).mirror().addBox(-1.0F, 5.0F, 0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false), PartPose.offsetAndRotation(5.85F, -22.5F, -3.75F, -0.3054F, 0.0F, 0.0F));

			PartDefinition shingle2 = rArm2.addOrReplaceChild("shingle2", CubeListBuilder.create().texOffs(80, 70)
					.mirror().addBox(-0.5F, -2.5F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(80, 70).mirror().addBox(0.5F, -3.5F, -3.0F, 1.0F, 3.0F, 6.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(80, 70).mirror()
					.addBox(-0.5F, -4.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(80, 70).mirror().addBox(-0.5F, 3.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(80, 70).mirror()
					.addBox(-1.5F, -0.5F, -3.0F, 1.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offsetAndRotation(8.6F, -21.75F, 0.0F, 0.0F, 0.0F, -0.4363F));

		}
		if (slot.equals(EquipmentSlot.LEGS)) {
			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
					.texOffs(73, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(0.15F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

			PartDefinition rBackShingle = right_leg.addOrReplaceChild("rBackShingle",
					CubeListBuilder.create().texOffs(60, 53)
							.addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(60, 53)
							.addBox(-2.0F, 2.0F, -2.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(60, 53)
							.addBox(-2.0F, -0.3007F, -1.5463F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0F, 1.75F, 2.5F, 0.3054F, 0.0F, 0.0F));

			PartDefinition rLegShingle = right_leg.addOrReplaceChild("rLegShingle",
					CubeListBuilder.create().texOffs(60, 41)
							.addBox(-0.5F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(60, 41)
							.addBox(0.5F, 0.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(60, 41)
							.addBox(1.5F, 2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-2.75F, 1.75F, 0.0F, 0.0F, 0.0F, 0.3054F));

			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
					.texOffs(0, 48).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.15F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition lBackShingle = left_leg.addOrReplaceChild("lBackShingle",
					CubeListBuilder.create().texOffs(48, 53)
							.addBox(-2.0F, -2.0F, -0.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 53)
							.addBox(-2.0F, 2.0F, -2.5F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 53)
							.addBox(-2.0F, -0.3007F, -1.5463F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.2F, 1.75F, 2.5F, 0.3054F, 0.0F, 0.0F));

			PartDefinition lLegShingle = left_leg.addOrReplaceChild("lLegShingle",
					CubeListBuilder.create().texOffs(60, 32)
							.addBox(1.5F, -2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(60, 32)
							.addBox(0.5F, 0.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(60, 32)
							.addBox(-0.5F, 2.0F, -2.0F, 1.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.7F, 2.5F, 0.0F, 0.0F, 0.0F, -0.3054F));

		}
		if (slot.equals(EquipmentSlot.FEET)) {
			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create()
					.texOffs(20, 38).addBox(-2.0F, 10.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2F))
					.texOffs(40, 44).addBox(-1.1F, 10.1798F, -2.9235F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
					.texOffs(40, 44).addBox(-1.35F, 10.1798F, -2.6735F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
					.texOffs(40, 44).addBox(0.15F, 10.1798F, -2.6735F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

			PartDefinition rShin = right_leg.addOrReplaceChild("rShin", CubeListBuilder.create().texOffs(36, 28)
					.addBox(0.9167F, -3.0833F, -0.4167F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(40, 35)
					.addBox(-1.0833F, -2.8333F, -0.6667F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(30, 28)
					.addBox(-1.8333F, -3.0833F, -0.4167F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-0.0167F, 7.5833F, -1.9833F, -0.0873F, 0.0F, 0.0F));

			PartDefinition rShin2 = right_leg.addOrReplaceChild("rShin2", CubeListBuilder.create().texOffs(48, 45)
					.addBox(0.9167F, -1.8333F, -0.4167F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 35)
					.addBox(-1.0833F, -2.3333F, -0.6667F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 27)
					.addBox(-1.8333F, -1.8333F, -0.4167F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-0.0167F, 10.0833F, 2.0167F, 0.0873F, 0.0F, 0.0F));

			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create()
					.texOffs(22, 48).addBox(-2.0F, 10.0F, -2.0F, 4.0F, 2.0F, 4.0F, new CubeDeformation(0.2F))
					.texOffs(40, 44).addBox(-0.9F, 10.1798F, -2.9235F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
					.texOffs(40, 44).addBox(0.35F, 10.1798F, -2.6735F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
					.texOffs(40, 44).addBox(-1.15F, 10.1798F, -2.6735F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition lShin = left_leg.addOrReplaceChild("lShin", CubeListBuilder.create().texOffs(63, 61)
					.addBox(0.75F, -3.0833F, -0.4167F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(55, 61)
					.addBox(-1.75F, -3.0833F, -0.4167F, 1.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(40, 44)
					.addBox(-1.0F, -2.8333F, -0.6667F, 2.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.1F, 7.5833F, -1.9833F, -0.0873F, 0.0F, 0.0F));

			PartDefinition lShin2 = left_leg.addOrReplaceChild("lShin2", CubeListBuilder.create().texOffs(40, 58)
					.addBox(0.75F, -1.75F, -0.4167F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(33, 55)
					.addBox(-1.75F, -1.75F, -0.4167F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(20, 29)
					.addBox(-1.0F, -2.5F, -0.6667F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.1F, 10.0F, 2.0167F, 0.0873F, 0.0F, 0.0F));
		}

		return LayerDefinition.create(meshdefinition, 256, 256);
	}

	@SuppressWarnings("unused")
	public static LayerDefinition createHeadLayer(EquipmentSlot slot, EnumBloodLustMaskTypes maskType) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		// Helmet
		if (slot.equals(EquipmentSlot.HEAD)) {
			PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create(),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition base = head.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 96)
					.addBox(-3.0F, 1.75F, -4.45F, 6.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
					.addBox(-3.0F, 2.0F, -4.25F, 6.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(141, 50)
					.addBox(-4.9F, 3.0F, -4.35F, 2.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(100, 177)
					.addBox(-4.7F, 1.5F, -3.35F, 1.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(32, 72)
					.addBox(-5.0F, 3.0F, 1.75F, 10.0F, 3.0F, 3.0F, new CubeDeformation(-0.02F)).texOffs(22, 81)
					.addBox(-5.0F, 1.25F, 1.35F, 10.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(22, 81)
					.addBox(-4.0F, 1.25F, -2.65F, 8.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(44, 95)
					.addBox(0.0F, -2.0F, -3.1F, 0.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)).texOffs(23, 90)
					.addBox(-5.0F, 6.0F, 2.25F, 10.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 109)
					.addBox(4.2184F, 7.0107F, -1.4862F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
					.addBox(4.2184F, 6.1107F, -0.4862F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(66, 106)
					.addBox(4.2184F, 5.0107F, -1.4862F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(95, 172)
					.addBox(2.9684F, 1.0F, -4.4862F, 2.0F, 5.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 109)
					.addBox(-5.2184F, 7.0107F, -1.4862F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
					.addBox(-5.2184F, 6.1107F, -0.4862F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(66, 106)
					.addBox(-5.2184F, 5.0107F, -1.4862F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, -8.9F, 0.15F));

			PartDefinition hat = head.addOrReplaceChild("hat",
					CubeListBuilder.create().texOffs(59, 158)
							.addBox(-4.5F, -1.0F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(49, 137)
							.addBox(-5.5F, 0.0F, -5.5F, 11.0F, 1.0F, 11.0F, new CubeDeformation(0.0F)).texOffs(0, 146)
							.addBox(-3.5F, -2.0F, -3.5F, 7.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
							.addBox(-0.5F, -3.0F, -1.5F, 1.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
							.addBox(-0.5F, -2.0F, 1.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
							.addBox(-0.5F, -1.0F, -2.5F, 1.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
							.addBox(-0.5F, 0.0F, -2.5F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)).texOffs(0, 155)
							.addBox(-6.5F, 1.0F, -6.5F, 13.0F, 1.0F, 13.0F, new CubeDeformation(0.0F)).texOffs(31, 204)
							.addBox(-7.5F, 2.0F, -7.5F, 15.0F, 0.0F, 15.0F, new CubeDeformation(0.05F)),
					PartPose.offsetAndRotation(0.0F, -8.9F, 0.15F, 0.1745F, 0.0436F, -0.1745F));

			PartDefinition brim = hat.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(48, 209)
					.addBox(-5.0F, -28.9F, -6.35F, 13.0F, 0.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(52, 205)
					.addBox(-7.0F, -28.9F, -3.35F, 1.0F, 0.0F, 11.0F, new CubeDeformation(0.05F)).texOffs(52, 205)
					.addBox(9.0F, -28.9F, -3.35F, 1.0F, 0.0F, 11.0F, new CubeDeformation(0.05F)).texOffs(48, 209)
					.addBox(-5.0F, -28.9F, 9.65F, 13.0F, 0.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(-1.5F, 31.0F, -2.15F));

			PartDefinition tassle = hat.addOrReplaceChild("tassle", CubeListBuilder.create().texOffs(41, 197)
					.addBox(-0.5F, 0.9167F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(-0.5F, -0.0833F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(0.0F, -0.0833F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(7.0F, 2.1333F, -7.0F));

			PartDefinition tassle2 = hat.addOrReplaceChild("tassle2", CubeListBuilder.create().texOffs(41, 197)
					.addBox(-0.5F, 0.9167F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(-0.5F, -0.0833F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(0.0F, -0.0833F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(-7.0F, 2.1333F, -7.0F));

			PartDefinition tassle3 = hat.addOrReplaceChild("tassle3", CubeListBuilder.create().texOffs(41, 197)
					.addBox(-0.5F, 0.9167F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(-0.5F, -0.0833F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(0.0F, -0.0833F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(-7.0F, 2.1333F, 7.0F));

			PartDefinition tassle4 = hat.addOrReplaceChild("tassle4", CubeListBuilder.create().texOffs(41, 197)
					.addBox(-0.5F, 0.9167F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(-0.5F, -0.0833F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(0.0F, -0.0833F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(7.0F, 2.1333F, 7.0F));

			PartDefinition tassle5 = hat.addOrReplaceChild("tassle5", CubeListBuilder.create().texOffs(41, 197)
					.addBox(-0.5F, 0.9167F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(-0.5F, -0.0833F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(0.0F, -0.0833F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(7.0F, 2.1333F, 0.0F));

			PartDefinition tassle6 = hat.addOrReplaceChild("tassle6", CubeListBuilder.create().texOffs(41, 197)
					.addBox(-0.5F, 0.9167F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(-0.5F, -0.0833F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.05F)).texOffs(42, 192)
					.addBox(0.0F, -0.0833F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.05F)),
					PartPose.offset(-7.0F, 2.1333F, 0.0F));

			if (maskType == EnumBloodLustMaskTypes.TENGU) {
				PartDefinition mask = head.addOrReplaceChild("mask", CubeListBuilder.create(),
						PartPose.offset(-1.0F, 21.0F, 1.0F));

				PartDefinition nose = mask.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(0, 96)
						.addBox(1.0F, -24.9F, -8.1F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(1.0F, -25.9F, -7.1F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(0.5F, -25.9F, -6.1F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(0.5F, -26.9F, -5.85F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 110)
						.addBox(1.5F, -31.9F, -8.85F, 0.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(0.5F, -27.9F, -4.85F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(0.5F, -26.9F, -4.1F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
						PartPose.offset(-0.5F, 0.0F, -1.0F));

				PartDefinition chin = mask.addOrReplaceChild("chin", CubeListBuilder.create().texOffs(12, 86)
						.addBox(1.0F, -19.8F, -8.45F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(0.0F, -21.1F, -6.35F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
						PartPose.offset(-0.5F, -1.0F, 0.0F));

				PartDefinition lBrow = mask.addOrReplaceChild("lBrow",
						CubeListBuilder.create().texOffs(123, 203).addBox(-1.7F, 0.25F, -0.25F, 3.0F, 3.0F, 0.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(2.5F, -26.65F, -4.8F, 0.0F, 0.0F, -0.4363F));

				PartDefinition rBrow = mask.addOrReplaceChild("rBrow",
						CubeListBuilder.create().texOffs(0, 96).addBox(-1.5F, 0.25F, -0.25F, 3.0F, 1.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-0.5F, -26.65F, -4.8F, 0.0F, 0.0F, 0.4363F));

				PartDefinition lCheek = mask.addOrReplaceChild("lCheek", CubeListBuilder.create().texOffs(0, 96)
						.addBox(-0.7328F, 0.3306F, -1.7627F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(-0.7328F, -1.639F, -1.4154F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(-0.8F, -0.7F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(5.5309F, -23.0F, -3.4437F, 0.1745F, 0.3491F, 0.0F));

				PartDefinition lChin = lCheek.addOrReplaceChild("lChin", CubeListBuilder.create().texOffs(0, 115)
						.addBox(-2.421F, 5.2531F, -1.3545F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 115)
						.addBox(-2.1754F, 3.2835F, -1.1089F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(-1.0583F, 4.124F, -1.071F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-1.1F, -4.9F, -1.1F, 0.0F, -0.7854F, 0.0F));

				PartDefinition lCheek2 = mask.addOrReplaceChild("lCheek2", CubeListBuilder.create().texOffs(0, 96)
						.addBox(-0.3142F, 0.3335F, -1.7459F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(-0.3142F, -1.6361F, -1.3986F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
						.texOffs(0, 96).addBox(-0.3F, -0.7F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-3.4809F, -23.0F, -3.4437F, 0.1745F, -0.3491F, 0.0F));

				PartDefinition rChin = lCheek2.addOrReplaceChild("rChin", CubeListBuilder.create().texOffs(0, 115)
						.addBox(-0.4474F, 5.2561F, -1.199F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 115)
						.addBox(-0.6929F, 3.2864F, -0.9534F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 96)
						.addBox(0.2351F, 4.124F, -0.8943F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.85F, -4.9F, -1.1F, 0.0F, 0.7854F, 0.0F));

			}
			if (maskType == EnumBloodLustMaskTypes.HORNED) {
				PartDefinition mask2 = head.addOrReplaceChild("mask2",
						CubeListBuilder.create().texOffs(85, 202)
								.addBox(-2.0316F, -27.2893F, -6.5862F, 6.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
								.texOffs(66, 189)
								.addBox(-4.2816F, -26.2893F, -6.3362F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
								.texOffs(50, 186)
								.addBox(3.2184F, -28.2893F, -6.3362F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
								.texOffs(103, 201)
								.addBox(-3.8316F, -24.2893F, -5.9862F, 2.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
								.texOffs(87, 193).addBox(3.8684F, -24.2893F, -5.9862F, 2.0F, 3.0F, 4.0F,
										new CubeDeformation(0.0F)),
						PartPose.offset(-1.0F, 21.0F, 1.0F));

				PartDefinition bone9 = mask2.addOrReplaceChild("bone9",
						CubeListBuilder.create().texOffs(88, 212).addBox(-2.5F, -0.317F, -0.6797F, 6.0F, 3.0F, 1.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.4684F, -22.2893F, -5.8362F, 0.4363F, 0.0F, 0.0F));

				PartDefinition horn = mask2.addOrReplaceChild("horn",
						CubeListBuilder.create().texOffs(76, 197)
								.addBox(-1.0F, 0.1F, 1.2F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(62, 195)
								.addBox(-0.5F, -0.5F, -2.0F, 1.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-0.7816F, -27.6393F, -7.5362F, -0.6109F, 0.0F, 0.0F));

				PartDefinition horn2 = horn.addOrReplaceChild("horn2",
						CubeListBuilder.create().texOffs(63, 196).addBox(-0.5F, -1.9026F, -1.1465F, 1.0F, 1.0F, 3.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, -0.6981F, 0.0F, 0.0F));

				PartDefinition bone10 = horn2.addOrReplaceChild("bone10",
						CubeListBuilder.create().texOffs(63, 196).addBox(-0.5F, -1.4508F, -1.4845F, 1.0F, 1.0F, 3.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, -1.4026F, -2.6465F, -0.6109F, 0.0F, 0.0F));

				PartDefinition bone = horn2.addOrReplaceChild("bone",
						CubeListBuilder.create().texOffs(64, 197).addBox(0.2709F, -2.4705F, 0.2063F, 1.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 1.5974F, 0.8535F, 0.6404F, -0.284F, -0.2058F));

				PartDefinition bone12 = bone.addOrReplaceChild("bone12",
						CubeListBuilder.create().texOffs(62, 195).addBox(-0.3967F, 5.5F, -3.6956F, 1.0F, 1.0F, 4.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.7709F, -7.9705F, 0.2063F, 0.0F, -0.6545F, 0.0F));

				PartDefinition bone2 = horn2.addOrReplaceChild("bone2",
						CubeListBuilder.create().texOffs(64, 197).addBox(-1.9806F, -2.4705F, -0.9908F, 1.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 1.5974F, 0.8535F, 0.8281F, 0.6783F, 0.5996F));

				PartDefinition bone11 = bone2.addOrReplaceChild("bone11",
						CubeListBuilder.create().texOffs(63, 196).addBox(-1.8208F, -0.5F, -1.1089F, 1.0F, 1.0F, 3.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-1.4806F, -1.9705F, -2.9908F, 0.0F, 0.6545F, 0.0F));

				PartDefinition horn3 = mask2.addOrReplaceChild("horn3",
						CubeListBuilder.create().texOffs(76, 197).addBox(-1.0F, -1.1956F, -1.4927F, 2.0F, 2.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(5.2184F, -25.4674F, -4.9138F, -3.0046F, -0.8823F, 2.7071F));

				PartDefinition bone5 = horn3
						.addOrReplaceChild("bone5",
								CubeListBuilder.create().texOffs(62, 195).addBox(2.9684F, -26.0393F, -10.3362F, 1.0F,
										1.0F, 4.0F, new CubeDeformation(0.0F)),
								PartPose.offset(-3.4684F, 25.2437F, 5.8435F));

				PartDefinition horn4 = bone5.addOrReplaceChild("horn4",
						CubeListBuilder.create().texOffs(63, 196).addBox(-0.25F, -1.9026F, -1.1465F, 1.0F, 1.0F, 3.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(3.4684F, -25.5393F, -12.3362F, -0.6981F, 0.0F, 0.0F));

				PartDefinition bone3 = horn4.addOrReplaceChild("bone3",
						CubeListBuilder.create().texOffs(64, 197).addBox(0.2709F, -2.4705F, 0.2063F, 1.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 1.5974F, 0.8535F, 0.6404F, -0.284F, -0.2058F));

				PartDefinition bone4 = horn4.addOrReplaceChild("bone4", CubeListBuilder.create(),
						PartPose.offsetAndRotation(0.0F, 1.5974F, 0.8535F, 0.8281F, 0.6783F, 0.5996F));

				PartDefinition bone6 = horn3.addOrReplaceChild("bone6",
						CubeListBuilder.create().texOffs(62, 195).addBox(-1.9253F, -6.6638F, -7.3647F, 1.0F, 1.0F, 4.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 5.7044F, -2.4927F, 0.0F, -0.5672F, 0.0F));

				PartDefinition horn5 = bone6.addOrReplaceChild("horn5",
						CubeListBuilder.create().texOffs(63, 196)
								.addBox(-1.4253F, -2.942F, -9.8609F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
								.texOffs(63, 196)
								.addBox(-1.6753F, -4.942F, -7.6109F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.0F, 0.0F, -4.0F, -0.6981F, 0.0F, 0.0F));

				PartDefinition bone7 = horn5.addOrReplaceChild("bone7",
						CubeListBuilder.create().texOffs(64, 197).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(-1.1135F, -2.0915F, -7.2038F, 0.6404F, -0.284F, -0.2058F));

				PartDefinition bone13 = horn5.addOrReplaceChild("bone13",
						CubeListBuilder.create().texOffs(62, 195).addBox(-0.5F, -0.079F, -2.1556F, 1.0F, 1.0F, 4.0F,
								new CubeDeformation(0.0F)),
						PartPose.offsetAndRotation(0.1421F, -1.9325F, -9.1679F, 0.4542F, -0.6484F, -0.5593F));

				PartDefinition bone8 = horn5.addOrReplaceChild("bone8", CubeListBuilder.create(),
						PartPose.offsetAndRotation(0.0F, 1.5974F, 0.8535F, 0.8281F, 0.6783F, 0.5996F));
			}

		}
		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	public BloodLustArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			float red, float green, float blue, float alpha) {
		head.render(poseStack, buffer, packedLight, packedOverlay);
		body.render(poseStack, buffer, packedLight, packedOverlay);
		leftArm.render(poseStack, buffer, packedLight, packedOverlay);
		rightLeg.render(poseStack, buffer, packedLight, packedOverlay);
		leftLeg.render(poseStack, buffer, packedLight, packedOverlay);
		rightArm.render(poseStack, buffer, packedLight, packedOverlay);

	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
	}

}