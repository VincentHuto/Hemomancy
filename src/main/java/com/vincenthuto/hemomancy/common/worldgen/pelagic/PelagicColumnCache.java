package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;

/** Thread-confined column samples, bounded to sixteen 16x16 tiles. */
final class PelagicColumnCache<T> {
    @FunctionalInterface
    interface Sampler<T> { T sample(int x, int z); }

    private final Long2ObjectLinkedOpenHashMap<Object[]> tiles = new Long2ObjectLinkedOpenHashMap<>(16);
    private int tileX, tileZ;
    private Object[] current;

    @SuppressWarnings("unchecked")
    T get(int x, int z, Sampler<T> sampler) {
        int tx = x >> 4, tz = z >> 4;
        if (current == null || tx != tileX || tz != tileZ) {
            long key = ((long) tx << 32) ^ (tz & 0xffffffffL);
            current = tiles.getAndMoveToFirst(key);
            if (current == null) {
                current = new Object[256];
                if (tiles.size() == 16) tiles.removeLast();
                tiles.putAndMoveToFirst(key, current);
            }
            tileX = tx;
            tileZ = tz;
        }
        // Generation revisits these columns at every Y; avoid hashing or updating LRU on each block.
        int index = (x & 15) * 16 + (z & 15);
        T value = (T) current[index];
        if (value == null) {
            value = sampler.sample(x, z);
            current[index] = value;
        }
        return value;
    }
}
