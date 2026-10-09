package com.vincenthuto.hemomancy.client.model.entity.summon;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.summon.RingmasterPatternEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.resources.ResourceLocation;

// Authored by tools/circus/build_ringmaster_pattern.py; regenerate the model, atlas and BBModel together.
public class RingmasterPatternModel extends HumanoidModel<RingmasterPatternEntity> {
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
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5F, -5.0F, -2.0F, 5.0F, 5.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition top_hat = head.addOrReplaceChild("top_hat", CubeListBuilder.create().texOffs(19, 0).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 1.0F, 8.0F).texOffs(52, 0).addBox(-3.0F, -9.0F, -3.0F, 6.0F, 8.0F, 6.0F).texOffs(77, 0).addBox(-3.5F, -3.0F, -3.5F, 7.0F, 2.0F, 7.0F).texOffs(106, 0).addBox(-1.0F, -3.0F, -4.5F, 2.0F, 2.0F, 1.0F), PartPose.offsetAndRotation(0.0F, -5.0F, 0.0F, 0.0F, 0.0F, -0.06981317007977318F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 15).addBox(-3.0F, 0.0F, -2.0F, 6.0F, 9.0F, 4.0F).texOffs(21, 15).addBox(-2.0F, 1.0F, -2.5F, 4.0F, 7.0F, 1.0F).texOffs(32, 15).addBox(-3.5F, 9.0F, -2.0F, 7.0F, 3.0F, 4.0F).texOffs(55, 15).addBox(-3.0F, -2.0F, -2.5F, 6.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_lapel = body.addOrReplaceChild("left_lapel", CubeListBuilder.create().texOffs(78, 15).addBox(0.0F, 0.0F, -0.5F, 2.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(1.0F, 0.0F, -2.5F, 0.0F, 0.0F, -0.19198621771937624F));
        PartDefinition left_coattail = body.addOrReplaceChild("left_coattail", CubeListBuilder.create().texOffs(85, 15).addBox(0.0F, 0.0F, 0.0F, 3.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(0.5F, 10.0F, 1.0F, 0.15707963267948966F, 0.0F, -0.10471975511965978F));
        PartDefinition left_command_thread = body.addOrReplaceChild("left_command_thread", CubeListBuilder.create().texOffs(94, 15).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 12.0F, 1.0F), PartPose.offset(3.5F, -1.0F, 2.5F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(99, 15).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(108, 15).addBox(-2.0F, -3.0F, -1.5F, 4.0F, 1.0F, 3.0F).texOffs(0, 29).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 1.0F, 3.0F).texOffs(15, 29).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(4.0F, 2.0F, 0.0F));
        PartDefinition left_forearm = left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create().texOffs(28, 29).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F).texOffs(37, 29).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 1.0F, 3.0F).texOffs(50, 29).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.4537856055185257F, 0.0F, 0.0F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(59, 29).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(1.5F, 12.0F, 0.0F));
        PartDefinition left_shin = left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create().texOffs(68, 29).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(77, 29).addBox(-1.0F, 5.0F, -2.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition right_lapel = body.addOrReplaceChild("right_lapel", CubeListBuilder.create().texOffs(88, 29).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 7.0F, 1.0F), PartPose.offsetAndRotation(-1.0F, 0.0F, -2.5F, 0.0F, 0.0F, 0.19198621771937624F));
        PartDefinition right_coattail = body.addOrReplaceChild("right_coattail", CubeListBuilder.create().texOffs(95, 29).addBox(-3.0F, 0.0F, 0.0F, 3.0F, 10.0F, 1.0F), PartPose.offsetAndRotation(-0.5F, 10.0F, 1.0F, 0.15707963267948966F, 0.0F, 0.10471975511965978F));
        PartDefinition right_command_thread = body.addOrReplaceChild("right_command_thread", CubeListBuilder.create().texOffs(104, 29).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 12.0F, 1.0F), PartPose.offset(-3.5F, -1.0F, 2.5F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(109, 29).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 6.0F, 2.0F).texOffs(0, 43).addBox(-2.0F, -3.0F, -1.5F, 4.0F, 1.0F, 3.0F).texOffs(15, 43).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 1.0F, 3.0F).texOffs(30, 43).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 1.0F, 3.0F), PartPose.offset(-4.0F, 2.0F, 0.0F));
        PartDefinition right_forearm = right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create().texOffs(43, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F).texOffs(52, 43).addBox(-1.5F, 3.0F, -1.5F, 3.0F, 1.0F, 3.0F).texOffs(65, 43).addBox(-1.0F, 4.0F, -1.0F, 2.0F, 3.0F, 2.0F), PartPose.offsetAndRotation(0.0F, 4.0F, 0.0F, -0.4537856055185257F, 0.0F, 0.0F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(74, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 6.0F, 2.0F), PartPose.offset(-1.5F, 12.0F, 0.0F));
        PartDefinition right_shin = right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create().texOffs(83, 43).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 5.0F, 2.0F).texOffs(92, 43).addBox(-1.0F, 5.0F, -2.0F, 2.0F, 1.0F, 3.0F), PartPose.offset(0.0F, 6.0F, 0.0F));
        PartDefinition baton = right_forearm.addOrReplaceChild("baton", CubeListBuilder.create().texOffs(103, 43).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 6.0F, 1.0F).texOffs(108, 43).addBox(-0.5F, 6.0F, -0.5F, 1.0F, 1.0F, 1.0F), PartPose.offsetAndRotation(0.0F, 6.0F, -0.5F, -2.0943951023931953F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	public void setupAnim(RingmasterPatternEntity entity, float limbSwing, float limbSwingAmount,
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
