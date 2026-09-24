package com.vincenthuto.hemomancy.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Pure plan for a Cortical Drift archipelago: two broken rings of floating islands orbiting a
 * Vagrant Mind, plus a handful of mid-air ganglion hubs.
 *
 * <p>Minecraft-free (java.util / java.lang only) and deterministic: every method is a pure function
 * of its arguments, so independently generated chunks that recompute the same region agree exactly.
 */
public final class CorticalArchipelagoPlan {
	private static final int MIN_ISLANDS = 22;
	private static final int MAX_ISLANDS_EXCLUSIVE = 33;
	private static final int MAX_ATTEMPTS_PER_NODE = 12;
	private static final int MIN_HUBS = 3;
	private static final int MAX_HUBS_EXCLUSIVE = 6;
	private static final int HUB_RADIUS = 2;
	private static final int HUB_MIN_CLEARANCE = 12;
	private static final double SAG_RATIO = 0.12;
	private static final int ORBIT_Y = 118;
	private static final double INNER_MIN_RADIUS = 155.0;
	private static final double INNER_MAX_RADIUS = 195.0;
	private static final double OUTER_MIN_RADIUS = 240.0;
	private static final double OUTER_MAX_RADIUS = 280.0;

	public enum Kind {
		ISLAND, HUB
	}

	public record Node(Kind kind, int x, int y, int z, int radius, long shapeSeed) {}

	@FunctionalInterface
	public interface SpotFilter {
		boolean accept(int x, int z, int radius);
	}

	private CorticalArchipelagoPlan() {}

