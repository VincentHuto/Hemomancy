package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.function.Predicate;

record MaterialRenderSnapshot(List<MaterialAtlasNode> nodes, Map<MaterialAtlasNode, int[]> positions) {
	MaterialRenderSnapshot {
		nodes = List.copyOf(nodes);
		positions = Collections.unmodifiableMap(new LinkedHashMap<>(positions));
	}

	static MaterialRenderSnapshot empty() {
		return new MaterialRenderSnapshot(List.of(), Map.of());
	}

	static MaterialRenderSnapshot filter(List<MaterialAtlasNode> nodes,
			Map<MaterialAtlasNode, int[]> positions, Predicate<String> includesFamily) {
		List<MaterialAtlasNode> filteredNodes = nodes.stream()
				.filter(node -> includesFamily.test(node.atlasEntry().bucket().id()))
				.toList();
		LinkedHashMap<MaterialAtlasNode, int[]> filteredPositions = new LinkedHashMap<>();
		for (var entry : positions.entrySet()) {
			if (includesFamily.test(entry.getKey().atlasEntry().bucket().id())) {
				filteredPositions.put(entry.getKey(), entry.getValue());
			}
		}
		return new MaterialRenderSnapshot(filteredNodes, filteredPositions);
	}
}
