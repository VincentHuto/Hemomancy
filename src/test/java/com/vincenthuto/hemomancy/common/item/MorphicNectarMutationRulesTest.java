package com.vincenthuto.hemomancy.common.item;

public final class MorphicNectarMutationRulesTest {
	private MorphicNectarMutationRulesTest() {
	}

	public static void main(String[] args) {
		assertTrue("explicit nectar marker shows mutation",
				MorphicNectarMutationRules.shouldShowMutation(true, false));
		assertTrue("primal morphling implies nectar mutation",
				MorphicNectarMutationRules.shouldShowMutation(false, true));
		assertFalse("unmarked non-primal item is not mutated",
				MorphicNectarMutationRules.shouldShowMutation(false, false));
		assertEquals("generic mutated border color", 0xAA516414,
				MorphicNectarMutationRules.borderColor(false));
		assertEquals("primal border color", 0xDDD13218,
				MorphicNectarMutationRules.borderColor(true));
		assertEquals("blood tendril color", 0xDDD13218,
				MorphicNectarMutationRules.tendrilColor(0));
		assertEquals("fungal tendril color", 0xCC60791C,
				MorphicNectarMutationRules.tendrilColor(1));
		assertEquals("bile tendril color", 0xFFE0B536,
				MorphicNectarMutationRules.tendrilColor(2));
		assertEquals("primal tendril count stays readable", 2,
				MorphicNectarMutationRules.primalTendrilCount());
		assertEquals("primal tendril animation is slower and less frantic", 140,
				MorphicNectarMutationRules.primalTendrilFrameMillis());
		assertEquals("primal tendrils are long enough to read as connected", 11,
				MorphicNectarMutationRules.primalTendrilSegments());
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
