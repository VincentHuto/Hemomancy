package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.util.Mth;

final class GourdHudFillPixels {
    static final int WIDTH = 32;
    static final int HEIGHT = 64;
    static final int WINDOW_TOP = 22;
    static final int WINDOW_BOTTOM = 48;
    private static final int[] WINDOW_HALF_WIDTHS = {
            1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1, 1, 1, 1, 1, 1,
            2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1
    };

    private GourdHudFillPixels() {
    }

    static int[] render(double ratio, int degree, int pomeProgress, float time) {
        int[] pixels = new int[WIDTH * HEIGHT];
        int fillHeight = Mth.clamp((int) Math.round(ratio * WINDOW_HALF_WIDTHS.length), 0, WINDOW_HALF_WIDTHS.length);
        int top = WINDOW_BOTTOM + 1 - fillHeight;
        for (int y = top; y <= WINDOW_BOTTOM; y++) {
            int halfWidth = WINDOW_HALF_WIDTHS[y - WINDOW_TOP];
            int vesselRow = (int) ((y - WINDOW_TOP) * (BloodFillPixels.HEIGHT - 1.0f)
                    / (WINDOW_BOTTOM - WINDOW_TOP));
            int vesselTop = (int) ((top - WINDOW_TOP) * (BloodFillPixels.HEIGHT - 1.0f)
                    / (WINDOW_BOTTOM - WINDOW_TOP));
            int color = visibleBlood(BloodFillPixels.colorAt(vesselRow, vesselTop,
                    BloodFillPixels.HEIGHT - vesselTop, degree, pomeProgress, time));
            for (int x = 16 - halfWidth; x <= 16 + halfWidth; x++) {
                pixels[y * WIDTH + x] = color;
            }
        }
        if (fillHeight > 0) {
            int surface = Mth.clamp(top + Math.round(Mth.sin(time * 2.2f + top * 0.3f) * 1.25f),
                    WINDOW_TOP, WINDOW_BOTTOM);
            int halfWidth = WINDOW_HALF_WIDTHS[surface - WINDOW_TOP];
            int color = visibleBlood(BloodFillPixels.meniscusColor(degree, pomeProgress));
            for (int x = 16 - halfWidth; x <= 16 + halfWidth; x++) {
                pixels[surface * WIDTH + x] = color;
            }
        }
        return pixels;
    }

    static int visibleBlood(int color) {
        int red = Mth.clamp((int) (70 + (color >>> 16 & 0xFF) * 1.3f), 165, 255);
        int green = Mth.clamp(10 + (int) ((color >>> 8 & 0xFF) * 0.85f), 0, 255);
        int blue = Mth.clamp(14 + (int) ((color & 0xFF) * 0.8f), 0, 255);
        return 0xFF000000 | red << 16 | green << 8 | blue;
    }
}
