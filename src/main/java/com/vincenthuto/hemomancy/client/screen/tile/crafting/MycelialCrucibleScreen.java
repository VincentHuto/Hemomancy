package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.MycelialCrucibleMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.MycelialCrucibleBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Random;

/**
 * GUI for the Mycelial Crucible. Visual palette shifts from the Incubator's
 * deep crimson to a fungal amber-green, reflecting the block's nature as part
 * of the Fungal Entity's biology rather than the blood-craft tradition.
 *
 * <p>Two distinct progress displays:
 * <ul>
 *   <li><b>Craft ring</b> — shows Phase-1 implantation countdown (same style as Incubator).</li>
 *   <li><b>Maturation bar</b> — persistent horizontal bar above the inventory that fills
 *       as the immature scar is fed enzymes across sessions.</li>
 * </ul>
 */
public class MycelialCrucibleScreen extends AbstractContainerScreen<MycelialCrucibleMenu> {

    // ── Layout constants ──────────────────────────────────────────────────────
    private static final int CRAFT_AREA_HEIGHT = 116;

    // ── Colour palette (amber / orange — matching SporeImplantScreen) ─────────
    private static final int BG_BASE          = 0xFF1A0D04;  // dark amber base
    private static final int BORDER_OUTER     = 0xFFAA2200;  // red-orange outer border
    private static final int BORDER_INNER     = 0xFF3A1208;  // darker inner border
    private static final int SLOT_BG          = 0xFF2A1508;
    private static final int SLOT_BORDER_DARK = 0xFF140A02;
    private static final int SLOT_BORDER_LITE = 0xFF4A2A10;

    private static final int VEIN_COUNT       = 18;

    final MycelialCrucibleBlockEntity te;
    private float[][] veinParams;
    private float animTime = 0f;

    // Blood bar hover bounds
    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;

