package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScarBrazierInteractionRulesTest {
	@Test
	void selectsBurningScarOfferingsWithoutASneakInteraction() {
		assertEquals(ScarBrazierInteractionRules.Burn.LEARN,
				ScarBrazierInteractionRules.selectOffering(true, true, false, false));
		assertEquals(ScarBrazierInteractionRules.Burn.COMMIT,
				ScarBrazierInteractionRules.selectOffering(true, false, true, false));
		assertEquals(ScarBrazierInteractionRules.Burn.CLEAR,
				ScarBrazierInteractionRules.selectOffering(true, false, false, true));
	}

	@Test
	void absorptionRequiresABurningActionableScarOffering() {
		assertEquals(ScarBrazierInteractionRules.Burn.NONE,
				ScarBrazierInteractionRules.selectOffering(false, true, false, false));
		assertEquals(ScarBrazierInteractionRules.Burn.NONE,
				ScarBrazierInteractionRules.selectOffering(true, false, false, false));
		assertEquals(true, ScarBrazierInteractionRules.canAbsorb(true, 1.0D));
		assertEquals(false, ScarBrazierInteractionRules.canAbsorb(false, 1.0D));
		assertEquals(false, ScarBrazierInteractionRules.canAbsorb(true, 0.0D));
	}

	@Test
	void exposesExistingDegreeBasedScarCapacity() {
		assertEquals(0, ScarBrazierInteractionRules.maxActiveScars(3));
		assertEquals(1, ScarBrazierInteractionRules.maxActiveScars(4));
		assertEquals(2, ScarBrazierInteractionRules.maxActiveScars(5));
		assertEquals(4, ScarBrazierInteractionRules.maxActiveScars(6));
	}
}
