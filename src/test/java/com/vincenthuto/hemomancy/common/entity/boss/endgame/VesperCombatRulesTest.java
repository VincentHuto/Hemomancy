package com.vincenthuto.hemomancy.common.entity.boss.endgame;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;

import static com.vincenthuto.hemomancy.common.entity.boss.endgame.VesperWeaponAction.*;
import static org.junit.jupiter.api.Assertions.*;

final class VesperCombatRulesTest {
	@Test
	void phaseOneHealthLocksExposeThreeThroneAnchorsInOrder() {
		assertEquals(0, VesperCombatRules.lockedAnchorIndex(374.4F, 520.0F, 0));
		assertEquals(-1, VesperCombatRules.lockedAnchorIndex(374.4F, 520.0F, 1));
		assertEquals(1, VesperCombatRules.lockedAnchorIndex(218.4F, 520.0F, 1));
		assertEquals(2, VesperCombatRules.lockedAnchorIndex(62.4F, 520.0F, 3));
		assertEquals(-1, VesperCombatRules.lockedAnchorIndex(1.0F, 520.0F, 7));
	}

	@Test
	void phaseOneDamageCannotCrossTheNextUnbrokenAnchorThreshold() {
		assertEquals(374.4F, VesperCombatRules.healthFloor(520.0F, 0), 0.001F);
		assertEquals(218.4F, VesperCombatRules.healthFloor(520.0F, 1), 0.001F);
		assertEquals(62.4F, VesperCombatRules.healthFloor(520.0F, 3), 0.001F);
		assertEquals(0.0F, VesperCombatRules.healthFloor(520.0F, 7), 0.001F);
	}

	@Test
	void aTemperTourUsesEveryTemperExactlyOnceBeforeRepeating() {
		List<EnumBloodTemper> first = VesperCombatRules.temperTour(918273L);
		List<EnumBloodTemper> sameSeed = VesperCombatRules.temperTour(918273L);
		List<EnumBloodTemper> differentSeed = VesperCombatRules.temperTour(918274L);

		assertEquals(EnumBloodTemper.values().length, first.size());
		assertEquals(EnumBloodTemper.values().length, new HashSet<>(first).size());
		assertEquals(first, sameSeed);
		assertNotEquals(first, differentSeed);
	}

	@Test
	void everyTemperHasTheApprovedWeaponAndManipulationProfile() {
		assertProfile(EnumBloodTemper.ANIMUS, "blade", "blood_rush", "blood_aneurysm");
		assertProfile(EnumBloodTemper.MORTEM, "axe", "exsanguinate", "grave_debt");
		assertProfile(EnumBloodTemper.LUX, "spear", "prismatic_reproof", "hematic_flare");
		assertProfile(EnumBloodTemper.TENEBRIS, "claws", "umbral_step", "gloam_laceration");
		assertProfile(EnumBloodTemper.DUCTILIS, "crossbow", "conductive_mark", "synaptic_jolt");
		assertProfile(EnumBloodTemper.FLAMMEUS, "torch", "sanguine_ignition", "scalding_updraft");
		assertProfile(EnumBloodTemper.CONGEATIO, "flail", "glacial_grasp", "glacial_rampart");
		assertProfile(EnumBloodTemper.FERRIC, "staff", "sanguine_magnetism", "iron_retort");
	}

	@Test
	void stanceCadenceEscalatesAtSixtyAndTwentyFivePercentHealth() {
		assertEquals(240, VesperCombatRules.stanceDuration(640.0F, 640.0F));
		assertEquals(180, VesperCombatRules.stanceDuration(384.0F, 640.0F));
		assertEquals(140, VesperCombatRules.stanceDuration(160.0F, 640.0F));
		assertTrue(VesperCombatRules.mayUseSecondary(384.0F, 640.0F));
	}

	@Test
	void phaseOneCyclesPrimalAttacksAndUsesPuppetsAsItsFifthBeat() {
		assertEquals(VesperPhaseOneAttack.ROYAL_SCUTTLE, VesperCombatRules.phaseOneAttack(0));
		assertEquals(VesperPhaseOneAttack.PINCER_VICE, VesperCombatRules.phaseOneAttack(1));
		assertEquals(VesperPhaseOneAttack.STINGER_SCRIPT, VesperCombatRules.phaseOneAttack(2));
		assertEquals(VesperPhaseOneAttack.BROOD_TRAMPLE, VesperCombatRules.phaseOneAttack(3));
		assertEquals(VesperPhaseOneAttack.PUPPET_MUSTER, VesperCombatRules.phaseOneAttack(4));
		assertEquals(VesperPhaseOneAttack.ROYAL_SCUTTLE, VesperCombatRules.phaseOneAttack(5));
	}

