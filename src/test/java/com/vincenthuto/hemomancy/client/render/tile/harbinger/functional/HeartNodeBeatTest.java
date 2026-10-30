package com.vincenthuto.hemomancy.client.render.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.world.HeartNodeRules;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeBeatTest {
	private static final int BEAT = HeartNodeRules.HEARTBEAT_TICKS;

	@Test
	void theOwnerSetsThePaceAndStrangersSeeItSlow() {
		assertEquals(HeartNodeBeat.Pace.QUICK, HeartNodeBeat.pace(true, true, true));
		assertEquals(HeartNodeBeat.Pace.STEADY, HeartNodeBeat.pace(true, true, false));
		assertEquals(HeartNodeBeat.Pace.SLOW, HeartNodeBeat.pace(true, false, false), "away from the sanctum");
		assertEquals(HeartNodeBeat.Pace.SLOW, HeartNodeBeat.pace(false, true, true), "a stranger, wherever they stand");
		assertTrue(HeartNodeBeat.Pace.QUICK.period < HeartNodeBeat.Pace.STEADY.period);
		assertTrue(HeartNodeBeat.Pace.STEADY.period < HeartNodeBeat.Pace.SLOW.period);
	}

	@Test
	void everyPaceThumpsAsTheSpreadsPulseLeavesTheHeart() {
		for (HeartNodeBeat.Pace pace : HeartNodeBeat.Pace.values()) {
			assertTrue(pace.period % BEAT == 0 || BEAT % pace.period == 0, pace + " is in step with the heartbeat");
			// A pulse leaves the heart at THUMP_PEAK of every heartbeat; the slow heart thumps on every other one.
			double pulse = 7 * (double) Math.max(BEAT, pace.period) + HeartNodeBeat.THUMP_PEAK;
			assertEquals(1.0F, HeartNodeBeat.thump(pulse, pace), 1e-4F, pace + " peaks with the pulse");
		}
		assertEquals(1.0F, HeartNodeBeat.thump(BEAT / 2.0D + HeartNodeBeat.THUMP_PEAK, HeartNodeBeat.Pace.QUICK), 1e-4F,
				"a quick heart beats between pulses too");
		assertTrue(HeartNodeBeat.thump(BEAT + HeartNodeBeat.THUMP_PEAK, HeartNodeBeat.Pace.SLOW) < 0.01F,
				"a slow heart sits out every other pulse");
	}

	@Test
	void eachBeatIsADoubleThumpWithASofterSecond() {
		float lub = HeartNodeBeat.doubleThump(HeartNodeBeat.THUMP_PEAK);
		float between = HeartNodeBeat.doubleThump(HeartNodeBeat.THUMP_PEAK + 3.0F);
		float dub = HeartNodeBeat.doubleThump(HeartNodeBeat.THUMP_PEAK + 5.5F);
		assertEquals(1.0F, lub, 1e-4F);
		assertTrue(dub > between, "the second thump rises again after the first falls");
		assertTrue(dub > 0.4F && dub < lub, "and is softer than the first");
		assertTrue(HeartNodeBeat.doubleThump(HeartNodeBeat.THUMP_PEAK + 20.0F) < 0.01F, "then the heart rests");
	}

	@Test
	void theBeatIndexTicksOverAtEachFirstThump() {
		HeartNodeBeat.Pace pace = HeartNodeBeat.Pace.STEADY;
		long before = HeartNodeBeat.beatIndex(BEAT * 5 + HeartNodeBeat.THUMP_PEAK - 0.01D, pace);
		long at = HeartNodeBeat.beatIndex(BEAT * 5 + HeartNodeBeat.THUMP_PEAK, pace);
		assertEquals(before + 1, at);
		assertEquals(at, HeartNodeBeat.beatIndex(BEAT * 6 + HeartNodeBeat.THUMP_PEAK - 0.01D, pace));
	}

	@Test
	void plantingBeatsThreeTimesThenStops() {
		int peaks = 0;
		float last = 0.0F;
		boolean rising = false;
		for (float age = 0.0F; age < 60.0F; age += 0.25F) {
			float beat = HeartNodeBeat.plantingBeats(age);
			if (rising && beat < last) peaks++;
			rising = beat > last;
			last = beat;
		}
		assertEquals(HeartNodeBeat.PLANT_BEATS, peaks);
		assertEquals(1.0F, HeartNodeBeat.plantingBeats(HeartNodeBeat.PLANT_FIRST), 1e-4F);
		assertEquals(0.0F, HeartNodeBeat.plantingBeats(60.0F), 1e-6F);
	}

	@Test
	void theHeartSwellsFromItsStageSize() {
		assertEquals(0.72F, HeartNodeBeat.scale(HeartNodeBeat.restingScale(0), 0.0F), 1e-6F);
		assertEquals(1.0F + HeartNodeBeat.SWELL, HeartNodeBeat.scale(HeartNodeBeat.restingScale(3), 1.0F), 1e-6F);
		assertEquals(HeartNodeBeat.restingScale(3), HeartNodeBeat.restingScale(9), 1e-6F, "stages past the last clamp");
	}
}
