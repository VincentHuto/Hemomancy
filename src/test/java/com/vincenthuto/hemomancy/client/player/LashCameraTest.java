package com.vincenthuto.hemomancy.client.player;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** UN-56 (10): with no lash out, or the steadying off, the mouse turn reaches the view in its own units, untouched. */
class LashCameraTest {
	@Test
	void anOrdinaryTurnPassesStraightThrough() {
		LashCamera.reset();
		double[] view = LashCamera.split(0.1D / 3.0D, -7.123456789D);
		assertEquals(0.1D / 3.0D, view[0], 0.0D);
		assertEquals(-7.123456789D, view[1], 0.0D);
	}
}
