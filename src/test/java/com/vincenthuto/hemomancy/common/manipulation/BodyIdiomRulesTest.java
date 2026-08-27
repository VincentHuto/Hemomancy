package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BodyIdiomRulesTest {

	@Test
	void ironHeartsClampAndAbsorbBeforeHealth() {
		assertEquals(BodyIdiomRules.MAX_IRON_HEART_HEALTH,
				BodyIdiomRules.addIronHeartHealth(9.0F));

		BodyIdiomRules.IronHeartAbsorption partial = BodyIdiomRules.absorbWithIronHearts(2.0F, 1.0F);
		assertEquals(1.0F, partial.ironHeartHealth(), 0.001F);
		assertEquals(0.0F, partial.remainingDamage(), 0.001F);

		BodyIdiomRules.IronHeartAbsorption depleted = BodyIdiomRules.absorbWithIronHearts(2.0F, 3.0F);
		assertEquals(0.0F, depleted.ironHeartHealth(), 0.001F);
		assertEquals(1.0F, depleted.remainingDamage(), 0.001F);
	}

	@Test
	void blackheartedConvertsWitherIntoHealingAndSaturation() {
		BodyIdiomRules.BlackheartedResult result = BodyIdiomRules.metabolizeWither(4.0F, 0.0F, false);

		assertEquals(1.4F, result.remainingDamage(), 0.001F);
		assertEquals(1.3F, result.healing(), 0.001F);
		assertEquals(2.6F, result.saturation(), 0.001F);
		assertFalse(result.ruptured());
	}

	@Test
	void blackheartedRupturesAtCapacityWithoutPreventingBeyondIt() {
		BodyIdiomRules.BlackheartedResult result = BodyIdiomRules.metabolizeWither(4.0F, 11.0F, false);

		assertEquals(3.0F, result.remainingDamage(), 0.001F);
		assertEquals(0.5F, result.healing(), 0.001F);
		assertEquals(0.0F, result.saturation(), 0.001F);
		assertTrue(result.ruptured());
	}

	@Test
	void blackheartedDoesNothingDuringRefractoryCooldown() {
		BodyIdiomRules.BlackheartedResult result = BodyIdiomRules.metabolizeWither(4.0F, 5.0F, true);

		assertEquals(4.0F, result.remainingDamage(), 0.001F);
		assertEquals(0.0F, result.healing(), 0.001F);
		assertEquals(5.0F, result.saturation(), 0.001F);
		assertFalse(result.ruptured());
	}
}
