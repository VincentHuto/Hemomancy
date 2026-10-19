package com.vincenthuto.hemomancy.client.model.entity.npc;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.util.Mth;

/**
 * The troupe's shared downed pose: sat on the ground, torso folded over the hips, head hung and hands
 * resting by the thighs. The torso folds about the hip, and the head and arms are carried with it, so
 * every limb stays seated on the posed body whatever the rig's proportions.
 */
final class CircusDownedPose {
	/** Model-space height of the ground under the feet. */
	private static final float GROUND = 24.0F;
	private final HumanoidModel<?> model;
	/** How far the legs reach behind their pivots; once laid forward, that is the seat height. */
	private final float seatDepth;
	/** How far each knee lifts, in radians; zero keeps the legs straight. */
	private final float kneeBend;
	private final ModelPart rightCalf;
	private final ModelPart leftCalf;

	CircusDownedPose(HumanoidModel<?> model, float seatDepth, float kneeBend) {
		this.model = model;
		this.seatDepth = seatDepth;
		this.kneeBend = kneeBend;
		rightCalf = model.rightLeg.hasChild("right_calf") ? model.rightLeg.getChild("right_calf") : null;
		leftCalf = model.leftLeg.hasChild("left_calf") ? model.leftLeg.getChild("left_calf") : null;
	}

	/** Poses the rig when downed, otherwise releases the knees; call last in setupAnim. */
	void apply(boolean downed, float ageInTicks) {
		if (!downed) {
			if (rightCalf != null) rightCalf.resetPose();
			if (leftCalf != null) leftCalf.resetPose();
			return;
		}
		float hipRestY = model.rightLeg.getInitialPose().y;
		float hipY = GROUND - seatDepth;
		float lean = 0.5F + Mth.sin(ageInTicks * 0.06F) * 0.03F;
		float cos = Mth.cos(lean);
		float sin = Mth.sin(lean);

		carry(model.body, hipRestY, hipY, cos, sin);
		model.body.setRotation(lean, 0.0F, 0.0F);
		carry(model.head, hipRestY, hipY, cos, sin);
		model.head.setRotation(lean + 0.45F, 0.0F, 0.18F);
		model.hat.copyFrom(model.head);
		carry(model.rightArm, hipRestY, hipY, cos, sin);
		model.rightArm.setRotation(-0.2F, 0.0F, 0.1F);
		carry(model.leftArm, hipRestY, hipY, cos, sin);
		model.leftArm.setRotation(-0.2F, 0.0F, -0.1F);

		seat(model.rightLeg, hipY, Mth.PI / 10.0F);
		seat(model.leftLeg, hipY, -Mth.PI / 10.0F);
		if (rightCalf != null) rightCalf.xRot = kneeBend * 2.0F;
		if (leftCalf != null) leftCalf.xRot = kneeBend * 2.0F;
	}

	/** Lays a leg forward along the ground from the lowered hip, knee lifted by the bend. */
	private void seat(ModelPart leg, float hipY, float splay) {
		leg.x = leg.getInitialPose().x;
		leg.y = hipY;
		leg.z = leg.getInitialPose().z;
		leg.setRotation(-Mth.HALF_PI - kneeBend, splay, 0.0F);
	}

	/** Moves an upper-body pivot rigidly with the torso as it lowers to the seat and folds forward. */
	private static void carry(ModelPart part, float hipRestY, float hipY, float cos, float sin) {
		PartPose rest = part.getInitialPose();
		float dy = rest.y - hipRestY;
		part.x = rest.x;
		part.y = hipY + dy * cos - rest.z * sin;
		part.z = dy * sin + rest.z * cos;
	}
}
