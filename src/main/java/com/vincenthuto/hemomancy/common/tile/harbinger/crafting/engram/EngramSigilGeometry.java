package com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The engram's shape: an eight-pointed {8/3} star around the loom. Each of its eight lines joins tip k to tip k+3
 * and is crossed four times; the crossings form two rings of eight, the notches and the inner octagon. Lanes run
 * along the drawn lines: five segments of each star line, the outer ring arcs between neighbouring tips, and a spoke
 * from each inner node into the loom (the Vessel), entering the face of the same index.
 * <p>
 * Positions are in tip-radius units in the north-facing loom's frame: x to the east, z to the south, so tip 0 points
 * out of the loom's front (-z) and the tips run clockwise seen from above.
 */
public final class EngramSigilGeometry {
	public static final int NODES = 25;
	public static final int TIPS = 8;
	public static final int VESSEL = 24;
	public static final int FACES = 8;

	private static final double[][] UNIT = new double[NODES][];
	private static final int[][] STAR_LINES = new int[8][];
	private static final int[][] EDGES;
	private static final int[][] NEIGHBOURS = new int[NODES][];
	private static final int[][] EDGE_INDEX = new int[NODES][NODES];

	static {
		for (int k = 0; k < 8; k++) UNIT[k] = bearing(k * Math.PI / 4, 1.0);
		// The crossings of an {8/3} star, worked out rather than tabled so the rings are exact.
		double notchRadius = 0, innerRadius = 1;
		for (int other : new int[]{1, 2, 6, 7}) {
			double[] at = crossing(0, other);
			double r = Math.hypot(at[0], at[1]);
			notchRadius = Math.max(notchRadius, r);
			innerRadius = Math.min(innerRadius, r);
		}
		for (int k = 0; k < 8; k++) {
			UNIT[8 + k] = bearing((k + 0.5) * Math.PI / 4, notchRadius);
			UNIT[16 + k] = bearing(k * Math.PI / 4, innerRadius);
		}
		UNIT[VESSEL] = new double[]{0, 0};

		List<int[]> edges = new ArrayList<>();
		for (int k = 0; k < 8; k++) {
			int a = k, b = (k + 3) % 8;
			List<Integer> on = new ArrayList<>();
			for (int n = 8; n < 24; n++) if (onSegment(UNIT[a], UNIT[b], UNIT[n])) on.add(n);
			on.sort((x, y) -> Double.compare(along(UNIT[a], UNIT[b], UNIT[x]), along(UNIT[a], UNIT[b], UNIT[y])));
			int[] line = new int[on.size() + 2];
			line[0] = a;
			for (int i = 0; i < on.size(); i++) line[i + 1] = on.get(i);
			line[line.length - 1] = b;
			STAR_LINES[k] = line;
			for (int i = 0; i + 1 < line.length; i++) edges.add(new int[]{line[i], line[i + 1]});
		}
		for (int k = 0; k < 8; k++) edges.add(new int[]{k, (k + 1) % 8});
		for (int k = 0; k < 8; k++) edges.add(new int[]{16 + k, VESSEL});
		EDGES = edges.toArray(new int[0][]);

		for (int[] row : EDGE_INDEX) Arrays.fill(row, -1);
		List<List<Integer>> around = new ArrayList<>();
		for (int n = 0; n < NODES; n++) around.add(new ArrayList<>());
		for (int i = 0; i < EDGES.length; i++) {
			int a = EDGES[i][0], b = EDGES[i][1];
			EDGE_INDEX[a][b] = i;
			EDGE_INDEX[b][a] = i;
			around.get(a).add(b);
			around.get(b).add(a);
		}
		for (int n = 0; n < NODES; n++) NEIGHBOURS[n] = around.get(n).stream().mapToInt(Integer::intValue).toArray();
	}

	private EngramSigilGeometry() {
	}

