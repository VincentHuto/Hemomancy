package com.vincenthuto.hemomancy.common.circus;

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
}
