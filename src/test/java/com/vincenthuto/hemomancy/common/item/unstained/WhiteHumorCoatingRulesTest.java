package com.vincenthuto.hemomancy.common.item.unstained;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WhiteHumorCoatingRulesTest {
	@Test
	void oneFlaskProvidesExactlyTwentyFourChargedHits() {
		int charges = WhiteHumorCoatingRules.CHARGES_PER_FLASK;
		for (int hit = 0; hit < 24; hit++) {
			assertTrue(WhiteHumorCoatingRules.isActive(charges));
			charges = WhiteHumorCoatingRules.afterHit(charges);
		}
		assertEquals(0, charges);
		assertFalse(WhiteHumorCoatingRules.isActive(charges));
		assertEquals(0, WhiteHumorCoatingRules.afterHit(charges));
	}
}
