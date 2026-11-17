package com.vincenthuto.hemomancy.common.worldgen.structure.circus;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TroupeFootprintScanTest {
    @Test void rejectsAnUnsuitableFarCornerBeforeTheGrid() {
        int[] probes = {0};
        assertFalse(TroupeFootprintScan.matches(-32, 32, -32, 32, 8, (x, z) -> {
            probes[0]++;
            return x != 32 || z != 32;
        }));
        assertTrue(probes[0] <= 4, "a steep or wet corner is refused before the grid is sampled");
    }

    @Test void samplesCornersEdgesAndACoarseGridOnce() {
        for (int width : new int[]{1, 2, 3, 17, 27, 65}) for (int depth : new int[]{1, 2, 3, 15, 27, 65}) {
            Set<Long> visited = new HashSet<>();
            assertTrue(TroupeFootprintScan.matches(-33, -34 + width, 7, 6 + depth, 8, (x, z) -> {
                assertTrue(visited.add(((long) x << 32) ^ (z & 0xffffffffL)), "a column was asked twice");
                assertTrue(x >= -33 && x < -33 + width && z >= 7 && z < 7 + depth);
                return true;
            }));
            int minX = -33, maxX = -34 + width, minZ = 7, maxZ = 6 + depth;
            for (int x : new int[]{minX, maxX, minX + (maxX - minX) / 2})
                for (int z : new int[]{minZ, maxZ, minZ + (maxZ - minZ) / 2})
                    assertTrue(visited.contains(((long) x << 32) ^ (z & 0xffffffffL)), "corner or edge midpoint skipped");
            int gridX = (width - 1) / 8 + 2, gridZ = (depth - 1) / 8 + 2;
            assertTrue(visited.size() <= gridX * gridZ + 9, "the scan stays coarse: " + visited.size());
        }
    }

    @Test void theGridIsNoCoarserThanItsStep() {
        var columns = TroupeFootprintScan.columns(0, 64, 0, 64, 8);
        for (int x = 0; x <= 64; x += 8) for (int z = 0; z <= 64; z += 8)
            assertTrue(columns.contains(((long) x << 32) | z), "grid column " + x + "," + z + " skipped");
        assertEquals(81, columns.size(), "a 65-block pavilion asks 81 columns, not 4,225");
    }
}
