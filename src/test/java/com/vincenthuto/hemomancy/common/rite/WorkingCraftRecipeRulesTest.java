package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftRecipeRulesTest {
	private static final String[][] LOOM = {{"P P", "TIT"}, {"P P", "TIT"}};
	private static final String[][] MONOLITH = {{"PSP", "SCS", "PGP"}, {"SIS", "III", "GIG"}, {"PSP", "SIS", "PGP"}};

	@Test
	void acceptsALesserLoom() {
		assertNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_lesser", LOOM, 150, 0, 0));
	}

	@Test
	void largerFloorsAlsoHoldSmallerSubjects() {
		assertNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_grand", LOOM, 150, 0, 0));
	}

	@Test
	void mustBeAMakingRiteOnAWorkingFloor() {
		assertNotNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.WORLD, "working_lesser", LOOM, 150, 0, 0));
		assertNotNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "dominion_lesser", LOOM, 150, 0, 0));
	}

	@Test
	void subjectMustFitTheFloorTiersBed() {
		assertNotNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_lesser", MONOLITH, 175, 0, 0));
		assertNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_greater", MONOLITH, 175, 0, 0));
	}

	@Test
	void feedIsPositiveAndCostLivesInTheFeed() {
		assertNotNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_lesser", LOOM, 0, 0, 0));
		assertNotNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_lesser", LOOM, 150, 0, 150));
	}

	@Test
	void craftsHaveNoAnchors() {
		assertNotNull(WorkingCraftRecipeRules.violation(CardinalRitePurpose.MAKING, "working_lesser", LOOM, 150, 4, 0));
	}

	@Test
	void craftsMayCarryUpToSixOfferingsAtAnyDegree() {
		assertEquals(6, WorkingCraftRecipeRules.MAX_OFFERINGS);
	}
}
