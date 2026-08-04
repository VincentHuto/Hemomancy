package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class ManipulationRingLayoutTest {
	@Test
	void anchorsTheInnermostManipulationAtTheRequestedStartingRadius() {
		assertEquals(255, ManipulationRingLayout.anchorRadius(175, List.of(-80.0, 10.0, 40.0)));
	}

	@Test
	void roundsOutwardSoFractionalProjectionsNeverCrossTheStartingRadius() {
		assertEquals(256, ManipulationRingLayout.anchorRadius(175, List.of(-80.2, 12.0)));
	}
}
