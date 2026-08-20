package com.vincenthuto.hemomancy.common.mission;

import com.vincenthuto.hemomancy.common.mission.alchemist.FirstSeparationSpinProof;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class FirstSeparationSpinProofTest {
	@Test
	void onlyTheAssignedPlayersExactSpinCanCompleteRecovery() {
		UUID player = UUID.fromString("3dd1fc47-604f-41a8-ad35-ef7119c71aa0");
		UUID otherPlayer = UUID.fromString("7fd91ed5-7590-43f8-96fb-5aad9ab9214a");
		UUID assignedSpin = UUID.fromString("65f09daf-a6e7-451e-aed3-9801852748c6");
		UUID otherSpin = UUID.fromString("198a12b0-567f-4412-a3cd-94767fcf048c");

		assertTrue(FirstSeparationSpinProof.matches(player, assignedSpin, player, assignedSpin));
		assertFalse(FirstSeparationSpinProof.matches(player, assignedSpin, player, otherSpin));
		assertFalse(FirstSeparationSpinProof.matches(player, assignedSpin, otherPlayer, assignedSpin));
		assertFalse(FirstSeparationSpinProof.matches(player, assignedSpin, null, null));
	}
}
