package com.vincenthuto.hemomancy.common.worldgen.structure;

import org.junit.jupiter.api.Test;

import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MausoleumEntranceRulesTest {
	@Test
	void flatGroundStaysWhereVanillaPutIt() {
		assertEquals(OptionalInt.of(0), MausoleumEntranceRules.rise(new int[] {70, 70, 70, 70}, 70));
	}

	/** A centre in a dip would leave the uphill side cut into a pit around the pavilion. */
	@Test
	void theEntranceStandsOnTheHighestGroundUnderIt() {
		assertEquals(OptionalInt.of(3), MausoleumEntranceRules.rise(new int[] {70, 71, 73, 72}, 70));
		assertEquals(OptionalInt.of(0), MausoleumEntranceRules.rise(new int[] {66, 68, 70, 69}, 70));
	}

	@Test
	void groundRisingMoreThanTheLimitRefusesTheSite() {
		int limit = MausoleumEntranceRules.MAX_RISE;
		assertEquals(OptionalInt.of(limit), MausoleumEntranceRules.rise(new int[] {60, 60 + limit}, 60));
		assertEquals(OptionalInt.empty(), MausoleumEntranceRules.rise(new int[] {60, 61 + limit}, 60));
	}
}
