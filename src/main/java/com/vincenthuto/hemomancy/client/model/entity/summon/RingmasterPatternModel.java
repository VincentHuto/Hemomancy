package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.MnemonistPuppetEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

public class RingmasterPatternModel extends HumanoidModel<MnemonistPuppetEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "ringmaster_pattern"), "main");
	private final ModelPart leftCoattail;
	private final ModelPart rightCoattail;

	public RingmasterPatternModel(ModelPart root) {
		super(root);
		leftCoattail = body.getChild("left_coattail");
		rightCoattail = body.getChild("right_coattail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(22, 0).addBox(-2.5F, -7F, -2F, 5F, 7F, 4F)
				.texOffs(48, 8).addBox(-2F, -6.5F, -2.5F, 4F, 6F, 1F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition hat = part.addOrReplaceChild("hat", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition topHat = head.addOrReplaceChild("top_hat", CubeListBuilder.create()
				.texOffs(40, 0).addBox(-4F, -1F, -3.5F, 8F, 1F, 7F)
				.texOffs(70, 0).addBox(-2.5F, -5F, -2F, 5F, 4F, 4F)
				.texOffs(66, 8).addBox(-3F, -2F, -2.5F, 6F, 1F, 5F),
				PartPose.offsetAndRotation(0F, -7F, 0F, 0F, 0F, -0.07F));

		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3F, 0F, -1.5F, 6F, 10F, 3F)
				.texOffs(88, 0).addBox(-2F, 1F, -2F, 4F, 7F, 1F)
				.texOffs(88, 8).addBox(-3F, 9F, -2F, 6F, 2F, 4F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition rightLapel = body.addOrReplaceChild("right_lapel", CubeListBuilder.create()
				.texOffs(98, 0).addBox(-2F, 0F, 0F, 2F, 7F, 1F),
				PartPose.offsetAndRotation(-0.5F, 0F, -2F, 0F, 0F, 0.2F));

		PartDefinition rightCoattail = body.addOrReplaceChild("right_coattail", CubeListBuilder.create()
				.texOffs(104, 0).addBox(-3F, 0F, 0F, 3F, 7F, 1F)
				.texOffs(112, 0).addBox(-3F, 0F, -0.1F, 1F, 7F, 1F),
				PartPose.offsetAndRotation(-0.3F, 10F, 1F, 0.15F, 0F, 0.1F));

		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(116, 0).addBox(-1F, -2F, -1F, 2F, 6F, 2F)
				.texOffs(108, 8).addBox(-1.5F, -2F, -1.5F, 3F, 1F, 3F)
				.texOffs(22, 11).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(-4F, 2F, 0F, 0F, 0F, 0F));

		PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
				.texOffs(58, 8).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(108, 8).addBox(-1.5F, 3.5F, -1.5F, 3F, 1F, 3F)
				.texOffs(120, 8).addBox(-1F, 5F, -1F, 2F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));

		PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(40, 8).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
				.texOffs(22, 11).addBox(-1.5F, 5F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(-1.5F, 12F, 0F, 0F, 0F, 0F));

		PartDefinition rightShin = rightLeg.addOrReplaceChild("right_shin", CubeListBuilder.create()
				.texOffs(58, 8).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(22, 11).addBox(-1.5F, 5F, -2F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));

		PartDefinition rightCommandThread = body.addOrReplaceChild("right_command_thread", CubeListBuilder.create()
				.texOffs(18, 0).addBox(-0.5F, 0F, 0F, 1F, 12F, 1F),
				PartPose.offsetAndRotation(-3F, -1F, 2F, 0F, 0F, 0F));

		PartDefinition leftLapel = body.addOrReplaceChild("left_lapel", CubeListBuilder.create()
				.texOffs(98, 0).addBox(0F, 0F, 0F, 2F, 7F, 1F),
				PartPose.offsetAndRotation(0.5F, 0F, -2F, 0F, 0F, -0.2F));

		PartDefinition leftCoattail = body.addOrReplaceChild("left_coattail", CubeListBuilder.create()
				.texOffs(104, 0).addBox(0F, 0F, 0F, 3F, 7F, 1F)
				.texOffs(112, 0).addBox(2F, 0F, -0.1F, 1F, 7F, 1F),
				PartPose.offsetAndRotation(0.3F, 10F, 1F, 0.15F, 0F, -0.1F));

		PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(116, 0).addBox(-1F, -2F, -1F, 2F, 6F, 2F)
				.texOffs(108, 8).addBox(-1.5F, -2F, -1.5F, 3F, 1F, 3F)
				.texOffs(22, 11).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(4F, 2F, 0F, 0F, 0F, 0F));

		PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
				.texOffs(58, 8).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(108, 8).addBox(-1.5F, 3.5F, -1.5F, 3F, 1F, 3F)
				.texOffs(120, 8).addBox(-1F, 5F, -1F, 2F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0F));

		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(40, 8).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
				.texOffs(22, 11).addBox(-1.5F, 5F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(1.5F, 12F, 0F, 0F, 0F, 0F));

		PartDefinition leftShin = leftLeg.addOrReplaceChild("left_shin", CubeListBuilder.create()
				.texOffs(58, 8).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(22, 11).addBox(-1.5F, 5F, -2F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));

		PartDefinition leftCommandThread = body.addOrReplaceChild("left_command_thread", CubeListBuilder.create()
				.texOffs(18, 0).addBox(-0.5F, 0F, 0F, 1F, 12F, 1F),
				PartPose.offsetAndRotation(3F, -1F, 2F, 0F, 0F, 0F));

		return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(MnemonistPuppetEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		// Humanoid attacks assume five-pixel shoulders; keep the authored puppet joints attached.
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
		float step = Mth.cos(limbSwing * 0.6662F) * limbSwingAmount * 0.18F;
		leftCoattail.xRot = 0.15F + step;
		rightCoattail.xRot = 0.15F - step;
		leftArm.zRot = -0.24F;
		rightArm.zRot = 0.24F;
		leftArm.getChild("left_forearm").xRot = -0.45F;
		rightArm.getChild("right_forearm").xRot = -0.45F;
	}
}
