package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class VesperMountAttackRulesTest {
	@Test
	void aneurysmStagesHonorEveryAuthoredBoundary() {
		assertEquals(VesperMountAttackRules.AneurysmStage.BRACE, VesperMountAttackRules.aneurysmStage(0));
		assertEquals(VesperMountAttackRules.AneurysmStage.BRACE, VesperMountAttackRules.aneurysmStage(23));
		assertEquals(VesperMountAttackRules.AneurysmStage.ERUPTION, VesperMountAttackRules.aneurysmStage(24));
		assertEquals(VesperMountAttackRules.AneurysmStage.EXPOSED, VesperMountAttackRules.aneurysmStage(25));
		assertEquals(VesperMountAttackRules.AneurysmStage.EXPOSED, VesperMountAttackRules.aneurysmStage(84));
		assertEquals(VesperMountAttackRules.AneurysmStage.REFORM, VesperMountAttackRules.aneurysmStage(85));
		assertEquals(VesperMountAttackRules.AneurysmStage.REFORM, VesperMountAttackRules.aneurysmStage(104));
		assertEquals(VesperMountAttackRules.AneurysmStage.RECOVERY, VesperMountAttackRules.aneurysmStage(105));
		assertEquals(VesperMountAttackRules.AneurysmStage.RECOVERY, VesperMountAttackRules.aneurysmStage(119));
		assertEquals(VesperMountAttackRules.AneurysmStage.COMPLETE, VesperMountAttackRules.aneurysmStage(120));
	}

	@Test
	void carapaceVulnerabilityLastsThroughReformAndEndsAtRecovery() {
		assertEquals(1.0F, VesperMountAttackRules.carapaceDamageMultiplier(24), 0.001F);
		assertEquals(1.5F, VesperMountAttackRules.carapaceDamageMultiplier(25), 0.001F);
		assertEquals(1.5F, VesperMountAttackRules.carapaceDamageMultiplier(104), 0.001F);
		assertEquals(1.0F, VesperMountAttackRules.carapaceDamageMultiplier(105), 0.001F);
		assertTrue(VesperMountAttackRules.isCarapaceExposed(85));
		assertFalse(VesperMountAttackRules.isCarapaceExposed(105));
	}

	@Test
	void aneurysmCooldownCountsDownAndRequiresAllMountStatesClear() {
		assertEquals(240, VesperMountAttackRules.startAneurysmCooldown());
		assertEquals(239, VesperMountAttackRules.tickCooldown(240));
		assertEquals(0, VesperMountAttackRules.tickCooldown(0));
		assertTrue(VesperMountAttackRules.mayStartAneurysm(0, false, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayStartAneurysm(1, false, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayStartAneurysm(0, true, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayStartAneurysm(0, false, true, false, false, false));
		assertFalse(VesperMountAttackRules.mayStartAneurysm(0, false, false, true, false, false));
		assertFalse(VesperMountAttackRules.mayStartAneurysm(0, false, false, false, true, false));
		assertFalse(VesperMountAttackRules.mayStartAneurysm(0, false, false, false, false, true));
	}

	@Test
	void combinedSelectorPreservesLegacyBeatsAndNeverRepeatsAneurysm() {
		assertEquals(VesperPhaseOneAttack.CARAPACE_ANEURYSM,
				VesperMountAttackRules.selectAttack(0, VesperPhaseOneAttack.IDLE, 0, false, false));
		assertEquals(VesperPhaseOneAttack.ROYAL_SCUTTLE,
				VesperMountAttackRules.selectAttack(0, VesperPhaseOneAttack.CARAPACE_ANEURYSM, 0, false, false));
		assertEquals(VesperPhaseOneAttack.ROYAL_SCUTTLE,
				VesperMountAttackRules.selectAttack(0, VesperPhaseOneAttack.IDLE, 20, false, false));
		assertEquals(VesperPhaseOneAttack.GRAB_IMPALEMENT,
				VesperMountAttackRules.selectAttack(1, VesperPhaseOneAttack.ROYAL_SCUTTLE, 0, true, false));
		assertEquals(VesperPhaseOneAttack.PINCER_VICE,
				VesperMountAttackRules.selectAttack(1, VesperPhaseOneAttack.ROYAL_SCUTTLE, 0, false, false));
		assertEquals(VesperPhaseOneAttack.IDLE,
				VesperMountAttackRules.selectAttack(2, VesperPhaseOneAttack.ROYAL_SCUTTLE, 0, true, true));
	}

	@Test
	void grabEligibilityRejectsEveryUnsafeTargetState() {
		assertTrue(VesperMountAttackRules.mayGrab(25.0D, true, true, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayGrab(25.01D, true, true, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayGrab(4.0D, false, true, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayGrab(4.0D, true, false, false, false, false, false));
		assertFalse(VesperMountAttackRules.mayGrab(4.0D, true, true, true, false, false, false));
		assertFalse(VesperMountAttackRules.mayGrab(4.0D, true, true, false, true, false, false));
		assertFalse(VesperMountAttackRules.mayGrab(4.0D, true, true, false, false, true, false));
		assertFalse(VesperMountAttackRules.mayGrab(4.0D, true, true, false, false, false, true));
	}

	@Test
	void grabStagesAndSingleHitMasksMatchTheCombatScript() {
		assertEquals(VesperMountAttackRules.GrabStage.TELEGRAPH, VesperMountAttackRules.grabStage(14, false));
		assertEquals(VesperMountAttackRules.GrabStage.LUNGE, VesperMountAttackRules.grabStage(15, false));
		assertEquals(VesperMountAttackRules.GrabStage.RECOVERY, VesperMountAttackRules.grabStage(21, false));
		assertEquals(VesperMountAttackRules.GrabStage.LIFT, VesperMountAttackRules.grabStage(21, true));
		assertEquals(VesperMountAttackRules.GrabStage.BITE, VesperMountAttackRules.grabStage(30, true));
		assertEquals(VesperMountAttackRules.GrabStage.TAIL_WINDUP, VesperMountAttackRules.grabStage(41, true));
		assertEquals(VesperMountAttackRules.GrabStage.IMPALE, VesperMountAttackRules.grabStage(42, true));
		assertEquals(VesperMountAttackRules.GrabStage.RELEASE, VesperMountAttackRules.grabStage(50, true));
		assertEquals(VesperMountAttackRules.GrabStage.RECOVERY, VesperMountAttackRules.grabStage(51, true));
		assertEquals(VesperMountAttackRules.GrabStage.COMPLETE, VesperMountAttackRules.grabStage(70, true));
		int mask = VesperMountAttackRules.markApplied(0, VesperMountAttackRules.Hit.BITE);
		assertTrue(VesperMountAttackRules.shouldApply(mask, VesperMountAttackRules.Hit.IMPALE));
		assertFalse(VesperMountAttackRules.shouldApply(mask, VesperMountAttackRules.Hit.BITE));
	}

	@Test
	void restraintReconciliationFailsClosedForEveryCleanupCondition() {
		assertFalse(VesperMountAttackRules.shouldReleaseRestraint(true, true, true, true, 64.0D, false, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(false, true, true, true, 1.0D, false, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, false, true, true, 1.0D, false, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, true, false, true, 1.0D, false, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, true, true, false, 1.0D, false, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, true, true, true, 64.01D, false, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, true, true, true, 1.0D, true, false, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, true, true, true, 1.0D, false, true, true));
		assertTrue(VesperMountAttackRules.shouldReleaseRestraint(true, true, true, true, 1.0D, false, false, false));
	}

	@Test
	void liftAndThrowGeometryAreSmoothAndExact() {
		assertEquals(0.0D, VesperMountAttackRules.liftProgress(20), 0.0001D);
		assertTrue(VesperMountAttackRules.liftProgress(25) > 0.0D);
		assertEquals(1.0D, VesperMountAttackRules.liftProgress(29), 0.0001D);
		assertEquals(2.5D, VesperMountAttackRules.RELEASE_HORIZONTAL_SPEED, 0.0001D);
		assertEquals(0.45D, VesperMountAttackRules.RELEASE_UPWARD_SPEED, 0.0001D);
		assertEquals(160, VesperMountAttackRules.BLOOD_LOSS_TICKS);
		assertEquals(100, VesperMountAttackRules.POISON_TICKS);
	}
}
