package com.vincenthuto.hemomancy.client.screen.tile.crafting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.util.InventoryPanelTextures;
import com.vincenthuto.hemomancy.client.screen.widget.BloodVolumeBarWidget;
import com.vincenthuto.hemomancy.common.menu.tile.crafting.GhastlyAlembicMenu;
import com.vincenthuto.hemomancy.common.tile.crafting.GhastlyAlembicBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.List;
import java.util.Random;

/**
 * Ghastly Alembic screen — fully programmatic, no texture files.
 * Renders a blood distillery UI with vein background, blood tank,
 * progress arrow, heat indicator, and item slots.
 */
public class GhastlyAlembicScreen extends AbstractContainerScreen<GhastlyAlembicMenu> {

    // ── Colors (matching the Hemomancy style) ──
    private static final int SLOT_BG = 0xFF1A0808;
    private static final int SLOT_BORDER_DARK = 0xFF0D0303;
    private static final int SLOT_BORDER_LIGHT = 0xFF3A1212;
    private static final int BORDER_OUTER = 0xFF330808;
    private static final int BORDER_INNER = 0xFF220606;

    private static final int CRAFT_AREA_HEIGHT = 86;
    private static final int VEIN_COUNT = 16;

    final GhastlyAlembicBlockEntity te;
    private float[][] veinParams;

    // Blood bar screen-space bounds for hover detection
    private BloodVolumeBarWidget.Bounds bloodBarBounds = BloodVolumeBarWidget.Bounds.EMPTY;
    // ───── Heat indicator ─────
    private float animTime = 0f;

    public GhastlyAlembicScreen(GhastlyAlembicMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.te = menu.getTe();
        this.imageWidth = 176;
        this.imageHeight = 176;
        this.inventoryLabelY = CRAFT_AREA_HEIGHT + 7;
    }

