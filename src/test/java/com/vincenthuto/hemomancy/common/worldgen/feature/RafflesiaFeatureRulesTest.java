package com.vincenthuto.hemomancy.common.worldgen.feature;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RafflesiaFeatureRulesTest {

	@Test
	void onlyGroundedBottomLogsQualifyForAttachment() {
		assertTrue(RafflesiaPlacementRules.isGroundedBase(true, false, true));
		assertFalse(RafflesiaPlacementRules.isGroundedBase(true, true, true));
		assertFalse(RafflesiaPlacementRules.isGroundedBase(true, false, false));
		assertFalse(RafflesiaPlacementRules.isGroundedBase(false, false, true));
	}
}
