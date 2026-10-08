package com.vincenthuto.hemomancy.common.worldgen.pelagic;

/** Smooth reef coverage from immutable biome inputs, without reading neighboring chunks. */
public final class PelagicReefBlend {
    @FunctionalInterface
    public interface ReefMask { boolean isReef(int x, int z); }

    private PelagicReefBlend() {}

    public static double weight(int x, int z, ReefMask mask) {
        int gx = Math.floorDiv(x, 16) * 16, gz = Math.floorDiv(z, 16) * 16;
        double u = PelagicTerrainSampler.smooth(0, 16, x - gx);
        double v = PelagicTerrainSampler.smooth(0, 16, z - gz);
        double a = node(gx, gz, mask), b = node(gx + 16, gz, mask);
        double c = node(gx, gz + 16, mask), d = node(gx + 16, gz + 16, mask);
        return (a + (b - a) * u) * (1 - v) + (c + (d - c) * u) * v;
    }

    private static double node(int x, int z, ReefMask mask) {
        int sum = 0;
        for (int dx = -1; dx <= 1; dx++) for (int dz = -1; dz <= 1; dz++)
            if (mask.isReef(x + dx * 16, z + dz * 16))
                sum += (dx == 0 ? 2 : 1) * (dz == 0 ? 2 : 1);
        return sum / 16.0;
    }
}
