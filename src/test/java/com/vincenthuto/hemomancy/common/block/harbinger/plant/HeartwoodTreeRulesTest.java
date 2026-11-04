package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tree a Dead Bush grows when blood is projected into it: tall, bending, branching, leafless, one core. */
class HeartwoodTreeRulesTest {
	private static final long[] SEEDS = { 0L, 1L, -7L, 20261103L, 9_876_543_210L, Long.MIN_VALUE };

	@Test
	void theTreeIsTall() {
		for (long seed : SEEDS) {
			int top = HeartwoodTreeRules.grow(seed).logs().stream().mapToInt(HeartwoodTreeRules.Cell::y).max().orElseThrow();
			assertTrue(top >= HeartwoodTreeRules.MIN_TRUNK_HEIGHT, "seed " + seed + " only reaches " + top);
		}
		assertTrue(HeartwoodTreeRules.MIN_TRUNK_HEIGHT >= 14, "taller than any vanilla oak");
	}

	@Test
	void theTrunkBends() {
		for (long seed : SEEDS) {
			Set<String> columns = new HashSet<>();
			for (HeartwoodTreeRules.Cell cell : HeartwoodTreeRules.grow(seed).trunk()) columns.add(cell.x() + "," + cell.z());
			assertTrue(columns.size() >= 4, "seed " + seed + " trunk stands in " + columns.size() + " columns");
		}
	}

	@Test
	void theTreeBranches() {
		for (long seed : SEEDS) {
			HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(seed);
			assertTrue(shape.branchCount() >= 5, "seed " + seed + " has " + shape.branchCount() + " branches");
			assertTrue(shape.logs().size() > shape.trunk().size() + 15, "branches are more than stubs");
		}
	}

	@Test
	void everyLogAndTheCoreJoinTheRootByFaces() {
		for (long seed : SEEDS) {
			HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(seed);
			Set<List<Integer>> all = new HashSet<>();
			for (HeartwoodTreeRules.Cell cell : shape.logs()) all.add(List.of(cell.x(), cell.y(), cell.z()));
			all.add(List.of(shape.core().x(), shape.core().y(), shape.core().z()));
			Set<List<Integer>> seen = new HashSet<>();
			Deque<List<Integer>> open = new ArrayDeque<>();
			open.add(List.of(0, 0, 0));
			seen.add(List.of(0, 0, 0));
			while (!open.isEmpty()) {
				List<Integer> at = open.pop();
				for (int[] step : new int[][] { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } }) {
					List<Integer> next = List.of(at.get(0) + step[0], at.get(1) + step[1], at.get(2) + step[2]);
					if (all.contains(next) && seen.add(next)) open.add(next);
				}
			}
			assertEquals(all, seen, "seed " + seed + " has floating wood");
		}
	}

	@Test
	void theCoreIsOneTrunkBlockAtReachWithNoLogOverIt() {
		for (long seed : SEEDS) {
			HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(seed);
			assertEquals(HeartwoodTreeRules.CORE_HEIGHT, shape.core().y());
			assertTrue(shape.core().y() >= 1 && shape.core().y() <= 3, "a player can reach it from the ground");
			assertFalse(shape.logs().stream().anyMatch(cell -> cell.x() == shape.core().x() && cell.y() == shape.core().y()
					&& cell.z() == shape.core().z()), "the core replaces the log there");
		}
	}

	@Test
	void theTrunkIsThickLowDownAndThinsOut() {
		for (long seed = 0; seed < 64; seed++) {
			HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(seed);
			long atWaist = shape.logs().stream().filter(cell -> cell.y() == HeartwoodTreeRules.CORE_HEIGHT + 2).count();
			assertTrue(atWaist >= 4, "seed " + seed + " has " + atWaist + " logs at its waist");
			java.util.Set<String> all = new java.util.HashSet<>();
			for (HeartwoodTreeRules.Cell cell : shape.logs()) all.add(cell.x() + "," + cell.y() + "," + cell.z());
			int trunkTop = shape.trunk().stream().mapToInt(HeartwoodTreeRules.Cell::y).max().orElseThrow();
			long thickLevels = shape.trunk().stream().filter(cell -> all.contains((cell.x() + 1) + "," + cell.y() + "," + cell.z())
					&& all.contains(cell.x() + "," + cell.y() + "," + (cell.z() + 1))
					&& all.contains((cell.x() + 1) + "," + cell.y() + "," + (cell.z() + 1))).map(HeartwoodTreeRules.Cell::y).distinct().count();
			assertTrue(thickLevels < trunkTop * 0.75D, "seed " + seed + " stays thick for " + thickLevels + " of " + trunkTop + " levels");
		}
	}

	@Test
	void theCoreKeepsAnOpenFaceToCutItFrom() {
		for (long seed = 0; seed < 64; seed++) {
			HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(seed);
			int x = shape.core().x();
			int y = shape.core().y();
			int z = shape.core().z();
			long open = java.util.stream.Stream.of(new int[] { 1, 0 }, new int[] { -1, 0 }, new int[] { 0, 1 }, new int[] { 0, -1 })
					.filter(side -> shape.logs().stream().noneMatch(cell -> cell.x() == x + side[0] && cell.y() == y && cell.z() == z + side[1])).count();
			assertTrue(open >= 2, "seed " + seed + " buries its core: " + open + " open sides");
		}
	}

	@Test
	void theRootIsALogAtTheBush() {
		HeartwoodTreeRules.Shape shape = HeartwoodTreeRules.grow(5L);
		assertTrue(shape.logs().stream().anyMatch(cell -> cell.x() == 0 && cell.y() == 0 && cell.z() == 0));
		assertTrue(shape.logs().stream().allMatch(cell -> cell.y() >= 0), "nothing grows below the bush");
	}

	@Test
	void eachCellOccursOnce() {
		for (long seed : SEEDS) {
			List<HeartwoodTreeRules.Cell> logs = HeartwoodTreeRules.grow(seed).logs();
			Set<String> unique = new HashSet<>();
			for (HeartwoodTreeRules.Cell cell : logs) assertTrue(unique.add(cell.x() + "," + cell.y() + "," + cell.z()));
		}
	}

	@Test
	void aSeedAlwaysGrowsTheSameTreeAndSeedsDiffer() {
		assertEquals(HeartwoodTreeRules.grow(42L), HeartwoodTreeRules.grow(42L));
		assertNotEquals(HeartwoodTreeRules.grow(42L).logs(), HeartwoodTreeRules.grow(43L).logs());
	}

	@Test
	void aSideStepIsLaidAlongItsAxis() {
		for (HeartwoodTreeRules.Cell cell : HeartwoodTreeRules.grow(11L).logs()) {
			assertTrue(cell.axis() == HeartwoodTreeRules.Axis.X || cell.axis() == HeartwoodTreeRules.Axis.Y
					|| cell.axis() == HeartwoodTreeRules.Axis.Z);
		}
		assertTrue(HeartwoodTreeRules.grow(11L).logs().stream().anyMatch(cell -> cell.axis() != HeartwoodTreeRules.Axis.Y),
				"branches run sideways");
	}

	@Test
	void theBushCostsTwiceTheSapling() {
		assertEquals(BloodwoodGrowthRules.PROJECTION_BLOOD_COST * 2, HeartwoodTreeRules.PROJECTION_BLOOD_COST);
	}
}
