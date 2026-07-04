package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

public final class WillSanctuaryRulesTest {
	private WillSanctuaryRulesTest() {
	}

	public static void main(String[] args) {
		anySanctuaryInputBlocksWillSpawns();
		noSanctuaryInputsAllowWillSpawns();
	}

	private static void anySanctuaryInputBlocksWillSpawns() {
		assertTrue("fane blocks", WillSanctuaryRules.isSanctuary(true, false, false));
		assertTrue("chamber blocks", WillSanctuaryRules.isSanctuary(false, true, false));
		assertTrue("outpost blocks", WillSanctuaryRules.isSanctuary(false, false, true));
	}

	private static void noSanctuaryInputsAllowWillSpawns() {
		assertFalse("no sanctuary", WillSanctuaryRules.isSanctuary(false, false, false));
	}

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}

	private static void assertFalse(String label, boolean condition) {
		if (condition) {
			throw new AssertionError(label);
		}
	}
}
