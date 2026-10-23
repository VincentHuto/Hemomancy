package com.vincenthuto.hemomancy.client.render.world;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * The encoded-UV vertex the school and rite material shaders read: U is the shape's tile ({@code shape * 2}) plus a
 * coordinate along the quad, V is the seed's tile ({@code tile * 2}) plus a coordinate across it, and the colour is
 * RGB with alpha × 255. Lux and Umbra, Thermal, Ferric and Ductilis, and the rite surfaces all write it here.
 */
public final class MaterialQuads {
    /** The seed tiles the shaders tell apart; a seed wraps into one of them. */
    public static final int SEED_TILES = 127;

    private MaterialQuads() {
    }

    public static int seedTile(int seed) {
        return Math.floorMod(seed, SEED_TILES);
    }

    public static void vertex(VertexConsumer v, Matrix4f pose, double x, double y, double z, int color, float alpha,
            float u, float texV) {
        v.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor((color >> 16) & 255, (color >> 8) & 255, color & 255, (int) (Mth.clamp(alpha, 0, 1) * 255))
                .setUv(u, texV);
    }

    /**
     * Corners a, b, c, d: {@code u0} to {@code u1} along the shape's tile, 0 to 1 across the seed's tile. With
     * {@code alternateDiagonal} the corners start at b, so the quad splits along its other diagonal.
     */
    public static void quad(VertexConsumer v, Matrix4f pose, double ax, double ay, double az, double bx, double by,
            double bz, double cx, double cy, double cz, double dx, double dy, double dz, int shape, int tile,
            int color, float alpha, double u0, double u1, boolean alternateDiagonal) {
        float start = (float) (shape * 2 + u0), end = (float) (shape * 2 + u1);
        float low = tile * 2, high = tile * 2 + 1;
        if (!alternateDiagonal) vertex(v, pose, ax, ay, az, color, alpha, start, low);
        vertex(v, pose, bx, by, bz, color, alpha, end, low);
        vertex(v, pose, cx, cy, cz, color, alpha, end, high);
        vertex(v, pose, dx, dy, dz, color, alpha, start, high);
        if (alternateDiagonal) vertex(v, pose, ax, ay, az, color, alpha, start, low);
    }
}
