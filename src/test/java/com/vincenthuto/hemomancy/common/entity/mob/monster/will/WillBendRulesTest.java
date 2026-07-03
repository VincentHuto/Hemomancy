package com.vincenthuto.hemomancy.common.entity.mob.monster.will;

public final class WillBendRulesTest {
	private WillBendRulesTest() {
	}

	public static void main(String[] args) {
		brokenFalteringWillResolvesBendVerbs();
		sentOrNonFalteringWillBackfires();
		costsAndSilentArchonEdgeStayStable();
	}

	private static void brokenFalteringWillResolvesBendVerbs() {
		assertEquals("plain use absorbs",
				WillBendRules.BendVerb.ABSORB,
				WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
						WillBendRules.HeldItemKind.EMPTY_OR_STAFF, false, true, false).verb());
		assertEquals("sneak plain use redirects",
				WillBendRules.BendVerb.REDIRECT,
				WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
						WillBendRules.HeldItemKind.EMPTY_OR_STAFF, true, true, false).verb());
		assertEquals("crossbar commandeers",
				WillBendRules.BendVerb.COMMANDEER,
				WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
						WillBendRules.HeldItemKind.MARIONETTE_CROSSBAR, false, true, false).verb());
	}

	private static void sentOrNonFalteringWillBackfires() {
		assertTrue("sent will backfires",
				WillBendRules.resolve(WillOrigin.SENT, WillPhase.FALTERING, 7,
						WillBendRules.HeldItemKind.EMPTY_OR_STAFF, false, true, false).backfire());
		assertTrue("materialized broken will backfires",
				WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.MATERIALIZED, 7,
						WillBendRules.HeldItemKind.EMPTY_OR_STAFF, false, true, false).backfire());
		assertTrue("under-degree broken will backfires",
				WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 6,
						WillBendRules.HeldItemKind.EMPTY_OR_STAFF, false, true, false).backfire());
	}

	private static void costsAndSilentArchonEdgeStayStable() {
		WillBendRules.BendOutcome redirect = WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
				WillBendRules.HeldItemKind.EMPTY_OR_STAFF, true, true, false);
		assertEquals("redirect blood cost", 400, redirect.bloodCost());
		WillBendRules.BendOutcome command = WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
				WillBendRules.HeldItemKind.MARIONETTE_CROSSBAR, false, true, false);
		WillBendRules.BendOutcome silentCommand = WillBendRules.resolve(WillOrigin.BROKEN, WillPhase.FALTERING, 7,
				WillBendRules.HeldItemKind.MARIONETTE_CROSSBAR, false, true, true);
		assertEquals("base commandeer thread cost", 56, command.threadCost());
		assertEquals("silent archon half thread cost", 28, silentCommand.threadCost());
		assertEquals("silent archon bonus cap", 1, WillBendRules.silentArchonBonusCap());
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(String label, boolean condition) {
		if (!condition) {
			throw new AssertionError(label);
		}
	}
}
