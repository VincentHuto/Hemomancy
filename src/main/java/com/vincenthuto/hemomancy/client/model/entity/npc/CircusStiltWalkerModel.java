package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusStiltWalkerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class CircusStiltWalkerModel extends HumanoidModel<CircusStiltWalkerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("circus_stilt_walker"), "main");
	private final ModelPart leftCord;
	private final ModelPart rightCord;
	private final ModelPart crown;

	public CircusStiltWalkerModel(ModelPart root) {
		super(root);
		leftCord = body.getChild("left_cord");
		rightCord = body.getChild("right_cord");
		crown = head.getChild("crown");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 8.0F, 7.0F), PartPose.offset(0.0F, -24.0F, 0.0F));
		head.addOrReplaceChild("crown", CubeListBuilder.create()
				.texOffs(66, 64).addBox(-0.7F, -1.0F, -0.2F, 1.4F, 3.2F, 0.8F)
				.texOffs(74, 64).addBox(-0.6F, 1.2F, 0.2F, 1.2F, 2.6F, 3.8F),
				PartPose.offsetAndRotation(0.0F, -7.0F, 1.6F, 0.35F, 0.0F, 0.0F));
		root.addOrReplaceChild("hat", CubeListBuilder.create()
				.texOffs(0, 64).addBox(-4.5F, -8.0F, -4.0F, 9.0F, 0.8F, 8.0F)
				.texOffs(36, 64).addBox(-3.5F, -7.2F, -3.2F, 7.0F, 3.2F, 6.4F)
				.texOffs(88, 68).addBox(-3.65F, -5.3F, -3.35F, 7.3F, 1.2F, 6.7F),
				PartPose.offset(0.0F, -24.0F, 0.0F));
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 16).addBox(-3.6F, 0.0F, -2.0F, 7.2F, 12.0F, 4.0F)
				.texOffs(24, 16).addBox(-2.5F, 2.1F, -2.35F, 5.0F, 8.8F, 0.7F)
				.texOffs(50, 16).addBox(-3.7F, 0.2F, -2.25F, 7.4F, 1.4F, 4.5F)
				.texOffs(60, 22).addBox(-3.75F, 8.0F, -2.3F, 7.5F, 1.4F, 0.7F),
				PartPose.offset(0.0F, -24.0F, 0.0F));
		body.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(38, 16)
				.addBox(-1.2F, 0.0F, -0.3F, 1.5F, 7.8F, 0.6F),
				PartPose.offsetAndRotation(-1.8F, 1.2F, -2.25F, 0.0F, 0.0F, -0.18F));
		body.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(44, 16).mirror()
				.addBox(-0.3F, 0.0F, -0.3F, 1.5F, 7.8F, 0.6F),
				PartPose.offsetAndRotation(1.8F, 1.2F, -2.25F, 0.0F, 0.0F, 0.18F));
		body.addOrReplaceChild("gold_clasps", CubeListBuilder.create()
				.texOffs(58, 16).addBox(-0.35F, 0.0F, -0.3F, 0.7F, 0.7F, 0.6F)
				.texOffs(58, 18).addBox(-0.35F, 2.2F, -0.3F, 0.7F, 0.7F, 0.6F)
				.texOffs(58, 20).addBox(-0.35F, 4.4F, -0.3F, 0.7F, 0.7F, 0.6F),
				PartPose.offset(0.0F, 3.2F, -2.3F));
		body.addOrReplaceChild("right_coat_tail", CubeListBuilder.create().texOffs(64, 16)
				.addBox(-3.0F, 0.0F, -0.6F, 3.0F, 9.0F, 1.2F),
				PartPose.offsetAndRotation(-0.15F, 10.4F, 1.7F, 0.08F, 0.0F, 0.06F));
		body.addOrReplaceChild("left_coat_tail", CubeListBuilder.create().texOffs(76, 16).mirror()
				.addBox(0.0F, 0.0F, -0.6F, 3.0F, 9.0F, 1.2F),
				PartPose.offsetAndRotation(0.15F, 10.4F, 1.7F, 0.08F, 0.0F, -0.06F));
		body.addOrReplaceChild("stilt_brace", CubeListBuilder.create()
				.texOffs(88, 8).addBox(-4.5F, -0.7F, -2.45F, 9.0F, 1.4F, 4.9F)
				.texOffs(88, 15).addBox(-5.0F, -1.1F, -0.8F, 1.0F, 2.2F, 1.6F)
				.texOffs(94, 15).addBox(4.0F, -1.1F, -0.8F, 1.0F, 2.2F, 1.6F),
				PartPose.offset(0.0F, 10.6F, 0.0F));
		body.addOrReplaceChild("left_cord", CubeListBuilder.create().texOffs(112, 0)
				.addBox(-0.3F, 0.0F, -0.25F, 0.6F, 14.0F, 0.5F), PartPose.offset(2.9F, 1.2F, 2.3F));
		body.addOrReplaceChild("right_cord", CubeListBuilder.create().texOffs(112, 0).mirror()
				.addBox(-0.3F, 0.0F, -0.25F, 0.6F, 14.0F, 0.5F), PartPose.offset(-2.9F, 1.2F, 2.3F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 36).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(68, 36).addBox(-3.1F, 7.0F, -2.1F, 4.2F, 2.0F, 4.2F),
				PartPose.offset(-4.8F, -22.0F, 0.0F));
		rightArm.addOrReplaceChild("right_shoulder", CubeListBuilder.create()
				.texOffs(50, 36).addBox(-3.5F, -1.4F, -2.6F, 5.5F, 2.2F, 5.2F), PartPose.ZERO);
		rightArm.addOrReplaceChild("right_glove", CubeListBuilder.create().texOffs(80, 36)
				.addBox(-2.8F, -0.5F, -2.0F, 3.8F, 3.5F, 4.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(16, 36).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(68, 36).mirror().addBox(-1.1F, 7.0F, -2.1F, 4.2F, 2.0F, 4.2F),
				PartPose.offset(4.8F, -22.0F, 0.0F));
		leftArm.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
				.texOffs(50, 36).mirror().addBox(-2.0F, -1.4F, -2.6F, 5.5F, 2.2F, 5.2F), PartPose.ZERO);
		leftArm.addOrReplaceChild("left_glove", CubeListBuilder.create().texOffs(80, 36).mirror()
				.addBox(-1.0F, -0.5F, -2.0F, 3.8F, 3.5F, 4.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 54).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
				PartPose.offset(-1.9F, -12.0F, 0.0F));
		PartDefinition rightCalf = rightLeg.addOrReplaceChild("right_calf", CubeListBuilder.create()
				.texOffs(32, 54).addBox(-1.9F, 0.0F, -1.8F, 3.8F, 6.0F, 3.6F), PartPose.offset(0.0F, 6.0F, 0.0F));
		PartDefinition rightBoot = rightCalf.addOrReplaceChild("right_boot", CubeListBuilder.create()
				.texOffs(64, 54).addBox(-2.15F, 0.0F, -3.0F, 4.3F, 3.0F, 5.0F)
				.texOffs(64, 63).addBox(-2.25F, -0.7F, -2.1F, 4.5F, 1.2F, 4.2F), PartPose.offset(0.0F, 5.0F, 0.0F));
		rightBoot.addOrReplaceChild("right_stilt", CubeListBuilder.create()
				.texOffs(96, 24).addBox(-2.5F, -0.4F, -3.2F, 5.0F, 1.0F, 5.6F)
				.texOffs(96, 33).addBox(-0.7F, 0.5F, -0.7F, 1.4F, 20.5F, 1.4F)
				.texOffs(104, 42).addBox(-1.1F, 8.0F, -1.1F, 2.2F, 1.2F, 2.2F)
				.texOffs(104, 48).addBox(-2.2F, 20.5F, -3.0F, 4.4F, 1.5F, 5.0F), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(16, 54).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F),
				PartPose.offset(1.9F, -12.0F, 0.0F));
		PartDefinition leftCalf = leftLeg.addOrReplaceChild("left_calf", CubeListBuilder.create()
				.texOffs(48, 54).mirror().addBox(-1.9F, 0.0F, -1.8F, 3.8F, 6.0F, 3.6F), PartPose.offset(0.0F, 6.0F, 0.0F));
		PartDefinition leftBoot = leftCalf.addOrReplaceChild("left_boot", CubeListBuilder.create()
				.texOffs(64, 54).mirror().addBox(-2.15F, 0.0F, -3.0F, 4.3F, 3.0F, 5.0F)
				.texOffs(64, 63).mirror().addBox(-2.25F, -0.7F, -2.1F, 4.5F, 1.2F, 4.2F), PartPose.offset(0.0F, 5.0F, 0.0F));
		leftBoot.addOrReplaceChild("left_stilt", CubeListBuilder.create()
				.texOffs(96, 24).mirror().addBox(-2.5F, -0.4F, -3.2F, 5.0F, 1.0F, 5.6F)
				.texOffs(96, 33).mirror().addBox(-0.7F, 0.5F, -0.7F, 1.4F, 20.5F, 1.4F)
				.texOffs(104, 42).mirror().addBox(-1.1F, 8.0F, -1.1F, 2.2F, 1.2F, 2.2F)
				.texOffs(104, 48).mirror().addBox(-2.2F, 20.5F, -3.0F, 4.4F, 1.5F, 5.0F), PartPose.offset(0.0F, 2.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(CircusStiltWalkerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		head.y = body.y = -24.0F;
		rightArm.y = leftArm.y = -22.0F;
		rightLeg.y = leftLeg.y = -12.0F;
		rightLeg.xRot *= 0.15F;
		leftLeg.xRot *= 0.15F;
		float sway = Mth.sin(ageInTicks * 0.08F) * 0.08F;
		leftCord.zRot = sway * 0.7F;
		rightCord.zRot = -sway * 0.7F;
		crown.zRot = sway * 1.4F;
		if (entity.getActState() == ActState.PERFORM) {
			leftArm.zRot = -0.7F;
			rightArm.zRot = 0.7F;
			if (entity.isSpinning()) {
				rightLeg.xRot = leftLeg.xRot = 0.0F;
				rightLeg.zRot = 0.0F;
				leftLeg.zRot = -Mth.PI / 4.0F;
			}
		}
		if (entity.getActState() == ActState.ALERT) leftArm.xRot = rightArm.xRot = -0.55F;
		if (entity.getActState() == ActState.DOWNED) {
			body.xRot = 1.1F;
			rightLeg.xRot = leftLeg.xRot = -1.1F;
		}
	}
}
