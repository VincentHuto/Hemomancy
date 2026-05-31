package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class PrimalMorphlingRulesTest {
	private PrimalMorphlingRulesTest() {
	}

	public static void main(String[] args) {
		assertFalse("raw enzyme power alone never grants primal",
				PrimalMorphlingRules.isPrimalMaturity(250.0f, false));
		assertTrue("primal marker grants primal maturity",
				PrimalMorphlingRules.isPrimalMaturity(100.0f, true));
		assertTrue("apex apotheos player can primalize",
				PrimalMorphlingRules.canPrimalize(4, false, 8));
		assertFalse("non-apex morphling cannot primalize",
				PrimalMorphlingRules.canPrimalize(3, false, 8));
		assertFalse("apex morphling cannot primalize before apotheos",
				PrimalMorphlingRules.canPrimalize(4, false, 7));
		assertFalse("already primal morphling cannot primalize again",
				PrimalMorphlingRules.canPrimalize(5, true, 8));
		assertEquals("primal morphogenesis reduces morphic strain duration", 154,
				PrimalMorphlingRules.morphicStrainDuration(220, 3));
		assertDouble("primal morphogenesis improves primal bonus scaling", 1.15,
				PrimalMorphlingRules.primalBonusMultiplier(3));
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
}
