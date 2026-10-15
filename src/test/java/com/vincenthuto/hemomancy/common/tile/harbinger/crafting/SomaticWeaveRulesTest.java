package com.vincenthuto.hemomancy.common.tile.harbinger.crafting;

import com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank;
import org.junit.jupiter.api.Test;

import static com.vincenthuto.hemomancy.common.manipulation.EnumManipulationRank.*;
import static org.junit.jupiter.api.Assertions.*;

class SomaticWeaveRulesTest {
	@Test
	void noWeaveCanKillAtAnyRank() {
		for (EnumManipulationRank r : EnumManipulationRank.values()) assertFalse(SomaticWeaveRules.lethal(r), r.name());
	}

	@Test
	void anIntrusionStrikesHarderAtHigherRanks() {
		assertTrue(SomaticWeaveRules.attackPower(PERFECTUS) > SomaticWeaveRules.attackPower(HUMILIS));
		assertTrue(SomaticWeaveRules.WINDUP_TICKS >= 20, "every bite has a tell");
	}

	@Test
	void theFloorClampsDamageAndBlacksOut() {
		assertEquals(3f, SomaticWeaveRules.flooredDamage(10f, 3f), 1e-6);
		assertFalse(SomaticWeaveRules.blacksOut(10f, 3f));
		assertEquals(6f, SomaticWeaveRules.flooredDamage(10f, 9f), 1e-6);
		assertTrue(SomaticWeaveRules.blacksOut(10f, 9f));
		assertEquals(0f, SomaticWeaveRules.flooredDamage(3f, 5f), 1e-6);
		assertTrue(SomaticWeaveRules.blacksOut(3f, 5f));
	}

	@Test
	void theWeaverMustBeAliveHereAndClose() {
		double r2 = SomaticWeaveRules.RITUAL_RADIUS * SomaticWeaveRules.RITUAL_RADIUS;
		assertTrue(SomaticWeaveRules.weaverPresent(true, true, r2 - 1));
		assertFalse(SomaticWeaveRules.weaverPresent(true, true, r2 + 1));
		assertFalse(SomaticWeaveRules.weaverPresent(false, true, 1));
		assertFalse(SomaticWeaveRules.weaverPresent(true, false, 1));
	}

	@Test
	void loomStagesWeaveTheRanksTheirDegreeTeaches() {
		assertTrue(SomaticWeaveRules.rankAllowed(SUMMA, 0));
		assertFalse(SomaticWeaveRules.rankAllowed(MAGISTER, 0));
		assertTrue(SomaticWeaveRules.rankAllowed(MAGISTER, 1));
		assertFalse(SomaticWeaveRules.rankAllowed(PERFECTUS, 1));
		assertTrue(SomaticWeaveRules.rankAllowed(PERFECTUS, 2));
		assertEquals(0, SomaticWeaveRules.stageFor(HUMILIS));
		assertEquals(0, SomaticWeaveRules.stageFor(SUMMA));
		assertEquals(1, SomaticWeaveRules.stageFor(MAGISTER));
		assertEquals(2, SomaticWeaveRules.stageFor(PERFECTUS));
	}

	@Test
	void theRitualAreaHoldsTheLargestEngram() {
		assertTrue(SomaticWeaveRules.RITUAL_RADIUS > com.vincenthuto.hemomancy.common.tile.harbinger.crafting.engram.EngramWeaveRules.tipRadius(2) + 2);
	}
}
