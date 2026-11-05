package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.data.ChamberVeilRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class LidlessHoleRulesTest {
	@Test
	void aGatewayWalkStartsAtTheEyeAsItLookedFromTheThresholdAndEndsPastTheView() {
		assertEquals(Math.atan2(1.95D, 0.6D), LidlessHoleRules.gatewayRadius(0.0F), 1e-6,
				"the eye's half width over the gap a body stands from the panel as it steps in");
		assertEquals(ChamberVeilRules.FULL_RADIUS, LidlessHoleRules.gatewayRadius(1.0F), 1e-6, "full, as the veil is full");
		float last = 0.0F;
		for (int i = 0; i <= 10; i++) {
			float r = LidlessHoleRules.gatewayRadius(i / 10.0F);
			assertTrue(r >= last, "never narrows: " + i);
			last = r;
		}
		assertTrue(LidlessHoleRules.gatewayRadius(0.5F) > (LidlessHoleRules.gatewayRadius(0.0F) + ChamberVeilRules.FULL_RADIUS) / 2.0F,
				"and opens quickly, as a window passed through does");
	}
}
