package com.vincenthuto.hemomancy.common.entity.mob;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GardensRosterRulesTest {
	@Test
	void theRosterCarriesTheAnsweredNumbers() {
		assertStats("fungling", 16, 0, 4);
		assertStats("chthonian", 30, 6, 6);
		assertStats("fargone", 36, 0, 7);
		assertStats("thirster", 44, 0, 8);
		assertStats("lump_of_thought", 52, 0, 9);
		assertStats("hunter_fungus", 56, 4, 10);
		assertStats("erythromycelium_eruptus", 64, 4, 11);
		assertStats("abhorrent_thought", 90, 8, 13);
	}

	@Test
	void healthClimbsAlongTheRoster() {
		double previous = 0.0D;
		for (String id : List.of("fungling", "chthonian", "fargone", "thirster", "lump_of_thought", "hunter_fungus",
				"erythromycelium_eruptus", "abhorrent_thought")) {
			double health = GardensRosterRules.stats(id).orElseThrow().maxHealth();
			assertTrue(health > previous, id + " is weaker than the mob before it");
			previous = health;
		}
	}

	@Test
	void onlyNaturalSpawnsInTheDimensionAreRaised() {
		assertTrue(GardensRosterRules.appliesTo(true, true));
		assertFalse(GardensRosterRules.appliesTo(false, true), "the Overworld biome keeps today's numbers");
		assertFalse(GardensRosterRules.appliesTo(true, false), "summons such as the Mycophant's brood keep theirs");
		assertTrue(GardensRosterRules.stats("chitinite").isEmpty(), "the Chitinite is not on the roster");
	}

	private static void assertStats(String id, double health, double armor, double damage) {
		GardensRosterRules.Stats stats = GardensRosterRules.stats(id).orElseThrow();
		assertEquals(health, stats.maxHealth(), id + " health");
		assertEquals(armor, stats.armor(), id + " armour");
		assertEquals(damage, stats.attackDamage(), id + " damage");
	}
}
