package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusKnifeThrowerEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public final class CircusKnifeThrowerModel extends HumanoidModel<CircusKnifeThrowerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
			Hemomancy.rloc("circus_knife_thrower"), "main");
	private final ModelPart knifeFan;
	private final ModelPart sideCowl;
	private final ModelPart[] jugglingKnives;

	public CircusKnifeThrowerModel(ModelPart root) {
		super(root);
		knifeFan = body.getChild("knife_fan");
		sideCowl = head.getChild("side_cowl");
		jugglingKnives = new ModelPart[] { root.getChild("juggle_knife_0"),
				root.getChild("juggle_knife_1"), root.getChild("juggle_knife_2") };
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition root = mesh.getRoot();
		PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F)
				.texOffs(32, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.3F))
				.texOffs(64, 0).addBox(-4.3F, -6.8F, -4.65F, 8.6F, 4.8F, 0.6F)
				.texOffs(84, 0).addBox(0.4F, -5.5F, -4.95F, 2.8F, 0.6F, 0.4F), PartPose.ZERO);
		head.addOrReplaceChild("side_cowl", CubeListBuilder.create()
				.texOffs(36, 74).addBox(-1.5F, -1.5F, -1.0F, 3.0F, 3.0F, 8.0F, new CubeDeformation(-0.15F))
				.texOffs(64, 58).addBox(-1.5F, -1.5F, 6.2F, 3.0F, 3.0F, 3.0F, new CubeDeformation(-0.45F)),
				PartPose.offsetAndRotation(-2.6F, -7.2F, 0.2F, 0.35F, -0.15F, 0.48F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
				.texOffs(16, 52).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.15F))
				.texOffs(88, 0).addBox(-4.7F, 9.2F, -2.7F, 9.4F, 2.3F, 5.4F)
				.texOffs(88, 8).addBox(-0.7F, -0.5F, -2.7F, 1.4F, 13.0F, 0.6F),
				PartPose.ZERO);
		body.addOrReplaceChild("knife_fan", CubeListBuilder.create()
				.texOffs(96, 24).addBox(-0.4F, -5.5F, -0.3F, 0.8F, 6.0F, 0.6F)
				.texOffs(100, 24).addBox(1.2F, -5.2F, -0.3F, 0.8F, 6.0F, 0.6F)
				.texOffs(104, 24).addBox(2.8F, -4.5F, -0.3F, 0.8F, 6.0F, 0.6F)
				.texOffs(108, 24).addBox(4.3F, -3.5F, -0.3F, 0.8F, 6.0F, 0.6F),
				PartPose.offsetAndRotation(-4.0F, 7.0F, 2.5F, 0.1F, 0.0F, -0.28F));
		root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(64, 32).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F)
				.texOffs(72, 16).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.3F))
				.texOffs(59, 48).addBox(-4.0F, 4.4F, -3.0F, 6.0F, 0.0F, 6.0F)
				.texOffs(96, 40).addBox(-3.4F, 4.8F, -2.4F, 4.8F, 4.5F, 4.8F, new CubeDeformation(0.08F)),
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
		root.addOrReplaceChild("juggle_knife_0", jugglingKnife(), PartPose.ZERO);
		root.addOrReplaceChild("juggle_knife_1", jugglingKnife(), PartPose.ZERO);
		root.addOrReplaceChild("juggle_knife_2", jugglingKnife(), PartPose.ZERO);
		return LayerDefinition.create(mesh, 128, 128);
	}

	private static CubeListBuilder jugglingKnife() {
		return CubeListBuilder.create().texOffs(96, 24)
				.addBox(-0.4F, -3.0F, -0.3F, 0.8F, 6.0F, 0.6F);
	}

	@Override
	public void setupAnim(CircusKnifeThrowerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		knifeFan.yRot = Mth.sin(ageInTicks * 0.07F) * 0.08F;
		sideCowl.zRot = 0.48F + Mth.sin(ageInTicks * 0.1F) * 0.07F;
		for (int i = 0; i < jugglingKnives.length; i++) {
			ModelPart knife = jugglingKnives[i];
			knife.visible = entity.isJuggling();
			if (!knife.visible) continue;
			float cycle = Mth.frac(ageInTicks * 0.08F + i / 3.0F);
			knife.x = Mth.lerp(cycle, -6.0F, 6.0F);
			knife.y = -1.0F - 36.0F * cycle * (1.0F - cycle);
			knife.z = -4.5F;
			knife.zRot = cycle * Mth.TWO_PI;
		}
		if (entity.getActState() == ActState.PERFORM || entity.getActState() == ActState.ALERT) {
			rightArm.xRot = -1.55F + Mth.sin(ageInTicks * 0.32F) * 0.35F;
			leftArm.xRot = -0.45F;
		}
		if (entity.getActState() == ActState.DOWNED) {
			body.xRot = 1.3F;
			rightArm.xRot = leftArm.xRot = -0.7F;
		}
	}
}
