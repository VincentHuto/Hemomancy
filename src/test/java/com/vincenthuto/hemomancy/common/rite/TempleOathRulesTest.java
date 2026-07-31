package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

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
	void initiationMediumLeavesAtLeastTwoHealthAndCannotRepeat() {
		assertFalse(TempleOathRules.canBeginInitiation(5.99F, true, false, false));
		assertTrue(TempleOathRules.canBeginInitiation(6.0F, true, false, false));
		assertFalse(TempleOathRules.canBeginInitiation(20.0F, false, false, false));
		assertFalse(TempleOathRules.canBeginInitiation(20.0F, true, true, false));
		assertFalse(TempleOathRules.canBeginInitiation(20.0F, true, false, true));
	}

	@Test
	void claimingTheHeartUnlocksHermitGuidanceBeforeBloodActivation() {
		assertFalse(TempleOathRules.shouldShowInitiationGuidance(false, false));
		assertTrue(TempleOathRules.shouldShowInitiationGuidance(false, true));
		assertTrue(TempleOathRules.shouldShowInitiationGuidance(true, false));
	}
}
