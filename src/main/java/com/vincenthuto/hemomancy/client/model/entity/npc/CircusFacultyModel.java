package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared rig behaviour for the faculty figures authored by tools/circus/build_faculty.py. */
public abstract class CircusFacultyModel extends HumanoidModel<CircusFacultyEntity> {
	private static final String STRONGMAN = "circus_strongman";
	private final CircusDownedPose downedPose;
	private final CircusWindupPose windup = new CircusWindupPose();

	/** @param seatDepth how far the authored legs reach behind their pivots */
	protected CircusFacultyModel(ModelPart root, float seatDepth) {
		super(root);
		downedPose = new CircusDownedPose(this, seatDepth, 0.0F);
	}

	@Override
	public final void setupAnim(CircusFacultyEntity entity, float limbSwing, float limbSwingAmount,
			float ageInTicks, float netHeadYaw, float headPitch) {
		super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
		// Humanoid attacks assume five-pixel shoulders; keep the authored shoulders attached.
		rightArm.x = Mth.cos(body.yRot) * rightArm.getInitialPose().x;
		rightArm.z = -Mth.sin(body.yRot) * rightArm.getInitialPose().x;
		leftArm.x = Mth.cos(body.yRot) * leftArm.getInitialPose().x;
		leftArm.z = -Mth.sin(body.yRot) * leftArm.getInitialPose().x;
		ActState state = entity.getActState();
		animateCostume(state, limbSwing, limbSwingAmount, ageInTicks);
		// Wind-up: the right arm raised back for an overhead blow; the strike brings it down.
		windup.sample(entity, ageInTicks);
		if (STRONGMAN.equals(entity.facultyRoleId())) {
			// FX-25: the Strongman coils with his whole frame, both fists hauled back over his head and his back
			// arched, so the blow reads from across the ring.
			rightArm.xRot = Mth.lerp(windup.charge(), rightArm.xRot, -3.05F);
			leftArm.xRot = Mth.lerp(windup.charge(), leftArm.xRot, -3.05F);
			rightArm.zRot = Mth.lerp(windup.charge(), rightArm.zRot, 0.35F);
			leftArm.zRot = Mth.lerp(windup.charge(), leftArm.zRot, -0.35F);
			body.xRot = Mth.lerp(windup.charge(), body.xRot, -0.32F);
			rightArm.xRot = Mth.lerp(windup.strike(), rightArm.xRot, -0.35F);
			leftArm.xRot = Mth.lerp(windup.strike(), leftArm.xRot, -0.35F);
			rightArm.zRot = Mth.lerp(windup.strike(), rightArm.zRot, 0.0F);
			leftArm.zRot = Mth.lerp(windup.strike(), leftArm.zRot, 0.0F);
			body.xRot = Mth.lerp(windup.strike(), body.xRot, 0.4F);
		} else {
			rightArm.xRot = Mth.lerp(windup.charge(), rightArm.xRot, -2.7F);
			body.xRot = Mth.lerp(windup.charge(), body.xRot, -0.12F);
			rightArm.xRot = Mth.lerp(windup.strike(), rightArm.xRot, -0.5F);
			body.xRot = Mth.lerp(windup.strike(), body.xRot, 0.15F);
		}
		downedPose.apply(state == ActState.DOWNED, ageInTicks);
	}

	/** Poses the costume parts; called every frame after the humanoid pose, so reset what you move. */
	protected abstract void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks);

	protected static boolean onStage(ActState state) {
		return state == ActState.SETUP || state == ActState.PERFORM;
	}
}
