package com.vincenthuto.hemomancy.client.particle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
