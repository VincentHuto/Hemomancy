package com.vincenthuto.hemomancy.common.worldgen.pelagic;

/** Coordinate-only ocean geology, shared by density, surfaces and biome selection. */
public final class PelagicTerrainSampler {
    public enum Surface { VANILLA, ROCKPOOL, REEF, OPEN }
    public enum Form { SHORE, REEF, BANK, TWILIGHT, CANYON, FAN, PLAIN }
    public record Column(double floor, double influence, Form form, double incision, boolean pool) {
        public int floorY() { return (int) Math.floor(floor); }
    }

    private static final double[] CONTINENTALNESS = {-.74, -.64, -.54, -.46, -.36, -.28, -.20};
    private static final double[] DEPTH = {-53.5, -31, -8, 13, 33, 46, 58};
    private final long seed;

    public PelagicTerrainSampler(long seed) { this.seed = seed; }

    public Column sample(int x, int z, double continentalness, double erosion, double ridges, Surface surface) {
        if (surface == Surface.ROCKPOOL) {
            var ocean = sample(x, z, continentalness, erosion, ridges, Surface.OPEN);
            var coast = shore(x, z, continentalness);
            return blend(ocean, new Column(coast.floor(), 1, coast.form(), 0, coast.pool()), coast.influence());
        }
        double influence = smooth(-1.05, -1.0, continentalness) * (1 - smooth(-.24, -.19, continentalness));
        if (influence == 0) return new Column(58, 0, Form.PLAIN, 0, false);

        double base = bathymetry(continentalness + erosion * .004);
        double wx = x + noise(x / 170.0, z / 170.0, 11) * 24;
        double wz = z + noise(x / 170.0, z / 170.0, 12) * 24;
        double channel = 1 - smooth(.015, .16, Math.abs(noise(wx / 74, wz / 74, 31)));
        double tributary = 1 - smooth(.012, .07, Math.abs(noise(wx / 34, wz / 34, 32)));
        double incision = Math.max(channel, tributary * .64);
        double floor;
        Form form;

        if (surface == Surface.REEF) {
            // Flat-topped lobes leave 5x5 footholds for the existing coral clusters.
            double lobe = Math.floor((noise(wx / 24, wz / 24, 21) + 1) * 2.5);
            double reef = 41 + lobe - 4.5 * smooth(.25, .9, channel);
            double shelf = smooth(-.44, -.38, continentalness) * (1 - smooth(-.245, -.20, continentalness));
            floor = lerp(base, reef, shelf);
            form = Form.REEF;
        } else {
            double scallop = noise(wx / 42, wz / 42, 41);
            double twilight = 3 * scallop - 6 * smooth(.15, .75, -scallop) - 2 * channel;
            double midnight = 3.5 * noise(wx / 32, wz / 55, 42) - 16 * incision;
            // Long, shallow ripples settle into fans below the canyon mouths.
            double fan = 1.7 * noise(wx / 100, wz / 38, 51)
                    + .65 * Math.sin(wx / 8 + noise(wx / 90, wz / 90, 52) * 5);
            double plain = 1.6 + 2.2 * noise(x / 105.0, z / 105.0, 61)
                    + .8 * noise(x / 36.0, z / 48.0, 62);
            if (base < -43) plain += ventMounds(x, z) * smooth(-43, -51, base);
            double relief = lerp(plain, fan, smooth(-49, -30, base));
            relief = lerp(relief, midnight, smooth(-29, -6, base));
            relief = lerp(relief, twilight, smooth(8, 23, base));
            relief = lerp(relief, 2 * noise(wx / 58, wz / 58, 71), smooth(31, 45, base));
            floor = Math.max(-54.9, base + relief);
            form = base > 33 ? Form.BANK : base > 12 ? Form.TWILIGHT
                    : base > -16 ? Form.CANYON : base > -43 ? Form.FAN : Form.PLAIN;
        }

        // Isolated elongated banks occupy a small fraction of deep basins.
        if (base < -35) {
            double bank = seamount(wx, wz);
            floor += bank * smooth(-35, -49, base);
            if (bank > 3) form = Form.BANK;
        }
        return new Column(Math.max(-54.9, Math.min(58, floor)), influence, form, incision, false);
    }

    /** Weight floor heights by their actual shaping strength; an inactive profile must not pull the coast down. */
    public static Column blend(Column a, Column b, double weight) {
        if (weight <= 0) return a;
        if (weight >= 1) return b;
        double first = a.influence() * (1 - weight), second = b.influence() * weight;
        double influence = first + second;
        if (influence == 0) return a;
        double portion = second / influence;
        var dominant = portion >= .5 ? b : a;
        return new Column(lerp(a.floor(), b.floor(), portion), influence, dominant.form(),
                lerp(a.incision(), b.incision(), portion), dominant.pool());
    }

