package com.vincenthuto.hemomancy.client.model.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
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

public class VestmentOfTheFinalMoltArmorModel<T extends LivingEntity> extends HumanoidModel<T> {
	public static final ModelLayerLocation VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER = new ModelLayerLocation(
			Hemomancy.rloc("vestment_of_the_final_molt_chest"), "main");
	public static final Lazy<VestmentOfTheFinalMoltArmorModel<LivingEntity>> chest = Lazy.of(
			() -> new VestmentOfTheFinalMoltArmorModel<>(
					Minecraft.getInstance().getEntityModels().bakeLayer(VESTMENT_OF_THE_FINAL_MOLT_CHEST_LAYER)));

	private final ModelPart hoodUp;
	private final ModelPart hoodDown;
	private final ModelPart leftWingPanel;
	private final ModelPart rightWingPanel;
	private final ModelPart centerTailPanel;
	private final ModelPart leftSleevePanel;
	private final ModelPart rightSleevePanel;

	public VestmentOfTheFinalMoltArmorModel(ModelPart root) {
		super(root, RenderType::entityTranslucent);
		this.hoodUp = this.head.getChild("hoodUp");
		this.hoodDown = this.body.getChild("hoodDown");
		this.leftWingPanel = this.body.getChild("leftWingPanel");
		this.rightWingPanel = this.body.getChild("rightWingPanel");
		this.centerTailPanel = this.body.getChild("centerTailPanel");
		this.leftSleevePanel = this.leftArm.getChild("leftSleevePanel");
		this.rightSleevePanel = this.rightArm.getChild("rightSleevePanel");
	}

