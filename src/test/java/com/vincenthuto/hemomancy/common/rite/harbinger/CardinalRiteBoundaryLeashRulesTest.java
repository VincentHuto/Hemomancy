package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardinalRiteBoundaryLeashRulesTest {
	@Test
	void casterLeashIsThreeTimesTheOutermostRitualRing() {
		assertEquals(7.5D, CardinalRiteBoundaryLeashRules.casterLeashRadius(3), 0.0001D);
		assertEquals(34.5D, CardinalRiteBoundaryLeashRules.casterLeashRadius(9), 0.0001D);
	}
}
