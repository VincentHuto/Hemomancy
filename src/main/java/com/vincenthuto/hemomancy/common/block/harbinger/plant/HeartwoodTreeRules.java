package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The shape a Dead Bush grows into when blood is projected into it: a tall, bending, leafless Heartwood
 * tree with one core low in its trunk. The shape comes only from the seed, so a bush's position fixes its tree, and
 * every log joins the root by a face.
 */
public final class HeartwoodTreeRules {
	public static final double PROJECTION_BLOOD_COST = BloodwoodGrowthRules.PROJECTION_BLOOD_COST * 2;
	public static final int MIN_TRUNK_HEIGHT = 18;
	/** The trunk stays straight this far up, so the core stands at chest height in plain reach. */
	public static final int CORE_HEIGHT = 2;
	private static final int STRAIGHT_BASE = CORE_HEIGHT + 2;
	private static final int MIN_BRANCHES = 5;
	private static final double THICK_SHARE = 0.45D;

	private HeartwoodTreeRules() {
	}

	public enum Axis {
		X, Y, Z
	}

	/** One log: its offset from the bush and the axis its grain runs along. */
	public record Cell(int x, int y, int z, Axis axis) {
	}

	/** {@code logs} holds every log including {@code trunk}; {@code core} replaces the trunk log at its height. */
	public record Shape(List<Cell> logs, List<Cell> trunk, BloodwoodGrowthRules.Offset core, int branchCount) {
	}

	public static Shape grow(long seed) {
		Random random = new Random(seed);
		Map<List<Integer>, Cell> logs = new LinkedHashMap<>();
		List<Cell> trunk = growTrunk(random, logs);
		growRoots(random, logs);
		int branches = growBranches(random, trunk, logs);
		logs.remove(List.of(0, CORE_HEIGHT, 0));
		trunk.removeIf(cell -> cell.x() == 0 && cell.y() == CORE_HEIGHT && cell.z() == 0);
		return new Shape(List.copyOf(logs.values()), List.copyOf(trunk), new BloodwoodGrowthRules.Offset(0, CORE_HEIGHT, 0), branches);
	}

	private static List<Cell> growTrunk(Random random, Map<List<Integer>, Cell> logs) {
		int height = MIN_TRUNK_HEIGHT + random.nextInt(8);
		int thickTop = (int) (height * THICK_SHARE);
		double swayX = 3.0D + random.nextDouble() * 2.0D;
		double swayZ = 3.0D + random.nextDouble() * 2.0D;
		double waveX = (1.25D + random.nextDouble()) * Math.PI / height;
		double waveZ = (1.25D + random.nextDouble()) * Math.PI / height;
		double phaseX = random.nextBoolean() ? 0.0D : Math.PI;
		double phaseZ = random.nextBoolean() ? 0.0D : Math.PI;
		List<Cell> trunk = new ArrayList<>();
		int x = 0;
		int z = 0;
		for (int y = 0; y < height; y++) {
			trunk.add(place(logs, x, y, z, Axis.Y));
			// The bend grows out of a straight base, so the tree leans further the higher it climbs.
			double lean = Math.max(0, y - STRAIGHT_BASE) / (double) (height - STRAIGHT_BASE);
			int targetX = (int) Math.round(swayX * lean * Math.sin(y * waveX + phaseX + Math.PI / 2) + swayX * lean);
			int targetZ = (int) Math.round(swayZ * lean * Math.sin(y * waveZ + phaseZ));
			while (y > STRAIGHT_BASE && x != targetX) {
				x += Integer.signum(targetX - x);
				trunk.add(place(logs, x, y, z, Axis.X));
			}
			while (y > STRAIGHT_BASE && z != targetZ) {
				z += Integer.signum(targetZ - z);
				trunk.add(place(logs, x, y, z, Axis.Z));
			}
			if (y > thickTop) continue;
			// The low trunk is two blocks wide on the side away from the bush's column, which leaves the core's other sides open.
			place(logs, x + 1, y, z, Axis.Y);
			place(logs, x, y, z + 1, Axis.Y);
			place(logs, x + 1, y, z + 1, Axis.Y);
		}
		return trunk;
	}

	private static void growRoots(Random random, Map<List<Integer>, Cell> logs) {
		int[][] sides = { { 1, 0 }, { -1, 0 }, { 0, 1 }, { 0, -1 } };
		for (int[] side : sides) {
			int reach = 2 + random.nextInt(3);
			// Thick roots buttress the thick trunk, a low course over the longer one.
			for (int step = 1; step <= reach; step++) {
				int offX = side[0] * step + Math.max(side[0], 0);
				int offZ = side[1] * step + Math.max(side[1], 0);
				place(logs, offX, 0, offZ, side[0] != 0 ? Axis.X : Axis.Z);
				if (step < reach - 1) place(logs, offX, 1, offZ, Axis.Y);
			}
		}
	}

	private static int growBranches(Random random, List<Cell> trunk, Map<List<Integer>, Cell> logs) {
		int top = trunk.stream().mapToInt(Cell::y).max().orElseThrow();
		int lowest = Math.max(STRAIGHT_BASE + 2, (int) (top * 0.4D));
		List<Cell> upper = trunk.stream().filter(cell -> cell.y() >= lowest).toList();
		int count = MIN_BRANCHES + random.nextInt(4);
		double heading = random.nextDouble() * Math.PI * 2.0D;
		for (int i = 0; i < count; i++) {
			Cell start = upper.get(Math.min(upper.size() - 1, (i + 1) * upper.size() / count));
			heading += 2.399D + (random.nextDouble() - 0.5D) * 0.5D;
			int length = 5 + random.nextInt(5);
			double rise = 0.25D + random.nextDouble() * 0.45D;
			Cell end = walk(random, logs, start, heading, length, rise);
			if (random.nextDouble() < 0.7D) {
				// A twig forks from the branch's outer half and curls off to one side.
				walk(random, logs, end, heading + (random.nextBoolean() ? 1.0D : -1.0D), 2 + random.nextInt(3), rise + 0.2D);
			}
		}
		return count;
	}

	/** Walks a limb out from {@code start}, laying one axis-aligned step at a time so each log touches the last. */
	private static Cell walk(Random random, Map<List<Integer>, Cell> logs, Cell start, double heading, int length, double rise) {
		double px = start.x();
		double py = start.y();
		double pz = start.z();
		int x = start.x();
		int y = start.y();
		int z = start.z();
		double course = heading;
		for (int step = 0; step < length; step++) {
			course += (random.nextDouble() - 0.5D) * 0.7D;
			px += Math.cos(course);
			pz += Math.sin(course);
			py += rise;
			int nextX = (int) Math.round(px);
			int nextY = (int) Math.round(py);
			int nextZ = (int) Math.round(pz);
			while (x != nextX) {
				x += Integer.signum(nextX - x);
				place(logs, x, y, z, Axis.X);
			}
			while (z != nextZ) {
				z += Integer.signum(nextZ - z);
				place(logs, x, y, z, Axis.Z);
			}
			while (y != nextY) {
				y += Integer.signum(nextY - y);
				place(logs, x, y, z, Axis.Y);
			}
		}
		return new Cell(x, y, z, Axis.Y);
	}

	static Cell place(Map<List<Integer>, Cell> logs, int x, int y, int z, Axis axis) {
		return logs.computeIfAbsent(List.of(x, y, z), ignored -> new Cell(x, y, z, axis));
	}
}
