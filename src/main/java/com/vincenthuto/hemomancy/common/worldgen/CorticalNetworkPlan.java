package com.vincenthuto.hemomancy.common.worldgen;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Region-seeded plan for the fiber bridges between Cortical Drift islands. Deterministic from the
 * island list plus the region seed so independently generated chunks agree on every cable, the
 * same contract PhlegethonticVeinPath relies on.
 */
public final class CorticalNetworkPlan {
	private static final int EXTRA_EDGES = 3;
	private static final double SAG_RATIO = 0.12;

	public record Island(int x, int y, int z) {}

	public record Cable(Island from, Island to, boolean bundle) {}

	private CorticalNetworkPlan() {}

	public static List<Cable> connect(List<Island> islands, long seed) {
		List<Cable> cables = new ArrayList<>();
		if (islands.size() < 2) {
			return cables;
		}
		Random random = new Random(seed ^ 0x4E37D2L);

		// Prim's algorithm: a minimum spanning tree guarantees every island is reachable.
		// `connected` is scanned in fixed index order (0..n-1) rather than iterated as a Set, so
		// ties on distance always resolve to the same edge regardless of hashing/insertion order.
		int islandCount = islands.size();
		boolean[] connected = new boolean[islandCount];
		connected[0] = true;
		int connectedCount = 1;
		while (connectedCount < islandCount) {
			int bestFrom = -1;
			int bestTo = -1;
			double bestDistance = Double.MAX_VALUE;
			for (int from = 0; from < islandCount; from++) {
				if (!connected[from]) {
					continue;
				}
				for (int to = 0; to < islandCount; to++) {
					if (connected[to]) {
						continue;
					}
					double distance = distanceSquared(islands.get(from), islands.get(to));
					if (distance < bestDistance) {
						bestDistance = distance;
						bestFrom = from;
						bestTo = to;
					}
				}
			}
			connected[bestTo] = true;
			connectedCount++;
			cables.add(new Cable(islands.get(bestFrom), islands.get(bestTo), random.nextInt(3) == 0));
		}

		// A few extra edges turn well-placed islands into the multi-bundle hubs in the references.
		for (int i = 0; i < EXTRA_EDGES && islandCount > 2; i++) {
			int from = random.nextInt(islandCount);
			int to = random.nextInt(islandCount);
			if (from != to) {
				cables.add(new Cable(islands.get(from), islands.get(to), random.nextInt(2) == 0));
			}
		}
		return cables;
	}

	/** Position along a cable at {@code t} in [0,1], hanging in a parabolic catenary approximation. */
	public static double[] sag(Cable cable, double t) {
		Island from = cable.from();
		Island to = cable.to();
		double dx = to.x() - from.x();
		double dz = to.z() - from.z();
		double span = Math.sqrt(dx * dx + dz * dz);
		double droop = span * SAG_RATIO * (4.0 * t * (1.0 - t));
		return new double[] {
				from.x() + dx * t,
				from.y() + (to.y() - from.y()) * t - droop,
				from.z() + dz * t };
	}

	private static double distanceSquared(Island a, Island b) {
		double dx = a.x() - b.x();
		double dy = a.y() - b.y();
		double dz = a.z() - b.z();
		return dx * dx + dy * dy + dz * dz;
	}
}
