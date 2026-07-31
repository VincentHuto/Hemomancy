package com.vincenthuto.hemomancy.common.capability.player.harbinger.degree;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class InitiatoryDegreeRiteRewardTest {
	@Test
	void riteRewardsAreExplicitPersistentCapabilityState() {
		InitiatoryDegree degree = new InitiatoryDegree();
		assertFalse(degree.hasHematicFortification());
		degree.setHematicFortification(true);
		assertTrue(degree.hasHematicFortification());

		degree.setAncestralCommunions(3);
		assertEquals(3, degree.getAncestralCommunions());
		degree.setAncestralCommunions(-2);
		assertEquals(0, degree.getAncestralCommunions());
	}
}
