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

    @Test
    void cappingShortensButNeverStartsOrLengthensACooldown() {
        ManipulationCooldownState cooldowns = new ManipulationCooldownState();
        UUID player = UUID.randomUUID();

        assertEquals(0L, cooldowns.capRemaining(player, "hematic_riposte", 100L, 10L));
        assertFalse(cooldowns.isOnCooldown(player, "hematic_riposte", 100L));

        cooldowns.start(player, "hematic_riposte", 100L, 40L);
        assertEquals(10L, cooldowns.capRemaining(player, "hematic_riposte", 105L, 10L));
        assertEquals(10L, cooldowns.remainingTicks(player, "hematic_riposte", 105L));

        cooldowns.start(player, "blood_rush", 100L, 6L);
        assertEquals(6L, cooldowns.capRemaining(player, "blood_rush", 100L, 10L));
        assertEquals(6L, cooldowns.remainingTicks(player, "blood_rush", 100L));
    }

    @Test
    void cappingToZeroClearsTheCooldown() {
        ManipulationCooldownState cooldowns = new ManipulationCooldownState();
        UUID player = UUID.randomUUID();
        cooldowns.start(player, "hematic_riposte", 100L, 40L);

        assertEquals(0L, cooldowns.capRemaining(player, "hematic_riposte", 101L, 0L));
        assertFalse(cooldowns.isOnCooldown(player, "hematic_riposte", 101L));
    }
}
