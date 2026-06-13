package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class VoidEyeOrganRulesTest {
    public static void main(String[] args) {
        assertTrue(VoidEyeOrganRules.shouldConsumeOnUse(false), "non-creative use should consume the organ");
        assertTrue(VoidEyeOrganRules.shouldConsumeOnUse(true), "creative use should still consume the organ");
        assertFalse(VoidEyeOrganRules.shouldApplyCooldownAfterUse(), "single-use organ should not leave a cooldown behind");
    }

    private static void assertTrue(boolean value, String message) {
        if (!value) {
            throw new AssertionError(message);
        }
    }

    private static void assertFalse(boolean value, String message) {
        if (value) {
            throw new AssertionError(message);
        }
    }
}
