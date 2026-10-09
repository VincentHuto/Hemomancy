package com.vincenthuto.hemomancy.client.render.item.harbinger.crossbar;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CrossbarMotionRulesTest {
	@Test
	void tugSnapsUpWithinTwoTicksThenDecaysAway() {
		assertEquals(0.0F, CrossbarMotionRules.tug(-1.0F), 1.0E-6F);
		assertEquals(0.0F, CrossbarMotionRules.tug(0.0F), 1.0E-6F);
		assertEquals(1.0F, CrossbarMotionRules.tug(2.0F), 1.0E-6F);
		float previous = 1.0F;
		for (int tick = 3; tick <= CrossbarMotionRules.TUG_TICKS; tick++) {
			float tug = CrossbarMotionRules.tug(tick);
			assertTrue(tug < previous, "the tug must keep easing after its peak");
			previous = tug;
		}
		assertEquals(0.0F, CrossbarMotionRules.tug(CrossbarMotionRules.TUG_TICKS + 1.0F), 1.0E-6F);
	}

	@Test
	void callAndRecallLiftTheBarThenSnapPastRestAndSettle() {
		assertEquals(0.0F, CrossbarMotionRules.lift(0.0F), 1.0E-6F);
		assertEquals(1.0F, CrossbarMotionRules.lift(4.0F), 1.0E-6F);
		assertTrue(CrossbarMotionRules.lift(6.0F) < 0.0F, "the snap drops below rest before settling");
		assertEquals(0.0F, CrossbarMotionRules.lift(CrossbarMotionRules.LIFT_TICKS), 1.0E-6F);
		assertEquals(0.0F, CrossbarMotionRules.lift(-2.0F), 1.0E-6F);
		assertEquals(0.0F, CrossbarMotionRules.lift(CrossbarMotionRules.LIFT_TICKS + 3.0F), 1.0E-6F);
	}

	@Test
	void engagementEasesInWhileAPuppetIsOutAndBackOutWhenNoneAre() {
		assertEquals(0.15F, CrossbarMotionRules.approachEngage(0.0F, true), 1.0E-6F);
		assertEquals(1.0F, CrossbarMotionRules.approachEngage(0.95F, true), 1.0E-6F);
		assertEquals(0.85F, CrossbarMotionRules.approachEngage(1.0F, false), 1.0E-6F);
		assertEquals(0.0F, CrossbarMotionRules.approachEngage(0.05F, false), 1.0E-6F);
	}

	@Test
	void triggersFireOnCallRecallAttackAndBreathEdgesOnly() {
		assertTrue(CrossbarMotionRules.isCallOrRecall(1, 2));
		assertTrue(CrossbarMotionRules.isCallOrRecall(2, 1));
		assertFalse(CrossbarMotionRules.isCallOrRecall(2, 2));
		assertFalse(CrossbarMotionRules.isCallOrRecall(CrossbarMotionRules.UNSEEN, 2),
				"picking up a Crossbar whose puppet is already out is not a call");
		assertTrue(CrossbarMotionRules.isNewSwing(false, true));
		assertFalse(CrossbarMotionRules.isNewSwing(true, true));
		assertTrue(CrossbarMotionRules.isBreathStart(20, 21));
		assertFalse(CrossbarMotionRules.isBreathStart(21, 22));
		assertFalse(CrossbarMotionRules.isBreathStart(0, 1));
	}

	@Test
	void idleBarHangsStillWhileAWorkingBarWalksItsRearBarAgainstTheFront() {
		CrossbarMotionRules.Pose idle = CrossbarMotionRules.pose(37.0F, 0.0F, 0.0F, 0.0F);
		assertEquals(0.0F, idle.frontRock(), 1.0E-6F);
		assertEquals(0.0F, idle.rearRock(), 1.0E-6F);
		assertEquals(0.0F, idle.pulse(), 1.0E-6F);
		assertTrue(Math.abs(idle.roll()) < 0.03F, "an idle bar only breathes");

		CrossbarMotionRules.Pose working = CrossbarMotionRules.pose(10.0F, 1.0F, 0.0F, 0.0F);
		assertTrue(working.frontRock() * working.rearRock() < 0.0F, "the rear bar walks opposite the front");
		assertTrue(working.pulse() > 0.0F);

		CrossbarMotionRules.Pose tugged = CrossbarMotionRules.pose(10.0F, 1.0F, 1.0F, 0.0F);
		assertTrue(tugged.pitch() < working.pitch(), "a tug snaps the front of the bar upward");
		assertTrue(tugged.threadFlick() > working.threadFlick());
		CrossbarMotionRules.Pose lifted = CrossbarMotionRules.pose(10.0F, 1.0F, 0.0F, 1.0F);
		assertTrue(lifted.rise() > working.rise());
	}

	@Test
	void puppetsKeepAStableStringByEntityIdOrder() {
		List<Integer> ids = List.of(42, 7, 19);
		assertEquals(0, CrossbarMotionRules.stringIndex(ids, 7, 5));
		assertEquals(1, CrossbarMotionRules.stringIndex(ids, 19, 5));
		assertEquals(2, CrossbarMotionRules.stringIndex(ids, 42, 5));
		assertEquals(1, CrossbarMotionRules.stringIndex(List.of(1, 2, 3, 4, 5, 6, 7), 7, 5), "extra puppets share strings");
		assertEquals(-1, CrossbarMotionRules.stringIndex(ids, 99, 5));
	}
}
