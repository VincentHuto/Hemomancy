package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import org.junit.jupiter.api.Test;
import java.util.*;
import java.util.function.BiFunction;
import static org.junit.jupiter.api.Assertions.*;

class PelagicLandmarksTest {
    @Test void ventFieldsHaveOneToFiveSeparatedChimneysInVariedLayouts() {
        for (long seed : new long[]{42, 1337, 8675309}) {
            var terrain = new PelagicTerrainSampler(seed);
            Set<Integer> counts = new HashSet<>();
            for (int cx = -8; cx < 8; cx++) for (int cz = -8; cz < 8; cz++) {
                var site = terrain.ventSite(cx, cz);
                if (!site.active()) continue;
                BiFunction<Integer, Integer, PelagicTerrainSampler.Column> isolated = (x, z) ->
                        new PelagicTerrainSampler.Column(-54,
                                Math.abs(x - site.x()) <= 12 && Math.abs(z - site.z()) <= 12 ? 1 : 0,
                                PelagicTerrainSampler.Form.PLAIN, 0, false);
                Set<PelagicLandmarks.Voxel> caps = new HashSet<>();
                for (int x = Math.floorDiv(site.x() - 12, 16); x <= Math.floorDiv(site.x() + 12, 16); x++)
                    for (int z = Math.floorDiv(site.z() - 12, 16); z <= Math.floorDiv(site.z() + 12, 16); z++)
                        PelagicLandmarks.plan(terrain, x, z, isolated).stream()
                                .filter(v -> v.material() == PelagicLandmarks.Material.MAGMA).forEach(caps::add);
                assertTrue(caps.size() >= 1 && caps.size() <= 5);
                counts.add(caps.size());
                var points = new ArrayList<>(caps);
                for (int a = 0; a < points.size(); a++) for (int b = a + 1; b < points.size(); b++) {
                    int dx = points.get(a).x() - points.get(b).x(), dz = points.get(a).z() - points.get(b).z();
                    assertTrue(dx * dx + dz * dz >= 16, "Chimneys must retain separate bodies");
                }
                if (points.size() >= 3) {
                    var a = points.get(0); var b = points.get(1);
                    assertTrue(points.stream().skip(2).anyMatch(c ->
                            (b.x() - a.x()) * (c.z() - a.z()) != (b.z() - a.z()) * (c.x() - a.x())),
                            "Larger fields must spread across the mound rather than form a line");
                }
            }
            assertEquals(Set.of(1, 2, 3, 4, 5), counts, "Every field size must occur for seed " + seed);
        }
    }
    private static BiFunction<Integer, Integer, PelagicTerrainSampler.Column> floor(double y, PelagicTerrainSampler.Form form) {
        return (x, z) -> new PelagicTerrainSampler.Column(y, 1, form, 0, false);
    }
    @Test void ventFieldsAreSparseTallAndOwnedIncludingNegativeCoordinates() {
        var terrain = new PelagicTerrainSampler(42);
        var voxels = new HashSet<PelagicLandmarks.Voxel>();
        for (int cx = -16; cx < 16; cx++) for (int cz = -16; cz < 16; cz++) {
            var plan = PelagicLandmarks.plan(terrain, cx, cz, floor(-54, PelagicTerrainSampler.Form.PLAIN));
            for (var v : plan) {
                assertEquals(cx, Math.floorDiv(v.x(), 16)); assertEquals(cz, Math.floorDiv(v.z(), 16));
                assertTrue(v.y() >= -54 && v.y() <= -46);
            }
            voxels.addAll(plan);
        }
        assertTrue(voxels.stream().anyMatch(v -> v.material() == PelagicLandmarks.Material.MAGMA && v.y() > -52));
        assertTrue(voxels.size() < 512 * 512 * .08, "Vents must leave most abyssal plain unobstructed");
        var reverse = new HashSet<PelagicLandmarks.Voxel>();
        for (int cx = 15; cx >= -16; cx--) for (int cz = 15; cz >= -16; cz--)
            reverse.addAll(PelagicLandmarks.plan(terrain, cx, cz, floor(-54, PelagicTerrainSampler.Form.PLAIN)));
        assertEquals(voxels, reverse);
    }
    @Test void fossilsStayInCollectionBasinsAndLandRemainsUntouched() {
        var terrain = new PelagicTerrainSampler(1337);
        int bones = 0;
        for (int cx = -10; cx <= 10; cx++) for (int cz = -10; cz <= 10; cz++) {
            var plan = PelagicLandmarks.plan(terrain, cx, cz, floor(-22, PelagicTerrainSampler.Form.FAN));
            bones += (int)plan.stream().filter(v -> v.material() == PelagicLandmarks.Material.BONE).count();
            assertTrue(PelagicLandmarks.plan(terrain, cx, cz, (x,z) ->
                    new PelagicTerrainSampler.Column(63, 0, PelagicTerrainSampler.Form.SHORE, 0, false)).isEmpty());
        }
        assertTrue(bones > 20, "Collection basins should contain sparse recognizable rib remains");
    }
}
