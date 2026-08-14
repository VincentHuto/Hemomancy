package com.vincenthuto.hemomancy.client.screen.tile.functional;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.OpenSSCScreenPacket;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.OpenSSCScreenPacket.DrudgeEntry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;
import java.util.Random;

/**
 * Client-side GUI for the Semi-Sentient Construct.
 * <p>
 * Displays:
 * <ul>
 *   <li>The SSC's current blood reserve as a labelled bar.</li>
 *   <li>One row per bound Drudge, showing memory equipped, mode
 *       (Passive / Commanded / ROGUE), and a blood-charge bar.</li>
 * </ul>
 * Opens via {@link OpenSSCScreenPacket}
 * sent from the server when the player right-clicks the SSC without a held item.
 */
@OnlyIn(Dist.CLIENT)
public class SSCScreen extends Screen {

    // ── Layout ──────────────────────────────────────────────────────────────

    private static final int PANEL_W     = 260;
    private static final int HEADER_H    = 66;   // title + coords + vertical blood bar
    private static final int ROW_H       = 26;
    private static final int ROW_PADDING = 3;
    private static final int FOOTER_PAD  = 8;
    private static final int MIN_H       = HEADER_H + FOOTER_PAD * 2;
    private static final int VEIN_COUNT  = 16;

    // ── Colors ───────────────────────────────────────────────────────────────

    private static final int BG           = 0xE0070102;
    private static final int BORDER_OUT   = 0xFF5A0808;
    private static final int BORDER_IN    = 0xFF2A0404;
    private static final int TEXT_TITLE   = 0xFFCC3333;
    private static final int TEXT_BRIGHT  = 0xFFDDAAAA;
    private static final int TEXT_DIM     = 0xFF886666;
    private static final int TEXT_ROGUE   = 0xFFFF4444;
    private static final int TEXT_PASSIVE = 0xFF77CC77;
    private static final int TEXT_CMD     = 0xFFCCAA55;
    private static final int BAR_BG       = 0xFF0D0303;
    private static final int BAR_BORDER   = 0xFF331010;
    private static final int CHARGE_FILL  = 0xFF882020;
    private static final int ROW_BG       = 0xCC100404;
    private static final int ROW_ROGUE_BG = 0xCC250606;
    private static final int ROW_BORDER   = 0xFF3A0808;
    private static final int SEP          = 0xFF220606;

    // ── State ────────────────────────────────────────────────────────────────

    private final BlockPos pos;
    private final double sscBlood;
    private final double sscMaxBlood;
    private final List<DrudgeEntry> drudges;

    private int panelH;
    private int px, py;       // top-left of the panel in screen space

    private float animTime = 0f;
    private float[][] veinParams;

    // ── Static factory (called from packet handler on client thread) ──────────

    public static void open(BlockPos pos, double sscBlood, double sscMaxBlood, List<DrudgeEntry> drudges) {
        Minecraft mc = Minecraft.getInstance();
        mc.setScreen(new SSCScreen(pos, sscBlood, sscMaxBlood, drudges));
    }

    // ── Constructor ──────────────────────────────────────────────────────────

    public SSCScreen(BlockPos pos, double sscBlood, double sscMaxBlood, List<DrudgeEntry> drudges) {
        super(Component.translatable("screen.hemomancy.ssc_status"));
        this.pos = pos;
        this.sscBlood = sscBlood;
        this.sscMaxBlood = sscMaxBlood;
        this.drudges = drudges;
    }

