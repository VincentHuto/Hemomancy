package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.rite.harbinger.CardinalRiteCancellationGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

final class ActiveCardinalRiteCeremonyTest {
	@Test
	void riteWithoutAuthoredWavesSkipsTheRemovedProfessionPhase() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/lower_tier"), 400, 3, 2,
				true, 0, 4);
		for (int i = 0; i < 4; i++) rite.fillAnchor(i, 50);
		assertTrue(rite.enterInscription());

		assertTrue(rite.sealAltar(false));

		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		assertEquals(0, rite.getTotalWaves());
	}

	@Test
	void savedProfessionPhaseMigratesToCulmination() {
		ActiveCardinalRite rite = rite(4, false, 0);
		CompoundTag saved = rite.serialize();
		saved.putString("Phase", "PROFESSION");

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(saved);

		assertEquals(CardinalRitePhase.CULMINATION, copy.getPhase());
	}

	@Test
	void threeWaveRiteHasExactlyTwoBetweenWaveStillIntervals() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.parse("hemomancy:cardinal_rite/grand"), 1200, 7, 6,
				false, 3, 12);
		for (int i = 0; i < 12; i++) rite.fillAnchor(i, 50);
		assertTrue(rite.enterInscription());
		assertTrue(rite.sealAltar(true));

		rite.completeWave();
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());
		rite.finishStillInterval();
		assertEquals(CardinalRitePhase.ORDEAL, rite.getPhase());
		rite.completeWave();
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());
		rite.finishStillInterval();
		assertEquals(CardinalRitePhase.ORDEAL, rite.getPhase());
		rite.completeWave();
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
	}

	@Test
	void consecrationRequiresEveryAnchorBeforeInscription() {
		ActiveCardinalRite rite = rite(2, false, 3);
		assertEquals(CardinalRitePhase.CONSECRATION, rite.getPhase());
		assertFalse(rite.enterInscription());

		for (int i = 0; i < 4; i++) {
			assertTrue(rite.fillAnchor(i, 50));
		}

		assertEquals(1, rite.completedRings());
		assertEquals(200, rite.getCommittedBloodMl());
		assertTrue(rite.enterInscription());
		assertTrue(rite.sealAltar());
		assertEquals(CardinalRitePhase.ORDEAL, rite.getPhase());
	}

	@Test
	void ordealFlowsThroughStillIntervalAndCulmination() {
		ActiveCardinalRite rite = rite(1, true, 1);
		rite.captureOfferingItinerary(List.of(
				new ActiveCardinalRite.RiteOffering(new BlockPos(2, 0, 0),
						null, true)));
		rite.fillAnchor(0, 50);
		rite.fillAnchor(1, 50);
		rite.fillAnchor(2, 50);
		rite.fillAnchor(3, 50);
		rite.enterInscription();
		rite.sealAltar();
		rite.completeWave();
		assertEquals(CardinalRitePhase.STILL_INTERVAL, rite.getPhase());
		rite.finishStillInterval();
		assertEquals(CardinalRitePhase.OFFERING_PROCESSION, rite.getPhase());
		assertEquals(new BlockPos(2, 0, 0), rite.getCurrentOffering().pos());
		assertTrue(rite.absorbCurrentOffering());
		assertTrue(rite.isReturningFromOfferings());
		assertTrue(rite.finishOfferingProcession());
		assertEquals(CardinalRitePhase.CULMINATION, rite.getPhase());
		rite.markComplete();
		assertTrue(rite.isComplete());
	}

	@Test
	void offeringProcessionVisitsOnlyConsumableOfferingsInCapturedOrder() {
		ActiveCardinalRite rite = rite(1, true, 1);
		BlockPos paperBrazier = new BlockPos(2, 0, 0);
		BlockPos preservedBrazier = new BlockPos(0, 0, 2);
		BlockPos boneBrazier = new BlockPos(-2, 0, 0);
		rite.captureOfferingItinerary(List.of(
				new ActiveCardinalRite.RiteOffering(paperBrazier, null, true),
				new ActiveCardinalRite.RiteOffering(preservedBrazier, null, false),
				new ActiveCardinalRite.RiteOffering(boneBrazier, null, true)));

		assertEquals(List.of(paperBrazier, boneBrazier),
				rite.getOfferingItinerary().stream()
						.map(ActiveCardinalRite.RiteOffering::pos).toList());
		assertEquals(paperBrazier, rite.getCurrentOffering().pos());
		assertTrue(rite.absorbCurrentOffering());
		assertEquals(boneBrazier, rite.getCurrentOffering().pos());
		assertTrue(rite.absorbCurrentOffering());
		assertTrue(rite.isReturningFromOfferings());
		assertEquals(2, rite.getAbsorbedOfferings().size());
		assertFalse(rite.absorbCurrentOffering(), "an absorbed itinerary cannot be consumed twice");
	}

	@Test
	void timedWaitingPhasesAdvanceOverallProgressContinuously() {
		ActiveCardinalRite rite = rite(1, true, 1);
		for (int anchor = 0; anchor < 4; anchor++) rite.fillAnchor(anchor, 50);
		assertEquals(0.25D, rite.getProgress(100), 0.0001D, "consecration occupies first quarter");
		rite.enterInscription();
		rite.sealAltar();
		rite.completeWave();

		assertEquals(0.75D, rite.getProgress(100), 0.0001D, "still interval begins after ordeal");
		for (int tick = 0; tick < 50; tick++) rite.tick();
		assertEquals(0.80D, rite.getProgress(100), 0.0001D, "still interval midpoint");
		for (int tick = 0; tick < 50; tick++) rite.tick();
		assertEquals(0.85D, rite.getProgress(100), 0.0001D, "still interval end");

		rite.finishStillInterval();
		assertEquals(0.95D, rite.getProgress(100), 0.0001D, "culmination begins");
		for (int tick = 0; tick < 20; tick++) rite.tick();
		assertEquals(0.9625D, rite.getProgress(100), 0.0001D, "growth midpoint");
		for (int tick = 0; tick < 20; tick++) rite.tick();
		assertEquals(0.975D, rite.getProgress(100), 0.0001D, "merge begins after growth");
	}

	@Test
	void ceremonyStateRoundTripsAndOldStateRemainsLegacy() {
		ActiveCardinalRite rite = rite(5, false, 6);
		UUID ally = UUID.randomUUID();
		rite.fillAnchor(0, 25);
		rite.addInstability(43);
		rite.setSigilProgress("hemomancy:reservoir", 3);
		rite.assignAlly(ally, CardinalRiteAllyRole.WARDEN);
		rite.setSharedPoolOptIn(ally, true);
		rite.carryIchor(30);

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertEquals(CardinalRitePhase.CONSECRATION, copy.getPhase());
		assertEquals(43, copy.getInstability());
		assertEquals(rite.getBrokenInstabilityAnchors(), copy.getBrokenInstabilityAnchors());
		assertEquals(CardinalRiteInstability.STRAINED, copy.getInstabilityBand());
		assertEquals(25, copy.getAnchorBloodMl()[0]);
		assertEquals(3, copy.getSigilProgress().get("hemomancy:reservoir"));
		assertEquals(CardinalRiteAllyRole.WARDEN, copy.getAllyRoles().get(ally));
		assertTrue(copy.getSharedPoolOptIns().contains(ally));
		assertEquals(30, copy.getCarriedIchorMl());

		CompoundTag old = new CompoundTag();
		old.putUUID("PlayerUUID", UUID.randomUUID());
		old.putLong("CenterPos", BlockPos.ZERO.asLong());
		old.putString("RecipeId", "hemomancy:cardinal_rite/old");
		old.putInt("TotalTicks", 100);
		old.putInt("RemainingTicks", 40);
		old.putInt("RiteSize", 3);
		assertEquals(CardinalRitePhase.LEGACY, ActiveCardinalRite.deserialize(old).getPhase());
	}

	@Test
	void repairingABrokenAnchorPreservesWhichBoundarySectionWasRestored() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(
				UUID.randomUUID(), BlockPos.ZERO, ResourceLocation.parse("hemomancy:test"),
				200, 5, 2, false, 1, 8);
		rite.addInstability(26);

		assertTrue(rite.getBrokenInstabilityAnchors().contains(0));
		assertTrue(rite.getBrokenInstabilityAnchors().contains(1));
		assertTrue(rite.offerInstabilityRepair(0, 50));

		assertEquals(13, rite.getInstability(), "tier-scaled repair");
		assertFalse(rite.getBrokenInstabilityAnchors().contains(0), "clicked anchor restored");
		assertTrue(rite.getBrokenInstabilityAnchors().contains(1));
	}

	@Test
	void instabilityUsesTheConfiguredOuterToInnerDamagePriority() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(
				UUID.randomUUID(), BlockPos.ZERO, ResourceLocation.parse("hemomancy:test"),
				200, 5, 2, false, 1, 8);
		rite.setInstabilityDamagePriority(new int[] {4, 5, 6, 7, 0, 1, 2, 3});

		rite.addInstability(26);

		assertEquals(java.util.Set.of(4, 5), rite.getBrokenInstabilityAnchors());
		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		copy.addInstability(13);
		assertEquals(java.util.Set.of(4, 5, 6), copy.getBrokenInstabilityAnchors(),
				"priority survives reload");
	}

	@Test
	void loadingAnOlderActiveRiteCanRehomeDamageToTheAuthoredOuterRing() {
		ActiveCardinalRite rite = ActiveCardinalRite.interactive(
				UUID.randomUUID(), BlockPos.ZERO, ResourceLocation.parse("hemomancy:test"),
				200, 5, 2, false, 1, 8);
		rite.addInstability(26);
		assertEquals(java.util.Set.of(0, 1), rite.getBrokenInstabilityAnchors());

		rite.setInstabilityDamagePriority(new int[] {4, 5, 6, 7, 0, 1, 2, 3});

		assertEquals(java.util.Set.of(4, 5), rite.getBrokenInstabilityAnchors());
	}

	@Test
	void awakeningASigilIsIdempotentAndPersistsAcrossReloads() {
		ActiveCardinalRite rite = rite(1, false, 2);

		assertTrue(rite.awakenSigil("hemomancy:reservoir"), "first completion spawns an entity");
		assertFalse(rite.awakenSigil("hemomancy:reservoir"), "repeat completion cannot spawn a duplicate");
		assertTrue(rite.isSigilAwakened("hemomancy:reservoir"));

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertTrue(copy.isSigilAwakened("hemomancy:reservoir"), "awakened state survives world reload");
		assertEquals(1, copy.getAwakenedSigils().size());
	}

	private static ActiveCardinalRite rite(int degree, boolean abbreviated, int waves) {
		return ActiveCardinalRite.interactive(UUID.randomUUID(), BlockPos.ZERO,
				ResourceLocation.fromNamespaceAndPath("hemomancy", "cardinal_rite/test"),
				100, 3, degree, abbreviated, waves);
	}

	@Test
	void capturedFloorTransformAndCompletionGuardPersist() {
		ActiveCardinalRite rite = rite(2, false, 2);
		rite.setMatchedFloor(ResourceLocation.fromNamespaceAndPath("hemomancy", "threshold_grand"),
				net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.UP);
		assertTrue(rite.commitCompletion());
		assertFalse(rite.commitCompletion());

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());
		assertEquals(ResourceLocation.fromNamespaceAndPath("hemomancy", "threshold_grand"),
				copy.getMatchedFloorId());
		assertEquals(net.minecraft.core.Direction.EAST, copy.getFloorForwards());
		assertFalse(copy.commitCompletion(), "completion guard survives reload");
	}

	@Test
	void cancellationRequiresAContinuousPerTickRequest() {
		ActiveCardinalRite rite = rite(2, false, 2);
		assertTrue(rite.requestCancellation(120L));
		assertTrue(rite.tickCancellation(120L));
		assertEquals(1, rite.getCancellationTicks());

		assertFalse(rite.tickCancellation(121L));
		assertEquals(0, rite.getCancellationTicks(), "releasing absorption resets the attempt");
	}

	@Test
	void cancellationRequestSurvivesUntilTheFollowingLevelPostTick() {
		ActiveCardinalRite rite = rite(2, false, 2);
		assertTrue(rite.requestCancellation(120L));
		assertTrue(rite.tickCancellation(121L),
				"item-use requests made after a level callback must be consumed by the next callback");
		assertEquals(1, rite.getCancellationTicks());

		assertFalse(rite.tickCancellation(122L),
				"a consumed request cannot advance the channel for a second tick");
		assertEquals(0, rite.getCancellationTicks());
	}

	@Test
	void restartedCancellationContinuesFromItsCurrentRecoveryProgress() {
		ActiveCardinalRite rite = rite(2, false, 2);
		for (int tick = 0; tick < 40; tick++) {
			assertTrue(rite.requestCancellation(tick));
			assertTrue(rite.tickCancellation(tick));
		}

		assertFalse(rite.tickCancellation(40L), "releasing absorption stops the channel");
		for (int tick = 41; tick <= 60; tick++) {
			assertFalse(rite.tickCancellation(tick));
		}

		assertTrue(rite.requestCancellation(61L));
		assertTrue(rite.tickCancellation(61L));
		assertEquals(31, rite.getCancellationTicks(),
				"restart resumes after half-speed recovery instead of resetting to one tick");
	}

	@Test
	void cancellationCompletesAfterTheConfiguredContinuousDuration() {
		ActiveCardinalRite rite = rite(2, false, 2);
		for (int tick = 0; tick < CardinalRiteCancellationRules.TOTAL_TICKS; tick++) {
			assertTrue(rite.requestCancellation(tick));
			assertTrue(rite.tickCancellation(tick));
		}

		assertTrue(rite.isCancellationComplete());
		assertEquals(CardinalRiteCancellationRules.TOTAL_TICKS, rite.getCancellationTicks());
		assertEquals(CardinalRitePhase.CONSECRATION, rite.getPhase(),
				"cancellation is orthogonal to the ceremony phase");
		assertEquals(0, rite.getPhaseTicks(), "the ceremony stays frozen while cancellation is processed");
	}

	@Test
	void cancellationKeepsOneDaemonStartingPoseUntilTheAttemptResets() {
		ActiveCardinalRite rite = rite(2, false, 2);
		Vec3 first = new Vec3(1.0D, 2.0D, 3.0D);
		Vec3 later = new Vec3(7.0D, 8.0D, 9.0D);

		rite.captureCancellationDaemonStart(first, 2.5F);
		rite.captureCancellationDaemonStart(later, 5.0F);
		assertEquals(first, rite.getCancellationDaemonStartPos());
		assertEquals(2.5F, rite.getCancellationDaemonStartScale());

		rite.resetCancellation();
		rite.captureCancellationDaemonStart(later, 5.0F);
		assertEquals(later, rite.getCancellationDaemonStartPos());
		assertEquals(5.0F, rite.getCancellationDaemonStartScale());
	}

	@Test
	void interruptedCancellationPreservesTheDaemonOriginalPoseForRecovery() {
		ActiveCardinalRite rite = rite(2, false, 2);
		Vec3 original = new Vec3(1.0D, 2.0D, 3.0D);
		assertTrue(rite.requestCancellation(40L));
		assertTrue(rite.tickCancellation(40L));
		rite.captureCancellationDaemonStart(original, 2.5F);

		assertFalse(rite.tickCancellation(41L));

		assertTrue(rite.isCancellationRecovering());
		assertEquals(CardinalRiteCancellationGeometry.RECOVERY_TICKS,
				rite.getCancellationRecoveryTicks());
		assertEquals(original, rite.getCancellationRecoveryTargetPos());
		assertEquals(2.5F, rite.getCancellationRecoveryTargetScale());
		assertNull(rite.getCancellationDaemonStartPos(),
				"a new absorption attempt must capture its own starting pose");
	}

	@Test
	void cancellationBeforeDaemonEmergenceCreatesNoRecoveryVisual() {
		ActiveCardinalRite rite = rite(2, false, 2);
		assertTrue(rite.requestCancellation(40L));
		assertTrue(rite.tickCancellation(40L));

		assertFalse(rite.tickCancellation(41L));

		assertFalse(rite.isCancellationRecovering());
	}

	@Test
	void plantingIntroRevealsTheStaffOnlyOnItsImpactTick() {
		ActiveCardinalRite rite = rite(2, false, 2);
		rite.beginStaffPlanting();

		for (int tick = 0; tick < 13; tick++) {
			assertFalse(rite.tickStaffPlanting());
			assertFalse(rite.isStaffImpactReached());
		}
		assertTrue(rite.tickStaffPlanting());
		assertTrue(rite.isStaffImpactReached());
		assertEquals(14, rite.getStaffPlantingTicks());
		assertEquals(0, rite.getPhaseTicks(), "ceremony progression is frozen during planting");
	}

	@Test
	void plantingIntroProgressSurvivesWorldReload() {
		ActiveCardinalRite rite = rite(2, false, 2);
		rite.beginStaffPlanting();
		for (int tick = 0; tick < 9; tick++) rite.tickStaffPlanting();

		ActiveCardinalRite copy = ActiveCardinalRite.deserialize(rite.serialize());

		assertTrue(copy.isStaffPlanting());
		assertEquals(9, copy.getStaffPlantingTicks());
		assertFalse(copy.isStaffImpactReached());
	}
}
