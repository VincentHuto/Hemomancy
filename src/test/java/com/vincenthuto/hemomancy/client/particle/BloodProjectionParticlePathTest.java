package com.vincenthuto.hemomancy.client.particle;

import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BloodProjectionParticlePathTest {
	private static final double EPSILON = 1.0E-9D;

	@Test
	void everySupportedDistancePreservesBothEndpoints() {
		Vec3 source = new Vec3(1.0D, 2.0D, 3.0D);
		Vec3 deviation = new Vec3(0.25D, -0.15D, 0.1D);

		for (double distance : new double[] { 1.0D, 2.0D, 5.5D }) {
			Vec3 target = source.add(0.0D, 0.0D, distance);

			assertVecEquals(source, BloodProjectionParticlePath.position(source, target, deviation, 0.0D));
			assertVecEquals(target, BloodProjectionParticlePath.position(source, target, deviation, 1.0D));
		}
	}

	@Test
	void organicDeviationExistsOnlyInsideThePath() {
		Vec3 source = Vec3.ZERO;
		Vec3 target = new Vec3(0.0D, 0.0D, 4.0D);
		Vec3 deviation = new Vec3(0.25D, 0.5D, 0.0D);

		assertVecEquals(new Vec3(0.25D, 0.5D, 2.0D),
				BloodProjectionParticlePath.position(source, target, deviation, 0.5D));
	}

	@Test
	void projectionDeviationRestoresAVisibleDistanceScaledArc() {
		Vec3 source = Vec3.ZERO;
		Vec3 target = new Vec3(5.5D, 0.0D, 0.0D);

		Vec3 deviation = BloodProjectionParticlePath.arcDeviation(source, target, 0.0D, 0.0D);
		Vec3 midpoint = BloodProjectionParticlePath.position(source, target, deviation, 0.5D);

		assertEquals(2.75D, midpoint.x, EPSILON);
		assertEquals(0.97D, midpoint.y, EPSILON);
		assertEquals(0.0D, midpoint.z, EPSILON);
	}

	@Test
	void arcRandomnessCannotMoveEitherEndpoint() {
		Vec3 source = new Vec3(-1.0D, 0.5D, 2.0D);
		Vec3 target = new Vec3(3.0D, 1.5D, -2.0D);
		Vec3 deviation = BloodProjectionParticlePath.arcDeviation(source, target, 1.0D, -1.0D);

		assertVecEquals(source, BloodProjectionParticlePath.position(source, target, deviation, 0.0D));
		assertVecEquals(target, BloodProjectionParticlePath.position(source, target, deviation, 1.0D));
	}

	@Test
	void lifetimeProgressIncludesAVisibleTerminalFrame() {
		assertEquals(0.0D, BloodProjectionParticlePath.progress(0, 10), EPSILON);
		assertEquals(1.0D, BloodProjectionParticlePath.progress(9, 10), EPSILON);
		assertEquals(1.0D, BloodProjectionParticlePath.progress(18, 19), EPSILON);
	}

	@Test
	void zeroLengthPathsRemainFinite() {
		Vec3 point = new Vec3(4.0D, -2.0D, 8.0D);
		Vec3 actual = BloodProjectionParticlePath.position(point, point, new Vec3(0.2D, 0.3D, 0.4D), 0.5D);

		assertTrue(Double.isFinite(actual.x));
		assertTrue(Double.isFinite(actual.y));
		assertTrue(Double.isFinite(actual.z));
	}

	private static void assertVecEquals(Vec3 expected, Vec3 actual) {
		assertEquals(expected.x, actual.x, EPSILON);
		assertEquals(expected.y, actual.y, EPSILON);
		assertEquals(expected.z, actual.z, EPSILON);
	}
}