    // ───── Main render ─────

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // Seed vein parameters for the animated background
        Random rand = new Random(31415L);
        veinParams = new float[VEIN_COUNT][9];
        for (int i = 0; i < VEIN_COUNT; i++) {
            veinParams[i][0] = rand.nextFloat();
            veinParams[i][1] = rand.nextFloat();
            veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
            veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
            veinParams[i][4] = 8f + rand.nextFloat() * 14f;
            veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
            veinParams[i][6] = 50 + rand.nextInt(100);
            veinParams[i][7] = 1 + rand.nextInt(2);
            veinParams[i][8] = rand.nextFloat();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(graphics, mouseX, mouseY, partialTicks);
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
        if (this.hoveredSlot != null
                && this.hoveredSlot.index == GhastlyAlembicMenu.FLASK_OUTPUT_SLOT
                && !this.hoveredSlot.hasItem()) {
            graphics.renderTooltip(font, List.of(
                    Component.literal("\u00A74Vial Output"),
                    Component.literal("\u00A77Place a blood gourd here to fill it from the alembic.")
            ), java.util.Optional.empty(), mouseX, mouseY);
        }

        BloodVolumeBarWidget.renderTooltip(graphics, font, bloodBarBounds,
                te.getBloodVolume(), te.getMaxBloodVolume(), mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics gfx, float partialTicks, int mouseX, int mouseY) {
        int gx = this.leftPos;
        int gy = this.topPos;
        int gw = this.imageWidth;
        int gh = this.imageHeight;

        // ── Venous background for upper crafting area ──
        renderVeinBackground(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);
        drawBorder(gfx, gx, gy, gw, CRAFT_AREA_HEIGHT);

        // ── Red gradient panel behind inventory section ──
        Slot firstInventorySlot = this.menu.slots.get(GhastlyAlembicMenu.SLOT_COUNT);
        InventoryPanelTextures.blit(gfx, InventoryPanelTextures.BLOODY,
                gx + firstInventorySlot.x - 5, gy + firstInventorySlot.y - 6);

        // ── Draw slot backgrounds ──
        for (int i = 0; i < GhastlyAlembicMenu.SLOT_COUNT; i++) {
            Slot slot = this.menu.slots.get(i);
            int sx = gx + slot.x;
            int sy = gy + slot.y;
            drawSlotBackground(gfx, sx, sy, slot.index);
        }
		animTime += 0.016f; // ~60 FPS approximation

        // ── Heat indicator (flame area below input slot) ──
        renderHeatIndicator(gfx, gx, gy);

        // ── Progress arrow from input → output ──
        renderProgressArrow(gfx, gx, gy);

        // ── Blood volume bar ──
        renderBloodBar(gfx, gx, gy);
    }

    // ───── Red gradient inventory background ─────

    @Override
    protected void renderLabels(GuiGraphics gfx, int mouseX, int mouseY) {
        // Title centered
        gfx.drawString(font, this.title, this.titleLabelX, 4, 0xFFAA2222, false);
        // Inventory label

        // Heat status text below heat indicator
        if (this.menu.isHeated()) {
            gfx.drawString(font, Component.literal("Distilling"), 36, 56, 0xFFCC4400, false);
        } else {
            gfx.drawString(font, Component.literal("No Heat"), 40, 56, 0xFF554444, false);
        }
    }

    // ───── Slot backgrounds ─────

    private void drawSlotBackground(GuiGraphics gfx, int sx, int sy, int slotIndex) {
        // Outer border (dark edge)
        gfx.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BORDER_DARK);
        // Inner fill
        gfx.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
        // Bottom/right highlight
        gfx.fill(sx + 16, sy, sx + 17, sy + 17, SLOT_BORDER_LIGHT);
        gfx.fill(sx, sy + 16, sx + 17, sy + 17, SLOT_BORDER_LIGHT);

        // Special tint for result slot (reddish)
        if (slotIndex == GhastlyAlembicMenu.RESULT_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
        }
        // Special tint for flask slot (dark red)
        if (slotIndex == GhastlyAlembicMenu.FLASK_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20AA0000);
        }
        // Special tint for flask output slot
        if (slotIndex == GhastlyAlembicMenu.FLASK_OUTPUT_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x20FF4444);
        }
        // Tint for input slot (slight highlight)
        if (slotIndex == GhastlyAlembicMenu.INGREDIENT_SLOT) {
            gfx.fill(sx, sy, sx + 16, sy + 16, 0x15FFAA88);
        }
    }

    private void renderHeatIndicator(GuiGraphics gfx, int gx, int gy) {
        // Flame area centered below the input slot
        int flameX = gx + 44;
        int flameY = gy + 50;
        int flameW = 16;
        int flameH = 14;

      //  animTime += 0.016f; // ~60 FPS approximation

        float time = animTime;

        if (this.menu.isHeated()) {
            // Animated flame — draw pixel fire from bottom up
            for (int row = 0; row < flameH; row++) {
                float rowT = (float) row / flameH; // 0 at top, 1 at bottom
                float intensity = rowT; // brighter at bottom
                float flicker = 0.7f + 0.3f * Mth.sin(time * 8f + row * 0.5f);

                for (int col = 0; col < flameW; col++) {
                    // Taper the flame: narrower at top
                    float center = flameW / 2f;
                    float dist = Math.abs(col - center) / center;
                    float taper = 1f - dist * (1f - rowT * 0.6f);
                    if (taper < 0.2f) continue;

                    float wave = Mth.sin(time * 6f + col * 0.7f + row * 0.3f) * 0.15f;
                    float alpha = Mth.clamp(intensity * taper * flicker + wave, 0, 1);

                    // Color: orange at bottom → red at top
                    int r = (int) (255 * alpha);
                    int g = (int) (Mth.clamp(180 * rowT * alpha, 0, 255));
                    int b = (int) (20 * alpha);
                    int a = (int) (220 * alpha);
                    if (a < 10) continue;

                    int px = flameX + col;
                    int py = flameY + flameH - 1 - row;
                    gfx.fill(px, py, px + 1, py - 1, (a << 24) | (r << 16) | (g << 8) | b);
                }
            }
        } else {
            // Cold — dim ash dots
            Random r = new Random(999L);
            for (int i = 0; i < 8; i++) {
                int dx = r.nextInt(flameW);
                int dy = r.nextInt(flameH / 2) + flameH / 2;
                gfx.fill(flameX + dx, flameY + dy, flameX + dx + 1, flameY + dy + 1, 0x30666666);
            }
        }
    }

    // ───── Progress arrow ─────

    private void renderProgressArrow(GuiGraphics gfx, int gx, int gy) {
        // Arrow from input slot (44+16=60) to result slot (134) at y=32+4 = center
        int arrowX = gx + 68;
        int arrowY = gy + 36;
        int arrowFullW = 58;
        int arrowH = 8;

        float time = animTime;
        double progress = this.menu.getBurnProgress() / 24.0;
        int filledW = (int) (arrowFullW * progress);

        // Background track (dark)
        gfx.fill(arrowX, arrowY, arrowX + arrowFullW, arrowY + arrowH, 0x40200808);
        // Inner track line
        gfx.fill(arrowX, arrowY + 3, arrowX + arrowFullW, arrowY + 5, 0x30400808);

        // Arrow head outline (always visible) — points RIGHT (narrows to tip)
        int headBaseX = arrowX + arrowFullW;  // where the shaft ends
        int midY = arrowY + arrowH / 2;
        int headLen = 5;
        for (int i = 0; i < headLen; i++) {
            int spread = headLen - 1 - i; // wide at left, narrow at right (tip)
            gfx.fill(headBaseX + i, midY - spread, headBaseX + i + 1, midY + spread + 1, 0x30600808);
        }

        if (filledW > 0) {
            // Filled portion — pulsing crimson
            for (int col = 0; col < filledW; col++) {
                float pulse = 0.7f + 0.3f * Mth.sin(time * 4f + col * 0.15f);
                int r = (int) (180 * pulse);
                int g = (int) (20 * pulse);
                int b = (int) (15 * pulse);
                int a = (int) (200 * pulse);
                int color = (a << 24) | (r << 16) | (g << 8) | b;
                gfx.fill(arrowX + col, arrowY + 1, arrowX + col + 1, arrowY + arrowH - 1, color);
            }

            // Bright leading edge
            float edgePulse = 0.5f + 0.5f * Mth.sin(time * 6f);
            int edgeAlpha = (int) (180 * edgePulse);
            int edgeColor = (edgeAlpha << 24) | (0xFF << 16) | (0x40 << 8) | 0x20;
            gfx.fill(arrowX + filledW - 1, arrowY, arrowX + filledW, arrowY + arrowH, edgeColor);

            // Filled arrow head — points RIGHT
            double headProgress = Mth.clamp((progress - 0.9) / 0.1, 0, 1);
            if (headProgress > 0) {
                int headAlpha = (int) (200 * headProgress);
                for (int i = 0; i < headLen; i++) {
                    int spread = headLen - 1 - i;
                    int hColor = (headAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x10;
                    gfx.fill(headBaseX + i, midY - spread, headBaseX + i + 1, midY + spread + 1, hColor);
                }
            }
        }
    }

    // ───── Blood volume bar ─────

    private void renderBloodBar(GuiGraphics gfx, int gx, int gy) {
        // Vertical vial on the right side of crafting area (above the flask slot)
        int barW = 10;
        int barH = 38;
        int barX = gx + 158;
        int barY = gy + 16;
        bloodBarBounds = BloodVolumeBarWidget.render(gfx, barX, barY, barW, barH,
                te.getBloodVolume(), te.getMaxBloodVolume(), animTime, BORDER_OUTER, BORDER_INNER);
    }

    // ───── Programmatic border ─────

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

    // ───── Procedural animated vein background ─────

    private void renderVeinBackground(GuiGraphics graphics, int gx, int gy, int gw, int gh) {
        graphics.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        // Layer 1: solid near-black base
        graphics.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

        // Layer 2: subtle dark-red radial glow in the center
        int cx = gx + gw / 2;
        int cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 4) {
            float t = (float) ring / glowRadius;
            int alpha = (int) (30 * (1f - t));
            int red = (int) (35 * (1f - t));
            int color = (alpha << 24) | (red << 16);
            graphics.fill(cx - ring, cy - ring, cx + ring, cy + ring, color);
        }

        // Layer 3: animated vein tendrils
     //   animTime += 0.016f; // ~60 FPS approximation
        float time = animTime;
        if (veinParams != null) {
            for (int i = 0; i < VEIN_COUNT; i++) {
                drawVeinTendril(graphics, i, time, gx, gy, gw, gh);
            }
        }

        // Layer 4: noise speckles
        Random speckRand = new Random(54321L);
        for (int s = 0; s < 60; s++) {
            int sx = gx + speckRand.nextInt(gw);
            int sy = gy + speckRand.nextInt(gh);
            int sr = 10 + speckRand.nextInt(20);
            int sg = speckRand.nextInt(6);
            int sa = 15 + speckRand.nextInt(20);
            graphics.fill(sx, sy, sx + 1, sy + 1, (sa << 24) | (sr << 16) | (sg << 8));
        }

        RenderSystem.disableBlend();
        graphics.disableScissor();
    }

    private void drawVeinTendril(GuiGraphics graphics, int index, float time,
                                 int gx, int gy, int gw, int gh) {
        float[] p = veinParams[index];
        float startX = gx + p[0] * gw;
        float startY = gy + p[1] * gh;
        float baseAngle = p[2];
        float speed = p[3];
        float amplitude = p[4];
        float frequency = p[5];
        int length = (int) p[6];
        int thickness = (int) p[7];
        float brightness = p[8];

        float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);
        float cosA = Mth.cos(angleDrift);
        float sinA = Mth.sin(angleDrift);

        float timeOffset = time * speed * 2.0f;

        int baseRed = (int) (40 + 50 * brightness);
        int baseGreen = (int) (2 + 8 * brightness);
        int baseBlue = (int) (5 + 5 * brightness);

        for (int step = 0; step < length; step++) {
            float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
            float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
            float displacement = squiggle + microSquiggle;

            float px = startX + step * cosA * 1.5f - displacement * sinA;
            float py = startY + step * sinA * 1.5f + displacement * cosA;

            int ix = (int) px;
            int iy = (int) py;

            if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

            float tipFade = 1f;
            if (step < 10) tipFade = step / 10f;
            else if (step > length - 10) tipFade = (length - step) / 10f;

            float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

            int a = (int) (Mth.clamp(tipFade * pulse * 180, 20, 200));
            int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
            int g = (int) Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
            int b = (int) Mth.clamp(baseBlue * pulse * 0.3f, 0, 255);

            graphics.fill(ix, iy, ix + thickness, iy + thickness,
                    (a << 24) | (r << 16) | (g << 8) | b);
        }
    }
}
