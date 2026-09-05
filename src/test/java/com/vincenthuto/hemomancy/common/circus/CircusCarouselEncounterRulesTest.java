package com.vincenthuto.hemomancy.common.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CircusCarouselEncounterRulesTest {
	@Test
	void eachBoundRiderExposesOnlyItsMatchingAnchor() {
		int severed = CircusCarouselEncounterRules.sever(0, 1);
		assertEquals(0b010, severed);
		assertFalse(CircusCarouselEncounterRules.canBreakAnchor(severed, 0));
		assertTrue(CircusCarouselEncounterRules.canBreakAnchor(severed, 1));
	}

	@Test
	void processionFallsOnlyAfterAllThreeAnchorsBreak() {
		int broken = CircusCarouselEncounterRules.breakAnchor(0, 0b111, 0);
		broken = CircusCarouselEncounterRules.breakAnchor(broken, 0b111, 1);
		assertFalse(CircusCarouselEncounterRules.allAnchorsBroken(broken));
		broken = CircusCarouselEncounterRules.breakAnchor(broken, 0b111, 2);
		assertTrue(CircusCarouselEncounterRules.allAnchorsBroken(broken));
	}

	@Test
	void unexposedAnchorCannotBeBroken() {
		assertEquals(0, CircusCarouselEncounterRules.breakAnchor(0, 0b001, 2));
	}
}
