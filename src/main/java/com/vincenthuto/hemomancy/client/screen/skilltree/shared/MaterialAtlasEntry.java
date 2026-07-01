package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import java.util.List;

public record MaterialAtlasEntry(
		MaterialAtlasPath path,
		String materialId,
		MaterialAtlasBucket bucket,
		MaterialGate gate,
		int order,
		List<String> parentIds,
		Integer nodeX,
		Integer nodeY
) {
	public MaterialAtlasEntry {
		parentIds = List.copyOf(parentIds);
	}

	public boolean hasExplicitPosition() {
		return nodeX != null && nodeY != null;
	}
}
