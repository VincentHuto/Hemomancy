package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteStructureConsumptionRulesTest {
	@Test
	void omittedFlagPreservesRequiredStructure() {
		assertFalse(CardinalRiteStructureConsumptionRules.fromNullable(null));
	}

	@Test
	void explicitFlagControlsSuccessfulConsumption() {
		assertTrue(CardinalRiteStructureConsumptionRules.fromNullable(Boolean.TRUE));
		assertFalse(CardinalRiteStructureConsumptionRules.fromNullable(Boolean.FALSE));
	}

	@Test
	void onlyCommittedSuccessMayConsumeStructure() {
		assertTrue(CardinalRiteStructureConsumptionRules.shouldConsume(true, true));
		assertFalse(CardinalRiteStructureConsumptionRules.shouldConsume(true, false));
		assertFalse(CardinalRiteStructureConsumptionRules.shouldConsume(false, true));
	}
}
