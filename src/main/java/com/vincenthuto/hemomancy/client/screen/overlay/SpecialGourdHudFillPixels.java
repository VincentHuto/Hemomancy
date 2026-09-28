package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.util.Mth;

/** Blood layer shared by the curved horn and Hemorath rib sprites. */
final class SpecialGourdHudFillPixels {
    static final int WIDTH = 32;
    static final int HEIGHT = 64;

    private SpecialGourdHudFillPixels() {
    }

    static int[] render(boolean[] mask, int spriteHeight, double ratio, int degree, int pomeProgress, float time) {
        if (spriteHeight > HEIGHT || mask.length != WIDTH * spriteHeight) {
            throw new IllegalArgumentException("Invalid special gourd HUD mask dimensions");
        }
        int[] pixels = new int[WIDTH * HEIGHT];
        int top = spriteHeight;
        int bottom = -1;
        for (int y = 0; y < spriteHeight; y++) {
            for (int x = 0; x < WIDTH; x++) {
                if (mask[y * WIDTH + x]) {
                    top = Math.min(top, y);
                    bottom = y;
                }
            }
        }
        if (bottom < top) {
            return pixels;
        }
        int span = bottom - top + 1;
        int fillRows = Mth.clamp((int) Math.round(Mth.clamp(ratio, 0.0, 1.0) * span), 0, span);
        if (fillRows == 0) {
            return pixels;
        }
        int surface = bottom + 1 - fillRows;
        int vesselTop = (surface - top) * (BloodFillPixels.HEIGHT - 1) / Math.max(1, span - 1);
        int meniscus = GourdHudFillPixels.visibleBlood(BloodFillPixels.meniscusColor(degree, pomeProgress));
        for (int y = surface; y <= bottom; y++) {
            int vesselRow = (y - top) * (BloodFillPixels.HEIGHT - 1) / Math.max(1, span - 1);
            int blood = GourdHudFillPixels.visibleBlood(BloodFillPixels.colorAt(vesselRow, vesselTop,
                    BloodFillPixels.HEIGHT - vesselTop, degree, pomeProgress, time));
            for (int x = 0; x < WIDTH; x++) {
                if (mask[y * WIDTH + x]) {
                    pixels[y * WIDTH + x] = y == surface ? meniscus : blood;
                }
            }
        }
        return pixels;
    }
}
