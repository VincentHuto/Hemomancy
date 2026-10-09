package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusUnderstudyModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_understudy"), "main");
	private final ModelPart halfMask;
	private final ModelPart rightShawlTail;
	private final ModelPart leftShawlTail;

	public CircusUnderstudyModel(ModelPart root) {
		super(root);
		halfMask = head.getChild("half_mask");
		rightShawlTail = body.getChild("right_shawl_tail");
		leftShawlTail = body.getChild("left_shawl_tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition half_mask = head.addOrReplaceChild("half_mask", CubeListBuilder.create().texOffs(33, 0).addBox(-3.5F, -2.0F, -1.0F, 7.0F, 3.0F, 1.0F), PartPose.offset(0.0F, -4.5F, -4.0F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(50, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition shawl = body.addOrReplaceChild("shawl", CubeListBuilder.create().texOffs(75, 0).addBox(-4.5F, -0.5F, -2.5F, 9.0F, 3.0F, 5.0F).texOffs(104, 0).addBox(-4.0F, 2.5F, 2.0F, 8.0F, 4.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_shawl_tail = body.addOrReplaceChild("right_shawl_tail", CubeListBuilder.create().texOffs(0, 17).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 7.0F, 0.5F), PartPose.offsetAndRotation(-2.5F, 2.5F, -2.5F, -0.06981317007977318F, 0.0F, 0.0F));
        PartDefinition left_shawl_tail = body.addOrReplaceChild("left_shawl_tail", CubeListBuilder.create().texOffs(7, 17).addBox(-1.0F, 0.0F, -0.5F, 2.0F, 7.0F, 0.5F), PartPose.offsetAndRotation(2.5F, 2.5F, -2.5F, -0.06981317007977318F, 0.0F, 0.0F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(14, 17).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(27, 17).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition cane = left_arm.addOrReplaceChild("cane", CubeListBuilder.create().texOffs(40, 17).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 12.5F, 1.0F).texOffs(45, 17).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 1.0F, 2.5F), PartPose.offset(0.5F, 9.5F, -2.0F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(54, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F).texOffs(67, 17).addBox(-1.5F, 10.0F, -2.5F, 3.0F, 2.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(82, 17).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F).texOffs(95, 17).addBox(-1.5F, 10.0F, -2.5F, 3.0F, 2.0F, 4.0F), PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		// An old stoop: shoulders forward, the cane hand barely swinging.
		body.xRot = 0.12F;
		head.z = -0.8F;
		rightArm.z -= 0.6F;
		leftArm.z -= 0.6F;
		leftArm.xRot *= 0.3F;
		float sway = Mth.sin(ageInTicks * 0.07F) * 0.04F;
		rightShawlTail.xRot = -0.07F - limbSwingAmount * 0.3F + sway;
		leftShawlTail.xRot = -0.07F - limbSwingAmount * 0.3F - sway;
		halfMask.resetPose();
		if (state == ActState.PERFORM) {
			// The mask comes down: for the length of the act they are someone else.
			rightArm.xRot = -2.1F;
			rightArm.yRot = -0.35F;
			head.xRot = 0.1F;
			return;
		}
		halfMask.y -= 3.5F;
		halfMask.xRot = -0.35F;
		if (state == ActState.SETUP) {
			rightArm.xRot = -1.7F;
			rightArm.yRot = -0.3F;
		}
	}
}
