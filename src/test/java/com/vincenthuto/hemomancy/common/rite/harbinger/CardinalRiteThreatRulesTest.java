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

	@Test
	void onlyExsanguinationFeedsOnBystanders() {
		assertFalse(CardinalRiteThreatRules.allowsPassiveSacrifice(
				"cardinal_rite/sanguine_initiation"));
		assertTrue(CardinalRiteThreatRules.allowsPassiveSacrifice(
				"cardinal_rite/exsanguination"));
		assertFalse(CardinalRiteThreatRules.allowsPassiveSacrifice(
				"cardinal_rite/vascular_mending"));
		assertFalse(CardinalRiteThreatRules.allowsPassiveSacrifice(
				"cardinal_rite/archon_rite"));
	}

	@Test
	void protectedRelationshipsAndBossesCannotBeSacrificed() {
		assertTrue(CardinalRiteThreatRules.isEligiblePassiveSacrifice(
				false, false, false, false, false));
		assertFalse(CardinalRiteThreatRules.isEligiblePassiveSacrifice(
				true, false, false, false, false));
		assertFalse(CardinalRiteThreatRules.isEligiblePassiveSacrifice(
				false, true, false, false, false));
		assertFalse(CardinalRiteThreatRules.isEligiblePassiveSacrifice(
				false, false, true, false, false));
		assertFalse(CardinalRiteThreatRules.isEligiblePassiveSacrifice(
				false, false, false, true, false));
		assertFalse(CardinalRiteThreatRules.isEligiblePassiveSacrifice(
				false, false, false, false, true));
	}
}
