package com.vincenthuto.hemomancy.client.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class LivingTorchEmissionGateTest {
	@Test
	void emitsOncePerEntityPerGameTickButResumesOnTheNextTick() {
		LivingTorchEmissionGate gate = new LivingTorchEmissionGate();
		assertTrue(gate.tryAcquire(7, 100L));
		assertFalse(gate.tryAcquire(7, 100L));
		assertTrue(gate.tryAcquire(7, 101L));
		assertTrue(gate.tryAcquire(8, 101L));
	}

	@Test
	void levelChangeCleanupAllowsReusedEntityIdsAndTimes() {
		LivingTorchEmissionGate gate = new LivingTorchEmissionGate();
		assertTrue(gate.tryAcquire(7, 100L));
		gate.clear();
		assertTrue(gate.tryAcquire(7, 100L));
	}
}
