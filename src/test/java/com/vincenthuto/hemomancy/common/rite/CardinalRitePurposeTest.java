package com.vincenthuto.hemomancy.common.rite;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class CardinalRitePurposeTest {
	@Test
	void eachPurposeOwnsOneFloorStyleAndCulmination() {
		assertEquals("threshold", CardinalRitePurpose.SELF.floorStyle());
		assertEquals("dominion", CardinalRitePurpose.WORLD.floorStyle());
		assertEquals("working", CardinalRitePurpose.MAKING.floorStyle());
		assertEquals("communion", CardinalRitePurpose.BOND.floorStyle());
		assertEquals(CardinalRiteCulmination.ASCENSION, CardinalRitePurpose.SELF.defaultCulmination());
		assertEquals(CardinalRiteCulmination.DOMAIN, CardinalRitePurpose.WORLD.defaultCulmination());
		assertEquals(CardinalRiteCulmination.FORGING, CardinalRitePurpose.MAKING.defaultCulmination());
		assertEquals(CardinalRiteCulmination.BINDING, CardinalRitePurpose.BOND.defaultCulmination());
	}

	@Test
	void parsesIdsAndRejectsUnknown() {
		assertEquals(CardinalRitePurpose.BOND, CardinalRitePurpose.byId(" Bond "));
		assertNull(CardinalRitePurpose.byId(""));
		assertNull(CardinalRitePurpose.byId("crafting"));
	}

	@Test
	void floorStyleIsThePathBeforeTheTier() {
		assertEquals("working", CardinalRitePurpose.floorStyleOf("working_lesser"));
		assertEquals("qliphoth_root", CardinalRitePurpose.floorStyleOf("qliphoth_root_grand"));
	}

	@Test
	void floorMustMatchPurposeUnlessSpecial() {
		assertNull(CardinalRitePurpose.violation(CardinalRitePurpose.MAKING, false, "working_greater"));
		assertNotNull(CardinalRitePurpose.violation(CardinalRitePurpose.MAKING, false, "threshold_greater"));
		assertNull(CardinalRitePurpose.violation(CardinalRitePurpose.WORLD, true, "qliphoth_grand"));
		assertNotNull(CardinalRitePurpose.violation(null, false, "working_lesser"));
	}

	@Test
	void authoredCulminationWinsThenRankThenPurpose() {
		assertEquals(CardinalRiteCulmination.GROUNDING, CardinalRiteCulmination.resolve(
				"grounding", false, CardinalRitePurpose.WORLD, false, "dominion"));
		assertEquals(CardinalRiteCulmination.ASCENSION, CardinalRiteCulmination.resolve(
				"", true, CardinalRitePurpose.SELF, false, "threshold"));
		assertEquals(CardinalRiteCulmination.BINDING, CardinalRiteCulmination.resolve(
				"", false, CardinalRitePurpose.BOND, true, "communion"));
		assertEquals(CardinalRiteCulmination.FORGING, CardinalRiteCulmination.resolve(
				"", false, null, true, null));
	}
}
