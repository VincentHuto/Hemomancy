package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BodyIdiomRulesTest {

	@Test
	void ironHeartsClampAndAbsorbBeforeHealth() {
		assertEquals(10.0F, BodyIdiomRules.addIronHeartHealth(9.0F, 10.0F));

		BodyIdiomRules.IronHeartAbsorption partial = BodyIdiomRules.absorbWithIronHearts(2.0F, 1.0F, 10.0F);
		assertEquals(1.0F, partial.ironHeartHealth(), 0.001F);
		assertEquals(0.0F, partial.remainingDamage(), 0.001F);

		BodyIdiomRules.IronHeartAbsorption depleted = BodyIdiomRules.absorbWithIronHearts(2.0F, 3.0F, 10.0F);
		assertEquals(0.0F, depleted.ironHeartHealth(), 0.001F);
		assertEquals(1.0F, depleted.remainingDamage(), 0.001F);
	}

	@Test
	void activeFerricScarBonusesExpandIronHeartCapacityByFullHearts() {
		assertEquals(10.0F, BodyIdiomRules.maxIronHeartHealth(0));
		assertEquals(14.0F, BodyIdiomRules.maxIronHeartHealth(2));
		assertEquals(18.0F, BodyIdiomRules.maxIronHeartHealth(4));
		assertEquals(22.0F, BodyIdiomRules.maxIronHeartHealth(6));
		assertEquals(26.0F, BodyIdiomRules.maxIronHeartHealth(8));
		assertEquals(34.0F, BodyIdiomRules.maxIronHeartHealth(12));
		assertEquals(17, BodyIdiomRules.ironHeartSlots(34.0F));
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
