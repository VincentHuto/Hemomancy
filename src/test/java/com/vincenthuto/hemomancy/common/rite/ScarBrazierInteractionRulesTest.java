package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScarBrazierInteractionRulesTest {
	@Test
	void selectsScarBurnOnlyForDeliberateUseOnEmptyLitBrazier() {
		assertEquals(ScarBrazierInteractionRules.Burn.LEARN,
				ScarBrazierInteractionRules.select(true, true, true, true, false, false));
		assertEquals(ScarBrazierInteractionRules.Burn.COMMIT,
				ScarBrazierInteractionRules.select(true, true, true, false, true, false));
		assertEquals(ScarBrazierInteractionRules.Burn.CLEAR,
				ScarBrazierInteractionRules.select(true, true, true, false, false, true));
	}

	@Test
	void preservesOrdinaryOfferingInteraction() {
		assertEquals(ScarBrazierInteractionRules.Burn.NONE,
				ScarBrazierInteractionRules.select(true, true, false, true, false, false));
		assertEquals(ScarBrazierInteractionRules.Burn.NONE,
				ScarBrazierInteractionRules.select(false, true, true, true, false, false));
		assertEquals(ScarBrazierInteractionRules.Burn.NONE,
				ScarBrazierInteractionRules.select(true, false, true, true, false, false));
		assertEquals(ScarBrazierInteractionRules.Burn.NONE,
				ScarBrazierInteractionRules.select(true, true, true, false, false, false));
	}

	@Test
	void exposesExistingDegreeBasedScarCapacity() {
		assertEquals(0, ScarBrazierInteractionRules.maxActiveScars(3));
		assertEquals(1, ScarBrazierInteractionRules.maxActiveScars(4));
		assertEquals(2, ScarBrazierInteractionRules.maxActiveScars(5));
		assertEquals(4, ScarBrazierInteractionRules.maxActiveScars(6));
	}
}
