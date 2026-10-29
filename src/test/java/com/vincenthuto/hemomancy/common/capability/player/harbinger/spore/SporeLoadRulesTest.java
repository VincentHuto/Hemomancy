package com.vincenthuto.hemomancy.common.capability.player.harbinger.spore;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SporeLoadRulesTest {
	@Test
	void sporesGatherOnlyInTheGardens() {
		assertEquals(2.0f, SporeLoadRules.gainPerSecond(true, false));
		assertEquals(0.5f, SporeLoadRules.gainPerSecond(false, true));
		assertEquals(0.0f, SporeLoadRules.gainPerSecond(false, false));
		assertEquals(0.0f, SporeLoadRules.withinCapacity(-3f));
		assertEquals(100.0f, SporeLoadRules.withinCapacity(140f));
	}

	@Test
	void aBurnSpendsNothingUnlessItCanHeal() {
		assertEquals(SporeLoadRules.BurnResult.WHOLE, SporeLoadRules.burn(50f, 20f, 20f, true));
		assertEquals(SporeLoadRules.BurnResult.COOLING, SporeLoadRules.burn(50f, 10f, 20f, false));
		assertEquals(SporeLoadRules.BurnResult.TOO_FEW_SPORES, SporeLoadRules.burn(20f, 10f, 20f, true));
		assertEquals(SporeLoadRules.BurnResult.OK, SporeLoadRules.burn(25f, 10f, 20f, true));
	}

	@Test
	void sinkingNeedsAStillCrouchOnFungalGround() {
		assertTrue(SporeLoadRules.isSinking(true, true, 0.0001, true, true));
		assertFalse(SporeLoadRules.isSinking(false, true, 0.0001, true, true));
		assertFalse(SporeLoadRules.isSinking(true, false, 0.0001, true, true));
		assertFalse(SporeLoadRules.isSinking(true, true, 0.01, true, true));
		assertFalse(SporeLoadRules.isSinking(true, true, 0.0001, false, true));
		assertFalse(SporeLoadRules.isSinking(true, true, 0.0001, true, false), "a carried fungal block outside the Gardens is not the network");
	}

	@Test
	void regrowthStartsAfterTwoSecondsAndPulsesEverySecond() {
		assertEquals(0f, SporeLoadRules.sinkHeal(39));
		assertEquals(2f, SporeLoadRules.sinkHeal(40));
		assertEquals(0f, SporeLoadRules.sinkHeal(41));
		assertEquals(2f, SporeLoadRules.sinkHeal(60));
	}
}
