package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RingDoorRulesTest {
	@Test
	void aSilentiumProjectsOnlyWhenEveryNodeIsLit() {
		assertTrue(RingDoorRules.mayProject(true, 3));
		assertFalse(RingDoorRules.mayProject(true, 2));
		assertFalse(RingDoorRules.mayProject(false, 3), "an Apotheos shatters; it does not project");
		assertEquals(300, RingDoorRules.BLOOD_PER_RING);
		assertEquals(3, RingDoorRules.RINGS);
	}

	@Test
	void anApotheosShattersAtAnyTime() {
		assertTrue(RingDoorRules.mayShatter(true));
		assertFalse(RingDoorRules.mayShatter(false));
	}

	@Test
	void theFightRemembersEveryChapelNotSeenThrough() {
		List<ChapelRules.Kind> kinds = List.of(ChapelRules.Kind.BEHOLDING, ChapelRules.Kind.LITANY,
				ChapelRules.Kind.DOWNFALL);
		assertEquals(List.of(ChapelRules.Kind.LITANY, ChapelRules.Kind.DOWNFALL),
				RingDoorRules.skipped(kinds, new boolean[] {true, false, false}));
		assertEquals(List.of(), RingDoorRules.skipped(kinds, new boolean[] {true, true, true}));
	}

	@Test
	void hallowedGroundBurnsOnlyAnApotheos() {
		assertTrue(HallowedGroundRules.burns(true));
		assertFalse(HallowedGroundRules.burns(false), "a Silentium cut their antenna; lower Harbingers are too faint");
		assertEquals(4f, HallowedGroundRules.SPORE_DRAIN_PER_SECOND);
		assertEquals(1f, HallowedGroundRules.BURN_DAMAGE);
	}

	@Test
	void theDoorKeepsItsStageTimings() {
		assertEquals(40, RingDoorRules.turnTicks());
		assertEquals(40, RingDoorRules.fillTicks());
		assertEquals(RingDoorRules.BLOOD_PER_RING, RingDoorRules.stageCost());
		assertEquals(30, RingDoorRules.shatterTicks());
		assertEquals(20, RingDoorRules.PULSE_TICKS);
		assertEquals(RingDoorRules.FALL_TICKS, RingDoorRules.SINK_TICKS);
		assertEquals(30, RingDoorRules.BURST_TICKS);
		assertEquals(3 * 40 + 20 + 40, RingDoorRules.projectionLength());
	}

	@Test
	void aProjectionFillsEachRingThenPulsesThenSinks() {
		assertEquals(0, RingDoorRules.stageAt(0));
		assertEquals(0, RingDoorRules.stageAt(39));
		assertEquals(1, RingDoorRules.stageAt(40));
		assertEquals(2, RingDoorRules.stageAt(119));
		assertEquals(RingDoorRules.STAGE_PULSE, RingDoorRules.stageAt(120));
		assertEquals(RingDoorRules.STAGE_PULSE, RingDoorRules.stageAt(139));
		assertEquals(RingDoorRules.STAGE_SINK, RingDoorRules.stageAt(140));
		assertEquals(RingDoorRules.STAGE_SINK, RingDoorRules.stageAt(179));
		assertEquals(RingDoorRules.STAGE_DONE, RingDoorRules.stageAt(180));
		assertEquals(5, RingDoorRules.STAGE_DONE);
	}

	@Test
	void eachRingIsPaidForOnceAtTheStartOfItsStage() {
		int paid = 0;
		for (int tick = -5; tick < RingDoorRules.projectionLength() + 5; tick++) {
			int ring = RingDoorRules.paysAt(tick);
			if (ring < 0) continue;
			assertEquals(ring * RingDoorRules.FILL_TICKS, tick, "ring " + ring + " is paid as its stage starts");
			assertEquals(paid, ring, "the rings are paid outer to inner");
			paid++;
		}
		assertEquals(RingDoorRules.RINGS, paid);
		assertEquals(RingDoorRules.BLOOD_PER_RING * RingDoorRules.RINGS, paid * RingDoorRules.stageCost(),
				"three stages pay what the door has always cost");
	}

	@Test
	void theRingsFillInTurnAndTheSlabSinksLast() {
		assertEquals(0f, RingDoorRules.ringFill(0, 0f));
		assertEquals(0.5f, RingDoorRules.ringFill(0, 20f), 1e-6f);
		assertEquals(1f, RingDoorRules.ringFill(0, 40f));
		assertEquals(0f, RingDoorRules.ringFill(1, 40f));
		assertEquals(1f, RingDoorRules.ringFill(2, 500f));
		assertEquals(0f, RingDoorRules.pulse(119f));
		assertEquals(0.5f, RingDoorRules.pulse(130f), 1e-6f);
		assertEquals(1f, RingDoorRules.pulse(140f));
		assertEquals(0f, RingDoorRules.sink(140f));
		assertEquals(0.5f, RingDoorRules.sink(160f), 1e-6f);
		assertEquals(1f, RingDoorRules.sink(180f));
		assertTrue(RingDoorRules.sink(145f) < 5f / 40f, "the sink eases in");
		assertEquals(0f, RingDoorRules.crack(0f));
		assertEquals(0.5f, RingDoorRules.crack(15f), 1e-6f);
		assertEquals(1f, RingDoorRules.crack(45f));
		assertEquals(0f, RingDoorRules.turn(0f));
		assertEquals(0.5f, RingDoorRules.turn(20f), 1e-6f);
		assertEquals(1f, RingDoorRules.turn(1000f));
	}

	@Test
	void anUnlitRingRestsScrambledButNeverOnTheSeam() {
		for (long seed = -2000; seed < 2000; seed += 7) {
			for (int ring = 0; ring < RingDoorRules.RINGS; ring++) {
				float angle = RingDoorRules.restAngle(ring, seed);
				assertTrue(angle >= 30f && angle <= 330f, "ring " + ring + " of " + seed + " rests at " + angle);
				assertEquals(angle, RingDoorRules.restAngle(ring, seed), "the same door rests the same way");
			}
		}
		assertFalse(RingDoorRules.restAngle(0, 42L) == RingDoorRules.restAngle(1, 42L)
				&& RingDoorRules.restAngle(1, 42L) == RingDoorRules.restAngle(2, 42L), "the rings rest apart");
	}
}
