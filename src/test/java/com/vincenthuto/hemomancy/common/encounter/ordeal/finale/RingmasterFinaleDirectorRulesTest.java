package com.vincenthuto.hemomancy.common.encounter.ordeal.finale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.circus.CircusRouteRules;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.FinaleRules.Ending;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirectorRules.Reason;
import com.vincenthuto.hemomancy.common.encounter.ordeal.finale.RingmasterFinaleDirectorRules.Release;

class RingmasterFinaleDirectorRulesTest {
	@Test
	void interruptedFinaleReleases() {
		for (Reason reason : Reason.values()) {
			Release release = RingmasterFinaleDirectorRules.onInterrupt(reason);
			assertTrue(release.returnHome() && release.unfreezeSite() && release.releaseSite() && release.restartable(),
					reason.name());
		}
	}

	@Test
	void settlementPicksTheRoute() {
		assertEquals(CircusRouteRules.Route.LIBERATION_COMPLETE, CircusFinaleSettlement.routeFor(Ending.KILLED));
		assertEquals(CircusRouteRules.Route.SUCCESSION_COMPLETE, CircusFinaleSettlement.routeFor(Ending.ABSORBED));
	}

	/** F6: once across, a player out of his Chamber for AWAY_LIMIT ticks has left the finale; back inside, it resets. */
	@Test
	void awayFromTheChamberBreaksTheFinaleOff() {
		int away = 0;
		for (int i = 0; i < RingmasterFinaleDirectorRules.AWAY_LIMIT - 1; i++)
			away = RingmasterFinaleDirectorRules.awayTicks(away, true, false);
		assertTrue(!RingmasterFinaleDirectorRules.strandedAway(away), "not yet at " + away);
		away = RingmasterFinaleDirectorRules.awayTicks(away, true, false);
		assertTrue(RingmasterFinaleDirectorRules.strandedAway(away), "broken off at " + away);
		assertEquals(0, RingmasterFinaleDirectorRules.awayTicks(away, true, true), "back in his Chamber resets it");
		assertEquals(0, RingmasterFinaleDirectorRules.awayTicks(5, false, false), "the opening in the real tent never counts");
	}

	/** F3: a site that cannot be claimed names why, as the old finale did. */
	@Test
	void siteRefusalNamesABusyOrSettledSite() {
		var neutral = com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules.Outcome.NEUTRAL;
		var ruin = com.vincenthuto.hemomancy.common.entity.npc.circus.CircusPavilionStateRules.Outcome.RUIN;
		assertEquals("hemomancy.circus.finale.busy", RingmasterFinaleDirectorRules.siteRefusalKey(java.util.UUID.randomUUID(), neutral));
		assertEquals("hemomancy.circus.finale.settled", RingmasterFinaleDirectorRules.siteRefusalKey(null, ruin));
		assertEquals(null, RingmasterFinaleDirectorRules.siteRefusalKey(null, neutral));
	}
}
