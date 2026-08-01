package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.tendency.EnumBloodTendency;

import java.util.List;

/** Renderer-neutral node description for cached tendency-colored tree traces. */
record TendencyTraceNode(String id, EnumBloodTendency tendency, int x, int y,
		List<String> parentIds, boolean known, boolean locked) {
	TendencyTraceNode {
		parentIds = List.copyOf(parentIds);
	}
}