    public MycelialCrucibleScreen(MycelialCrucibleMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.te          = menu.getTe();
        this.imageWidth  = 176;
        this.imageHeight = 206;
        this.inventoryLabelY = CRAFT_AREA_HEIGHT + 2;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
        Random rand = new Random(99L);
        veinParams = new float[VEIN_COUNT][9];
        for (int i = 0; i < VEIN_COUNT; i++) {
            veinParams[i][0] = rand.nextFloat();
            veinParams[i][1] = rand.nextFloat();
            veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
            veinParams[i][3] = 0.3f + rand.nextFloat() * 0.6f;
            veinParams[i][4] = 6f + rand.nextFloat() * 14f;
            veinParams[i][5] = 0.04f + rand.nextFloat() * 0.07f;
            veinParams[i][6] = 50 + rand.nextInt(100);
            veinParams[i][7] = 1 + rand.nextInt(2);
            veinParams[i][8] = rand.nextFloat();
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partial) {
        this.renderBackground(gfx, mouseX, mouseY, partial);
        super.render(gfx, mouseX, mouseY, partial);
        this.renderTooltip(gfx, mouseX, mouseY);
        BloodVolumeBarWidget.renderTooltip(gfx, font, bloodBarBounds,
                te.getBloodVolume(), te.getMaxBloodVolume(), mouseX, mouseY);
        if (BloodVolumeBarWidget.Bounds.EMPTY != null) return;

        // Blood bar tooltip
        if (bloodBarBounds.contains(mouseX, mouseY)) {
            double vol    = te.getBloodVolume();
            double maxVol = te.getMaxBloodVolume();
            gfx.renderTooltip(font, List.of(
                    Component.literal(String.format("§6Blood: §e%.0f §6/ §e%.0f", vol, maxVol))
            ), java.util.Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partial, int mouseX, int mouseY) {
        animTime += 0.016f;
        int gx = leftPos, gy = topPos, gw = imageWidth, gh = imageHeight;

        // Upper craft area
        renderFungalBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
        drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

        // Lower inventory area
        int invTop = gy + CRAFT_AREA_HEIGHT;
        int invH   = gh - CRAFT_AREA_HEIGHT;
        renderInventoryBackground(gfx, gx, invTop, gw, invH);

        // Separator
        gfx.fill(gx, invTop, gx + gw, invTop + 1, BORDER_OUTER);
        gfx.fill(gx, invTop + 1, gx + gw, invTop + 2, BORDER_INNER);

        // Slot decorations
        for (Slot slot : menu.slots) {
            drawSlotBg(gfx, gx + slot.x, gy + slot.y, slot.index);
        }

        // Progress ring (craft countdown)
        renderProgressRing(gfx, gx, gy);

        // Maturation bar
        renderMaturationBar(gfx, gx, gy);

        // Blood bar
        renderBloodBar(gfx, gx, gy);
    }

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        gfx.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0xFF886633, false);
        int mode = menu.getMode();
        if (mode == 1) {
            String txt = "Implanting...";
            gfx.drawString(font, txt, imageWidth - font.width(txt) - 4,
                    CRAFT_AREA_HEIGHT - 10, 0xFFBB8822, false);
        } else if (mode == 2) {
            String txt = "Feeding...";
            gfx.drawString(font, txt, imageWidth - font.width(txt) - 4,
                    CRAFT_AREA_HEIGHT - 10, 0xFFCC7722, false);
        }
    }

    // ── Fungal vein background ────────────────────────────────────────────────

    private void renderFungalBackground(GuiGraphics gfx, int x, int y, int w, int h) {
        gfx.enableScissor(x, y, x + w, y + h);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.fill(x, y, x + w, y + h, BG_BASE);

        // Radial amber glow in centre
        int cx = x + w / 2, cy = y + h / 2;
        int glowR = Math.max(w, h) / 2;
        for (int ring = glowR; ring > 0; ring -= 4) {
            float t = (float) ring / glowR;
            int alpha = (int) (30 * (1f - t));
            int red   = (int) (60 * (1f - t));
            int green = (int) (25 * (1f - t));
            int blue  = (int) (5  * (1f - t));
            gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring,
                    (alpha << 24) | (red << 16) | (green << 8) | blue);
        }

        // Mycelial tendrils
        if (veinParams != null) {
            for (int i = 0; i < VEIN_COUNT; i++) {
                drawFungalTendril(gfx, i, animTime, x, y, w, h);
            }
        }

        // Warm amber spore speckles
        Random rand = new Random(99876L);
        for (int s = 0; s < 60; s++) {
            int sx = x + rand.nextInt(w), sy = y + rand.nextInt(h);
            int sa = 8 + rand.nextInt(20);
            int sr = 15 + rand.nextInt(25);
            int sg = 8  + rand.nextInt(15);
            gfx.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
        }

        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private void drawFungalTendril(GuiGraphics gfx, int idx, float time,
            int gx, int gy, int gw, int gh) {
        float[] p = veinParams[idx];
        float sx = gx + p[0] * gw, sy = gy + p[1] * gh;
        float angle = p[2] + 0.12f * Mth.sin(time * p[3] * 0.3f + idx);
        float cosA = Mth.cos(angle), sinA = Mth.sin(angle);
        int length = (int) p[6], thick = (int) p[7];
        float amp = p[4], freq = p[5], bright = p[8];
        float timeOff = time * p[3] * 1.8f;

        // Amber-red-yellow tendril palette (matching SporeImplantScreen)
        int baseR = (int) (50 + 80 * bright);
        int baseG = (int) (20 + 40 * bright);
        int baseB = (int) (2  +  8 * bright);

        for (int step = 0; step < length; step++) {
            float sq = amp * Mth.sin(freq * step + timeOff)
                     + amp * 0.3f * Mth.sin(freq * 2.5f * step + timeOff * 1.3f);
            float px = sx + step * cosA * 1.5f - sq * sinA;
            float py = sy + step * sinA * 1.5f + sq * cosA;
            int ix = (int) px, iy = (int) py;
            if (ix + thick < gx || ix >= gx + gw || iy + thick < gy || iy >= gy + gh) continue;

            float fade = 1f;
            if (step < 8) fade = step / 8f;
            else if (step > length - 8) fade = (length - step) / 8f;
            float pulse = 0.65f + 0.35f * Mth.sin(time * 1.4f + idx * 0.6f + step * 0.025f);

            int a = (int) Mth.clamp(fade * pulse * 160, 15, 180);
            int r = (int) Mth.clamp(baseR * pulse, 0, 255);
            int g = (int) Mth.clamp(baseG * pulse, 0, 255);
            int b = (int) Mth.clamp(baseB * pulse, 0, 255);
            gfx.fill(ix, iy, ix + thick, iy + thick, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

    // ── Inventory background ──────────────────────────────────────────────────

    private void renderInventoryBackground(GuiGraphics gfx, int x, int y, int w, int h) {
        for (int row = 0; row < h; row++) {
            float t = (float) row / h;
            // Warm dark amber gradient (matching SporeImplantScreen)
            int r = (int) (30 + 20 * t);
            int g = (int) (16 + 12 * t);
            int b = (int) (6  +  6 * t);
            gfx.fill(x, y + row, x + w, y + row + 1, (0xFF << 24) | (r << 16) | (g << 8) | b);
        }
    }

    // ── Slot decorations ──────────────────────────────────────────────────────

    private void drawSlotBg(GuiGraphics gfx, int sx, int sy, int slotIndex) {
        gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
        gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
        gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LITE);
        gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LITE);

        if (slotIndex == MycelialCrucibleMenu.CENTER_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x208B5500);
        if (slotIndex == MycelialCrucibleMenu.OUTPUT_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x2033CC33);
        if (slotIndex == MycelialCrucibleMenu.BLOOD_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA5500);
        if (slotIndex == MycelialCrucibleMenu.FLASK_OUT_SLOT)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA5500);
        // Enzyme slots — subtle amber glow
        if (slotIndex >= MycelialCrucibleMenu.ENZYME_SLOT_1
                && slotIndex <= MycelialCrucibleMenu.ENZYME_SLOT_4)
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20885522);
    }

