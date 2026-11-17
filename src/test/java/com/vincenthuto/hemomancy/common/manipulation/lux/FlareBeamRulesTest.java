package com.vincenthuto.hemomancy.common.manipulation.lux;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Hematic Flare's searchlight: how heat builds on a body, how it is lost, and what the flash is worth. */
class FlareBeamRulesTest {
	@Test
	void heatBuildsOnOneBodyAndFallsAwayTwiceAsFastOffIt() {
		double heat = 0.0D;
		for (int tick = 0; tick < FlareBeamRules.FULL_HEAT_TICKS; tick++) heat = FlareBeamRules.heat(heat, true);
		assertEquals(1.0D, heat, 1.0E-9D);
		assertEquals(1.0D, FlareBeamRules.heat(heat, true), 1.0E-9D, "heat climbed past full");
		for (int tick = 0; tick < FlareBeamRules.FULL_HEAT_TICKS / 2; tick++) heat = FlareBeamRules.heat(heat, false);
		assertEquals(0.0D, heat, 1.0E-9D);
		assertEquals(0.0D, FlareBeamRules.heat(heat, false), 1.0E-9D, "heat fell below nothing");
	}

	@Test
	void aColdBeamCannotFlash() {
		assertFalse(FlareBeamRules.mayFlash(FlareBeamRules.MIN_FLASH_HEAT - 0.01D));
		assertEquals(0.0F, FlareBeamRules.flash(0.1D));
		assertTrue(FlareBeamRules.mayFlash(FlareBeamRules.MIN_FLASH_HEAT));
	}

	@Test
	void theFlashRunsFromTwoToEightByHeat() {
		assertEquals(2.0F, FlareBeamRules.flash(FlareBeamRules.MIN_FLASH_HEAT), 1.0E-6F);
		assertEquals(8.0F, FlareBeamRules.flash(1.0D), 1.0E-6F);
		assertEquals(8.0F, FlareBeamRules.flash(4.0D), 1.0E-6F);
		assertTrue(FlareBeamRules.flash(0.65D) > 2.0F && FlareBeamRules.flash(0.65D) < 8.0F);
	}
}
