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
				.texOffs(0, 0).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 8.0F, 7.0F), PartPose.ZERO);
		head.addOrReplaceChild("side_cowl", CubeListBuilder.create()
				.texOffs(96, 0).addBox(-2.5F, -1.0F, -2.0F, 5.0F, 2.0F, 4.0F)
				.texOffs(96, 8).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 1.0F, 3.0F),
				PartPose.offsetAndRotation(-2.0F, -7.4F, 0.25F, 0.12F, -0.08F, 0.32F));
		root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 20).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F)
				.texOffs(26, 20).addBox(-3.0F, 2.0F, -2.25F, 6.0F, 8.0F, 1.0F)
				.texOffs(46, 20).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 1.0F, 5.0F)
				.texOffs(70, 72).addBox(-4.0F, 9.0F, -2.5F, 8.0F, 1.0F, 5.0F), PartPose.ZERO);
		body.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(46, 28)
				.addBox(-1.0F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(-1.75F, 1.0F, -2.3F, 0.0F, 0.0F, -0.17F));
		body.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(52, 28).mirror()
				.addBox(0.0F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(1.75F, 1.0F, -2.3F, 0.0F, 0.0F, 0.17F));
		body.addOrReplaceChild("gold_clasps", CubeListBuilder.create()
				.texOffs(60, 28).addBox(-0.5F, 0.0F, -0.3F, 1.0F, 1.0F, 1.0F)
				.texOffs(60, 30).addBox(-0.5F, 2.15F, -0.3F, 1.0F, 1.0F, 1.0F)
				.texOffs(60, 32).addBox(-0.5F, 4.3F, -0.3F, 1.0F, 1.0F, 1.0F),
				PartPose.offset(0.0F, 3.0F, -2.55F));
		body.addOrReplaceChild("right_coat_tail", CubeListBuilder.create()
				.texOffs(66, 28).addBox(-3.0F, 0.0F, -0.5F, 3.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(-0.15F, 10.3F, 1.65F, 0.08F, 0.0F, 0.06F));
		body.addOrReplaceChild("left_coat_tail", CubeListBuilder.create()
				.texOffs(66, 28).mirror().addBox(0.0F, 0.0F, -0.5F, 3.0F, 7.0F, 1.0F),
				PartPose.offsetAndRotation(0.15F, 10.3F, 1.65F, 0.08F, 0.0F, -0.06F));
		body.addOrReplaceChild("knife_fan", CubeListBuilder.create()
				.texOffs(88, 28).addBox(-0.45F, -5.5F, -0.25F, 1.0F, 4.0F, 1.0F)
				.texOffs(92, 28).addBox(-0.95F, -1.6F, -0.45F, 2.0F, 1.0F, 1.0F)
				.texOffs(98, 28).addBox(-0.4F, -1.0F, -0.3F, 1.0F, 2.0F, 1.0F)
				.texOffs(88, 28).addBox(1.15F, -5.35F, -0.25F, 1.0F, 4.0F, 1.0F)
				.texOffs(92, 28).addBox(0.65F, -1.45F, -0.45F, 2.0F, 1.0F, 1.0F)
				.texOffs(98, 28).addBox(1.2F, -0.85F, -0.3F, 1.0F, 2.0F, 1.0F)
				.texOffs(88, 28).addBox(2.75F, -4.95F, -0.25F, 1.0F, 4.0F, 1.0F)
				.texOffs(92, 28).addBox(2.25F, -1.05F, -0.45F, 2.0F, 1.0F, 1.0F)
				.texOffs(98, 28).addBox(2.8F, -0.45F, -0.3F, 1.0F, 2.0F, 1.0F),
				PartPose.offsetAndRotation(-3.7F, 7.2F, 2.35F, 0.12F, 0.0F, -0.26F));

		PartDefinition rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(0, 42).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(-4.8F, 2.0F, 0.0F));
		rightArm.addOrReplaceChild("right_shoulder", CubeListBuilder.create()
				.texOffs(34, 42).addBox(-3.5F, -2.25F, -2.5F, 5.0F, 2.0F, 5.0F), PartPose.ZERO);
		rightArm.addOrReplaceChild("right_glove", CubeListBuilder.create()
				.texOffs(70, 42).addBox(-3.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F)
				.texOffs(88, 42).addBox(-3.5F, -0.5F, -2.5F, 5.0F, 1.0F, 5.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition leftArm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(16, 42).mirror().addBox(-1.0F, -2.0F, -2.0F, 4.0F, 10.0F, 4.0F), PartPose.offset(4.8F, 2.0F, 0.0F));
		leftArm.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
				.texOffs(34, 42).mirror().addBox(-1.5F, -2.25F, -2.5F, 5.0F, 2.0F, 5.0F), PartPose.ZERO);
		leftArm.addOrReplaceChild("left_glove", CubeListBuilder.create()
				.texOffs(70, 42).mirror().addBox(-1.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F)
				.texOffs(88, 42).mirror().addBox(-1.5F, -0.5F, -2.5F, 5.0F, 1.0F, 5.0F), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition rightLeg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(0, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F));
		PartDefinition rightCalf = rightLeg.addOrReplaceChild("right_calf", CubeListBuilder.create()
				.texOffs(32, 60).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
		rightCalf.addOrReplaceChild("right_boot", CubeListBuilder.create()
				.texOffs(52, 60).addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 5.0F)
				.texOffs(72, 60).addBox(-2.0F, -1.0F, -2.25F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 4.0F, 0.0F));

		PartDefinition leftLeg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(16, 60).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(1.9F, 12.0F, 0.0F));
		PartDefinition leftCalf = leftLeg.addOrReplaceChild("left_calf", CubeListBuilder.create()
				.texOffs(32, 60).mirror().addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
		leftCalf.addOrReplaceChild("left_boot", CubeListBuilder.create()
				.texOffs(52, 60).mirror().addBox(-2.0F, 0.0F, -3.0F, 4.0F, 2.0F, 5.0F)
				.texOffs(72, 60).mirror().addBox(-2.0F, -1.0F, -2.25F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 4.0F, 0.0F));

		root.addOrReplaceChild("juggle_knife_0", jugglingKnife(), PartPose.ZERO);
		root.addOrReplaceChild("juggle_knife_1", jugglingKnife(), PartPose.ZERO);
		root.addOrReplaceChild("juggle_knife_2", jugglingKnife(), PartPose.ZERO);
		return LayerDefinition.create(mesh, 128, 128);
	}

	private static CubeListBuilder jugglingKnife() {
		return CubeListBuilder.create()
				.texOffs(88, 28).addBox(-0.5F, -3.5F, -0.5F, 1.0F, 4.0F, 1.0F)
				.texOffs(92, 28).addBox(-1.0F, 0.5F, -0.5F, 2.0F, 1.0F, 1.0F)
				.texOffs(98, 28).addBox(-0.5F, 1.5F, -0.5F, 1.0F, 2.0F, 1.0F);
	}

	@Override
	public void setupAnim(CircusKnifeThrowerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		knifeFan.yRot = Mth.sin(ageInTicks * 0.07F) * 0.08F;
		sideCowl.zRot = 0.32F + Mth.sin(ageInTicks * 0.1F) * 0.07F;
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
