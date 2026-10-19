package com.vincenthuto.hemomancy.client.render.world;

import com.vincenthuto.hemomancy.client.render.world.QliphothBloomGeometry.Point;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QliphothSeveredGeometryTest {

	@Test
	void cutStaysNearBaseAndFallsSharplyAcrossTheTrunk() {
		QliphothSeveredGeometry.Stump stump = QliphothSeveredGeometry.stump();
		double high = Double.NEGATIVE_INFINITY;
		double low = Double.POSITIVE_INFINITY;
		for (Point point : stump.rim()) {
			high = Math.max(high, point.y());
			low = Math.min(low, point.y());
		}
		assertTrue(high < 3.0, "the pruned tree should be a low stump");
		assertTrue(low > 1.0, "the cut must remain above the root flare");
		assertTrue(high - low > 0.8, "the cross-section should read as a decisive diagonal cut");
	}

	@Test
	void barkWallNeverRisesThroughTheCut() {
		QliphothSeveredGeometry.Stump stump = QliphothSeveredGeometry.stump();
		for (Point[] row : stump.wall()) {
			for (Point point : row) {
				assertTrue(QliphothSeveredGeometry.aboveCut(point) <= 1.0e-9);
			}
		}
		Point[] top = stump.wall()[stump.wall().length - 1];
		for (int side = 0; side < top.length; side++) {
			assertEquals(stump.rim()[side], top[side], "the top ring must be the shared rim");
		}
	}

	@Test
	void rimRindAndFaceShareTheCutPlane() {
		QliphothSeveredGeometry.Stump stump = QliphothSeveredGeometry.stump();
		assertEquals(0, QliphothSeveredGeometry.aboveCut(stump.center()), 1.0e-9);
		for (int side = 0; side < stump.rim().length; side++) {
			assertEquals(0, QliphothSeveredGeometry.aboveCut(stump.rim()[side]), 1.0e-9);
			assertEquals(0, QliphothSeveredGeometry.aboveCut(stump.inner()[side]), 1.0e-9);
			assertEquals(QliphothSeveredGeometry.RIND,
					stump.rim()[side].distanceTo(stump.inner()[side]), 1.0e-9);
			float u = stump.faceU(stump.inner()[side]);
			float v = stump.faceV(stump.inner()[side]);
			assertTrue(u >= 0 && u <= 1 && v >= 0 && v <= 1);
		}
	}

	@Test
	void trunkVeinsStopUnderTheFace() {
		QliphothBloomGeometry.Limb trunk = QliphothSeveredGeometry.trunk();
		for (int index = 0; index < 3; index++) {
			List<Point> vein = QliphothSeveredGeometry.clipBelowCut(
					QliphothBloomGeometry.surfaceVein(trunk, index), .05);
			assertTrue(vein.size() >= 2);
			for (Point point : vein) assertTrue(QliphothSeveredGeometry.aboveCut(point) <= -.05 + 1.0e-9);
		}
	}
}
