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
				.texOffs(0, 0).addBox(-3.4F, -7.8F, -3.4F, 6.8F, 7.8F, 6.8F), PartPose.ZERO);
		head.addOrReplaceChild("left_tail", CubeListBuilder.create()
				.texOffs(94, 0).addBox(-0.55F, -0.5F, -0.5F, 1.1F, 1.0F, 5.0F)
				.texOffs(108, 0).addBox(-0.9F, -0.8F, 3.8F, 1.8F, 1.6F, 2.0F),
				PartPose.offsetAndRotation(2.4F, -6.7F, 1.7F, 0.22F, 0.18F, -0.45F));
		head.addOrReplaceChild("right_tail", CubeListBuilder.create()
				.texOffs(94, 0).mirror().addBox(-0.55F, -0.5F, -0.5F, 1.1F, 1.0F, 5.0F)
				.texOffs(108, 0).mirror().addBox(-0.9F, -0.8F, 3.8F, 1.8F, 1.6F, 2.0F),
				PartPose.offsetAndRotation(-2.4F, -6.7F, 1.7F, 0.22F, -0.18F, 0.45F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-3.3F, 0.0F, -1.8F, 6.6F, 11.5F, 3.6F)
				.texOffs(24, 20).addBox(-2.4F, 1.5F, -2.15F, 4.8F, 8.5F, 0.6F)
				.texOffs(44, 20).addBox(-3.45F, 0.0F, -2.05F, 6.9F, 1.25F, 4.1F)
				.texOffs(66, 20).addBox(-3.5F, 8.6F, -2.1F, 7.0F, 1.2F, 4.2F), PartPose.ZERO);
		body.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(44, 28)
				.addBox(-1.0F, 0.0F, -0.3F, 1.3F, 6.8F, 0.6F),
				PartPose.offsetAndRotation(-1.55F, 0.8F, -2.15F, 0.0F, 0.0F, -0.2F));
		body.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(50, 28).mirror()
				.addBox(-0.3F, 0.0F, -0.3F, 1.3F, 6.8F, 0.6F),
				PartPose.offsetAndRotation(1.55F, 0.8F, -2.15F, 0.0F, 0.0F, 0.2F));
		body.addOrReplaceChild("right_harness", CubeListBuilder.create(),
				PartPose.offsetAndRotation(-2.0F, 1.1F, 1.95F, 0.0F, 0.0F, -0.12F));
		body.addOrReplaceChild("left_harness", CubeListBuilder.create(),
				PartPose.offsetAndRotation(2.0F, 1.1F, 1.95F, 0.0F, 0.0F, 0.12F));
		body.addOrReplaceChild("right_coat_tail", CubeListBuilder.create()
				.texOffs(66, 28).addBox(-2.6F, 0.0F, -0.5F, 2.6F, 6.8F, 1.0F),
				PartPose.offsetAndRotation(-0.15F, 9.4F, 1.45F, 0.12F, 0.0F, 0.08F));
		body.addOrReplaceChild("left_coat_tail", CubeListBuilder.create()
				.texOffs(76, 28).mirror().addBox(0.0F, 0.0F, -0.5F, 2.6F, 6.8F, 1.0F),
				PartPose.offsetAndRotation(0.15F, 9.4F, 1.45F, 0.12F, 0.0F, -0.08F));
		body.addOrReplaceChild("aerial_ribbon", CubeListBuilder.create()
				.texOffs(84, 28).addBox(-1.5F, -0.6F, -0.4F, 3.0F, 1.2F, 0.8F)
				.texOffs(96, 20).addBox(-0.55F, 0.2F, -0.25F, 1.1F, 7.5F, 0.5F)
				.texOffs(102, 20).addBox(-1.45F, 7.2F, -0.22F, 2.9F, 5.4F, 0.45F)
				.texOffs(114, 20).addBox(-1.7F, 12.2F, -0.3F, 3.4F, 1.0F, 0.6F),
				PartPose.offsetAndRotation(0.0F, 3.8F, 2.0F, 0.1F, 0.0F, 0.0F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 42).addBox(-2.5F, -2.0F, -1.55F, 3.2F, 12.0F, 3.1F), PartPose.offset(-4.0F, 2.4F, 0.0F));
		rightArm.addOrReplaceChild("right_shoulder", CubeListBuilder.create()
				.texOffs(30, 42).addBox(-2.9F, -1.35F, -2.05F, 4.2F, 1.8F, 4.1F), PartPose.ZERO);
		rightArm.addOrReplaceChild("right_bracer", CubeListBuilder.create()
				.texOffs(66, 42).addBox(-2.55F, -0.5F, -1.65F, 3.3F, 2.7F, 3.3F)
				.texOffs(80, 42).addBox(-2.65F, -0.8F, -1.75F, 3.5F, 0.7F, 3.5F), PartPose.offset(0.0F, 7.7F, 0.0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(14, 42).mirror().addBox(-0.7F, -2.0F, -1.55F, 3.2F, 12.0F, 3.1F), PartPose.offset(4.0F, 2.4F, 0.0F));
		leftArm.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
				.texOffs(30, 42).mirror().addBox(-1.3F, -1.35F, -2.05F, 4.2F, 1.8F, 4.1F), PartPose.ZERO);
		leftArm.addOrReplaceChild("left_bracer", CubeListBuilder.create()
				.texOffs(66, 42).mirror().addBox(-0.75F, -0.5F, -1.65F, 3.3F, 2.7F, 3.3F)
				.texOffs(80, 42).mirror().addBox(-0.85F, -0.8F, -1.75F, 3.5F, 0.7F, 3.5F), PartPose.offset(0.0F, 7.7F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 60).addBox(-1.7F, 0.0F, -1.7F, 3.4F, 6.0F, 3.4F), PartPose.offset(-1.6F, 12.0F, 0.0F));
		PartDefinition rightCalf = rightLeg.addOrReplaceChild("right_calf", CubeListBuilder.create()
				.texOffs(30, 60).addBox(-1.6F, 0.0F, -1.55F, 3.2F, 6.0F, 3.1F), PartPose.offset(0.0F, 6.0F, 0.0F));
		rightCalf.addOrReplaceChild("right_boot", CubeListBuilder.create()
				.texOffs(48, 60).addBox(-1.85F, 0.0F, -2.6F, 3.7F, 1.5F, 4.3F)
				.texOffs(66, 60).addBox(-1.95F, -0.65F, -1.8F, 3.9F, 1.1F, 3.6F), PartPose.offset(0.0F, 4.5F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(14, 60).mirror().addBox(-1.7F, 0.0F, -1.7F, 3.4F, 6.0F, 3.4F), PartPose.offset(1.6F, 12.0F, 0.0F));
		PartDefinition leftCalf = leftLeg.addOrReplaceChild("left_calf", CubeListBuilder.create()
				.texOffs(38, 60).mirror().addBox(-1.6F, 0.0F, -1.55F, 3.2F, 6.0F, 3.1F), PartPose.offset(0.0F, 6.0F, 0.0F));
		leftCalf.addOrReplaceChild("left_boot", CubeListBuilder.create()
				.texOffs(48, 60).mirror().addBox(-1.85F, 0.0F, -2.6F, 3.7F, 1.5F, 4.3F)
				.texOffs(66, 60).mirror().addBox(-1.95F, -0.65F, -1.8F, 3.9F, 1.1F, 3.6F), PartPose.offset(0.0F, 4.5F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(CircusAcrobatEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float flutter = Mth.sin(ageInTicks * 0.22F) * 0.12F;
		aerialRibbon.xRot = 0.1F + flutter;
		leftTail.zRot = -0.45F + flutter;
		rightTail.zRot = 0.45F - flutter;
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
