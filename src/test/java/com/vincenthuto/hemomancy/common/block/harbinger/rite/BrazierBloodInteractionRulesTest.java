package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BrazierBloodInteractionRulesTest {
	private static final double CAPACITY = 50.0D;

	@Test
	void absorptionExtinguishesOnlyAnEmptyLitBrazier() {
		assertTrue(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(true, true, 1.0D));
		assertFalse(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(false, true, 1.0D));
		assertFalse(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(true, false, 1.0D));
		assertFalse(BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(true, true, 0.0D));
	}

	@Test
	void absorptionParticlesRequireALitBrazierRegardlessOfOfferingState() {
		assertFalse(BrazierBloodInteractionRules.shouldDrawAbsorptionParticles(false));
		assertTrue(BrazierBloodInteractionRules.shouldDrawAbsorptionParticles(true));
	}

	@Test
	void projectionPoursTheLightingCostOverTwentyTicks() {
		double pooled = 0.0D;
		int ticks = 0;
		while (!BrazierBloodInteractionRules.isFull(pooled, CAPACITY)) {
			pooled += BrazierBloodInteractionRules.fillStep(pooled, CAPACITY, 100.0D, 1000.0D);
			ticks++;
		}
		assertEquals(20, ticks);
		assertEquals(CAPACITY, pooled, 1.0E-9D);
	}

	@Test
	void aPourNeverExceedsTheToolTheCasterOrTheBowl() {
		assertEquals(1.0D, BrazierBloodInteractionRules.fillStep(0.0D, CAPACITY, 1.0D, 1000.0D), 1.0E-9D);
		assertEquals(0.5D, BrazierBloodInteractionRules.fillStep(0.0D, CAPACITY, 100.0D, 0.5D), 1.0E-9D);
		assertEquals(1.0D, BrazierBloodInteractionRules.fillStep(49.0D, CAPACITY, 100.0D, 1000.0D), 1.0E-9D);
		assertEquals(0.0D, BrazierBloodInteractionRules.fillStep(CAPACITY, CAPACITY, 100.0D, 1000.0D), 1.0E-9D);
		assertEquals(0.0D, BrazierBloodInteractionRules.fillStep(0.0D, CAPACITY, 100.0D, 0.0D), 1.0E-9D);
	}

	@Test
	void absorptionDrainsTheBowlOverTwentyTicks() {
		double pooled = CAPACITY;
		int ticks = 0;
		while (!BrazierBloodInteractionRules.isDry(pooled)) {
			pooled -= BrazierBloodInteractionRules.drainStep(pooled, 100.0D);
			ticks++;
		}
		assertEquals(20, ticks);
		assertEquals(0.0D, BrazierBloodInteractionRules.drainStep(0.0D, 100.0D), 1.0E-9D);
	}

	@Test
	void fireChangesThatSkipTheBowlFillOrBurnItDry() {
		assertEquals(CAPACITY, BrazierBloodInteractionRules.settle(false, true, 12.0D, CAPACITY), 1.0E-9D);
		assertEquals(0.0D, BrazierBloodInteractionRules.settle(true, false, CAPACITY, CAPACITY), 1.0E-9D);
		assertEquals(12.0D, BrazierBloodInteractionRules.settle(false, false, 12.0D, CAPACITY), 1.0E-9D);
		assertEquals(30.0D, BrazierBloodInteractionRules.settle(true, true, 30.0D, CAPACITY), 1.0E-9D);
	}

	@Test
	void savedBraziersFromBeforeTheBowlStayConsistent() {
		assertEquals(CAPACITY, BrazierBloodInteractionRules.settleLoaded(true, 0.0D, CAPACITY), 1.0E-9D);
		assertEquals(0.0D, BrazierBloodInteractionRules.settleLoaded(false, CAPACITY, CAPACITY), 1.0E-9D);
		assertEquals(20.0D, BrazierBloodInteractionRules.settleLoaded(false, 20.0D, CAPACITY), 1.0E-9D);
	}

	@Test
	void theBowlNeverLooksEmptyWhileLitOrFullWhileOut() {
		assertEquals(1.0F, BrazierBloodInteractionRules.poolFraction(true, 0.0D, CAPACITY), 1.0E-6F);
		assertEquals(0.0F, BrazierBloodInteractionRules.poolFraction(false, CAPACITY, CAPACITY), 1.0E-6F);
		assertEquals(0.5F, BrazierBloodInteractionRules.poolFraction(true, 25.0D, CAPACITY), 1.0E-6F);
		assertEquals(0.4F, BrazierBloodInteractionRules.poolFraction(false, 20.0D, CAPACITY), 1.0E-6F);
	}
}
