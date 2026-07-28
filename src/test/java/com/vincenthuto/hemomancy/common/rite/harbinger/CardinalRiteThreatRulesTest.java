package com.vincenthuto.hemomancy.common.rite.harbinger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CardinalRiteThreatRulesTest {
	@Test
	void riteSpawnedThreatsIgnorePassiveRitualAreaDamage() {
		assertTrue(CardinalRiteThreatRules.isProtectedFromPassiveRiteDamage(true));
		assertFalse(CardinalRiteThreatRules.isProtectedFromPassiveRiteDamage(false));
	}

	@Test
	void bloodlickersSurviveMoreThanOneBastionPulse() {
		assertTrue(CardinalRiteThreatRules.BLOODLICKER_MAX_HEALTH >= 10.0D);
		assertTrue(CardinalRiteThreatRules.BLOODLICKER_MAX_HEALTH
				> CardinalRiteThreatRules.BASTION_DAMAGE_PER_PULSE);
	}
}
