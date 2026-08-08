package com.vincenthuto.hemomancy.common.block.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MultiBlockBreakRulesTest {
	@Test
	void creativePlayersMayBreakQliphothFillersThroughTheirMainBlock() {
		assertFalse(MultiBlockBreakRules.shouldDestroyMainFromPlayer(true, false));
		assertTrue(MultiBlockBreakRules.shouldDestroyMainFromPlayer(true, true));
		assertTrue(MultiBlockBreakRules.shouldDestroyMainFromPlayer(false, false));
	}
}
