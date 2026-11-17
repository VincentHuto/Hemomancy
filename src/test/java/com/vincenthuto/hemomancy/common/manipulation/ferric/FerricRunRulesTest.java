package com.vincenthuto.hemomancy.common.manipulation.ferric;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ferric Spikes' steered run of blades: its stride, the ground it will follow, and its leash. */
class FerricRunRulesTest {
	@Test
	void theRunTravelsAtItsOwnPaceHoweverFarTheAim() {
		double[] far = FerricRunRules.stride(0.0D, 0.0D, 100.0D, 0.0D);
		assertEquals(FerricRunRules.SPEED, far[0], 1.0E-9D);
		assertEquals(0.0D, far[1], 1.0E-9D);
		double[] diagonal = FerricRunRules.stride(0.0D, 0.0D, 30.0D, 40.0D);
		assertEquals(FerricRunRules.SPEED, Math.hypot(diagonal[0], diagonal[1]), 1.0E-9D);
		assertEquals(0.6D * FerricRunRules.SPEED, diagonal[0], 1.0E-9D);
	}

	@Test
	void theRunComesToRestOnTheAim() {
		double[] close = FerricRunRules.stride(1.0D, 1.0D, 1.2D, 1.0D);
		assertEquals(1.2D, close[0], 1.0E-9D);
		double[] there = FerricRunRules.stride(1.2D, 1.0D, 1.2D, 1.0D);
		assertEquals(1.2D, there[0], 1.0E-9D);
		assertEquals(1.0D, there[1], 1.0E-9D);
	}

	@Test
	void theRunClimbsOneBlockAndDropsTwoAndNoMore() {
		assertTrue(FerricRunRules.follows(0) && FerricRunRules.follows(1) && FerricRunRules.follows(-2));
		assertFalse(FerricRunRules.follows(2), "the run climbed a two-block wall");
		assertFalse(FerricRunRules.follows(-3), "the run dropped three blocks");
	}

	@Test
	void theRunStaysOnItsLeash() {
		assertTrue(FerricRunRules.leashed(16.0D));
		assertFalse(FerricRunRules.leashed(16.1D));
	}

	@Test
	void anEruptionsBladesStandEitherSideOfTheRun() {
		assertEquals(-FerricRunRules.ERUPTION_SPACING, FerricRunRules.bladeOffset(0), 1.0E-9D);
		assertEquals(0.0D, FerricRunRules.bladeOffset(1), 1.0E-9D);
		assertEquals(FerricRunRules.ERUPTION_SPACING, FerricRunRules.bladeOffset(2), 1.0E-9D);
	}
}
