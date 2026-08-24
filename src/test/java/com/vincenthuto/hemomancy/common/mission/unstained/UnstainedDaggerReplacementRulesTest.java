package com.vincenthuto.hemomancy.common.mission.unstained;

public final class UnstainedDaggerReplacementRulesTest {
	private UnstainedDaggerReplacementRulesTest() {}

	public static void main(String[] args) {
		assertTrue(UnstainedDaggerReplacementRules.canExchange(true, false, 2, 1));
		assertFalse(UnstainedDaggerReplacementRules.canExchange(false, false, 2, 1));
		assertFalse(UnstainedDaggerReplacementRules.canExchange(true, true, 2, 1));
		assertFalse(UnstainedDaggerReplacementRules.canExchange(true, false, 1, 1));
		assertFalse(UnstainedDaggerReplacementRules.canExchange(true, false, 2, 0));
	}

	private static void assertTrue(boolean value) { if (!value) throw new AssertionError(); }
	private static void assertFalse(boolean value) { if (value) throw new AssertionError(); }
}
