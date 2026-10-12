package com.vincenthuto.hemomancy.client.render.world.chamberofwill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SeamProfileTest {
	@Test
	void knotsSwellOutOfTheLipWithoutAStep() {
		assertEquals(1.0, SeamProfile.knot(0, 2.5), 1e-9, "a knot is fullest at its centre");
		assertTrue(SeamProfile.knot(6, 2.5) < 0.02, "and gone a few samples away");
		for (double d = -6; d < 6; d += 0.5)
			assertTrue(Math.abs(SeamProfile.knot(d + 0.5, 2.5) - SeamProfile.knot(d, 2.5)) < 0.25,
					"neighbouring samples never jump: " + d);
	}

	@Test
	void aVeinGrowsOutOfTheLipItsRootAsWideAsTheLipCanHoldIt() {
		double lip = 0.3, width = 0.11;
		double[] radii = SeamProfile.veinRadii(17, lip, width, -1, 0);
		assertEquals(lip * SeamProfile.ROOT_FLARE, radii[0], 1e-9, "the root fills most of the lip it leaves");
		assertTrue(radii[0] < lip, "and stays inside it, so it reads as a branch of the lip");
		assertTrue(radii[16] < width * 0.3, "the tip thins out");
		for (int i = 0; i < 16; i++) assertTrue(radii[i + 1] <= radii[i] + 1e-9, "without a bead it only tapers");
	}

	@Test
	void aBeadOfBloodIsASwellingTravellingInsideTheVein() {
		double[] plain = SeamProfile.veinRadii(17, 0.3, 0.11, -1, 0);
		double[] bead = SeamProfile.veinRadii(17, 0.3, 0.11, 0.5, 0.06);
		assertTrue(bead[8] > plain[8] + 0.04, "the vein swells where the bead is");
		assertEquals(plain[0], bead[0], 1e-6, "and nowhere near it");
		for (int i = 0; i < 16; i++)
			assertTrue(Math.abs(bead[i + 1] - bead[i]) < 0.06, "the swelling eases in and out: " + i);
	}
}
