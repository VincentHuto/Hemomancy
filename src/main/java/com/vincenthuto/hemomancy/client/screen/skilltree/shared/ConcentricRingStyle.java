package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

/** Shared visual treatment for the degree rings used by Harbinger progress maps. */
final class ConcentricRingStyle {
	static final int BASE_ALPHA = 0x18;

	private ConcentricRingStyle() {}

	static int segmentCount(int radius) {
		return Math.max(72, radius / 3);
	}

	static int withBaseAlpha(int color) {
		return (color & 0x00FFFFFF) | (BASE_ALPHA << 24);
	}
}
