package com.vincenthuto.hemomancy.client.model.entity.boss.velorum;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VelorumDevoteeGaitTest {
	@Test
	void aPlantedFootKeepsPaceWithTheFloor() {
		// a planted foot moves back exactly as far as the body moves forward
		float step = 0.002F;
		for (float start : new float[]{VelorumDevoteeGait.LEFT, VelorumDevoteeGait.RIGHT}) {
			for (float p = start + VelorumDevoteeGait.SWING + 0.01F; p < start + 0.99F; p += step) {
				float a = VelorumDevoteeGait.footAhead(VelorumDevoteeGait.thigh(p, start));
				float b = VelorumDevoteeGait.footAhead(VelorumDevoteeGait.thigh(p + step, start));
				float walked = step * VelorumDevoteeGait.CYCLE * VelorumDevoteeGait.BLOCK;
				assertEquals(-walked, b - a, 0.02F, "foot " + start + " at " + p);
				assertEquals(0.0F, VelorumDevoteeGait.lift(p, start), 1.0E-6F, "a planted foot is on the floor");
			}
		}
	}

	@Test
	void theFeetTakeTurnsAndTheStepsAreShort() {
		assertTrue(VelorumDevoteeGait.lift(0.2F, VelorumDevoteeGait.LEFT) > 0.9F);
		assertEquals(0.0F, VelorumDevoteeGait.lift(0.2F, VelorumDevoteeGait.RIGHT), 1.0E-6F);
		assertTrue(VelorumDevoteeGait.lift(0.7F, VelorumDevoteeGait.RIGHT) > 0.9F);
		assertTrue(VelorumDevoteeGait.amplitude() < 0.45F, "a short stiff stride");
		assertEquals(0.5F, VelorumDevoteeGait.phase(VelorumDevoteeGait.SWING_PER_BLOCK * VelorumDevoteeGait.CYCLE * 2.5F),
				1.0E-5F);
	}

	@Test
	void theHipsSinkSoTheLowerPlantedSoleMeetsTheFloor() {
		assertEquals(0.0F, VelorumDevoteeGait.sink(0, 0, 0, 0, 0, 0), 1.0E-6F);
		float thigh = 0.3F;
		float sink = VelorumDevoteeGait.sink(thigh, 0, 0, thigh, 0.6F, 0);
		assertEquals(12.0F - VelorumDevoteeGait.reach(thigh, 0), sink, 1.0E-5F, "the straighter leg carries him");
		assertEquals(6.0F, VelorumDevoteeGait.sink(0, (float) Math.PI / 2, 0, 0, (float) Math.PI / 2, 0), 1.0E-4F,
				"kneeling on both shins drops the hips by a shin's length");
		// through a whole walk a planted sole stays on the floor and the swinging one clears it
		for (float p = 0; p < 1; p += 0.01F) {
			float lt = VelorumDevoteeGait.thigh(p, VelorumDevoteeGait.LEFT), rt = VelorumDevoteeGait.thigh(p, VelorumDevoteeGait.RIGHT);
			float ll = VelorumDevoteeGait.lift(p, VelorumDevoteeGait.LEFT), rl = VelorumDevoteeGait.lift(p, VelorumDevoteeGait.RIGHT);
			float lk = ll * VelorumDevoteeGait.KNEE_LIFT, rk = rl * VelorumDevoteeGait.KNEE_LIFT;
			float hip = 12.0F - VelorumDevoteeGait.sink(lt, lk, ll, rt, rk, rl);
			float left = hip - VelorumDevoteeGait.reach(lt, lk), right = hip - VelorumDevoteeGait.reach(rt, rk);
			assertTrue(left >= -1.0E-4F && right >= -1.0E-4F, "no sole sinks into the floor at " + p);
			assertEquals(0.0F, Math.min(left, right), 0.05F, "a planted sole is on the floor at " + p);
			if (ll > 0.5F) assertTrue(left > 0.1F, "the swinging foot clears the floor at " + p);
			if (rl > 0.5F) assertTrue(right > 0.1F, "the swinging foot clears the floor at " + p);
			if (ll > 0.95F) assertTrue(left > 0.5F, "and lifts well clear at mid-swing, " + p);
			if (rl > 0.95F) assertTrue(right > 0.5F, "and lifts well clear at mid-swing, " + p);
		}
	}
}
