package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PelagicReefBlendTest {
    @Test void neighboringChunkRevisitsReuseBlendNodes() {
        int[] probes = {0};
        var blend = new PelagicReefBlend();
        PelagicReefBlend.ReefMask mask = (x, z) -> { probes[0]++; return x >= 0; };
        blend.cachedWeight(-1, 0, mask);
        blend.cachedWeight(0, 0, mask);
        int warmed = probes[0];
        for (int i = 0; i < 256; i++) {
            assertEquals(.25 + .5 * PelagicTerrainSampler.smooth(0, 16, 15), blend.cachedWeight(-1, 0, mask), 0);
            assertEquals(.75, blend.cachedWeight(0, 0, mask), 0);
        }
        assertEquals(warmed, probes[0], "Landmark scans revisit neighboring chunks without resampling climate");
    }

    @Test void samplingAChunkReusesItsBiomeNeighborhood() {
        int[] probes = {0};
        var blend = new PelagicReefBlend();
        PelagicReefBlend.ReefMask mask = (x, z) -> {
            probes[0]++;
            return x >= 0;
        };
        for (int x = -16; x < 0; x++) for (int z = 16; z < 32; z++) {
            double u = PelagicTerrainSampler.smooth(0, 16, x + 16);
            assertEquals(.25 + .5 * u, blend.cachedWeight(x, z, mask), 1e-12);
        }
        assertTrue(probes[0] <= 36, "A chunk shares four blend nodes; biome probes: " + probes[0]);
    }

    @Test void reusedBlendKeepsExactWeightsAcrossChunkBoundariesAndRevisits() {
        PelagicReefBlend.ReefMask mask = (x, z) -> x >= 0 && z >= -16;
        var blend = new PelagicReefBlend();
        for (int[] origin : new int[][]{{-32, -32}, {-16, -16}, {0, 0}, {16, 16}, {-32, -32}})
            for (int x = origin[0]; x < origin[0] + 16; x++)
                for (int z = origin[1]; z < origin[1] + 16; z++)
                    assertEquals(PelagicReefBlend.weight(x, z, mask), blend.cachedWeight(x, z, mask), 0);
    }

    @Test void coastCoverageFadesShapingStrengthAsWellAsFloorHeight() {
        var inactive = new PelagicTerrainSampler.Column(58, 0, PelagicTerrainSampler.Form.PLAIN, 0, false);
        var coast = new PelagicTerrainSampler.Column(65, 1, PelagicTerrainSampler.Form.SHORE, 0, false);
        double previous = PelagicDensity.shape(.2, inactive, 64, false, false);
        for (int x = -64; x <= 64; x++) {
            double weight = PelagicReefBlend.weight(x, -17, (px, pz) -> px >= 0);
            var blended = PelagicTerrainSampler.blend(inactive, coast, weight);
            double density = PelagicDensity.shape(.2, blended, 64, false, false);
            assertTrue(Math.abs(density - previous) < .01);
            if (weight > 0) assertEquals(65, blended.floor(), 1e-12);
            previous = density;
        }
        assertEquals(inactive, PelagicTerrainSampler.blend(inactive, coast, 0));
        assertEquals(coast, PelagicTerrainSampler.blend(inactive, coast, 1));
    }

    @Test void interiorsKeepTheirOriginalTerrain() {
        assertEquals(1, PelagicReefBlend.weight(-17, 31, (x, z) -> true));
        assertEquals(0, PelagicReefBlend.weight(-17, 31, (x, z) -> false));
    }

    @Test void straightAndCornerBoundariesFadeAcrossPositiveAndNegativeChunkEdges() {
        for (int boundary : new int[]{-32, -1, 0, 16, 35}) {
            for (int z = -48; z <= 48; z++) {
                double previous = 0;
                for (int x = boundary - 64; x <= boundary + 64; x++) {
                    double weight = PelagicReefBlend.weight(x, z, (px, pz) -> px >= boundary);
                    assertTrue(weight >= previous && weight <= 1);
                    assertTrue(weight - previous < .05, "No biome or chunk sized step");
                    previous = weight;
                    double corner = PelagicReefBlend.weight(x, z, (px, pz) -> px >= boundary && pz >= boundary);
                    double next = PelagicReefBlend.weight(x + 1, z, (px, pz) -> px >= boundary && pz >= boundary);
                    assertTrue(Math.abs(corner - next) < .05);
                }
                assertEquals(1, previous);
            }
        }
    }
}
