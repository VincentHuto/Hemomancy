package com.vincenthuto.hemomancy.client.render.world;

/** Shared geometry for the low, diagonally-pruned Qliphoth stump. */
final class QliphothSeveredGeometry {
	static final int FACETS = 10;
	static final float CUT_RADIUS = 0.50f;

	private static final float CUT_CENTER_HEIGHT = 2.05f;
	private static final float CUT_X_SLOPE = -1.15f;
	private static final float CUT_Z_SLOPE = 0.18f;

	private QliphothSeveredGeometry() {
	}

	static float cutHeight(float x, float z) {
		return CUT_CENTER_HEIGHT + x * CUT_X_SLOPE + z * CUT_Z_SLOPE;
	}

	static RimPoint rimPoint(int facet) {
		float angle = (float) (facet * Math.PI * 2.0 / FACETS);
		float x = (float) Math.cos(angle) * CUT_RADIUS;
		float z = (float) Math.sin(angle) * CUT_RADIUS;
		float u = x / (CUT_RADIUS * 2.0f) + 0.5f;
		float v = z / (CUT_RADIUS * 2.0f) + 0.5f;
		return new RimPoint(x, cutHeight(x, z), z, u, v);
	}

	record RimPoint(float x, float y, float z, float u, float v) {
	}
}
