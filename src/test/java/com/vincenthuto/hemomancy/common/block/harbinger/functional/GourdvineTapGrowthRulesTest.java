package com.vincenthuto.hemomancy.common.block.harbinger.functional;

public final class GourdvineTapGrowthRulesTest {
	private GourdvineTapGrowthRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("paste advances one stage", 2, GourdvineTapGrowthRules.advance(1, 1));
		assertEquals("enzyme advances two stages", 3, GourdvineTapGrowthRules.advance(1, 2));
		assertEquals("growth clamps at mature", 3, GourdvineTapGrowthRules.advance(2, 2));
		assertEquals("invalid boosts do nothing", 2, GourdvineTapGrowthRules.advance(2, 0));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
