package com.vincenthuto.hemomancy.common.manipulation.mortem;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BlackheartedRulesTest {

	@Test
	void blackheartedConvertsWitherIntoHealingAndSaturation() {
		BlackheartedRules.BlackheartedResult result = BlackheartedRules.metabolizeWither(4.0F, 0.0F, false);

		assertEquals(1.4F, result.remainingDamage(), 0.001F);
		assertEquals(1.3F, result.healing(), 0.001F);
		assertEquals(2.6F, result.saturation(), 0.001F);
		assertFalse(result.ruptured());
	}

	@Test
	void blackheartedRupturesAtCapacityWithoutPreventingBeyondIt() {
		BlackheartedRules.BlackheartedResult result = BlackheartedRules.metabolizeWither(4.0F, 11.0F, false);

		assertEquals(3.0F, result.remainingDamage(), 0.001F);
		assertEquals(0.5F, result.healing(), 0.001F);
		assertEquals(0.0F, result.saturation(), 0.001F);
		assertTrue(result.ruptured());
	}

	@Test
	void blackheartedDoesNothingDuringRefractoryCooldown() {
		BlackheartedRules.BlackheartedResult result = BlackheartedRules.metabolizeWither(4.0F, 5.0F, true);

		assertEquals(4.0F, result.remainingDamage(), 0.001F);
		assertEquals(0.0F, result.healing(), 0.001F);
		assertEquals(5.0F, result.saturation(), 0.001F);
		assertFalse(result.ruptured());
	}
}
