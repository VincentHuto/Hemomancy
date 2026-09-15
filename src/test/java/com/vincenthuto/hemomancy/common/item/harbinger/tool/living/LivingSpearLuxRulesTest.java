package com.vincenthuto.hemomancy.common.item.harbinger.tool.living;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingSpearLuxRulesTest {
	@Test
	void qualifyingDamageChargesOnceAndCapsAtThirty() {
		assertFalse(LivingSpearLuxRules.qualifies(9, false));
		assertTrue(LivingSpearLuxRules.qualifies(10, false));
		assertTrue(LivingSpearLuxRules.qualifies(0, true));
		assertEquals(12.0F, LivingSpearLuxRules.addCharge(5.0F, 7.0F, true), 0.0001F);
		assertEquals(5.0F, LivingSpearLuxRules.addCharge(5.0F, 7.0F, false), 0.0001F);
		assertEquals(30.0F, LivingSpearLuxRules.addCharge(28.0F, 7.0F, true), 0.0001F);
	}

	@Test
	void fullChargeRequiresASuccessfulFullyCooledStrike() {
		assertFalse(LivingSpearLuxRules.shouldBurst(29.99F, 1.0F, true));
		assertFalse(LivingSpearLuxRules.shouldBurst(30.0F, 0.99F, true));
		assertFalse(LivingSpearLuxRules.shouldBurst(30.0F, 1.0F, false));
		assertTrue(LivingSpearLuxRules.shouldBurst(30.0F, 1.0F, true));
	}

	@Test
	void glintIntensityTracksStoredDamage() {
		assertEquals(0.0F, LivingSpearLuxRules.chargeFraction(0.0F), 0.0001F);
		assertEquals(0.5F, LivingSpearLuxRules.chargeFraction(15.0F), 0.0001F);
		assertEquals(1.0F, LivingSpearLuxRules.chargeFraction(40.0F), 0.0001F);
	}
}
