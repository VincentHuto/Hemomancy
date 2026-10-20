package com.vincenthuto.hemomancy.client.render.outpouring;

import net.minecraft.world.phys.Vec3;

/** Block-face directions shared by the marks and the renderer (Direction ordinals). */
final class OutpouringGeometry {
    private OutpouringGeometry() {}

    /** Two unit axes lying in the plane of a face with this normal. */
    static Vec3[] faceAxes(Vec3 normal) {
        if (Math.abs(normal.y) > .9) return new Vec3[]{new Vec3(1, 0, 0), new Vec3(0, 0, 1)};
        if (Math.abs(normal.z) > .9) return new Vec3[]{new Vec3(1, 0, 0), new Vec3(0, 1, 0)};
        return new Vec3[]{new Vec3(0, 0, 1), new Vec3(0, 1, 0)};
    }

    static Vec3 faceNormal(int face) {
        return switch (face) {
            case 0 -> new Vec3(0, -1, 0);
            case 1 -> new Vec3(0, 1, 0);
            case 2 -> new Vec3(0, 0, -1);
            case 3 -> new Vec3(0, 0, 1);
            case 4 -> new Vec3(-1, 0, 0);
            default -> new Vec3(1, 0, 0);
        };
    }
}
