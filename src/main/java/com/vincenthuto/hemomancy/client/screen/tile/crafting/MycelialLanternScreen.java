package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
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

    private static final int CRAFT_AREA_HEIGHT = 108;
    private static final int TENDRIL_COUNT = 14;
    private static final int SPORE_COUNT = 28;

    private float[][] tendrilParams;
    private float[][] sporeParams;
    private int[][] speckleParams;
    private float animTime = 0f;

    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;

    public MycelialLanternScreen(MycelialLanternMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 176;
        this.imageHeight = 198;
        this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
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
        speckleParams = new int[48][5];
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
        BloodVolumeBarWidget.renderTooltip(gfx, font, bloodBarBounds,
                menu.getBloodVolume(), menu.getMaxBloodVolume(), mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTick, int mouseX, int mouseY) {
        int gx = leftPos;
        int gy = topPos;
        int gw = imageWidth;
        int gh = imageHeight;

        renderFungalBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
        drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

        Slot firstInventorySlot = this.menu.slots.get(MycelialLanternMenu.SLOT_COUNT);
        InventoryPanelTextures.blit(gfx, InventoryPanelTextures.FUNGAL,
                gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

        for (int i = 0; i < menu.slots.size(); i++) {
            if (i >= MycelialLanternMenu.SLOT_COUNT) {
                continue;
            }
            Slot slot = menu.slots.get(i);
            int sx = gx + slot.x;
            int sy = gy + slot.y;
            drawSlotBackground(gfx, sx, sy, slot.index);
        }

        renderProgress(gfx, gx, gy);
        renderBloodBar(gfx, gx, gy);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(font, title, titleLabelX, 4, AMBER, false);
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

    private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
        int barW = 8;
        int barH = 54;
        int barX = gx + 12;
        int barY = gy + 26;
        bloodBarBounds = BloodVolumeBarWidget.render(gfx, barX, barY, barW, barH,
                menu.getBloodVolume(), menu.getMaxBloodVolume(), animTime, BORDER_RED, 0xFF220606);
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

        gfx.enableScissor(gx, gy, gx + gw, gy + gh);
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
        gfx.disableScissor();
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

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
        gfx.fill(x, y, x + w, y + 1, BORDER_RED);
        gfx.fill(x, y + h - 1, x + w, y + h, BORDER_YELLOW);
        gfx.fill(x, y, x + 1, y + h, BORDER_RED);
        gfx.fill(x + w - 1, y, x + w, y + h, BORDER_YELLOW);
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
    }
}
