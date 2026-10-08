package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import java.util.*;
import java.util.function.BiFunction;

/** Seeded geology plans clipped to the owning chunk, including candidates rooted across its edge. */
public final class PelagicLandmarks {
    public enum Material { BASALT, MAGMA, TUFF, BONE, STONE }
    public record Voxel(int x, int y, int z, Material material) {}
    public static List<Voxel> plan(PelagicTerrainSampler terrain, int chunkX, int chunkZ,
            BiFunction<Integer, Integer, PelagicTerrainSampler.Column> floor) {
        var result = new ArrayList<Voxel>();
        int cellX = Math.floorDiv(chunkX, 4), cellZ = Math.floorDiv(chunkZ, 4);
        for (int cx = cellX - 1; cx <= cellX + 1; cx++) for (int cz = cellZ - 1; cz <= cellZ + 1; cz++) {
            var site = terrain.ventSite(cx, cz);
            long salt = site.salt();
            int x = site.x(), z = site.z();
            if (x < chunkX * 16 - 12 || x > chunkX * 16 + 27 || z < chunkZ * 16 - 12 || z > chunkZ * 16 + 27) continue;
            var center = floor.apply(x, z);
            if (center.influence() < .999) continue;
            double chance = PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(salt + 1));
            double angle = site.angle();
            var writer = new Writer(result, chunkX, chunkZ, floor);
            if (center.floor() < -34 && center.form() == PelagicTerrainSampler.Form.PLAIN
                    && site.active()) {
                vent(terrain, writer, x, z, angle, salt);
            } else if (center.form() == PelagicTerrainSampler.Form.FAN && center.floor() > -30 && center.floor() < -9 && chance < .20) {
                fossil(writer, x, z, angle);
            } else if (center.form() == PelagicTerrainSampler.Form.SHORE && !center.pool() && center.floor() > 63 && chance < .24) {
                int height = 4 + (int)(chance * 18);
                for (int dx = -2; dx <= 2; dx++) for (int dz = -3; dz <= 3; dz++)
                    for (int y = 1; y <= height; y++) if (dx * dx / 3.0 + dz * dz / 5.0 < 1.5 - y / (double)height)
                        writer.put(x + dx, center.floorY() + y, z + dz, Material.STONE);
            }
        }
        return result;
    }

    private static void vent(PelagicTerrainSampler terrain, Writer writer, int x, int z, double angle, long salt) {
        for (int dx = -9; dx <= 9; dx++) for (int dz = -9; dz <= 9; dz++) {
            double u = dx * Math.cos(angle) + dz * Math.sin(angle);
            double v = -dx * Math.sin(angle) + dz * Math.cos(angle);
            double radius = u * u / 81 + v * v / 18;
            if (radius > 1 + terrain.noise((x + dx) / 3.0, (z + dz) / 3.0, 204) * .2) continue;
            int ground = writer.floor.apply(x + dx, z + dz).floorY();
            writer.put(x + dx, ground, z + dz, radius < .45 ? Material.BASALT : Material.TUFF);
        }
        int count = 1 + (int)(PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(salt + 210)) * 5);
        double rotation = PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(salt + 211)) * Math.PI * 2;
        for (int i = 0; i < count; i++) {
            long chimney = PelagicTerrainSampler.mix(salt + 220 + i);
            // Jittered sectors leave room between bodies without imposing a row or fixed compass direction.
            double direction = rotation + (i + (PelagicTerrainSampler.unit(chimney) - .5) * .24) * Math.PI * 2 / count;
            double distance = count == 1 ? 2 * PelagicTerrainSampler.unit(chimney)
                    : 6 + 2 * PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(chimney));
            int vx = x + (int)Math.round(Math.cos(direction) * distance);
            int vz = z + (int)Math.round(Math.sin(direction) * distance);
            int ground = writer.floor.apply(vx, vz).floorY();
            int height = 3 + (int)(PelagicTerrainSampler.unit(PelagicTerrainSampler.mix(chimney + 1)) * 6);
            for (int y = 1; y <= height; y++) for (int dx = -2; dx <= 2; dx++) for (int dz = -2; dz <= 2; dz++) {
                double radius = y <= 2 ? 1.8 : y == height ? .8 : 1.15;
                if (dx * dx + dz * dz <= radius * radius)
                    writer.put(vx + dx, ground + y, vz + dz, y == height ? Material.MAGMA : Material.BASALT);
            }
        }
    }

    private static void fossil(Writer writer, int x, int z, double angle) {
        int ground = writer.floor.apply(x, z).floorY();
        for (int spine = -5; spine <= 5; spine++) {
            writer.rotated(x, z, spine, 0, ground + 1, angle, Material.BONE);
            if (spine % 2 == 0 && Math.abs(spine) < 5) for (int side : new int[]{-1, 1}) {
                // Paired broken arches leave water between the ribs; their ends disappear into silt.
                for (int rib = 1; rib <= 3; rib++) {
                    int low = rib == 1 ? 1 : rib == 2 ? 2 : 0;
                    int high = rib == 1 ? 2 : 3;
                    for (int y = low; y <= high; y++)
                        writer.rotated(x, z, spine, side * rib, ground + y, angle, Material.BONE);
                }
            }
        }
        for (int u = 5; u <= 6; u++) for (int v = -1; v <= 1; v++)
            writer.rotated(x, z, u, v, ground + 1, angle, Material.BONE);
    }

    private record Writer(List<Voxel> result, int chunkX, int chunkZ,
                          BiFunction<Integer, Integer, PelagicTerrainSampler.Column> floor) {
        void put(int x, int y, int z, Material material) {
            if (Math.floorDiv(x, 16) != chunkX || Math.floorDiv(z, 16) != chunkZ) return;
            var column = floor.apply(x, z);
            if (column.influence() >= .999 && !column.pool() && y >= column.floorY() - 1 && y > -59)
                result.add(new Voxel(x, y, z, material));
        }
        void rotated(int x, int z, int u, int v, int y, double angle, Material material) {
            put(x + (int)Math.round(u * Math.cos(angle) - v * Math.sin(angle)), y,
                    z + (int)Math.round(u * Math.sin(angle) + v * Math.cos(angle)), material);
        }
    }
}
