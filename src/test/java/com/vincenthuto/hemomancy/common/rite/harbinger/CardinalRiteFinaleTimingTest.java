package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteFinaleTimingTest {
	@Test
	void growthFinishesBeforeTheDaemonBeginsMergingIntoThePlayer() {
		assertEquals(0.0D, CardinalRiteFinaleTiming.growthProgress(0), 0.0001D);
		assertEquals(0.5D, CardinalRiteFinaleTiming.growthProgress(20), 0.0001D);
		assertEquals(1.0D, CardinalRiteFinaleTiming.growthProgress(40), 0.0001D);
		assertEquals(0.0D, CardinalRiteFinaleTiming.mergeProgress(40), 0.0001D);
		assertEquals(0.5D, CardinalRiteFinaleTiming.mergeProgress(60), 0.0001D);
		assertEquals(1.0D, CardinalRiteFinaleTiming.mergeProgress(80), 0.0001D);
	}

	@Test
	void absorbedItemFragmentsFadeDuringGrowthAndAreGoneBeforeMerge() {
		assertEquals(1.0D, CardinalRiteFinaleTiming.offeringParticleStrength(0), 0.0001D);
		assertEquals(0.5D, CardinalRiteFinaleTiming.offeringParticleStrength(20), 0.0001D);
		assertEquals(0.0D, CardinalRiteFinaleTiming.offeringParticleStrength(40), 0.0001D);
		assertEquals(0.0D, CardinalRiteFinaleTiming.offeringParticleStrength(60), 0.0001D);
	}

	@Test
	void daemonStaysAtProcessionSizeUntilItReturnsFromTheBraziers() {
		assertEquals(0.8D, CardinalRiteFinaleTiming.preProcessionHeight(0.8D), 0.0001D);
		assertEquals(1.4D, CardinalRiteFinaleTiming.preProcessionHeight(4.5D), 0.0001D);
	}

	@Test
	void impactCueOccursOnceAtPlayerArrival() {
		assertFalse(CardinalRiteFinaleTiming.isImpactTick(79));
		assertTrue(CardinalRiteFinaleTiming.isImpactTick(80));
		assertFalse(CardinalRiteFinaleTiming.isImpactTick(81));
	}
}
