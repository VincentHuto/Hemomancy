package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ScarNodeVisualStateTest {
	@Test
	void lockedTierOverridesLearnedAndActiveState() {
		assertEquals(ScarNodeVisualState.LOCKED, ScarNodeVisualState.resolve(true, true, true));
	}

	@Test
	void accessibleScarsProgressFromUnlearnedToKnownToActive() {
		assertEquals(ScarNodeVisualState.UNLEARNED, ScarNodeVisualState.resolve(false, false, false));
		assertEquals(ScarNodeVisualState.KNOWN, ScarNodeVisualState.resolve(false, true, false));
		assertEquals(ScarNodeVisualState.ACTIVE, ScarNodeVisualState.resolve(false, true, true));
	}
}
