package com.vincenthuto.hemomancy.common.item.harbinger.memory;

import java.util.List;

public final class MemoryThreadRules {
    public static final int MAX_POINTS = 96;
    public static final int RECORD_DISTANCE_BLOCKS = 3;
    public static final int BLOOD_COST = 8;
    public static final int USE_COOLDOWN_TICKS = 20;

    private MemoryThreadRules() {
    }

    public static boolean shouldRecord(long packedPosition, long lastPackedPosition) {
        int dx = LastDeathMemory.unpackX(packedPosition) - LastDeathMemory.unpackX(lastPackedPosition);
        int dy = LastDeathMemory.unpackY(packedPosition) - LastDeathMemory.unpackY(lastPackedPosition);
        int dz = LastDeathMemory.unpackZ(packedPosition) - LastDeathMemory.unpackZ(lastPackedPosition);
        return dx * dx + dy * dy + dz * dz >= RECORD_DISTANCE_BLOCKS * RECORD_DISTANCE_BLOCKS;
    }

    public static void appendRoutePoint(List<Long> points, long packedPosition) {
        if (points.isEmpty() || shouldRecord(packedPosition, points.get(points.size() - 1))) {
            points.add(packedPosition);
            while (points.size() > MAX_POINTS) {
                points.remove(0);
            }
        }
    }

    public static long packPosition(int x, int y, int z) {
        return LastDeathMemory.packPosition(x, y, z);
    }
}
