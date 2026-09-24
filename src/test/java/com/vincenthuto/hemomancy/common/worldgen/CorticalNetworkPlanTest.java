package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.worldgen.CorticalNetworkPlan.Cable;
import com.vincenthuto.hemomancy.common.worldgen.CorticalNetworkPlan.Island;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CorticalNetworkPlanTest {
	private static List<Island> islands(int count, long seed) {
		java.util.Random random = new java.util.Random(seed);
		List<Island> islands = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			islands.add(new Island(random.nextInt(2000) - 1000, 60 + random.nextInt(40),
					random.nextInt(2000) - 1000));
		}
		return islands;
	}

	@Test
	void everyIslandIsReachable() {
		for (long seed = 0; seed < 20; seed++) {
			List<Island> islands = islands(12, seed);
			List<Cable> cables = CorticalNetworkPlan.connect(islands, seed);

			Map<Island, List<Island>> adjacency = new HashMap<>();
			for (Cable cable : cables) {
				adjacency.computeIfAbsent(cable.from(), key -> new ArrayList<>()).add(cable.to());
				adjacency.computeIfAbsent(cable.to(), key -> new ArrayList<>()).add(cable.from());
			}

			Set<Island> visited = new HashSet<>();
			Deque<Island> queue = new ArrayDeque<>();
			queue.add(islands.get(0));
			visited.add(islands.get(0));
			while (!queue.isEmpty()) {
				for (Island next : adjacency.getOrDefault(queue.poll(), List.of())) {
					if (visited.add(next)) {
						queue.add(next);
					}
				}
			}
			assertEquals(islands.size(), visited.size(),
					"seed " + seed + " left islands unconnected — the spec requires every island wired");
		}
	}

	@Test
	void hubIslandsCarryMultipleCables() {
		List<Cable> cables = CorticalNetworkPlan.connect(islands(12, 5L), 5L);
		Map<Island, Integer> degree = new HashMap<>();
		for (Cable cable : cables) {
			degree.merge(cable.from(), 1, Integer::sum);
			degree.merge(cable.to(), 1, Integer::sum);
		}
		assertTrue(degree.values().stream().anyMatch(count -> count >= 3),
				"at least one hub should fan out, matching the reference imagery");
	}

	@Test
	void planIsDeterministic() {
		assertEquals(CorticalNetworkPlan.connect(islands(10, 3L), 3L),
				CorticalNetworkPlan.connect(islands(10, 3L), 3L),
				"chunks are generated independently, so the plan must be reproducible");
	}

	@Test
	void cablesSagInTheMiddle() {
		Island a = new Island(0, 80, 0);
		Island b = new Island(100, 80, 0);
		Cable cable = new Cable(a, b, false);
		assertTrue(CorticalNetworkPlan.sag(cable, 0.5)[1] < 80.0, "the midpoint must hang below the endpoints");
		assertEquals(80.0, CorticalNetworkPlan.sag(cable, 0.0)[1], 0.001, "endpoints are anchored");
		assertEquals(80.0, CorticalNetworkPlan.sag(cable, 1.0)[1], 0.001, "endpoints are anchored");
	}

	@Test
	void singleIslandProducesNoCables() {
		assertTrue(CorticalNetworkPlan.connect(islands(1, 0L), 0L).isEmpty(),
				"a lone island cannot be wired to anything and must not crash");
	}
}
