package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public final class LastDeathMemorySnapshotTest {
    public static void main(String[] args) {
        LastDeathMemory memory = new LastDeathMemory();
        long packed = LastDeathMemory.packPosition(12, 64, -9);
        LastDeathMemorySnapshot.present("minecraft:overworld", packed, 2400L).applyTo(memory);

        assertTrue(memory.hasMemory(), "present snapshot should mark memory present");
        assertEquals("minecraft:overworld", memory.dimensionId(), "dimension should copy exactly");
        assertEquals(packed, memory.packedPosition(), "packed position should copy exactly");
        assertEquals(2400L, memory.gameTime(), "game time should copy exactly");

        LastDeathMemorySnapshot.empty().applyTo(memory);

        assertTrue(!memory.hasMemory(), "empty snapshot should clear memory");
        assertEquals("", memory.dimensionId(), "empty snapshot should clear dimension");
        assertEquals(0L, memory.packedPosition(), "empty snapshot should clear packed position");
        assertEquals(0L, memory.gameTime(), "empty snapshot should clear game time");
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!expected.equals(actual)) {
            throw new AssertionError(message + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertEquals(long expected, long actual, String message) {
        if (expected != actual) {
            throw new AssertionError(message + " expected " + expected + " but got " + actual);
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
