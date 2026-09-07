package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class VeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "veinwing_vulture"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart leftForewing;
	private final ModelPart rightForewing;
	private final ModelPart tail;
	private final ModelPart leftTalon;
	private final ModelPart rightTalon;

	public VeinwingVultureModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.leftForewing = this.leftWing.getChild("left_forewing");
		this.rightForewing = this.rightWing.getChild("right_forewing");
		this.tail = this.root.getChild("tail");
		this.leftTalon = this.root.getChild("left_talon");
		this.rightTalon = this.root.getChild("right_talon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-2F, -5F, -1.5F, 4F, 8F, 3F)
				.texOffs(78, 0).addBox(-3F, -5.5F, -1F, 6F, 2F, 2F)
				.texOffs(94, 0).addBox(-0.5F, -7F, -0.5F, 1F, 3F, 1F),
				PartPose.offsetAndRotation(0F, 15F, 0F, 0F, 0F, 0F));

		PartDefinition breastKeel = root.addOrReplaceChild("breast_keel", CubeListBuilder.create()
				.texOffs(30, 0).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F),
				PartPose.offsetAndRotation(0F, -3F, -1.6F, 0F, 0F, 0F));

		PartDefinition rib_0 = root.addOrReplaceChild("rib_0", CubeListBuilder.create()
				.texOffs(92, 4).addBox(-2F, 0F, -0.5F, 4F, 1F, 1F),
				PartPose.offsetAndRotation(0F, -3F, -1.7F, 0F, 0F, 0F));

		PartDefinition rib_1 = root.addOrReplaceChild("rib_1", CubeListBuilder.create()
				.texOffs(92, 4).addBox(-2F, 0F, -0.5F, 4F, 1F, 1F),
				PartPose.offsetAndRotation(0F, -1F, -1.7F, 0F, 0F, 0F));

		PartDefinition rib_2 = root.addOrReplaceChild("rib_2", CubeListBuilder.create()
				.texOffs(92, 4).addBox(-2F, 0F, -0.5F, 4F, 1F, 1F),
				PartPose.offsetAndRotation(0F, 1F, -1.7F, 0F, 0F, 0F));

		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(34, 0).addBox(-2F, -2F, -3F, 4F, 3F, 3F)
				.texOffs(98, 0).addBox(-1.5F, -2.5F, -4F, 3F, 2F, 2F)
				.texOffs(48, 0).addBox(-1F, -0.5F, -8F, 2F, 1F, 5F)
				.texOffs(124, 3).addBox(-2F, -1.2F, -3.5F, 1F, 1F, 1F)
				.texOffs(124, 3).addBox(1F, -1.2F, -3.5F, 1F, 1F, 1F),
				PartPose.offsetAndRotation(0F, -6F, -1F, 0F, 0F, 0F));

		PartDefinition beakHook = head.addOrReplaceChild("beak_hook", CubeListBuilder.create()
				.texOffs(124, 0).addBox(-0.5F, 0F, -1F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(0F, 0F, -7.5F, 0.28F, 0F, 0F));

		PartDefinition rightWing = root.addOrReplaceChild("right_wing", CubeListBuilder.create()
				.texOffs(108, 0).addBox(-1F, -1F, -1F, 2F, 2F, 2F)
				.texOffs(102, 4).addBox(-6F, -0.5F, -0.5F, 6F, 1F, 1F),
				PartPose.offsetAndRotation(-2F, -4F, 0F, 0F, 0F, 0F));

		PartDefinition rightForewing = rightWing.addOrReplaceChild("right_forewing", CubeListBuilder.create()
				.texOffs(34, 6).addBox(-9F, -0.5F, -0.5F, 9F, 1F, 1F),
				PartPose.offsetAndRotation(-6F, 0F, 0F, 0F, 0.1F, 0.3F));

		PartDefinition rightMembrane_0 = rightForewing.addOrReplaceChild("right_membrane_0", CubeListBuilder.create()
				.texOffs(116, 0).addBox(-3F, 0.5F, 0F, 3F, 3F, 1F)
				.texOffs(62, 0).addBox(-3F, 0F, -0.2F, 1F, 5F, 1F),
				PartPose.offsetAndRotation(-0.2F, 0F, 0F, 0F, 0F, 0.09F));

		PartDefinition rightMembrane_1 = rightForewing.addOrReplaceChild("right_membrane_1", CubeListBuilder.create()
				.texOffs(66, 0).addBox(-3F, 0.5F, 0F, 3F, 5F, 1F)
				.texOffs(18, 0).addBox(-3F, 0F, -0.2F, 1F, 7F, 1F),
				PartPose.offsetAndRotation(-3.2F, 0F, 0F, 0F, 0F, 0.09F));

		PartDefinition rightMembrane_2 = rightForewing.addOrReplaceChild("right_membrane_2", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-3F, 0.5F, 0F, 3F, 7F, 1F)
				.texOffs(14, 0).addBox(-3F, 0F, -0.2F, 1F, 9F, 1F),
				PartPose.offsetAndRotation(-6.2F, 0F, 0F, 0F, 0F, 0.09F));

		PartDefinition rightTalon = root.addOrReplaceChild("right_talon", CubeListBuilder.create()
				.texOffs(94, 0).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F)
				.texOffs(78, 4).addBox(-1F, 2.5F, -3F, 1F, 1F, 3F)
				.texOffs(78, 4).addBox(0.5F, 2.5F, -3F, 1F, 1F, 3F)
				.texOffs(86, 4).addBox(-0.5F, 2.5F, 0F, 1F, 1F, 2F),
				PartPose.offsetAndRotation(-1.2F, 3F, 0F, 0F, 0F, 0F));

		PartDefinition leftWing = root.addOrReplaceChild("left_wing", CubeListBuilder.create()
				.texOffs(108, 0).addBox(-1F, -1F, -1F, 2F, 2F, 2F)
				.texOffs(102, 4).addBox(0F, -0.5F, -0.5F, 6F, 1F, 1F),
				PartPose.offsetAndRotation(2F, -4F, 0F, 0F, 0F, 0F));

		PartDefinition leftForewing = leftWing.addOrReplaceChild("left_forewing", CubeListBuilder.create()
				.texOffs(34, 6).addBox(0F, -0.5F, -0.5F, 9F, 1F, 1F),
				PartPose.offsetAndRotation(6F, 0F, 0F, 0F, -0.1F, -0.3F));

		PartDefinition leftMembrane_0 = leftForewing.addOrReplaceChild("left_membrane_0", CubeListBuilder.create()
				.texOffs(116, 0).addBox(0F, 0.5F, 0F, 3F, 3F, 1F)
				.texOffs(62, 0).addBox(2F, 0F, -0.2F, 1F, 5F, 1F),
				PartPose.offsetAndRotation(0.2F, 0F, 0F, 0F, 0F, -0.09F));

		PartDefinition leftMembrane_1 = leftForewing.addOrReplaceChild("left_membrane_1", CubeListBuilder.create()
				.texOffs(66, 0).addBox(0F, 0.5F, 0F, 3F, 5F, 1F)
				.texOffs(18, 0).addBox(2F, 0F, -0.2F, 1F, 7F, 1F),
				PartPose.offsetAndRotation(3.2F, 0F, 0F, 0F, 0F, -0.09F));

		PartDefinition leftMembrane_2 = leftForewing.addOrReplaceChild("left_membrane_2", CubeListBuilder.create()
				.texOffs(22, 0).addBox(0F, 0.5F, 0F, 3F, 7F, 1F)
				.texOffs(14, 0).addBox(2F, 0F, -0.2F, 1F, 9F, 1F),
				PartPose.offsetAndRotation(6.2F, 0F, 0F, 0F, 0F, -0.09F));

		PartDefinition leftTalon = root.addOrReplaceChild("left_talon", CubeListBuilder.create()
				.texOffs(94, 0).addBox(-0.5F, 0F, -0.5F, 1F, 3F, 1F)
				.texOffs(78, 4).addBox(-1F, 2.5F, -3F, 1F, 1F, 3F)
				.texOffs(78, 4).addBox(0.5F, 2.5F, -3F, 1F, 1F, 3F)
				.texOffs(86, 4).addBox(-0.5F, 2.5F, 0F, 1F, 1F, 2F),
				PartPose.offsetAndRotation(1.2F, 3F, 0F, 0F, 0F, 0F));

		PartDefinition tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
				.texOffs(74, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(0F, 3F, 1F, 0F, 0F, 0F));

		PartDefinition tailCord = tail.addOrReplaceChild("tail_cord", CubeListBuilder.create()
				.texOffs(74, 0).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(0F, 3.5F, 0F, 0.2F, 0F, 0F));

		PartDefinition tailNeedle = tailCord.addOrReplaceChild("tail_needle", CubeListBuilder.create()
				.texOffs(124, 0).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
				PartPose.offsetAndRotation(0F, 3.5F, 0F, 0.3F, 0F, 0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(VeinwingVultureEntity entity, float limbSwing, float limbSwingAmount,
						  float ageInTicks, float netHeadYaw, float headPitch) {
		float flap = Mth.sin(ageInTicks * 0.55F) * 0.45F;
		this.root.y = 15.0F + Mth.sin(ageInTicks * 0.16F) * 0.6F;
		this.head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		this.head.xRot = headPitch * Mth.DEG_TO_RAD * 0.7F;
		this.leftWing.zRot = 0.25F + flap;
		this.rightWing.zRot = -0.25F - flap;
		this.leftWing.yRot = -0.18F;
		this.rightWing.yRot = 0.18F;
		this.leftForewing.zRot = -0.3F - flap * 0.35F;
		this.rightForewing.zRot = 0.3F + flap * 0.35F;
		this.tail.xRot = 0.18F + Mth.sin(ageInTicks * 0.22F) * 0.08F;
		this.leftTalon.xRot = Mth.cos(ageInTicks * 0.18F) * 0.12F;
		this.rightTalon.xRot = -this.leftTalon.xRot;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight,
							   int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
