package com.vincenthuto.hemomancy.common.world;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HeartNodeRulesTest {
	@Test
	void onlyAnApotheosPlantsAndOnlyOnTheGardensNetwork() {
		assertTrue(HeartNodeRules.mayPlant(true, true, true));
		assertFalse(HeartNodeRules.mayPlant(false, true, true));
		assertFalse(HeartNodeRules.mayPlant(true, false, true), "the Overworld biome is not the Gardens");
		assertFalse(HeartNodeRules.mayPlant(true, true, false), "only on network ground");
	}

	@Test
	void deathDropsAreCachedOnlyForAnApotheosDyingInTheirOwnEnvelopeWithoutKeepInventory() {
		assertTrue(HeartNodeRules.shouldCache(true, false, true));
		assertFalse(HeartNodeRules.shouldCache(false, false, true));
		assertFalse(HeartNodeRules.shouldCache(true, true, true), "keepInventory already keeps everything");
		assertFalse(HeartNodeRules.shouldCache(true, false, false), "a death outside the envelope drops normally");
	}

	@Test
	void onlyTheOwnerOpensACache() {
		UUID owner = UUID.randomUUID();
		assertTrue(HeartNodeRules.mayOpen(owner, owner));
		assertFalse(HeartNodeRules.mayOpen(owner, UUID.randomUUID()));
		assertFalse(HeartNodeRules.mayOpen(null, owner));
	}

	@Test
	void theNodeMaturesInThreeStagesOverThreeMinutes() {
		long planted = 10_000L;
		assertEquals(0, HeartNodeRules.stageAt(planted, planted));
		assertEquals(0, HeartNodeRules.stageAt(planted, planted - 500), "a clock behind planting reads as fresh");
		assertEquals(0, HeartNodeRules.stageAt(planted, planted + HeartNodeRules.MATURE_TICKS / 3 - 1));
		assertEquals(1, HeartNodeRules.stageAt(planted, planted + HeartNodeRules.MATURE_TICKS / 3));
		assertEquals(2, HeartNodeRules.stageAt(planted, planted + HeartNodeRules.MATURE_TICKS * 2 / 3));
		assertEquals(3, HeartNodeRules.stageAt(planted, planted + HeartNodeRules.MATURE_TICKS));
		assertEquals(3, HeartNodeRules.stageAt(HeartNodeRules.LONG_GROWN, 0L), "an old node is mature on any clock");
		assertEquals(3, HeartNodeRules.stageAt(HeartNodeRules.LONG_GROWN, Long.MAX_VALUE / 2));
		assertEquals(3 * 60 * 20, HeartNodeRules.MATURE_TICKS);
	}

	@Test
	void theTimeToTheNextStageCountsDownToEachStep() {
		long planted = 10_000L;
		long third = HeartNodeRules.MATURE_TICKS / 3;
		assertEquals(third, HeartNodeRules.ticksToNextStage(planted, planted));
		assertEquals(1, HeartNodeRules.ticksToNextStage(planted, planted + third - 1));
		assertEquals(third, HeartNodeRules.ticksToNextStage(planted, planted + third));
		assertEquals(1, HeartNodeRules.ticksToNextStage(planted, planted + HeartNodeRules.MATURE_TICKS - 1));
		assertEquals(0, HeartNodeRules.ticksToNextStage(planted, planted + HeartNodeRules.MATURE_TICKS));
		assertEquals(0, HeartNodeRules.ticksToNextStage(HeartNodeRules.LONG_GROWN, 0L));
		for (long age = 0; age < HeartNodeRules.MATURE_TICKS; age += 97) {
			long wait = HeartNodeRules.ticksToNextStage(planted, planted + age);
			assertEquals(HeartNodeRules.stageAt(planted, planted + age) + 1,
					HeartNodeRules.stageAt(planted, planted + age + wait), "the wait lands on the step");
			assertEquals(HeartNodeRules.stageAt(planted, planted + age),
					HeartNodeRules.stageAt(planted, planted + age + wait - 1), "and not before it");
		}
	}

	@Test
	void leavingTheSanctumWarnsOnlyOnTheWayOutAndAtMostOnceEveryTenSeconds() {
		long now = 5_000L;
		assertTrue(HeartNodeRules.warnsLeaving(true, false, HeartNodeRules.NEVER_WARNED, now));
		assertTrue(HeartNodeRules.warnsLeaving(true, false, HeartNodeRules.NEVER_WARNED, Long.MIN_VALUE + 5),
				"the never-warned mark is not a time to subtract from");
		assertFalse(HeartNodeRules.warnsLeaving(false, true, HeartNodeRules.NEVER_WARNED, now), "coming home is silent");
		assertFalse(HeartNodeRules.warnsLeaving(true, true, HeartNodeRules.NEVER_WARNED, now));
		assertFalse(HeartNodeRules.warnsLeaving(false, false, HeartNodeRules.NEVER_WARNED, now));
		assertFalse(HeartNodeRules.warnsLeaving(true, false, now - HeartNodeRules.EDGE_WARNING_TICKS + 1, now));
		assertTrue(HeartNodeRules.warnsLeaving(true, false, now - HeartNodeRules.EDGE_WARNING_TICKS, now));
		assertEquals(200, HeartNodeRules.EDGE_WARNING_TICKS);
	}

	@Test
	void sporeLoadScalesWithStrength() {
		assertEquals(HeartNodeRules.BONUS_SPORES, HeartNodeRules.bonusSpores(1.0D), 1e-6);
		assertEquals(HeartNodeRules.BONUS_SPORES / 2f, HeartNodeRules.bonusSpores(0.5D), 1e-6);
		assertEquals(0f, HeartNodeRules.bonusSpores(0.0D), 1e-6);
	}
}