    @Override
    protected void init() {
        super.init();

        int rows = drudges.size();
        panelH = Math.max(MIN_H, HEADER_H + rows * (ROW_H + ROW_PADDING) + FOOTER_PAD * 2);

        px = (this.width  - PANEL_W) / 2;
        py = (this.height - panelH)  / 2;

        // Seed vein animations
        Random rand = new Random(0xB100DC00L);
        veinParams = new float[VEIN_COUNT][9];
        for (int i = 0; i < VEIN_COUNT; i++) {
            veinParams[i][0] = rand.nextFloat();
            veinParams[i][1] = rand.nextFloat();
            veinParams[i][2] = (float) (rand.nextFloat() * Math.PI * 2);
            veinParams[i][3] = 0.3f + rand.nextFloat() * 0.6f;
            veinParams[i][4] = 6f  + rand.nextFloat() * 16f;
            veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
            veinParams[i][6] = 50 + rand.nextInt(100);
            veinParams[i][7] = 1 + rand.nextInt(2);
            veinParams[i][8] = rand.nextFloat();
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    // ── Render ───────────────────────────────────────────────────────────────

    @Override
    public void render(GuiGraphics gfx, int mx, int my, float partialTick) {
        // Paint a semi-transparent dark overlay ourselves (no blur shader).
        gfx.fill(0, 0, this.width, this.height, 0x88000000);
        renderPanel(gfx);
        super.render(gfx, mx, my, partialTick);
    }

    private void renderPanel(GuiGraphics gfx) {
        animTime += 0.016f;

        // ── Panel background + animated veins ────────────────────────────────
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.fill(px, py, px + PANEL_W, py + panelH, BG);
        drawVeins(gfx);
        drawBorder(gfx, px, py, PANEL_W, panelH);

        RenderSystem.disableBlend();

        // ── Title ─────────────────────────────────────────────────────────────
        int titleX = px + (PANEL_W - font.width(title)) / 2;
        gfx.drawString(font, title, titleX, py + 5, TEXT_TITLE, false);

        // Coordinate subtitle
        String coords = "(" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ() + ")";
        int cw = font.width(coords);
        gfx.drawString(font, coords, px + (PANEL_W - cw) / 2, py + 15, TEXT_DIM, false);

        // ── SSC Blood bar (vertical, left side of header) ────────────────────
        renderVerticalBloodBar(gfx);

        // Separator
        gfx.fill(px + 6, py + HEADER_H - 2, px + PANEL_W - 6, py + HEADER_H - 1, SEP);

        // ── Drudge rows ───────────────────────────────────────────────────────
        if (drudges.isEmpty()) {
            String none = "No constructs are bound to this station.";
            int nw = font.width(none);
            gfx.drawString(font, none, px + (PANEL_W - nw) / 2,
                    py + HEADER_H + FOOTER_PAD, TEXT_DIM, false);
        } else {
            int rowTop = py + HEADER_H + ROW_PADDING;
            for (int i = 0; i < drudges.size(); i++) {
                renderDrudgeRow(gfx, i, drudges.get(i), rowTop);
                rowTop += ROW_H + ROW_PADDING;
            }
        }
    }

    // ── Vertical blood bar (SSC reservoir, left side of header) ─────────────

    private void renderVerticalBloodBar(GuiGraphics gfx) {
        int barW = 10;
        int barH = 34;
        int barX = px + 14;
        int barY = py + 26;

        double ratio = sscMaxBlood > 0 ? Mth.clamp(sscBlood / sscMaxBlood, 0, 1) : 0;
        float time = animTime;

        // Double border
        gfx.fill(barX - 2, barY - 2, barX + barW + 2, barY + barH + 2, BAR_BORDER);
        gfx.fill(barX - 1, barY - 1, barX + barW + 1, barY + barH + 1, 0xFF1A0404);

        // Dark background
        gfx.fill(barX, barY, barX + barW, barY + barH, BAR_BG);

        // Fill from bottom up with vertical gradient
        int fillH = (int) (barH * ratio);
        if (fillH > 0) {
            int fillTop = barY + barH - fillH;
            for (int row = 0; row < fillH; row++) {
                float rowT  = (float) row / fillH;
                float pulse = 0.75f + 0.25f * Mth.sin(time * 2.5f + row * 0.08f);
                int r = (int) Mth.clamp((100 + 80 * rowT) * pulse, 0, 255);
                int g = (int) Mth.clamp((5  + 15 * rowT) * pulse, 0, 255);
                int b = (int) Mth.clamp((8  + 10 * rowT) * pulse, 0, 255);
                gfx.fill(barX, fillTop + row, barX + barW, fillTop + row + 1,
                         (0xEE << 24) | (r << 16) | (g << 8) | b);
            }

            // Meniscus highlight
            float mp = 0.6f + 0.4f * Mth.sin(time * 3f);
            int mAlpha = (int) (200 * mp);
            gfx.fill(barX, fillTop, barX + barW, fillTop + 1,
                     (mAlpha << 24) | (0xCC << 16) | (0x20 << 8) | 0x18);

            // Specular strip on the left edge
            for (int row = 0; row < fillH; row++) {
                float fade = 0.3f + 0.15f * Mth.sin(time * 1.5f + row * 0.15f);
                gfx.fill(barX + 1, fillTop + row, barX + 2, fillTop + row + 1,
                         ((int)(80 * fade) << 24) | (0xFF << 16) | (0x60 << 8) | 0x50);
            }
        }

        // Text label to the right of the bar
        int labelX = barX + barW + 8;
        int midY   = barY + barH / 2 - 8;
        gfx.drawString(font, "SSC Blood", labelX, midY,     TEXT_DIM,   false);
        String vol = (int) sscBlood + " / " + (int) sscMaxBlood + " mL";
        gfx.drawString(font, vol,          labelX, midY + 10, TEXT_BRIGHT, false);
    }

    // ── Drudge row ───────────────────────────────────────────────────────────

    private void renderDrudgeRow(GuiGraphics gfx, int index, DrudgeEntry d, int rowTop) {
        int rowBg = d.rogue() ? ROW_ROGUE_BG : ROW_BG;

        // Row background
        gfx.fill(px + 8, rowTop, px + PANEL_W - 8, rowTop + ROW_H, rowBg);
        // Row border
        gfx.fill(px + 8, rowTop,             px + PANEL_W - 8, rowTop + 1,       ROW_BORDER);
        gfx.fill(px + 8, rowTop + ROW_H - 1, px + PANEL_W - 8, rowTop + ROW_H,   ROW_BORDER);
        gfx.fill(px + 8,              rowTop, px + 9,             rowTop + ROW_H, ROW_BORDER);
        gfx.fill(px + PANEL_W - 9, rowTop, px + PANEL_W - 8, rowTop + ROW_H,    ROW_BORDER);

        int textY = rowTop + 3;
        int textX = px + 13;

        // Index
        gfx.drawString(font, "#" + (index + 1), textX, textY, TEXT_DIM, false);

        // Name: custom name (from name tag) takes priority over memory name
        String displayName;
        if (!d.customName().isEmpty()) {
            displayName = "§e" + d.customName();
        } else if (!d.memoryName().isEmpty()) {
            displayName = "§7" + d.memoryName();
        } else {
            displayName = "§8none";
        }
        gfx.drawString(font, Component.literal(displayName), textX + 22, textY, 0xFFFFFFFF, false);

        // Mode badge (right-aligned relative to bar start)
        int modeColor;
        String modeStr;
        if (d.rogue()) {
            modeColor = TEXT_ROGUE;
            modeStr = "ROGUE";
        } else if (d.passive()) {
            modeColor = TEXT_PASSIVE;
            modeStr = "Passive";
        } else {
            modeColor = TEXT_CMD;
            modeStr = "Commanded";
        }
        int mw = font.width(modeStr);
        gfx.drawString(font, modeStr, px + PANEL_W - 14 - mw, textY, modeColor, false);

        // Charge bar (bottom half of row)
        int cBarX = textX + 22;
        int cBarY = textY + 10;
        int cBarW = PANEL_W - 50;
        int cBarH = 5;
        float ratio = d.maxCharge() > 0 ? d.charge() / d.maxCharge() : 0f;
        gfx.fill(cBarX - 1, cBarY - 1, cBarX + cBarW + 1, cBarY + cBarH + 1, BAR_BORDER);
        gfx.fill(cBarX, cBarY, cBarX + cBarW, cBarY + cBarH, BAR_BG);
        int filled = (int) (cBarW * ratio);
        if (filled > 0) {
            float pulse = 0.75f + 0.25f * Mth.sin(animTime * 2f + index * 0.8f);
            int r = (int) (((CHARGE_FILL >> 16) & 0xFF) * pulse);
            int green = (int) (((CHARGE_FILL >> 8) & 0xFF) * pulse);
            int b = (int) ((CHARGE_FILL & 0xFF) * pulse);
            gfx.fill(cBarX, cBarY, cBarX + filled, cBarY + cBarH,
                    (0xFF << 24) | (r << 16) | (green << 8) | b);
        }
        // Charge text — centered on the bar
        String chargeText = (int) d.charge() + "/" + (int) d.maxCharge() + " mL";
        int ctw = font.width(chargeText);
        gfx.drawString(font, chargeText, cBarX + (cBarW - ctw) / 2, cBarY - 1, TEXT_DIM, false);
    }

    // ── Vein animation ───────────────────────────────────────────────────────

    private void drawVeins(GuiGraphics gfx) {
        if (veinParams == null) return;
        float t = animTime;
        for (int i = 0; i < VEIN_COUNT; i++) {
            float[] p = veinParams[i];
            float startX = px + p[0] * PANEL_W;
            float startY = py + p[1] * panelH;
            float baseAngle = p[2];
            float speed = p[3];
            float amplitude = p[4];
            float frequency = p[5];
            int length = (int) p[6];
            int thickness = (int) p[7];
            float brightness = p[8];

            float angleDrift = baseAngle + 0.12f * Mth.sin(t * speed * 0.4f + i);
            float cosA = Mth.cos(angleDrift);
            float sinA = Mth.sin(angleDrift);
            float timeOffset = t * speed * 1.5f;

            int baseRed   = (int) (40 + 60 * brightness);
            int baseGreen = (int) (2  +  6 * brightness);
            int baseBlue  = (int) (2  +  4 * brightness);

            for (int step = 0; step < length; step++) {
                float squiggle = amplitude * Mth.sin(frequency * step + timeOffset);
                float micro    = (amplitude * 0.25f) * Mth.sin(frequency * 2.5f * step + timeOffset * 1.3f + i);
                float disp = squiggle + micro;

                float vx = startX + step * cosA * 1.5f - disp * sinA;
                float vy = startY + step * sinA * 1.5f + disp * cosA;
                int ix = (int) vx;
                int iy = (int) vy;

                if (ix + thickness < px || ix >= px + PANEL_W
                        || iy + thickness < py || iy >= py + panelH) continue;

                float tipFade = 1f;
                if (step < 8) tipFade = step / 8f;
                else if (step > length - 8) tipFade = (length - step) / 8f;

                float pulse = 0.6f + 0.4f * Mth.sin(t * 1.2f + i * 0.6f + step * 0.025f);
                int a = (int) Mth.clamp(tipFade * pulse * 120, 10, 130);
                int r = (int) Mth.clamp(baseRed * pulse, 0, 255);
                int g = (int) Mth.clamp(baseGreen * pulse, 0, 255);
                int b = (int) Mth.clamp(baseBlue * pulse, 0, 255);

                gfx.fill(ix, iy, ix + thickness, iy + thickness,
                        (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
    }

    // ── Border ───────────────────────────────────────────────────────────────

    private void drawBorder(GuiGraphics gfx, int x, int y, int w, int h) {
        // Outer 1px
        gfx.fill(x,     y,     x + w, y + 1, BORDER_OUT);
        gfx.fill(x,     y + h - 1, x + w, y + h, BORDER_OUT);
        gfx.fill(x,     y,     x + 1, y + h, BORDER_OUT);
        gfx.fill(x + w - 1, y, x + w, y + h, BORDER_OUT);
        // Inner 1px
        gfx.fill(x + 1, y + 1, x + w - 1, y + 2, BORDER_IN);
        gfx.fill(x + 1, y + h - 2, x + w - 1, y + h - 1, BORDER_IN);
        gfx.fill(x + 1, y + 1, x + 2, y + h - 1, BORDER_IN);
        gfx.fill(x + w - 2, y + 1, x + w - 1, y + h - 1, BORDER_IN);
    }

    // ── Key handling ─────────────────────────────────────────────────────────

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Allow Escape or Inventory key to close
        if (keyCode == 256 /* GLFW_KEY_ESCAPE */) {
            this.onClose();
            return true;
        }
        assert this.minecraft != null;
        if (this.minecraft.options.keyInventory.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
