package com.vincenthuto.hemomancy.common.summon;

public final class PuppeteerSummonRulesTest {
	private PuppeteerSummonRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("three v1 summons", 3, PuppeteerSummonDefinitions.all().size());

		PuppeteerSummonDefinition vulture = PuppeteerSummonDefinitions.byName("veinwing_vulture")
				.orElseThrow(() -> new AssertionError("missing veinwing vulture"));
		PuppeteerSummonDefinition spitter = PuppeteerSummonDefinitions.byName("marrow_spitter")
				.orElseThrow(() -> new AssertionError("missing marrow spitter"));
		PuppeteerSummonDefinition hulk = PuppeteerSummonDefinitions.byName("gorebound_hulk")
				.orElseThrow(() -> new AssertionError("missing gorebound hulk"));

		assertEquals("vulture degree", 2, vulture.requiredDegree());
		assertEquals("spitter degree", 3, spitter.requiredDegree());
		assertEquals("hulk degree", 4, hulk.requiredDegree());
		assertTrue("degree 2 can unlock vulture", PuppeteerSummonRules.canUnlockAtDegree(vulture, 2));
		assertFalse("degree 2 cannot unlock spitter", PuppeteerSummonRules.canUnlockAtDegree(spitter, 2));
		assertTrue("degree 4 can unlock hulk", PuppeteerSummonRules.canUnlockAtDegree(hulk, 4));

		assertEquals("base active cap", 1, PuppeteerSummonRules.activeSummonCap(0));
		assertEquals("puppet skein level 3 cap", 4, PuppeteerSummonRules.activeSummonCap(3));
		assertDouble("living sinew health scale", 1.45, PuppeteerSummonRules.healthMultiplier(3));
		assertDouble("living sinew damage scale", 1.30, PuppeteerSummonRules.damageMultiplier(3));
		assertDouble("far tether range", 40.0, PuppeteerSummonRules.commandRange(3));
		assertDouble("bound command extends range after far tether", 52.0, PuppeteerSummonRules.commandRange(3, 3));

		assertEquals("thread refill respects capacity", 256, PuppeteerSummonRules.refilledThread(240, 40));
		assertEquals("bound command raises thread capacity", 352, PuppeteerSummonRules.threadCapacity(3));
		assertEquals("thread refill respects upgraded capacity", 300, PuppeteerSummonRules.refilledThread(240, 60, 3));
		assertDouble("thread economy reduces call cost", 0.85, PuppeteerSummonRules.threadCostMultiplier(3));
		assertEquals("thread refill ignores negative input", 120, PuppeteerSummonRules.refilledThread(120, -5));
		assertTrue("vulture has higher upkeep than hulk", vulture.threadUpkeepPerMinute() > hulk.threadUpkeepPerMinute());

		assertEquals("missing crossbar dismissal lasts five seconds", 100, PuppeteerSummonRules.CROSSBAR_DISMISSAL_TICKS);
		assertEquals("bound command gives longer dismissal grace", 160, PuppeteerSummonRules.dismissalGraceTicks(3));
		assertDouble("stable summon renders fully", 1.0, PuppeteerSummonRules.dismissalAlpha(0, 0.0F));
		assertDouble("half dismissed summon renders half strength", 0.5, PuppeteerSummonRules.dismissalAlpha(50, 0.0F));
		assertDouble("expired dismissal is transparent", 0.0, PuppeteerSummonRules.dismissalAlpha(1, 1.0F));
		assertTrue("fresh dismissal is visible", PuppeteerSummonRules.shouldRenderDismissingSummon(20, 100));
		assertFalse("late dismissal flickers out", PuppeteerSummonRules.shouldRenderDismissingSummon(20, 10));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertDouble(String label, double expected, double actual) {
		if (Math.abs(expected - actual) > 0.000001) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label);
		}
	}
}
