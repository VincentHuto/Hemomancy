package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class HematicCommandRulesTest {
	@Test
	void commandsRejectPlayersBossesBloodlessBodiesAndPowerfulTargets() {
		assertTrue(HematicCommandRules.canCommand(true, false, false, false, 20.0F));
		assertFalse(HematicCommandRules.canCommand(false, false, false, false, 20.0F));
		assertFalse(HematicCommandRules.canCommand(true, true, false, false, 20.0F));
		assertFalse(HematicCommandRules.canCommand(true, false, true, false, 20.0F));
		assertFalse(HematicCommandRules.canCommand(true, false, false, true, 20.0F));
		assertFalse(HematicCommandRules.canCommand(true, false, false, false, 81.0F));
	}

	@Test
	void rebukeAndImpressmentStayInsideTheirAuthoredWindows() {
		assertEquals(160, HematicCommandRules.REBUKE_DURATION_TICKS);
		assertEquals(500, HematicCommandRules.impressmentDurationTicks(20.0F));
		assertEquals(300, HematicCommandRules.impressmentDurationTicks(60.0F));
		assertEquals(300, HematicCommandRules.impressmentDurationTicks(80.0F));
	}
}
