package com.vincenthuto.hemomancy.common.manipulation.animus;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** LP-16: a boss takes max HP / 100 bindings in its life, and each attempt can slip. */
class BossBindingRulesTest {
	@Test
	void aBossTakesOneBindingPerHundredHealth() {
		assertEquals(10, BossBindingRules.total(1000.0D));
		assertEquals(3, BossBindingRules.total(300.0D), "the Wither");
		assertEquals(5, BossBindingRules.total(500.0D), "the Warden");
		assertEquals(1, BossBindingRules.total(60.0D), "at least one");
		assertEquals(1, BossBindingRules.total(0.0D));
	}

	@Test
	void theSlipGrowsWithMaximumHealthAndWithHowHealthyTheBossIs() {
		assertEquals(0.5D, BossBindingRules.failChance(1000.0D, 1000.0D), 1e-9, "a 1,000 HP boss at full health slips half the time");
		assertEquals(0.25D, BossBindingRules.failChance(1000.0D, 0.0D), 1e-9, "a dying one half as often");
		assertEquals(0.10D, BossBindingRules.failChance(100.0D, 100.0D), 1e-9, "the base is held at 10%");
		assertEquals(0.15D, BossBindingRules.failChance(300.0D, 300.0D), 1e-9, "the Wither at full health");
		assertEquals(0.60D, BossBindingRules.failChance(5000.0D, 5000.0D), 1e-9, "and at 60%");
		assertEquals(0.45D, BossBindingRules.failChance(1000.0D, 800.0D), 1e-9);
		assertEquals(60, BossBindingRules.RETRY_WAIT_TICKS);
	}
}
