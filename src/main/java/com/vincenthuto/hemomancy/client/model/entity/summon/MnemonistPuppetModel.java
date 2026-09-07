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

public class MnemonistPuppetModel extends HumanoidModel<MnemonistPuppetEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "mnemonist_puppet"), "main");

	private final ModelPart memorySpool;
	private final ModelPart leftThread;
	private final ModelPart rightThread;

	public MnemonistPuppetModel(ModelPart root) {
		super(root);
		this.memorySpool = this.body.getChild("memory_spool");
		this.leftThread = this.body.getChild("left_thread");
		this.rightThread = this.body.getChild("right_thread");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
		PartDefinition part = mesh.getRoot();

		PartDefinition head = part.addOrReplaceChild("head", CubeListBuilder.create()
				.texOffs(18, 0).addBox(-2.5F, -7F, -2F, 5F, 7F, 4F)
				.texOffs(54, 0).addBox(-2F, -6.5F, -2.5F, 4F, 6F, 1F)
				.texOffs(46, 8).addBox(-2.5F, -7F, -2.5F, 5F, 1F, 1F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition hat = part.addOrReplaceChild("hat", CubeListBuilder.create(),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition body = part.addOrReplaceChild("body", CubeListBuilder.create()
				.texOffs(4, 0).addBox(-2F, 0F, -1.5F, 4F, 10F, 3F)
				.texOffs(0, 15).addBox(-2.5F, 10F, -1.5F, 5F, 2F, 3F)
				.texOffs(26, 17).addBox(-3F, 0F, -1.5F, 6F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0F));

		PartDefinition binding_0 = body.addOrReplaceChild("binding_0", CubeListBuilder.create()
				.texOffs(4, 13).addBox(-2.5F, 0F, -0.4F, 5F, 1F, 1F),
				PartPose.offsetAndRotation(0F, 2F, -1.6F, 0F, 0F, 0F));

		PartDefinition binding_1 = body.addOrReplaceChild("binding_1", CubeListBuilder.create()
				.texOffs(4, 13).addBox(-2.5F, 0F, -0.4F, 5F, 1F, 1F),
				PartPose.offsetAndRotation(0F, 5F, -1.6F, 0F, 0F, 0F));

		PartDefinition binding_2 = body.addOrReplaceChild("binding_2", CubeListBuilder.create()
				.texOffs(4, 13).addBox(-2.5F, 0F, -0.4F, 5F, 1F, 1F),
				PartPose.offsetAndRotation(0F, 8F, -1.6F, 0F, 0F, 0F));

		PartDefinition memorySpool = body.addOrReplaceChild("memory_spool", CubeListBuilder.create()
				.texOffs(36, 10).addBox(-4F, -2F, -1.5F, 8F, 4F, 3F)
				.texOffs(36, 0).addBox(-5F, -3F, -2F, 1F, 6F, 4F)
				.texOffs(36, 0).addBox(4F, -3F, -2F, 1F, 6F, 4F)
				.texOffs(12, 21).addBox(-6F, -0.5F, -0.5F, 12F, 1F, 1F),
				PartPose.offsetAndRotation(0F, 4F, 3F, 0F, 0F, 0F));

		PartDefinition rightThread = body.addOrReplaceChild("right_thread", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-0.5F, 0F, 0F, 1F, 14F, 1F),
				PartPose.offsetAndRotation(-3F, -3F, -2F, 0F, 0F, 0F));

		PartDefinition rightArm = part.addOrReplaceChild("right_arm", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-1F, -2F, -1F, 2F, 6F, 2F)
				.texOffs(44, 17).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(-4F, 2F, 0F, 0F, 0F, 0F));

		PartDefinition rightForearm = rightArm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
				.texOffs(18, 11).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(0, 20).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F)
				.texOffs(26, 11).addBox(-1F, 5F, -1F, 2F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, 0.06F));

		PartDefinition rightLeg = part.addOrReplaceChild("right_leg", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
				.texOffs(44, 17).addBox(-1.5F, 5F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(-1.5F, 12F, 0F, 0F, 0F, 0F));

		PartDefinition rightShin = rightLeg.addOrReplaceChild("right_shin", CubeListBuilder.create()
				.texOffs(18, 11).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(44, 17).addBox(-1.5F, 5F, -2F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));

		PartDefinition leftThread = body.addOrReplaceChild("left_thread", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-0.5F, 0F, 0F, 1F, 14F, 1F),
				PartPose.offsetAndRotation(3F, -3F, -2F, 0F, 0F, 0F));

		PartDefinition leftArm = part.addOrReplaceChild("left_arm", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-1F, -2F, -1F, 2F, 6F, 2F)
				.texOffs(44, 17).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(4F, 2F, 0F, 0F, 0F, 0F));

		PartDefinition leftForearm = leftArm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
				.texOffs(18, 11).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(0, 20).addBox(-1.5F, 3F, -1.5F, 3F, 1F, 3F)
				.texOffs(26, 11).addBox(-1F, 5F, -1F, 2F, 2F, 2F),
				PartPose.offsetAndRotation(0F, 4F, 0F, 0F, 0F, -0.06F));

		PartDefinition leftLeg = part.addOrReplaceChild("left_leg", CubeListBuilder.create()
				.texOffs(46, 0).addBox(-1F, 0F, -1F, 2F, 6F, 2F)
				.texOffs(44, 17).addBox(-1.5F, 5F, -1.5F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(1.5F, 12F, 0F, 0F, 0F, 0F));

		PartDefinition leftShin = leftLeg.addOrReplaceChild("left_shin", CubeListBuilder.create()
				.texOffs(18, 11).addBox(-1F, 0F, -1F, 2F, 5F, 2F)
				.texOffs(44, 17).addBox(-1.5F, 5F, -2F, 3F, 1F, 3F),
				PartPose.offsetAndRotation(0F, 6F, 0F, 0F, 0F, 0F));

		return LayerDefinition.create(mesh, 64, 64);
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
		float pulse = Mth.sin(ageInTicks * 0.16F) * 0.04F;
		this.memorySpool.zRot = pulse;
		this.leftThread.xRot = pulse * 0.5F;
		this.rightThread.xRot = -pulse * 0.5F;
	}
}
