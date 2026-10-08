package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicTerrainSampler.Surface.*;

class PelagicTerrainSamplerTest {
    @Test void shoreProfileDoesNotSwitchDensityAtItsOceanClimateLimit() {
        var sampler = new PelagicTerrainSampler(42);
        for (int x = -32; x <= 32; x++) {
            var before = sampler.sample(x, 17, -.220001, 0, 0, ROCKPOOL);
            var after = sampler.sample(x, 17, -.219999, 0, 0, ROCKPOOL);
            for (int y = 40; y <= 70; y++)
                assertEquals(PelagicDensity.shape(.2, before, y, false, false),
                        PelagicDensity.shape(.2, after, y, false, false), .001,
                        "Entering the shore climate must not suddenly disable ocean shaping");
        }
    }

    @Test void deepBasinsReachBedrockWithoutBecomingAnotherRoughMountainRange() {
        for (long seed : new long[]{42, 1337, 8675309}) {
            var sampler = new PelagicTerrainSampler(seed);
            int plain = 0, count = 0;
            Set<Integer> plainHeights = new HashSet<>();
            for (int x = -512; x < 512; x += 4) for (int z = -512; z < 512; z += 4) {
                var column = sampler.sample(x, z, -.85, 0, 0, OPEN);
                assertTrue(column.floorY() >= -55, "Keep a solid foundation above bedrock");
                if (column.floorY() <= -49) {
                    plain++;
                    plainHeights.add(column.floorY());
                    var next = sampler.sample(x + 1, z, -.85, 0, 0, OPEN);
                    if (column.form() == PelagicTerrainSampler.Form.PLAIN && next.form() == PelagicTerrainSampler.Form.PLAIN)
                        assertTrue(Math.abs(next.floorY() - column.floorY()) <= 1, "Plain must remain smooth: " + seed + " " + x + "," + z + " " + column + " -> " + next);
                }
                count++;
            }
            assertTrue(plain >= count * .8, "At least 80% must remain low plain; seed=" + seed + " plain=" + plain);
            assertTrue(plainHeights.size() >= 5, "Low plains need gentle relief across several elevations");
        }
    }

