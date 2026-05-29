package com.vincenthuto.hemomancy.common.entity.boss.endgame;

final class EndgameBossCombatRulesTest {
	public static void main(String[] args) {
		scalesLegacyAttackCadenceAsBossWeakens();
		keepsLegacyHealthThresholds();
	}

	private static void scalesLegacyAttackCadenceAsBossWeakens() {
		assertEquals("healthy cadence", 120,
				EndgameBossCombatRules.scaledCadence(120, 520.0F, 520.0F));
		assertEquals("armored cadence", 84,
				EndgameBossCombatRules.scaledCadence(120, 260.0F, 520.0F));
		assertEquals("vulnerable cadence", 42,
				EndgameBossCombatRules.scaledCadence(120, 130.0F, 520.0F));
		assertEquals("minimum cadence floor", EndgameBossCombatRules.MIN_ATTACK_CADENCE,
				EndgameBossCombatRules.scaledCadence(30, 5.0F, 520.0F));
	}

	private static void keepsLegacyHealthThresholds() {
		assertTrue(EndgameBossCombatRules.isArmored(260.0F, 520.0F),
				"boss should armor up at half health");
		assertFalse(EndgameBossCombatRules.isArmored(261.0F, 520.0F),
				"boss should stay unarmored above half health");
		assertTrue(EndgameBossCombatRules.isVulnerable(130.0F, 520.0F),
				"boss should enter vulnerable pressure at quarter health");
		assertFalse(EndgameBossCombatRules.isVulnerable(131.0F, 520.0F),
				"boss should leave vulnerable pressure above quarter health");
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertTrue(boolean condition, String message) {
		if (!condition) {
			throw new AssertionError(message);
		}
	}

	private static void assertFalse(boolean condition, String message) {
		assertTrue(!condition, message);
	}
}
