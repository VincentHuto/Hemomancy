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

	@Test
	void theJarStandsInForTheMorphlingEffectIcon() {
		assertFalse(HudVisibilityRules.showsMorphlingEffectIcon(true), "the jar already shows the Morphling");
		assertTrue(HudVisibilityRules.showsMorphlingEffectIcon(false),
				"without the jar (no charm, HUD mode off) the vanilla icon is the only sign");
	}

	@Test
	void theCharmShowsTheBloodAndTheBandolierShowsTheVials() {
		assertTrue(HudVisibilityRules.showsCharmParts(true, false), "the charm alone shows the vessel, reserve and jar");
		assertFalse(HudVisibilityRules.showsCharmParts(false, true));
		assertTrue(HudVisibilityRules.showsVials(false, true), "the bandolier shows the vials without the charm");
		assertFalse(HudVisibilityRules.showsVials(true, false), "the charm alone no longer shows them");
		assertFalse(HudVisibilityRules.showsAnything(false, false));
		assertTrue(HudVisibilityRules.showsAnything(false, true));
	}
}
