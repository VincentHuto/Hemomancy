package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.floor.CardinalRiteFloorVisualRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CardinalRiteMathTest {
	@Test
	void smoothstepClampsAndEasesBothEnds() {
		assertEquals(0.0F, CardinalRiteMath.smoothstep(-1.0F), 0.0001F);
		assertEquals(0.5F, CardinalRiteMath.smoothstep(0.5F), 0.0001F);
		assertEquals(1.0F, CardinalRiteMath.smoothstep(4.0F), 0.0001F);
		assertEquals(0.5F, CardinalRiteMath.smoothstep(2.0F, 4.0F, 3.0F), 0.0001F);
	}

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

	@Test
	void pulseRisesThenDecaysToNothing() {
		assertEquals(0.0F, CardinalRiteMath.pulse(-1.0F, 2.0F, 10.0F), 0.0001F);
		assertEquals(1.0F, CardinalRiteMath.pulse(2.0F, 2.0F, 10.0F), 0.0001F);
		float later = CardinalRiteMath.pulse(6.0F, 2.0F, 10.0F);
		assertTrue(later > 0.0F && later < 1.0F);
		assertEquals(0.0F, CardinalRiteMath.pulse(10.0F, 2.0F, 10.0F), 0.0001F);
	}
}
