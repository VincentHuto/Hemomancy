package com.vincenthuto.hemomancy.client.screen.widget;

import com.vincenthuto.hemomancy.client.screen.skilltree.util.ScreenDrawUtils;
import com.vincenthuto.hemomancy.common.tile.ReservoirReading;
import com.vincenthuto.hemomancy.common.tile.ReservoirReadout;
import com.vincenthuto.hemomancy.common.util.HemoText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;

/**
 * A station's blood tank. Since core-loop Track 3 one bar shows two parts: fresh blood, whose colour drains toward a
 * dull brown as it ages through the fresh window, standing on a still, dark band of clot. A sutured tank whose suture
 * has stalled shows a broken-suture pip on the bar's top edge, and its hover says why.
 */
public final class BloodVolumeBarWidget {

    private BloodVolumeBarWidget() {
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
        return render(gfx, barX, barY, barW, barH, ReservoirReading.plain(volume, maxVolume), animTime,
                outerBorderColor, innerBorderColor);
    }

    public static Bounds render(GuiGraphics gfx, int barX, int barY, int barW, int barH, ReservoirReading reading,
                                float animTime, int outerBorderColor, int innerBorderColor) {
        Bounds bounds = new Bounds(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2);
        double capacity = reading.capacity();
        double clotRatio = capacity > 0 ? Mth.clamp(reading.clotted() / capacity, 0, 1) : 0;
        double freshRatio = capacity > 0 ? Mth.clamp(reading.fresh() / capacity, 0, 1 - clotRatio) : 0;

        gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, outerBorderColor);
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, innerBorderColor);
        gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF060102);

        int clotH = (int) Math.round(barH * clotRatio);
        int clotTop = barY + barH - clotH;
        drawClot(gfx, barX, clotTop, barW, clotH);

        int fillH = (int) (barH * freshRatio);
        if (fillH > 0) {
            int fillTop = clotTop - fillH;
            // Freshness drains the colour: bright arterial red when just fed, a dull liver brown at the window's end.
            float fresh = (float) Mth.clamp(reading.freshness(), 0, 1);
            float vivid = 0.45f + 0.55f * fresh;
            float pulseDepth = 0.25f * fresh;
            for (int row = 0; row < fillH; row++) {
                float rowT = (float) row / Math.max(fillH, 1);
                float pulse = (1f - pulseDepth) + pulseDepth * Mth.sin(animTime * 2.5f + row * 0.08f);
                int r = (int) Mth.clamp((100 + 80 * rowT) * pulse * vivid, 0, 255);
                int g = (int) Mth.clamp(((5 + 15 * rowT) * pulse + 22 * (1f - fresh)) * (0.6f + 0.4f * vivid), 0, 255);
                int b = (int) Mth.clamp(((8 + 10 * rowT) * pulse + 8 * (1f - fresh)), 0, 255);
                gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1,
                        (0xEE << 24) | (r << 16) | (g << 8) | b);
            }

            float meniscusPulse = 0.6f + 0.4f * Mth.sin(animTime * 3f);
            int meniscusAlpha = (int) (200 * meniscusPulse * (0.4f + 0.6f * fresh));
            gfx.fill(barX, fillTop, barX + barW, fillTop + 1,
                    (meniscusAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x18);

            for (int row = 0; row < fillH; row++) {
                float fade = 0.3f + 0.15f * Mth.sin(animTime * 1.5f + row * 0.15f);
                int highlightAlpha = (int) (80 * fade * (0.3f + 0.7f * fresh));
                gfx.fill(barX + 1, fillTop + row, barX + 2, fillTop + row + 1,
                        (highlightAlpha << 24) | (0xFF << 16) | (0x60 << 8) | 0x50);
            }

            Random bubbleRand = new Random(7777L);
            for (int bi = 0; bi < 3; bi++) {
                float speed = 0.4f + bubbleRand.nextFloat() * 0.6f;
                float phase = bubbleRand.nextFloat() * 100f;
                int bx = barX + 2 + bubbleRand.nextInt(Math.max(barW - 4, 1));
                float bubbleProgress = (animTime * speed + phase) % 1.0f;
                int by = fillTop + fillH - (int) (bubbleProgress * fillH);
                if (by >= fillTop && by < fillTop + fillH - 1) {
                    int alpha = (int) (60 * fresh * (1f - Math.abs(bubbleProgress - 0.5f) * 2f));
                    gfx.fill(bx, by, bx + 1, by + 1,
                            (alpha << 24) | (0xFF << 16) | (0x40 << 8) | 0x30);
                }
            }
        }

        for (int tick = 1; tick <= 3; tick++) {
            int tickY = barY + barH - (barH * tick / 4);
            gfx.fill(barX + barW, tickY, barX + barW + 1, tickY + 1, 0x60FFFFFF);
        }

        if (reading.link().stalled()) drawStalledPip(gfx, barX + barW / 2 - STALL_PIP / 2, barY - 2 - STALL_PIP / 2, animTime);
        return bounds;
    }

    /** The broken-suture pip's size: drawn on the bar's top edge while the tank's suture is stalled. */
    private static final int STALL_PIP = 7;

    /**
     * A broken suture: a bevelled well with a gold stitch cut through the middle, so a stalled machine says so without
     * a hover (UN-55). A gold halo pulses round it; the hover line says why.
     */
    private static void drawStalledPip(GuiGraphics gfx, int x, int y, float animTime) {
        float beat = 0.5f + 0.5f * Mth.sin(animTime * 4.0f);
        int halo = ((int) (30 + 110 * beat) << 24) | 0xD8A040;
        ScreenDrawUtils.drawSimpleBorder(gfx, x - 1, y - 1, STALL_PIP + 2, STALL_PIP + 2, halo);
        int gold = ((int) (190 + 65 * beat) << 24) | 0xD8A040;
        ScreenDrawUtils.drawBevelBorder(gfx, x, y, STALL_PIP, STALL_PIP, 0xFF6A4A1A, 0xFF24160A);
        gfx.fill(x + 1, y + 1, x + STALL_PIP - 1, y + STALL_PIP - 1, 0xFF0C0503);
        gfx.fill(x + 1, y + 5, x + 2, y + 6, gold);
        gfx.fill(x + 2, y + 4, x + 3, y + 5, gold);
        gfx.fill(x + 4, y + 2, x + 5, y + 3, gold);
        gfx.fill(x + 5, y + 1, x + 6, y + 2, gold);
    }

    /** Clot: still, dark and grained, with a lighter crust where it meets the fresh blood. */
    private static void drawClot(GuiGraphics gfx, int x, int top, int w, int h) {
        if (h <= 0) return;
        gfx.fill(x, top, x + w, top + h, 0xFF2A0C09);
        for (int row = 0; row < h; row++) {
            for (int col = 0; col < w; col++) {
                int hash = (row * 73 + col * 151 + (row * col) * 17) & 31;
                if (hash < 5) gfx.fill(x + col, top + row, x + col + 1, top + row + 1, 0xFF140504);
                else if (hash > 28) gfx.fill(x + col, top + row, x + col + 1, top + row + 1, 0xFF4A1A12);
            }
        }
        gfx.fill(x, top, x + w, top + 1, 0xFF5C2418);
    }

    public static void renderTooltip(GuiGraphics gfx, Font font, Bounds bounds,
                                     double volume, double maxVolume, int mouseX, int mouseY) {
        renderTooltip(gfx, font, bounds, ReservoirReading.plain(volume, maxVolume), mouseX, mouseY);
    }

    public static void renderTooltip(GuiGraphics gfx, Font font, Bounds bounds, ReservoirReading reading,
                                     int mouseX, int mouseY) {
        if (!bounds.contains(mouseX, mouseY)) return;
        List<Component> lines = new ArrayList<>();
        lines.add(HemoText.translatable("screen.hemomancy.blood_volume_bar_widget.blood",
                String.format("%.0f", reading.fresh()), String.format("%.0f", reading.capacity())));
        if (reading.clotted() > 0.5D) {
            lines.add(HemoText.translatable("screen.hemomancy.blood_volume_bar_widget.clot",
                    String.format("%.0f", reading.clotted())));
            if (!reading.living())
                lines.add(Component.translatable("screen.hemomancy.blood_volume_bar_widget.revive").withStyle(ChatFormatting.GRAY));
        }
        ReservoirReadout.State state = ReservoirReadout.state(reading.living(), reading.fresh(), reading.clotted(),
                reading.freshness());
        // A plain or just-fed tank needs no state line; ageing, clot and living vessels do.
        boolean untouched = state == ReservoirReadout.State.FRESH && reading.freshness() >= 1.0D;
        if (state != ReservoirReadout.State.EMPTY && !untouched)
            lines.add(Component.translatable("message.hemomancy.living_blood.state." + state.name().toLowerCase(Locale.ROOT)));
        if (reading.link().stalled())
            lines.add(Component.translatable("screen.hemomancy.blood_volume_bar_widget.stalled." + reading.link().key())
                    .withStyle(ChatFormatting.GOLD));
        else if (reading.link() == ReservoirReadout.Link.DRAWING)
            lines.add(Component.translatable("screen.hemomancy.blood_volume_bar_widget.drawing").withStyle(ChatFormatting.GRAY));
        else if (reading.link() == ReservoirReadout.Link.KIN_DRAWING)
            lines.add(Component.translatable("screen.hemomancy.blood_volume_bar_widget.drawing.kin").withStyle(ChatFormatting.GRAY));
        gfx.renderTooltip(font, lines, Optional.empty(), mouseX, mouseY);
    }
}
