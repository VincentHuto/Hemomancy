package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;

/** Projects the view ray onto the cabinet's displayed specimen plane. */
public final class CabinetInspection {
    private CabinetInspection() {}

    public static int cellAt(Direction facing, Vec3 eye, Vec3 hit) {
        var start = local(facing, eye);
        var end = local(facing, hit);
        double dz = end.z - start.z;
        if (dz <= 0) return -1;
        double t = (5.0 / 16 - start.z) / dz;
        if (t < 0) return -1;
        double x = (start.x + (end.x - start.x) * t) * 16;
        double y = (start.y + (end.y - start.y) * t) * 16;
        if (x < 2 || x >= 14 || y <= 2 || y >= 14) return -1;
        if (divider(x) || divider(y)) return -1;
        return (int)((14 - y) / 4) * 3 + (int)((x - 2) / 4);
    }

    private static boolean divider(double value) {
        return value >= 5.8 && value <= 6.2 || value >= 9.8 && value <= 10.2;
    }

    private static Vec3 local(Direction facing, Vec3 p) {
        return switch (facing) {
            case NORTH -> p;
            case SOUTH -> new Vec3(1 - p.x, p.y, 1 - p.z);
            case EAST -> new Vec3(p.z, p.y, 1 - p.x);
            case WEST -> new Vec3(1 - p.z, p.y, p.x);
            default -> p;
        };
    }
}
