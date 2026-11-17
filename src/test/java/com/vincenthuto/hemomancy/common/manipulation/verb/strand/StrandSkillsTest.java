package com.vincenthuto.hemomancy.common.manipulation.verb.strand;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Round two P5: what each level of the strand skill line is worth, and where each one stops. */
class StrandSkillsTest {
	@Test
	void longStrandAddsATenthALevelToThree() {
		assertEquals(1.0D, StrandSkills.lengthScale(0), 1.0E-9D);
		assertEquals(1.1D, StrandSkills.lengthScale(1), 1.0E-9D);
		assertEquals(1.3D, StrandSkills.lengthScale(3), 1.0E-9D);
		assertEquals(1.3D, StrandSkills.lengthScale(9), 1.0E-9D, "past its cap");
		assertEquals(1.0D, StrandSkills.lengthScale(-2), 1.0E-9D, "below nothing");
	}

	@Test
	void tautLineTightensTheLashAndQuickensTheJetToTwo() {
		assertEquals(1.0D, StrandSkills.taut(0), 1.0E-9D);
		assertEquals(1.4D, StrandSkills.taut(2), 1.0E-9D);
		assertEquals(1.4D, StrandSkills.taut(3), 1.0E-9D);
		assertEquals(1.5D, StrandSkills.quicken(2), 1.0E-9D);
		assertEquals(JetRules.FILL_TICKS, JetRules.fillTicks(StrandSkills.quicken(0)));
		assertTrue(JetRules.fillTicks(StrandSkills.quicken(2)) < JetRules.FILL_TICKS, "Taut Line does not fill the jet faster");
	}

	@Test
	void whipcrackRaisesAccentDamageAndStateToThree() {
		assertEquals(1.0D, StrandSkills.accentDamage(0), 1.0E-9D);
		assertEquals(1.45D, StrandSkills.accentDamage(3), 1.0E-9D);
		assertEquals(1.6D, StrandSkills.accentState(3), 1.0E-9D);
		assertEquals(1.45D, StrandSkills.accentDamage(4), 1.0E-9D);
	}

	@Test
	void freeHandGivesBackPaceButNeverPastWalking() {
		assertEquals(0.5D, StrandSkills.pace(0.5D, 0), 1.0E-9D);
		assertEquals(0.8D, StrandSkills.pace(0.5D, 3), 1.0E-9D);
		assertEquals(1.0D, StrandSkills.pace(0.8D, 3), 1.0E-9D, "a lash at full Free Hand is past walking");
		assertEquals(1.0D, StrandSkills.pace(1.0D, 2), 1.0E-9D, "the spray already walks");
	}

	@Test
	void deepBrandSetsFasterAndLastsLongerToTwo() {
		assertEquals(BrandRules.SET_TICKS, StrandSkills.brandSetTicks(0));
		assertEquals(15, StrandSkills.brandSetTicks(1));
		assertEquals(10, StrandSkills.brandSetTicks(2));
		assertEquals(10, StrandSkills.brandSetTicks(5));
		assertEquals(BrandRules.BRAND_TICKS, StrandSkills.brandTicks(0));
		assertEquals(1200, StrandSkills.brandTicks(2));
	}

	@Test
	void theTwinCostsHalfAgain() {
		assertEquals(30.0D, StrandSkills.pulseCost(20.0D, true), 1.0E-9D);
		assertEquals(20.0D, StrandSkills.pulseCost(20.0D, false), 1.0E-9D);
	}
}
