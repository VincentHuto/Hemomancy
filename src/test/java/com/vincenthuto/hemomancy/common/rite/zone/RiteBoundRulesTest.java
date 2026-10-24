package com.vincenthuto.hemomancy.common.rite.zone;

import com.vincenthuto.hemomancy.common.rite.CardinalRitePurpose;
import com.vincenthuto.hemomancy.support.EnglishText;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.*;

/** What a rite-bound thing needs, and whether the space it stands in gives it. */
final class RiteBoundRulesTest {
	private static final RiteBoundRules.Requirement WORLD =
			new RiteBoundRules.Requirement(EnumSet.of(CardinalRitePurpose.WORLD), false);
	private static final RiteBoundRules.Requirement ANY =
			new RiteBoundRules.Requirement(EnumSet.noneOf(CardinalRitePurpose.class), true);

	@Test
	void untaggedThingsWorkAnywhere() {
		assertFalse(RiteBoundRules.Requirement.NONE.bound());
		assertTrue(RiteBoundRules.admits(RiteBoundRules.Requirement.NONE, null));
		assertTrue(RiteBoundRules.admits(RiteBoundRules.Requirement.NONE, CardinalRitePurpose.MAKING));
	}

	@Test
	void aPurposeTagNeedsASpaceOfThatPurpose() {
		assertTrue(RiteBoundRules.admits(WORLD, CardinalRitePurpose.WORLD));
		assertFalse(RiteBoundRules.admits(WORLD, CardinalRitePurpose.MAKING));
		assertFalse(RiteBoundRules.admits(WORLD, null), "no space, no function");
	}

	@Test
	void anyAcceptsEverySpaceButStillNeedsOne() {
		for (CardinalRitePurpose purpose : CardinalRitePurpose.values()) assertTrue(RiteBoundRules.admits(ANY, purpose));
		assertFalse(RiteBoundRules.admits(ANY, null));
	}

	@Test
	void nonPlayerPlacementIsIgnored() {
		assertTrue(RiteBoundRules.refusesPlacement(WORLD, true, null), "a player cannot set it down outside a space");
		assertFalse(RiteBoundRules.refusesPlacement(WORLD, false, null),
				"dispensers, structures and commands are not stopped; the thing is simply inert outside");
		assertFalse(RiteBoundRules.refusesPlacement(WORLD, true, CardinalRitePurpose.WORLD));
	}

	@Test
	void describesTheSpaceByItsFloor() {
		assertEquals("a Dominion rite space", EnglishText.render(RiteBoundRules.describe(WORLD)));
		assertEquals("any rite space", EnglishText.render(RiteBoundRules.describe(ANY)));
		assertEquals("a Threshold or Working rite space", EnglishText.render(RiteBoundRules.describe(new RiteBoundRules.Requirement(
				EnumSet.of(CardinalRitePurpose.SELF, CardinalRitePurpose.MAKING), false))));
	}

	@Test
	void unboundEntitiesUnravelAfterTheirGrace() {
		assertFalse(RiteBoundRules.unravels(RiteBoundRules.UNRAVEL_GRACE_TICKS - 1));
		assertTrue(RiteBoundRules.unravels(RiteBoundRules.UNRAVEL_GRACE_TICKS));
		assertEquals(100, RiteBoundRules.UNRAVEL_GRACE_TICKS);
	}
}
