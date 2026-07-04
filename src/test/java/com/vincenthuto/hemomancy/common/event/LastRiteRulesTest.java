package com.vincenthuto.hemomancy.common.event;

public final class LastRiteRulesTest {
	private LastRiteRulesTest() {
	}

	public static void main(String[] args) {
		armedRiteMayFireWhenNoCooldownIsActive();
		unarmedRiteNeverFires();
		armedMismatchNeverFires();
		sharedCooldownBlocksEverySource();
		cooldownExpiryFreesTheArmedSourceOnly();
		emptySourceIdsNeverFire();
	}

	private static void armedRiteMayFireWhenNoCooldownIsActive() {
		assertTrue("ink mantle fires when it is armed",
				LastRiteRules.canFire("hemomancy:ink_mantle", "hemomancy:ink_mantle", 1_000L, 0L));
	}

	private static void unarmedRiteNeverFires() {
		assertFalse("fresh unarmed players must be armed before any rite fires",
				LastRiteRules.canFire("", "hemomancy:ink_mantle", 1_000L, 0L));
	}

	private static void armedMismatchNeverFires() {
		assertFalse("silent refusal cannot fire while ink mantle is armed",
				LastRiteRules.canFire("hemomancy:ink_mantle", "hemomancy:silent_refusal", 1_000L, 0L));
	}

	private static void sharedCooldownBlocksEverySource() {
		long until = LastRiteRules.nextSharedCooldownUntil(1_000L, LastRiteRules.DEFAULT_SHARED_COOLDOWN_TICKS);
		assertEquals("cooldown target", 13_000L, until);
		assertFalse("the same rite cannot refire inside the window",
				LastRiteRules.canFire("hemomancy:ink_mantle", "hemomancy:ink_mantle", 5_000L, until));
		assertFalse("a different rite is blocked by the shared window",
				LastRiteRules.canFire("hemomancy:ink_mantle", "hemomancy:silent_refusal", 5_000L, until));
	}

	private static void cooldownExpiryFreesTheArmedSourceOnly() {
		long until = LastRiteRules.nextSharedCooldownUntil(1_000L, LastRiteRules.DEFAULT_SHARED_COOLDOWN_TICKS);
		assertTrue("after expiry the armed source may fire",
				LastRiteRules.canFire("hemomancy:ink_mantle", "hemomancy:ink_mantle", until, until));
		assertFalse("after expiry an unarmed source still cannot fire",
				LastRiteRules.canFire("hemomancy:ink_mantle", "hemomancy:silent_refusal", until, until));
	}

	private static void emptySourceIdsNeverFire() {
		assertFalse("empty request id is invalid", LastRiteRules.canFire("", "", 1_000L, 0L));
		assertFalse("null request id is invalid", LastRiteRules.canFire("", null, 1_000L, 0L));
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

	private static void assertEquals(String label, long expected, long actual) {
		if (expected != actual) {
			throw new AssertionError(label + ": expected " + expected + " but got " + actual);
		}
	}
}
