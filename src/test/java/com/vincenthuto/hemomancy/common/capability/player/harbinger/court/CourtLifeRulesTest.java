package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtMembershipRules.Outcome;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.ActKind;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.court.CourtStandingRules.Standing;
import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourtLifeRulesTest {
	private static final CourtHouse STONE = CourtHouse.HOLDING_STONE;
	private static final CourtHouse FOLD = CourtHouse.VEILED_FOLD;

	// --- Membership ----------------------------------------------------------------------------------------------

	@Test
	void theFirstEntryIsFreeAtTheFifthDegreeWithGuestStanding() {
		assertEquals(Outcome.JOINED, CourtMembershipRules.join(null, false, STONE, true, Standing.GUEST, false, false));
		assertEquals(Outcome.JOINED, CourtMembershipRules.join(null, false, STONE, true, Standing.FRIEND, false, false));
	}

	@Test
	void oneFreeSwitchThenMembershipIsFixed() {
		assertEquals(Outcome.SWITCHED, CourtMembershipRules.join(STONE, false, FOLD, true, Standing.GUEST, false, false));
		assertEquals(Outcome.REFUSED_SWITCH_USED, CourtMembershipRules.join(FOLD, true, STONE, true, Standing.FRIEND, false, false));
		assertEquals(Outcome.ALREADY_MEMBER, CourtMembershipRules.join(FOLD, true, FOLD, true, Standing.FRIEND, false, false));
	}

	@Test
	void joiningRefusesWithoutTheFifthDegreeOrGuestStanding() {
		assertEquals(Outcome.REFUSED_DEGREE, CourtMembershipRules.join(null, false, STONE, false, Standing.FRIEND, false, false));
		assertEquals(Outcome.REFUSED_STANDING, CourtMembershipRules.join(null, false, STONE, true, Standing.STRANGER, false, false));
		assertEquals(Outcome.REFUSED_STANDING, CourtMembershipRules.join(STONE, false, FOLD, true, Standing.STRANGER, false, false));
	}

	@Test
	void anUnstainedPlayerIsShunnedEvenFromTheirOwnHouse() {
		assertEquals(Outcome.REFUSED_SHUNNED, CourtMembershipRules.join(null, false, STONE, true, Standing.FRIEND, true, false));
		assertEquals(Outcome.REFUSED_SHUNNED, CourtMembershipRules.join(STONE, false, STONE, true, Standing.FRIEND, true, false));
	}

	@Test
	void onlyTheGivingLightTurnsATransgressorAway() {
		assertTrue(CourtMembershipRules.turnsAway(CourtHouse.GIVING_LIGHT, true));
		assertFalse(CourtMembershipRules.turnsAway(CourtHouse.GIVING_LIGHT, false));
		assertFalse(CourtMembershipRules.turnsAway(CourtHouse.KINDLING, true));
		assertEquals(Outcome.REFUSED_TRANSGRESSION,
				CourtMembershipRules.join(null, false, CourtHouse.GIVING_LIGHT, true, Standing.FRIEND, false, true));
	}

	@Test
	void everyOutcomeHasItsOwnKey() {
		for (Outcome outcome : Outcome.values()) {
			assertEquals("hemomancy.court.membership." + outcome.name().toLowerCase(Locale.ROOT), outcome.translationKey());
		}
		assertTrue(Outcome.JOINED.changed() && Outcome.SWITCHED.changed());
		assertFalse(Outcome.REFUSED_SWITCH_USED.changed());
	}

	// --- Standing ------------------------------------------------------------------------------------------------

	@Test
	void aVisitMakesAStrangerAGuestAndNoMore() {
		assertEquals(Standing.GUEST, CourtStandingRules.afterAct(Standing.STRANGER, ActKind.VISIT));
		assertEquals(Standing.GUEST, CourtStandingRules.afterAct(Standing.GUEST, ActKind.VISIT));
		assertEquals(Standing.FRIEND, CourtStandingRules.afterAct(Standing.FRIEND, ActKind.VISIT));
	}

	@Test
	void aCustomRaisesOneStepToFriend() {
		assertEquals(Standing.GUEST, CourtStandingRules.afterAct(Standing.STRANGER, ActKind.CUSTOM));
		assertEquals(Standing.FRIEND, CourtStandingRules.afterAct(Standing.GUEST, ActKind.CUSTOM));
		assertEquals(Standing.FRIEND, CourtStandingRules.afterAct(Standing.FRIEND, ActKind.CUSTOM));
	}

	@Test
	void anActCountsOnceAndNeverForTheShunnedOrTheTurnedAway() {
		assertTrue(CourtStandingRules.actCounts(false, false, false));
		assertFalse(CourtStandingRules.actCounts(true, false, false));
		assertFalse(CourtStandingRules.actCounts(false, true, false));
		assertFalse(CourtStandingRules.actCounts(false, false, true));
	}

	@Test
	void leavingDropsToGuestNeverLowerAndAmendsRestoreGuest() {
		assertEquals(Standing.GUEST, CourtStandingRules.afterLeaving(Standing.FRIEND));
		assertEquals(Standing.GUEST, CourtStandingRules.afterLeaving(Standing.GUEST));
		assertEquals(Standing.STRANGER, CourtStandingRules.afterTransgression());
		assertEquals(Standing.GUEST, CourtStandingRules.afterAmends(Standing.STRANGER));
	}

	@Test
	void savedLevelsClampIntoRange() {
		assertEquals(Standing.STRANGER, Standing.ofLevel(-3));
		assertEquals(Standing.GUEST, Standing.ofLevel(1));
		assertEquals(Standing.FRIEND, Standing.ofLevel(9));
	}

	// --- The lesson ----------------------------------------------------------------------------------------------

	@Test
	void theLessonMultipliesOnlyInsideTheStrainsOwnHouseAfterItIsTaught() {
		assertEquals(1.5D, CourtLessonRules.MULTIPLIER);
		assertEquals(1.5D, CourtLessonRules.multiplier(true, STONE, STONE));
		assertEquals(1.0D, CourtLessonRules.multiplier(false, STONE, STONE), "not before the lesson");
		assertEquals(1.0D, CourtLessonRules.multiplier(true, STONE, FOLD), "not in another house");
		assertEquals(1.0D, CourtLessonRules.multiplier(true, STONE, null), "not outside the Court");
		assertEquals(1.0D, CourtLessonRules.multiplier(true, null, STONE), "not without a strain");
	}

	@Test
	void theLessonIsTaughtOnceFromTheFifthDegree() {
		assertEquals(CourtLessonRules.Outcome.TAUGHT, CourtLessonRules.teach(false, true, false, false));
		assertEquals(CourtLessonRules.Outcome.ALREADY_TAUGHT, CourtLessonRules.teach(true, true, false, false));
		assertEquals(CourtLessonRules.Outcome.REFUSED_DEGREE, CourtLessonRules.teach(false, false, false, false));
		assertEquals(CourtLessonRules.Outcome.REFUSED_SHUNNED, CourtLessonRules.teach(false, true, true, false));
		assertEquals(CourtLessonRules.Outcome.REFUSED_TRANSGRESSION, CourtLessonRules.teach(false, true, false, true));
	}

	// --- The Champion --------------------------------------------------------------------------------------------

	@Test
	void theUsurpationGateNeedsTheCourtAnApexStrainAndKindlingFriendship() {
		assertEquals(CourtChampionRules.Gate.OPEN, CourtChampionRules.challenge(true, false, false, true, Standing.FRIEND, 100L, 0L));
		assertEquals(CourtChampionRules.Gate.REFUSED_UNKNOWN, CourtChampionRules.challenge(false, false, false, true, Standing.FRIEND, 100L, 0L));
		assertEquals(CourtChampionRules.Gate.REFUSED_SHUNNED, CourtChampionRules.challenge(true, true, false, true, Standing.FRIEND, 100L, 0L));
		assertEquals(CourtChampionRules.Gate.REFUSED_CHAMPION, CourtChampionRules.challenge(true, false, true, true, Standing.FRIEND, 100L, 0L));
		assertEquals(CourtChampionRules.Gate.REFUSED_STRAIN, CourtChampionRules.challenge(true, false, false, false, Standing.FRIEND, 100L, 0L));
		assertEquals(CourtChampionRules.Gate.REFUSED_STANDING, CourtChampionRules.challenge(true, false, false, true, Standing.GUEST, 100L, 0L));
	}

	@Test
	void aLostUsurpationWaitsOneDay() {
		long retry = CourtChampionRules.retryAfterLoss(1000L);
		assertEquals(25000L, retry);
		assertEquals(CourtChampionRules.Gate.REFUSED_WAIT, CourtChampionRules.challenge(true, false, false, true, Standing.FRIEND, 24999L, retry));
		assertEquals(CourtChampionRules.Gate.OPEN, CourtChampionRules.challenge(true, false, false, true, Standing.FRIEND, 25000L, retry));
	}

	// --- Saved state ---------------------------------------------------------------------------------------------

	@Test
	void courtLifeStateRoundTripsBesideTheDiscoveryKeys() {
		CourtProgress progress = new CourtProgress();
		progress.setKnown(CourtProgress.Route.FOUND);
		progress.setHouse(FOLD, true);
		progress.setStanding(STONE, Standing.GUEST);
		progress.setStanding(FOLD, Standing.FRIEND);
		progress.countAct(FOLD, "visit");
		progress.countAct(FOLD, "custom");
		progress.setLessonTaught(true);
		progress.recordTransgression("broke_growth", 1234L);
		progress.setChampion(true, 99L);
		progress.setUsurpRetryAt(5000L);
		CompoundTag tag = progress.serializeNBT(null);
		assertTrue(tag.getBoolean("Known") && "found".equals(tag.getString("KnownBy")), "Track 5's keys are untouched");

		CourtProgress copy = new CourtProgress();
		copy.deserializeNBT(null, tag);
		assertEquals(FOLD, copy.house());
		assertTrue(copy.switchUsed());
		assertEquals(Standing.GUEST, copy.standing(STONE));
		assertEquals(Standing.FRIEND, copy.standing(FOLD));
		assertEquals(Standing.STRANGER, copy.standing(CourtHouse.KINDLING));
		assertTrue(copy.actCounted(FOLD, "visit") && copy.actCounted(FOLD, "custom"));
		assertFalse(copy.actCounted(STONE, "visit"));
		assertTrue(copy.lessonTaught());
		assertEquals("broke_growth", copy.transgressionAct());
		assertEquals(1234L, copy.transgressionTime());
		assertEquals(1, copy.transgressionCount());
		assertTrue(copy.champion());
		assertEquals(99L, copy.championSince());
		assertEquals(5000L, copy.usurpRetryAt());
	}

	@Test
	void aTrack5SaveLoadsWithNoCourtLife() {
		CompoundTag old = new CompoundTag();
		old.putBoolean("Known", true);
		old.putString("KnownBy", "voyager");
		old.putBoolean("LetterDropped", true);
		old.putBoolean("ChartIssued", true);
		CourtProgress progress = new CourtProgress();
		progress.deserializeNBT(null, old);
		assertTrue(progress.known() && progress.chartIssued());
		assertNull(progress.house());
		assertFalse(progress.switchUsed() || progress.lessonTaught() || progress.hasTransgression() || progress.champion());
		assertEquals(0L, progress.usurpRetryAt());
		for (CourtHouse house : CourtHouse.values()) assertEquals(Standing.STRANGER, progress.standing(house));
	}

	@Test
	void amendsClearTheRecordButKeepTheCount() {
		CourtProgress progress = new CourtProgress();
		progress.recordTransgression("took_from_undercroft", 10L);
		progress.clearTransgression();
		assertFalse(progress.hasTransgression());
		assertEquals(1, progress.transgressionCount());
		CourtProgress copy = new CourtProgress();
		copy.deserializeNBT(null, progress.serializeNBT(null));
		assertFalse(copy.hasTransgression());
		assertEquals(1, copy.transgressionCount());
	}
}
