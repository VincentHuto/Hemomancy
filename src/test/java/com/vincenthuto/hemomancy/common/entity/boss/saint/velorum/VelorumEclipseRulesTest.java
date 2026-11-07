package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumCongregationRules.State;
import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumEclipseRules.Shape;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumEclipseRulesTest {
	private static final long NEVER = Long.MAX_VALUE / 2;

	@Test
	void theGateIsStateOnly() {
		assertTrue(VelorumEclipseRules.gate(3, true, NEVER, NEVER, 0.35D, 1.0F), "the dark at 35%");
		assertTrue(VelorumEclipseRules.gate(3, true, NEVER, NEVER, 0.0D, 0.15F), "or his health at 15%");
		assertFalse(VelorumEclipseRules.gate(3, true, NEVER, NEVER, 0.34D, 0.16F));
		assertFalse(VelorumEclipseRules.gate(2, true, NEVER, NEVER, 1.0D, 0.1F), "phase 3 only");
		assertFalse(VelorumEclipseRules.gate(3, false, NEVER, NEVER, 1.0D, 0.1F), "after the first Endless Hour");
		assertFalse(VelorumEclipseRules.gate(3, true, 399, NEVER, 1.0D, 0.1F), "400 ticks clear of an Hour's end");
		assertFalse(VelorumEclipseRules.gate(3, true, NEVER, 599, 1.0D, 0.1F), "600 clear of the last Eclipse");
		assertTrue(VelorumEclipseRules.gate(3, true, 400, 600, 1.0D, 0.1F));
	}

	@Test
	void twoSeedsGiveTwoStartTicks() {
		assertNotEquals(firstOpen(3L), firstOpen(4L), "the dark the fight leaves decides when, not a clock");
	}

	/** The first tick the gate holds when the dark's coverage wanders as a fight's pools come and go. */
	private static int firstOpen(long seed) {
		Random random = new Random(seed);
		double coverage = 0.0D;
		for (int tick = 0; tick < 20_000; tick++) {
			coverage = Math.max(0.0D, Math.min(0.45D, coverage + (random.nextDouble() - 0.49D) * 0.01D));
			if (VelorumEclipseRules.gate(3, true, tick, NEVER, coverage, 0.2F)) return tick;
		}
		return -1;
	}

	@Test
	void theHourAndTheEclipseKeepFourHundredTicksApart() {
		assertFalse(VelorumEclipseRules.hourAllowed(399));
		assertTrue(VelorumEclipseRules.hourAllowed(400));
		assertEquals(VelorumEclipseRules.HOUR_GAP, VelorumEntity.HOUR_REST);
	}

	@Test
	void theRelightTakesTwentyFedTicksThirtyUnderTheLitanyWithAGraceAndABleed() {
		float fed = 0.0F;
		for (int i = 0; i < 19; i++) fed = VelorumCongregationRules.feed(fed, true, 0);
		assertFalse(VelorumEclipseRules.relit(fed, false));
		for (int idle = 1; idle <= VelorumCongregationRules.GRACE_TICKS; idle++) fed = VelorumCongregationRules.feed(fed, false, idle);
		assertEquals(19.0F, fed, "the grace holds what was fed");
		fed = VelorumCongregationRules.feed(fed, false, VelorumCongregationRules.GRACE_TICKS + 1);
		assertEquals(18.5F, fed, "then it bleeds at half speed");
		assertTrue(VelorumEclipseRules.relit(20.0F, false));
		assertFalse(VelorumEclipseRules.relit(29.0F, true));
		assertTrue(VelorumEclipseRules.relit(30.0F, true));
		assertEquals(3.0F, VelorumEclipseRules.KINDLE_RATE);
	}

	@Test
	void relitItGuttersSixtyUnbrokenHalf() {
		assertEquals(60, VelorumEclipseRules.gutterTicks(3, true));
		assertEquals(60, VelorumEclipseRules.gutterTicks(1, true), "whatever the phase");
		assertEquals(20, VelorumEclipseRules.gutterTicks(3, false));
		assertEquals(60.0F, VelorumEclipseRules.PRESERVES_AT);
		assertEquals(6.0F, VelorumEclipseRules.UNBROKEN_DAMAGE);
	}

	@Test
	void theShapesReachFromFrozenAndStilledFiguresAndPinnedOnesAreHeld() {
		assertEquals(Shape.REACHES, VelorumEclipseRules.shape(State.FROZEN));
		assertEquals(Shape.REACHES, VelorumEclipseRules.shape(State.STILLED));
		assertEquals(Shape.HELD, VelorumEclipseRules.shape(State.PINNED));
		assertEquals(Shape.NONE, VelorumEclipseRules.shape(State.AWAKE), "awake allies never become shapes");
		assertEquals(Shape.NONE, VelorumEclipseRules.shape(State.HOME));
		assertEquals(4.0D, VelorumEclipseRules.reach(false));
		assertEquals(5.0D, VelorumEclipseRules.reach(true), "the Beholding deed");
		assertEquals(16, VelorumEclipseRules.REACH_TELL);
		assertEquals(60, VelorumEclipseRules.SHAPE_REST);
		assertTrue(VelorumEclipseRules.gripHolds(5.9D, 29));
		assertFalse(VelorumEclipseRules.gripHolds(6.1D, 0), "a walk of 6 tears free");
		assertFalse(VelorumEclipseRules.gripHolds(1.0D, 30), "at most 30 ticks");
	}

	@Test
	void theCongregationReachesTogetherAtFortyNinetyAndOneForty() {
		assertEquals(0, VelorumEclipseRules.revealAt(20));
		assertEquals(1, VelorumEclipseRules.revealAt(70));
		assertEquals(2, VelorumEclipseRules.revealAt(120));
		assertEquals(-1, VelorumEclipseRules.revealAt(40));
		// an arm revealed 20 ticks before runs its 9 blocks at 0.9 a tick: thrown out by the beat, done ten ticks after
		for (int beat : VelorumEclipseRules.TOGETHER) {
			assertTrue(beat + VelorumEclipseRules.ARM_LENGTH / VelorumEclipseRules.ARM_SPEED <= VelorumMove.ECLIPSE.authored().active());
		}
		assertEquals(3, VelorumEclipseRules.TOGETHER_SHAPES);
		assertEquals(15, VelorumEclipseRules.ARM_ROOT);
	}
}
