package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantControllerRulesTest {
	private static void assertTimeline(MycophantAttack attack, int phase, int windup, int active, int recovery) {
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase);
		assertEquals(new BossMoveTimeline(windup, active, recovery), timeline, attack + " phase " + phase);
	}

	@Test
	void timelinesMatchSpecStartValues() {
		assertTimeline(MycophantAttack.RUSHING_EMBRACE, 1, 24, 10, 26);
		assertTimeline(MycophantAttack.SIPHON_FEED, 1, 20, 60, 16);
		assertTimeline(MycophantAttack.PLUCK, 1, 14, 6, 16);
		assertTimeline(MycophantAttack.CLAW_RAKE, 1, 16, 6, 18);
		assertTimeline(MycophantAttack.BROOD_CALL, 1, 30, 10, 30);
		assertTimeline(MycophantAttack.NECTAR_SURGE, 1, 30, 4, 20);
		assertTimeline(MycophantAttack.FUSION_EMBRACE, 2, 22, 12, 26);
		assertTimeline(MycophantAttack.WING_GUARD, 2, 8, 60, 10);
		assertTimeline(MycophantAttack.WING_SWEEP, 2, 18, 6, 20);
		assertTimeline(MycophantAttack.PULP_BLOOM, 2, 26, 8, 24);
		assertTimeline(MycophantAttack.RUSHING_EMBRACE, 2, 18, 10, 22);
	}

	@Test
	void phaseGating() {
		for (MycophantAttack attack : EnumSet.of(MycophantAttack.FUSION_EMBRACE, MycophantAttack.WING_GUARD,
				MycophantAttack.WING_SWEEP, MycophantAttack.PULP_BLOOM)) {
			assertFalse(MycophantControllerRules.allowed(attack, 1), attack.name());
			assertTrue(MycophantControllerRules.allowed(attack, 2), attack.name());
		}
		assertTrue(MycophantControllerRules.allowed(MycophantAttack.RUSHING_EMBRACE, 1));
		assertFalse(MycophantControllerRules.allowed(MycophantAttack.RUSHING_EMBRACE, 2));
		for (MycophantAttack attack : EnumSet.of(MycophantAttack.SIPHON_FEED, MycophantAttack.PLUCK,
				MycophantAttack.CLAW_RAKE, MycophantAttack.BROOD_CALL, MycophantAttack.NECTAR_SURGE)) {
			assertTrue(MycophantControllerRules.allowed(attack, 1), attack.name());
			assertTrue(MycophantControllerRules.allowed(attack, 2), attack.name());
		}
		assertFalse(MycophantControllerRules.allowed(MycophantAttack.NONE, 1));
	}

	@Test
	void rangesGateChoice() {
		for (int roll = 0; roll < 200; roll++) {
			final int r = roll;
			MycophantAttack close = MycophantControllerRules.choose(1, 2.0, a -> 0, sum -> r % sum);
			assertNotEquals(MycophantAttack.SIPHON_FEED, close);
			MycophantAttack far = MycophantControllerRules.choose(1, 14.0, a -> 0, sum -> r % sum);
			assertTrue(Set.of(MycophantAttack.RUSHING_EMBRACE, MycophantAttack.BROOD_CALL, MycophantAttack.NECTAR_SURGE)
					.contains(far), far.name());
			MycophantAttack farTwo = MycophantControllerRules.choose(2, 14.0, a -> 0, sum -> r % sum);
			assertTrue(Set.of(MycophantAttack.FUSION_EMBRACE, MycophantAttack.BROOD_CALL, MycophantAttack.NECTAR_SURGE)
					.contains(farTwo), farTwo.name());
		}
		assertEquals(4.0, MycophantControllerRules.minRange(MycophantAttack.SIPHON_FEED));
		assertEquals(10.0, MycophantControllerRules.maxRange(MycophantAttack.SIPHON_FEED));
		assertEquals(5.0, MycophantControllerRules.minRange(MycophantAttack.RUSHING_EMBRACE));
		assertEquals(16.0, MycophantControllerRules.maxRange(MycophantAttack.RUSHING_EMBRACE));
	}

	@Test
	void cooldownBlocksRepeats() {
		for (int roll = 0; roll < 100; roll++) {
			final int r = roll;
			MycophantAttack chosen = MycophantControllerRules.choose(1, 6.0,
					a -> a == MycophantAttack.RUSHING_EMBRACE ? 20 : 0, sum -> r % sum);
			assertNotEquals(MycophantAttack.RUSHING_EMBRACE, chosen);
		}
		assertEquals(MycophantAttack.NONE, MycophantControllerRules.choose(1, 6.0, a -> 5, sum -> 0));
	}

	@Test
	void splitQueuesUntilRecovery() {
		assertEquals(MycophantControllerRules.Interrupt.STAGGER, MycophantControllerRules.interrupt(BossMoveTimeline.Stage.WINDUP));
		assertEquals(MycophantControllerRules.Interrupt.QUEUE, MycophantControllerRules.interrupt(BossMoveTimeline.Stage.ACTIVE));
		assertEquals(MycophantControllerRules.Interrupt.QUEUE, MycophantControllerRules.interrupt(BossMoveTimeline.Stage.RECOVERY));
	}

	@Test
	void staggerValues() {
		assertEquals(16, MycophantControllerRules.staggerTicks());
		assertEquals(30, MycophantControllerRules.griefTicks());
		assertEquals(40, MycophantControllerRules.breakStaggerTicks());
	}

	@Test
	void turnRatesFasterInPhaseTwoAndWindup() {
		assertEquals(6.0F, MycophantControllerRules.turnRateDegrees(1, true));
		assertEquals(3.0F, MycophantControllerRules.turnRateDegrees(1, false));
		assertEquals(8.0F, MycophantControllerRules.turnRateDegrees(2, true));
		assertEquals(4.0F, MycophantControllerRules.turnRateDegrees(2, false));
	}

	@Test
	void aHoldStopsTheLungeAndAnEmbraceCatchesOnce() {
		assertTrue(MycophantControllerRules.mayLunge(false, false));
		assertFalse(MycophantControllerRules.mayLunge(true, false));
		assertFalse(MycophantControllerRules.mayLunge(false, true));
		assertTrue(MycophantControllerRules.mayCatch(false, false));
		assertFalse(MycophantControllerRules.mayCatch(true, false));
		assertFalse(MycophantControllerRules.mayCatch(false, true));
	}

	@Test
	void theAimLocksExactlyOnce() {
		assertTrue(MycophantControllerRules.shouldCommit(24, 24, false));
		assertFalse(MycophantControllerRules.shouldCommit(24, 24, true));
		assertFalse(MycophantControllerRules.shouldCommit(23, 24, false));
	}
}
