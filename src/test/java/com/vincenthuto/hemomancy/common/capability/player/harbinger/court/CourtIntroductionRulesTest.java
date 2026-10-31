package com.vincenthuto.hemomancy.common.capability.player.harbinger.court;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourtIntroductionRulesTest {
	@Test
	void theVicarRefersFromTheFifthDegree() {
		assertFalse(CourtIntroductionRules.vicarRefers(4));
		assertTrue(CourtIntroductionRules.vicarRefers(5));
		assertTrue(CourtIntroductionRules.vicarRefers(8));
	}

	@Test
	void aVoyagerSpeaksOfTheCourtOnceItIsKnownOrALetterDroppedOrFromTheFifthDegree() {
		assertFalse(CourtIntroductionRules.voyagerSpeaks(4, false, false));
		assertTrue(CourtIntroductionRules.voyagerSpeaks(5, false, false));
		assertTrue(CourtIntroductionRules.voyagerSpeaks(1, true, false));
		assertTrue(CourtIntroductionRules.voyagerSpeaks(1, false, true));
	}

	@Test
	void theAlchemistNeedsTheFifthDegreeAndAMatureStrain() {
		assertFalse(CourtIntroductionRules.alchemistPoints(5, -1), "no strain");
		assertFalse(CourtIntroductionRules.alchemistPoints(5, 2), "Developing is not enough");
		assertTrue(CourtIntroductionRules.alchemistPoints(5, CourtIntroductionRules.MATURE));
		assertTrue(CourtIntroductionRules.alchemistPoints(6, 5), "Primal counts");
		assertFalse(CourtIntroductionRules.alchemistPoints(4, 5), "a Primal strain doesn't skip the Degree");
	}

	@Test
	void theFirstRouteIsKeptAndLaterRoutesAcknowledgeIt() {
		assertEquals(CourtIntroductionRules.Arrival.FIRST, CourtIntroductionRules.arrive(false));
		assertEquals(CourtIntroductionRules.Arrival.ACKNOWLEDGED, CourtIntroductionRules.arrive(true));
	}

	@Test
	void theLetterDropsOncePerUnknowingPlayer() {
		assertTrue(CourtIntroductionRules.letterDrops(false, false));
		assertFalse(CourtIntroductionRules.letterDrops(false, true), "never a second time");
		assertFalse(CourtIntroductionRules.letterDrops(true, false), "never for a player who knows");
	}

	@Test
	void aLostLetterCanBeExplainedUntilTheCourtIsKnown() {
		assertTrue(CourtIntroductionRules.letterLost(false, true, false));
		assertFalse(CourtIntroductionRules.letterLost(false, true, true), "still carried: the inquiry reads it");
		assertFalse(CourtIntroductionRules.letterLost(true, true, false));
		assertFalse(CourtIntroductionRules.letterLost(false, false, false));
	}

	@Test
	void theChartIsReissuedToAnyoneWhoKnowsAndCarriesNone() {
		assertTrue(CourtIntroductionRules.canIssueChart(true, false));
		assertFalse(CourtIntroductionRules.canIssueChart(true, true));
		assertFalse(CourtIntroductionRules.canIssueChart(false, false));
	}

	@Test
	void routesKeepTheirSavedNames() {
		for (CourtProgress.Route route : CourtProgress.Route.values())
			assertEquals(route, CourtProgress.Route.fromSerializedName(route.serializedName()));
		assertEquals("found", CourtProgress.Route.FOUND.serializedName());
		assertNull(CourtProgress.Route.fromSerializedName("mnemonist"));
	}
}
