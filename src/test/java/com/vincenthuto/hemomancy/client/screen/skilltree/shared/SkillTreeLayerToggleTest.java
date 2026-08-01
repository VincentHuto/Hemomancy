package com.vincenthuto.hemomancy.client.screen.skilltree.shared;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SkillTreeLayerToggleTest {
	@Test
	void accessibilityToggleHonorsUnlockAndSwitchesBothWays() {
		SkillTreeDiveState state = new SkillTreeDiveState();

		assertFalse(state.toggleLayer(4));
		assertFalse(state.isDeepActive());

		assertTrue(state.toggleLayer(5));
		assertTrue(state.isDeepActive());
		assertTrue(state.transitionPulseEnteringDeep());

		assertTrue(state.toggleLayer(5));
		assertFalse(state.isDeepActive());
		assertFalse(state.transitionPulseEnteringDeep());
	}
}
