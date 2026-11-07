package com.vincenthuto.hemomancy.common.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MindBrokenRulesTest {
	@Test
	void aBrokenMindPaysMoreAndRegrowsLess() {
		assertEquals(1.15D, MindBrokenRules.manipulationCostMultiplier(true), 1.0E-9D);
		assertEquals(1.0D, MindBrokenRules.manipulationCostMultiplier(false), 1.0E-9D);
		assertEquals(0.85D, MindBrokenRules.passiveRegenMultiplier(true), 1.0E-9D);
		assertEquals(1.0D, MindBrokenRules.passiveRegenMultiplier(false), 1.0E-9D);
	}

	@Test
	void theVestmentsGoQuietWhileTheMindIsBroken() {
		assertTrue(MindBrokenRules.vestmentsAnswer(true, false));
		assertFalse(MindBrokenRules.vestmentsAnswer(true, true));
		assertFalse(MindBrokenRules.vestmentsAnswer(false, false));
	}
}
