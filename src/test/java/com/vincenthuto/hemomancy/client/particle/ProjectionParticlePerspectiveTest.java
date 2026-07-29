package com.vincenthuto.hemomancy.client.particle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class ProjectionParticlePerspectiveTest {

	@Test
	void localThirdPersonLayerCannotEmitInFirstPersonView() {
		assertFalse(ProjectionParticlePerspective.allowsThirdPersonEmission(true, true));
	}

	@Test
	void thirdPersonViewAndRemotePlayersStillUseThirdPersonEmission() {
		assertTrue(ProjectionParticlePerspective.allowsThirdPersonEmission(true, false));
		assertTrue(ProjectionParticlePerspective.allowsThirdPersonEmission(false, true));
	}
}
