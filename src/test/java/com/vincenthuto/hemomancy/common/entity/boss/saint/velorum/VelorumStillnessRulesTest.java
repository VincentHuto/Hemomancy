package com.vincenthuto.hemomancy.common.entity.boss.saint.velorum;

import com.vincenthuto.hemomancy.common.entity.boss.saint.velorum.VelorumStillnessRules.Inputs;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VelorumStillnessRulesTest {
	/** A tick's inputs from a short flag string: s sneak, r sprint, i item, p presence, h shelter, w watched, o open, b bow, e seam, x hour, t thawed. */
	private static Inputs in(double travel, int slow, int rime, String flags) {
		return new Inputs(travel, flags.contains("s"), flags.contains("r"), flags.contains("i"), slow, rime,
				flags.contains("p"), flags.contains("h"), flags.contains("w"), flags.contains("o"), flags.contains("b"),
				flags.contains("e"), flags.contains("x"), flags.contains("t"));
	}

	@Test
	void everyFillAndDrainTerm() {
		assertEquals(0.70F, VelorumStillnessRules.drift(Inputs.standing()), 1.0E-5F);
		assertEquals(-0.55F, VelorumStillnessRules.drift(Inputs.walking()), 1.0E-5F);
		assertEquals(-0.90F, VelorumStillnessRules.drift(in(0.3, 0, 0, "r")), 1.0E-5F);
		// sneaking replaces standing, moving or not
		assertEquals(0.70F, VelorumStillnessRules.drift(in(0.2, 0, 0, "s")), 1.0E-5F);
		// jumping in place does not help: under 0.08 blocks of travel is standing
		assertEquals(0.70F, VelorumStillnessRules.drift(in(0.07, 0, 0, "")), 1.0E-5F);
		assertEquals(1.0F, VelorumStillnessRules.drift(in(0, 0, 0, "i")), 1.0E-5F);
		assertEquals(1.0F, VelorumStillnessRules.drift(in(0, 2, 2, "")), 1.0E-5F);
	}

	@Test
	void presenceThenShelterThenTheHour() {
		assertEquals(1.05F, VelorumStillnessRules.drift(in(0, 0, 0, "p")), 1.0E-5F);
		assertEquals(0.525F, VelorumStillnessRules.drift(in(0, 0, 0, "ph")), 1.0E-5F);
		assertEquals(-0.44F, VelorumStillnessRules.drift(in(0.2, 0, 0, "p")), 1.0E-5F);
		assertEquals(1.4F, VelorumStillnessRules.drift(in(0, 0, 0, "x")), 1.0E-5F);
		assertEquals(-0.275F, VelorumStillnessRules.drift(in(0.2, 0, 0, "x")), 1.0E-5F);
	}

	@Test
	void theGazeCountsAsStandingUnlessSprinting() {
		assertEquals(0.70F, VelorumStillnessRules.drift(in(0.2, 0, 0, "w")), 1.0E-5F);
		assertEquals(-0.90F, VelorumStillnessRules.drift(in(0.3, 0, 0, "wr")), 1.0E-5F);
	}

	@Test
	void openingsHaltTheFillButNotTheDrainAndTheBowDrainsEveryone() {
		assertEquals(0.0F, VelorumStillnessRules.drift(in(0, 1, 0, "ipo")), 1.0E-5F);
		assertEquals(-0.55F, VelorumStillnessRules.drift(in(0.2, 0, 0, "o")), 1.0E-5F);
		assertEquals(-0.8F, VelorumStillnessRules.drift(in(0, 0, 0, "ob")), 1.0E-5F);
		assertEquals(-1.8F, VelorumStillnessRules.drift(in(0, 0, 0, "obe")), 1.0E-5F);
		assertEquals(0.0F, VelorumStillnessRules.drift(in(0, 0, 0, "t")), 1.0E-5F);
	}

	@Test
	void theSpiralAtStillingStillWalksOut() {
		// Slowness I at Stilling feeds the meter: walking nets -0.45, -0.29 in his presence
		assertEquals(-0.45F, VelorumStillnessRules.drift(in(0.2, 1, 0, "")), 1.0E-5F);
		assertEquals(-0.29F, VelorumStillnessRules.drift(in(0.2, 1, 0, "p")), 1.0E-5F);
	}

	@Test
	void theDesignsNumbers() {
		assertEquals(143, VelorumStillnessRules.ticksBetween(0, 100, Inputs.standing()));
		assertEquals(96, VelorumStillnessRules.ticksBetween(0, 100, in(0, 0, 0, "p")));
		assertEquals(100, VelorumStillnessRules.ticksBetween(0, 100, in(0, 0, 0, "i")));
		assertEquals(67, VelorumStillnessRules.ticksBetween(0, 100, in(0, 0, 0, "ip")));
		assertEquals(182, VelorumStillnessRules.ticksBetween(100, 0, Inputs.walking()));
		assertEquals(112, VelorumStillnessRules.ticksBetween(100, 0, in(0.3, 0, 0, "r")));
		assertEquals(-1, VelorumStillnessRules.ticksBetween(0, 100, Inputs.walking()));
	}

	@Test
	void outOfTheDarkNothingChanges() {
		Inputs standing = Inputs.standing();
		assertFalse(standing.dark());
		assertTrue(standing.inDark().dark());
		assertEquals(0.98F, VelorumStillnessRules.drift(standing.inDark()), 1.0E-5F);
		assertEquals(-0.495F, VelorumStillnessRules.drift(Inputs.walking().inDark()), 1.0E-5F);
		// openings still halt the fill in the dark
		assertEquals(0.0F, VelorumStillnessRules.fill(in(0, 0, 0, "o").inDark()), 1.0E-5F);
	}

	@Test
	void thresholdsAndHealing() {
		assertFalse(VelorumStillnessRules.cold(39.9F));
		assertTrue(VelorumStillnessRules.cold(40));
		assertTrue(VelorumStillnessRules.stilling(70));
		assertTrue(VelorumStillnessRules.preserves(100));
		assertEquals(1.0F, VelorumStillnessRules.healing(39));
		assertEquals(0.5F, VelorumStillnessRules.healing(40));
		assertEquals(0.0F, VelorumStillnessRules.healing(70));
	}

	@Test
	void preservationBreaksAndTheCap() {
		assertFalse(VelorumStillnessRules.iceBreaks(13.9F, 1, 14));
		assertTrue(VelorumStillnessRules.iceBreaks(14, 0, 0));
		assertTrue(VelorumStillnessRules.iceBreaks(0, 2, 0));
		assertTrue(VelorumStillnessRules.iceBreaks(0, 0, 15));
		assertEquals(95.0F, VelorumStillnessRules.next(94.8F, Inputs.standing(), true), 1.0E-5F);
		assertEquals(100.0F, VelorumStillnessRules.next(99.8F, Inputs.standing(), false), 1.0E-5F);
		assertEquals(30, VelorumStillnessRules.PRESERVED_TICKS);
		assertEquals(50.0F, VelorumStillnessRules.RELEASE_VALUE);
		assertEquals(60, VelorumStillnessRules.THAW_TICKS);
		assertEquals(200, VelorumStillnessRules.CAP_TICKS);
	}
}
