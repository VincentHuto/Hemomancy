package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

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
	void theDaemonKeepsOneSizeAndSwellsOnlyWithItsOfferings() {
		assertEquals(CardinalRiteFinaleTiming.PROCESSION_SCALE, CardinalRiteFinaleTiming.daemonScale(0), 1e-6F,
				"from the moment it appears, not grown into");
		assertEquals(0.35F * 1.36F, CardinalRiteFinaleTiming.daemonScale(3), 1e-6F, "each offering swells it");
		assertEquals(CardinalRiteFinaleTiming.daemonScale(0), CardinalRiteFinaleTiming.daemonScale(-2), 1e-6F);
	}

	@Test
	void impactCueOccursOnceAtPlayerArrival() {
		assertFalse(CardinalRiteFinaleTiming.isImpactTick(79));
		assertTrue(CardinalRiteFinaleTiming.isImpactTick(80));
		assertFalse(CardinalRiteFinaleTiming.isImpactTick(81));
	}
}
