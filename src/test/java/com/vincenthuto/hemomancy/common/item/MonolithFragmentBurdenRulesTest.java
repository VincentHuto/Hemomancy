package com.vincenthuto.hemomancy.common.item;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.item.harbinger.MonolithFragmentBurdenRules;

public final class MonolithFragmentBurdenRulesTest {

	private MonolithFragmentBurdenRulesTest() {
	}

	public static void main(String[] args) {
		assertFalse("ordinary archon does not suffer the fragment burden",
				MonolithFragmentBurdenRules.shouldBurdenCarrier(EnumArchonPath.NONE));
		assertTrue("the Apotheos carries the rejected-self burden",
				MonolithFragmentBurdenRules.shouldBurdenCarrier(EnumArchonPath.APOTHEOS));
		assertFalse("a Silentium is not the hive's heir and is not burdened",
				MonolithFragmentBurdenRules.shouldBurdenCarrier(EnumArchonPath.SILENTIUM));
		assertEquals("burden check interval", 80, MonolithFragmentBurdenRules.BURDEN_CHECK_INTERVAL_TICKS);
		assertEquals("darkness burden duration", 100, MonolithFragmentBurdenRules.BURDEN_EFFECT_TICKS);
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label + ": expected true");
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label + ": expected false");
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
