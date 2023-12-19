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

public class ChitiniteArmorModel<T extends LivingEntity> extends HumanoidModel<T> {

	public static final ModelLayerLocation CHITINITE_HELMET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("chitinite_helmet"), "main");
	public static final ModelLayerLocation CHITINITE_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("chitinite_chest"), "main");
	public static final ModelLayerLocation CHITINITE_LEGS_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("chitinite_leggings"), "main");
	public static final ModelLayerLocation CHITINITE_FEET_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("chitinite_boots"), "main");

	public static final Lazy<ChitiniteArmorModel<LivingEntity>> helmet = Lazy.of(() -> new ChitiniteArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(CHITINITE_HELMET_LAYER)));
	public static final Lazy<ChitiniteArmorModel<LivingEntity>> chest = Lazy.of(() -> new ChitiniteArmorModel<>(
			Minecraft.getInstance().getEntityModels().bakeLayer(CHITINITE_CHEST_LAYER)));
	public static final Lazy<ChitiniteArmorModel<LivingEntity>> legs = Lazy.of(
			() -> new ChitiniteArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(CHITINITE_LEGS_LAYER)));
	public static final Lazy<ChitiniteArmorModel<LivingEntity>> boots = Lazy.of(
			() -> new ChitiniteArmorModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(CHITINITE_FEET_LAYER)));

	@SuppressWarnings("unused")
	public static LayerDefinition createBodyLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot.equals(EquipmentSlot.CHEST)) {
			PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
					.addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));

			PartDefinition scale = body.addOrReplaceChild("scale",
					CubeListBuilder.create().texOffs(64, 90)
							.addBox(3.0F, -20.5F, -3.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(64, 90)
							.addBox(3.0F, -21.3F, -3.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(64, 90)
							.addBox(-5.0F, -20.5F, -3.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(64, 90)
							.addBox(-5.0F, -21.3F, -3.75F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(28, 91)
							.addBox(-4.0F, -24.0F, -4.5F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(28, 91)
							.addBox(-3.0F, -25.0F, -4.0F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(28, 91)
							.addBox(-4.0F, -24.0F, -2.5F, 8.0F, 4.0F, 4.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 24.0F, 0.0F));

			PartDefinition scale2 = body.addOrReplaceChild("scale2",
					CubeListBuilder.create().texOffs(64, 90)
							.addBox(3.25F, -21.3F, -3.85F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(64, 90)
							.addBox(-4.25F, -21.3F, -3.85F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F))
							.texOffs(28, 91).addBox(-3.0F, -24.0F, -4.5F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 27.0F, 0.5F));

			PartDefinition scale3 = body.addOrReplaceChild("scale3",
					CubeListBuilder.create().texOffs(64, 90)
							.addBox(2.75F, -21.3F, -3.85F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(64, 90)
							.addBox(-3.75F, -21.3F, -3.85F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.25F))
							.texOffs(28, 91).addBox(-3.0F, -24.0F, -4.5F, 6.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 30.5F, 1.5F));

			PartDefinition shell = body.addOrReplaceChild("shell", CubeListBuilder.create().texOffs(3, 85)
					.addBox(-4.0F, -14.5F, -3.75F, 8.0F, 3.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(0, 96)
					.addBox(-3.0F, -22.0F, 0.25F, 6.0F, 8.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(0, 116)
					.addBox(-4.5F, -22.5F, -1.75F, 9.0F, 9.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(21, 109)
					.addBox(-4.0F, -22.5F, -1.75F, 8.0F, 9.0F, 2.0F, new CubeDeformation(0.25F)).texOffs(1, 73)
					.addBox(-4.0F, -16.5F, -1.75F, 8.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(18, 75)
					.addBox(-3.5F, -23.5F, -1.75F, 7.0F, 4.0F, 1.0F, new CubeDeformation(0.25F)).texOffs(0, 81)
					.addBox(-5.0F, -24.0F, -4.0F, 10.0F, 11.0F, 2.0F, new CubeDeformation(0.25F)),
					PartPose.offset(0.0F, 22.0F, 6.0F));

			PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(),
					PartPose.offset(5.0F, 2.0F, 0.0F));

			PartDefinition rArm2 = left_arm.addOrReplaceChild("rArm2",
					CubeListBuilder.create().texOffs(78, 116).mirror()
							.addBox(4.1F, -24.25F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false)
							.texOffs(102, 104).mirror()
							.addBox(4.6F, -25.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offset(-5.0F, 22.0F, 0.0F));

			PartDefinition rArmBackShing2 = rArm2.addOrReplaceChild("rArmBackShing2", CubeListBuilder.create()
					.texOffs(89, 104).mirror().addBox(-2.0F, -3.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-1.0F, -4.0F, -1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(89, 104).mirror().addBox(-2.0F, -1.0F, -1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-2.0F, 1.0F, -2.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offsetAndRotation(5.85F, -22.5F, 3.75F, 0.3054F, 0.0F, 0.0F));

			PartDefinition rArmFrontShing2 = rArm2.addOrReplaceChild("rArmFrontShing2", CubeListBuilder.create()
					.texOffs(89, 104).mirror().addBox(-2.0F, -3.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-1.0F, -4.0F, 0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(89, 104).mirror().addBox(-2.0F, -1.0F, 0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(89, 104).mirror()
					.addBox(-2.0F, 1.0F, 1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offsetAndRotation(5.85F, -22.5F, -3.75F, -0.3054F, 0.0F, 0.0F));

			PartDefinition shingle2 = rArm2.addOrReplaceChild("shingle2", CubeListBuilder.create().texOffs(80, 70)
					.mirror().addBox(-4.5F, -3.5F, -3.0F, 5.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false)
					.texOffs(80, 70).mirror().addBox(-3.5F, -4.5F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F))
					.mirror(false).texOffs(80, 70).mirror()
					.addBox(-1.25F, -0.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false),
					PartPose.offset(8.6F, -21.75F, 0.0F));

			PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(),
					PartPose.offset(-5.0F, 2.0F, 0.0F));

			PartDefinition right_arm3 = right_arm.addOrReplaceChild("right_arm3", CubeListBuilder.create()
					.texOffs(78, 116).addBox(-8.1F, -24.25F, -2.5F, 4.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
					.texOffs(102, 104).addBox(-7.6F, -25.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(5.0F, 22.0F, 0.0F));

			PartDefinition rArmBackShing = right_arm3.addOrReplaceChild("rArmBackShing",
					CubeListBuilder.create().texOffs(89, 104)
							.addBox(-2.0F, -3.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, -4.0F, -1.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, -1.0F, -1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, 1.0F, -2.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-5.85F, -22.5F, 3.75F, 0.3054F, 0.0F, 0.0F));

			PartDefinition rArmFrontShing = right_arm3.addOrReplaceChild("rArmFrontShing",
					CubeListBuilder.create().texOffs(89, 104)
							.addBox(-2.0F, -3.0F, -0.5F, 4.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, -4.0F, 0.5F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, -1.0F, 0.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(89, 104)
							.addBox(-2.0F, 1.0F, 1.5F, 4.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offsetAndRotation(-5.85F, -22.5F, -3.75F, -0.3054F, 0.0F, 0.0F));

			PartDefinition shingle = right_arm3.addOrReplaceChild("shingle",
					CubeListBuilder.create().texOffs(80, 70)
							.addBox(-0.5F, -3.5F, -3.0F, 4.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(80, 70)
							.addBox(0.5F, -4.5F, -2.0F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).texOffs(80, 70)
							.addBox(0.25F, -0.5F, -3.0F, 1.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-8.6F, -21.75F, 0.0F));

		}
		if (slot.equals(EquipmentSlot.LEGS)) {

			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(50, 116)
							.addBox(-1.4F, -0.9F, -2.3F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(50, 116)
							.addBox(-1.4F, 3.1F, -2.2F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(1.6F, 6.1F, -1.45F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(51, 94)
							.addBox(1.85F, 6.6F, -0.95F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(1.6F, -0.9F, -1.45F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(1.35F, 3.1F, -1.45F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(49, 116)
							.addBox(-1.4F, 8.1F, -2.45F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-1.4F, 7.1F, -2.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(46, 91)
							.addBox(-1.4F, -0.9F, 1.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(56, 99)
							.addBox(-0.9F, -0.9F, 2.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(38, 77)
							.addBox(-0.9F, 6.1F, 2.25F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(27, 102)
							.addBox(-0.4F, 6.6F, 3.25F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(29, 88)
							.addBox(-1.4F, 3.1F, 1.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(35, 79)
							.addBox(-1.4F, 7.1F, 1.3F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
							.mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
							.mirror(false),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(50, 116)
							.addBox(-1.6F, -0.9F, -2.3F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(50, 116)
							.addBox(-1.6F, 3.1F, -2.2F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(49, 116)
							.addBox(-1.6F, 8.1F, -2.45F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-1.6F, 7.1F, -2.1F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(43, 95)
							.addBox(-1.6F, -0.9F, 1.75F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(32, 91)
							.addBox(-1.6F, 3.1F, 1.5F, 3.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(46, 91)
							.addBox(-1.6F, 7.1F, 1.3F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-2.6F, 6.1F, -1.45F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-2.6F, -0.9F, -1.45F, 1.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(47, 76)
							.addBox(-2.35F, 3.1F, -1.45F, 1.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).texOffs(56, 99)
							.addBox(-1.1F, -0.9F, 2.75F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(38, 75)
							.addBox(-1.1F, 6.1F, 2.25F, 2.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(27, 102)
							.addBox(-0.6F, 6.6F, 3.25F, 1.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(43, 74)
							.addBox(-2.85F, 6.6F, -0.95F, 1.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).texOffs(0, 16)
							.addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));


		}
		if (slot.equals(EquipmentSlot.FEET)) {

			PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg",
					CubeListBuilder.create().texOffs(38, 91)
							.addBox(-1.9F, 10.1F, -2.75F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(40, 89)
							.addBox(-1.9F, 11.1F, -3.75F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(40, 89)
							.addBox(-1.9F, 11.1F, 2.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(48, 108)
							.addBox(1.6F, 10.6F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)),
					PartPose.offset(1.9F, 12.0F, 0.0F));

			PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg",
					CubeListBuilder.create().texOffs(48, 108)
							.addBox(-2.6F, 10.6F, -3.0F, 1.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)).texOffs(39, 97)
							.addBox(-2.1F, 10.1F, -2.75F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.0F)).texOffs(46, 123)
							.addBox(-2.1F, 11.1F, -3.75F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)).texOffs(46, 123)
							.addBox(-2.1F, 11.1F, 2.25F, 4.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
					PartPose.offset(-1.9F, 12.0F, 0.0F));

		}
		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	@SuppressWarnings("unused")
	public static LayerDefinition createHeadLayer(EquipmentSlot slot) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();
		if (slot.equals(EquipmentSlot.HEAD)) {
			PartDefinition head = partdefinition.addOrReplaceChild("head",
					CubeListBuilder.create().texOffs(0, 0)
							.addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(1.0F)).texOffs(39, 101)
							.addBox(5.0F, -7.0F, -2.0F, 0.0F, 4.0F, 5.0F, new CubeDeformation(1.0F)).texOffs(26, 102)
							.addBox(-5.0F, -7.0F, -2.0F, 0.0F, 4.0F, 5.0F, new CubeDeformation(1.0F)).texOffs(35, 82)
							.addBox(-2.5F, -9.0F, -3.75F, 5.0F, 0.0F, 7.0F, new CubeDeformation(1.0F)).texOffs(45, 105)
							.addBox(-2.5F, -7.5F, 4.75F, 5.0F, 4.0F, 0.0F, new CubeDeformation(1.0F)),
					PartPose.offset(0.0F, 0.0F, 0.0F));
		}

		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	public ChitiniteArmorModel(ModelPart root) {
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
