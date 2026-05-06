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
