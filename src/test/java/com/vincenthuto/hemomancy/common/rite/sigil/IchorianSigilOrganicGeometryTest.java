package com.vincenthuto.hemomancy.common.rite.sigil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class IchorianSigilOrganicGeometryTest {

	@Test
	void livingStrokeKeepsAuthoredNodesFixedWhileItsBodyWrithes() {
		var start = IchorianSigilOrganicGeometry.sample(
				-1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D,
				40.0F, 91L, 0, 6, 0.1F);
		var middle = IchorianSigilOrganicGeometry.sample(
				-1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D,
				40.0F, 91L, 3, 6, 0.1F);
		var end = IchorianSigilOrganicGeometry.sample(
				-1.0D, 0.0D, 0.0D, 1.0D, 0.0D, 0.0D,
				40.0F, 91L, 6, 6, 0.1F);

		assertPoint(start, -1.0D, 0.0D, 0.0D);
		assertPoint(end, 1.0D, 0.0D, 0.0D);
		assertNotEquals(0.0D, middle.z(), 0.00001D, "the vessel body should not remain ruler-straight");
		assertNotEquals(0.0D, middle.y(), 0.00001D, "the vessel should subtly lift and contract");
	}

	@Test
	void deformationStaysSmallEnoughToPreserveTheReadableSigil() {
		for (int step = 0; step <= 8; step++) {
			var point = IchorianSigilOrganicGeometry.sample(
					0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 3.0D,
					125.0F, 287L, step, 8, 0.12F);
			double authoredX = 0.0D;
			double authoredY = 0.0D;
			double authoredZ = 3.0D * step / 8.0D;
			assertTrue(Math.sqrt(
					(point.x() - authoredX) * (point.x() - authoredX)
							+ (point.y() - authoredY) * (point.y() - authoredY)
							+ (point.z() - authoredZ) * (point.z() - authoredZ)) <= 0.13D);
			assertTrue(point.halfWidth() > 0.0F);
		}
	}

	@Test
	void nodesBeatAtDifferentPhasesWithoutExtremeScaling() {
		float first = IchorianSigilOrganicGeometry.nodePulse(60.0F, 41L, 0);
		float second = IchorianSigilOrganicGeometry.nodePulse(60.0F, 41L, 1);

		assertNotEquals(first, second);
		assertTrue(first >= 0.88F && first <= 1.12F);
		assertTrue(second >= 0.88F && second <= 1.12F);
	}

	private static void assertPoint(IchorianSigilOrganicGeometry.Sample point,
			double x, double y, double z) {
		assertEquals(x, point.x(), 0.000001D);
		assertEquals(y, point.y(), 0.000001D);
		assertEquals(z, point.z(), 0.000001D);
	}
}
