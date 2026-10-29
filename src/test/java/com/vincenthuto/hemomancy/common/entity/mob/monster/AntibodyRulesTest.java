package com.vincenthuto.hemomancy.common.entity.mob.monster;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AntibodyRulesTest {
	@Test
	void anAntibodyDissolvesWhenItsWorkCannotBeDone() {
		assertFalse(AntibodyRules.shouldDissolve(10, true, true, true, 100));
		assertTrue(AntibodyRules.shouldDissolve(1201, true, true, true, 100), "lifetime spent");
		assertTrue(AntibodyRules.shouldDissolve(10, false, true, true, 100), "no summoner in this level");
		assertTrue(AntibodyRules.shouldDissolve(10, true, false, true, 100), "summoner dead");
		assertTrue(AntibodyRules.shouldDissolve(10, true, true, false, 100), "summoner elsewhere");
		assertTrue(AntibodyRules.shouldDissolve(10, true, true, true, 65 * 65), "summoner far beyond reach");
	}

	@Test
	void anAntibodyStrikesOnlyItsSummoner() {
		UUID summoner = UUID.randomUUID();
		assertTrue(AntibodyRules.mayStrike(summoner, summoner));
		assertFalse(AntibodyRules.mayStrike(summoner, UUID.randomUUID()));
		assertFalse(AntibodyRules.mayStrike(null, summoner));
	}
}
