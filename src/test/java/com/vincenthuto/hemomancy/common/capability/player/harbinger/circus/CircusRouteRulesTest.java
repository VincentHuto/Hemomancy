package com.vincenthuto.hemomancy.common.capability.player.harbinger.circus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CircusRouteRulesTest {
	@Test
	void neutralPlayerCanChooseEitherRouteButCompletedPlayerCannotSwitch() {
		assertTrue(CircusRouteRules.canChoose(CircusRouteRules.Route.NEUTRAL));
		assertEquals(CircusRouteRules.Route.SUCCESSION,
				CircusRouteRules.choose(CircusRouteRules.Route.NEUTRAL, CircusRouteRules.Route.SUCCESSION));
		assertEquals(CircusRouteRules.Route.LIBERATION,
				CircusRouteRules.choose(CircusRouteRules.Route.NEUTRAL, CircusRouteRules.Route.LIBERATION));
		assertFalse(CircusRouteRules.canChoose(CircusRouteRules.Route.SUCCESSION_COMPLETE));
		assertEquals(CircusRouteRules.Route.SUCCESSION_COMPLETE,
				CircusRouteRules.choose(CircusRouteRules.Route.SUCCESSION_COMPLETE, CircusRouteRules.Route.LIBERATION));
	}

	@Test
	void successionRequiresAttunementAndEveryChallenge() {
		assertFalse(CircusRouteRules.canBeginFinale(CircusRouteRules.Route.SUCCESSION, 999, 0b1_1111));
		assertFalse(CircusRouteRules.canBeginFinale(CircusRouteRules.Route.SUCCESSION, 1000, 0b0_1111));
		assertTrue(CircusRouteRules.canBeginFinale(CircusRouteRules.Route.SUCCESSION, 1000, 0b1_1111));
		assertTrue(CircusRouteRules.canBeginFinale(CircusRouteRules.Route.LIBERATION, 0, 0));
	}

	@Test
	void oneRepairIsAvailableOnlyWhileAChosenRouteIsUnfinished() {
		assertTrue(CircusRouteRules.canRepair(CircusRouteRules.Route.SUCCESSION, false));
		assertTrue(CircusRouteRules.canRepair(CircusRouteRules.Route.LIBERATION, false));
		assertFalse(CircusRouteRules.canRepair(CircusRouteRules.Route.NEUTRAL, false));
		assertFalse(CircusRouteRules.canRepair(CircusRouteRules.Route.SUCCESSION, true));
		assertFalse(CircusRouteRules.canRepair(CircusRouteRules.Route.LIBERATION_COMPLETE, false));
	}

	/** D-30, NQ-15-5: Degree 4 and any one school report open the pact for everyone but the Unstained. */
	@Test
	void thePactNeedsTheFourthDegreeAndAnyOneReport() {
		for (CircusRouteRules.Route route : new CircusRouteRules.Route[]{CircusRouteRules.Route.SUCCESSION,
				CircusRouteRules.Route.LIBERATION}) {
			assertEquals(CircusRouteRules.PactGate.OPEN, CircusRouteRules.pactGate(route, true, true, false, false));
			assertEquals(CircusRouteRules.PactGate.NEEDS_DEGREE, CircusRouteRules.pactGate(route, false, true, true, false));
			assertEquals(CircusRouteRules.PactGate.NEEDS_REPORT, CircusRouteRules.pactGate(route, true, false, false, false));
			assertEquals(CircusRouteRules.PactGate.NEEDS_DEGREE_AND_REPORT,
					CircusRouteRules.pactGate(route, false, false, false, false));
		}
		assertEquals(4, CircusRouteRules.PACT_DEGREE);
	}

	/** D-31, NQ-15-6 B: the Unstained run Liberation only, on The Missing Understudy report and no Degree. */
	@Test
	void anUnstainedLiberatorNeedsTheUnderstudyReportAndNoDegree() {
		assertEquals(CircusRouteRules.PactGate.OPEN,
				CircusRouteRules.pactGate(CircusRouteRules.Route.LIBERATION, false, true, true, true));
		assertEquals(CircusRouteRules.PactGate.NEEDS_UNDERSTUDY,
				CircusRouteRules.pactGate(CircusRouteRules.Route.LIBERATION, true, true, false, true));
		assertEquals(CircusRouteRules.PactGate.UNSTAINED,
				CircusRouteRules.pactGate(CircusRouteRules.Route.SUCCESSION, true, true, true, true));
	}
}
