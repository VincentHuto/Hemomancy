package com.vincenthuto.hemomancy.client.model.entity.npc.court;

import com.mojang.blaze3d.vertex.PoseStack;
import com.vincenthuto.hemomancy.common.entity.npc.court.CourtMemberEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * The Brined Court's jointed humanoid (tools/model_export/court_humanoid.py): root -> hips -> body -> head and arms,
 * hips -> legs, with forearms and shins. The shared stances live here (idle, walk with bent knees and elbows, a
 * swimming stroke, the anchored kneel and its withdrawal, speaking, and the closed posture a member keeps near a
 * shunned visitor); each role's generated subclass adds its own touches in {@link #rolePose}.
 */
public abstract class CourtHumanoidModel extends CourtMemberModel {
	protected static final float DEG = Mth.DEG_TO_RAD;

	protected final ModelPart root;
	protected final ModelPart hips;
	protected final ModelPart body;
	protected final ModelPart head;
	protected final ModelPart rightArm;
	protected final ModelPart rightForearm;
	protected final ModelPart leftArm;
	protected final ModelPart leftForearm;
	protected final ModelPart rightLeg;
	protected final ModelPart rightShin;
	protected final ModelPart leftLeg;
	protected final ModelPart leftShin;

	protected CourtHumanoidModel(ModelPart mesh) {
		this.root = mesh.getChild("root");
		this.hips = root.getChild("hips");
		this.body = hips.getChild("body");
		this.head = body.getChild("head");
		this.rightArm = body.getChild("right_arm");
		this.rightForearm = rightArm.getChild("right_forearm");
		this.leftArm = body.getChild("left_arm");
		this.leftForearm = leftArm.getChild("left_forearm");
		this.rightLeg = hips.getChild("right_leg");
		this.rightShin = rightLeg.getChild("right_shin");
		this.leftLeg = hips.getChild("left_leg");
		this.leftShin = leftLeg.getChild("left_shin");
	}

	@Override
	public ModelPart root() {
		return root;
	}

	@Override
	public void setupAnim(CourtMemberEntity entity, float limbSwing, float limbSwingAmount, float ageInTicks,
			float netHeadYaw, float headPitch) {
		root.getAllParts().forEach(ModelPart::resetPose);
		float partial = ageInTicks - entity.tickCount;
		float phase = limbSwing * 0.6662F;
		float stride = Math.min(1.0F, limbSwingAmount * 1.4F);
		float breathe = Mth.sin(ageInTicks * 0.08F);
		head.yRot = netHeadYaw * DEG;
		head.xRot = headPitch * DEG;
		body.xRot = 0.02F * breathe;
		if (entity.role().anchored()) {
			anchored(entity.withdrawAmount(partial), ageInTicks);
		} else if (swimming(entity)) {
			swim(phase, stride, ageInTicks);
		} else {
			walk(phase, stride, breathe);
		}
		float talk = entity.talkAmount(partial);
		if (talk > 0.0F && !entity.role().anchored()) speak(talk, ageInTicks);
		float wary = entity.waryAmount(partial);
		if (wary > 0.0F && !entity.role().anchored()) closeUp(wary);
		rolePose(entity, limbSwing, stride, ageInTicks, talk);
	}

	/** In open water, off the floor and not anchored: the renderer lays the body along the stroke. */
	public static boolean swimming(CourtMemberEntity entity) {
		return entity.isInWater() && !entity.onGround() && !entity.role().anchored();
	}

	/** The role's own touches over the shared stance (generated from court_<role>_anim.java). */
	protected void rolePose(CourtMemberEntity entity, float limbSwing, float stride, float age, float talk) {
	}

	private void walk(float phase, float stride, float breathe) {
		float swing = Mth.cos(phase) * stride;
		rightLeg.xRot = swing;
		leftLeg.xRot = -swing;
		// The knee folds as the foot comes through, never backwards past straight.
		rightShin.xRot = stride * (0.25F + 0.75F * Math.max(0.0F, Mth.sin(phase)));
		leftShin.xRot = stride * (0.25F + 0.75F * Math.max(0.0F, -Mth.sin(phase)));
		rightArm.xRot = -swing * 0.7F;
		leftArm.xRot = swing * 0.7F;
		rightArm.zRot = 0.05F + 0.02F * breathe;
		leftArm.zRot = -0.05F - 0.02F * breathe;
		rightForearm.xRot = -0.15F - 0.3F * stride;
		leftForearm.xRot = -0.15F - 0.3F * stride;
	}

