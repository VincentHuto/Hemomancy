package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteOfferingConsumptionRulesTest {
	@Test
	void omittedConsumptionSettingSpendsTheOffering() {
		assertTrue(CardinalRiteOfferingConsumptionRules.fromNullable(null));
	}

	@Test
	void explicitConsumptionSettingRemainsAnOptOut() {
		assertTrue(CardinalRiteOfferingConsumptionRules.fromNullable(true));
		assertFalse(CardinalRiteOfferingConsumptionRules.fromNullable(false));
	}
}
