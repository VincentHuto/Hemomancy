package com.vincenthuto.hemomancy.common.item.harbinger.morphlings;

public final class MorphlingHungerRulesTest {
	private MorphlingHungerRulesTest() {
	}

	public static void main(String[] args) {
		disabledHungerIsInert();
		matureWildBoundMorphlingsAreExempt();
		enabledMatureMorphlingsMoveThroughHungerStates();
		hungryAndStarvingPreservePassiveAmplifier();
	}

	private static void disabledHungerIsInert() {
		assertEquals("disabled state", MorphlingHungerRules.HungerState.DISABLED,
				MorphlingHungerRules.state(false, 5, false, 6000L, 0L, 1200));
		assertEquals("disabled amplifier", 3,
				MorphlingHungerRules.adjustedAmplifier(3, MorphlingHungerRules.HungerState.DISABLED));
	}

	private static void matureWildBoundMorphlingsAreExempt() {
		assertEquals("wild-bound exempt", MorphlingHungerRules.HungerState.EXEMPT,
				MorphlingHungerRules.state(true, 3, true, 6000L, 0L, 1200));
		assertEquals("developing exempt", MorphlingHungerRules.HungerState.EXEMPT,
				MorphlingHungerRules.state(true, 2, false, 6000L, 0L, 1200));
	}

	private static void enabledMatureMorphlingsMoveThroughHungerStates() {
		assertEquals("recently fed", MorphlingHungerRules.HungerState.FED,
				MorphlingHungerRules.state(true, 3, false, 2000L, 1000L, 1200));
		assertEquals("unfed starts hungry", MorphlingHungerRules.HungerState.HUNGRY,
				MorphlingHungerRules.state(true, 3, false, 2000L, 0L, 1200));
		assertEquals("past one duration is hungry", MorphlingHungerRules.HungerState.HUNGRY,
				MorphlingHungerRules.state(true, 3, false, 2600L, 1000L, 1200));
		assertEquals("past three durations is starving", MorphlingHungerRules.HungerState.STARVING,
				MorphlingHungerRules.state(true, 3, false, 4700L, 1000L, 1200));
	}

	private static void hungryAndStarvingPreservePassiveAmplifier() {
		assertEquals("fed keeps amplifier", 2,
				MorphlingHungerRules.adjustedAmplifier(2, MorphlingHungerRules.HungerState.FED));
		assertEquals("hungry keeps amplifier", 2,
				MorphlingHungerRules.adjustedAmplifier(2, MorphlingHungerRules.HungerState.HUNGRY));
		assertEquals("starving keeps amplifier", 2,
				MorphlingHungerRules.adjustedAmplifier(2, MorphlingHungerRules.HungerState.STARVING));
		assertEquals("zero remains zero", 0,
				MorphlingHungerRules.adjustedAmplifier(0, MorphlingHungerRules.HungerState.HUNGRY));
	}

	private static void assertEquals(String label, Object expected, Object actual) {
		if (!expected.equals(actual)) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
