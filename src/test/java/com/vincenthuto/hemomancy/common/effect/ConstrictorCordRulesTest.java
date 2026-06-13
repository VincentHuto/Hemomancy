package com.vincenthuto.hemomancy.common.effect;

public final class ConstrictorCordRulesTest {
	private ConstrictorCordRulesTest() {
	}

	public static void main(String[] args) {
		assertEquals(100, ConstrictorCordRules.DURATION_TICKS, "normal duration");
		assertEquals(50, ConstrictorCordRules.BOSS_DURATION_TICKS, "boss duration");
		assertEquals(20, ConstrictorCordRules.USE_COOLDOWN_TICKS, "use cooldown");
		assertEquals(1.25D, ConstrictorCordRules.PROJECTILE_VELOCITY, "projectile velocity");
		assertEquals(3, ConstrictorCordRules.BLOOD_BINDING_AMPLIFIER, "blood binding amplifier");
		assertTrue(ConstrictorCordRules.MOVEMENT_SPEED_MULTIPLIER > 0.0D, "movement multiplier stays above zero");
		assertTrue(ConstrictorCordRules.MOVEMENT_SPEED_MULTIPLIER < 0.15D, "movement multiplier strongly constricts");
	}

	private static void assertEquals(int expected, int actual, String label) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertEquals(double expected, double actual, String label) {
		if (Math.abs(expected - actual) > 0.001D) {
			throw new AssertionError(label + ": expected " + expected + " got " + actual);
		}
	}

	private static void assertTrue(boolean value, String label) {
		if (!value) {
			throw new AssertionError(label);
		}
	}
}
