package com.vincenthuto.hemomancy.client.screen.widget;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public final class WhiteHumorBarWidget {

    private WhiteHumorBarWidget() {
    }

    public record Bounds(int x1, int y1, int x2, int y2) {
        public static final Bounds EMPTY = new Bounds(0, 0, 0, 0);

        public boolean contains(int x, int y) {
            return x >= x1 && x < x2 && y >= y1 && y < y2;
        }
    }

    public static Bounds render(GuiGraphics gfx, int barX, int barY, int barW, int barH,
                                double volume, double maxVolume, float animTime,
                                int outerBorderColor, int innerBorderColor) {
        Bounds bounds = new Bounds(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2);
        double ratio = maxVolume > 0 ? Mth.clamp(volume / maxVolume, 0, 1) : 0;

        gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, outerBorderColor);
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, innerBorderColor);
        gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF060102);

        int fillH = (int) (barH * ratio);
        if (fillH > 0) {
            int fillTop = barY + barH - fillH;
            for (int row = 0; row < fillH; row++) {
                float rowT = (float) row / Math.max(fillH, 1);
                float pulse = 0.75f + 0.25f * Mth.sin(animTime * 2.5f + row * 0.08f);
                int r = (int) Mth.clamp((140 + 55 * rowT) * pulse, 0, 255);
                int g = (int) Mth.clamp((165 + 55 * rowT) * pulse, 0, 255);
                int b = (int) Mth.clamp((190 + 45 * rowT) * pulse, 0, 255);
                gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1,
                        (0xEE << 24) | (r << 16) | (g << 8) | b);
            }

            float meniscusPulse = 0.6f + 0.4f * Mth.sin(animTime * 3f);
            int meniscusAlpha = (int) (200 * meniscusPulse);
            gfx.fill(barX, fillTop, barX + barW, fillTop + 1,
                    (meniscusAlpha << 24) | (0xEE << 16) | (0xF8 << 8) | 0xFF);

            for (int row = 0; row < fillH; row++) {
                float fade = 0.3f + 0.15f * Mth.sin(animTime * 1.5f + row * 0.15f);
                int highlightAlpha = (int) (80 * fade);
                gfx.fill(barX + 1, fillTop + row, barX + 2, fillTop + row + 1,
                        (highlightAlpha << 24) | (0xFF << 16) | (0xFF << 8) | 0xFF);
            }

            Random bubbleRand = new Random(7777L);
            for (int bi = 0; bi < 4; bi++) {
                float speed = 0.4f + bubbleRand.nextFloat() * 0.6f;
                float phase = bubbleRand.nextFloat() * 100f;
                int bx = barX + 2 + bubbleRand.nextInt(Math.max(barW - 4, 1));
                float bubbleProgress = (animTime * speed + phase) % 1.0f;
                int by = fillTop + fillH - (int) (bubbleProgress * fillH);
                if (by >= fillTop && by < fillTop + fillH - 1) {
                    int alpha = (int) (60 * (1f - Math.abs(bubbleProgress - 0.5f) * 2f));
                    gfx.fill(bx, by, bx + 1, by + 1,
                            (alpha << 24) | (0xF2 << 16) | (0xFB << 8) | 0xFF);
                }
            }
        }

        for (int tick = 1; tick <= 3; tick++) {
            int tickY = barY + barH - (barH * tick / 4);
            gfx.fill(barX + barW, tickY, barX + barW + 1, tickY + 1, 0x60FFFFFF);
        }

        return bounds;
    }

    public static void renderTooltip(GuiGraphics gfx, Font font, Bounds bounds,
                                     double volume, double maxVolume, int mouseX, int mouseY) {
        if (!bounds.contains(mouseX, mouseY)) return;
        gfx.renderTooltip(font, List.of(
                Component.literal(String.format("\u00A77White Humor: \u00A7f%.0f \u00A77/ \u00A7f%.0f", volume, maxVolume))
        ), Optional.empty(), mouseX, mouseY);
    }
}
