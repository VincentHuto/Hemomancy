package com.vincenthuto.hemomancy.common.effect;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MorphicTonicRulesTest {
	@Test
	void onlyAnApotheosDrinksItAndOnlyInTheGardens() {
		assertEquals(MorphicTonicRules.Refusal.NONE, MorphicTonicRules.refusal(true, true));
		assertEquals(MorphicTonicRules.Refusal.OUTSIDE_GARDENS, MorphicTonicRules.refusal(true, false));
		assertEquals(MorphicTonicRules.Refusal.NOT_APOTHEOS, MorphicTonicRules.refusal(false, true));
		assertEquals(MorphicTonicRules.Refusal.NOT_APOTHEOS, MorphicTonicRules.refusal(false, false));
	}

	@Test
	void theMendingRegrowsBloodAsWellAsHealth() {
		assertEquals(500.0D, MorphicTonicRules.bloodPerTick(true) * MorphicTonicRules.MENDING_TICKS, 1.0E-9D);
		assertEquals(0.0D, MorphicTonicRules.bloodPerTick(false), 1.0E-9D);
		assertEquals(8.0F, MorphicTonicRules.MENDING_TICKS / MorphicTonicRules.HEALTH_PULSE_TICKS
				* MorphicTonicRules.HEALTH_PER_PULSE, 1.0E-6F);
	}
}
