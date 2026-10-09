package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusThreadkeeperModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_threadkeeper"), "main");
	private final ModelPart spindle;

	public CircusThreadkeeperModel(ModelPart root) {
		super(root);
		spindle = body.getChild("spindle");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition eyeshade = head.addOrReplaceChild("eyeshade", CubeListBuilder.create().texOffs(33, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 2.0F, 9.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition visor = eyeshade.addOrReplaceChild("visor", CubeListBuilder.create().texOffs(70, 0).addBox(-4.0F, -0.5F, -3.0F, 8.0F, 0.5F, 3.0F), PartPose.offsetAndRotation(0.0F, -6.5F, -4.5F, 0.24434609527920614F, 0.0F, 0.0F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(93, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F).texOffs(0, 17).addBox(-3.0F, 1.0F, -2.5F, 6.0F, 6.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition collar = body.addOrReplaceChild("collar", CubeListBuilder.create().texOffs(15, 17).addBox(-3.0F, -1.5F, -2.5F, 6.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(38, 17).addBox(-4.5F, 7.0F, -2.5F, 9.0F, 1.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition spindle = body.addOrReplaceChild("spindle", CubeListBuilder.create().texOffs(67, 17).addBox(-0.5F, -1.5F, -0.5F, 1.0F, 3.0F, 1.0F).texOffs(72, 17).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 0.5F, 2.0F).texOffs(81, 17).addBox(-1.0F, 1.5F, -1.0F, 2.0F, 0.5F, 2.0F), PartPose.offset(-2.5F, 9.5F, -3.0F));
        PartDefinition right_coat_tail = body.addOrReplaceChild("right_coat_tail", CubeListBuilder.create().texOffs(90, 17).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(-2.0F, 11.0F, 2.5F, 0.10471975511965978F, 0.0F, 0.05235987755982989F));
        PartDefinition left_coat_tail = body.addOrReplaceChild("left_coat_tail", CubeListBuilder.create().texOffs(99, 17).addBox(-1.5F, 0.0F, -0.5F, 3.0F, 6.0F, 1.0F), PartPose.offsetAndRotation(2.0F, 11.0F, 2.5F, 0.10471975511965978F, 0.0F, -0.05235987755982989F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(108, 17).addBox(-2.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition right_garter = right_arm.addOrReplaceChild("right_garter", CubeListBuilder.create().texOffs(0, 33).addBox(-2.5F, 2.5F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(17, 33).addBox(-1.0F, -2.0F, -1.5F, 3.0F, 12.0F, 3.0F), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition left_garter = left_arm.addOrReplaceChild("left_garter", CubeListBuilder.create().texOffs(30, 33).addBox(-1.5F, 2.5F, -2.0F, 4.0F, 1.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition ledger = left_arm.addOrReplaceChild("ledger", CubeListBuilder.create().texOffs(47, 33).addBox(2.0F, -2.0F, -2.5F, 1.5F, 4.0F, 5.0F), PartPose.offset(0.0F, 9.0F, 0.0F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(62, 33).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F).texOffs(75, 33).addBox(-1.5F, 10.0F, -2.5F, 3.0F, 2.0F, 4.0F), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(90, 33).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 10.0F, 3.0F).texOffs(103, 33).addBox(-1.5F, 10.0F, -2.5F, 3.0F, 2.0F, 4.0F), PartPose.offset(1.9F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		// The spindle pays out slowly at rest and runs while a Crossbar is under inspection.
		spindle.yRot = ageInTicks * (onStage(state) ? 0.45F : 0.06F);
		if (onStage(state)) {
			head.xRot = 0.5F;
			leftArm.xRot = -1.05F;
			leftArm.yRot = 0.35F;
			leftArm.zRot = 0.0F;
			rightArm.xRot = -0.85F + Mth.sin(ageInTicks * 0.3F) * 0.12F;
			rightArm.yRot = -0.3F;
			rightArm.zRot = 0.0F;
		}
	}
}
