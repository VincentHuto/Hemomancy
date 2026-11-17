package com.vincenthuto.hemomancy.common.manipulation.congeatio;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Glacial Grasp's hand of ice: when it holds, and what its squeeze is worth. */
class GlacialGraspRulesTest {
	@Test
	void theHandHoldsOnlyOnceItHasFormed() {
		assertFalse(GlacialGraspRules.holds(GlacialGraspRules.FORM_TICKS - 1));
		assertTrue(GlacialGraspRules.holds(GlacialGraspRules.FORM_TICKS));
		assertEquals(0.5D, GlacialGraspRules.formed(10.0D), 1.0E-9D);
		assertEquals(1.0D, GlacialGraspRules.formed(500.0D), 1.0E-9D);
	}

	@Test
	void anUnformedHandCannotSqueeze() {
		assertEquals(0.0F, GlacialGraspRules.squeeze(0));
		assertEquals(0.0F, GlacialGraspRules.squeeze(GlacialGraspRules.FORM_TICKS - 1));
	}

	@Test
	void theSqueezeGrowsWithTheHoldAndStopsAtItsCeiling() {
		assertEquals(4.0F, GlacialGraspRules.squeeze(GlacialGraspRules.FORM_TICKS), 1.0E-6F);
		assertEquals(9.0F, GlacialGraspRules.squeeze(GlacialGraspRules.FORM_TICKS + 30), 1.0E-6F);
		assertEquals(14.0F, GlacialGraspRules.squeeze(GlacialGraspRules.FORM_TICKS + 60), 1.0E-6F);
		assertEquals(14.0F, GlacialGraspRules.squeeze(10_000), 1.0E-6F);
	}
}
