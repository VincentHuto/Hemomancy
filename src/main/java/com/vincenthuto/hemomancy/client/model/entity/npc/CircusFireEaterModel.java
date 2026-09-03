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
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("circus_fire_eater"), "main");
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
				.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
				.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.3F))
				.texOffs(64, 0).addBox(-4.3F, -4.8F, -4.65F, 8.6F, 3.8F, 0.6F), PartPose.ZERO);
		head.addOrReplaceChild("throat_frame", CubeListBuilder.create()
				.texOffs(64, 8).addBox(-3.5F, -0.5F, -3.0F, 7.0F, 1.0F, 6.0F)
				.texOffs(64, 15).addBox(-3.7F, -0.8F, -3.2F, 0.8F, 4.0F, 6.4F)
				.texOffs(80, 15).addBox(2.9F, -0.8F, -3.2F, 0.8F, 4.0F, 6.4F), PartPose.ZERO);
		head.addOrReplaceChild("left_cowl", CubeListBuilder.create()
				.texOffs(36, 74).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 7.0F, new CubeDeformation(-0.15F))
				.texOffs(64, 58).addBox(-1.5F, -1.5F, 5.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.45F)),
				PartPose.offsetAndRotation(2.4F, -7.2F, 0.5F, 0.35F, 0.2F, -0.35F));
		head.addOrReplaceChild("right_cowl", CubeListBuilder.create()
				.texOffs(36, 74).mirror().addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 7.0F, new CubeDeformation(-0.15F))
				.texOffs(64, 58).mirror().addBox(-1.5F, -1.5F, 5.0F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.45F)),
				PartPose.offsetAndRotation(-2.4F, -7.2F, 0.5F, 0.35F, -0.2F, 0.35F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
				.texOffs(16, 52).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F))
				.texOffs(88, 0).addBox(-0.7F, 0.5F, -2.75F, 1.4F, 9.5F, 0.7F)
				.texOffs(92, 0).addBox(-4.6F, 9.5F, -2.6F, 9.2F, 2.0F, 5.2F), PartPose.ZERO);
		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(64, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(72, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(59, 48).addBox(-4.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F),
				PartPose.offset(-5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(40, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(56, 16).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(59, 48).addBox(-2.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
		root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(16, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(0, 42).addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.08F))
				.texOffs(32, 32).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)),
				PartPose.offset(-1.9F, 12.0F, 0.0F));
		root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(16, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F)
				.texOffs(16, 42).mirror().addBox(-2.0F, 6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.08F))
				.texOffs(48, 32).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F)),
				PartPose.offset(1.9F, 12.0F, 0.0F));
		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(CircusFireEaterEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float cowlSway = Mth.sin(ageInTicks * 0.09F) * 0.08F;
		leftCowl.zRot = -0.35F + cowlSway;
		rightCowl.zRot = 0.35F - cowlSway;
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
