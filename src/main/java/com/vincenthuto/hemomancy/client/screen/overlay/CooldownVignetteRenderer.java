package com.vincenthuto.hemomancy.client.screen.overlay;

import net.minecraft.client.gui.GuiGraphics;

final class CooldownVignetteRenderer {
    private CooldownVignetteRenderer() {
    }

    static void renderRedVignette(GuiGraphics graphics, int screenWidth, int screenHeight, float alpha) {
        int edgeSize = (int) (Math.min(screenWidth, screenHeight) * 0.35F);
        int a = (int) (alpha * 255);
        int opaqueColor = (a << 24) | (153 << 16);
        int transparentColor = 153 << 16;

        graphics.fillGradient(0, 0, screenWidth, edgeSize, opaqueColor, transparentColor);
        graphics.fillGradient(0, screenHeight - edgeSize, screenWidth, screenHeight, transparentColor, opaqueColor);
        for (int column = 0; column < edgeSize; column++) {
            float leftStrength = 1.0F - (float) column / edgeSize;
            int leftColor = ((int) (a * leftStrength) << 24) | (153 << 16);
            graphics.fill(column, 0, column + 1, screenHeight, leftColor);

            float rightStrength = (float) column / edgeSize;
            int rightColor = ((int) (a * rightStrength) << 24) | (153 << 16);
            int x = screenWidth - edgeSize + column;
            graphics.fill(x, 0, x + 1, screenHeight, rightColor);
        }
    }
}
