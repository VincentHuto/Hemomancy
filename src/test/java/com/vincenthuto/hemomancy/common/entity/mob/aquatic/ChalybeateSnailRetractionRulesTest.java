package com.vincenthuto.hemomancy.common.entity.mob.aquatic;

public final class ChalybeateSnailRetractionRulesTest {
	private ChalybeateSnailRetractionRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals("nearby threat starts a meaningful hold",
				ChalybeateSnailRetractionRules.THREAT_HOLD_TICKS,
				ChalybeateSnailRetractionRules.refreshForThreat(0, true));
		assertEquals("nearby threat refreshes a nearly expired tuck",
				ChalybeateSnailRetractionRules.THREAT_HOLD_TICKS,
				ChalybeateSnailRetractionRules.refreshForThreat(12, true));
		assertEquals("longer interaction tuck is not shortened by threat refresh",
				ChalybeateSnailRetractionRules.INTERACTION_TUCK_TICKS,
				ChalybeateSnailRetractionRules.refreshForThreat(
						ChalybeateSnailRetractionRules.INTERACTION_TUCK_TICKS, true));
		assertEquals("timer ticks down by one",
				4,
				ChalybeateSnailRetractionRules.tickDown(5));
		assertEquals("timer never goes negative",
				0,
				ChalybeateSnailRetractionRules.tickDown(0));
		assertTrue("positive timer renders tucked",
				ChalybeateSnailRetractionRules.shouldRenderRetracted(false, 1));
		assertTrue("synced flag still renders tucked",
				ChalybeateSnailRetractionRules.shouldRenderRetracted(true, 0));
		assertFalse("no flag and no timer renders extended",
				ChalybeateSnailRetractionRules.shouldRenderRetracted(false, 0));
		assertFalse("other vent snails are not threats",
				ChalybeateSnailRetractionRules.isThreat(false, false, true));
		assertFalse("creative players are not threats",
				ChalybeateSnailRetractionRules.isThreat(true, false, false));
		assertFalse("vanilla aquatic mobs are not threats",
				ChalybeateSnailRetractionRules.isThreat(false, true, false));
		assertTrue("ordinary non-aquatic mobs are threats",
				ChalybeateSnailRetractionRules.isThreat(false, false, false));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
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
