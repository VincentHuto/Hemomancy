package com.vincenthuto.hemomancy.common.entity.summon;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class MnemonistPuppetRulesTest {
    private MnemonistPuppetRulesTest() {
    }

    public static void main(String[] args) {
        dropsOldestMemoryWhenBufferIsFull();
        expiresOldMemories();
        rejectsReplayDamageAndTargetMismatch();
        clampsReplayDamage();
        consumesMatchingReplayMemory();
    }

    private static void dropsOldestMemoryWhenBufferIsFull() {
        UUID target = UUID.randomUUID();
        List<MnemonistPuppetRules.AttackMemory> memories = new ArrayList<>();
        for (int i = 0; i < MnemonistPuppetRules.MAX_MEMORIES + 2; i++) {
            MnemonistPuppetRules.record(memories, new MnemonistPuppetRules.AttackMemory(target, i + 1, i));
        }
        assertEquals(MnemonistPuppetRules.MAX_MEMORIES, memories.size(), "memory cap");
        assertEquals(3.0F, memories.get(0).damage(), "oldest entries should be dropped");
    }

    private static void expiresOldMemories() {
        UUID target = UUID.randomUUID();
        List<MnemonistPuppetRules.AttackMemory> memories = new ArrayList<>();
        memories.add(new MnemonistPuppetRules.AttackMemory(target, 4.0F, 10L));
        memories.add(new MnemonistPuppetRules.AttackMemory(target, 5.0F, 160L));
        MnemonistPuppetRules.pruneExpired(memories, 10L + MnemonistPuppetRules.MEMORY_EXPIRY_TICKS + 1L);
        assertEquals(1, memories.size(), "expired memory count");
        assertEquals(5.0F, memories.get(0).damage(), "fresh memory should remain");
    }

    private static void rejectsReplayDamageAndTargetMismatch() {
        UUID target = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        assertFalse(MnemonistPuppetRules.canRecord(target, other, 4.0F, false), "target mismatch");
        assertFalse(MnemonistPuppetRules.canRecord(target, target, 4.0F, true), "replay recursion");
        assertFalse(MnemonistPuppetRules.canRecord(target, target, 0.0F, false), "zero damage");
        assertTrue(MnemonistPuppetRules.canRecord(target, target, 0.25F, false), "valid memory");
    }

    private static void clampsReplayDamage() {
        assertEquals(0.0F, MnemonistPuppetRules.replayDamage(0.0F), "zero replay");
        assertEquals(2.25F, MnemonistPuppetRules.replayDamage(5.0F), "fractional replay");
        assertEquals(MnemonistPuppetRules.MAX_REPLAY_DAMAGE, MnemonistPuppetRules.replayDamage(100.0F), "max replay");
    }

    private static void consumesMatchingReplayMemory() {
        UUID target = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        List<MnemonistPuppetRules.AttackMemory> memories = new ArrayList<>();
        memories.add(new MnemonistPuppetRules.AttackMemory(other, 4.0F, 20L));
        memories.add(new MnemonistPuppetRules.AttackMemory(target, 6.0F, 22L));
        MnemonistPuppetRules.AttackMemory memory = MnemonistPuppetRules.pollReplayMemory(memories, target, 40L);
        assertEquals(target, memory.targetId(), "matching memory target");
        assertEquals(1, memories.size(), "matching memory consumed");
        assertEquals(other, memories.get(0).targetId(), "non-matching memory remains");
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

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected " + expected + " but was " + actual);
        }
    }

    private static void assertEquals(float expected, float actual, String message) {
        if (Math.abs(expected - actual) > 0.0001F) {
            throw new AssertionError(message + " expected " + expected + " but was " + actual);
        }
    }
}