	public static List<Node> plan(long orbitSeed, int mindX, int mindZ, SpotFilter filter) {
		Random random = new Random(orbitSeed ^ (mindX * 341873128712L) ^ (mindZ * 132897987541L));

		List<Node> islands = new ArrayList<>();
		int targetIslands = MIN_ISLANDS + random.nextInt(MAX_ISLANDS_EXCLUSIVE - MIN_ISLANDS);
		int innerCount = targetIslands / 2;
		int outerCount = targetIslands - innerCount;
		double phase = random.nextDouble() * Math.PI * 2.0;
		double tiltX = 16.0 + random.nextDouble() * 12.0;
		double tiltZ = 10.0 + random.nextDouble() * 10.0;
		planRing(random, filter, islands, mindX, mindZ, innerCount, phase,
				INNER_MIN_RADIUS, INNER_MAX_RADIUS, tiltX, tiltZ);
		planRing(random, filter, islands, mindX, mindZ, outerCount,
				phase + Math.PI / Math.max(1, outerCount), OUTER_MIN_RADIUS, OUTER_MAX_RADIUS,
				tiltX, tiltZ);

		List<Node> nodes = new ArrayList<>(islands);
		int targetHubs = MIN_HUBS + random.nextInt(MAX_HUBS_EXCLUSIVE - MIN_HUBS);
		for (int i = 0; i < targetHubs; i++) {
			for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_NODE; attempt++) {
				double angle = phase + random.nextDouble() * Math.PI * 2.0;
				double distance = 205.0 + random.nextDouble() * 25.0;
				int x = mindX + (int) Math.round(Math.cos(angle) * distance);
				int z = mindZ + (int) Math.round(Math.sin(angle) * distance);
				int y = orbitalY(angle, tiltX, tiltZ) - 8 + random.nextInt(17);
				long shapeSeed = random.nextLong();

				if (!filter.accept(x, z, HUB_RADIUS)) {
					continue;
				}
				Node candidate = new Node(Kind.HUB, x, y, z, HUB_RADIUS, shapeSeed);
				if (tooCloseToAnyIsland(candidate, islands)) {
					continue;
				}
				nodes.add(candidate);
				break;
			}
		}
		return nodes;
	}

	private static void planRing(Random random, SpotFilter filter, List<Node> islands, int mindX,
			int mindZ, int count, double phase, double minDistance, double maxDistance,
			double tiltX, double tiltZ) {
		for (int i = 0; i < count; i++) {
			for (int attempt = 0; attempt < MAX_ATTEMPTS_PER_NODE; attempt++) {
				int radius = rollRadius(random);
				double slot = phase + (Math.PI * 2.0 * i / count);
				double angle = slot + (random.nextDouble() - 0.5) * (Math.PI * 0.55 / count);
				double distance = minDistance + random.nextDouble() * (maxDistance - minDistance);
				int x = mindX + (int) Math.round(Math.cos(angle) * distance);
				int z = mindZ + (int) Math.round(Math.sin(angle) * distance);
				int y = orbitalY(angle, tiltX, tiltZ) - 6 + random.nextInt(13);
				long shapeSeed = random.nextLong();

				if (!filter.accept(x, z, radius)) {
					continue;
				}
				Node candidate = new Node(Kind.ISLAND, x, y, z, radius, shapeSeed);
				if (overlapsAnyIsland(candidate, islands)) {
					continue;
				}
				islands.add(candidate);
				break;
			}
		}
	}

	private static int orbitalY(double angle, double tiltX, double tiltZ) {
		return (int) Math.round(ORBIT_Y + Math.sin(angle) * tiltX + Math.cos(angle) * tiltZ);
	}

	private static int rollRadius(Random random) {
		double roll = random.nextDouble();
		if (roll < 0.6) {
			return 5 + random.nextInt(10 - 5 + 1);
		} else if (roll < 0.9) {
			return 10 + random.nextInt(16 - 10 + 1);
		}
		return 16 + random.nextInt(24 - 16 + 1);
	}

	private static boolean overlapsAnyIsland(Node candidate, List<Node> islands) {
		int candidateDepth = CorticalIslandShape.maxDepth(candidate);
		double candidateBandTop = candidate.y() + 3;
		double candidateBandBottom = candidate.y() - candidateDepth;
		for (Node other : islands) {
			double horizontalDist = horizontalDistance(candidate, other);
			if (horizontalDist >= candidate.radius() + other.radius() + 10) {
				continue;
			}
			int otherDepth = CorticalIslandShape.maxDepth(other);
			double otherBandTop = other.y() + 3;
			double otherBandBottom = other.y() - otherDepth;
			boolean verticalOverlap = candidateBandBottom <= otherBandTop && otherBandBottom <= candidateBandTop;
			if (verticalOverlap) {
				return true;
			}
		}
		return false;
	}

	private static boolean tooCloseToAnyIsland(Node hub, List<Node> islands) {
		for (Node island : islands) {
			int depth = CorticalIslandShape.maxDepth(island);
			double bandTop = island.y() + 3;
			double bandBottom = island.y() - depth;
			double clampedY = Math.max(bandBottom, Math.min(hub.y(), bandTop));
			double dx = hub.x() - island.x();
			double dz = hub.z() - island.z();
			double horizontalDist = Math.sqrt(dx * dx + dz * dz);
			double horizontalGap = Math.max(0.0, horizontalDist - island.radius());
			double dy = hub.y() - clampedY;
			double distance = Math.sqrt(horizontalGap * horizontalGap + dy * dy);
			if (distance < HUB_MIN_CLEARANCE) {
				return true;
			}
		}
		return false;
	}

	private static double horizontalDistance(Node a, Node b) {
		double dx = a.x() - b.x();
		double dz = a.z() - b.z();
		return Math.sqrt(dx * dx + dz * dz);
	}

	/** Rim-to-rim endpoints for a cable between two nodes: {startXYZ, endXYZ}. */
	public static double[][] cableEndpoints(Node from, Node to) {
		return new double[][] { endpointFor(from, to), endpointFor(to, from) };
	}

	private static double[] endpointFor(Node node, Node other) {
		if (node.kind() == Kind.HUB) {
			return new double[] { node.x(), node.y(), node.z() };
		}
		double dx = other.x() - node.x();
		double dz = other.z() - node.z();
		double horizontalDist = Math.sqrt(dx * dx + dz * dz);
		// 0.7 stays inside the shape's minimum noisy edge (radius*0.75), so anchors always sit on land.
		double reach = node.radius() * 0.7;
		double ux;
		double uz;
		if (horizontalDist < 1.0e-6) {
			ux = 1.0;
			uz = 0.0;
		} else {
			ux = dx / horizontalDist;
			uz = dz / horizontalDist;
		}
		return new double[] { node.x() + ux * reach, node.y() - 1, node.z() + uz * reach };
	}

	/** Parabolic sag between two points, t in [0,1]; endpoints exact; dips span*0.12 at midpoint. */
	public static double[] sag(double[] from, double[] to, double t) {
		double dx = to[0] - from[0];
		double dy = to[1] - from[1];
		double dz = to[2] - from[2];
		double horizontalSpan = Math.sqrt(dx * dx + dz * dz);
		double droop = horizontalSpan * SAG_RATIO * (4.0 * t * (1.0 - t));
		return new double[] {
				from[0] + dx * t,
				from[1] + dy * t - droop,
				from[2] + dz * t };
	}
}
