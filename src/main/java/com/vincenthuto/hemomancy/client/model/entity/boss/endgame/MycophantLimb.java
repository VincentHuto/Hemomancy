package com.vincenthuto.hemomancy.client.model.entity.boss.endgame;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/** Two joint solver in the limb parent's frame, with the bend inferred from the authored rest pose. */
final class MycophantLimb {
	private final ModelPart upper;
	private final ModelPart elbow;
	final ModelPart wrist;
	private final Vector3f shoulder;
	private final Vector3f upperRest;
	private final Vector3f foreRest;
	private final Vector3f wristRest;
	private final Vector3f bend;
	private final float upperLength;
	private final float foreLength;
	private final Quaternionf foreRotation = new Quaternionf();
	private final Vector3f wristPosition = new Vector3f();

	MycophantLimb(ModelPart upper, ModelPart elbow, ModelPart wrist) {
		this.upper = upper;
		this.elbow = elbow;
		this.wrist = wrist;
		this.shoulder = pivot(upper);
		this.upperRest = pivot(elbow);
		this.foreRest = pivot(wrist);
		this.wristRest = new Vector3f(shoulder).add(upperRest).add(foreRest);
		Vector3f axis = new Vector3f(wristRest).sub(shoulder).normalize();
		this.bend = new Vector3f(upperRest).sub(new Vector3f(axis).mul(upperRest.dot(axis))).normalize();
		this.upperLength = upperRest.length();
		this.foreLength = foreRest.length();
	}

	Vector3f target(float x, float y, float z) {
		return new Vector3f(wristRest).add(x, y, z);
	}

	Vector3f fromShoulder(Vector3f target, float distance) {
		target.sub(shoulder);
		if (target.lengthSquared() < 0.001F) target.set(wristRest).sub(shoulder);
		return target.normalize(distance).add(shoulder);
	}

	void transformWrist(PoseStack poses) {
		upper.translateAndRotate(poses);
		elbow.translateAndRotate(poses);
		wrist.translateAndRotate(poses);
	}

	void pose(Vector3f target, boolean steadyWrist) {
		pose(target, steadyWrist, new Vector3f());
	}

	void pose(Vector3f target, boolean steadyWrist, Vector3f guide) {
		Vector3f axis = new Vector3f(target).sub(shoulder);
		float length = axis.length();
		if (length > 0.001F) axis.div(length);
		else axis.set(wristRest).sub(shoulder).normalize();
		float distance = Mth.clamp(length, Math.abs(upperLength - foreLength) + 0.25F, upperLength + foreLength - 0.25F);
		float along = (upperLength * upperLength - foreLength * foreLength + distance * distance) / (2.0F * distance);
		float lift = Mth.sqrt(Math.max(0.0F, upperLength * upperLength - along * along));
		Vector3f plane = new Vector3f(bend).add(guide);
		plane.sub(new Vector3f(axis).mul(plane.dot(axis)));
		if (plane.lengthSquared() < 0.001F) {
			if (Math.abs(axis.z) < 0.9F) plane.set(axis).cross(0.0F, 0.0F, 1.0F);
			else plane.set(axis).cross(0.0F, 1.0F, 0.0F);
		}
		wristPosition.set(axis).mul(distance).add(shoulder);
		Vector3f first = new Vector3f(axis).mul(along).add(plane.normalize(lift));
		Vector3f second = new Vector3f(axis).mul(distance).sub(first);
		Quaternionf upperRotation = new Quaternionf().rotationTo(new Vector3f(upperRest).normalize(), first.normalize());
		foreRotation.rotationTo(new Vector3f(foreRest).normalize(), second.normalize());
		rotate(upper, upperRotation);
		rotate(elbow, upperRotation.conjugate().mul(foreRotation));
		if (steadyWrist) rotate(wrist, new Quaternionf(foreRotation).conjugate());
	}

	void aimWrist(Vector3f target, Vector3f restForward, float weight) {
		Vector3f direction = new Vector3f(target).sub(wristPosition);
		if (direction.lengthSquared() < 0.001F) return;
		new Quaternionf(foreRotation).conjugate().transform(direction.normalize());
		Quaternionf aim = new Quaternionf().rotationTo(restForward, direction);
		rotate(wrist, new Quaternionf().slerp(aim, weight));
	}

	private static Vector3f pivot(ModelPart part) {
		return new Vector3f(part.x, part.y, part.z);
	}

	private static void rotate(ModelPart part, Quaternionf rotation) {
		Vector3f angles = rotation.getEulerAnglesZYX(new Vector3f());
		part.xRot = angles.x;
		part.yRot = angles.y;
		part.zRot = angles.z;
	}
}
