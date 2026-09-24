package com.vincenthuto.hemomancy.common.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.vincenthuto.hemomancy.common.entity.mob.arthropod.MyelinBorerMovementRules;
import org.junit.jupiter.api.Test;

class MyelinBorerMovementRulesTest {
	@Test
	void agitationDecaysToCalm() {
		int agitation = MyelinBorerMovementRules.AGITATION_TICKS;
		assertTrue(MyelinBorerMovementRules.isAgitated(agitation));
		for (int i = 0; i < MyelinBorerMovementRules.AGITATION_TICKS; i++) {
			agitation = MyelinBorerMovementRules.decayAgitation(agitation);
		}
		assertEquals(0, agitation, "agitation must reach exactly zero, not go negative");
		assertFalse(MyelinBorerMovementRules.isAgitated(agitation),
				"a calm Borer mends instead of boring");
	}

	@Test
	void agitationNeverGoesNegative() {
		assertEquals(0, MyelinBorerMovementRules.decayAgitation(0));
	}

	@Test
	void provocationResetsTheFullTimer() {
		assertEquals(MyelinBorerMovementRules.AGITATION_TICKS,
				MyelinBorerMovementRules.raiseAgitation(5),
				"a fresh provocation restarts the full 30 seconds rather than stacking on top");
	}

}
