package com.vincenthuto.hemomancy.common.worldgen;

public final class PhlegethonticRules {
    public static final int CELL_SIZE = 128;
    public static final int VEIN_REACH_CHUNKS = 5;
    private PhlegethonticRules() {}

    public static boolean pulsing(long time) { return Math.floorMod(time, 160) < 30; }
    public static float pulseStrength(long time,float partialTick) {
        double phase=Math.floorMod(time,160)+partialTick;
        return phase<30?(float)Math.sin(Math.PI*phase/30):0;
    }
    public static boolean pullToRiver(double bloodRatio) { return bloodRatio > .5; }
    public static double severCost(double maximumBlood) { return Math.min(100, Math.max(0, maximumBlood) * .01); }
    public static long seed(long worldSeed, int x, int z, long salt) {
        long n = worldSeed ^ salt ^ x * 0x4f9939f508L ^ z * 0x1ef1565bd5L;
        n = (n ^ n >>> 30) * 0xbf58476d1ce4e5b9L;
        n = (n ^ n >>> 27) * 0x94d049bb133111ebL;
        return n ^ n >>> 31;
    }

    public static double noise(long seed, double x, double z) {
        int ix = (int)Math.floor(x), iz = (int)Math.floor(z);
        double tx = x - ix, tz = z - iz;
        tx = tx * tx * (3 - 2 * tx);
        tz = tz * tz * (3 - 2 * tz);
        double a = unit(seed(seed, ix, iz, 0)), b = unit(seed(seed, ix + 1, iz, 0));
        double c = unit(seed(seed, ix, iz + 1, 0)), d = unit(seed(seed, ix + 1, iz + 1, 0));
        return (a + (b - a) * tx) * (1 - tz) + (c + (d - c) * tx) * tz;
    }

    private static double unit(long n) { return (n >>> 11) * 0x1.0p-53 * 2 - 1; }
}
