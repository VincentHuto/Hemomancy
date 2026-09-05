package com.vincenthuto.hemomancy.common.circus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CircusFinaleRulesTest {
	@Test
	void sharedOpeningMovesFromRaftersToCarousel() {
		assertEquals(CircusPavilionStateRules.Phase.RAFTERS,
				CircusFinaleRules.nextPhase(CircusRouteRules.Route.SUCCESSION,
						CircusPavilionStateRules.Phase.RAFTERS, 159, false, false));
		assertEquals(CircusPavilionStateRules.Phase.CAROUSEL,
				CircusFinaleRules.nextPhase(CircusRouteRules.Route.LIBERATION,
						CircusPavilionStateRules.Phase.RAFTERS, 160, false, false));
	}

	@Test
	void routeObjectiveUnlocksDescent() {
		assertEquals(CircusPavilionStateRules.Phase.DESCENT,
				CircusFinaleRules.nextPhase(CircusRouteRules.Route.SUCCESSION,
						CircusPavilionStateRules.Phase.CAROUSEL, 0, true, false));
		assertEquals(CircusPavilionStateRules.Phase.DESCENT,
				CircusFinaleRules.nextPhase(CircusRouteRules.Route.LIBERATION,
						CircusPavilionStateRules.Phase.CAROUSEL, 0, false, true));
	}
}
