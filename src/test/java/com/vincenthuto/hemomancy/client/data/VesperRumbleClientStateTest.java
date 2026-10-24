package com.vincenthuto.hemomancy.client.data;

import com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperBurrowRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperRumbleClientStateTest {
	private static final VesperBurrowRules.Plan PLAN = new VesperBurrowRules.Plan(220, false);

	@Test
	void rumbleBuildsMonotonicallyTowardTheEruption() {
		float previous = -1.0F;
		for (int tick = VesperBurrowRules.rumbleStart(PLAN); tick < VesperBurrowRules.eruptionStart(PLAN); tick++) {
			float amplitude = VesperRumbleClientState.amplitude(PLAN, tick);
			assertTrue(amplitude >= previous, "the rumble never eases off before the eruption");
			assertTrue(amplitude <= VesperRumbleClientState.MAX_AMPLITUDE);
			previous = amplitude;
		}
		assertTrue(previous > VesperRumbleClientState.MAX_AMPLITUDE * 0.8F);
	}

	@Test
	void silentWhileHuntingAndAfterTheEruptionSettles() {
		assertEquals(0.0F, VesperRumbleClientState.amplitude(PLAN, VesperBurrowRules.DIVE_TICKS + 10), 1.0E-6F);
		assertEquals(0.0F, VesperRumbleClientState.amplitude(PLAN, VesperBurrowRules.recoveryStart(PLAN) + 5), 1.0E-6F);
	}

	@Test
	void eruptionPeaksThenDecays() {
		int start = VesperBurrowRules.eruptionStart(PLAN);
		float peak = VesperRumbleClientState.amplitude(PLAN, start);
		float late = VesperRumbleClientState.amplitude(PLAN, start + VesperBurrowRules.ERUPTION_TICKS - 1);
		assertEquals(VesperRumbleClientState.MAX_AMPLITUDE, peak, 1.0E-5F);
		assertTrue(late < peak);
	}
}
