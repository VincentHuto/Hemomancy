package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/**
 * A Blood Wood tree is a low fork of arterial limbs, each carrying its own clot of leaves with strands hanging from it.
 * The flat single umbrella it replaced read as an acacia.
 */
class BloodwoodGrowthRulesTest {
	private static final int SEEDS = 64;

	private static String key(int x, int y, int z) {
		return x + "," + y + "," + z;
	}

	private static Set<String> logKeys(BloodwoodGrowthRules.Shape shape) {
		Set<String> keys = new HashSet<>();
		for (HeartwoodTreeRules.Cell cell : shape.logs()) keys.add(key(cell.x(), cell.y(), cell.z()));
		return keys;
	}

	@Test
	void aSaplingTakesNineHundredMillilitres() {
		assertEquals(900.0D, BloodwoodGrowthRules.PROJECTION_BLOOD_COST);
	}

	@Test
	void theTrunkForksLowIntoSeveralLimbs() {
		for (long seed = 0; seed < SEEDS; seed++) {
			BloodwoodGrowthRules.Shape shape = BloodwoodGrowthRules.grow(seed);
			assertTrue(shape.limbs() >= 3, "seed " + seed + " has " + shape.limbs() + " limbs");
			long aboveFork = shape.logs().stream().filter(cell -> cell.y() == shape.forkHeight() + 2).count();
			assertTrue(aboveFork >= 3, "seed " + seed + " has " + aboveFork + " logs two above its fork");
			assertTrue(shape.forkHeight() <= 5, "seed " + seed + " forks at " + shape.forkHeight());
		}
	}

