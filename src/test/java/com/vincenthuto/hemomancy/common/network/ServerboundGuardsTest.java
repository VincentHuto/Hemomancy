package com.vincenthuto.hemomancy.common.network;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The pure guards. The world-dependent ones are covered by {@code ServerboundGuardsGameTests}. */
class ServerboundGuardsTest {
	@Test
	void finiteRejectsNanAndInfinity() {
		assertTrue(ServerboundGuards.finite());
		assertTrue(ServerboundGuards.finite(0.0D, -1.5D, 1.0E300D));
		assertFalse(ServerboundGuards.finite(Double.NaN));
		assertFalse(ServerboundGuards.finite(1.0D, Double.POSITIVE_INFINITY));
		assertFalse(ServerboundGuards.finite(Double.NEGATIVE_INFINITY, 1.0D));
		assertFalse(ServerboundGuards.finite(Float.NaN));
	}
}
