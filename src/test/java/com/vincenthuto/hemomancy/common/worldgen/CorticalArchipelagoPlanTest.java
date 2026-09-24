package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class CorticalArchipelagoPlanTest {
	private static final CorticalArchipelagoPlan.SpotFilter ACCEPT_ALL = (x, z, r) -> true;

	@Test
	void planIsDeterministicAcrossSeeds() {
		for (long seed : new long[] { 0L, 1L, 7L, 42L, -31337L }) {
			List<CorticalArchipelagoPlan.Node> a = CorticalArchipelagoPlan.plan(seed, 3, -2, ACCEPT_ALL);
			List<CorticalArchipelagoPlan.Node> b = CorticalArchipelagoPlan.plan(seed, 3, -2, ACCEPT_ALL);
			assertEquals(a, b, "seed " + seed + " must plan identically or chunks will disagree at borders");
		}
	}

	@Test
	void islandAndHubCountsStayInRange() {
		for (long seed = 0; seed < 30; seed++) {
			List<CorticalArchipelagoPlan.Node> nodes = CorticalArchipelagoPlan.plan(seed, 0, 0, ACCEPT_ALL);
			long islands = nodes.stream().filter(n -> n.kind() == CorticalArchipelagoPlan.Kind.ISLAND).count();
			long hubs = nodes.stream().filter(n -> n.kind() == CorticalArchipelagoPlan.Kind.HUB).count();
			assertTrue(islands >= 22 && islands <= 32,
					"seed " + seed + " produced " + islands + " islands, expected 22..32");
			assertTrue(hubs >= 3 && hubs <= 5, "seed " + seed + " produced " + hubs + " hubs, expected 3..5");
		}
	}

	@Test
	void islandsFormTwoReadableOrbitsAroundTheMind() {
		int mindX = 1200;
		int mindZ = -900;
		for (long seed = 0; seed < 20; seed++) {
			List<CorticalArchipelagoPlan.Node> nodes = CorticalArchipelagoPlan.plan(
					seed, mindX, mindZ, ACCEPT_ALL);
			boolean[] occupiedSectors = new boolean[12];
			int inner = 0;
			int outer = 0;
			for (CorticalArchipelagoPlan.Node node : nodes) {
				if (node.kind() != CorticalArchipelagoPlan.Kind.ISLAND) {
					continue;
				}
				double dx = node.x() - mindX;
				double dz = node.z() - mindZ;
				double distance = Math.sqrt(dx * dx + dz * dz);
				assertTrue(distance >= 150.0 && distance <= 285.0,
						"seed " + seed + " island at radius " + distance
								+ " is not orbiting the Mind");
				if (distance < 215.0) {
					inner++;
				} else {
					outer++;
				}
				double angle = Math.atan2(dz, dx);
				int sector = Math.floorMod((int) Math.floor((angle + Math.PI) / (Math.PI * 2.0) * 12.0), 12);
				occupiedSectors[sector] = true;
			}
			int sectors = 0;
			for (boolean occupied : occupiedSectors) {
				sectors += occupied ? 1 : 0;
			}
			assertTrue(inner >= 8, "seed " + seed + " needs a readable inner orbit");
			assertTrue(outer >= 8, "seed " + seed + " needs a readable outer orbit");
			assertTrue(sectors >= 10,
					"seed " + seed + " leaves too large an angular gap around the central Mind");
		}
	}

	@Test
	void orbitalIslandsStayNearTheMindsVerticalBand() {
		for (long seed = 0; seed < 20; seed++) {
			for (CorticalArchipelagoPlan.Node node : CorticalArchipelagoPlan.plan(seed, 0, 0, ACCEPT_ALL)) {
				if (node.kind() == CorticalArchipelagoPlan.Kind.ISLAND) {
					assertTrue(node.y() >= 70 && node.y() <= 166,
							"an orbit should tilt around the Mind, not scatter through the full End height");
				}
			}
		}
	}

	@Test
	void everyNodeStaysWithinTheOrbitalPlanningReach() {
		int mindX = 5;
		int mindZ = -3;
		for (long seed = 0; seed < 20; seed++) {
			List<CorticalArchipelagoPlan.Node> nodes = CorticalArchipelagoPlan.plan(seed, mindX, mindZ, ACCEPT_ALL);
			for (CorticalArchipelagoPlan.Node node : nodes) {
				double dx = node.x() - mindX;
				double dz = node.z() - mindZ;
				assertTrue(Math.sqrt(dx * dx + dz * dz) <= 300.0,
						"seed " + seed + " node escaped the Mind's orbital system");
			}
		}
	}

	@Test
	void noTwoIslandsViolateTheNonOverlapRule() {
		for (long seed = 0; seed < 20; seed++) {
			List<CorticalArchipelagoPlan.Node> nodes = CorticalArchipelagoPlan.plan(seed, 0, 0, ACCEPT_ALL);
			List<CorticalArchipelagoPlan.Node> islands = nodes.stream()
					.filter(n -> n.kind() == CorticalArchipelagoPlan.Kind.ISLAND).toList();
			for (int i = 0; i < islands.size(); i++) {
				for (int j = i + 1; j < islands.size(); j++) {
					CorticalArchipelagoPlan.Node a = islands.get(i);
					CorticalArchipelagoPlan.Node b = islands.get(j);
					double dx = a.x() - b.x();
					double dz = a.z() - b.z();
					double horizontalDist = Math.sqrt(dx * dx + dz * dz);
					if (horizontalDist >= a.radius() + b.radius() + 10) {
						continue;
					}
					int depthA = CorticalIslandShape.maxDepth(a);
					int depthB = CorticalIslandShape.maxDepth(b);
					double topA = a.y() + 3;
					double bottomA = a.y() - depthA;
					double topB = b.y() + 3;
					double bottomB = b.y() - depthB;
					boolean verticalOverlap = bottomA <= topB && bottomB <= topA;
					assertFalse(verticalOverlap,
							"seed " + seed + " islands " + i + " and " + j + " are horizontally close ("
									+ horizontalDist + ") and vertically overlap");
				}
			}
		}
	}

	@Test
	void filterRejectingEverythingProducesAnEmptyList() {
		List<CorticalArchipelagoPlan.Node> nodes = CorticalArchipelagoPlan.plan(123L, 0, 0, (x, z, r) -> false);
		assertTrue(nodes.isEmpty(), "a filter that rejects everything must never place a node");
	}

	@Test
	void filterRejectingAHalfPlaneKeepsNodesOutOfIt() {
		int mindX = 0;
		int mindZ = 0;
		int midX = mindX;
		CorticalArchipelagoPlan.SpotFilter rejectEastHalf = (x, z, r) -> x < midX;
		List<CorticalArchipelagoPlan.Node> nodes = CorticalArchipelagoPlan.plan(9001L, mindX, mindZ, rejectEastHalf);
		assertFalse(nodes.isEmpty(), "sanity: the west half should still be plannable");
		for (CorticalArchipelagoPlan.Node node : nodes) {
			assertTrue(node.x() < midX, "node at x=" + node.x() + " should have been rejected by the half-plane filter");
		}
	}

	@Test
	void cableEndpointOnIslandLiesInsideTheMinimumNoisyEdge() {
		CorticalArchipelagoPlan.Node island = new CorticalArchipelagoPlan.Node(
				CorticalArchipelagoPlan.Kind.ISLAND, 0, 100, 0, 10, 5L);
		CorticalArchipelagoPlan.Node other = new CorticalArchipelagoPlan.Node(
				CorticalArchipelagoPlan.Kind.ISLAND, 100, 100, 0, 10, 6L);
		double[][] endpoints = CorticalArchipelagoPlan.cableEndpoints(island, other);
		double[] fromEnd = endpoints[0];
		double expectedX = 7.0;
		assertEquals(expectedX, fromEnd[0], 1.0e-6, "the rim point sits at radius*0.7, inside the minimum noisy edge (radius*0.75), so cable anchors never float off the island");
		assertEquals(0.0, fromEnd[2], 1.0e-6, "the rim point should stay on the line toward the other node");
		assertEquals(island.y() - 1, fromEnd[1], 1.0e-6, "island cable ends hang one block below the top");
	}

	@Test
	void hubCableEndpointIsExactlyItsCentre() {
		CorticalArchipelagoPlan.Node hub = new CorticalArchipelagoPlan.Node(
				CorticalArchipelagoPlan.Kind.HUB, 50, 70, -20, 2, 1L);
		CorticalArchipelagoPlan.Node other = new CorticalArchipelagoPlan.Node(
				CorticalArchipelagoPlan.Kind.ISLAND, 0, 100, 0, 10, 2L);
		double[][] endpoints = CorticalArchipelagoPlan.cableEndpoints(other, hub);
		double[] hubEnd = endpoints[1];
		assertEquals(hub.x(), hubEnd[0], 1.0e-6);
		assertEquals(hub.y(), hubEnd[1], 1.0e-6);
		assertEquals(hub.z(), hubEnd[2], 1.0e-6);
	}

	@Test
	void sagEndpointsAreExactAndMidpointIsLower() {
		double[] from = { 0, 100, 0 };
		double[] to = { 40, 100, 0 };
		double[] start = CorticalArchipelagoPlan.sag(from, to, 0.0);
		double[] end = CorticalArchipelagoPlan.sag(from, to, 1.0);
		double[] mid = CorticalArchipelagoPlan.sag(from, to, 0.5);
		assertEquals(from[0], start[0], 1.0e-9);
		assertEquals(from[1], start[1], 1.0e-9);
		assertEquals(from[2], start[2], 1.0e-9);
		assertEquals(to[0], end[0], 1.0e-9);
		assertEquals(to[1], end[1], 1.0e-9);
		assertEquals(to[2], end[2], 1.0e-9);
		assertTrue(mid[1] < from[1], "the cable should sag below the level line at its midpoint");
		double expectedDroop = 40 * 0.12;
		assertEquals(from[1] - expectedDroop, mid[1], 1.0e-9, "the midpoint should dip span*0.12 below the line");
	}
}
