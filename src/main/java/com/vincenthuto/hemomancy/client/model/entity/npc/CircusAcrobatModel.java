package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusAcrobatEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class CircusAcrobatModel extends HumanoidModel<CircusAcrobatEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("circus_acrobat"), "main");
	private final ModelPart aerialRibbon;
	private final ModelPart leftTail;
	private final ModelPart rightTail;

	public CircusAcrobatModel(ModelPart root) {
		super(root);
		aerialRibbon = body.getChild("aerial_ribbon");
		leftTail = head.getChild("left_tail");
		rightTail = head.getChild("right_tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.6F, -7.6F, -3.6F, 7.2F, 7.6F, 7.2F)
				.texOffs(32, 0).addBox(-3.6F, -7.6F, -3.6F, 7.2F, 7.6F, 7.2F, new CubeDeformation(0.25F))
				.texOffs(64, 0).addBox(-3.9F, -5.2F, -4.2F, 7.8F, 3.2F, 0.6F), PartPose.offset(0.0F, 1.0F, 0.0F));
		head.addOrReplaceChild("left_tail", CubeListBuilder.create()
				.texOffs(36, 74).addBox(-1.3F, -1.3F, -1.0F, 2.6F, 2.6F, 7.0F, new CubeDeformation(-0.15F))
				.texOffs(64, 58).addBox(-1.3F, -1.3F, 5.2F, 2.6F, 2.6F, 2.6F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(2.2F, -6.7F, 0.3F, 0.25F, 0.25F, -0.55F));
		head.addOrReplaceChild("right_tail", CubeListBuilder.create()
				.texOffs(36, 74).mirror().addBox(-1.3F, -1.3F, -1.0F, 2.6F, 2.6F, 7.0F, new CubeDeformation(-0.15F))
				.texOffs(64, 58).mirror().addBox(-1.3F, -1.3F, 5.2F, 2.6F, 2.6F, 2.6F, new CubeDeformation(-0.4F)),
				PartPose.offsetAndRotation(-2.2F, -6.7F, 0.3F, 0.25F, -0.25F, 0.55F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 16).addBox(-3.2F, 0.0F, -1.8F, 6.4F, 11.0F, 3.6F)
				.texOffs(16, 52).addBox(-3.2F, 0.0F, -1.8F, 6.4F, 11.0F, 3.6F, new CubeDeformation(0.12F))
				.texOffs(88, 0).addBox(-3.8F, 1.2F, -2.3F, 7.6F, 1.0F, 4.6F)
				.texOffs(88, 6).addBox(-3.8F, 7.4F, -2.3F, 7.6F, 1.0F, 4.6F), PartPose.offset(0.0F, 1.0F, 0.0F));
		body.addOrReplaceChild("aerial_ribbon", CubeListBuilder.create()
				.texOffs(96, 16).addBox(-0.5F, 0.0F, -0.3F, 1.0F, 13.0F, 0.6F)
				.texOffs(102, 16).addBox(-2.5F, 11.0F, -0.25F, 5.0F, 7.0F, 0.5F),
				PartPose.offsetAndRotation(0.0F, 6.5F, 2.0F, 0.12F, 0.0F, 0.0F));
		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(64, 32).addBox(-2.5F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F)
				.texOffs(72, 16).addBox(-2.5F, -2.0F, -1.5F, 3.0F, 5.5F, 3.0F, new CubeDeformation(0.25F))
				.texOffs(59, 48).addBox(-3.2F, 4.2F, -2.2F, 4.4F, 0.0F, 4.4F), PartPose.offset(-4.0F, 3.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(40, 16).addBox(-0.5F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F)
				.texOffs(56, 16).addBox(-0.5F, -2.0F, -1.5F, 3.0F, 5.5F, 3.0F, new CubeDeformation(0.25F))
				.texOffs(59, 48).addBox(-1.2F, 4.2F, -2.2F, 4.4F, 0.0F, 4.4F), PartPose.offset(4.0F, 3.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(16, 32).addBox(-1.6F, 0.0F, -1.6F, 3.2F, 6.0F, 3.2F)
				.texOffs(0, 42).addBox(-1.6F, 6.0F, -1.6F, 3.2F, 5.0F, 3.2F, new CubeDeformation(-0.08F))
				.texOffs(32, 32).addBox(-1.6F, 0.0F, -1.6F, 3.2F, 6.0F, 3.2F, new CubeDeformation(0.22F)),
				PartPose.offset(-1.5F, 13.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(16, 32).mirror().addBox(-1.6F, 0.0F, -1.6F, 3.2F, 6.0F, 3.2F)
				.texOffs(16, 42).mirror().addBox(-1.6F, 6.0F, -1.6F, 3.2F, 5.0F, 3.2F, new CubeDeformation(-0.08F))
				.texOffs(48, 32).mirror().addBox(-1.6F, 0.0F, -1.6F, 3.2F, 6.0F, 3.2F, new CubeDeformation(0.22F)),
				PartPose.offset(1.5F, 13.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(CircusAcrobatEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float flutter = Mth.sin(ageInTicks * 0.22F) * 0.12F;
		aerialRibbon.xRot = 0.12F + flutter;
		leftTail.zRot = -0.55F + flutter;
		rightTail.zRot = 0.55F - flutter;
		if (entity.getActState() == ActState.SETUP || entity.getActState() == ActState.PERFORM) {
			leftArm.xRot = rightArm.xRot = -2.65F;
			leftArm.zRot = -0.15F;
			rightArm.zRot = 0.15F;
		}
		if (entity.getActState() == ActState.DOWNED) {
			body.xRot = 1.35F;
			leftArm.xRot = rightArm.xRot = -0.5F;
		}
	}
}
