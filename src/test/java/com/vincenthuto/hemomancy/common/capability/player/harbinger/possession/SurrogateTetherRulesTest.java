package com.vincenthuto.hemomancy.common.capability.player.harbinger.possession;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.possession.SurrogateTetherRules.Hold;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurrogateTetherRulesTest {
	@Test
	void aBodyCountsOnTheFloorInsideItsFootprintAndJustAboveIt() {
		assertTrue(SurrogateTetherRules.onFloor(0, 1, 0, 4));
		assertTrue(SurrogateTetherRules.onFloor(4, 1, -4, 4));
		assertTrue(SurrogateTetherRules.onFloor(2, SurrogateTetherRules.HEIGHT, 1, 4));
		assertFalse(SurrogateTetherRules.onFloor(5, 1, 0, 4), "past the edge");
		assertFalse(SurrogateTetherRules.onFloor(0, -1, 0, 4), "under the floor");
		assertFalse(SurrogateTetherRules.onFloor(0, SurrogateTetherRules.HEIGHT + 1, 0, 4), "flying over it");
	}

	@Test
	void aHeldBindingLastsWhileTheFloorFormsUntilARiteOnItEnds() {
		assertEquals(Hold.KEEP, SurrogateTetherRules.hold(true, false, false), "no rite yet: held");
		assertEquals(Hold.KEEP, SurrogateTetherRules.hold(true, true, true), "the rite is running");
		assertEquals(Hold.RELEASE, SurrogateTetherRules.hold(true, true, false), "the rite ended");
		assertEquals(Hold.RELEASE, SurrogateTetherRules.hold(false, false, false), "the floor was broken");
		assertEquals(Hold.RELEASE, SurrogateTetherRules.hold(false, true, true), "the floor broke mid-rite");
	}
}
