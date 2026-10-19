package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperMountControllerRulesTest {
	@Test
	void timelinesCoverEveryGroundAttackAndEndAfterRecovery() {
		for (VesperPhaseOneAttack attack : VesperPhaseOneAttack.values()) {
			if (attack == VesperPhaseOneAttack.IDLE) continue;
			VesperMoveTimeline timeline = VesperMountControllerRules.timeline(attack);
			assertTrue(timeline.windup() >= 10, attack + " needs a readable wind-up");
			assertEquals(VesperMoveTimeline.Stage.COMPLETE, timeline.stageAt(timeline.total()));
			assertEquals(VesperMoveTimeline.Stage.WINDUP, timeline.stageAt(0));
		}
	}

	@Test
	void timelinesKeepTheAuthoredImpactTicks() {
		assertEquals(25, VesperMountControllerRules.timeline(VesperPhaseOneAttack.ROYAL_SCUTTLE).commitTick());
		assertEquals(24, VesperMountControllerRules.timeline(VesperPhaseOneAttack.PINCER_VICE).commitTick());
		assertEquals(22, VesperMountControllerRules.timeline(VesperPhaseOneAttack.BROOD_TRAMPLE).commitTick());
		assertEquals(32, VesperMountControllerRules.timeline(VesperPhaseOneAttack.PUPPET_MUSTER).commitTick());
		assertEquals(120, VesperMountControllerRules.timeline(VesperPhaseOneAttack.CARAPACE_ANEURYSM).total());
		assertEquals(70, VesperMountControllerRules.timeline(VesperPhaseOneAttack.GRAB_IMPALEMENT).total());
		assertEquals(15, VesperMountControllerRules.timeline(VesperPhaseOneAttack.GRAB_IMPALEMENT).commitTick());
	}

	@Test
	void interruptsNeverCutAnActiveSwing() {
		for (VesperMountControllerRules.Priority priority : VesperMountControllerRules.Priority.values()) {
			assertEquals(VesperMountControllerRules.Interrupt.QUEUE,
					VesperMountControllerRules.interrupt(VesperMoveTimeline.Stage.ACTIVE, priority));
		}
		assertEquals(VesperMountControllerRules.Interrupt.STAGGER, VesperMountControllerRules.interrupt(
				VesperMoveTimeline.Stage.WINDUP, VesperMountControllerRules.Priority.WOUND));
		assertEquals(VesperMountControllerRules.Interrupt.TAKE_NOW, VesperMountControllerRules.interrupt(
				VesperMoveTimeline.Stage.RECOVERY, VesperMountControllerRules.Priority.WOUND));
		assertEquals(VesperMountControllerRules.Interrupt.TAKE_NOW, VesperMountControllerRules.interrupt(
				VesperMoveTimeline.Stage.COMPLETE, VesperMountControllerRules.Priority.WING_GROWTH));
	}

	@Test
	void turnTowardIsRateLimitedAndWrapsTheShortWay() {
		assertEquals(10.0F, VesperMountControllerRules.turnToward(0.0F, 90.0F, 10.0F), 1.0E-4F);
		assertEquals(-10.0F, VesperMountControllerRules.turnToward(0.0F, 350.0F, 10.0F), 1.0E-4F);
		assertEquals(45.0F, VesperMountControllerRules.turnToward(40.0F, 45.0F, 10.0F), 1.0E-4F);
	}

	@Test
	void patrolPointStaysInsideSafeHalfExtent() {
		for (long time = 0; time < 4000; time += 20) {
			double[] point = VesperMountControllerRules.patrolPoint(100.0D, -50.0D, time);
			assertTrue(Math.abs(point[0] - 100.0D) <= VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT);
			assertTrue(Math.abs(point[1] + 50.0D) <= VesperWingedFlightRules.SAFE_ARENA_HALF_EXTENT);
		}
	}

	@Test
	void targetLossFinishesMoveThenPatrols() {
		assertEquals(VesperMountControllerRules.Next.CONTINUE,
				VesperMountControllerRules.nextState(VesperMoveTimeline.Stage.ACTIVE, true));
		assertEquals(VesperMountControllerRules.Next.SKIP_TO_RECOVERY,
				VesperMountControllerRules.nextState(VesperMoveTimeline.Stage.WINDUP, false));
		assertEquals(VesperMountControllerRules.Next.FINISH,
				VesperMountControllerRules.nextState(VesperMoveTimeline.Stage.ACTIVE, false));
		assertEquals(VesperMountControllerRules.Next.FINISH,
				VesperMountControllerRules.nextState(VesperMoveTimeline.Stage.RECOVERY, false));
		assertEquals(VesperMountControllerRules.Next.PATROL,
				VesperMountControllerRules.nextState(VesperMoveTimeline.Stage.COMPLETE, false));
	}

	@Test
	void everyGroundMoveHasATelegraphCue() {
		for (VesperPhaseOneAttack attack : VesperPhaseOneAttack.values()) {
			if (attack == VesperPhaseOneAttack.IDLE) continue;
			assertNotEquals(VesperMountControllerRules.Telegraph.NONE, VesperMountControllerRules.telegraph(attack),
					attack + " must show its wind-up");
		}
		assertEquals(VesperMountControllerRules.Telegraph.MARKERS,
				VesperMountControllerRules.telegraph(VesperPhaseOneAttack.STINGER_SCRIPT));
		assertEquals(VesperMountControllerRules.Telegraph.STRINGS,
				VesperMountControllerRules.telegraph(VesperPhaseOneAttack.PUPPET_MUSTER));
		assertEquals(VesperMountControllerRules.Telegraph.CONE,
				VesperMountControllerRules.telegraph(VesperPhaseOneAttack.PINCER_VICE));
	}

	@Test
	void aimTracksOnlyDuringWindup() {
		assertTrue(VesperMountControllerRules.tracksTarget(VesperMoveTimeline.Stage.WINDUP));
		assertFalse(VesperMountControllerRules.tracksTarget(VesperMoveTimeline.Stage.ACTIVE));
		assertFalse(VesperMountControllerRules.tracksTarget(VesperMoveTimeline.Stage.RECOVERY));
	}
}