	@Test
	void anchorHitFlashLastsExactlyFourTicks() {
		assertEquals(0.0F, VesperCombatRules.anchorFlashStrength(0), 0.001F);
		assertEquals(0.25F, VesperCombatRules.anchorFlashStrength(1), 0.001F);
		assertEquals(1.0F, VesperCombatRules.anchorFlashStrength(4), 0.001F);
		assertEquals(1.0F, VesperCombatRules.anchorFlashStrength(8), 0.001F);
	}

	@Test
	void onlyTheExposedThroneAnchorHasAHitbox() {
		assertEquals(0.0F, VesperCombatRules.anchorHitboxScale(0, 1), 0.001F);
		assertEquals(1.0F, VesperCombatRules.anchorHitboxScale(1, 1), 0.001F);
		assertEquals(0.0F, VesperCombatRules.anchorHitboxScale(2, 1), 0.001F);
		assertEquals(0.0F, VesperCombatRules.anchorHitboxScale(0, -1), 0.001F);
	}

	@Test
	void phaseTwoUsesAThirtyTickMorphThenPrimaryAndSecondaryBeats() {
		assertEquals(40, VesperCombatRules.primaryAttackTick());
		assertEquals(118, VesperCombatRules.secondaryAttackTick());
		assertEquals(VesperCombatRules.temperTour(77L).get(0), VesperCombatRules.temperAt(77L, 0));
		assertEquals(VesperCombatRules.temperTour(77L).get(0), VesperCombatRules.temperAt(77L, 8));
	}

	@Test
	void defeatRecoilFlowsIntoAKneelBeforeAbsorptionUnlocks() {
		assertEquals(0.0F, VesperCombatRules.defeatRecoilProgress(0.0F), 0.001F);
		assertEquals(1.0F, VesperCombatRules.defeatRecoilProgress(3.0F), 0.001F);
		assertEquals(0.0F, VesperCombatRules.defeatRecoilProgress(6.0F), 0.001F);
		assertEquals(0.0F, VesperCombatRules.defeatKneelProgress(6.0F), 0.001F);
		assertEquals(0.5F, VesperCombatRules.defeatKneelProgress(19.0F), 0.001F);
		assertEquals(1.0F, VesperCombatRules.defeatKneelProgress(32.0F), 0.001F);
		assertTrue(!VesperCombatRules.isDefeatAnimationComplete(39));
		assertTrue(VesperCombatRules.isDefeatAnimationComplete(40));
	}

	@Test
	void weaponsDissolveBeforeSigilsFizzleOutInReverseOrder() {
		assertEquals(0.0F, VesperCombatRules.weaponDissolveProgress(0.0F), 0.001F);
		assertEquals(0.5F, VesperCombatRules.weaponDissolveProgress(14.0F), 0.001F);
		assertEquals(1.0F, VesperCombatRules.weaponDissolveProgress(28.0F), 0.001F);
		assertEquals(0.0F, VesperCombatRules.sigilFizzleProgress(4.0F, 7), 0.001F);
		assertEquals(1.0F, VesperCombatRules.sigilFizzleProgress(12.0F, 7), 0.001F);
		assertEquals(0.0F, VesperCombatRules.sigilFizzleProgress(31.0F, 0), 0.001F);
		assertEquals(0.0F, VesperCombatRules.sigilFizzleProgress(32.0F, 0), 0.001F);
		assertEquals(1.0F, VesperCombatRules.sigilFizzleProgress(40.0F, 0), 0.001F);
	}

	@Test
	void defeatAbsorptionRequiresAFullChannel() {
		assertEquals(100.0F, VesperCombatRules.advanceDefeatAbsorption(98.5F, 4.0F), 0.001F);
		assertTrue(VesperCombatRules.isDefeatAbsorptionComplete(100.0F));
	}

	@Test
	void hoodRemovalTriggersOnceAtHalfHealthAndNeverReversesAfterHealing() {
		assertFalse(VesperEveningStarPresentationRules.shouldBeginHoodRemoval(false, 321.0F, 640.0F));
		assertTrue(VesperEveningStarPresentationRules.shouldBeginHoodRemoval(false, 320.0F, 640.0F));
		assertFalse(VesperEveningStarPresentationRules.shouldBeginHoodRemoval(true, 100.0F, 640.0F));
		assertTrue(VesperEveningStarPresentationRules.isHoodRemovalActive(true, 0));
		assertTrue(VesperEveningStarPresentationRules.isHoodRemovalActive(true, 29));
		assertFalse(VesperEveningStarPresentationRules.isHoodRemovalActive(true, 30));
		assertTrue(VesperEveningStarPresentationRules.isHoodVisible(true, 29));
		assertFalse(VesperEveningStarPresentationRules.isHoodVisible(true, 30));
	}

