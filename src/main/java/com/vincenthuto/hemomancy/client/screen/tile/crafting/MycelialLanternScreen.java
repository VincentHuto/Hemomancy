package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialLanternMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Random;

public class MycelialLanternScreen extends AbstractContainerScreen<MycelialLanternMenu> {

    private static final int BG_BASE = 0xFF1A0D04;
    private static final int SLOT_BG = 0xFF2A1508;
    private static final int SLOT_BORDER_DARK = 0xFF140A02;
    private static final int SLOT_BORDER_LIGHT = 0xFF4A2A10;
    private static final int BORDER_RED = 0xFFAA2200;
    private static final int BORDER_YELLOW = 0xFFCC8800;
    private static final int BORDER_INNER = 0xFF3A1A08;
    private static final int AMBER = 0xFFDD8822;

    private static final int CRAFT_AREA_HEIGHT = 104;
    private static final int TENDRIL_COUNT = 20;
    private static final int SPORE_COUNT = 44;

    private float[][] tendrilParams;
    private float[][] sporeParams;
    private int[][] speckleParams;
    private float animTime = 0f;

    private int bloodBarX1;
    private int bloodBarY1;
    private int bloodBarX2;
    private int bloodBarY2;

    public MycelialLanternScreen(MycelialLanternMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 198;
        this.inventoryLabelY = CRAFT_AREA_HEIGHT + 2;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        Random rand = new Random(0xF12A7E1L);
        tendrilParams = new float[TENDRIL_COUNT][9];
        for (int i = 0; i < TENDRIL_COUNT; i++) {
            tendrilParams[i][0] = rand.nextFloat();
            tendrilParams[i][1] = rand.nextFloat();
            tendrilParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
            tendrilParams[i][3] = 0.18f + rand.nextFloat() * 0.55f;
            tendrilParams[i][4] = 6f + rand.nextFloat() * 16f;
            tendrilParams[i][5] = 0.04f + rand.nextFloat() * 0.1f;
            tendrilParams[i][6] = 42 + rand.nextInt(92);
            tendrilParams[i][7] = 1 + rand.nextInt(2);
            tendrilParams[i][8] = rand.nextFloat();
        }

        sporeParams = new float[SPORE_COUNT][5];
        for (int i = 0; i < SPORE_COUNT; i++) {
            sporeParams[i][0] = rand.nextFloat();
            sporeParams[i][1] = rand.nextFloat();
            sporeParams[i][2] = 0.25f + rand.nextFloat() * 0.7f;
            sporeParams[i][3] = rand.nextFloat() * 100f;
            sporeParams[i][4] = rand.nextFloat();
        }

        Random speckRand = new Random(0x51EADL);
        speckleParams = new int[72][5];
        for (int s = 0; s < speckleParams.length; s++) {
            speckleParams[s][0] = speckRand.nextInt(this.imageWidth);
            speckleParams[s][1] = speckRand.nextInt(CRAFT_AREA_HEIGHT);
            speckleParams[s][2] = 30 + speckRand.nextInt(48);
            speckleParams[s][3] = 10 + speckRand.nextInt(26);
            speckleParams[s][4] = 14 + speckRand.nextInt(34);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(gfx, mouseX, mouseY, partialTick);
        super.render(gfx, mouseX, mouseY, partialTick);
        this.renderTooltip(gfx, mouseX, mouseY);
        if (mouseX >= bloodBarX1 && mouseX < bloodBarX2 && mouseY >= bloodBarY1 && mouseY < bloodBarY2) {
            double vol = menu.getBloodVolume();
            double maxVol = menu.getMaxBloodVolume();
            gfx.renderTooltip(font, List.of(
                    Component.literal(String.format("\u00A74Blood: \u00A7c%.0f \u00A74/ \u00A7c%.0f", vol, maxVol))
            ), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int gx = leftPos;
        int gy = topPos;
        int gw = imageWidth;
        int gh = imageHeight;

        renderFungalBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
        drawGradientBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

        int invTop = gy + CRAFT_AREA_HEIGHT;
        renderAmberGradientBackground(gfx, gx, invTop, gw, gh - CRAFT_AREA_HEIGHT);
        gfx.fill(gx, invTop, gx + gw, invTop + 1, BORDER_RED);
        gfx.fill(gx, invTop + 1, gx + gw, invTop + 2, BORDER_INNER);

        for (int i = 0; i < menu.slots.size(); i++) {
            Slot slot = menu.slots.get(i);
            int sx = gx + slot.x;
            int sy = gy + slot.y;
            drawSlotBackground(gfx, sx, sy, i < MycelialLanternMenu.SLOT_COUNT ? slot.index : -1);
        }

        renderCultureGlow(gfx, gx, gy);
        renderProgress(gfx, gx, gy);
        renderBloodBar(gfx, gx, gy);
        drawGradientBorder(gfx, gx, gy, gw, gh);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(font, title, titleLabelX, 4, AMBER, false);
        gfx.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF886633, false);
    }

    private void renderProgress(GuiGraphics gfx, int gx, int gy) {
        int progress = menu.getScaledCraftProgress();
        int trackX = gx + 101;
        int trackY = gy + 54;
        int trackW = 27;
        int trackH = 7;

        gfx.fill(trackX, trackY + 2, trackX + trackW, trackY + 5, 0x70201006);
        gfx.fill(trackX + trackW, trackY, trackX + trackW + 1, trackY + trackH, 0x30201006);
        gfx.fill(trackX + trackW + 1, trackY + 1, trackX + trackW + 2, trackY + trackH - 1, 0x30201006);

        if (progress > 0) {
            for (int col = 0; col < progress; col++) {
                float pulse = 0.65f + 0.35f * Mth.sin(animTime * 3f + col * 0.22f);
                int r = (int) (210 * pulse);
                int g = (int) (110 * pulse);
                int b = (int) (18 * pulse);
                gfx.fill(trackX + 1 + col, trackY + 1, trackX + 2 + col, trackY + trackH - 1,
                        (0xCC << 24) | (r << 16) | (g << 8) | b);
            }
            int edgeX = trackX + Math.min(progress, 24);
            gfx.fill(edgeX, trackY, edgeX + 1, trackY + trackH, 0xDDFFB342);
        }
    }

    private void renderCultureGlow(GuiGraphics gfx, int gx, int gy) {
        int cx = gx + 88;
        int cy = gy + 52;
        float pulse = 0.45f + 0.55f * Mth.sin(animTime * 2.2f);
        for (int ring = 16; ring >= 9; ring--) {
            float t = (ring - 9) / 7f;
            int alpha = (int) (28 * pulse * (1f - t));
            int color = (alpha << 24) | (0xD0 << 16) | (0x72 << 8) | 0x12;
            gfx.fill(cx - ring, cy - ring, cx + ring, cy - ring + 1, color);
            gfx.fill(cx - ring, cy + ring - 1, cx + ring, cy + ring, color);
            gfx.fill(cx - ring, cy - ring, cx - ring + 1, cy + ring, color);
            gfx.fill(cx + ring - 1, cy - ring, cx + ring, cy + ring, color);
        }
    }

    private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
        int barW = 8;
        int barH = 54;
        int barX = gx + 12;
        int barY = gy + 26;

        bloodBarX1 = barX - 2;
        bloodBarY1 = barY - 2;
        bloodBarX2 = barX + barW + 2;
        bloodBarY2 = barY + barH + 2;

        double maxVol = menu.getMaxBloodVolume();
        double ratio = maxVol > 0 ? Mth.clamp(menu.getBloodVolume() / maxVol, 0, 1) : 0;

        gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, BORDER_RED);
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF220606);
        gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF060102);

        int fillH = (int) (barH * ratio);
        if (fillH > 0) {
            int fillTop = barY + barH - fillH;
            for (int row = 0; row < fillH; row++) {
                float rowT = (float) row / Math.max(fillH, 1);
                float pulse = 0.75f + 0.25f * Mth.sin(animTime * 2.5f + row * 0.08f);
                int r = (int) Mth.clamp((100 + 80 * rowT) * pulse, 0, 255);
                int g = (int) Mth.clamp((5 + 15 * rowT) * pulse, 0, 255);
                int b = (int) Mth.clamp((8 + 10 * rowT) * pulse, 0, 255);
                gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1,
                        (0xEE << 24) | (r << 16) | (g << 8) | b);
            }

            float meniscusPulse = 0.6f + 0.4f * Mth.sin(animTime * 3f);
            int mAlpha = (int) (200 * meniscusPulse);
            gfx.fill(barX, fillTop, barX + barW, fillTop + 1,
                    (mAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x18);

            for (int row = 0; row < fillH; row++) {
                float fade = 0.3f + 0.15f * Mth.sin(animTime * 1.5f + row * 0.15f);
                int hAlpha = (int) (80 * fade);
                gfx.fill(barX + 1, fillTop + row, barX + 2, fillTop + row + 1,
                        (hAlpha << 24) | (0xFF << 16) | (0x60 << 8) | 0x50);
            }

            Random bubbleRand = new Random(7777L);
            for (int bi = 0; bi < 3; bi++) {
                float speed = 0.4f + bubbleRand.nextFloat() * 0.6f;
                float phase = bubbleRand.nextFloat() * 100f;
                int bx = barX + 2 + bubbleRand.nextInt(Math.max(barW - 4, 1));
                float bubbleProgress = (animTime * speed + phase) % 1.0f;
                int by = fillTop + fillH - (int) (bubbleProgress * fillH);
                if (by >= fillTop && by < fillTop + fillH - 1) {
                    int alpha = (int) (60 * (1f - Math.abs(bubbleProgress - 0.5f) * 2f));
                    gfx.fill(bx, by, bx + 1, by + 1,
                            (alpha << 24) | (0xFF << 16) | (0x40 << 8) | 0x30);
                }
            }
        }

        for (int tick = 1; tick <= 3; tick++) {
            int tickY = barY + barH - (barH * tick / 4);
            gfx.fill(barX + barW, tickY, barX + barW + 1, tickY + 1, 0x60FFFFFF);
        }
    }

    private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
        gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
        gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
        gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
        gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

        if (slotIndex == MycelialLanternMenu.CULTURE_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x28CC7700);
        } else if (slotIndex == MycelialLanternMenu.BLOOD_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x24AA0000);
        } else if (slotIndex == MycelialLanternMenu.OUTPUT_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x22FFAA44);
        } else if (slotIndex == MycelialLanternMenu.EMPTY_CONTAINER_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x18DD8822);
        }
    }

    private void renderFungalBackground(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
        animTime += 0.016f;
        float time = animTime;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.fill(gx, gy, gx + gw, gy + gh, BG_BASE);

        int cx = gx + gw / 2;
        int cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 3) {
            float t = (float) ring / glowRadius;
            int alpha = (int) (32 * (1f - t));
            int r = (int) (62 * (1f - t));
            int g = (int) (28 * (1f - t));
            int b = (int) (6 * (1f - t));
            gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (r << 16) | (g << 8) | b);
        }

        if (tendrilParams != null) {
            for (int i = 0; i < TENDRIL_COUNT; i++) {
                drawFungalTendril(gfx, i, time, gx, gy, gw, gh);
            }
        }

        if (sporeParams != null) {
            drawFloatingSpores(gfx, time, gx, gy, gw, gh);
        }

        if (speckleParams != null) {
            for (int[] sp : speckleParams) {
                int sx = gx + sp[0];
                int sy = gy + sp[1];
                gfx.fill(sx, sy, sx + 1, sy + 1, (sp[4] << 24) | (sp[2] << 16) | (sp[3] << 8));
            }
        }

        RenderSystem.disableBlend();
    }

    private void drawFungalTendril(GuiGraphics gfx, int index, float time, int gx, int gy, int gw, int gh) {
        float[] p = tendrilParams[index];
        float startX = gx + p[0] * gw;
        float startY = gy + p[1] * gh;
        float angle = p[2] + 0.12f * Mth.sin(time * p[3] * 0.4f + index);
        float cosA = Mth.cos(angle);
        float sinA = Mth.sin(angle);
        float timeOffset = time * p[3] * 1.5f;
        int length = (int) p[6];
        int thickness = (int) p[7];
        float brightness = p[8];

        int baseRed = (int) (50 + 80 * brightness);
        int baseGreen = (int) (20 + 40 * brightness);
        int baseBlue = (int) (2 + 8 * brightness);

        for (int step = 0; step < length; step++) {
            float squiggle = p[4] * Mth.sin(p[5] * step + timeOffset);
            float microSquiggle = p[4] * 0.25f * Mth.sin(p[5] * 2.5f * step + timeOffset * 1.3f + index);
            float displacement = squiggle + microSquiggle;
            int ix = (int) (startX + step * cosA * 1.5f - displacement * sinA);
            int iy = (int) (startY + step * sinA * 1.5f + displacement * cosA);

            if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

            float tipFade = 1f;
            if (step < 8) tipFade = step / 8f;
            else if (step > length - 8) tipFade = (length - step) / 8f;

            float pulse = 0.65f + 0.35f * Mth.sin(time * 1.2f + index * 0.6f + step * 0.025f);
            int a = (int) Mth.clamp(tipFade * pulse * 160, 15, 180);
            int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
            int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
            int b = (int) Mth.clamp(baseBlue * pulse * 0.4f, 0, 255);
            gfx.fill(ix, iy, ix + thickness, iy + thickness, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private void drawFloatingSpores(GuiGraphics gfx, float time, int gx, int gy, int gw, int gh) {
        for (int i = 0; i < SPORE_COUNT; i++) {
            float[] p = sporeParams[i];
            float yOffset = ((time * p[2] * 8f + p[3]) % (gh + 10)) - 5;
            float xWobble = 3f * Mth.sin(time * p[2] * 2f + p[3]);

            int sx = gx + Math.floorMod((int) (p[0] * gw + xWobble), gw);
            int sy = gy + gh - (int) yOffset;
            if (sy < gy || sy >= gy + gh || sx < gx || sx >= gx + gw) continue;

            float pulse = 0.5f + 0.5f * Mth.sin(time * 3f + p[3]);
            int alpha = (int) (40 + 60 * pulse * p[4]);
            int r = (int) (120 + 80 * p[4]);
            int g = (int) (60 + 40 * p[4]);
            int b = (int) (5 + 10 * p[4]);
            gfx.fill(sx, sy, sx + 1, sy + 1, (alpha << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private void renderAmberGradientBackground(GuiGraphics gfx, int x, int y, int w, int h) {
        for (int row = 0; row < h; row++) {
            float t = (float) row / h;
            int r = (int) (30 + 20 * t);
            int g = (int) (16 + 12 * t);
            int b = (int) (6 + 6 * t);
            gfx.fill(x, y + row, x + w, y + row + 1, (0xFF << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private void drawGradientBorder(GuiGraphics gfx, int x, int y, int w, int h) {
        int redR = (BORDER_RED >> 16) & 0xFF;
        int redG = (BORDER_RED >> 8) & 0xFF;
        int redB = BORDER_RED & 0xFF;
        int yelR = (BORDER_YELLOW >> 16) & 0xFF;
        int yelG = (BORDER_YELLOW >> 8) & 0xFF;
        int yelB = BORDER_YELLOW & 0xFF;

        for (int col = 0; col < w; col++) {
            gfx.fill(x + col, y, x + col + 1, y + 1, BORDER_RED);
            gfx.fill(x + col, y + 1, x + col + 1, y + 2, darken(BORDER_RED, 0.7f));
            gfx.fill(x + col, y + h - 1, x + col + 1, y + h, BORDER_YELLOW);
            gfx.fill(x + col, y + h - 2, x + col + 1, y + h - 1, darken(BORDER_YELLOW, 0.7f));
        }

        for (int row = 0; row < h; row++) {
            float t = (float) row / Math.max(h - 1, 1);
            int r = (int) (redR + (yelR - redR) * t);
            int g = (int) (redG + (yelG - redG) * t);
            int b = (int) (redB + (yelB - redB) * t);
            int color = (0xFF << 24) | (r << 16) | (g << 8) | b;
            int innerColor = darken(color, 0.7f);

            gfx.fill(x, y + row, x + 1, y + row + 1, color);
            gfx.fill(x + 1, y + row, x + 2, y + row + 1, innerColor);
            gfx.fill(x + w - 1, y + row, x + w, y + row + 1, color);
            gfx.fill(x + w - 2, y + row, x + w - 1, y + row + 1, innerColor);
        }
    }

    private static int darken(int color, float factor) {
        int a = (color >> 24) & 0xFF;
        int r = (int) (((color >> 16) & 0xFF) * factor);
        int g = (int) (((color >> 8) & 0xFF) * factor);
        int b = (int) ((color & 0xFF) * factor);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
