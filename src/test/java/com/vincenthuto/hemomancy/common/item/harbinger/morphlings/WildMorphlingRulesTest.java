package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class WildMorphlingRulesTest {
	private WildMorphlingRulesTest() {
	}

	public static void main(String[] args) {
		wildBoundMorphlingsAreCappedBeforeIncubation();
		primalizedMorphlingsIgnoreWildBoundCap();
		unboundMorphlingsUseNaturalMaturity();
	}

	private static void wildBoundMorphlingsAreCappedBeforeIncubation() {
		assertEquals("wild-bound apex power is capped to developing",
				2, WildMorphlingRules.applyMaturityCap(4, true, false));
	}

	private static void primalizedMorphlingsIgnoreWildBoundCap() {
		assertEquals("primal marker wins over wild-bound cap",
				5, WildMorphlingRules.applyMaturityCap(5, true, true));
	}

	private static void unboundMorphlingsUseNaturalMaturity() {
		assertEquals("unbound apex remains apex",
				4, WildMorphlingRules.applyMaturityCap(4, false, false));
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