	@Test
	void redLinesStayDimAndDisappearWhileVesperIsShamed() {
		assertFalse(VesperEveningStarPresentationRules.shouldRenderRedLines(321.0F, 640.0F, false));
		assertTrue(VesperEveningStarPresentationRules.shouldRenderRedLines(320.0F, 640.0F, false));
		assertFalse(VesperEveningStarPresentationRules.shouldRenderRedLines(100.0F, 640.0F, true));
		for (int tick = 0; tick < 240; tick++) {
			float alpha = VesperEveningStarPresentationRules.redLineAlpha(tick);
			assertTrue(alpha >= 0.22F && alpha <= 0.38F, "alpha=" + alpha);
		}
	}

	@Test
	void bloodAbsorptionSmoothlyShrinksLowersAndDissolvesTheKneelingBody() {
		assertEquals(1.0F, VesperEveningStarPresentationRules.absorptionScale(0.0F), 0.001F);
		assertEquals(0.54F, VesperEveningStarPresentationRules.absorptionScale(50.0F), 0.001F);
		assertEquals(0.08F, VesperEveningStarPresentationRules.absorptionScale(100.0F), 0.001F);
		assertEquals(0.0F, VesperEveningStarPresentationRules.absorptionDissolve(0.0F), 0.001F);
		assertEquals(0.5F, VesperEveningStarPresentationRules.absorptionDissolve(50.0F), 0.001F);
		assertEquals(1.0F, VesperEveningStarPresentationRules.absorptionDissolve(100.0F), 0.001F);
		assertEquals(0.0F, VesperEveningStarPresentationRules.absorptionLowering(0.0F), 0.001F);
		assertTrue(VesperEveningStarPresentationRules.absorptionLowering(100.0F) > 0.7F);
		assertEquals(0.0F, VesperEveningStarPresentationRules.finalCollapseProgress(95.0F), 0.001F);
		assertEquals(1.0F, VesperEveningStarPresentationRules.finalCollapseProgress(100.0F), 0.001F);
		assertFalse(VesperEveningStarPresentationRules.isFinalCollapseComplete(4));
		assertTrue(VesperEveningStarPresentationRules.isFinalCollapseComplete(5));
	}

