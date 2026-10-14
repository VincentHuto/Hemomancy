package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRiteRingRulesTest {
	@Test
	void ranksRenumberAuthoredRingsDenselyInnermostFirst() {
		assertArrayEquals(new int[] {0, 0, 1, 1}, CardinalRiteRingRules.ranks(List.of(1, 1, 2, 2)),
				"layout rings start at 1");
		assertArrayEquals(new int[] {1, 0, 1, 0}, CardinalRiteRingRules.ranks(List.of(3, 0, 3, 0)),
				"gaps close and order follows the authored value, not the index");
		assertEquals(2, CardinalRiteRingRules.ringCount(new int[] {1, 0, 1, 0}));
		assertEquals(0, CardinalRiteRingRules.ringCount(new int[0]));
	}

	@Test
	void eachRingAsksAQuarterMoreBloodPerAnchor() {
		int[] expected = {50, 63, 75, 88, 100, 113, 125, 138};
		for (int rank = 0; rank < expected.length; rank++) {
			assertEquals(expected[rank], CardinalRiteRingRules.bloodForRank(rank), "rank " + rank);
		}
		assertEquals(50, CardinalRiteRingRules.bloodForRank(-1));
	}

	@Test
	void openersAreThePhasesAfterTheSealBeforeCulmination() {
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.STATION_PROJECTION));
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.ORDEAL));
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.STILL_INTERVAL));
		assertTrue(CardinalRiteRingRules.isOpener(CardinalRitePhase.OFFERING_PROCESSION));
		assertFalse(CardinalRiteRingRules.isOpener(CardinalRitePhase.INSCRIPTION));
		assertFalse(CardinalRiteRingRules.isOpener(CardinalRitePhase.CULMINATION));

		assertEquals(1, CardinalRiteRingRules.openerCount(true, 0, true, false), "a station rite projects once");
		assertEquals(2, CardinalRiteRingRules.openerCount(true, 0, false, true));
		assertEquals(3, CardinalRiteRingRules.openerCount(false, 3, false, false), "one per wave");
		assertEquals(6, CardinalRiteRingRules.openerCount(false, 3, true, true), "waves, intervals between them, procession");
		assertEquals(1, CardinalRiteRingRules.openerCount(false, 0, true, false), "a lone still interval");
		assertEquals(0, CardinalRiteRingRules.openerCount(false, 0, false, false));
	}

	@Test
	void oneRingWakesPerOpenerAndTheLastOpenerWakesTheRest() {
		assertEquals(1, CardinalRiteRingRules.ringsToOpen(0, 3, 2));
		assertEquals(1, CardinalRiteRingRules.ringsToOpen(1, 3, 1));
		assertEquals(5, CardinalRiteRingRules.ringsToOpen(0, 1, 5), "more rings than openers");
		assertEquals(3, CardinalRiteRingRules.ringsToOpen(4, 2, 3), "an opener entered again past the plan");
		assertEquals(0, CardinalRiteRingRules.ringsToOpen(0, 3, 0), "nothing left asleep");
	}
	@Test
	void theRiteIsConsecratedWhenItsOpenAnchorsFillNotEveryAnchor() {
		assertTrue(CardinalRiteRingRules.justConsecrated(3, 4, 4), "ring 0 of a two-ring rite fills");
		assertFalse(CardinalRiteRingRules.justConsecrated(4, 4, 4), "already announced");
		assertFalse(CardinalRiteRingRules.justConsecrated(3, 4, 8), "a woken ring still hungers");
		assertFalse(CardinalRiteRingRules.justConsecrated(0, 0, 0), "no open anchors");
	}
}
