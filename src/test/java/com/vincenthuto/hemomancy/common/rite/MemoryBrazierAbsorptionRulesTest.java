package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MemoryBrazierAbsorptionRulesTest {
	@Test
	void absorptionCanLearnAStandardMemoryOnlyWhileItBurns() {
		assertTrue(MemoryBrazierAbsorptionRules.shouldAttempt(true, true, true, 1.0D));
		assertFalse(MemoryBrazierAbsorptionRules.shouldAttempt(false, true, true, 1.0D));
		assertFalse(MemoryBrazierAbsorptionRules.shouldAttempt(true, false, true, 1.0D));
	}

	@Test
	void learningRequiresAnActiveAbsorptionChannelAndPositiveTransfer() {
		assertFalse(MemoryBrazierAbsorptionRules.shouldAttempt(true, true, false, 1.0D));
		assertFalse(MemoryBrazierAbsorptionRules.shouldAttempt(true, true, true, 0.0D));
	}
}
