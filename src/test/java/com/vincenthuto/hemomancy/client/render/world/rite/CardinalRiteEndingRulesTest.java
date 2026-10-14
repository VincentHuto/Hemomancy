package com.vincenthuto.hemomancy.client.render.world.rite;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteEventKind;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteEndingRulesTest {
	@Test
	void liveRitesHaveNoEnding() {
		assertEquals(1.0F, CardinalRiteEndingRules.fade(null, 12.0F), 0.0001F);
		assertEquals(0.0F, CardinalRiteEndingRules.domeSink(null, 12.0F), 0.0001F);
		assertEquals(7.0F, CardinalRiteEndingRules.staffMeltTicks(null, 12.0F, 7.0F, 80.0F), 0.0001F);
		assertFalse(CardinalRiteEndingRules.finished(null, 1000.0F));
	}

	@Test
	void everyEndingFitsInsideTheGhostLifetime() {
		for (CardinalRiteEventKind kind : CardinalRiteEventKind.values()) {
			if (!kind.terminal()) continue;
			assertTrue(CardinalRiteEndingRules.duration(kind) <= 80.0F, kind + " outlives its ghost");
			assertEquals(0.0F, CardinalRiteEndingRules.fade(kind, CardinalRiteEndingRules.duration(kind)),
					0.0001F, kind + " fades fully");
			assertTrue(CardinalRiteEndingRules.domeSink(kind, CardinalRiteEndingRules.duration(kind)) >= 1.0F,
					kind + " sinks its dome fully");
		}
	}

	@Test
	void collapseHoldsBrieflyThenDrains() {
		assertEquals(1.0F, CardinalRiteEndingRules.fade(CardinalRiteEventKind.COLLAPSED, 6.0F), 0.0001F);
		float midway = CardinalRiteEndingRules.fade(CardinalRiteEventKind.BROKEN, 24.0F);
		assertTrue(midway > 0.0F && midway < 1.0F);
	}

	@Test
	void ruptureTravelsAroundTheRingAndFinishesBeforeTheGhostEnds() {
		float first = CardinalRiteEndingRules.rupture(6.0F, 0, 0.0F, 0.0F);
		float last = CardinalRiteEndingRules.rupture(6.0F, 0, 0.95F, 0.0F);
		assertTrue(first > last, "the tear reaches the first chunk before the last");
		for (int ring = 0; ring < 8; ring++) {
			assertEquals(1.0F, CardinalRiteEndingRules.rupture(
					CardinalRiteEndingRules.VIOLENT_TICKS - 1.0F, ring, 0.999F, 0.999F), 0.0001F,
					"ring " + ring + " finishes tearing in time");
		}
	}

	@Test
	void staffMeltNeverRunsBackwards() {
		assertEquals(60.0F, CardinalRiteEndingRules.staffMeltTicks(
				CardinalRiteEventKind.COLLAPSED, 0.0F, 60.0F, 80.0F), 0.0001F);
		assertEquals(80.0F, CardinalRiteEndingRules.staffMeltTicks(
				CardinalRiteEventKind.COLLAPSED, 30.0F, 10.0F, 80.0F), 0.0001F);
		assertEquals(80.0F, CardinalRiteEndingRules.staffMeltTicks(
				CardinalRiteEventKind.COMPLETED, 16.0F, 0.0F, 80.0F), 0.0001F);
	}

	@Test
	void socketsBurstOneAfterAnother() {
		assertTrue(CardinalRiteEndingRules.socketBurstDelay(3) > CardinalRiteEndingRules.socketBurstDelay(0));
		assertTrue(CardinalRiteEndingRules.socketBurstDelay(7) < CardinalRiteEndingRules.VIOLENT_TICKS - 20.0F,
				"the last socket of an eight-anchor rite still bursts with time to fade");
	}

	@Test
	void hashIsStableAndBounded() {
		assertEquals(CardinalRiteEndingRules.hash(3, 9), CardinalRiteEndingRules.hash(3, 9), 0.0F);
		for (int a = 0; a < 50; a++) {
			float value = CardinalRiteEndingRules.hash(a, a * 7);
			assertTrue(value >= 0.0F && value < 1.0F);
		}
	}
}
