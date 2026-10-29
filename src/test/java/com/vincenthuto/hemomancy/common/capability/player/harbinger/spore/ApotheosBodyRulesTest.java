package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApotheosBodyRulesTest {
	@Test
	void theApotheosRefusesFoodEverywhereAndRemediesOnlyInTheGardens() {
		assertEquals(17, ApotheosBodyRules.HUNGER_PIN);
		assertFalse(ApotheosBodyRules.refusesUse(true, true, false, false, true), "the tonic is never refused");
		assertFalse(ApotheosBodyRules.refusesUse(false, true, true, true, false), "others eat and drink as before");
		assertTrue(ApotheosBodyRules.refusesUse(true, false, true, false, false));
		assertFalse(ApotheosBodyRules.refusesUse(true, false, false, true, false));
		assertTrue(ApotheosBodyRules.refusesUse(true, true, false, true, false));
	}

	@Test
	void anApotheosPaysMorphlingUpkeepInBlood() {
		assertFalse(ApotheosBodyRules.symbioticSplit(true, true));
		assertTrue(ApotheosBodyRules.symbioticSplit(true, false));
		assertFalse(ApotheosBodyRules.symbioticSplit(false, false));
	}
}
