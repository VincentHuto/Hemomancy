package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.UUID;

public final class HuskEffigyRulesTest {
    public static void main(String[] args) {
        UUID owner = UUID.fromString("00000000-0000-0000-0000-000000000001");
        UUID stranger = UUID.fromString("00000000-0000-0000-0000-000000000002");

        assertTrue(HuskEffigyRules.isOwnerTarget(owner, owner), "matching owner uuid should be treated as owner target");
        assertFalse(HuskEffigyRules.isOwnerTarget(owner, stranger), "different uuid should not be owner target");
        assertFalse(HuskEffigyRules.isOwnerTarget(null, owner), "missing owner uuid should not match");
        assertFalse(HuskEffigyRules.isOwnerTarget(owner, null), "missing target uuid should not match");
        assertTrue(HuskEffigyRules.shouldRejectTarget(owner, owner), "effigy should reject its owner as a target");
        assertFalse(HuskEffigyRules.shouldRejectTarget(owner, stranger), "effigy should allow non-owner targets");
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
