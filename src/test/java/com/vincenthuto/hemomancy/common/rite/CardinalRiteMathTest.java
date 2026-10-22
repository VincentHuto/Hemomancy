package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** The easing this class used to hold lives in {@code HemoEasing}; see {@code HemoEasingTest}. */
final class CardinalRiteMathTest {
	@Test
	void anglesWrapAndMeasureTheShortWayRound() {
		assertEquals(Math.PI * 1.5D, CardinalRiteMath.normalizeAngle(-Math.PI * 0.5D), 0.0001D);
		assertEquals(0.2D, CardinalRiteMath.angularDistance(0.1D, Math.PI * 2.0D - 0.1D), 0.0001D);
		assertEquals(Math.PI, CardinalRiteMath.angularDistance(0.0D, Math.PI), 0.0001D);
	}

	@Test
	void heartbeatIsTheFloorsLubDub() {
		for (float time = 0.0F; time < 72.0F; time += 1.5F) {
			assertEquals(CardinalRiteFloorVisualRules.heartbeat(time), CardinalRiteMath.heartbeat(time), 0.0F);
		}
	}
}
