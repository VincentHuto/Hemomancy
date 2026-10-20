package com.vincenthuto.hemomancy.client.render.outpouring;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SkinBodyAtlasTest {
    @Test void faceFrontIsForward() {
        float[] p = SkinBodyAtlas.point(12, 12, false);
        assertEquals(4, p[2], 1e-6);
        assertEquals(27.5, p[1], .6);
        assertEquals(0, p[3]);
    }

    @Test void headTopIsHighest() {
        assertEquals(32, SkinBodyAtlas.point(12, 4, false)[1], 1e-6);
    }

    @Test void rightHandIsLowAndRight() {
        float[] p = SkinBodyAtlas.point(50, 18, false);
        assertEquals(12, p[1], 1e-6);
        assertTrue(p[0] >= -8 && p[0] <= -4, "right arm hangs on the model's negative x: " + p[0]);
        assertEquals(2, p[3]);
    }

    @Test void slimArmIsNarrower() {
        assertEquals(7, widest(true), 1e-6);
        assertEquals(8, widest(false), 1e-6);
    }

    private static double widest(boolean slim) {
        double max = 0;
        for (int u = 40; u < 56; u++) for (int v = 16; v < 32; v++) {
            float[] p = SkinBodyAtlas.point(u, v, slim);
            if (p != null && p[3] == 2) max = Math.max(max, Math.abs(p[0]));
        }
        return max;
    }

    @Test void overlayMatchesBase() {
        assertArrayEquals(SkinBodyAtlas.point(44, 20, false), SkinBodyAtlas.point(44, 36, false));
        assertArrayEquals(SkinBodyAtlas.point(20, 52, false), SkinBodyAtlas.point(4, 52, false), "left leg and left pants");
    }

    @Test void unmappedIsNull() {
        assertNull(SkinBodyAtlas.point(0, 0, false));
        assertNull(SkinBodyAtlas.point(60, 2, false));
    }

    @Test void legsReachTheFeet() {
        // Right leg UP face (model y max) is the sole of the foot.
        assertEquals(0, SkinBodyAtlas.point(10, 18, false)[1], 1e-6);
    }

    @Test void encodingRoundTrips() {
        for (boolean slim : new boolean[]{false, true}) {
            int[] pixels = SkinBodyAtlas.rgba(slim);
            assertEquals(64 * 64, pixels.length);
            for (int v = 0; v < 64; v++) for (int u = 0; u < 64; u++) {
                float[] p = SkinBodyAtlas.point(u, v, slim);
                int abgr = pixels[v * 64 + u];
                int a = abgr >>> 24, b = abgr >> 16 & 255, g = abgr >> 8 & 255, r = abgr & 255;
                if (p == null) { assertEquals(0, a, "unmapped " + u + "," + v); continue; }
                assertEquals(p[0], r / 255f * 16 - 8, 16 / 250f);
                assertEquals(p[1], g / 255f * 32, 32 / 250f);
                assertEquals(p[2], b / 255f * 8 - 4, 8 / 250f);
                assertEquals((int) p[4] * 6 + (int) p[5] + 1, a, "axis codes at " + u + "," + v);
            }
        }
    }

    /** Stepping one texel along u or v moves the body point one pixel along the axis the texel records. */
    @Test void gradientsMatchNeighbourTexels() {
        float[][] axes = {{1, 0, 0}, {-1, 0, 0}, {0, 1, 0}, {0, -1, 0}, {0, 0, 1}, {0, 0, -1}};
        int checked = 0;
        for (boolean slim : new boolean[]{false, true}) for (int v = 0; v < 63; v++) for (int u = 0; u < 63; u++) {
            float[] p = SkinBodyAtlas.point(u, v, slim), right = SkinBodyAtlas.point(u + 1, v, slim), down = SkinBodyAtlas.point(u, v + 1, slim);
            if (p == null) continue;
            if (right != null && right[4] == p[4] && right[5] == p[5] && right[6] == p[6] && right[3] == p[3]) {
                float[] axis = axes[(int) p[4]];
                for (int i = 0; i < 3; i++) assertEquals(p[i] + axis[i], right[i], 1e-4, "u step at " + u + "," + v);
                checked++;
            }
            if (down != null && down[4] == p[4] && down[5] == p[5] && down[6] == p[6] && down[3] == p[3]) {
                float[] axis = axes[(int) p[5]];
                for (int i = 0; i < 3; i++) assertEquals(p[i] + axis[i], down[i], 1e-4, "v step at " + u + "," + v);
                checked++;
            }
        }
        assertTrue(checked > 3000, "most texels have a same-face neighbour: " + checked);
    }
}
