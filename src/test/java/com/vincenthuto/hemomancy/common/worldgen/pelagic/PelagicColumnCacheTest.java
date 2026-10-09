package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PelagicColumnCacheTest {
    @Test void aFullHeightChunkSamplesEachColumnOnlyOnce() {
        int[] calls = {0};
        var cache = new PelagicColumnCache<Long>();
        PelagicColumnCache.Sampler<Long> sampler = (x, z) -> {
            calls[0]++;
            return x * 100_000L + z;
        };
        for (int y = -64; y < 320; y++)
            for (int x = -16; x < 0; x++) for (int z = 16; z < 32; z++)
                assertEquals(x * 100_000L + z, cache.get(x, z, sampler));
        assertEquals(256, calls[0]);
    }

    @Test void negativeCoordinatesAndDiagonalChunksNeverAlias() {
        var cache = new PelagicColumnCache<String>();
        PelagicColumnCache.Sampler<String> sampler = (x, z) -> x + "," + z;
        int[] coordinates = {-30_000_000, -257, -256, -17, -16, -1, 0, 1, 15, 16, 255, 256, 30_000_000};
        for (int pass = 0; pass < 2; pass++)
            for (int x : coordinates) for (int z : coordinates)
                assertEquals(x + "," + z, cache.get(x, z, sampler));
    }

    @Test void boundedCacheRetainsActiveChunksAndRecomputesEvictedColumns() {
        int[] hotCalls = {0}, coldCalls = {0};
        var cache = new PelagicColumnCache<String>();
        PelagicColumnCache.Sampler<String> sampler = (x, z) -> {
            if (x == -1 && z == -1) hotCalls[0]++;
            if (x == 0 && z == 0) coldCalls[0]++;
            return x + "," + z;
        };
        assertEquals("0,0", cache.get(0, 0, sampler));
        for (int i = 1; i <= 256; i++) {
            assertEquals("-1,-1", cache.get(-1, -1, sampler));
            assertEquals(i * 16 + "," + i * 16, cache.get(i * 16, i * 16, sampler));
        }
        assertEquals(1, hotCalls[0], "Interleaved generation must retain the active chunk");
        assertEquals("0,0", cache.get(0, 0, sampler));
        assertEquals(2, coldCalls[0], "Old chunks must be evicted rather than accumulating per worker");
    }
}
