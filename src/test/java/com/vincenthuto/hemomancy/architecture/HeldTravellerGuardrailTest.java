package com.vincenthuto.hemomancy.architecture;

import com.vincenthuto.hemomancy.support.SourceAssertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A player held in place by a travel system (Axonal Transduction, the Lidless Passage) is held by one rule:
 * {@code common/util/HeldTraveller.isHeld}. The mixins and listeners that stop a held player from moving, being
 * pushed, picking things up, dropping or clicking ask that predicate, never one system's own flag, so a new hold
 * reaches every one of them at once. Standing space for a landing comes from {@code StandingSpaces}, not a copy.
 */
class HeldTravellerGuardrailTest {
	private static final List<String> HOLD_SITES = List.of(
			"mixin/core/MixinEntity.java",
			"mixin/core/MixinAxonalLivingEntity.java",
			"mixin/core/MixinParalysisPlayer.java",
			"mixin/core/MixinLocalPlayer.java",
			"mixin/core/MixinParalysisServerInput.java",
			"common/manipulation/ductilis/Paralysis.java",
			"common/entity/boss/endgame/NaeglerophaeonEntity.java");

	@Test
	void holdSitesAskTheSharedPredicate() {
		for (String file : HOLD_SITES) {
			String text = SourceAssertions.readMainJava(file);
			assertFalse(text.contains("AxonalTransductionManager.isTraveling("),
					file + " asks HeldTraveller.isHeld, not Axonal's own flag");
			assertTrue(text.contains("HeldTraveller.isHeld("), file + " asks HeldTraveller.isHeld");
		}
	}

	@Test
	void axonalLandsThroughTheSharedStandingSpaces() {
		String text = SourceAssertions.readMainJava("common/manipulation/ductilis/AxonalTransductionManager.java");
		assertFalse(text.contains("private static boolean safe("), "the standing-space check lives in StandingSpaces");
		assertTrue(text.contains("StandingSpaces.near("), "Axonal lands through StandingSpaces.near");
		assertTrue(text.contains("LidlessPassageService.isCrossing("), "a player crossing a door cannot enter Axonal");
	}
}
