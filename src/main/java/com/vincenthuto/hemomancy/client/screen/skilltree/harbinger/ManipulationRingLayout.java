package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.util.List;

/** Compact radial anchoring rules for the tendency manipulation families. */
final class ManipulationRingLayout {
	private ManipulationRingLayout() {
	}

	static int anchorRadius(int innerNodeRadius, List<Double> localRadialOffsets) {
		double innermostOffset = localRadialOffsets.stream()
				.mapToDouble(Double::doubleValue)
				.min()
				.orElse(0.0);
		return Math.max(0, (int) Math.ceil(innerNodeRadius - innermostOffset));
	}
}