	@Test
	void everyTemperOwnsAWeaponSpecificCoreAndAdvancedAction() {
		assertEquals(ICHIMONJI, VesperWeaponCombatRules.coreAction(EnumBloodTemper.ANIMUS));
		assertEquals(CROSSCUT, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.ANIMUS));
		assertEquals(LEAPING_CLEAVE, VesperWeaponCombatRules.coreAction(EnumBloodTemper.MORTEM));
		assertEquals(REAPER_SWEEP, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.MORTEM));
		assertEquals(SKY_LANCE, VesperWeaponCombatRules.coreAction(EnumBloodTemper.LUX));
		assertEquals(LANCE_FLURRY, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.LUX));
		assertEquals(TWIN_REND, VesperWeaponCombatRules.coreAction(EnumBloodTemper.TENEBRIS));
		assertEquals(PREDATOR_POUNCE, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.TENEBRIS));
		assertEquals(CONDUCTIVE_VOLLEY, VesperWeaponCombatRules.coreAction(EnumBloodTemper.DUCTILIS));
		assertEquals(STORM_LOCK, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.DUCTILIS));
		assertEquals(BRANDING_THRUSTS, VesperWeaponCombatRules.coreAction(EnumBloodTemper.FLAMMEUS));
		assertEquals(FLAMMEUS_CONCENTRATION, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.FLAMMEUS));
		assertEquals(CHAIN_SWEEP, VesperWeaponCombatRules.coreAction(EnumBloodTemper.CONGEATIO));
		assertEquals(HOOK_AND_CRUSH, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.CONGEATIO));
		assertEquals(MAGNETIC_AXIS, VesperWeaponCombatRules.coreAction(EnumBloodTemper.FERRIC));
		assertEquals(IRON_RETORT, VesperWeaponCombatRules.advancedAction(EnumBloodTemper.FERRIC));
	}

	@Test
	void actionsHaveReadableWindupsAndNeverImmediatelyRepeat() {
		for (VesperWeaponAction action : VesperWeaponAction.values()) {
			if (action != NONE) {
				assertTrue(action.impactTick() >= 10, action.name());
				int recovery = action.durationTicks() - action.lastImpactTick();
				assertTrue(recovery >= 8 && recovery <= 14, action.name() + " recovery=" + recovery);
				for (int i = 0; i < action.contactCount(); i++) {
					assertTrue(action.contactTick(i) >= action.impactTick(), action.name());
					assertTrue(action.contactTick(i) <= action.lastImpactTick(), action.name());
				}
			}
		}
		assertEquals(STORM_LOCK, VesperWeaponCombatRules.selectAction(
				EnumBloodTemper.DUCTILIS, true, CONDUCTIVE_VOLLEY, 5.0D));
		assertEquals(CONDUCTIVE_VOLLEY, VesperWeaponCombatRules.selectAction(
				EnumBloodTemper.DUCTILIS, true, STORM_LOCK, 18.0D));
		assertNotEquals(REAPER_SWEEP, VesperWeaponCombatRules.selectAction(
				EnumBloodTemper.MORTEM, true, REAPER_SWEEP, 4.0D));
	}

	@Test
	void eachFormDefinesItsOwnPreferredRangeAndLowHealthRecovery() {
		assertEquals(new VesperWeaponCombatRules.RangeBand(3.0D, 6.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.ANIMUS));
		assertEquals(new VesperWeaponCombatRules.RangeBand(3.0D, 7.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.MORTEM));
		assertEquals(new VesperWeaponCombatRules.RangeBand(7.0D, 14.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.LUX));
		assertEquals(new VesperWeaponCombatRules.RangeBand(2.0D, 5.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.TENEBRIS));
		assertEquals(new VesperWeaponCombatRules.RangeBand(12.0D, 20.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.DUCTILIS));
		assertEquals(new VesperWeaponCombatRules.RangeBand(2.0D, 5.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.FLAMMEUS));
		assertEquals(new VesperWeaponCombatRules.RangeBand(5.0D, 9.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.CONGEATIO));
		assertEquals(new VesperWeaponCombatRules.RangeBand(8.0D, 14.0D),
				VesperWeaponCombatRules.rangeBand(EnumBloodTemper.FERRIC));
		assertEquals(8, VesperWeaponCombatRules.recoveryTicks(10, 0.24F));
		assertEquals(8, VesperWeaponCombatRules.recoveryTicks(10, 0.25F));
	}

	@Test
	void aCommittedActionCanHitEachDamageBeatOnlyOnce() {
		assertTrue(VesperWeaponCombatRules.canApplyHit(0, 2));
		int mask = VesperWeaponCombatRules.recordHit(0, 2);
		assertEquals(4, mask);
		assertTrue(!VesperWeaponCombatRules.canApplyHit(mask, 2));
		assertTrue(VesperWeaponCombatRules.canApplyHit(mask, 1));
	}

	@Test
	void stanceTransitionWaitsUntilActionRecoveryFinishes() {
		assertTrue(!VesperWeaponCombatRules.mayAdvanceStance(240, 240, ICHIMONJI));
		assertTrue(VesperWeaponCombatRules.mayAdvanceStance(240, 240, NONE));
		assertTrue(!VesperWeaponCombatRules.mayAdvanceStance(239, 240, NONE));
	}

	@Test
	void selectionAccountsForVisibilityElevationAndAttackAngle() {
		assertEquals(NONE, VesperWeaponCombatRules.selectAction(
				EnumBloodTemper.LUX, true, NONE, 10.0D, 0.0D, 0.0D, false));
		assertEquals(SKY_LANCE, VesperWeaponCombatRules.selectAction(
				EnumBloodTemper.LUX, true, NONE, 10.0D, 20.0D, 4.0D, true));
		assertEquals(TWIN_REND, VesperWeaponCombatRules.selectAction(
				EnumBloodTemper.TENEBRIS, true, NONE, 4.0D, 110.0D, 0.0D, true));
	}

	@Test
	void committedHitGeometryRejectsTargetsOutsideTheMarkedLaneOrArc() {
		assertTrue(VesperWeaponCombatRules.withinLane(4.0D, 0.8D, 6.0D, 1.1D));
		assertTrue(!VesperWeaponCombatRules.withinLane(4.0D, 1.2D, 6.0D, 1.1D));
		assertTrue(!VesperWeaponCombatRules.withinLane(-0.1D, 0.0D, 6.0D, 1.1D));
		assertTrue(VesperWeaponCombatRules.withinArc(4.0D, 25.0D, 5.0D, 130.0D));
		assertTrue(!VesperWeaponCombatRules.withinArc(4.0D, 70.0D, 5.0D, 130.0D));
	}

	private static void assertProfile(EnumBloodTemper temper, String weapon, String primary, String secondary) {
		VesperCombatRules.StanceProfile profile = VesperCombatRules.profile(temper);
		assertEquals(weapon, profile.weapon());
		assertEquals(primary, profile.primaryManipulation());
		assertEquals(secondary, profile.secondaryManipulation());
	}
}
