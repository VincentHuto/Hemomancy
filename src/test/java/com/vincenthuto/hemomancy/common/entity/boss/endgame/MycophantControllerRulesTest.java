package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.entity.boss.BossMoveTimeline;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class MycophantControllerRulesTest {
	private static void assertTimeline(MycophantAttack attack, int phase, int windup, int active, int recovery) {
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase);
		assertEquals(new BossMoveTimeline(windup, active, recovery), timeline, attack + " phase " + phase);
	}

	private static void assertTimeline(MycophantAttack attack, int phase, int variant, int windup, int active,
			int recovery) {
		BossMoveTimeline timeline = MycophantControllerRules.timeline(attack, phase, variant);
		assertEquals(new BossMoveTimeline(windup, active, recovery), timeline,
				attack + " phase " + phase + " variant " + variant);
	}

	@Test
	void timelinesMatchSpecStartValues() {
		assertTimeline(MycophantAttack.RUSHING_EMBRACE, 1, 24, 10, 26);
		assertTimeline(MycophantAttack.SIPHON_FEED, 1, 20, 60, 16);
		assertTimeline(MycophantAttack.PLUCK, 1, 14, 6, 16);
		assertTimeline(MycophantAttack.BROOD_CALL, 1, 30, 10, 30);
		assertTimeline(MycophantAttack.NECTAR_SURGE, 1, 30, 4, 20);
		assertTimeline(MycophantAttack.FUSION_EMBRACE, 2, 22, 12, 26);
		assertTimeline(MycophantAttack.WING_GUARD, 2, 8, 60, 10);
		assertTimeline(MycophantAttack.WING_SWEEP, 2, 18, 6, 20);
		assertTimeline(MycophantAttack.PULP_BLOOM, 2, 26, 8, 24);
		assertTimeline(MycophantAttack.RUSHING_EMBRACE, 2, 18, 10, 22);
	}

	@Test
	void newMoveTimelines() {
		assertTimeline(MycophantAttack.RAKE_STRING, 1, 0, 14, 16, 18);
		assertTimeline(MycophantAttack.RAKE_STRING, 1, 1, 14, 30, 22);
		assertTimeline(MycophantAttack.RAKE_STRING, 2, 1, 12, 28, 20);
		assertTimeline(MycophantAttack.RAKE_STRING, 2, 2, 12, 34, 22);
		assertTimeline(MycophantAttack.RAKE_STRING, 1, 14, 16, 18);
		assertTimeline(MycophantAttack.BROOD_SETTLE, 1, 18, 6, 40);
		assertTimeline(MycophantAttack.BROOD_SETTLE, 2, 16, 6, 36);
		assertTimeline(MycophantAttack.CLAW_PIN, 1, 14, 4, 16);
		assertTimeline(MycophantAttack.CLAW_PIN, 2, 12, 4, 14);
		assertTimeline(MycophantAttack.TAIL_LASH, 1, 0, 12, 6, 18);
		assertTimeline(MycophantAttack.TAIL_LASH, 2, 1, 10, 16, 18);
		assertTimeline(MycophantAttack.VENT_BLOOM, 1, 16, 4, 18);
		assertTimeline(MycophantAttack.VENT_BLOOM, 2, 14, 4, 16);
		assertTimeline(MycophantAttack.HAND_SWEEP, 1, 16, 6, 20);
		assertTimeline(MycophantAttack.HAND_SWEEP, 2, 14, 6, 18);
		assertTimeline(MycophantAttack.SIPHON_SPIT, 1, 18, 12, 18);
		assertTimeline(MycophantAttack.SIPHON_SPIT, 2, 16, 14, 16);
		assertTimeline(MycophantAttack.SPORE_MORTAR, 1, 22, 10, 22);
		assertTimeline(MycophantAttack.SPORE_MORTAR, 2, 20, 10, 20);
		assertTimeline(MycophantAttack.NURSING, 1, 12, 80, 16);
		assertTimeline(MycophantAttack.NURSING, 2, 12, 80, 16);
		assertTimeline(MycophantAttack.HYPHAE_LANCES, 2, 18, 24, 20);
		assertTimeline(MycophantAttack.LATTICE_NET, 2, 16, 6, 24);
		assertTimeline(MycophantAttack.GAZE, 2, 24, 40, 20);
		assertTimeline(MycophantAttack.LAST_BROOD, 2, 30, 160, 0);
	}

	@Test
	void cooldownValues() {
		int[][] expected = {
				{MycophantAttack.RAKE_STRING.ordinal(), 50, 40},
				{MycophantAttack.BROOD_SETTLE.ordinal(), 120, 100},
				{MycophantAttack.CLAW_PIN.ordinal(), 140, 120},
				{MycophantAttack.TAIL_LASH.ordinal(), 60, 50},
				{MycophantAttack.VENT_BLOOM.ordinal(), 200, 170},
				{MycophantAttack.HAND_SWEEP.ordinal(), 80, 70},
				{MycophantAttack.SIPHON_SPIT.ordinal(), 120, 100},
				{MycophantAttack.SPORE_MORTAR.ordinal(), 220, 180},
				{MycophantAttack.NURSING.ordinal(), 300, 260}};
		for (int[] row : expected) {
			MycophantAttack attack = MycophantAttack.byOrdinal(row[0]);
			assertEquals(row[1], MycophantControllerRules.cooldownTicks(attack, 1), attack + " phase 1");
			assertEquals(row[2], MycophantControllerRules.cooldownTicks(attack, 2), attack + " phase 2");
		}
		assertEquals(140, MycophantControllerRules.cooldownTicks(MycophantAttack.HYPHAE_LANCES, 2));
		assertEquals(260, MycophantControllerRules.cooldownTicks(MycophantAttack.LATTICE_NET, 2));
		assertEquals(320, MycophantControllerRules.cooldownTicks(MycophantAttack.GAZE, 2));
		assertEquals(120, MycophantControllerRules.cooldownTicks(MycophantAttack.RUSHING_EMBRACE, 1));
		assertEquals(100, MycophantControllerRules.cooldownTicks(MycophantAttack.FUSION_EMBRACE, 2));
	}

	@Test
	void phaseGating() {
		for (MycophantAttack attack : EnumSet.of(MycophantAttack.FUSION_EMBRACE, MycophantAttack.WING_GUARD,
				MycophantAttack.WING_SWEEP, MycophantAttack.PULP_BLOOM, MycophantAttack.HYPHAE_LANCES,
				MycophantAttack.LATTICE_NET, MycophantAttack.GAZE, MycophantAttack.LAST_BROOD)) {
			assertFalse(MycophantControllerRules.allowed(attack, 1), attack.name());
			assertTrue(MycophantControllerRules.allowed(attack, 2), attack.name());
		}
		assertTrue(MycophantControllerRules.allowed(MycophantAttack.RUSHING_EMBRACE, 1));
		assertFalse(MycophantControllerRules.allowed(MycophantAttack.RUSHING_EMBRACE, 2));
		for (MycophantAttack attack : EnumSet.of(MycophantAttack.SIPHON_FEED, MycophantAttack.PLUCK,
				MycophantAttack.RAKE_STRING, MycophantAttack.BROOD_CALL, MycophantAttack.NECTAR_SURGE,
				MycophantAttack.BROOD_SETTLE, MycophantAttack.CLAW_PIN, MycophantAttack.TAIL_LASH,
				MycophantAttack.VENT_BLOOM, MycophantAttack.HAND_SWEEP, MycophantAttack.SIPHON_SPIT,
				MycophantAttack.SPORE_MORTAR, MycophantAttack.NURSING)) {
			assertTrue(MycophantControllerRules.allowed(attack, 1), attack.name());
			assertTrue(MycophantControllerRules.allowed(attack, 2), attack.name());
		}
		assertFalse(MycophantControllerRules.allowed(MycophantAttack.NONE, 1));
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

	@Test
	void theLastBroodIsNeverCutByAStagger() {
		assertTrue(MycophantControllerRules.queuesInterrupts(MycophantAttack.LAST_BROOD));
		for (MycophantAttack attack : MycophantAttack.values())
			if (attack != MycophantAttack.LAST_BROOD) assertFalse(MycophantControllerRules.queuesInterrupts(attack), attack.name());
	}
}
