package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

// Authored by tools/circus/build_faculty.py; regenerate the model, atlases and BBModel together.
public final class CircusStrongmanModel extends CircusFacultyModel {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Hemomancy.rloc("circus_strongman"), "main");
	/** Shoulder pivot to the centre of the fist. */
	private static final float GRIP = 10.0F;
	private final ModelPart barbell;

	public CircusStrongmanModel(ModelPart root) {
		super(root);
		barbell = body.getChild("barbell");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition mesh = new MeshDefinition();
        PartDefinition head = mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.5F, -7.0F, -3.5F, 7.0F, 7.0F, 7.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition moustache = head.addOrReplaceChild("moustache", CubeListBuilder.create().texOffs(29, 0).addBox(-2.5F, -2.0F, -4.0F, 5.0F, 1.0F, 0.5F).texOffs(42, 0).addBox(-3.5F, -2.5F, -4.0F, 1.0F, 1.0F, 0.5F).texOffs(47, 0).addBox(2.5F, -2.5F, -4.0F, 1.0F, 1.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition hat = mesh.getRoot().addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().texOffs(52, 0).addBox(-5.0F, 0.0F, -2.5F, 10.0F, 8.0F, 5.0F).texOffs(83, 0).addBox(-4.0F, 8.0F, -2.0F, 8.0F, 4.0F, 4.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition belt = body.addOrReplaceChild("belt", CubeListBuilder.create().texOffs(0, 15).addBox(-4.5F, 7.5F, -2.5F, 9.0F, 2.0F, 5.0F).texOffs(29, 15).addBox(-1.5F, 7.5F, -3.0F, 3.0F, 2.0F, 0.5F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition barbell = body.addOrReplaceChild("barbell", CubeListBuilder.create().texOffs(38, 15).addBox(-14.0F, -0.5F, -0.5F, 28.0F, 1.0F, 1.0F).texOffs(97, 15).addBox(-13.5F, -2.5F, -2.5F, 2.0F, 5.0F, 5.0F).texOffs(112, 15).addBox(11.5F, -2.5F, -2.5F, 2.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 4.0F, -10.0F));
        PartDefinition right_arm = mesh.getRoot().addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 26).addBox(-3.5F, -2.5F, -2.5F, 5.0F, 6.0F, 5.0F).texOffs(21, 26).addBox(-3.0F, 3.5F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(-6.5F, 2.0F, 0.0F));
        PartDefinition right_wristband = right_arm.addOrReplaceChild("right_wristband", CubeListBuilder.create().texOffs(38, 26).addBox(-3.5F, 6.5F, -2.5F, 5.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_leg = mesh.getRoot().addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(59, 26).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(80, 26).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 3.0F, 4.0F).texOffs(97, 26).addBox(-2.5F, 10.0F, -3.0F, 5.0F, 2.0F, 5.5F), PartPose.offset(-2.5F, 12.0F, 0.0F));
        PartDefinition left_arm = mesh.getRoot().addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 39).addBox(-1.5F, -2.5F, -2.5F, 5.0F, 6.0F, 5.0F).texOffs(21, 39).addBox(-1.0F, 3.5F, -2.0F, 4.0F, 6.0F, 4.0F), PartPose.offset(6.5F, 2.0F, 0.0F));
        PartDefinition left_wristband = left_arm.addOrReplaceChild("left_wristband", CubeListBuilder.create().texOffs(38, 39).addBox(-1.5F, 6.5F, -2.5F, 5.0F, 2.0F, 5.0F), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition left_leg = mesh.getRoot().addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(59, 39).addBox(-2.5F, 0.0F, -2.5F, 5.0F, 7.0F, 5.0F).texOffs(80, 39).addBox(-2.0F, 7.0F, -2.0F, 4.0F, 3.0F, 4.0F).texOffs(97, 39).addBox(-2.5F, 10.0F, -3.0F, 5.0F, 2.0F, 5.5F), PartPose.offset(2.5F, 12.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
	}

	@Override
	protected void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks) {
		barbell.visible = onStage(state);
		if (!onStage(state)) {
			// A wide, heavy stance between acts.
			rightArm.zRot += 0.12F;
			leftArm.zRot -= 0.12F;
			return;
		}
		float lift = state == ActState.PERFORM ? -2.95F + Mth.sin(ageInTicks * 0.2F) * 0.12F : -1.35F;
		rightArm.xRot = leftArm.xRot = lift;
		rightArm.yRot = leftArm.yRot = 0.0F;
		rightArm.zRot = leftArm.zRot = 0.0F;
		head.xRot = state == ActState.PERFORM ? -0.3F : 0.15F;
		barbell.y = rightArm.y + Mth.cos(lift) * GRIP;
		barbell.z = rightArm.z + Mth.sin(lift) * GRIP;
	}
}
