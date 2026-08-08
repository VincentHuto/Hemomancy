package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class VesperOrdealRecoveryRulesTest {
	@Test
	void inactivePlayersNeedNoReconnectRecovery() {
		assertEquals(VesperOrdealRecoveryRules.Action.NONE,
				VesperOrdealRecoveryRules.reconnectAction(false, true, false));
	}

	@Test
	void staleOrdealsOutsideTheChamberAreAbandoned() {
		assertEquals(VesperOrdealRecoveryRules.Action.ABANDON,
				VesperOrdealRecoveryRules.reconnectAction(true, false, false));
	}

	@Test
	void survivingBossPhasesAreRetargetedInsteadOfDuplicated() {
		assertEquals(VesperOrdealRecoveryRules.Action.RETARGET,
				VesperOrdealRecoveryRules.reconnectAction(true, true, true));
	}

	@Test
	void missingBossIsRespawnedForAnActiveChamberOrdeal() {
		assertEquals(VesperOrdealRecoveryRules.Action.RESPAWN,
				VesperOrdealRecoveryRules.reconnectAction(true, true, false));
	}
}
