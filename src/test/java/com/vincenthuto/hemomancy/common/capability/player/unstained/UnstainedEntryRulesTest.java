package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedEntryRulesTest {
	private UnstainedEntryRulesTest() {}
	public static void main(String[] args) {
		assertTrue("non-founder remains curable", UnstainedEntryRules.canBeginCure(false, false));
		assertFalse("founder crosses the ordinary cutoff", UnstainedEntryRules.canBeginCure(true, false));
		assertTrue("Annetta severance restores the exceptional route", UnstainedEntryRules.canBeginCure(true, true));
		assertTrue("an active non-founder is suppressible", UnstainedEntryRules.canSuppressForCure(true, false, false));
		assertFalse("healthy candidates use vows", UnstainedEntryRules.canSuppressForCure(false, false, false));
		assertFalse("founder must sever first", UnstainedEntryRules.canSuppressForCure(true, true, false));
		assertTrue("severed founder is suppressible", UnstainedEntryRules.canSuppressForCure(true, true, true));
	}
	private static void assertTrue(String label, boolean value) { if (!value) throw new AssertionError(label); }
	private static void assertFalse(String label, boolean value) { if (value) throw new AssertionError(label); }
}
