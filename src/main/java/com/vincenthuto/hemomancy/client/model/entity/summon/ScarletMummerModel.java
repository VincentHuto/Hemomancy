package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.ScarletMummerEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class ScarletMummerModel extends HumanoidModel<ScarletMummerEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "scarlet_mummer"), "main");

	private final ModelPart leftGorget;
	private final ModelPart rightGorget;

	public ScarletMummerModel(ModelPart root) {
		super(root);
		leftGorget = body.getChild("left_gorget");
		rightGorget = body.getChild("right_gorget");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(20, 0).addBox(-2F, -7F, -1.5F, 4F, 7F, 3F)
				.texOffs(50, 0).addBox(-1.5F, -6.5F, -2F, 3F, 6F, 1F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition hat = part.addOrReplaceChild("hat", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-1.5F, 0F, -1F, 3F, 10F, 2F)
				.texOffs(20, 10).addBox(-2F, 10F, -1.5F, 4F, 2F, 3F)
				.texOffs(34, 0).addBox(-0.5F, 1F, -1.5F, 1F, 8F, 1F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition rightGorget = body.addOrReplaceChild("right_gorget", CubeListBuilder.create()
				.texOffs(42, 8).addBox(-0.5F, -2F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(-1.3F, 0F, 1F, 0F, 0.55F, 0.08F));

		PartDefinition rightUpperFan = rightGorget.addOrReplaceChild("right_upper_fan", CubeListBuilder.create()
				.texOffs(10, 0).addBox(-4F, -6F, 0F, 4F, 10F, 1F)
				.texOffs(34, 0).addBox(-4F, -5F, -0.2F, 1F, 8F, 1F),
				PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, -0.12F));

		PartDefinition rightOuterFan = rightUpperFan.addOrReplaceChild("right_outer_fan", CubeListBuilder.create()
				.texOffs(38, 0).addBox(-3F, -2F, 0F, 3F, 7F, 1F)
				.texOffs(50, 7).addBox(-3F, -1F, -0.2F, 1F, 5F, 1F),
				PartPose.offsetAndRotation(-4F, -2F, 0F, 0F, 0F, 0.18F));

		PartDefinition rightLowerFan = rightGorget.addOrReplaceChild("right_lower_fan", CubeListBuilder.create()
				.texOffs(54, 7).addBox(-3F, 0F, 0F, 3F, 5F, 1F),
				PartPose.offsetAndRotation(0F, 3F, 0F, 0F, 0F, -0.4F));

		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-0.5F, -2F, -0.5F, 1F, 7F, 1F)
				.texOffs(0, 12).addBox(-1F, 4F, -1F, 2F, 1F, 2F),
				PartPose.offsetAndRotation(-2.8F, 2F, 0F, 0F, 0F, 0F));

		PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
				.texOffs(58, 0).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F)
				.texOffs(42, 13).addBox(-1F, 4.5F, -1F, 2F, 1F, 2F)
				.texOffs(46, 8).addBox(-0.5F, 6F, -0.5F, 1F, 3F, 1F),
				PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, 0.1F));

		PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(58, 0).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F)
				.texOffs(0, 12).addBox(-1F, 5F, -1F, 2F, 1F, 2F),
				PartPose.offsetAndRotation(-1.2F, 12F, 0F, 0F, 0F, 0F));

		PartDefinition rightShin = rightLeg.addOrReplaceChild("right_shin", CubeListBuilder.create()
				.texOffs(38, 8).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
				.texOffs(10, 11).addBox(-1F, 5F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));

		PartDefinition leftGorget = body.addOrReplaceChild("left_gorget", CubeListBuilder.create()
				.texOffs(42, 8).addBox(-0.5F, -2F, -0.5F, 1F, 4F, 1F),
				PartPose.offsetAndRotation(1.3F, 0F, 1F, 0F, -0.55F, -0.08F));

		PartDefinition leftUpperFan = leftGorget.addOrReplaceChild("left_upper_fan", CubeListBuilder.create()
				.texOffs(10, 0).addBox(0F, -6F, 0F, 4F, 10F, 1F)
				.texOffs(34, 0).addBox(3F, -5F, -0.2F, 1F, 8F, 1F),
				PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, 0.12F));

		PartDefinition leftOuterFan = leftUpperFan.addOrReplaceChild("left_outer_fan", CubeListBuilder.create()
				.texOffs(38, 0).addBox(0F, -2F, 0F, 3F, 7F, 1F)
				.texOffs(50, 7).addBox(2F, -1F, -0.2F, 1F, 5F, 1F),
				PartPose.offsetAndRotation(4F, -2F, 0F, 0F, 0F, -0.18F));

		PartDefinition leftLowerFan = leftGorget.addOrReplaceChild("left_lower_fan", CubeListBuilder.create()
				.texOffs(54, 7).addBox(0F, 0F, 0F, 3F, 5F, 1F),
				PartPose.offsetAndRotation(0F, 3F, 0F, 0F, 0F, 0.4F));

		PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-0.5F, -2F, -0.5F, 1F, 7F, 1F)
				.texOffs(0, 12).addBox(-1F, 4F, -1F, 2F, 1F, 2F),
				PartPose.offsetAndRotation(2.8F, 2F, 0F, 0F, 0F, 0F));

		PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
				.texOffs(58, 0).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F)
				.texOffs(42, 13).addBox(-1F, 4.5F, -1F, 2F, 1F, 2F)
				.texOffs(46, 8).addBox(-0.5F, 6F, -0.5F, 1F, 3F, 1F),
				PartPose.offsetAndRotation(0F, 5F, 0F, 0F, 0F, -0.1F));

		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(58, 0).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F)
				.texOffs(0, 12).addBox(-1F, 5F, -1F, 2F, 1F, 2F),
				PartPose.offsetAndRotation(1.2F, 12F, 0F, 0F, 0F, 0F));

		PartDefinition leftShin = leftLeg.addOrReplaceChild("left_shin", CubeListBuilder.create()
				.texOffs(38, 8).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
				.texOffs(10, 11).addBox(-1F, 5F, -2F, 2F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));

		return LayerDefinition.create(mesh, 64, 64);
	}

	@Override
	public void setupAnim(ScarletMummerEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		float flare = entity.isPerforming() ? 0.34F + Mth.sin(ageInTicks * 0.3F) * 0.05F : 0.08F;
		leftGorget.zRot = -flare;
		rightGorget.zRot = flare;
		leftGorget.yRot = entity.isPerforming() ? -0.08F : -1.15F;
		rightGorget.yRot = entity.isPerforming() ? 0.08F : 1.15F;
		if (entity.isPerforming()) {
			body.yRot = Mth.sin(ageInTicks * 0.22F) * 0.08F;
			leftArm.zRot -= 0.35F;
			rightArm.zRot += 0.35F;
		}
		// Humanoid attacks assume five-pixel shoulders; keep the authored puppet joints attached.
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
	}
}
