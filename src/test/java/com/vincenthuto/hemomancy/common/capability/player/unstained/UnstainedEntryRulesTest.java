package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedEntryRulesTest {
	private UnstainedEntryRulesTest() {}
	public static void main(String[] args) {
		assertTrue("non-founder remains curable", UnstainedEntryRules.canBeginCure(false, false));
		assertFalse("founder crosses the ordinary cutoff", UnstainedEntryRules.canBeginCure(true, false));
		assertTrue("Annetta severance restores the exceptional route", UnstainedEntryRules.canBeginCure(true, true));
	}
	private static void assertTrue(String label, boolean value) { if (!value) throw new AssertionError(label); }
	private static void assertFalse(String label, boolean value) { if (value) throw new AssertionError(label); }
}
