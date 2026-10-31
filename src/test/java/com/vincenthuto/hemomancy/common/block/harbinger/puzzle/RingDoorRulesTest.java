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
		assertFalse(RingDoorRules.mayProject(false, 3), "a door opened properly wants a Silentium");
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
	void tenSecondsOfHeldProjectionCostAThousand() {
		assertEquals(200, RingDoorRules.OPEN_TICKS);
		assertEquals(1000, RingDoorRules.BLOOD_COST);
		assertEquals(RingDoorRules.BLOOD_COST, RingDoorRules.BLOOD_PER_TICK * RingDoorRules.OPEN_TICKS,
				"every fed tick pays its share and the whole opening pays the cost");
		assertEquals(40, RingDoorRules.turnTicks());
		assertEquals(RingDoorRules.FALL_TICKS, RingDoorRules.SINK_TICKS);
		assertEquals(30, RingDoorRules.BURST_TICKS);
	}

	@Test
	void aFedDoorFillsAndALeftDoorBleedsOffSlowly() {
		assertEquals(1, RingDoorRules.nextCharge(0, true, 0));
		assertEquals(RingDoorRules.OPEN_TICKS, RingDoorRules.nextCharge(RingDoorRules.OPEN_TICKS, true, 0));
		for (long since = 1; since <= RingDoorRules.FEED_GRACE; since++) {
			assertEquals(50, RingDoorRules.nextCharge(50, false, since), "a moment's slip keeps the charge");
			assertTrue(RingDoorRules.fed(since));
		}
		assertFalse(RingDoorRules.fed(RingDoorRules.FEED_GRACE + 1));
		int charge = RingDoorRules.OPEN_TICKS;
		long since = RingDoorRules.FEED_GRACE;
		int ticks = 0;
		while (charge > 0) {
			since++;
			ticks++;
			charge = RingDoorRules.nextCharge(charge, false, since);
		}
		assertEquals(RingDoorRules.OPEN_TICKS * RingDoorRules.BLEED_TICKS, ticks, 2,
				"a full door drains in twice the time it took to fill");
		assertEquals(0, RingDoorRules.nextCharge(0, false, 1000), "a drained door stays at nothing");
	}

	@Test
	void theRingsFillInTurnAsTheChargeRises() {
		assertEquals(0f, RingDoorRules.progress(0));
		assertEquals(0.5f, RingDoorRules.progress(100), 1e-6f);
		assertEquals(1f, RingDoorRules.progress(500));
		assertEquals(0f, RingDoorRules.ringFill(0, 0.1f));
		assertTrue(RingDoorRules.ringFill(0, 0.4f) > RingDoorRules.ringFill(1, 0.4f), "the outer ring leads");
		assertTrue(RingDoorRules.ringFill(1, 0.6f) > RingDoorRules.ringFill(2, 0.6f), "the inner ring fills last");
		for (int ring = 0; ring < RingDoorRules.RINGS; ring++) {
			assertEquals(1f, RingDoorRules.ringFill(ring, RingDoorRules.PULSE_START), "ring " + ring + " is full before the pulse");
		}
		assertEquals(0f, RingDoorRules.pulse(RingDoorRules.PULSE_START));
		assertEquals(1f, RingDoorRules.pulse(1f));
		assertEquals(0f, RingDoorRules.sink(0f));
		assertEquals(0.5f, RingDoorRules.sink(20f), 1e-6f);
		assertEquals(1f, RingDoorRules.sink(40f));
		assertTrue(RingDoorRules.sink(5f) < 5f / 40f, "the sink eases in");
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
