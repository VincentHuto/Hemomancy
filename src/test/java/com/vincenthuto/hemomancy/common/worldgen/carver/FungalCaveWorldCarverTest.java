package com.vincenthuto.hemomancy.common.worldgen.carver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FungalCaveWorldCarverTest {
	@Test
	void dryCavesNeverOpenAFluidBoundary() {
		assertFalse(DryFungalCarving.isCandidate(true, false, true));
		assertFalse(DryFungalCarving.isCandidate(true, true, false));
		assertTrue(DryFungalCarving.isCandidate(true, false, false));
	}
}
