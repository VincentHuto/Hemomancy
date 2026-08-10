package com.vincenthuto.hemomancy.common.entity.summon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BloodConstructSoundGateTest {

	@Test
	void lifecycleCuesUseTheConfiguredConstructVolumeAndOnlyPlayOnce() {
		BloodConstructSoundGate gate = new BloodConstructSoundGate();

		BloodConstructSoundGate.CueRequest expiration = gate.claimExpiration(0.1F);
		assertTrue(expiration.shouldPlay());
		assertEquals(0.1F, expiration.volume());
		assertFalse(gate.claimExpiration(0.1F).shouldPlay());

		BloodConstructSoundGate.CueRequest dissolution = gate.claimDissolution(0.1F);
		assertTrue(dissolution.shouldPlay());
		assertEquals(0.1F, dissolution.volume());
		assertFalse(gate.claimDissolution(0.1F).shouldPlay());
	}
}
