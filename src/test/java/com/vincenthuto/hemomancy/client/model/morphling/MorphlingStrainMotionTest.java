package com.vincenthuto.hemomancy.client.model.morphling;

import com.vincenthuto.hemomancy.client.model.entity.morphling.MorphlingStrainMotion;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class MorphlingStrainMotionTest {
	@Test
	void partsGrowInOverTwoStagesThenHold() {
		assertEquals(0F, MorphlingStrainMotion.growth(4, 3));
		assertEquals(0.7F, MorphlingStrainMotion.growth(2, 2));
		assertEquals(0.88F, MorphlingStrainMotion.growth(2, 3));
		assertEquals(1F, MorphlingStrainMotion.growth(2, 5));
	}

	@Test
	void aHeldBreathSwellsThenStaysStill() {
		assertEquals(0F, MorphlingStrainMotion.breath(0, 0), 1e-4);
		assertEquals(1F, MorphlingStrainMotion.breath(MorphlingStrainMotion.BREATH_RISE / 2, 0), 1e-4);
		for (float t = MorphlingStrainMotion.BREATH_RISE; t < MorphlingStrainMotion.BREATH_CYCLE; t += 5) {
			assertEquals(0F, MorphlingStrainMotion.breath(t, 0), "still after the breath at " + t);
		}
		assertEquals(MorphlingStrainMotion.breath(10, 0),
				MorphlingStrainMotion.breath(10 + MorphlingStrainMotion.BREATH_CYCLE, 0), 1e-3);
	}

	@Test
	void stateMotionsSettleRatherThanSnap() {
		float value = MorphlingStrainMotion.settle(0, true, 1);
		assertTrue(value > 0 && value < 0.5F);
		for (int i = 0; i < 40; i++) value = MorphlingStrainMotion.settle(value, true, 1);
		assertEquals(1F, value, 0.01F);
		assertEquals(0F, MorphlingStrainMotion.settle(0.5F, false, 100), 1e-6);
		assertEquals(0.5F, MorphlingStrainMotion.settle(0.5F, true, 0), 1e-6, "no time, no change");
	}

	@Test
	void frondsOpenWithLightAndFullyWithTheirPowers() {
		assertEquals(0F, MorphlingStrainMotion.frondOpen(0, 0));
		assertEquals(0.7F, MorphlingStrainMotion.frondOpen(1, 0), 1e-6);
		assertEquals(1F, MorphlingStrainMotion.frondOpen(0, 1));
	}

	@Test
	void theVeinPulseTravelsFromGroupToGroup() {
		float peak0 = 0, peak3 = 0, time0 = 0, time3 = 0;
		for (float t = 0; t < 52; t += 0.25F) {
			float a = MorphlingStrainMotion.veinPulse(t, 0), b = MorphlingStrainMotion.veinPulse(t, 3);
			if (a > peak0) { peak0 = a; time0 = t; }
			if (b > peak3) { peak3 = b; time3 = t; }
		}
		assertTrue(time3 > time0, "a later group lights later");
		assertTrue(peak0 <= 1F);
	}

	@Test
	void dropletsCycleAndKindlingBrightensTheMane() {
		float d = MorphlingStrainMotion.drip(10, 0.02F, 0.3F);
		assertTrue(d >= 0 && d < 1);
		assertTrue(MorphlingStrainMotion.flame(5, 1, true) >= MorphlingStrainMotion.flame(5, 1, false));
	}
}
