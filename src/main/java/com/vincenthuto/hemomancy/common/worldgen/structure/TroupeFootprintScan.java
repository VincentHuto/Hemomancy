package com.vincenthuto.hemomancy.common.worldgen.structure;

/** Reject unsuitable terrain at widely separated points before checking every interior column. */
final class TroupeFootprintScan {
    @FunctionalInterface
    interface ColumnCheck { boolean test(int x, int z); }

    static boolean matches(int minX, int maxX, int minZ, int maxZ, ColumnCheck check) {
        return matches(minX, maxX, minZ, maxZ, check, check);
    }

    static boolean matches(int minX, int maxX, int minZ, int maxZ, ColumnCheck screen, ColumnCheck interior) {
        int middleX = minX + (maxX - minX) / 2;
        int middleZ = minZ + (maxZ - minZ) / 2;
        for (int x : samples(minX, maxX)) for (int z : samples(minZ, maxZ))
            if (!screen.test(x, z)) return false;
        for (int x = minX; x <= maxX; x++) for (int z = minZ; z <= maxZ; z++) {
            if ((x == minX || x == middleX || x == maxX) && (z == minZ || z == middleZ || z == maxZ)) continue;
            if (!interior.test(x, z)) return false;
        }
        return true;
    }

    private static int[] samples(int min, int max) {
        if (min == max) return new int[]{min};
        if (max - min == 1) return new int[]{min, max};
        return new int[]{min, max, min + (max - min) / 2};
    }
}
