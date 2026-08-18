package com.vincenthuto.hemomancy.common.mission;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArtificerProgressionRulesTest {
	@Test
	void demonstrationsRequireTheirOrderedBriefingAndCorrespondence() {
		assertFalse(ArtificerProgressionRules.canDemonstrate(false, true, true, true));
		assertFalse(ArtificerProgressionRules.canDemonstrate(true, false, true, true));
		assertFalse(ArtificerProgressionRules.canDemonstrate(true, true, false, true));
		assertFalse(ArtificerProgressionRules.canDemonstrate(true, true, true, false));
		assertTrue(ArtificerProgressionRules.canDemonstrate(true, true, true, true));
	}

	@Test
	void threeAnswersReportsOnlyTheNextAction() {
		assertEquals(ArtificerProgressionRules.Step.BRIEFING,
				ArtificerProgressionRules.nextThreeAnswers(false, false, false, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.FIRST_UPGRADE,
				ArtificerProgressionRules.nextThreeAnswers(true, false, false, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.RECOVER_BRANCH,
				ArtificerProgressionRules.nextThreeAnswers(true, true, false, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.INSPECTION,
				ArtificerProgressionRules.nextThreeAnswers(true, true, true, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.CORRESPONDENCE,
				ArtificerProgressionRules.nextThreeAnswers(true, true, true, true, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.FULL_SET,
				ArtificerProgressionRules.nextThreeAnswers(true, true, true, true, true, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.DEMONSTRATION,
				ArtificerProgressionRules.nextThreeAnswers(true, true, true, true, true, true, false, false));
		assertEquals(ArtificerProgressionRules.Step.FITTING,
				ArtificerProgressionRules.nextThreeAnswers(true, true, true, true, true, true, true, false));
		assertEquals(ArtificerProgressionRules.Step.COMPLETE,
				ArtificerProgressionRules.nextThreeAnswers(true, true, true, true, true, true, true, true));
	}

	@Test
	void eachAssignmentStopsAtItsFirstIncompleteObjective() {
		assertEquals(ArtificerProgressionRules.Step.PLACE_ARMATURE,
				ArtificerProgressionRules.nextWornVow(true, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.INSPECTION,
				ArtificerProgressionRules.nextWornVow(true, true, true, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.FULL_SET,
				ArtificerProgressionRules.nextWornVow(true, true, true, true, false, false));
		assertEquals(ArtificerProgressionRules.Step.FITTING,
				ArtificerProgressionRules.nextWornVow(true, true, true, true, true, false));
		assertEquals(ArtificerProgressionRules.Step.CORRESPONDENCE,
				ArtificerProgressionRules.nextCrimsonVestment(true, true, true, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.DEMONSTRATION,
				ArtificerProgressionRules.nextAssumedLimb(true, true, true, false, 7, false));
		assertEquals(ArtificerProgressionRules.Step.INSPECTION,
				ArtificerProgressionRules.nextWeightOfFrame(true, true, true, true, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.RECOVER_BRANCH,
				ArtificerProgressionRules.nextWeightOfFrame(true, true, true, false, false, false, false, false, false));
		assertEquals(ArtificerProgressionRules.Step.MATERIAL_REWARD,
				ArtificerProgressionRules.nextWeightOfFrame(true, true, true, true, true, false, false, false, false));
	}

	@Test
	void ledgerStepsRoundTripThroughOneField() {
		int packed = ArtificerProgressionRules.packSteps(ArtificerProgressionRules.Step.PLACE_ARMATURE,
				ArtificerProgressionRules.Step.CORRESPONDENCE, ArtificerProgressionRules.Step.DEMONSTRATION,
				ArtificerProgressionRules.Step.LEARN_FORMS, ArtificerProgressionRules.Step.MATERIAL_REWARD);
		assertEquals(ArtificerProgressionRules.Step.PLACE_ARMATURE, ArtificerProgressionRules.unpackStep(packed, 0));
		assertEquals(ArtificerProgressionRules.Step.LEARN_FORMS, ArtificerProgressionRules.unpackStep(packed, 3));
		assertEquals(ArtificerProgressionRules.Step.MATERIAL_REWARD, ArtificerProgressionRules.unpackStep(packed, 4));
	}
}
