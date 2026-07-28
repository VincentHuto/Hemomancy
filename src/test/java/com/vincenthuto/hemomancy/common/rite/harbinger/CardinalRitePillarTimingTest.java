package com.vincenthuto.hemomancy.common.rite.harbinger;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRitePillarTimingTest {
	@Test
	void replacementPillarsNeverOverlapOrLeaveAnEmptyFrame() {
		for (int tick = 0; tick < 8; tick++) {
			int visiblePillars = 0;
			for (int spawnTick = 0; spawnTick <= tick; spawnTick += 2) {
				if (CardinalRitePillarTiming.isVisibleAtAge(tick - spawnTick)) {
					visiblePillars++;
				}
			}
			assertEquals(1, visiblePillars, "visible pillar count at tick " + tick);
		}
	}

	@Test
	void pillarOpacityDoesNotPulseWhileWaitingForItsReplacement() {
		assertEquals(0.82F, CardinalRitePillarTiming.opacityAtAge(0), 0.0001F);
		assertEquals(0.82F, CardinalRitePillarTiming.opacityAtAge(1), 0.0001F);
		assertEquals(0.0F, CardinalRitePillarTiming.opacityAtAge(2), 0.0001F);
	}
}
