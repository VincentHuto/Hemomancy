package com.vincenthuto.hemomancy.client.render.entity.boss.annetta;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;

/**
 * Draws living tendrils from centre lines given in model pixels: a lit, textured eight-sided core whose texture
 * runs the length, an additive glow shell, and veined webbing stretched between two tendrils. Frames are
 * parallel-transported so a curling tendril never twists its texture.
 */
final class TendrilTube {
    static final int SIDES = 8;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final float[] COS = new float[SIDES + 1];
    private static final float[] SIN = new float[SIDES + 1];

    static {
        for (int k = 0; k <= SIDES; k++) {
            COS[k] = Mth.cos(k * Mth.TWO_PI / SIDES);
            SIN[k] = Mth.sin(k * Mth.TWO_PI / SIDES);
        }
    }

    private TendrilTube() {}

    /** Right/up frame per station, plus the running length in pixels (index 6). */
    static float[] frames(float[] line) {
        int n = line.length / 3;
        float[] f = new float[n * 7];
        float rx = 0, ry = 0, rz = 0;
        float run = 0.0F;
        for (int i = 0; i < n; i++) {
            int a = Math.max(0, i - 1) * 3, b = Math.min(n - 1, i + 1) * 3;
            float tx = line[b] - line[a], ty = line[b + 1] - line[a + 1], tz = line[b + 2] - line[a + 2];
            float tl = Mth.sqrt(tx * tx + ty * ty + tz * tz);
            if (tl < 1.0E-5F) {
                tx = 0;
                ty = -1;
                tz = 0;
            } else {
                tx /= tl;
                ty /= tl;
                tz /= tl;
            }
            if (i == 0) {
                // Seed the frame from world up, falling back to +X for vertical tendrils.
                rx = ty * 0 - tz * 1;
                ry = tz * 0 - tx * 0;
                rz = tx * 1 - ty * 0;
                if (rx * rx + ry * ry + rz * rz < 1.0E-4F) {
                    rx = 0;
                    ry = tz;
                    rz = -ty;
                }
            } else {
                float d = rx * tx + ry * ty + rz * tz;
                rx -= tx * d;
                ry -= ty * d;
                rz -= tz * d;
                int p = (i - 1) * 3, c = i * 3;
                float dx = line[c] - line[p], dy = line[c + 1] - line[p + 1], dz = line[c + 2] - line[p + 2];
                run += Mth.sqrt(dx * dx + dy * dy + dz * dz);
            }
            float rl = Mth.sqrt(rx * rx + ry * ry + rz * rz);
            if (rl < 1.0E-5F) {
                rx = 1;
                ry = 0;
                rz = 0;
                rl = 1;
            }
            rx /= rl;
            ry /= rl;
            rz /= rl;
            int o = i * 7;
            f[o] = rx;
            f[o + 1] = ry;
            f[o + 2] = rz;
            f[o + 3] = ty * rz - tz * ry;
            f[o + 4] = tz * rx - tx * rz;
            f[o + 5] = tx * ry - ty * rx;
            f[o + 6] = run;
        }
        return f;
    }

    /** Radius per station: tapering from root to tip with a few swollen vessel knots along the way. */
    static float[] taper(int stations, float root, float tip, float knots, long seed) {
        float[] radii = new float[stations];
        for (int i = 0; i < stations; i++) {
            float s = i / (stations - 1.0F);
            float knot = 1.0F + 0.22F * Math.max(0.0F, Mth.sin(s * knots * Mth.TWO_PI + seed % 7)) * (1.0F - s);
            radii[i] = Mth.lerp(s * s * 0.6F + s * 0.4F, root, tip) * knot;
        }
        return radii;
    }

