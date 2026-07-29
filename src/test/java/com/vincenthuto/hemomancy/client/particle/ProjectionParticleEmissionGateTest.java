package com.vincenthuto.hemomancy.client.particle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ProjectionParticleEmissionGateTest {

	@Test
	void rejectsDuplicateRenderPassForSameEmitterAndTick() {
		ProjectionParticleEmissionGate gate = new ProjectionParticleEmissionGate();

		assertTrue(gate.tryAcquire(17, 200L));
		assertFalse(gate.tryAcquire(17, 200L));
	}

	@Test
	void permitsNextTickAndDifferentEmitters() {
		ProjectionParticleEmissionGate gate = new ProjectionParticleEmissionGate();

		assertTrue(gate.tryAcquire(17, 200L));
		assertTrue(gate.tryAcquire(18, 200L));
		assertTrue(gate.tryAcquire(17, 201L));
	}
}
