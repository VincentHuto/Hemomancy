package com.vincenthuto.hemomancy.common.rite;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CardinalRiteFootprintRulesTest {

	@Test
	void awakenedSigilOrbitScalesAcrossTheFullRitualFootprint() {
		float smallFootprint = 3.75F;
		float largeFootprint = 11.75F;

		float smallOrbit = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(smallFootprint);
		float largeOrbit = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(largeFootprint);

		assertEquals(2.5F, smallOrbit, 0.0001F);
		assertEquals(10.5F, largeOrbit, 0.0001F);
		assertTrue(largeOrbit > 3.6F, "large rites must not retain the legacy orbit cap");
	}

	@Test
	void awakenedSigilOrbitLeavesClearanceInsideTheFaneShell() {
		float footprint = 11.75F;
		float orbit = CardinalRiteFootprintRules.awakenedSigilOrbitRadius(footprint);

		assertTrue(orbit + CardinalRiteFootprintRules.AWAKENED_SIGIL_CLEARANCE <= footprint);
	}

	@Test
	void awakenedSigilOrbitKeepsAMinimumUsefulRadiusForTinyFootprints() {
		assertEquals(CardinalRiteFootprintRules.MIN_AWAKENED_SIGIL_ORBIT_RADIUS,
				CardinalRiteFootprintRules.awakenedSigilOrbitRadius(0.0F), 0.0001F);
	}
}
