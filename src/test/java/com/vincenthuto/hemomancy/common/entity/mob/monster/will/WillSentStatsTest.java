package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WillSentStatsTest {
	@Test
	void sentWillsAgainstATwentyHealthPlayerFollowTheAnsweredCurve() {
		assertStats(6, 60.0D, 8.0D);
		assertStats(7, 90.0D, 10.0D);
		assertStats(8, 120.0D, 12.0D);
	}

	@Test
	void sentWillsNeverFallBelowTheirDegreeSixFloor() {
		WillCombatRules.Stats early = WillCombatRules.sentStats(3, 10.0D);
		assertEquals(60.0D, early.maxHealth(), "a weakened or early target still meets the D6 test");
		assertEquals(8.0D, early.attackDamage());
	}

	@Test
	void brokenWillsKeepTheirTierStats() {
		WillCombatRules.Stats tierTwo = WillCombatRules.brokenStats(2);
		assertEquals(30.0D, tierTwo.maxHealth(), "NQ-06-6 raises sent Wills only");
		assertEquals(5.0D, tierTwo.attackDamage());
	}

	private static void assertStats(int degree, double health, double damage) {
		WillCombatRules.Stats stats = WillCombatRules.sentStats(degree, 20.0D);
		assertEquals(health, stats.maxHealth(), "sent Will health at D" + degree);
		assertEquals(damage, stats.attackDamage(), "sent Will damage at D" + degree);
	}
}
