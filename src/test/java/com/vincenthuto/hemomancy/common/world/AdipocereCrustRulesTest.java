package com.vincenthuto.hemomancy.common.world;

import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.vincenthuto.hemomancy.common.world.AdipocereCrustRules.Host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdipocereCrustRulesTest {
	@Test
	void onlyStrongholdsAndAncientCitiesHostByStructure() {
		assertEquals(Host.STRONGHOLD, AdipocereCrustRules.hostFor("minecraft:stronghold"));
		assertEquals(Host.ANCIENT_CITY, AdipocereCrustRules.hostFor("minecraft:ancient_city"));
		assertNull(AdipocereCrustRules.hostFor("hemomancy:mausoleum"), "the mausoleums keep their own wax");
		assertNull(AdipocereCrustRules.hostFor("minecraft:mineshaft"));
	}

	@Test
	void aCandidateNeedsAirAContinuingWallAndARoof() {
		assertTrue(AdipocereCrustRules.isCandidate(true, true, true, true));
		assertFalse(AdipocereCrustRules.isCandidate(false, true, true, true), "the cell must be empty");
		assertFalse(AdipocereCrustRules.isCandidate(true, false, true, true), "the neighbour must be masonry");
		assertFalse(AdipocereCrustRules.isCandidate(true, true, false, true), "a lone block is not a wall");
		assertFalse(AdipocereCrustRules.isCandidate(true, true, true, false), "outer faces under open cave are not interior");
	}

	@Test
	void aSiteTakesItsShareAndNeverMoreThanItsCap() {
		int strongholdChunks = 0;
		for (long seed = 0; seed < 10_000; seed++) {
			assertEquals(2, AdipocereCrustRules.crustsFor(Host.MONSTER_ROOM, 1.0, seed), "two per monster room");
			strongholdChunks += AdipocereCrustRules.crustsFor(Host.STRONGHOLD, 1.0, seed);
		}
		assertEquals(3500, strongholdChunks, 250, "about one stronghold chunk in three");
		assertEquals(0, AdipocereCrustRules.crustsFor(Host.STRONGHOLD, 0.0, 7), "density zero turns it off");
		assertEquals(3, AdipocereCrustRules.crustsFor(Host.MONSTER_ROOM, 10.0, 7), "density never breaks the cap");
		assertEquals(2, AdipocereCrustRules.crustsFor(Host.ANCIENT_CITY, 10.0, 7), "density never breaks the cap");
	}

	@Test
	void picksAreDistinctBoundedAndRepeatable() {
		List<Integer> first = AdipocereCrustRules.pick(40, 2, 1234L);
		assertEquals(first, AdipocereCrustRules.pick(40, 2, 1234L));
		assertEquals(2, new HashSet<>(first).size());
		first.forEach(i -> assertTrue(i >= 0 && i < 40));
		assertEquals(List.of(0), AdipocereCrustRules.pick(1, 3, 99L), "never more picks than candidates");
		assertTrue(AdipocereCrustRules.pick(0, 2, 5L).isEmpty());
	}

	@Test
	void theChunkCapCannotCarpetAWall() {
		assertEquals(AdipocereCrustRules.PER_CHUNK_CAP, AdipocereCrustRules.remaining(0));
		assertEquals(0, AdipocereCrustRules.remaining(AdipocereCrustRules.PER_CHUNK_CAP));
		assertEquals(0, AdipocereCrustRules.remaining(AdipocereCrustRules.PER_CHUNK_CAP + 3));
	}

	@Test
	void seedsDependOnWorldChunkHostAndSite() {
		long base = AdipocereCrustRules.seed(1L, 2L, Host.STRONGHOLD, 3L);
		assertEquals(base, AdipocereCrustRules.seed(1L, 2L, Host.STRONGHOLD, 3L));
		assertNotEquals(base, AdipocereCrustRules.seed(9L, 2L, Host.STRONGHOLD, 3L));
		assertNotEquals(base, AdipocereCrustRules.seed(1L, 9L, Host.STRONGHOLD, 3L));
		assertNotEquals(base, AdipocereCrustRules.seed(1L, 2L, Host.ANCIENT_CITY, 3L));
		assertNotEquals(base, AdipocereCrustRules.seed(1L, 2L, Host.STRONGHOLD, 9L));
	}
}