    // ── Craft progress ring ───────────────────────────────────────────────────

    private void renderProgressRing(GuiGraphics gfx, int gx, int gy) {
        int cx = gx + 88, cy = gy + 48;
        int outerR = 24, innerR = 19;
        int segs   = 64;

        double progress = menu.isCrafting() ? menu.getScaledCraftProgress() / 24.0 : 0;
        int filled = (int) (segs * progress);

        for (int i = 0; i < segs; i++) {
            double a1 = -Math.PI / 2 + (2 * Math.PI / segs) * i;
            double a2 = -Math.PI / 2 + (2 * Math.PI / segs) * (i + 1);
            for (int r = innerR; r <= outerR; r++) {
                for (double a = a1; a < a2; a += 0.04) {
                    int px = cx + (int) (r * Math.cos(a));
                    int py = cy + (int) (r * Math.sin(a));
                    int color;
                    if (i < filled) {
                        float pulse = 0.7f + 0.3f * Mth.sin(animTime * 3f + i * 0.1f);
                        int green = (int) (180 * pulse), amber = (int) (90 * pulse);
                        color = (0xDD << 24) | (amber << 16) | (green << 8) | 0x10;
                    } else {
                        color = 0x20101A08;
                    }
                    gfx.fill(px, py, px + 1, py + 1, color);
                }
            }
        }

        // Leading-edge glow
        if (filled > 0 && filled < segs) {
            double la = -Math.PI / 2 + (2 * Math.PI / segs) * filled;
            int lx = cx + (int) ((innerR + outerR) / 2.0 * Math.cos(la));
            int ly = cy + (int) ((innerR + outerR) / 2.0 * Math.sin(la));
            float pulse = 0.5f + 0.5f * Mth.sin(animTime * 5f);
            int ga = (int) (120 * pulse);
            gfx.fill(lx - 1, ly - 1, lx + 2, ly + 2, (ga << 24) | (0x60 << 16) | (0xFF << 8) | 0x30);
        }
    }

    // ── Maturation progress bar ───────────────────────────────────────────────

