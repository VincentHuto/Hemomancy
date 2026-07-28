package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

public final class SanguineFormationProjectionRendererTest {
	@Test
	void organicUndulationEmergesWithTheNodeInsteadOfPrecedingItsGrowth() {
		assertFloatEquals(0.0F,
				SanguineFormationProjectionRenderer.scaledUndulation(0.0F, 0.03F),
				"zero-radius node");
		assertFloatEquals(0.015F,
				SanguineFormationProjectionRenderer.scaledUndulation(0.02F, 0.03F),
				"half-emerged node");
		assertFloatEquals(0.03F,
				SanguineFormationProjectionRenderer.scaledUndulation(0.04F, 0.03F),
				"fully undulating node");
	}

	private static void assertFloatEquals(float expected, float actual, String label) {
		if (Math.abs(expected - actual) > 0.0001F) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
