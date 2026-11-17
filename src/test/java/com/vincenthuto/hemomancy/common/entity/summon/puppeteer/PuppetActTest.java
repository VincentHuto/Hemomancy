package com.vincenthuto.hemomancy.common.entity.summon.puppeteer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/** D-37, D-38, NQ-15-8 A, NQ-15-9 A: one act per taught body, the Pattern's Ring, and the act arithmetic. */
class PuppetActTest {
	@Test
	void everyBodyHasItsNamedAct() {
		Map<String, PuppetAct> bySummon = java.util.Arrays.stream(PuppetAct.values())
				.collect(Collectors.toMap(PuppetAct::summon, act -> act));
		assertEquals(PuppetAct.SPOTLIGHT, bySummon.get(PuppeteerSummonDefinitions.SCARLET_MUMMER));
		assertEquals(PuppetAct.TRAPEZE, bySummon.get(PuppeteerSummonDefinitions.VEINWING_VULTURE));
		assertEquals(PuppetAct.FEINT, bySummon.get(PuppeteerSummonDefinitions.MARROW_SPITTER));
		assertEquals(PuppetAct.SMOKE, bySummon.get(PuppeteerSummonDefinitions.CINDER_BELLOWS));
		assertEquals(PuppetAct.ENCORE, bySummon.get(PuppeteerSummonDefinitions.MNEMONIST_PUPPET));
		assertEquals(PuppetAct.SCENT, bySummon.get(PuppeteerSummonDefinitions.SANGUINE_HOUND));
		assertEquals(PuppetAct.LIFT, bySummon.get(PuppeteerSummonDefinitions.GOREBOUND_HULK));
		assertEquals(PuppetAct.RING, bySummon.get(PuppeteerSummonDefinitions.RINGMASTER_PATTERN));
		for (PuppeteerSummonDefinition definition : PuppeteerSummonDefinitions.all()) {
			assertTrue(PuppetAct.forSummon(definition.name()).isPresent(), definition.name() + " has no act");
		}
		assertTrue(PuppetAct.forSummon("claimed_will").isEmpty());
	}

	@Test
	void everyActCostsThreadAndWaits() {
		for (PuppetAct act : PuppetAct.values()) {
			assertTrue(act.threadCost() > 0 && act.cooldownTicks() >= 20, act + " is free or has no cooldown");
		}
		assertTrue(PuppetAct.RING.cooldownTicks() > PuppetAct.SPOTLIGHT.cooldownTicks(), "the group act waits longest");
	}

	@Test
	void cooldownsCountWholeSecondsUp() {
		assertTrue(PuppetActRules.isReady(100, 100));
		assertFalse(PuppetActRules.isReady(99, 100));
		assertEquals(0, PuppetActRules.secondsLeft(100, 100));
		assertEquals(1, PuppetActRules.secondsLeft(99, 100));
		assertEquals(1, PuppetActRules.secondsLeft(80, 100));
		assertEquals(2, PuppetActRules.secondsLeft(79, 100));
	}

	@Test
	void trapezeReachesTwentyFourBlocks() {
		assertTrue(PuppetActRules.withinTrapeze(24.0D * 24.0D));
		assertFalse(PuppetActRules.withinTrapeze(24.1D * 24.1D));
	}

	/** NQ-15-9 A: the Pattern stays callable but leaves the transposition ring. */
	@Test
	void thePatternLeavesTheTranspositionRing() {
		assertFalse(PuppeteerSummonRules.inTranspositionRing(PuppeteerSummonDefinitions.RINGMASTER_PATTERN));
		assertTrue(PuppeteerSummonRules.inTranspositionRing(PuppeteerSummonDefinitions.SCARLET_MUMMER));
	}
}
