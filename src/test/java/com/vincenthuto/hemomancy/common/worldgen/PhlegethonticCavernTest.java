package com.vincenthuto.hemomancy.common.worldgen;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;
import com.vincenthuto.hemomancy.common.worldgen.PhlegethonticTerrainPlan.*;

class PhlegethonticCavernTest {
    private static final int SURFACE = 48;

    @Test void basinClearanceFormsAVaultInsteadOfAFlatTwentyFourBlockRoof() {
        var layout = bowl(0, 0, 32, SURFACE);
        var center = plan(layout, 0, 0, p -> true);
        var shoulder = plan(layout, 1, 0, p -> true);
        int centerRoof = roof(center, 0, 0);
        int shoulderRoof = roof(shoulder, 28, 0);
        assertTrue(centerRoof >= SURFACE + 18, "Keep generous headroom over the basin");
        assertTrue(centerRoof - shoulderRoof >= 5,
                "The roof must descend toward the shore instead of clearing one flat slab: "
                        + centerRoof + " / " + shoulderRoof);
        assertEquals(Material.ICHOR, center.get(new Voxel(0, SURFACE, 0)));
    }

    @Test void overheadClearanceTapersBeforeAProtectedVerticalBoundary() {
        var result = plan(bowl(0, 0, 32, SURFACE), 0, 0, p -> p.x() < 12);
        assertTrue(roof(result, 0, 0) - roof(result, 10, 0) >= 6,
                "High clearance must recede before the biome/structure boundary, not leave a vertical cut");
        assertTrue(result.keySet().stream().allMatch(p -> p.x() < 12));
        assertEquals(Material.SCAB, result.get(new Voxel(10, SURFACE, 0)),
                "The closed bank must still contain the river");
    }

    @Test void overlappingVaultsJoinAcrossNegativeChunkBoundariesWithoutAHeightStep() {
        var layout = new PhlegethonticBasinLayout(17, -1, 0, List.of(
                new PhlegethonticBasinLayout.Basin(-20, 0, 32, 28, 48),
                new PhlegethonticBasinLayout.Basin(16, 0, 28, 32, 60)), List.of());
        var west = plan(layout, -1, 0, p -> true);
        var east = plan(layout, 0, 0, p -> true);
        Map<Voxel, Material> forward = new HashMap<>(west);
        forward.putAll(east);
        Map<Voxel, Material> reverse = new HashMap<>(plan(layout, 0, 0, p -> true));
        reverse.putAll(plan(layout, -1, 0, p -> true));
        assertEquals(forward, reverse);
        assertTrue(west.keySet().stream().allMatch(p -> PhlegethonticTerrainPlan.owns(p, -1, 0)));
        assertTrue(east.keySet().stream().allMatch(p -> PhlegethonticTerrainPlan.owns(p, 0, 0)));
        for (int x = -15; x < 15; x++)
            assertTrue(Math.abs(roof(forward, x, 0) - roof(forward, x + 1, 0)) <= 2,
                    "Overlapping basin heights must not leave a wall at x=" + x);
    }

    private static PhlegethonticBasinLayout bowl(double x, double z, double radius, int surface) {
        return new PhlegethonticBasinLayout(17, 0, 0,
                List.of(new PhlegethonticBasinLayout.Basin(x, z, radius, radius, surface)), List.of());
    }

    private static Map<Voxel, Material> plan(PhlegethonticBasinLayout layout, int x, int z,
                                             Predicate<Voxel> allowed) {
        return PhlegethonticTerrainPlan.basin(List.of(layout), 17, x, z, 0, 128, allowed, allowed);
    }

    private static int roof(Map<Voxel, Material> plan, int x, int z) {
        return plan.entrySet().stream().filter(e -> e.getKey().x() == x && e.getKey().z() == z
                && e.getValue() == Material.AIR).mapToInt(e -> e.getKey().y()).max().orElseThrow();
    }
}
