package com.vincenthuto.hemomancy.client.model.entity.npc;

import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusFacultyEntity;
import com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPerformerEntity.ActState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/** Shared rig behaviour for the faculty figures authored by tools/circus/build_faculty.py. */
public abstract class CircusFacultyModel extends HumanoidModel<CircusFacultyEntity> {
	protected CircusFacultyModel(ModelPart root) {
		super(root);
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
		if (state == ActState.DOWNED) {
			body.xRot = 1.25F;
			head.xRot = 0.55F;
			rightArm.xRot = leftArm.xRot = -0.8F;
		}
	}

	/** Poses the costume parts; called every frame after the humanoid pose, so reset what you move. */
	protected abstract void animateCostume(ActState state, float limbSwing, float limbSwingAmount, float ageInTicks);

	protected static boolean onStage(ActState state) {
		return state == ActState.SETUP || state == ActState.PERFORM;
	}
}
