package com.vincenthuto.hemomancy.client.player;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

final class PlayerAnimationPriorityTest {
	@Test
	void cardinalPlantingSuppressesBufferedLivingTorchPresentation() {
		assertTrue(PlayerAnimationClientState.shouldApplyTorchPresentation(false, true));
		assertFalse(PlayerAnimationClientState.shouldApplyTorchPresentation(true, true));
		assertFalse(PlayerAnimationClientState.shouldApplyTorchPresentation(false, false));
	}
}