	public void setHoodRaised(boolean hoodRaised) {
		this.hoodUp.visible = hoodRaised;
		this.hoodDown.visible = !hoodRaised;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);
		head.addOrReplaceChild("hoodUp",
				CubeListBuilder.create()
						.texOffs(0, 0).addBox(-5.5F, -10.5F, -5.0F, 11.0F, 11.0F, 10.0F, new CubeDeformation(0.0F))
						.texOffs(42, 0).addBox(-4.5F, -9.5F, -5.75F, 9.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(62, 0).addBox(-6.5F, -7.5F, -1.5F, 2.0F, 8.0F, 7.0F, new CubeDeformation(0.0F))
						.texOffs(80, 0).mirror().addBox(4.5F, -7.5F, -1.5F, 2.0F, 8.0F, 7.0F,
								new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 22).addBox(-4.5F, 0.0F, -2.7F, 9.0F, 12.0F, 5.4F, new CubeDeformation(0.0F))
				.texOffs(34, 22).addBox(-2.5F, 1.0F, -3.4F, 5.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(48, 22).addBox(-1.5F, 3.0F, -4.1F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.ZERO);
		body.addOrReplaceChild("hoodDown",
				CubeListBuilder.create()
						.texOffs(0, 40).addBox(-6.0F, -1.0F, 1.5F, 12.0F, 5.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(32, 40).addBox(-5.0F, -0.75F, -3.5F, 10.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2618F, 0.0F, 0.0F));
		body.addOrReplaceChild("leftWingPanel",
				CubeListBuilder.create()
						.texOffs(0, 52).addBox(0.0F, 0.0F, 0.0F, 9.0F, 22.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(20, 52).addBox(1.0F, 4.0F, -0.1F, 6.0F, 5.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(34, 52).addBox(3.0F, 12.0F, -0.1F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(3.5F, 1.0F, 2.6F, 0.1745F, -0.2793F, -0.0698F));
		body.addOrReplaceChild("rightWingPanel",
				CubeListBuilder.create()
						.texOffs(0, 76).mirror().addBox(-9.0F, 0.0F, 0.0F, 9.0F, 22.0F, 1.0F,
								new CubeDeformation(0.0F)).mirror(false)
						.texOffs(20, 76).mirror().addBox(-7.0F, 4.0F, -0.1F, 6.0F, 5.0F, 1.0F,
								new CubeDeformation(0.0F)).mirror(false)
						.texOffs(34, 76).mirror().addBox(-7.0F, 12.0F, -0.1F, 4.0F, 4.0F, 1.0F,
								new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-3.5F, 1.0F, 2.6F, 0.1745F, 0.2793F, 0.0698F));
		body.addOrReplaceChild("centerTailPanel",
				CubeListBuilder.create()
						.texOffs(58, 22).addBox(-2.5F, 0.0F, -0.5F, 5.0F, 22.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(70, 22).addBox(-1.5F, 14.0F, -0.65F, 3.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(0.0F, 10.0F, -3.0F, -0.0698F, 0.0F, 0.0F));
		body.addOrReplaceChild("leftFrontFold",
				CubeListBuilder.create().texOffs(82, 22).addBox(0.0F, 0.0F, -0.5F, 4.0F, 16.0F, 1.0F,
						new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(1.0F, 3.0F, -3.1F, -0.0524F, 0.0F, -0.0698F));
		body.addOrReplaceChild("rightFrontFold",
				CubeListBuilder.create().texOffs(92, 22).mirror().addBox(-4.0F, 0.0F, -0.5F, 4.0F, 16.0F, 1.0F,
						new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(-1.0F, 3.0F, -3.1F, -0.0524F, 0.0F, 0.0698F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm",
				CubeListBuilder.create().texOffs(104, 22).addBox(-3.0F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F,
						new CubeDeformation(0.0F)),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("rightSleevePanel",
				CubeListBuilder.create()
						.texOffs(104, 40).addBox(-5.5F, 0.0F, -0.5F, 6.0F, 14.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(118, 40).addBox(-4.5F, 6.0F, -0.65F, 4.0F, 4.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offsetAndRotation(-1.0F, 2.0F, 2.25F, 0.1571F, 0.0F, 0.2094F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm",
				CubeListBuilder.create().texOffs(128, 22).mirror().addBox(-2.0F, -2.0F, -2.5F, 5.0F, 12.0F, 5.0F,
						new CubeDeformation(0.0F)).mirror(false),
				PartPose.offset(5.0F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("leftSleevePanel",
				CubeListBuilder.create()
						.texOffs(128, 40).mirror().addBox(-0.5F, 0.0F, -0.5F, 6.0F, 14.0F, 1.0F,
								new CubeDeformation(0.0F)).mirror(false)
						.texOffs(142, 40).mirror().addBox(0.5F, 6.0F, -0.65F, 4.0F, 4.0F, 1.0F,
								new CubeDeformation(0.0F)).mirror(false),
				PartPose.offsetAndRotation(1.0F, 2.0F, 2.25F, 0.1571F, 0.0F, -0.2094F));

		root.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.offset(1.9F, 12.0F, 0.0F));

		return LayerDefinition.create(mesh, 256, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		boolean hoodRaised = entity.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
		setHoodRaised(hoodRaised);

		float idleSway = Mth.sin(ageInTicks * 0.08F) * 0.035F;
		float walkSway = Mth.sin(limbSwing * 0.6662F) * limbSwingAmount * 0.08F;
		this.leftWingPanel.zRot = -0.0698F - idleSway - walkSway;
		this.rightWingPanel.zRot = 0.0698F + idleSway + walkSway;
		this.leftWingPanel.yRot = -0.2793F + idleSway;
		this.rightWingPanel.yRot = 0.2793F - idleSway;
		this.centerTailPanel.xRot = -0.0698F + idleSway + (limbSwingAmount * 0.04F);
		this.leftSleevePanel.zRot = -0.2094F - (walkSway * 0.75F);
		this.rightSleevePanel.zRot = 0.2094F + (walkSway * 0.75F);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
			int packedColor) {
		this.head.visible = this.hoodUp.visible;
		this.hat.visible = false;
		renderPart(this.head, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.body, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.rightArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
		renderPart(this.leftArm, poseStack, buffer, packedLight, packedOverlay, packedColor);
	}

	private static void renderPart(ModelPart part, PoseStack poseStack, VertexConsumer buffer, int packedLight,
			int packedOverlay, int packedColor) {
		if (part.visible) {
			part.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
		}
	}
}
