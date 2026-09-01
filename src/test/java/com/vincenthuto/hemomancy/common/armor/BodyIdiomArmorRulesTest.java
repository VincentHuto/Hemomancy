package com.vincenthuto.hemomancy.common.armor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BodyIdiomArmorRulesTest {
	@Test
	void searingContactRequiresTheFullSetAndToggle() {
		assertFalse(BodyIdiomArmorRules.searingContactActive(false, true));
		assertFalse(BodyIdiomArmorRules.searingContactActive(true, false));
		assertTrue(BodyIdiomArmorRules.searingContactActive(true, true));
		assertTrue(BodyIdiomArmorRules.nextSearingContactState(false));
		assertEquals(80, BodyIdiomArmorRules.SEARING_CONTACT_FIRE_TICKS);
	}

	@Test
	void pelagicMotionCoversBothAquaticArmorLines() {
		assertEquals(1.35D, BodyIdiomArmorRules.pelagicSwimMultiplier(true, false), 0.000001D);
		assertEquals(1.35D, BodyIdiomArmorRules.pelagicSwimMultiplier(false, true), 0.000001D);
		assertEquals(1.0D, BodyIdiomArmorRules.pelagicSwimMultiplier(false, false), 0.000001D);
	}
}
