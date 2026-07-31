package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CovenantVigilRulesTest {
	@Test
	void theVigilRewardsItsSurvivorsWithTenMinutesOfMinorProtectionAndRecovery() {
		assertEquals(12_000, CovenantVigilRules.REWARD_DURATION_TICKS);
		assertEquals(0, CovenantVigilRules.RESISTANCE_AMPLIFIER);
		assertEquals(0, CovenantVigilRules.REGENERATION_AMPLIFIER);
	}
}
