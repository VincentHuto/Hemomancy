package com.vincenthuto.hemomancy.common.event;

public final class SilentArchonArmorRulesTest {
	private SilentArchonArmorRulesTest() {
	}

	public static void main(String[] args) {
		silentDegreeSevenCanRefuseLethalDamage();
		gatesRejectApotheosMissingChoiceCooldownAndBlood();
		leavesPlayerBarelyAliveAndStartsLongCooldown();
	}

	private static void silentDegreeSevenCanRefuseLethalDamage() {
		assertTrue("silent archon at degree seven qualifies",
				SilentArchonArmorRules.canRefuseDeath(true, 7, "silent", 4000, 3000, 2000, 0));
	}

	private static void gatesRejectApotheosMissingChoiceCooldownAndBlood() {
		assertFalse("full set required",
				SilentArchonArmorRules.canRefuseDeath(false, 7, "silent", 4000, 3000, 2000, 0));
		assertFalse("degree eight apotheos players are excluded",
				SilentArchonArmorRules.canRefuseDeath(true, 8, "silent", 4000, 3000, 2000, 0));
		assertFalse("silent choice is required",
				SilentArchonArmorRules.canRefuseDeath(true, 7, "apotheos", 4000, 3000, 2000, 0));
		assertFalse("enough player blood is required",
				SilentArchonArmorRules.canRefuseDeath(true, 7, "silent", 2000, 3000, 2000, 0));
		assertFalse("cooldown blocks activation",
				SilentArchonArmorRules.canRefuseDeath(true, 7, "silent", 4000, 3000, 2000, 2100));
	}

	private static void leavesPlayerBarelyAliveAndStartsLongCooldown() {
		assertDouble("new damage leaves one heart sliver", 9.5F,
				SilentArchonArmorRules.damageLeavingBarelyAlive(10.5F));
		assertEquals("death save cooldown", 12_000, SilentArchonArmorRules.DEATH_REFUSAL_COOLDOWN_TICKS);
		assertEquals("cooldown target", 14_000L,
				SilentArchonArmorRules.nextCooldownUntil(2_000L));
	}

	private static void assertTrue(String label, boolean value) {
		if (!value) {
			throw new AssertionError(label + ": expected true");
		}
	}

	private static void assertFalse(String label, boolean value) {
		if (value) {
			throw new AssertionError(label + ": expected false");
		}
	}

	private static void assertEquals(String label, int expected, int actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}

	private static void assertEquals(String label, long expected, long actual) {
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
