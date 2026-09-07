package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFireEaterEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class CircusFireEaterModel extends HumanoidModel<CircusFireEaterEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_fire_eater"), "main");
	private final ModelPart throatFrame;
	private final ModelPart leftCowl;
	private final ModelPart rightCowl;

	public CircusFireEaterModel(ModelPart root) {
		super(root);
		throatFrame = head.getChild("throat_frame");
		leftCowl = head.getChild("left_cowl");
		rightCowl = head.getChild("right_cowl");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.4F, -7.8F, -3.4F, 6.8F, 7.8F, 6.8F), PartPose.ZERO);
		head.addOrReplaceChild("throat_frame", CubeListBuilder.create()
				.texOffs(94, 0).addBox(-3.6F, -1.1F, -3.55F, 7.2F, 0.8F, 1.0F)
				.texOffs(94, 4).addBox(-3.65F, -1.0F, -3.45F, 0.65F, 3.0F, 1.1F)
				.texOffs(100, 4).addBox(3.0F, -1.0F, -3.45F, 0.65F, 3.0F, 1.1F), PartPose.ZERO);
		head.addOrReplaceChild("left_cowl", CubeListBuilder.create()
				.texOffs(94, 10).addBox(-0.7F, -0.7F, -0.8F, 1.4F, 1.4F, 4.8F)
				.texOffs(106, 10).addBox(-0.9F, -0.9F, 3.4F, 1.8F, 1.8F, 2.2F),
				PartPose.offsetAndRotation(2.3F, -7.1F, 1.2F, 0.28F, 0.15F, -0.3F));
		head.addOrReplaceChild("right_cowl", CubeListBuilder.create()
				.texOffs(94, 10).mirror().addBox(-0.7F, -0.7F, -0.8F, 1.4F, 1.4F, 4.8F)
				.texOffs(106, 10).mirror().addBox(-0.9F, -0.9F, 3.4F, 1.8F, 1.8F, 2.2F),
				PartPose.offsetAndRotation(-2.3F, -7.1F, 1.2F, 0.28F, -0.15F, 0.3F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-3.4F, 0.0F, -1.8F, 6.8F, 11.5F, 3.6F)
				.texOffs(24, 20).addBox(-2.45F, 1.0F, -2.15F, 4.9F, 8.5F, 0.6F)
				.texOffs(44, 20).addBox(-3.55F, 0.0F, -2.05F, 7.1F, 1.25F, 4.1F)
				.texOffs(66, 20).addBox(-3.55F, 9.4F, -2.1F, 7.1F, 1.2F, 4.2F)
				.texOffs(44, 28).addBox(-2.6F, 0.7F, -2.45F, 1.3F, 6.7F, 0.6F)
				.texOffs(50, 28).addBox(1.3F, 0.7F, -2.45F, 1.3F, 6.7F, 0.6F), PartPose.ZERO);
		body.addOrReplaceChild("right_coat_tail", CubeListBuilder.create().texOffs(66, 28)
				.addBox(-2.6F, 0.0F, -0.5F, 2.5F, 6.3F, 1.0F), PartPose.offsetAndRotation(0.0F, 9.8F, 1.45F, 0.1F, 0.0F, 0.04F));
		body.addOrReplaceChild("left_coat_tail", CubeListBuilder.create().texOffs(76, 28)
				.addBox(0.1F, 0.0F, -0.5F, 2.5F, 6.3F, 1.0F), PartPose.offsetAndRotation(0.0F, 9.8F, 1.45F, 0.1F, 0.0F, -0.04F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 42).addBox(-3.2F, -2.0F, -1.55F, 3.2F, 12.0F, 3.1F), PartPose.offset(-3.4F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("right_shoulder", CubeListBuilder.create()
				.texOffs(30, 42).addBox(-3.8F, -2.75F, -2.05F, 4.2F, 1.8F, 4.1F), PartPose.ZERO);
		rightArm.addOrReplaceChild("right_gauntlet", CubeListBuilder.create()
				.texOffs(66, 42).addBox(-3.3F, 6.2F, -1.65F, 3.3F, 2.7F, 3.3F)
				.texOffs(80, 42).addBox(-3.45F, 5.9F, -1.75F, 3.5F, 0.7F, 3.5F)
				.texOffs(96, 42).addBox(-3.7F, 6.5F, -2.0F, 0.55F, 2.1F, 4.0F), PartPose.ZERO);

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(14, 42).addBox(0.0F, -2.0F, -1.55F, 3.2F, 12.0F, 3.1F), PartPose.offset(3.4F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
				.texOffs(30, 42).mirror().addBox(-0.4F, -2.75F, -2.05F, 4.2F, 1.8F, 4.1F), PartPose.ZERO);
		leftArm.addOrReplaceChild("left_gauntlet", CubeListBuilder.create()
				.texOffs(66, 42).mirror().addBox(0.0F, 6.2F, -1.65F, 3.3F, 2.7F, 3.3F)
				.texOffs(80, 42).mirror().addBox(-0.05F, 5.9F, -1.75F, 3.5F, 0.7F, 3.5F)
				.texOffs(96, 42).mirror().addBox(3.15F, 6.5F, -2.0F, 0.55F, 2.1F, 4.0F), PartPose.ZERO);

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 60).addBox(-3.4F, 0.0F, -1.7F, 3.4F, 6.0F, 3.4F), PartPose.offset(-0.1F, 12.0F, 0.0F));
		PartDefinition rightCalf = rightLeg.addOrReplaceChild("right_calf", CubeListBuilder.create()
				.texOffs(30, 60).addBox(-3.2F, 0.0F, -1.55F, 3.2F, 6.0F, 3.1F), PartPose.offset(0.0F, 6.0F, 0.0F));
		rightCalf.addOrReplaceChild("right_boot", CubeListBuilder.create()
				.texOffs(48, 60).addBox(-3.45F, -1.5F, -2.6F, 3.7F, 1.5F, 4.3F)
				.texOffs(66, 60).addBox(-3.55F, -4.95F, -1.8F, 3.9F, 1.1F, 3.6F), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(14, 60).addBox(0.0F, 0.0F, -1.7F, 3.4F, 6.0F, 3.4F), PartPose.offset(0.1F, 12.0F, 0.0F));
		PartDefinition leftCalf = leftLeg.addOrReplaceChild("left_calf", CubeListBuilder.create()
				.texOffs(38, 60).addBox(0.0F, 0.0F, -1.55F, 3.2F, 6.0F, 3.1F), PartPose.offset(0.0F, 6.0F, 0.0F));
		leftCalf.addOrReplaceChild("left_boot", CubeListBuilder.create()
				.texOffs(48, 60).mirror().addBox(-0.25F, -1.5F, -2.6F, 3.7F, 1.5F, 4.3F)
				.texOffs(66, 60).mirror().addBox(-0.35F, -4.95F, -1.8F, 3.9F, 1.1F, 3.6F), PartPose.offset(0.0F, 6.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(CircusFireEaterEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float cowlSway = Mth.sin(ageInTicks * 0.09F) * 0.08F;
		leftCowl.zRot = -0.3F + cowlSway;
		rightCowl.zRot = 0.3F - cowlSway;
		throatFrame.xScale = throatFrame.yScale = throatFrame.zScale = 1.0F;
		if (entity.getActState() == ActState.SETUP || entity.getActState() == ActState.PERFORM) {
			head.xRot -= 0.35F;
			rightArm.xRot = -1.55F;
			leftArm.xRot = -0.8F + Mth.sin(ageInTicks * 0.18F) * 0.08F;
			throatFrame.xScale = throatFrame.zScale = 1.08F;
		}
		if (entity.getActState() == ActState.DOWNED) {
			body.xRot = 1.25F;
			head.xRot = 0.55F;
			rightArm.xRot = leftArm.xRot = -0.8F;
		}
	}
}