	@Test
	void everyLogJoinsTheRootByFaces() {
		for (long seed = 0; seed < SEEDS; seed++) {
			Set<String> all = logKeys(BloodwoodGrowthRules.grow(seed));
			assertTrue(all.contains(key(0, 0, 0)), "no root at the sapling");
			Set<String> seen = new HashSet<>(List.of(key(0, 0, 0)));
			ArrayDeque<int[]> open = new ArrayDeque<>(List.of(new int[] { 0, 0, 0 }));
			int[][] sides = { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } };
			while (!open.isEmpty()) {
				int[] at = open.poll();
				for (int[] side : sides) {
					String next = key(at[0] + side[0], at[1] + side[1], at[2] + side[2]);
					if (all.contains(next) && seen.add(next)) open.add(new int[] { at[0] + side[0], at[1] + side[1], at[2] + side[2] });
				}
			}
			assertEquals(all, seen, "seed " + seed + " has floating wood");
		}
	}

	@Test
	void theLeavesFormSeparateCrownsNotOneCanopy() {
		for (long seed = 0; seed < SEEDS; seed++) {
			Set<BloodwoodGrowthRules.Offset> leaves = BloodwoodGrowthRules.grow(seed).leaves();
			Set<BloodwoodGrowthRules.Offset> unseen = new HashSet<>(leaves);
			int crowns = 0;
			while (!unseen.isEmpty()) {
				BloodwoodGrowthRules.Offset start = unseen.iterator().next();
				unseen.remove(start);
				ArrayDeque<BloodwoodGrowthRules.Offset> open = new ArrayDeque<>(List.of(start));
				int size = 1;
				while (!open.isEmpty()) {
					BloodwoodGrowthRules.Offset at = open.poll();
					for (int[] side : new int[][] { { 1, 0, 0 }, { -1, 0, 0 }, { 0, 1, 0 }, { 0, -1, 0 }, { 0, 0, 1 }, { 0, 0, -1 } }) {
						BloodwoodGrowthRules.Offset next = new BloodwoodGrowthRules.Offset(at.x() + side[0], at.y() + side[1], at.z() + side[2]);
						if (unseen.remove(next)) {
							open.add(next);
							size++;
						}
					}
				}
				if (size >= 12) crowns++;
			}
			assertTrue(crowns >= 2, "seed " + seed + " has " + crowns + " distinct crowns");
		}
	}

	@Test
	void noSingleLayerHoldsTheCrown() {
		for (long seed = 0; seed < SEEDS; seed++) {
			Set<BloodwoodGrowthRules.Offset> leaves = BloodwoodGrowthRules.grow(seed).leaves();
			Map<Integer, Integer> perLayer = new HashMap<>();
			for (BloodwoodGrowthRules.Offset leaf : leaves) perLayer.merge(leaf.y(), 1, Integer::sum);
			int fullest = perLayer.values().stream().mapToInt(Integer::intValue).max().orElseThrow();
			assertTrue(fullest * 100 / leaves.size() < 35, "seed " + seed + " puts " + fullest + " of " + leaves.size() + " leaves in one layer");
			assertTrue(perLayer.size() >= 7, "seed " + seed + " is only " + perLayer.size() + " layers deep");
		}
	}

	@Test
	void strandsOfLeavesHangBelowTheCrowns() {
		for (long seed = 0; seed < SEEDS; seed++) {
			BloodwoodGrowthRules.Shape shape = BloodwoodGrowthRules.grow(seed);
			Set<String> logs = logKeys(shape);
			Set<BloodwoodGrowthRules.Offset> leaves = shape.leaves();
			int strands = 0;
			for (BloodwoodGrowthRules.Offset leaf : leaves) {
				boolean tipOfAStrand = !leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x(), leaf.y() - 1, leaf.z()))
						&& leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x(), leaf.y() + 1, leaf.z()))
						&& leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x(), leaf.y() + 2, leaf.z()))
						&& !logs.contains(key(leaf.x(), leaf.y() - 1, leaf.z()));
				boolean thin = !leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x() + 1, leaf.y(), leaf.z()))
						|| !leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x() - 1, leaf.y(), leaf.z()))
						|| !leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x(), leaf.y(), leaf.z() + 1))
						|| !leaves.contains(new BloodwoodGrowthRules.Offset(leaf.x(), leaf.y(), leaf.z() - 1));
				if (tipOfAStrand && thin) strands++;
			}
			assertTrue(strands >= 8, "seed " + seed + " has " + strands + " hanging strands");
			assertTrue(leaves.stream().allMatch(leaf -> leaf.y() >= BloodwoodGrowthRules.LOWEST_LEAF), "a strand reaches head height");
		}
	}

	@Test
	void logsAndLeavesNeverShareACell() {
		for (long seed = 0; seed < SEEDS; seed++) {
			BloodwoodGrowthRules.Shape shape = BloodwoodGrowthRules.grow(seed);
			Set<String> logs = logKeys(shape);
			assertEquals(logs.size(), shape.logs().size(), "seed " + seed + " repeats a log");
			for (BloodwoodGrowthRules.Offset leaf : shape.leaves()) assertTrue(!logs.contains(key(leaf.x(), leaf.y(), leaf.z())), "seed " + seed);
		}
	}

	@Test
	void theWoodStaysInsideTheSpaceTheLessonPromises() {
		for (long seed = 0; seed < SEEDS; seed++) {
			for (HeartwoodTreeRules.Cell cell : BloodwoodGrowthRules.grow(seed).logs()) {
				assertTrue(Math.abs(cell.x()) <= BloodwoodGrowthRules.MAX_WOOD_OUT && Math.abs(cell.z()) <= BloodwoodGrowthRules.MAX_WOOD_OUT,
						"seed " + seed + " reaches out to " + cell);
				assertTrue(cell.y() >= 0 && cell.y() <= BloodwoodGrowthRules.MAX_WOOD_UP, "seed " + seed + " reaches up to " + cell);
			}
		}
		assertEquals(8, BloodwoodGrowthRules.MAX_WOOD_OUT);
		assertEquals(13, BloodwoodGrowthRules.MAX_WOOD_UP);
	}

	@Test
	void aSeedAlwaysGrowsTheSameTreeAndSeedsDiffer() {
		assertEquals(BloodwoodGrowthRules.grow(42L), BloodwoodGrowthRules.grow(42L));
		assertNotEquals(BloodwoodGrowthRules.grow(42L).logs(), BloodwoodGrowthRules.grow(43L).logs());
	}
}
