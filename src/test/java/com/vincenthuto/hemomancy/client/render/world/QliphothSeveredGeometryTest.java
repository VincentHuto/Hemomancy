package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QliphothSeveredGeometryTest {

	@Test
	void cutStaysNearBaseAndFallsSharplyAcrossTheTrunk() {
		float highEdge = QliphothSeveredGeometry.cutHeight(-QliphothSeveredGeometry.CUT_RADIUS, 0.0f);
		float lowEdge = QliphothSeveredGeometry.cutHeight(QliphothSeveredGeometry.CUT_RADIUS, 0.0f);

		assertTrue(highEdge < 3.0f, "the pruned tree should be a low stump");
		assertTrue(lowEdge > 1.0f, "the cut must remain above the root flare");
		assertTrue(highEdge - lowEdge > 0.8f, "the cross-section should read as a decisive diagonal cut");
	}

	@Test
	void capAndTrunkShareTheSameCutPlane() {
		for (int facet = 0; facet < QliphothSeveredGeometry.FACETS; facet++) {
			QliphothSeveredGeometry.RimPoint point = QliphothSeveredGeometry.rimPoint(facet);
			assertEquals(QliphothSeveredGeometry.cutHeight(point.x(), point.z()), point.y(), 0.0001f);
			assertTrue(point.u() >= 0.0f && point.u() <= 1.0f);
			assertTrue(point.v() >= 0.0f && point.v() <= 1.0f);
		}
	}
}
