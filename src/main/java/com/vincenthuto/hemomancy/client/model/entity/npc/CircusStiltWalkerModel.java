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
				.texOffs(0, 0).addBox(-3.7F, -8.0F, -3.7F, 7.4F, 8.0F, 7.4F)
				.texOffs(32, 0).addBox(-3.7F, -8.0F, -3.7F, 7.4F, 8.0F, 7.4F, new CubeDeformation(0.3F))
				.texOffs(64, 0).addBox(-3.9F, -5.0F, -4.3F, 7.8F, 2.6F, 0.6F),
				PartPose.offset(0.0F, -24.0F, 0.0F));
		head.addOrReplaceChild("crown", CubeListBuilder.create()
				.texOffs(36, 74).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 9.0F, new CubeDeformation(-0.2F))
				.texOffs(64, 58).addBox(-1.5F, -1.5F, 7.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.45F)),
				PartPose.offsetAndRotation(0.0F, -7.4F, 0.0F, 0.45F, 0.0F, 0.0F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 16).addBox(-3.6F, 0.0F, -2.0F, 7.2F, 12.0F, 4.0F)
				.texOffs(16, 52).addBox(-3.6F, 0.0F, -2.0F, 7.2F, 12.0F, 4.0F, new CubeDeformation(0.15F))
				.texOffs(88, 0).addBox(-4.5F, 0.0F, -2.6F, 9.0F, 2.0F, 5.2F),
				PartPose.offset(0.0F, -24.0F, 0.0F));
		body.addOrReplaceChild("stilt_brace", CubeListBuilder.create()
				.texOffs(88, 8).addBox(-4.8F, -0.5F, -0.7F, 9.6F, 1.0F, 1.4F)
				.texOffs(88, 12).addBox(-0.7F, -0.5F, -2.8F, 1.4F, 1.0F, 5.6F), PartPose.offset(0.0F, 10.5F, 0.0F));
		body.addOrReplaceChild("left_cord", CubeListBuilder.create().texOffs(112, 0)
				.addBox(-0.4F, 0.0F, -0.3F, 0.8F, 14.0F, 0.6F), PartPose.offset(3.0F, 2.0F, 2.3F));
		body.addOrReplaceChild("right_cord", CubeListBuilder.create().texOffs(112, 0).mirror()
				.addBox(-0.4F, 0.0F, -0.3F, 0.8F, 14.0F, 0.6F), PartPose.offset(-3.0F, 2.0F, 2.3F));
		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(64, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(72, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(59, 48).addBox(-4.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F),
				PartPose.offset(-4.6F, -22.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(56, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(59, 48).addBox(-2.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F),
				PartPose.offset(4.6F, -22.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(0, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.08F))
				.texOffs(32, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(96, 24).addBox(-0.8F, 10.0F, -0.8F, 1.6F, 25.0F, 1.6F)
				.texOffs(104, 24).addBox(-2.4F, 33.0F, -4.8F, 4.8F, 2.0F, 6.4F),
				PartPose.offset(-1.9F, -12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(16, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(16, 42).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.08F))
				.texOffs(48, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(96, 24).mirror().addBox(-0.8F, 10.0F, -0.8F, 1.6F, 25.0F, 1.6F)
				.texOffs(104, 24).mirror().addBox(-2.4F, 33.0F, -4.8F, 4.8F, 2.0F, 6.4F),
				PartPose.offset(1.9F, -12.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(CircusStiltWalkerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float sway = Mth.sin(ageInTicks * 0.08F) * 0.08F;
		body.zRot = sway;
		leftCord.zRot = sway * 0.7F;
		rightCord.zRot = -sway * 0.7F;
		crown.zRot = sway * 1.4F;
		if (entity.getActState() == ActState.PERFORM) {
			leftArm.zRot = -0.7F;
			rightArm.zRot = 0.7F;
		}
		if (entity.getActState() == ActState.ALERT) leftArm.xRot = rightArm.xRot = -0.55F;
		if (entity.getActState() == ActState.DOWNED) {
			body.xRot = 1.1F;
			rightLeg.xRot = leftLeg.xRot = -1.1F;
		}
	}
}
