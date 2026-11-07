package com.vincenthuto.hemomancy.client.render.world;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class VanityMirrorRulesTest {
	@Test
	void theGlassStandsAtTheBackOfTheDesk() {
		// North-facing: the front is -z, so the glass is toward +z.
		double[] north = VanityMirrorRules.glassPoint(10, 64, 20, 0, -1);
		assertEquals(20 + 14.35 / 16.0, north[2], 1e-6);
		assertEquals(10.5, north[0], 1e-6);
		// East-facing: the front is +x, so the glass is toward -x.
		double[] east = VanityMirrorRules.glassPoint(10, 64, 20, 1, 0);
		assertEquals(11 - 14.35 / 16.0, east[0], 1e-6);
		assertEquals(20.5, east[2], 1e-6);
	}

	@Test
	void theEyeIsInFrontOnlyOnTheFrontSide() {
		double[] glass = VanityMirrorRules.glassPoint(0, 64, 0, 0, -1);
		assertTrue(VanityMirrorRules.inFront(new double[] { 0.5, 65.6, -2.0 }, glass, 0, -1) > 2.0);
		assertTrue(VanityMirrorRules.inFront(new double[] { 0.5, 65.6, 3.0 }, glass, 0, -1) < 0.0);
	}

	@Test
	void aPointInFrontAppearsAsFarBehindTheGlass() {
		double[] camera = { 3.2, 70.0, -4.0 };
		double[] glass = VanityMirrorRules.glassPoint(0, 64, 0, 0, -1);
		float[] reflect = VanityMirrorRules.reflection(camera, glass, 0, -1);
		// A player standing a block and a half in front of the glass, as seen from the camera.
		double[] player = { 0.5 - camera[0], 64.0 - camera[1], glass[2] - 1.5 - camera[2] };
		double[] seen = VanityMirrorRules.apply(reflect, player);
		assertArrayEquals(new double[] { player[0], player[1], glass[2] + 1.5 - camera[2] }, seen, 1e-5);
	}

	@Test
	void theGlassItselfStaysPut() {
		double[] camera = { -6.0, 66.0, 1.0 };
		double[] glass = VanityMirrorRules.glassPoint(0, 64, 0, 1, 0);
		float[] reflect = VanityMirrorRules.reflection(camera, glass, 1, 0);
		double[] onGlass = { glass[0] - camera[0], 65.2 - camera[1], 0.3 - camera[2] };
		assertArrayEquals(onGlass, VanityMirrorRules.apply(reflect, onGlass), 1e-5);
	}
}
