package com.vincenthuto.hemomancy.common.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ArmorSetBonusRulesTest {
	@Test
	void searingContactRequiresTheFullSetAndToggle() {
		assertFalse(ArmorSetBonusRules.searingContactActive(false, true));
		assertFalse(ArmorSetBonusRules.searingContactActive(true, false));
		assertTrue(ArmorSetBonusRules.searingContactActive(true, true));
		assertTrue(ArmorSetBonusRules.nextSearingContactState(false));
		assertEquals(80, ArmorSetBonusRules.SEARING_CONTACT_FIRE_TICKS);
	}

	@Test
	void pelagicMotionCoversBothAquaticArmorLines() {
		assertEquals(1.35D, ArmorSetBonusRules.pelagicSwimMultiplier(true, false), 0.000001D);
		assertEquals(1.35D, ArmorSetBonusRules.pelagicSwimMultiplier(false, true), 0.000001D);
		assertEquals(1.0D, ArmorSetBonusRules.pelagicSwimMultiplier(false, false), 0.000001D);
	}
}
