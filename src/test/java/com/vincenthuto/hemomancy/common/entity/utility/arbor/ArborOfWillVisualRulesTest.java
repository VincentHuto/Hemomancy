package com.vincenthuto.hemomancy.common.entity.utility.arbor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArborOfWillVisualRulesTest {
	@Test
	void degreeGrowthIsMonotonicThroughSevenWhorls() {
		double previousHeight = 0.0;
		double previousRoots = 0.0;
		for (int degree = 1; degree <= 7; degree++) {
			assertTrue(ArborOfWillVisualRules.treeHeight(degree) > previousHeight);
			assertTrue(ArborOfWillVisualRules.rootRadius(degree, 10.0) > previousRoots);
			assertEquals(degree, ArborOfWillVisualRules.visibleWhorls(degree));
			previousHeight = ArborOfWillVisualRules.treeHeight(degree);
			previousRoots = ArborOfWillVisualRules.rootRadius(degree, 10.0);
		}
	}

	@Test
	void ninePomesLeaveTreeNearlySkeletalButApotheosisRestoresFungalTissue() {
		double healthy = ArborOfWillVisualRules.foliageFraction(20, 20, 0, false);
		double wounded = ArborOfWillVisualRules.foliageFraction(20, 20, 9, false);
		double apotheosis = ArborOfWillVisualRules.foliageFraction(20, 20, 9, true);

		assertEquals(1.0, healthy, 0.0001);
		assertTrue(wounded <= 0.12, "ninth Pome should leave the Arbor nearly skeletal");
		assertEquals(1.0, apotheosis, 0.0001);
		assertEquals(9, ArborOfWillVisualRules.woundCount(20));
	}

	@Test
	void distinguishesDegreeSealedPrerequisiteDormantAndFruitStates() {
		assertEquals(ArborOfWillVisualRules.GrowthState.DEGREE_SEALED_BUD,
				ArborOfWillVisualRules.growthState(false, false, 4, 3));
		assertEquals(ArborOfWillVisualRules.GrowthState.DORMANT_BUD,
				ArborOfWillVisualRules.growthState(false, false, 3, 3));
		assertEquals(ArborOfWillVisualRules.GrowthState.RIPE_FRUIT,
				ArborOfWillVisualRules.growthState(true, true, 3, 3));
		assertEquals(ArborOfWillVisualRules.GrowthState.CLOSED_CALYX,
				ArborOfWillVisualRules.growthState(true, false, 3, 3));
	}

	@Test
	void fruitScaleAndSeedChambersFollowLevel() {
		assertTrue(ArborOfWillVisualRules.fruitScale(1, 5) < ArborOfWillVisualRules.fruitScale(5, 5));
		assertEquals(1.0, ArborOfWillVisualRules.fruitScale(5, 5), 0.0001);
		assertEquals(3, ArborOfWillVisualRules.seedChambers(3, 5));
	}

	@Test
	void foliageFloorKeepsAGrownTreeFullUntilPomesWoundIt() {
		double previous = 0.0;
		for (int whorls = 1; whorls <= 7; whorls++) {
			double floor = ArborOfWillVisualRules.minimumFoliage(whorls, 0);
			assertTrue(floor > previous);
			previous = floor;
		}
		assertTrue(ArborOfWillVisualRules.minimumFoliage(1, 0) >= 0.4, "even a young Arbor carries real leaves");
		assertTrue(ArborOfWillVisualRules.minimumFoliage(7, 0) >= 0.75, "a grown Arbor reads full before skills fill it");
		assertTrue(ArborOfWillVisualRules.minimumFoliage(7, 9) <= 0.1, "nine Pomes still leave it nearly skeletal");
	}
}
