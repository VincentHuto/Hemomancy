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

// Authored by tools/circus/build_mnemonist_puppet.py; regenerate the model, atlas and BBModel together.
public class MnemonistPuppetModel extends HumanoidModel<MnemonistPuppetEntity> {
	public static final ModelLayerLocation LAYER_LOCATION =
			new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Hemomancy.MOD_ID, "mnemonist_puppet"), "main");

	private final ModelPart memorySpool;

	public MnemonistPuppetModel(ModelPart root) {
		super(root);
		this.memorySpool = this.body.getChild("memory_spool");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition skull = head.addOrReplaceChild("skull", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -6.0F, -2.0F, 5.0F, 5.0F, 4.0F).texOffs(19, 0).addBox(-1.5F, -7.0F, -1.5F, 3.0F, 1.0F, 3.0F).texOffs(32, 0).addBox(-1.5F, -1.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.10471975511965978F, 0.0F, 0.20943951023931956F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(45, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(50, 0).addBox(-2.0F, 0.0F, -1.5F, 4.0F, 4.0F, 3.0F).texOffs(65, 0).addBox(-1.5F, 4.0F, -1.0F, 3.0F, 2.0F, 2.0F).texOffs(76, 0).addBox(-1.5F, 6.0F, -1.0F, 3.0F, 2.0F, 2.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition binding_0 = body.addOrReplaceChild("binding_0", CubeListBuilder.create().texOffs(87, 0).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 1.0F, 0.0F, 0.0F, 0.0F, 0.08726646259971647F));
        PartDefinition binding_1 = body.addOrReplaceChild("binding_1", CubeListBuilder.create().texOffs(106, 0).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 2.5F, 0.0F, 0.0F, 0.0F, -0.06981317007977318F));
        PartDefinition binding_2 = body.addOrReplaceChild("binding_2", CubeListBuilder.create().texOffs(0, 10).addBox(-2.5F, 0.0F, -2.0F, 5.0F, 0.5F, 4.0F), PartPose.offsetAndRotation(0.0F, 5.0F, 0.0F, 0.0F, 0.0F, 0.10471975511965978F));
        PartDefinition memory_spool = body.addOrReplaceChild("memory_spool", CubeListBuilder.create().texOffs(19, 10).addBox(-2.5F, -1.5F, 0.0F, 5.0F, 3.0F, 1.0F).texOffs(32, 10).addBox(-1.5F, -2.5F, 0.0F, 3.0F, 5.0F, 1.0F).texOffs(41, 10).addBox(-1.5F, -1.5F, 1.0F, 3.0F, 3.0F, 2.0F).texOffs(52, 10).addBox(-2.5F, -1.5F, 3.0F, 5.0F, 3.0F, 1.0F).texOffs(65, 10).addBox(-1.5F, -2.5F, 3.0F, 3.0F, 5.0F, 1.0F).texOffs(74, 10).addBox(-0.5F, -0.5F, 4.0F, 1.0F, 1.0F, 1.0F), PartPose.offset(0.0F, 3.5F, 1.5F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(79, 10).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(88, 10).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offset(3.0F, 1.0F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(93, 10).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(98, 10).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(103, 10).addBox(-1.0F, 6.5F, -0.5F, 2.0F, 2.0F, 1.0F).texOffs(110, 10).addBox(-1.0F, 8.5F, -0.5F, 0.5F, 3.0F, 0.5F).texOffs(115, 10).addBox(-0.25F, 8.5F, -0.5F, 0.5F, 4.0F, 0.5F).texOffs(120, 10).addBox(0.5F, 8.5F, -0.5F, 0.5F, 3.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.0F, 0.0F, -0.05235987755982989F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(5, 18).addBox(-0.5F, 7.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(1.0F, 8.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(10, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(15, 18).addBox(-0.5F, 7.0F, -2.0F, 1.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 18).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F).texOffs(33, 18).addBox(-0.5F, 1.0F, -0.5F, 1.0F, 5.0F, 1.0F), PartPose.offset(-3.0F, 1.0F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(38, 18).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F).texOffs(43, 18).addBox(-0.5F, 0.5F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(48, 18).addBox(-1.0F, 6.5F, -0.5F, 2.0F, 2.0F, 1.0F).texOffs(55, 18).addBox(-1.0F, 8.5F, -0.5F, 0.5F, 3.0F, 0.5F).texOffs(60, 18).addBox(-0.25F, 8.5F, -0.5F, 0.5F, 4.0F, 0.5F).texOffs(65, 18).addBox(0.5F, 8.5F, -0.5F, 0.5F, 3.0F, 0.5F), PartPose.offsetAndRotation(0.0F, 6.5F, 0.0F, 0.0F, 0.0F, 0.05235987755982989F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(70, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(75, 18).addBox(-0.5F, 7.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offset(-1.0F, 8.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(80, 18).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 7.0F, 1.0F).texOffs(85, 18).addBox(-0.5F, 7.0F, -2.0F, 1.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 8.0F, 0.0F));
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
		// HumanoidModel resets limb heights to vanilla proportions; restore the short torso and long legs.
		float crouch = crouching ? 3.2F : 0;
		leftArm.y = leftArm.getInitialPose().y + crouch;
		rightArm.y = rightArm.getInitialPose().y + crouch;
		leftLeg.y = leftLeg.getInitialPose().y + (crouching ? .2F : 0);
		rightLeg.y = rightLeg.getInitialPose().y + (crouching ? .2F : 0);
		// The spindle turns slowly as the live threads pay out of its core.
		float pulse = Mth.sin(ageInTicks * 0.16F) * 0.04F;
		this.memorySpool.zRot = ageInTicks * 0.03F + pulse;
	}
}
