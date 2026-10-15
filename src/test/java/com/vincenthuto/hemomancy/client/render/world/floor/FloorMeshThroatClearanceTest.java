package com.vincenthuto.hemomancy.client.render.world.floor;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** A brazier's mound melts into the floor before it reaches the Focus throat, so the two never cut through each other. */
final class FloorMeshThroatClearanceTest {
	@Test
	void moundsFadeOutBeforeTheThroat() {
		// Local coordinates: the Focus centre is (0.5, 0.5).
		assertEquals(0.0F, FloorMesh.throatClearance(0.5F, 0.5F), 1.0e-6F, "nothing over the Focus mouth");
		assertEquals(0.0F, FloorMesh.throatClearance(0.5F + 0.55F, 0.5F), 1.0e-6F, "nothing over the throat's lumpy rim");
		assertEquals(1.0F, FloorMesh.throatClearance(0.5F + 0.8F, 0.5F), 1.0e-6F, "full height at the near brazier's dais");
		float previous = -1.0F;
		for (float d = 0.5F; d <= 1.0F; d += 0.02F) {
			float clear = FloorMesh.throatClearance(0.5F, 0.5F + d);
			assertTrue(clear >= previous, "rises steadily away from the throat");
			previous = clear;
		}
	}
}
