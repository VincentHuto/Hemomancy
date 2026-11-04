package com.vincenthuto.hemomancy.client.screen.overlay;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The mod's HUD hides with F1 and for spectators, like vanilla's status bars (cleanup audit VS-24 / A-25). */
class HudVisibilityRulesTest {
	@Test
	void hudHidesUnderF1AndForSpectators() {
		assertTrue(HudVisibilityRules.shows(false, false));
		assertFalse(HudVisibilityRules.shows(true, false), "F1 hides the HUD");
		assertFalse(HudVisibilityRules.shows(false, true), "a spectator has no blood bar");
	}
}
