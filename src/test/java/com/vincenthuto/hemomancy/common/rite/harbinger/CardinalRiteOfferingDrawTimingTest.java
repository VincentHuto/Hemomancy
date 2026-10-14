package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteOfferingDrawTimingTest {
	private static final int INTERVAL = CardinalRiteFinaleTiming.OFFERING_DRAW_INTERVAL_TICKS;
	private static final int TRAVEL = CardinalRiteFinaleTiming.OFFERING_DRAW_TRAVEL_TICKS;

	@Test
	void drawsAreStaggeredFromTheFirstProcessionTick() {
		assertEquals(0, CardinalRiteFinaleTiming.offeringsDue(0, 3));
		assertEquals(1, CardinalRiteFinaleTiming.offeringsDue(1, 3));
		assertEquals(1, CardinalRiteFinaleTiming.offeringsDue(INTERVAL, 3));
		assertEquals(2, CardinalRiteFinaleTiming.offeringsDue(INTERVAL + 1, 3));
		assertEquals(3, CardinalRiteFinaleTiming.offeringsDue(1000, 3), "never more than the itinerary");
	}

	@Test
	void offeringsArriveOneTravelAfterTheirDraw() {
		assertEquals(0, CardinalRiteFinaleTiming.offeringsArrived(TRAVEL, 2));
		assertEquals(1, CardinalRiteFinaleTiming.offeringsArrived(TRAVEL + 1, 2));
		assertEquals(2, CardinalRiteFinaleTiming.offeringsArrived(TRAVEL + INTERVAL + 1, 2));
	}

	@Test
	void processionEndsWhenTheLastOfferingArrives() {
		int last = 1 + 2 * INTERVAL + TRAVEL;
		assertFalse(CardinalRiteFinaleTiming.processionFinished(last - 1, 3));
		assertTrue(CardinalRiteFinaleTiming.processionFinished(last, 3));
		assertTrue(CardinalRiteFinaleTiming.processionFinished(0, 0), "no offerings means no procession");
	}
}
