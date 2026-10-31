package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The shared home bonus, the table's home predicates and the still-tracker's step rule. The predicates themselves
 * read a live world; {@code MorphlingPowerGameTests} checks them in and out of home.
 */
final class MorphlingHomeConditionsTest {
	@Test
	void homeGivesHalfAgainToDurationsAndRatesAndAQuarterToRadii() {
		assertEquals(60, MorphlingHomeBonus.duration(40, true));
		assertEquals(40, MorphlingHomeBonus.duration(40, false));
		assertEquals(1.5D, MorphlingHomeBonus.rate(1.0D, true), 1.0E-9D);
		assertEquals(10.0D, MorphlingHomeBonus.radius(8.0D, true), 1.0E-9D);
		assertEquals(8.0D, MorphlingHomeBonus.radius(8.0D, false), 1.0E-9D);
	}

	@Test
	void everyStrainHasItsOwnHomeCondition() {
		for (MorphlingStrain strain : MorphlingStrains.ALL) {
			assertNotSame(MorphlingHomeCondition.UNSET, strain.homeCondition(), strain.id() + " has no home condition");
		}
		assertEquals(MorphlingStrains.ALL.size(), MorphlingStrains.ALL.stream().map(MorphlingStrain::homeCondition)
				.distinct().count(), "each strain names its own predicate");
	}

	@Test
	void stillnessToleratesDriftButNotStepsOrJumps() {
		assertTrue(MorphlingAnchorState.isStillStep(0.03D, 0.03D, false));
		assertFalse(MorphlingAnchorState.isStillStep(0.1D, 0.0D, false));
		assertFalse(MorphlingAnchorState.isStillStep(0.0D, 0.0D, true), "jumping uproots");
	}

	@Test
	void fractionalFeedingCarriesTheRemainder() {
		double[] first = MorphlingFeeding.split(0.0D, 0.75D);
		assertEquals(0.0D, first[0]);
		double[] second = MorphlingFeeding.split(first[1], 0.75D);
		assertEquals(1.0D, second[0]);
		assertEquals(0.5D, second[1], 1.0E-9D);
	}

	@Test
	void activeStateFlagsAndFillRoundTrip() {
		int flags = MorphlingActiveState.set(0, MorphlingActiveState.SAIL_RAISED, true);
		flags = MorphlingActiveState.withFill(flags, 1.0D);
		assertTrue(MorphlingActiveState.has(flags, MorphlingActiveState.SAIL_RAISED));
		assertFalse(MorphlingActiveState.has(flags, MorphlingActiveState.KINDLED));
		assertEquals(1.0D, MorphlingActiveState.fill(flags), 1.0E-9D);
		assertEquals(0.0D, MorphlingActiveState.fill(MorphlingActiveState.withFill(flags, 0.0D)), 1.0E-9D);
		assertTrue(MorphlingActiveState.BLOOM < (1 << 24), "flags stay clear of the fill bits");
	}
}
