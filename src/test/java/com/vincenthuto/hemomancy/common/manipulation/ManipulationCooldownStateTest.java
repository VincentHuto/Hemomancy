package com.vincenthuto.hemomancy.common.manipulation;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ManipulationCooldownStateTest {
    @Test
    void castingOneManipulationDoesNotCoolDownAnother() {
        ManipulationCooldownState cooldowns = new ManipulationCooldownState();
        UUID player = UUID.randomUUID();

        cooldowns.start(player, "blood_binding", 100L, 60L);

        assertTrue(cooldowns.isOnCooldown(player, "blood_binding", 100L));
        assertFalse(cooldowns.isOnCooldown(player, "ironhearted", 100L));
    }

    @Test
    void simultaneousManipulationsKeepIndependentExpiryTimes() {
        ManipulationCooldownState cooldowns = new ManipulationCooldownState();
        UUID player = UUID.randomUUID();

        cooldowns.start(player, "blood_binding", 100L, 20L);
        cooldowns.start(player, "ironhearted", 100L, 60L);

        assertEquals(0L, cooldowns.remainingTicks(player, "blood_binding", 120L));
        assertEquals(40L, cooldowns.remainingTicks(player, "ironhearted", 120L));
        assertFalse(cooldowns.isOnCooldown(player, "blood_binding", 120L));
        assertTrue(cooldowns.isOnCooldown(player, "ironhearted", 120L));
    }

    @Test
    void clearingSessionStateRemovesEveryPlayersManipulationCooldowns() {
        ManipulationCooldownState cooldowns = new ManipulationCooldownState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        cooldowns.start(first, "blood_binding", 10L, 60L);
        cooldowns.start(second, "ironhearted", 10L, 80L);

        cooldowns.clear();

        assertFalse(cooldowns.isOnCooldown(first, "blood_binding", 10L));
        assertFalse(cooldowns.isOnCooldown(second, "ironhearted", 10L));
    }
}
