package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SilentiumAscensionRulesTest {
	@Test
	void onlyAPendingSilenceThatBeatVesperCanDrawInHisMemory() {
		assertTrue(SilentiumAscensionRules.canAbsorbVesperMemory(7, EnumArchonPath.SILENT_PENDING, true));
	}

	@Test
	void aLostMemoryIsReissuedOnlyToTheSilenceThatEarnedItAndNeverDoubled() {
		assertTrue(SilentiumAscensionRules.shouldReissueMemory(7, EnumArchonPath.SILENT_PENDING, true, false, false, false));
		assertFalse(SilentiumAscensionRules.shouldReissueMemory(7, EnumArchonPath.SILENT_PENDING, true, false, true, false));
		assertFalse(SilentiumAscensionRules.shouldReissueMemory(7, EnumArchonPath.SILENT_PENDING, true, false, false, true));
		assertFalse(SilentiumAscensionRules.shouldReissueMemory(7, EnumArchonPath.SILENT_PENDING, true, true, false, false));
		assertFalse(SilentiumAscensionRules.shouldReissueMemory(7, EnumArchonPath.SILENT_PENDING, false, false, false, false));
		assertFalse(SilentiumAscensionRules.shouldReissueMemory(8, EnumArchonPath.SILENTIUM, true, true, false, false));
	}

	@Test
	void refusesWithoutVesperVictory() {
		assertFalse(SilentiumAscensionRules.canAbsorbVesperMemory(7, EnumArchonPath.SILENT_PENDING, false));
		assertFalse(SilentiumAscensionRules.canAbsorbVesperMemory(7, EnumArchonPath.APOTHEOS_PENDING, true));
		assertFalse(SilentiumAscensionRules.canAbsorbVesperMemory(7, EnumArchonPath.NONE, true));
		assertFalse(SilentiumAscensionRules.canAbsorbVesperMemory(8, EnumArchonPath.SILENTIUM, true));
		assertFalse(SilentiumAscensionRules.canAbsorbVesperMemory(6, EnumArchonPath.SILENT_PENDING, true));
	}
}