    @Test void ventFieldsRiseAboveTheirSurroundingPlain() {
        for (long seed : new long[]{42, 1337, 8675309}) {
            var sampler = new PelagicTerrainSampler(seed);
            int fields = 0;
            for (int cx = -8; cx < 8; cx++) for (int cz = -8; cz < 8; cz++) {
                long salt = sampler.hash(cx, cz, 201);
                int x = cx * 64 + 8 + (int)(PelagicTerrainSampler.unit(salt) * 48);
                int z = cz * 64 + 8 + (int)(PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(salt)) * 48);
                if (Math.abs(sampler.noise(x / 380.0, z / 380.0, 203)) >= .23
                        || PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(salt + 1)) >= .55) continue;
                var center = sampler.sample(x, z, -.85, 0, 0, OPEN);
                if (center.form() != PelagicTerrainSampler.Form.PLAIN) continue;
                double surroundings = 0;
                boolean besideBank = false;
                for (int[] offset : new int[][]{{32, 0}, {-32, 0}, {0, 32}, {0, -32}}) {
                    var edge = sampler.sample(x + offset[0], z + offset[1], -.85, 0, 0, OPEN);
                    surroundings += edge.floor() / 4;
                    besideBank |= edge.form() == PelagicTerrainSampler.Form.BANK;
                }
                if (besideBank) continue;
                assertTrue(center.floor() >= surroundings + 2, "Chimneys need a raised geological foundation: " + seed + " " + x + "," + z + " " + center.floor() + " vs " + surroundings);
                fields++;
            }
            assertTrue(fields > 10, "Exercise multiple vent fields per seed");
        }
    }

    @Test void mainlandMushroomCoresAndUnselectedBeachesKeepTheirOriginalDensity() {
        var sampler = new PelagicTerrainSampler(42);
        for (double c : new double[]{-1.2, -1.05, -.18, -.12, 0, .3, 1})
            assertEquals(0, sampler.sample(81, -225, c, .2, -.1, VANILLA).influence(), 0);
        assertEquals(1, sampler.sample(81, -225, -.5, .2, -.1, VANILLA).influence(), 0);
    }

    @Test void rockpoolPlatformsContainBothDryRimsAndSubmergedPockets() {
        var sampler = new PelagicTerrainSampler(42);
        int pools = 0, dry = 0;
        for (int x = -128; x < 128; x++) for (int z = -128; z < 128; z++) {
            var c = sampler.sample(x, z, -.155, 0, 0, ROCKPOOL);
            if (c.pool()) {
                pools++;
                assertTrue(c.floorY() >= 59 && c.floorY() <= 61, "Pool bottom must hold shallow sea-level water");
            }
            if (c.floorY() >= 63) dry++;
        }
        assertTrue(pools > 1000 && pools < 20000, "Scattered pocket pools, not a flooded platform: " + pools);
        assertTrue(dry > 30000, "Most of the platform must remain exposed");
    }

    @Test void reefShelfProvidesShallowStableGrowthSurfaces() {
        var sampler = new PelagicTerrainSampler(1337);
        int stable = 0;
        DoubleSummaryStatistics heights = new DoubleSummaryStatistics();
        for (int x = -128; x < 128; x += 4) for (int z = -128; z < 128; z += 4) {
            var c = sampler.sample(x, z, -.31, 0, 0, REEF);
            heights.accept(c.floor());
            assertTrue(c.floorY() >= 35 && c.floorY() <= 51, "Reef must occupy the coastal shelf");
            int level = 0;
            for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++)
                if (Math.abs(sampler.sample(x + dx, z + dz, -.31, 0, 0, REEF).floorY() - c.floorY()) <= 1) level++;
            if (level >= 16) stable++;
        }
        assertTrue(stable >= 2500, "Existing coral needs broad stable pockets; got " + stable);
        assertTrue(heights.getMax() - heights.getMin() >= 4, "Reef needs lobes and intervening channels");
    }

    @Test void midnightIncisionsHaveMoreReliefThanDepositionalFans() {
        var sampler = new PelagicTerrainSampler(8675309);
        DoubleSummaryStatistics canyon = new DoubleSummaryStatistics(), fan = new DoubleSummaryStatistics();
        for (int x = -256; x < 256; x += 2) for (int z = -256; z < 256; z += 2) {
            canyon.accept(sampler.sample(x, z, -.49, 0, 0, OPEN).floor());
            fan.accept(sampler.sample(x, z, -.63, 0, 0, OPEN).floor());
        }
        assertTrue(canyon.getMax() - canyon.getMin() >= 12, "Canyons need visible relief");
        assertTrue(fan.getMax() - fan.getMin() < canyon.getMax() - canyon.getMin(), "Fans should settle below cliffs");
    }

    @Test void coordinateSamplingIsIndependentOfTraversalAndWorksAcrossNegativeChunkEdges() {
        var sampler = new PelagicTerrainSampler(42);
        Map<Long, PelagicTerrainSampler.Column> forward = new HashMap<>();
        for (int x = -33; x <= 33; x++) for (int z = -33; z <= 33; z++)
            forward.put(((long)x << 32) ^ (z & 0xffffffffL), sampler.sample(x, z, -.43, .2, .1, OPEN));
        for (int x = 33; x >= -33; x--) for (int z = 33; z >= -33; z--)
            assertEquals(forward.get(((long)x << 32) ^ (z & 0xffffffffL)), sampler.sample(x, z, -.43, .2, .1, OPEN));
        assertNotEquals(new PelagicTerrainSampler(1337).sample(19, -28, -.43, .2, .1, OPEN),
                sampler.sample(19, -28, -.43, .2, .1, OPEN), "World seed must affect the geology");
    }
}
