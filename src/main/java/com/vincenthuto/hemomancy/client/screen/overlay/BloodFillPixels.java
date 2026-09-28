package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.util.Mth;

final class BloodFillPixels {
    static final int WIDTH = 25;
    static final int HEIGHT = 92;

    private BloodFillPixels() {
    }

    static int[] render(int fillHeight, int degree, int pomeProgress, float time) {
        int[] pixels = new int[WIDTH * HEIGHT];
        int height = Mth.clamp(fillHeight, 0, HEIGHT);
        int top = HEIGHT - height;
        for (int row = top; row < HEIGHT; row++) {
            int halfInner = Math.max(1, BloodVesselShape.halfWidth(row, false));
            int color = colorAt(row, top, height, degree, pomeProgress, time);
            for (int x = 12 - halfInner; x <= 12 + halfInner; x++) {
                pixels[row * WIDTH + x] = color;
            }
        }
        return pixels;
    }

    static int colorAt(int row, int top, int height, int degree, int pomeProgress, float time) {
        float corruption = degree >= 7 ? Mth.clamp(pomeProgress / 9.0f, 0.0f, 1.0f) : 0.0f;
        float depth = Mth.clamp((row - top) / (float) Math.max(1, height), 0.0f, 1.0f);
        float pulse = 0.82f + 0.18f * Mth.sin(time * 1.5f + row * 0.11f);
        float fade = 1.0f - corruption * 0.70f;
        int color = multiplyColor(blendColor(0xEED32327, 0xEE520507, depth), pulse * fade);
        if (corruption > 0.0f) {
            color = alphaBlend(color, ((int) (118 * corruption) << 24) | 0x00030106);
        }
        return color;
    }

    static int meniscusColor(int degree, int pomeProgress) {
        int corruptionAlpha = degree >= 7 ? (int) (120 * (pomeProgress / 9.0f)) : 0;
        return degree >= 8 ? 0xAA560710
                : alphaBlend(0xBBDD2F2F, (corruptionAlpha << 24) | 0x00030106);
    }

    private static int blendColor(int from, int to, float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        return ((int) (alpha(from) + (alpha(to) - alpha(from)) * t) << 24)
                | ((int) (red(from) + (red(to) - red(from)) * t) << 16)
                | ((int) (green(from) + (green(to) - green(from)) * t) << 8)
                | (int) (blue(from) + (blue(to) - blue(from)) * t);
    }

    private static int alphaBlend(int base, int overlay) {
        float a = alpha(overlay) / 255.0f;
        return (Math.max(alpha(base), alpha(overlay)) << 24)
                | ((int) (red(base) * (1.0f - a) + red(overlay) * a) << 16)
                | ((int) (green(base) * (1.0f - a) + green(overlay) * a) << 8)
                | (int) (blue(base) * (1.0f - a) + blue(overlay) * a);
    }

    private static int multiplyColor(int color, float factor) {
        return (alpha(color) << 24)
                | ((int) Mth.clamp(red(color) * factor, 0, 255) << 16)
                | ((int) Mth.clamp(green(color) * factor, 0, 255) << 8)
                | (int) Mth.clamp(blue(color) * factor, 0, 255);
    }

    private static int alpha(int color) { return color >>> 24 & 0xFF; }
    private static int red(int color) { return color >>> 16 & 0xFF; }
    private static int green(int color) { return color >>> 8 & 0xFF; }
    private static int blue(int color) { return color & 0xFF; }
}
