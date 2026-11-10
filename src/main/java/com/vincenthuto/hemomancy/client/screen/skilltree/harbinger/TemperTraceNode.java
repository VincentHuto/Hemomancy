package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;

import java.util.List;

/** Renderer-neutral node description for cached temper-colored tree traces. */
record TemperTraceNode(String id, EnumBloodTemper temper, int x, int y,
		List<String> parentIds, boolean known, boolean locked) {
	TemperTraceNode {
		parentIds = List.copyOf(parentIds);
	}
}