    private Column shore(int x, int z, double c) {
        double influence = smooth(-.22, -.197, c) * (1 - smooth(-.13, -.11, c));
        double bank = 60 + (c + .19) * 115;
        double joints = noise(x / 36.0, z / 58.0, 81);
        double platform = Math.floor((bank + joints * 1.3) / 2) * 2 + .3;
        double floor = lerp(bank, platform, smooth(60, 63, bank));
        boolean pool = false;
        if (bank >= 63 && bank <= 67) {
            int cellX = Math.floorDiv(x, 16), cellZ = Math.floorDiv(z, 16);
            for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
                long salt = hash(cx, cz, 82);
                double px = cx * 16 + 4 + unit(salt) * 8;
                double pz = cz * 16 + 4 + unit(mix(salt)) * 8;
                double rx = 2.1 + unit(mix(salt + 1)) * 2.1;
                double rz = 2.0 + unit(mix(salt + 2)) * 2.0;
                double dx = (x - px) / rx, dz = (z - pz) / rz;
                double pocket = Math.sqrt(dx * dx + dz * dz)
                        + noise(x / 3.5, z / 3.5, 83) * .16;
                if (pocket < 1) {
                    floor = 59.8 + smooth(.25, 1, pocket) * 1.7;
                    pool = true;
                } else if (pocket < 1.4 && !pool) {
                    floor = Math.max(63.2, floor);
                }
            }
        }
        return new Column(floor, influence, Form.SHORE, 0, pool);
    }

    private double seamount(double x, double z) {
        int cellX = (int)Math.floor(x / 256), cellZ = (int)Math.floor(z / 256);
        double result = 0;
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
            long salt = hash(cx, cz, 91);
            if (unit(salt) > .13) continue;
            double dx = x - (cx * 256 + 64 + unit(mix(salt)) * 128);
            double dz = z - (cz * 256 + 64 + unit(mix(salt + 1)) * 128);
            double angle = unit(mix(salt + 2)) * Math.PI * 2;
            double u = (dx * Math.cos(angle) + dz * Math.sin(angle)) / 44;
            double v = (-dx * Math.sin(angle) + dz * Math.cos(angle)) / 83;
            double radius = Math.sqrt(u * u + v * v);
            double bell = 1 - smooth(0, 1, radius);
            result = Math.max(result, bell * bell * (56 + unit(mix(salt + 3)) * 26));
        }
        return result;
    }

    record VentSite(int x, int z, long salt, double angle, boolean active) {}

    VentSite ventSite(int cellX, int cellZ) {
        long salt = hash(cellX, cellZ, 201);
        int x = cellX * 64 + 8 + (int)(unit(salt) * 48);
        int z = cellZ * 64 + 8 + (int)(unit(mix(salt)) * 48);
        return new VentSite(x, z, salt, noise(x / 380.0, z / 380.0, 202) * Math.PI,
                Math.abs(noise(x / 380.0, z / 380.0, 203)) < .23 && unit(mix(salt + 1)) < .55);
    }

    /** Broad deposits share chimney roots; neighboring cells contribute across chunk edges. */
    private double ventMounds(int x, int z) {
        int cellX = Math.floorDiv(x, 64), cellZ = Math.floorDiv(z, 64);
        double height = 0;
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
            var site = ventSite(cx, cz);
            if (!site.active()) continue;
            double dx = x - site.x(), dz = z - site.z();
            double u = (dx * Math.cos(site.angle()) + dz * Math.sin(site.angle())) / 29;
            double v = (-dx * Math.sin(site.angle()) + dz * Math.cos(site.angle())) / 20;
            double radius = Math.sqrt(u * u + v * v);
            radius *= 1 + noise(x / 13.0, z / 13.0, 205) * .16;
            height = Math.max(height, (1 - smooth(.05, 1, radius)) * (5 + unit(mix(site.salt() + 9)) * 3));
        }
        return height;
    }

    /** Wet alcoves stay above the sealed foundation and retain a stone roof. */
    public boolean hollow(Column column, int x, int y, int z) {
        if (column.influence() < .99 || column.floor() < -35 || y >= column.floor() - 1.5
                || y < column.floor() - 5.5) return false;
        if (column.form() != Form.CANYON && column.form() != Form.REEF && column.form() != Form.TWILIGHT) return false;
        return column.incision() > .15 && column.incision() < .65
                && noise(x / 27.0, z / 27.0, 101) > .38;
    }

    public double noise(double x, double z, long salt) {
        int ix = (int)Math.floor(x), iz = (int)Math.floor(z);
        double u = fade(x - ix), v = fade(z - iz);
        return lerp(lerp(unit(hash(ix, iz, salt)), unit(hash(ix + 1, iz, salt)), u),
                lerp(unit(hash(ix, iz + 1, salt)), unit(hash(ix + 1, iz + 1, salt)), u), v) * 2 - 1;
    }

    public long hash(int x, int z, long salt) {
        return mix(seed ^ (x * 0x632BE59BD9B4E019L) ^ (z * 0x9E3779B97F4A7C15L) ^ (salt * 0xD1B54A32D192ED03L));
    }

    public static double unit(long value) { return (value >>> 11) * 0x1.0p-53; }

    public static long mix(long value) {
        value = (value ^ (value >>> 30)) * 0xBF58476D1CE4E5B9L;
        value = (value ^ (value >>> 27)) * 0x94D049BB133111EBL;
        return value ^ (value >>> 31);
    }

    private static double bathymetry(double c) {
        if (c <= CONTINENTALNESS[0]) return DEPTH[0];
        for (int i = 1; i < CONTINENTALNESS.length; i++)
            if (c < CONTINENTALNESS[i])
                return lerp(DEPTH[i - 1], DEPTH[i], (c - CONTINENTALNESS[i - 1]) / (CONTINENTALNESS[i] - CONTINENTALNESS[i - 1]));
        return DEPTH[DEPTH.length - 1];
    }

    public static double smooth(double start, double end, double value) {
        double t = Math.max(0, Math.min(1, (value - start) / (end - start)));
        return t * t * (3 - 2 * t);
    }
    private static double fade(double t) { return t * t * t * (t * (t * 6 - 15) + 10); }
    private static double lerp(double a, double b, double t) { return a + t * (b - a); }
}
