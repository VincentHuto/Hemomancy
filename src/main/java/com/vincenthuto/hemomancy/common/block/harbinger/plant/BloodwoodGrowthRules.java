package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * The shape any sapling grows into when blood is projected into it: a short trunk that forks low into limbs like
 * arteries, sweeping out and then up, each ending in its own clot of leaves with strands hanging from it like drips.
 * The shape comes only from the seed, so a sapling's position fixes its tree.
 */
public final class BloodwoodGrowthRules {
	public static final double PROJECTION_BLOOD_COST = 900.0D;
	/** How far the wood reaches from the sapling; the Alchemist's lesson quotes these. Leaves grow around what they meet. */
	public static final int MAX_WOOD_OUT = 8;
	public static final int MAX_WOOD_UP = 13;
	/** A strand stops here, so a player can walk under the tree without pushing through leaves. */
	public static final int LOWEST_LEAF = 3;
	private static final int ARC_STEPS = 8;

	private BloodwoodGrowthRules() {
	}

	/** {@code logs} joins the sapling's position by faces; {@code limbs} counts the main limbs from the fork. */
	public record Shape(List<HeartwoodTreeRules.Cell> logs, Set<Offset> leaves, int forkHeight, int limbs) {
	}

	public record Offset(int x, int y, int z) {
	}

	public static Shape grow(long seed) {
		Random random = new Random(seed);
		Map<List<Integer>, HeartwoodTreeRules.Cell> logs = new LinkedHashMap<>();
		int forkHeight = 3 + random.nextInt(3);
		for (int y = 0; y <= forkHeight; y++) {
			HeartwoodTreeRules.place(logs, 0, y, 0, HeartwoodTreeRules.Axis.Y);
		}
		growRootKnuckles(random, logs);

		List<int[]> crowns = new ArrayList<>();
		int limbs = 3 + random.nextInt(2);
		double heading = random.nextDouble() * Math.PI * 2.0D;
		for (int i = 0; i < limbs; i++) {
			double angle = heading + i * Math.PI * 2.0D / limbs + (random.nextDouble() - 0.5D) * 0.5D;
			double curl = (random.nextDouble() - 0.5D) * 0.8D;
			int reach = 6 + random.nextInt(2);
			int climb = 5 + random.nextInt(4);
			int[] middle = new int[3];
			int[] tip = arc(logs, new int[] { 0, forkHeight, 0 }, angle, curl, reach, climb, middle);
			crowns.add(new int[] { tip[0], tip[1], tip[2], 3 });
			if (random.nextDouble() < 0.75D) {
				double side = angle + (random.nextBoolean() ? 1.1D : -1.1D);
				int[] twig = arc(logs, middle, side, 0.0D, 2 + random.nextInt(2), 2 + random.nextInt(2), new int[3]);
				crowns.add(new int[] { twig[0], twig[1], twig[2], 2 });
			}
		}

		Set<Offset> leaves = new LinkedHashSet<>();
		for (int[] crown : crowns) growCrown(random, crown, leaves);
		leaves.removeIf(leaf -> logs.containsKey(List.of(leaf.x(), leaf.y(), leaf.z())));
		return new Shape(List.copyOf(logs.values()), Collections.unmodifiableSet(leaves), forkHeight, limbs);
	}

	/** Two or three short knuckles at the foot, so the trunk grips the ground instead of standing like a post. */
	private static void growRootKnuckles(Random random, Map<List<Integer>, HeartwoodTreeRules.Cell> logs) {
		int[][] sides = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		int first = random.nextInt(sides.length);
		int count = 2 + random.nextInt(2);
		for (int i = 0; i < count; i++) {
			int[] side = sides[(first + i) % sides.length];
			HeartwoodTreeRules.place(logs, side[0], 0, side[1], side[0] != 0 ? HeartwoodTreeRules.Axis.X : HeartwoodTreeRules.Axis.Z);
		}
	}

	/**
	 * Lays one limb from {@code start}: fast outward and barely rising at first, then bending up, as a vessel leaves a
	 * heart. {@code middle} receives the point halfway along, where a side limb can fork. Returns the tip.
	 */
	private static int[] arc(Map<List<Integer>, HeartwoodTreeRules.Cell> logs, int[] start, double angle, double curl, int reach, int climb, int[] middle) {
		int x = start[0];
		int y = start[1];
		int z = start[2];
		for (int step = 1; step <= ARC_STEPS; step++) {
			double t = step / (double) ARC_STEPS;
			double radial = reach * (1.0D - (1.0D - t) * (1.0D - t));
			double course = angle + curl * t;
			int nextX = start[0] + (int) Math.round(Math.cos(course) * radial);
			int nextZ = start[2] + (int) Math.round(Math.sin(course) * radial);
			int nextY = start[1] + (int) Math.round(climb * t * t);
			while (x != nextX) {
				x += Integer.signum(nextX - x);
				HeartwoodTreeRules.place(logs, x, y, z, HeartwoodTreeRules.Axis.X);
			}
			while (z != nextZ) {
				z += Integer.signum(nextZ - z);
				HeartwoodTreeRules.place(logs, x, y, z, HeartwoodTreeRules.Axis.Z);
			}
			while (y != nextY) {
				y += Integer.signum(nextY - y);
				HeartwoodTreeRules.place(logs, x, y, z, HeartwoodTreeRules.Axis.Y);
			}
			if (step == ARC_STEPS / 2) {
				middle[0] = x;
				middle[1] = y;
				middle[2] = z;
			}
		}
		return new int[] { x, y, z };
	}

	/** A lumpy clot around a limb's tip, and thin strands hanging from its underside. */
	private static void growCrown(Random random, int[] crown, Set<Offset> leaves) {
		int radius = crown[3];
		double tall = radius * 0.8D;
		Map<List<Integer>, Integer> undersides = new LinkedHashMap<>();
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				for (int dy = -radius; dy <= radius; dy++) {
					double shape = (dx * dx + dz * dz) / (double) (radius * radius) + dy * dy / (tall * tall);
					if (shape > 1.0D + (random.nextDouble() - 0.6D) * 0.5D) continue;
					int y = crown[1] + dy;
					leaves.add(new Offset(crown[0] + dx, y, crown[2] + dz));
					undersides.merge(List.of(crown[0] + dx, crown[2] + dz), y, Math::min);
				}
			}
		}
		for (Map.Entry<List<Integer>, Integer> column : undersides.entrySet()) {
			// Sparse enough that the limbs still show through; a dense fringe reads as a solid curtain.
			if (random.nextDouble() > 0.25D) continue;
			int length = 2 + random.nextInt(radius + 1);
			for (int drop = 1; drop <= length; drop++) {
				int y = column.getValue() - drop;
				if (y < LOWEST_LEAF) break;
				leaves.add(new Offset(column.getKey().get(0), y, column.getKey().get(1)));
			}
		}
	}
}
