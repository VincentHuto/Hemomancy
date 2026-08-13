package com.vincenthuto.hemomancy.common.capability.player.shared.skill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class ToggleablePlayerPowerRulesTest {
	@Test
	void thresholdsAndCostsRemainBounded() {
		assertTrue(ToggleablePlayerPowerRules.bloodhoundCanSense(true, 9.0F, 20.0F));
		assertFalse(ToggleablePlayerPowerRules.bloodhoundCanSense(true, 20.0F, 20.0F));
		assertTrue(ToggleablePlayerPowerRules.leaveCrimsonWake(true, true, 8.0F, 20.0F));
		assertFalse(ToggleablePlayerPowerRules.leaveCrimsonWake(true, false, 8.0F, 20.0F));
		assertTrue(ToggleablePlayerPowerRules.summonShouldSpare(true, false, 2.0F, 20.0F));
		assertFalse(ToggleablePlayerPowerRules.summonShouldSpare(true, true, 2.0F, 20.0F));
	}
}