	private void swim(float phase, float stride, float age) {
		float stroke = Mth.sin(age * 0.12F);
		// A breast stroke: arms sweep out and back, forearms fold in on the recovery; legs kick from the hip.
		rightArm.xRot = -2.6F + 0.5F * stroke;
		leftArm.xRot = -2.6F + 0.5F * stroke;
		rightArm.zRot = 0.4F + 0.5F * Math.max(0.0F, stroke);
		leftArm.zRot = -0.4F - 0.5F * Math.max(0.0F, stroke);
		rightForearm.xRot = -0.9F * Math.max(0.0F, -stroke);
		leftForearm.xRot = -0.9F * Math.max(0.0F, -stroke);
		float kick = Mth.sin(age * 0.24F) * (0.3F + 0.3F * stride);
		rightLeg.xRot = kick;
		leftLeg.xRot = -kick;
		rightShin.xRot = 0.2F + 0.3F * Math.max(0.0F, kick);
		leftShin.xRot = 0.2F + 0.3F * Math.max(0.0F, -kick);
		head.xRot -= 0.9F;
	}

	/** Kneeling on the anchor, arms open; withdrawing folds the arms in and the head down. */
	private void anchored(float withdraw, float age) {
		float sway = Mth.sin(age * 0.03F);
		// Upright on the knees: thighs plumb, shins laid back along the ground, the knee on the anchor.
		hips.y += 6.0F;
		rightLeg.xRot = -0.08F;
		leftLeg.xRot = -0.08F;
		rightLeg.yRot = 0.1F;
		leftLeg.yRot = -0.1F;
		rightShin.xRot = 1.57F;
		leftShin.xRot = 1.57F;
		body.xRot = 0.12F + 0.01F * sway;
		rightArm.xRot = -0.45F;
		leftArm.xRot = -0.45F;
		rightArm.zRot = 0.55F;
		leftArm.zRot = -0.55F;
		rightForearm.xRot = -0.8F;
		leftForearm.xRot = -0.8F;
		if (withdraw <= 0.0F) return;
		toward(body, withdraw, 0.5F, 0.0F, 0.0F);
		toward(head, withdraw, 0.55F, head.yRot * 0.3F, 0.0F);
		toward(rightArm, withdraw, -0.7F, -0.2F, -0.2F);
		toward(leftArm, withdraw, -0.7F, 0.2F, 0.2F);
		toward(rightForearm, withdraw, -1.7F, 0.6F, 0.0F);
		toward(leftForearm, withdraw, -1.7F, -0.6F, 0.0F);
	}

	private void speak(float talk, float age) {
		float beat = Mth.sin(age * 0.35F);
		toward(rightArm, talk, -0.55F + 0.12F * beat, 0.1F, 0.15F);
		toward(rightForearm, talk, -0.95F - 0.15F * beat, 0.0F, 0.0F);
		head.xRot += talk * 0.06F * Mth.sin(age * 0.5F);
	}

	/** Arms folded and the head turned a little away. */
	private void closeUp(float wary) {
		toward(rightArm, wary, -0.55F, -0.25F, -0.25F);
		toward(leftArm, wary, -0.55F, 0.25F, 0.25F);
		toward(rightForearm, wary, -1.55F, 0.75F, 0.0F);
		toward(leftForearm, wary, -1.55F, -0.75F, 0.0F);
		head.yRot += wary * 0.35F;
		head.xRot += wary * 0.2F;
	}

	// ---------------------------------------------------------------- the strain anchor adapter

	public void toHead(PoseStack poseStack) {
		root.translateAndRotate(poseStack);
		hips.translateAndRotate(poseStack);
		body.translateAndRotate(poseStack);
		head.translateAndRotate(poseStack);
	}

	/** The player's body frame is at the neck; the rig's body pivots at the waist, ten pixels lower. */
	public void toBody(PoseStack poseStack) {
		root.translateAndRotate(poseStack);
		hips.translateAndRotate(poseStack);
		body.translateAndRotate(poseStack);
		poseStack.translate(0.0F, -10.0F / 16.0F, 0.0F);
	}

	public void toArm(PoseStack poseStack, boolean right) {
		root.translateAndRotate(poseStack);
		hips.translateAndRotate(poseStack);
		body.translateAndRotate(poseStack);
		(right ? rightArm : leftArm).translateAndRotate(poseStack);
	}

	/**
	 * The forearm, in the upper arm's frame: geometry authored down a straight player arm bends with the elbow. The
	 * segment's joint is undone after its rotation, so arm-space coordinates still land on the forearm's pixels.
	 */
	public void toForearm(PoseStack poseStack, boolean right) {
		toArm(poseStack, right);
		ModelPart forearm = right ? rightForearm : leftForearm;
		forearm.translateAndRotate(poseStack);
		poseStack.translate(-forearm.x / 16.0F, -forearm.y / 16.0F, -forearm.z / 16.0F);
	}

	public void toLeg(PoseStack poseStack, boolean right) {
		root.translateAndRotate(poseStack);
		hips.translateAndRotate(poseStack);
		(right ? rightLeg : leftLeg).translateAndRotate(poseStack);
	}

	public void toShin(PoseStack poseStack, boolean right) {
		toLeg(poseStack, right);
		ModelPart shin = right ? rightShin : leftShin;
		shin.translateAndRotate(poseStack);
		poseStack.translate(-shin.x / 16.0F, -shin.y / 16.0F, -shin.z / 16.0F);
	}
}