    static void core(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] radii, float vOffset, int light,
            int overlay, float shade) {
        float[] f = frames(line);
        int n = line.length / 3;
        float vScale = 1.0F / 24.0F;
        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k < SIDES; k++) {
                coreVertex(pose, out, line, f, radii, i, k, vOffset, vScale, light, overlay, shade);
                coreVertex(pose, out, line, f, radii, i, k + 1, vOffset, vScale, light, overlay, shade);
                coreVertex(pose, out, line, f, radii, i + 1, k + 1, vOffset, vScale, light, overlay, shade);
                coreVertex(pose, out, line, f, radii, i + 1, k, vOffset, vScale, light, overlay, shade);
            }
        }
        // Close both ends so a tendril never shows as a hollow pipe.
        cap(pose, out, line, f, radii, 0, -1.0F, vOffset, light, overlay, shade);
        cap(pose, out, line, f, radii, n - 1, 1.0F, vOffset + f[(n - 1) * 7 + 6] * vScale, light, overlay, shade);
    }

    /** A fan over one end ring, facing along the tendril (direction +1) or back against it (-1). */
    private static void cap(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] f, float[] radii, int i,
            float direction, float v, int light, int overlay, float shade) {
        int o = i * 7;
        // The tangent is right x up.
        float tx = (f[o + 1] * f[o + 5] - f[o + 2] * f[o + 4]) * direction;
        float ty = (f[o + 2] * f[o + 3] - f[o] * f[o + 5]) * direction;
        float tz = (f[o] * f[o + 4] - f[o + 1] * f[o + 3]) * direction;
        float cx = line[i * 3], cy = line[i * 3 + 1], cz = line[i * 3 + 2];
        float r = radii[i];
        for (int k = 0; k < SIDES; k++) {
            int a = direction > 0 ? k : k + 1, b = direction > 0 ? k + 1 : k;
            capVertex(pose, out, cx, cy, cz, 0.5F, v, tx, ty, tz, light, overlay, shade);
            capVertex(pose, out, cx + (f[o] * COS[a] + f[o + 3] * SIN[a]) * r, cy + (f[o + 1] * COS[a] + f[o + 4] * SIN[a]) * r,
                    cz + (f[o + 2] * COS[a] + f[o + 5] * SIN[a]) * r, a / (float) SIDES, v, tx, ty, tz, light, overlay, shade);
            capVertex(pose, out, cx + (f[o] * COS[b] + f[o + 3] * SIN[b]) * r, cy + (f[o + 1] * COS[b] + f[o + 4] * SIN[b]) * r,
                    cz + (f[o + 2] * COS[b] + f[o + 5] * SIN[b]) * r, b / (float) SIDES, v, tx, ty, tz, light, overlay, shade);
            capVertex(pose, out, cx, cy, cz, 0.5F, v, tx, ty, tz, light, overlay, shade);
        }
    }

    private static void capVertex(PoseStack.Pose pose, VertexConsumer out, float x, float y, float z, float u, float v,
            float nx, float ny, float nz, int light, int overlay, float shade) {
        out.addVertex(pose, x * PIXEL, y * PIXEL, z * PIXEL)
                .setColor(shade, shade, shade, 1.0F)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    private static void coreVertex(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] f, float[] radii,
            int i, int k, float vOffset, float vScale, int light, int overlay, float shade) {
        int o = i * 7;
        float nx = f[o] * COS[k] + f[o + 3] * SIN[k];
        float ny = f[o + 1] * COS[k] + f[o + 4] * SIN[k];
        float nz = f[o + 2] * COS[k] + f[o + 5] * SIN[k];
        float r = radii[i];
        out.addVertex(pose, (line[i * 3] + nx * r) * PIXEL, (line[i * 3 + 1] + ny * r) * PIXEL,
                        (line[i * 3 + 2] + nz * r) * PIXEL)
                .setColor(shade, shade, shade, 1.0F)
                .setUv(k / (float) SIDES, vOffset + f[o + 6] * vScale)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    /** Additive shell; alpha per station lets a pulse travel along it. */
    static void glow(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] radii, float widthScale,
            float red, float green, float blue, float[] alpha) {
        float[] f = frames(line);
        int n = line.length / 3;
        for (int i = 0; i < n - 1; i++) {
            for (int k = 0; k < SIDES; k += 2) {
                glowVertex(pose, out, line, f, radii, widthScale, i, k, red, green, blue, alpha[i]);
                glowVertex(pose, out, line, f, radii, widthScale, i, k + 2, red, green, blue, alpha[i]);
                glowVertex(pose, out, line, f, radii, widthScale, i + 1, k + 2, red, green, blue, alpha[i + 1]);
                glowVertex(pose, out, line, f, radii, widthScale, i + 1, k, red, green, blue, alpha[i + 1]);
            }
        }
    }

    private static void glowVertex(PoseStack.Pose pose, VertexConsumer out, float[] line, float[] f, float[] radii,
            float widthScale, int i, int k, float red, float green, float blue, float alpha) {
        int o = i * 7;
        int side = k % SIDES;
        float r = radii[i] * widthScale;
        float nx = f[o] * COS[side] + f[o + 3] * SIN[side];
        float ny = f[o + 1] * COS[side] + f[o + 4] * SIN[side];
        float nz = f[o + 2] * COS[side] + f[o + 5] * SIN[side];
        out.addVertex(pose, (line[i * 3] + nx * r) * PIXEL, (line[i * 3 + 1] + ny * r) * PIXEL,
                (line[i * 3 + 2] + nz * r) * PIXEL).setColor(red, green, blue, alpha);
    }

    /**
     * Membrane between stations {@code from..to} of two tendrils, billowing downward between them. Texture u runs
     * across the gap and v from root to the torn edge.
     */
    static void webbing(PoseStack.Pose pose, VertexConsumer out, float[] a, float[] b, int from, int to, float sag,
            int light, int overlay, float alpha) {
        final int columns = 4;
        for (int i = from; i < to; i++) {
            for (int c = 0; c < columns; c++) {
                float u0 = c / (float) columns, u1 = (c + 1) / (float) columns;
                webVertex(pose, out, a, b, i, u0, from, to, sag, light, overlay, alpha);
                webVertex(pose, out, a, b, i, u1, from, to, sag, light, overlay, alpha);
                webVertex(pose, out, a, b, i + 1, u1, from, to, sag, light, overlay, alpha);
                webVertex(pose, out, a, b, i + 1, u0, from, to, sag, light, overlay, alpha);
            }
        }
    }

    private static void webVertex(PoseStack.Pose pose, VertexConsumer out, float[] a, float[] b, int i, float u,
            int from, int to, float sag, int light, int overlay, float alpha) {
        float v = (i - from) / (float) Math.max(1, to - from);
        float billow = sag * Mth.sin(u * Mth.PI) * (0.4F + v);
        float x = Mth.lerp(u, a[i * 3], b[i * 3]);
        float y = Mth.lerp(u, a[i * 3 + 1], b[i * 3 + 1]) + billow;
        float z = Mth.lerp(u, a[i * 3 + 2], b[i * 3 + 2]);
        float ex = b[i * 3] - a[i * 3], ey = b[i * 3 + 1] - a[i * 3 + 1], ez = b[i * 3 + 2] - a[i * 3 + 2];
        // Along-finger direction, taken backward on the last row and flipped so the normal never reverses.
        int j = i < to ? i + 1 : i - 1;
        float sign = i < to ? 1.0F : -1.0F;
        float sx = (a[j * 3] - a[i * 3]) * sign, sy = (a[j * 3 + 1] - a[i * 3 + 1]) * sign, sz = (a[j * 3 + 2] - a[i * 3 + 2]) * sign;
        float nx = ey * sz - ez * sy, ny = ez * sx - ex * sz, nz = ex * sy - ey * sx;
        float nl = Mth.sqrt(nx * nx + ny * ny + nz * nz);
        if (nl < 1.0E-5F) {
            nx = 0;
            ny = -1;
            nz = 0;
            nl = 1;
        }
        out.addVertex(pose, x * PIXEL, y * PIXEL, z * PIXEL)
                .setColor(1.0F, 1.0F, 1.0F, alpha)
                .setUv(u, v)
                .setOverlay(overlay)
                .setLight(light)
                .setNormal(pose, nx / nl, ny / nl, nz / nl);
    }

    /** A forward-facing (-Z) card centred in pixels, rolled in degrees; used for the eyes. */
    static void card(PoseStack.Pose pose, VertexConsumer out, float cx, float cy, float cz, float width,
            float height, float rollDegrees, float brightness) {
        float roll = rollDegrees * Mth.DEG_TO_RAD;
        float ux = Mth.cos(roll) * width / 2, uy = Mth.sin(roll) * width / 2;
        float vx = -Mth.sin(roll) * height / 2, vy = Mth.cos(roll) * height / 2;
        float[][] corners = {{-ux - vx, -uy - vy, 0, 0}, {ux - vx, uy - vy, 1, 0}, {ux + vx, uy + vy, 1, 1},
                {-ux + vx, -uy + vy, 0, 1}};
        // Both windings, so the card shows whichever way the eyes render type culls.
        for (int pass = 0; pass < 2; pass++) {
            for (int c = 0; c < 4; c++) {
                float[] corner = corners[pass == 0 ? c : 3 - c];
                out.addVertex(pose, (cx + corner[0]) * PIXEL, (cy + corner[1]) * PIXEL, cz * PIXEL)
                        .setColor(brightness, brightness, brightness, 1.0F)
                        .setUv(corner[2], corner[3])
                        .setOverlay(0)
                        .setLight(0xF000F0)
                        .setNormal(pose, 0, 0, -1);
            }
        }
    }

    /** A soft additive disc of quads fanning from the centre, fading to nothing at its rim. */
    static void halo(PoseStack.Pose pose, VertexConsumer out, float cx, float cy, float cz, float width, float height,
            float red, float green, float blue, float alpha) {
        final int slices = 12;
        for (int i = 0; i < slices; i++) {
            float a0 = i * Mth.TWO_PI / slices, a1 = (i + 1) * Mth.TWO_PI / slices;
            // Both windings: the glow render type culls, and the bloom must show from the front.
            for (int pass = 0; pass < 2; pass++) {
                float first = pass == 0 ? a0 : a1, second = pass == 0 ? a1 : a0;
                out.addVertex(pose, cx * PIXEL, cy * PIXEL, cz * PIXEL).setColor(red, green, blue, alpha);
                out.addVertex(pose, (cx + Mth.cos(first) * width / 2) * PIXEL, (cy + Mth.sin(first) * height / 2) * PIXEL, cz * PIXEL).setColor(red, green, blue, 0.0F);
                out.addVertex(pose, (cx + Mth.cos(second) * width / 2) * PIXEL, (cy + Mth.sin(second) * height / 2) * PIXEL, cz * PIXEL).setColor(red, green, blue, 0.0F);
                out.addVertex(pose, cx * PIXEL, cy * PIXEL, cz * PIXEL).setColor(red, green, blue, alpha);
            }
        }
    }
}
