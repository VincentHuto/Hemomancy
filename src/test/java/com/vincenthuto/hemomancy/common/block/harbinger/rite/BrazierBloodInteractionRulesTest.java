package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BrazierBloodInteractionRulesTest {
	@Test
	void absorptionExtinguishesOnlyAnEmptyLitBrazier() {
		assertTrue(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(true, true, 1.0D));
		assertFalse(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(false, true, 1.0D));
		assertFalse(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(true, false, 1.0D));
		assertFalse(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(true, true, 0.0D));
	}

	@Test
	void absorptionParticlesRequireALitBrazierRegardlessOfOfferingState() {
		assertFalse(BrazierBloodInteractionRules.shouldDrawAbsorptionParticles(false));
		assertTrue(BrazierBloodInteractionRules.shouldDrawAbsorptionParticles(true));
	}
}
