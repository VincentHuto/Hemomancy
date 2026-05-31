package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.VeinwingVultureEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

public class VeinwingVultureModel extends EntityModel<VeinwingVultureEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(Hemomancy.rloc("veinwing_vulture"), "main");

	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart tail;
	private final ModelPart leftTalon;
	private final ModelPart rightTalon;

	public VeinwingVultureModel(ModelPart root) {
		this.root = root.getChild("root");
		this.head = this.root.getChild("head");
		this.leftWing = this.root.getChild("left_wing");
		this.rightWing = this.root.getChild("right_wing");
		this.tail = this.root.getChild("tail");
		this.leftTalon = this.root.getChild("left_talon");
		this.rightTalon = this.root.getChild("right_talon");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition root = part.addOrReplaceChild("root", CubeListBuilder.create()
						.texOffs(0, 0).addBox(-2.0F, -5.0F, -1.5F, 4.0F, 9.0F, 3.0F, new CubeDeformation(0.15F))
						.texOffs(18, 0).addBox(-1.0F, -7.0F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, 15.0F, 0.0F));

		root.addOrReplaceChild("head", CubeListBuilder.create()
						.texOffs(0, 16).addBox(-2.5F, -2.5F, -3.5F, 5.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(20, 16).addBox(-1.0F, -0.5F, -7.5F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
						.texOffs(33, 16).addBox(-0.5F, 1.0F, -9.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, -6.0F, -0.8F));

		root.addOrReplaceChild("left_wing", CubeListBuilder.create()
						.texOffs(0, 28).addBox(0.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(0, 34).addBox(2.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(6, 34).addBox(5.0F, -0.5F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(12, 34).addBox(8.0F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(2.0F, -4.0F, 0.0F));

		root.addOrReplaceChild("right_wing", CubeListBuilder.create()
						.texOffs(32, 28).addBox(-12.0F, -1.0F, -1.0F, 12.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
						.texOffs(32, 34).addBox(-3.0F, -0.5F, -0.5F, 1.0F, 9.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(38, 34).addBox(-6.0F, -0.5F, -0.5F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(44, 34).addBox(-9.0F, -0.5F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-2.0F, -4.0F, 0.0F));

		root.addOrReplaceChild("tail", CubeListBuilder.create()
						.texOffs(48, 0).addBox(-0.5F, 0.0F, 0.0F, 1.0F, 11.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(54, 0).addBox(-1.0F, 9.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.05F)),
				PartPose.offset(0.0F, 3.0F, 1.0F));

		root.addOrReplaceChild("left_talon", CubeListBuilder.create()
						.texOffs(64, 0).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(70, 0).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(1.2F, 3.0F, -0.4F));

		root.addOrReplaceChild("right_talon", CubeListBuilder.create()
						.texOffs(64, 6).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
						.texOffs(70, 6).addBox(-0.5F, 3.0F, -3.5F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
				PartPose.offset(-1.2F, 3.0F, -0.4F));

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
