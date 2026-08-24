package com.vincenthuto.hemomancy.common.capability.player.unstained;

public final class UnstainedEntryRulesTest {
	private UnstainedEntryRulesTest() {}
	public static void main(String[] args) {
		assertTrue("non-founder remains curable", UnstainedEntryRules.canBeginCure(false, false));
		assertFalse("founder crosses the ordinary cutoff", UnstainedEntryRules.canBeginCure(true, false));
		assertTrue("Annetta severance restores the exceptional route", UnstainedEntryRules.canBeginCure(true, true));
		assertTrue("active D0 is suppressible", UnstainedEntryRules.canSuppressForCure(true, 0, false, false));
		assertTrue("active D5 is suppressible", UnstainedEntryRules.canSuppressForCure(true, 5, false, false));
		assertFalse("healthy candidates use vows", UnstainedEntryRules.canSuppressForCure(false, 0, false, false));
		assertFalse("D6 is beyond ordinary cure", UnstainedEntryRules.canSuppressForCure(true, 6, false, false));
		assertFalse("founder must sever first", UnstainedEntryRules.canSuppressForCure(true, 5, true, false));
		assertTrue("severed founder is suppressible", UnstainedEntryRules.canSuppressForCure(true, 8, true, true));
	}
	private static void assertTrue(String label, boolean value) { if (!value) throw new AssertionError(label); }
	private static void assertFalse(String label, boolean value) { if (value) throw new AssertionError(label); }
}
