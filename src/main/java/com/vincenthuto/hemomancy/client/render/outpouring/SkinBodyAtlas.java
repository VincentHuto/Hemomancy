package com.vincenthuto.hemomancy.client.render.outpouring;

/**
 * Maps every texel of the 64×64 player skin to the point it covers on a standing body, so the vein shader can grow veins in
 * body space: continuous across UV seams, across the overlay layers, and across an armour shell baked with the same UVs.
 * Body space is in model pixels: x is model x (negative is the player's right), y is height above the feet (0..32), z is
 * forward. Pure, so the unwrap is pinned by JVM tests.
 */
public final class SkinBodyAtlas {
    /** texOffs u, v, box origin x, y, z, wide width, height, depth, pivot x, y, part id; overlay layers repeat their base box. */
    private static final float[][] PARTS = {
            {0, 0, -4, -8, -4, 8, 8, 8, 0, 0, 0}, {32, 0, -4, -8, -4, 8, 8, 8, 0, 0, 0},
            {16, 16, -4, 0, -2, 8, 12, 4, 0, 0, 1}, {16, 32, -4, 0, -2, 8, 12, 4, 0, 0, 1},
            {40, 16, -3, -2, -2, 4, 12, 4, -5, 2, 2}, {40, 32, -3, -2, -2, 4, 12, 4, -5, 2, 2},
            {32, 48, -1, -2, -2, 4, 12, 4, 5, 2, 3}, {48, 48, -1, -2, -2, 4, 12, 4, 5, 2, 3},
            {0, 16, -2, 0, -2, 4, 12, 4, -1.9f, 12, 4}, {0, 32, -2, 0, -2, 4, 12, 4, -1.9f, 12, 4},
            {16, 48, -2, 0, -2, 4, 12, 4, 1.9f, 12, 5}, {0, 48, -2, 0, -2, 4, 12, 4, 1.9f, 12, 5}};
    private static final int[][] CACHE = new int[2][];

    private SkinBodyAtlas() {}

    /**
     * {x, y, z, part, uAxis, vAxis, face} for the texel's centre, or null when no face of the player mesh samples it.
     * Position is in body pixels. uAxis and vAxis name the body axis one texel step along u or v moves by one pixel
     * (0 +x, 1 -x, 2 +y, 3 -y, 4 +z, 5 -z), so a shader can rebuild an exact position inside the texel.
     */
    public static float[] point(int u, int v, boolean slim) {
        for (float[] part : PARTS) {
            float[] p = onPart(part, u + .5f, v + .5f, slim);
            if (p != null) return p;
        }
        return null;
    }

    /**
     * ABGR pixels for {@code NativeImage.setPixelRGBA}: R = (x + 8) / 16, G = y / 32, B = (z + 4) / 8,
     * A = uAxis · 6 + vAxis + 1. Unmapped texels are fully transparent so the shader discards them.
     */
    public static synchronized int[] rgba(boolean slim) {
        int index = slim ? 1 : 0;
        if (CACHE[index] == null) {
            int[] pixels = new int[64 * 64];
            for (int v = 0; v < 64; v++) for (int u = 0; u < 64; u++) {
                float[] p = point(u, v, slim);
                if (p == null) continue;
                int r = channel((p[0] + 8) / 16), g = channel(p[1] / 32), b = channel((p[2] + 4) / 8);
                pixels[v * 64 + u] = ((int) p[4] * 6 + (int) p[5] + 1) << 24 | b << 16 | g << 8 | r;
            }
            CACHE[index] = pixels;
        }
        return CACHE[index];
    }

    private static int channel(float value) { return Math.round(Math.clamp(value, 0, 1) * 255); }

    private static float[] onPart(float[] part, float u, float v, boolean slim) {
        int id = (int) part[10];
        boolean arm = id == 2 || id == 3;
        float x0 = part[2], w = slim && arm ? 3 : part[5], h = part[6], d = part[7];
        // The slim right arm keeps its outer face where the wide one does; it narrows towards the body.
        if (slim && id == 2) x0 += 1;
        float y0 = part[3], z0 = part[4];
        float f = x0, f1 = y0, f2 = z0, f3 = x0 + w, f4 = y0 + h, f5 = z0 + d;
        float tu = part[0], tv = part[1];
        float u1 = tu + d, u2 = u1 + w, u3 = u2 + w, u4 = u2 + d, u5 = u4 + w, v1 = tv + d, v2 = v1 + h;
        // Model y runs down and model z runs backward, so body +y is model -y and body +z is model -z.
        float[] m = null;
        int face = -1, uAxis = 0, vAxis = 0;
        if (v >= tv && v < v1) {
            if (u >= u1 && u < u2) { m = new float[]{f + (u - u1), f1, f5 - (v - tv)}; face = 0; uAxis = 0; vAxis = 4; }
            else if (u >= u2 && u < u3) { m = new float[]{f + (u - u2), f4, f5 - (v - tv)}; face = 1; uAxis = 0; vAxis = 4; }
        } else if (v >= v1 && v < v2) {
            float t = v - v1;
            if (u >= tu && u < u1) { m = new float[]{f, f1 + t, f5 - (u - tu)}; face = 2; uAxis = 4; vAxis = 3; }
            else if (u >= u1 && u < u2) { m = new float[]{f + (u - u1), f1 + t, f2}; face = 3; uAxis = 0; vAxis = 3; }
            else if (u >= u2 && u < u4) { m = new float[]{f3, f1 + t, f2 + (u - u2)}; face = 4; uAxis = 5; vAxis = 3; }
            else if (u >= u4 && u < u5) { m = new float[]{f3 - (u - u4), f1 + t, f5}; face = 5; uAxis = 1; vAxis = 3; }
        }
        if (m == null) return null;
        // Vanilla hangs slim arms half a pixel lower.
        float pivotY = part[9] + (slim && arm ? .5f : 0);
        return new float[]{m[0] + part[8], 24 - (m[1] + pivotY), -m[2], id, uAxis, vAxis, face};
    }
}
