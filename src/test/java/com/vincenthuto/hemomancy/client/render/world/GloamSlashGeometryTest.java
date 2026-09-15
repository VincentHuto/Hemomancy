package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

final class GloamSlashGeometryTest {
	@Test
	void gloamSlashUsesThreeSeparatedStrands() {
		assertArrayEquals(new double[] {-0.34D, 0.0D, 0.34D}, LuxUmbraGeometry.slashOffsets(), 0.0001D);
	}
}
