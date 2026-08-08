package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

final class VesperFightFissureStyleTest {
	@Test
	void fissuresRemainBelowStoneReliefAndAboveTheCrimsonUnderlayer() {
		assertTrue(VesperFightFissureStyle.SURFACE_Y < -0.026F);
		assertTrue(VesperFightFissureStyle.SURFACE_Y > -0.035F);
	}

	@Test
	void glowBreathesAndBranchEndsFadeMoreThanTheirCenters() {
		float brightTime = (float) (Math.PI * 0.5D / 0.06D);
		float dimTime = (float) (Math.PI * 1.5D / 0.06D);
		float brightCenter = VesperFightFissureStyle.alphaScale(brightTime, 0, 2, 5);
		float dimCenter = VesperFightFissureStyle.alphaScale(dimTime, 0, 2, 5);
		float brightEnd = VesperFightFissureStyle.alphaScale(brightTime, 0, 0, 5);

		assertTrue(brightCenter > dimCenter * 1.5F, "the glow should visibly fade between breaths");
		assertTrue(brightCenter > brightEnd, "branch ends should taper rather than terminate at full brightness");
		assertTrue(VesperFightFissureStyle.coreAlpha(200, brightCenter)
				> VesperFightFissureStyle.glowAlpha(200, brightCenter));
		assertTrue(VesperFightFissureStyle.glowAlpha(200, brightCenter) > 0);
	}
}
