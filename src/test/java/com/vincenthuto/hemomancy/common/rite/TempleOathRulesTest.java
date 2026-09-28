package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TempleOathRulesTest {
	@Test
	void heartClaimRequiresTheBlessingOfThatTemplesHermit() {
		UUID linkedHermit = UUID.randomUUID();
		assertFalse(TempleOathRules.canClaimHeart(linkedHermit, null, false));
		assertFalse(TempleOathRules.canClaimHeart(linkedHermit, UUID.randomUUID(), false));
		assertTrue(TempleOathRules.canClaimHeart(linkedHermit, linkedHermit, false));
		assertFalse(TempleOathRules.canClaimHeart(linkedHermit, linkedHermit, true));
	}

	@Test
	void claimingTheHeartUnlocksHermitGuidanceBeforeBloodActivation() {
		assertFalse(TempleOathRules.shouldShowInitiationGuidance(false, false));
		assertTrue(TempleOathRules.shouldShowInitiationGuidance(false, true));
		assertTrue(TempleOathRules.shouldShowInitiationGuidance(true, false));
	}
}
