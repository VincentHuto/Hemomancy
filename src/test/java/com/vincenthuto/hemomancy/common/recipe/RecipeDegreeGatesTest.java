package com.vincenthuto.hemomancy.common.recipe;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class RecipeDegreeGatesTest {
	@Test
	void rankRiteParticipatesOnlyUntilItsTargetDegreeIsReached() {
		assertTrue(RecipeDegreeGates.rankupWindowOpen(2, 3));
		assertFalse(RecipeDegreeGates.rankupWindowOpen(3, 3));
		assertFalse(RecipeDegreeGates.rankupWindowOpen(4, 3));
	}
}
