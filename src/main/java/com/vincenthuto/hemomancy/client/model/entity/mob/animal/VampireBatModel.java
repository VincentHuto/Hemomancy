package com.vincenthuto.hemomancy.client.model.entity.mob.animal;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.mob.animal.VampireBatEntity;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class VampireBatModel extends HierarchicalModel<VampireBatEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("vampire_bat"), "main");
	private final ModelPart root;
	private final ModelPart head;
	private final ModelPart leftWing;
	private final ModelPart rightWing;
	private final ModelPart leftWingTip;
	private final ModelPart rightWingTip;

	public VampireBatModel(ModelPart bakedRoot) {
		root = bakedRoot.getChild("root");
		head = root.getChild("body").getChild("head");
		leftWing = root.getChild("left_wing");
		rightWing = root.getChild("right_wing");
		leftWingTip = leftWing.getChild("tip");
		rightWingTip = rightWing.getChild("tip");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition meshRoot = mesh.getRoot();
		PartDefinition root = meshRoot.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 16.0F, 0.0F));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -4.0F, -2.0F, 5.0F, 8.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-3.5F, -4.5F, -2.5F, 7.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.ZERO);
		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(18, 0).addBox(-3.0F, -4.0F, -2.5F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(-2.0F, -1.5F, -5.0F, 4.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, -0.5F));
		head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(54, 0).addBox(-2.0F, -5.0F, -0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -3.5F, 0.5F));
		head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(54, 0).addBox(0.0F, -5.0F, -0.5F, 2.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, -3.5F, 0.5F));
		body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(4, 13).addBox(-1.5F, 3.5F, -2.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.5F, 3.5F, 0.0F));
		body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 13).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 4.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(4, 13).addBox(-0.5F, 3.5F, -2.5F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.5F, 3.5F, 0.0F));
		PartDefinition leftWing = root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(14, 13).addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.15F))
				.texOffs(32, 13).addBox(0.0F, 0.0F, -0.5F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -3.0F, 0.0F, 0.0F, -0.15F, 0.2F));
		leftWing.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(14, 17).addBox(0.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.1F))
				.texOffs(32, 22).addBox(0.0F, 0.0F, -0.5F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 0.0F, 0.0F, 0.0F, -0.15F, 0.25F));
		PartDefinition rightWing = root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(14, 13).addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.15F))
				.texOffs(32, 13).addBox(-8.0F, 0.0F, -0.5F, 8.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.5F, -3.0F, 0.0F, 0.0F, 0.15F, -0.2F));
		rightWing.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(14, 17).addBox(-8.0F, -0.5F, -0.5F, 8.0F, 1.0F, 1.0F, new CubeDeformation(0.1F))
				.texOffs(32, 22).addBox(-8.0F, 0.0F, -0.5F, 8.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-8.0F, 0.0F, 0.0F, 0.0F, 0.15F, -0.25F));
		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override public ModelPart root() { return root; }

	@Override
	public void setupAnim(VampireBatEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		head.yRot = netHeadYaw * Mth.DEG_TO_RAD;
		head.xRot = headPitch * Mth.DEG_TO_RAD;
		if (entity.isResting()) {
			root.y -= 2.0F;
			root.xRot = Mth.PI;
			leftWing.zRot = -1.35F;
			rightWing.zRot = 1.35F;
			leftWingTip.zRot = -0.35F;
			rightWingTip.zRot = 0.35F;
			return;
		}
		float flap = Mth.cos(ageInTicks * 0.8F) * 0.75F;
		leftWing.zRot = 0.2F + flap;
		rightWing.zRot = -0.2F - flap;
		leftWingTip.zRot = 0.25F + flap * 0.55F;
		rightWingTip.zRot = -0.25F - flap * 0.55F;
		root.y += Mth.cos(ageInTicks * 0.3F) * 0.35F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, int packedColor) {
		root.render(poseStack, buffer, packedLight, packedOverlay, packedColor);
	}
}
