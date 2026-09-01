package com.vincenthuto.hemomancy.client.render.item.harbinger;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CellHandRenderOriginTest {
	private static final double EPSILON = 1.0E-6D;

	@Test
	void poseTranslationAndPalmOffsetBecomeAWorldPosition() {
		Matrix4f pose = new Matrix4f().translation(1.0F, 2.0F, 3.0F);
		Vec3 camera = new Vec3(10.0D, 20.0D, 30.0D);

		Vec3 actual = CellHandRenderOrigin.fromPose(pose, camera, 0.0F, 10.0F / 16.0F, 0.0F);

		assertEquals(11.0D, actual.x, EPSILON);
		assertEquals(22.625D, actual.y, EPSILON);
		assertEquals(33.0D, actual.z, EPSILON);
	}

	@Test
	void poseRotationControlsThePalmInsteadOfBodyYawApproximation() {
		Matrix4f pose = new Matrix4f().rotateZ((float) (Math.PI / 2.0D));

		Vec3 actual = CellHandRenderOrigin.fromPose(pose, Vec3.ZERO, 0.0F, 10.0F / 16.0F, 0.0F);

		assertEquals(-0.625D, actual.x, EPSILON);
		assertEquals(0.0D, actual.y, EPSILON);
		assertEquals(0.0D, actual.z, EPSILON);
	}
}
