package com.vincenthuto.hemomancy.common.worldgen.structure;

import org.junit.jupiter.api.Test;
import java.util.HashSet;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TroupeFootprintScanTest {
    @Test void rejectsAnUnsuitableFarCornerBeforeScanningTheInterior() {
        int[] probes = {0};
        assertFalse(TroupeFootprintScan.matches(-32, 32, -32, 32, (x, z) -> {
            probes[0]++;
            return x != 32 || z != 32;
        }));
        assertTrue(probes[0] <= 5, "Reject a steep or wet opposite corner before thousands of terrain queries");
    }

    @Test void validSitesStillCheckEveryColumnExactlyOnceIncludingNarrowBounds() {
        for (int width : new int[]{1, 2, 3, 25, 65}) for (int depth : new int[]{1, 2, 3, 25, 65}) {
            Set<Long> visited = new HashSet<>();
            assertTrue(TroupeFootprintScan.matches(-33, -34 + width, 7, 6 + depth, (x, z) -> {
                assertTrue(visited.add(((long)x << 32) ^ (z & 0xffffffffL)), "Repeated terrain column");
                assertTrue(x >= -33 && x < -33 + width && z >= 7 && z < 7 + depth);
                return true;
            }));
            assertEquals(width * depth, visited.size());
        }
    }

    @Test void interiorObstructionsCannotPassTheEarlyScreen() {
        assertFalse(TroupeFootprintScan.matches(-32, 32, -32, 32, (x, z) -> x != 7 || z != 11));
    }
}
