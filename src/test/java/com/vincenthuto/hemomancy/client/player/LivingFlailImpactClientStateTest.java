package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class LivingFlailImpactClientStateTest {
	@Test
	void maximumChargeProducesStrongerCameraImpulseThanMinimumCharge() {
		LivingFlailImpactClientState state = new LivingFlailImpactClientState();
		state.start(0.0F, 2);
		float minimum = Math.abs(state.pitchShake(0.0F));
		state.start(1.0F, 2);
		float maximum = Math.abs(state.pitchShake(0.0F));
		assertTrue(maximum > minimum * 2.0F);
	}
}
