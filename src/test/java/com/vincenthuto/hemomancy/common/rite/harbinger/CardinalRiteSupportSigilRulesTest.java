package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteSupportSigilRulesTest {
	@Test
	void eitherMemoryOrSightCanExposeFalseOmens() {
		assertFalse(CardinalRiteSupportSigilRules.revealsFalseOmens(false, false));
		assertTrue(CardinalRiteSupportSigilRules.revealsFalseOmens(true, false));
		assertTrue(CardinalRiteSupportSigilRules.revealsFalseOmens(false, true));
	}

	@Test
	void aCompletedCageBindsThreats() {
		assertFalse(CardinalRiteSupportSigilRules.bindsThreats(false));
		assertTrue(CardinalRiteSupportSigilRules.bindsThreats(true));
	}
}