	public static boolean isTip(int node) { return node >= 0 && node < 8; }
	public static boolean isNotch(int node) { return node >= 8 && node < 16; }
	public static boolean isInner(int node) { return node >= 16 && node < 24; }

	/** The node's position in tip-radius units, {x, z}. */
	public static double[] unit(int node) {
		return UNIT[node].clone();
	}

	public static int[] neighbours(int node) {
		return NEIGHBOURS[node].clone();
	}

	public static boolean adjacent(int a, int b) {
		return a >= 0 && b >= 0 && a < NODES && b < NODES && EDGE_INDEX[a][b] >= 0;
	}

	public static int edgeIndex(int a, int b) {
		return a < 0 || b < 0 || a >= NODES || b >= NODES ? -1 : EDGE_INDEX[a][b];
	}

	public static int[][] edges() {
		int[][] copy = new int[EDGES.length][];
		for (int i = 0; i < EDGES.length; i++) copy[i] = EDGES[i].clone();
		return copy;
	}

	/** Each star line's six nodes, tip to tip. */
	public static int[][] starLines() {
		int[][] copy = new int[STAR_LINES.length][];
		for (int i = 0; i < STAR_LINES.length; i++) copy[i] = STAR_LINES[i].clone();
		return copy;
	}

	/** The Vessel face an inner node's spoke enters. */
	public static int face(int innerNode) {
		return innerNode - 16;
	}

	public static int spokeInner(int face) {
		return 16 + Math.floorMod(face, FACES);
	}

	/**
	 * The nodes strictly between two nodes on one straight star line, nearest `from` first: what a sweep that skipped
	 * past them between two looks has crossed. Empty when they are adjacent; null when no star line holds both.
	 */
	public static int[] collinearRun(int from, int to) {
		if (from == to) return null;
		for (int[] line : STAR_LINES) {
			int i = indexOf(line, from), j = indexOf(line, to);
			if (i < 0 || j < 0) continue;
			int step = j > i ? 1 : -1;
			int[] run = new int[Math.abs(j - i) - 1];
			for (int k = 0; k < run.length; k++) run[k] = line[i + step * (k + 1)];
			return run;
		}
		return adjacent(from, to) ? new int[0] : null;
	}

	private static int indexOf(int[] line, int node) {
		for (int i = 0; i < line.length; i++) if (line[i] == node) return i;
		return -1;
	}

	/** A point `radius` out along a bearing measured clockwise from the front (-z). */
	private static double[] bearing(double angle, double radius) {
		return new double[]{Math.sin(angle) * radius, -Math.cos(angle) * radius};
	}

	private static double[] crossing(int lineA, int lineB) {
		double[] a = bearing(lineA * Math.PI / 4, 1), b = bearing(((lineA + 3) % 8) * Math.PI / 4, 1);
		double[] c = bearing(lineB * Math.PI / 4, 1), d = bearing(((lineB + 3) % 8) * Math.PI / 4, 1);
		double den = (a[0] - b[0]) * (c[1] - d[1]) - (a[1] - b[1]) * (c[0] - d[0]);
		double t = ((a[0] - c[0]) * (c[1] - d[1]) - (a[1] - c[1]) * (c[0] - d[0])) / den;
		return new double[]{a[0] + t * (b[0] - a[0]), a[1] + t * (b[1] - a[1])};
	}

	private static boolean onSegment(double[] a, double[] b, double[] p) {
		double cross = (b[0] - a[0]) * (p[1] - a[1]) - (b[1] - a[1]) * (p[0] - a[0]);
		double t = along(a, b, p);
		return Math.abs(cross) < 1e-6 && t > 1e-6 && t < 1 - 1e-6;
	}

	private static double along(double[] a, double[] b, double[] p) {
		double dx = b[0] - a[0], dz = b[1] - a[1];
		return ((p[0] - a[0]) * dx + (p[1] - a[1]) * dz) / (dx * dx + dz * dz);
	}
}
