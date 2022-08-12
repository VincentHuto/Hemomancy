package com.vincenthuto.hemomancy.model.armor;

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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.Lazy;

public class BarbedArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation BARBED_HELMET_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "barbed_helmet"), "main");
	public static final ModelLayerLocation BARBED_CHEST_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "barbed_chest"), "main");
	public static final ModelLayerLocation BARBED_LEGS_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "barbed_leggings"), "main");
	public static final ModelLayerLocation BARBED_FEET_LAYER = new ModelLayerLocation(
			new ResourceLocation(Hemomancy.MOD_ID, "barbed_boots"), "main");

	public static final Lazy<BarbedArmorModel<LivingEntity>> helmet = Lazy
			.of(() -> new BarbedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BARBED_HELMET_LAYER)));
	public static final Lazy<BarbedArmorModel<LivingEntity>> chest = Lazy
			.of(() -> new BarbedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BARBED_CHEST_LAYER)));
	public static final Lazy<BarbedArmorModel<LivingEntity>> legs = Lazy
			.of(() -> new BarbedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BARBED_LEGS_LAYER)));
	public static final Lazy<BarbedArmorModel<LivingEntity>> boots = Lazy
			.of(() -> new BarbedArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(BARBED_FEET_LAYER)));

	public BarbedArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);

	}

	@SuppressWarnings("unused")
	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot.equals(EquipmentSlot.HEAD)) {
			PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
					.addBox(-4.0F, -8.0F, -3.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition spike5 = head.addOrReplaceChild("spike5", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-6.1667F, -9.5F, -1.6667F, 0.0F, 2.1817F, -2.3562F));

			PartDefinition spike11 = head.addOrReplaceChild("spike11", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-0.1667F, -9.5F, -3.6667F, 0.0F, 2.7053F, -1.5708F));

			PartDefinition spike12 = head.addOrReplaceChild("spike12", CubeListBuilder.create().texOffs(116, 73)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 78)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-2.1667F, -9.5F, -3.4167F, 0.0F, 2.7053F, -2.0071F));

			PartDefinition spike15 = head.addOrReplaceChild("spike15", CubeListBuilder.create().texOffs(116, 73)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(13, 78)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(1.8333F, -9.5F, -3.4167F, 0.0F, 2.7053F, -1.1345F));

			PartDefinition spike8 = head.addOrReplaceChild("spike8", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.0151F, -2.6499F, -2.9383F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.0151F, -2.1499F, -1.9383F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(110, 141)
					.addBox(-2.0151F, -2.1499F, -2.9383F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-6.1667F, -9.5F, -1.6667F, 0.0F, 2.1817F, -2.3562F));

			PartDefinition spike10 = head.addOrReplaceChild("spike10", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-0.6175F, -4.7712F, -5.2496F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-0.6175F, -4.2712F, -4.2496F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.6175F, -4.2712F, -5.2496F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-5.1667F, -9.5F, -1.6667F, 0.0F, 2.1817F, -2.3562F));

			PartDefinition spike6 = head.addOrReplaceChild("spike6", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-6.4148F, 8.2496F, 6.5168F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-6.4148F, 8.7496F, 7.5168F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-7.4148F, 8.7496F, 6.5168F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-6.1667F, -9.5F, -1.6667F, 0.0F, 2.1817F, -0.7854F));

			PartDefinition spike7 = head.addOrReplaceChild("spike7", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-6.0172F, 10.3709F, 4.2055F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-6.0172F, 10.8709F, 5.2055F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-7.0172F, 10.8709F, 4.2055F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-6.1667F, -9.5F, -1.6667F, 0.0F, 2.1817F, -0.7854F));

			PartDefinition spike9 = head.addOrReplaceChild("spike9", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-5.6197F, 12.4922F, 1.8943F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.6197F, 12.9922F, 2.8943F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-6.6197F, 12.9922F, 1.8943F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-7.1667F, -9.5F, -1.6667F, 0.0F, 2.1817F, -0.7854F));

		}

		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot.equals(EquipmentSlot.CHEST)) {
			PartDefinition body = partdefinition.addOrReplaceChild("body",
					CubeListBuilder.create().texOffs(16, 16)
							.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(29, 95)
							.addBox(-4.0F, 0.0F, -3.0F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(27, 92)
							.addBox(-3.0F, 6.0F, -3.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(27, 92)
							.addBox(-3.0F, 6.0F, 2.0F, 6.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(26, 96)
							.addBox(-4.0F, 0.0F, 2.0F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition spike13 = body.addOrReplaceChild("spike13", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.2049F, -1.8333F, -0.5359F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.2049F, -1.3333F, 0.4641F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.2049F, -1.3333F, -0.5359F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-1.9167F, 2.5F, 4.3333F, 0.0F, 0.6545F, -1.5708F));

			PartDefinition spike16 = body.addOrReplaceChild("spike16", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-5.1717F, -1.8333F, -3.5797F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.1717F, -1.3333F, -2.5797F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-6.1717F, -1.3333F, -3.5797F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.0833F, 2.5F, 4.3333F, 0.0F, 0.6545F, -1.5708F));

			PartDefinition spike14 = body.addOrReplaceChild("spike14", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.2049F, 0.1667F, -0.5359F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.2049F, 0.6667F, 0.4641F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.2049F, 0.6667F, -0.5359F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(2.5833F, 2.5F, 4.3333F, 0.0F, 0.6545F, -1.5708F));

			PartDefinition spike17 = body.addOrReplaceChild("spike17", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-5.1717F, 0.1667F, -3.5797F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.1717F, 0.6667F, -2.5797F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-6.1717F, 0.6667F, -3.5797F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.5833F, 2.5F, 4.3333F, 0.0F, 0.6545F, -1.5708F));

			PartDefinition spike18 = body.addOrReplaceChild("spike18", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-5.1717F, 0.1667F, -3.5797F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.1717F, 0.6667F, -2.5797F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-6.1717F, 0.6667F, -3.5797F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-0.6667F, -2.5F, 4.3333F, 0.0F, 0.6545F, -1.5708F));

			PartDefinition spike23 = body.addOrReplaceChild("spike23", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-5.1717F, 0.1667F, -3.5797F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.1717F, 0.6667F, -2.5797F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(118, 141)
					.addBox(-6.1717F, 0.6667F, -3.5797F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-0.6667F, 7.5F, -6.6667F, 0.0F, 3.1416F, -1.5708F));

			PartDefinition spike24 = body.addOrReplaceChild("spike24", CubeListBuilder.create().texOffs(116, 73)
					.addBox(-5.1717F, 0.1667F, -3.5797F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.1717F, 0.6667F, -2.5797F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(118, 141)
					.addBox(-6.1717F, 0.6667F, -3.5797F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-2.6667F, 5.5F, -6.6667F, 0.0F, 3.1416F, -1.5708F));

			PartDefinition spike25 = body.addOrReplaceChild("spike25", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-5.1717F, 0.1667F, -3.5797F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-5.1717F, 0.6667F, -2.5797F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(118, 141)
					.addBox(-6.1717F, 0.6667F, -3.5797F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(1.3333F, 5.5F, -6.6667F, 0.0F, 3.1416F, -1.5708F));

			PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm",
					CubeListBuilder.create().texOffs(40, 16)
							.addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(42, 89)
							.addBox(-3.9F, -1.0F, -1.0F, 1.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(42, 89)
							.addBox(-2.0F, -1.0F, 1.9F, 2.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(145, 84)
							.addBox(-1.0F, -1.0F, 1.9F, 0.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-5.0F, 2.0F, 0.0F));

			PartDefinition spike3 = right_arm.addOrReplaceChild("spike3", CubeListBuilder.create().texOffs(13, 78)
					.addBox(0.1667F, -0.5F, -0.3333F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(0.1667F, 0.0F, 0.6667F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-0.8333F, 0.0F, -0.3333F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-1.1667F, -1.5F, 2.3333F, 0.0F, 1.3963F, -2.3562F));

			PartDefinition spike2 = right_arm.addOrReplaceChild("spike2", CubeListBuilder.create().texOffs(13, 78)
					.addBox(0.1667F, -0.5F, -0.3333F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(0.1667F, 0.0F, 0.6667F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-0.8333F, 0.0F, -0.3333F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(0.8333F, -1.5F, 2.3333F, 0.0F, 1.3963F, -2.3562F));

			PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm",
					CubeListBuilder.create().texOffs(42, 89)
							.addBox(2.9F, -1.0F, -1.0F, 1.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(42, 89)
							.addBox(0.0F, -1.0F, 1.9F, 2.0F, 9.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(145, 84)
							.addBox(1.0F, -1.0F, 1.9F, 0.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(40, 16)
							.mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
							.mirror(false),
					PartPose.offset(5.0F, 2.0F, 0.0F));

			PartDefinition spike4 = left_arm.addOrReplaceChild("spike4", CubeListBuilder.create().texOffs(13, 78)
					.addBox(0.1667F, -0.5F, -0.3333F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(0.1667F, 0.0F, 0.6667F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-0.8333F, 0.0F, -0.3333F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(1.8333F, -1.5F, 2.3333F, 0.0F, 1.3963F, -0.7854F));

			PartDefinition spike = left_arm.addOrReplaceChild("spike", CubeListBuilder.create().texOffs(13, 78)
					.addBox(0.1667F, -0.5F, -0.3333F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(0.1667F, 0.0F, 0.6667F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(213, 136)
					.addBox(-0.8333F, 0.0F, -0.3333F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-0.1667F, -1.5F, 2.3333F, 0.0F, 1.3963F, -0.7854F));

		}
		if (slot.equals(EquipmentSlot.LEGS)) {

			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(50, 116)
							.addBox(-1.6F, -0.9F, -2.3F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-1.6F, 7.1F, -2.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(150, 71)
							.addBox(-0.1F, 3.1F, -3.1F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-2.6F, -1.9F, -1.45F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

			PartDefinition RightLeg = right_leg.addOrReplaceChild("RightLeg",
					CubeListBuilder.create().texOffs(0, 16).mirror()
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(0, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition spike20 = right_leg.addOrReplaceChild("spike20", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-2.1167F, 6.5F, 0.3333F, 2.7925F, 3.1416F, -1.5708F));

			PartDefinition spike22 = right_leg.addOrReplaceChild("spike22", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-2.1167F, 0.5F, 2.3333F, 2.7925F, 3.1416F, -1.5708F));

			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(50, 116)
							.addBox(-1.4F, -0.9F, -2.3F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(1.6F, -1.9F, -1.45F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-1.4F, 7.1F, -2.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(150, 71)
							.addBox(0.1F, 3.1F, -3.1F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition spike19 = left_leg.addOrReplaceChild("spike19", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(2.0833F, 6.5F, 0.3333F, -2.7925F, 3.1416F, -1.5708F));

			PartDefinition spike21 = left_leg.addOrReplaceChild("spike21", CubeListBuilder.create().texOffs(13, 78)
					.addBox(-1.4127F, -0.5286F, -0.627F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-1.4127F, -0.0286F, 0.373F, 4.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(116, 73)
					.addBox(-2.4127F, -0.0286F, -0.627F, 6.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(2.0833F, 0.5F, 2.3333F, -2.7925F, 3.1416F, -1.5708F));

			PartDefinition LeftLeg = left_leg.addOrReplaceChild("LeftLeg",
					CubeListBuilder.create().texOffs(0, 16)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(0, 48)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));
		}
		if (slot.equals(EquipmentSlot.FEET)) {

			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(38, 91)
							.addBox(-1.9F, 10.1F, -2.75F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(40, 89)
							.addBox(-0.9F, 11.1F, -5.75F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(143, 84)
							.addBox(0.1F, 10.1F, -5.75F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(40, 89)
							.addBox(-1.9F, 11.1F, 2.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 108)
							.addBox(1.6F, 9.6F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(151, 103)
							.addBox(1.6F, 10.6F, -3.0F, 2.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(40, 89)
							.addBox(-2.1F, 11.1F, 2.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 108)
							.addBox(-2.6F, 9.6F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(151, 103)
							.addBox(-3.6F, 10.6F, -3.0F, 2.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(39, 97)
							.addBox(-2.1F, 10.1F, -2.75F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(46, 123)
							.addBox(-1.1F, 11.1F, -5.75F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(149, 118)
							.addBox(-0.1F, 10.1F, -5.75F, 0.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

		}
		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
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
}
