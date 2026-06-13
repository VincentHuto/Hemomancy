package com.vincenthuto.hemomancy.common.item.harbinger.memory;

public record LastDeathMemorySnapshot(boolean present, String dimensionId, long packedPosition, long gameTime) {
    public static LastDeathMemorySnapshot from(LastDeathMemory memory) {
        return memory.hasMemory()
                ? present(memory.dimensionId(), memory.packedPosition(), memory.gameTime())
                : empty();
    }

    public static LastDeathMemorySnapshot present(String dimensionId, long packedPosition, long gameTime) {
        return new LastDeathMemorySnapshot(true, dimensionId, packedPosition, gameTime);
    }

    public static LastDeathMemorySnapshot empty() {
        return new LastDeathMemorySnapshot(false, "", 0L, 0L);
    }

    public void applyTo(LastDeathMemory memory) {
        if (present) {
            memory.remember(dimensionId, packedPosition, gameTime);
        } else {
            memory.clear();
        }
    }
}
