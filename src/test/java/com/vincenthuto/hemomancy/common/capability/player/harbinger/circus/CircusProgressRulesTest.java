package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CircusProgressRulesTest {
	@Test
	void acclimationStagesUseTheAuthoredThresholds() {
		assertEquals(CircusProgressRules.Stage.UNAWARE, CircusProgressRules.stage(149));
		assertEquals(CircusProgressRules.Stage.DISTURBED, CircusProgressRules.stage(150));
		assertEquals(CircusProgressRules.Stage.ACCLIMATING, CircusProgressRules.stage(500));
		assertEquals(CircusProgressRules.Stage.ATTUNED, CircusProgressRules.stage(1000));
	}

	@Test
	void passiveExposureAwardsOnePointEveryFourSeconds() {
		assertEquals(0, CircusProgressRules.passivePoints(79));
		assertEquals(1, CircusProgressRules.passivePoints(80));
		assertEquals(300, CircusProgressRules.passivePoints(24_000));
	}

	@Test
	void onlyAcclimatingPlayersCanReceiveThePactAudience() {
		assertFalse(CircusProgressRules.canReceivePact(499));
		assertTrue(CircusProgressRules.canReceivePact(500));
	}

	@Test
	void presentationStartsAfterUnawareAndSettlesTowardAttunement() {
		assertFalse(CircusProgressRules.Stage.UNAWARE.hasPresentation());
		assertEquals(0, CircusProgressRules.Stage.UNAWARE.particleIntervalTicks());
		assertTrue(CircusProgressRules.Stage.DISTURBED.motionEchoAlpha() > 0);
		assertEquals(0, CircusProgressRules.Stage.ACCLIMATING.motionEchoAlpha());
		assertEquals(0, CircusProgressRules.Stage.ATTUNED.motionEchoAlpha());
		assertTrue(CircusProgressRules.Stage.ATTUNED.hasPresentation());
	}
}
