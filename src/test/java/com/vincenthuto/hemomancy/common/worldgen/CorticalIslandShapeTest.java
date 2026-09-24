package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CorticalIslandShapeTest {
	private static CorticalArchipelagoPlan.Node island(long shapeSeed) {
		return new CorticalArchipelagoPlan.Node(CorticalArchipelagoPlan.Kind.ISLAND, 0, 100, 0, 14, shapeSeed);
	}

	@Test
	void topCentreIsMyelinAndOneBelowIsSlate() {
		CorticalArchipelagoPlan.Node node = island(7L);
		assertEquals(CorticalIslandShape.Layer.MYELIN, CorticalIslandShape.sample(node, 0, 0, 0));
		assertEquals(CorticalIslandShape.Layer.SLATE, CorticalIslandShape.sample(node, 0, -1, 0));
	}

	@Test
	void deepBelowCentreIsTissueOrGanglion() {
		CorticalArchipelagoPlan.Node node = island(7L);
		CorticalIslandShape.Layer layer = CorticalIslandShape.sample(node, 0, -10, 0);
		assertTrue(layer == CorticalIslandShape.Layer.TISSUE || layer == CorticalIslandShape.Layer.GANGLION,
				"expected TISSUE or GANGLION ten blocks below centre, got " + layer);
	}

	@Test
	void farOutsideMaxReachIsAir() {
		CorticalArchipelagoPlan.Node node = island(7L);
		int reach = CorticalIslandShape.maxReach(node);
		assertNull(CorticalIslandShape.sample(node, reach + 20, 0, 0),
				"far outside maxReach must be air");
	}

	@Test
	void belowMaxDepthIsAir() {
		CorticalArchipelagoPlan.Node node = island(7L);
		int depth = CorticalIslandShape.maxDepth(node);
		assertNull(CorticalIslandShape.sample(node, 0, -(depth + 20), 0),
				"below maxDepth must be air");
	}

	@Test
	void undersideAtCentreIsDeeperThanNearTheEdge() {
		CorticalArchipelagoPlan.Node node = island(7L);
		int deepestNull = -1;
		for (int dy = 0; dy > -400; dy--) {
			if (CorticalIslandShape.sample(node, 0, dy, 0) == null) {
				deepestNull = dy;
				break;
			}
		}
		int centreBottom = deepestNull + 1;

		int reach = CorticalIslandShape.maxReach(node);
		int edgeDx = (int) Math.round(reach * 0.9);
		int edgeDeepestNull = -1;
		for (int dy = 0; dy > -400; dy--) {
			if (CorticalIslandShape.sample(node, edgeDx, dy, 0) == null) {
				edgeDeepestNull = dy;
				break;
			}
		}
		int edgeBottom = edgeDeepestNull + 1;

		assertTrue(centreBottom < edgeBottom,
				"expected centre column (bottom=" + centreBottom + ") to reach deeper than the near-edge column "
						+ "(bottom=" + edgeBottom + ")");
	}

	@Test
	void sampleIsDeterministic() {
		CorticalArchipelagoPlan.Node node = island(42L);
		for (int i = 0; i < 200; i++) {
			int dx = (i * 3) % 21 - 10;
			int dy = -(i % 30);
			int dz = (i * 5) % 21 - 10;
			assertEquals(CorticalIslandShape.sample(node, dx, dy, dz), CorticalIslandShape.sample(node, dx, dy, dz),
					"resampling the same offset must give the same layer");
		}
	}

	@Test
	void someGanglionExistsSomewhereInASampledIsland() {
		CorticalArchipelagoPlan.Node node = island(7L);
		boolean foundGanglion = false;
		for (int dx = -18; dx <= 18 && !foundGanglion; dx++) {
			for (int dz = -18; dz <= 18 && !foundGanglion; dz++) {
				for (int dy = 0; dy > -40; dy--) {
					if (CorticalIslandShape.sample(node, dx, dy, dz) == CorticalIslandShape.Layer.GANGLION) {
						foundGanglion = true;
						break;
					}
				}
			}
		}
		assertTrue(foundGanglion, "expected at least one GANGLION vein somewhere in the sampled island");
	}
}
