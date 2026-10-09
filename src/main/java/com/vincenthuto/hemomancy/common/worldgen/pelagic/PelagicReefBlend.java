package com.vincenthuto.hemomancy.common.worldgen.pelagic;

/** Smooth reef coverage from immutable biome inputs, without reading neighboring chunks. */
public final class PelagicReefBlend {
    @FunctionalInterface
    public interface ReefMask { boolean isReef(int x, int z); }

    private boolean initialized;
    private int gridX, gridZ;
    private double a, b, c, d;
    private final PelagicColumnCache<Double> nodes = new PelagicColumnCache<>();

    /** Use one instance per fixed mask and worker; all 256 columns share four nodes. */
    public double cachedWeight(int x, int z, ReefMask mask) {
        int gx = Math.floorDiv(x, 16) * 16, gz = Math.floorDiv(z, 16) * 16;
        if (!initialized || gx != gridX || gz != gridZ) {
            // Landmark footprints cross chunk edges repeatedly; retain shared grid nodes on revisits.
            PelagicColumnCache.Sampler<Double> sampler = (nx, nz) -> node(nx * 16, nz * 16, mask);
            a = nodes.get(gx >> 4, gz >> 4, sampler);
            b = nodes.get((gx >> 4) + 1, gz >> 4, sampler);
            c = nodes.get(gx >> 4, (gz >> 4) + 1, sampler);
            d = nodes.get((gx >> 4) + 1, (gz >> 4) + 1, sampler);
            gridX = gx;
            gridZ = gz;
            initialized = true;
        }
        double u = PelagicTerrainSampler.smooth(0, 16, x - gx);
        double v = PelagicTerrainSampler.smooth(0, 16, z - gz);
        return interpolate(u, v, a, b, c, d);
    }

    public static double weight(int x, int z, ReefMask mask) {
        int gx = Math.floorDiv(x, 16) * 16, gz = Math.floorDiv(z, 16) * 16;
        double u = PelagicTerrainSampler.smooth(0, 16, x - gx);
        double v = PelagicTerrainSampler.smooth(0, 16, z - gz);
        double a = node(gx, gz, mask), b = node(gx + 16, gz, mask);
        double c = node(gx, gz + 16, mask), d = node(gx + 16, gz + 16, mask);
        return interpolate(u, v, a, b, c, d);
    }

    private static double interpolate(double u, double v, double a, double b, double c, double d) {
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