    private void renderMaturationBar(GuiGraphics gfx, int gx, int gy) {
        int barX = gx + 44;
        int barY = gy + CRAFT_AREA_HEIGHT - 14;
        int barW = 88;
        int barH = 6;

        int progress   = menu.getMatureProgress();
        int threshold  = Math.max(1, menu.getMatureThreshold());
        float fraction = Mth.clamp((float) progress / threshold, 0, 1);
        int filled     = (int) (barW * fraction);

        // Background track
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, BORDER_OUTER);
        gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF0B130A);

        // Fill
        if (filled > 0) {
            for (int col = 0; col < filled; col++) {
                float t = (float) col / barW;
                float pulse = 0.75f + 0.25f * Mth.sin(animTime * 2f + col * 0.05f);
                int r = (int) (30 * pulse);
                int g = (int) (Mth.clamp((80 + 80 * t) * pulse, 0, 255));
                int b = (int) (10 * pulse);
                gfx.fill(barX + col, barY, barX + col + 1, barY + barH,
                        (0xFF << 24) | (r << 16) | (g << 8) | b);
            }
            // Bright leading pixel
            if (filled < barW) {
                float pulse = 0.5f + 0.5f * Mth.sin(animTime * 4f);
                int ga = (int) (180 * pulse);
                gfx.fill(barX + filled, barY, barX + filled + 1, barY + barH,
                        (ga << 24) | (0x20 << 16) | (0xEE << 8) | 0x20);
            }
        }

        // Label
        String label = "Maturation: " + progress + " / " + threshold;
        int textColor = fraction >= 1f ? 0xFF44FF44 : 0xFF99CC55;
        gfx.drawString(font, label,
                barX + barW / 2 - font.width(label) / 2,
                barY - 9, textColor, false);
    }

    // ── Blood volume bar ──────────────────────────────────────────────────────

    private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
        int barW = 8, barH = 52;
        int barX = gx + 8 + (16 - barW) / 2;
        int barY = gy + 74 - barH - 4;
        if (BloodVolumeBarWidget.Bounds.EMPTY != null) {
            bloodBarBounds = BloodVolumeBarWidget.render(gfx, barX, barY, barW, barH,
                    te.getBloodVolume(), te.getMaxBloodVolume(), animTime, BORDER_OUTER, BORDER_INNER);
            return;
        }

        bloodBarBounds = new BloodVolumeBarWidget.Bounds(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2);

        double vol    = te.getBloodVolume();
        double maxVol = te.getMaxBloodVolume();
        double ratio  = maxVol > 0 ? Mth.clamp(vol / maxVol, 0, 1) : 0;

        // Frame
        gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, BORDER_OUTER);
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, BORDER_INNER);
        gfx.fill(barX, barY, barX + barW, barY + barH, 0xFF040902);

        int fillH = (int) (barH * ratio);
        if (fillH > 0) {
            int fillTop = barY + barH - fillH;
            for (int row = 0; row < fillH; row++) {
                float rowT = (float) row / fillH;
                float pulse = 0.75f + 0.25f * Mth.sin(animTime * 2.5f + row * 0.08f);
                int r = (int) Mth.clamp((60 + 50 * rowT) * pulse, 0, 255);
                int g = (int) Mth.clamp((30 + 80 * rowT) * pulse, 0, 255);
                int b = (int) Mth.clamp((5 + 8 * rowT) * pulse, 0, 255);
                gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1,
                        (0xEE << 24) | (r << 16) | (g << 8) | b);
            }
            // Meniscus
            float mp = 0.6f + 0.4f * Mth.sin(animTime * 3f);
            int ma = (int) (200 * mp);
            gfx.fill(barX, fillTop, barX + barW, fillTop + 1,
                    (ma << 24) | (0x40 << 16) | (0xCC << 8) | 0x20);
        }

        // Tick marks
        for (int tick = 1; tick <= 3; tick++) {
            int ty = barY + barH - barH * tick / 4;
            gfx.fill(barX + barW, ty, barX + barW + 1, ty + 1, 0x60AAFFAA);
        }
    }

    // ── Border ────────────────────────────────────────────────────────────────

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
        gfx.fill(x, y, x + w, y + 1, BORDER_OUTER);
        gfx.fill(x, y + h - 1, x + w, y + h, BORDER_OUTER);
        gfx.fill(x, y, x + 1, y + h, BORDER_OUTER);
        gfx.fill(x + w - 1, y, x + w, y + h, BORDER_OUTER);
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_INNER);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_INNER);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_INNER);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_INNER);
    }
}
