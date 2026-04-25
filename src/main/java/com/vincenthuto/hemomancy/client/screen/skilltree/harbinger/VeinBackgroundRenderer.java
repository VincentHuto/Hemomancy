package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import java.util.Random;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class VeinBackgroundRenderer {
    private static final int VEIN_COUNT = 28;
    private float[][] veinParams;


    public VeinBackgroundRenderer() {
        seed(42L);
    }

    public void seed(long s) {
        Random rand = new Random(s);
        veinParams = new float[VEIN_COUNT][9];
        for (int i = 0; i < VEIN_COUNT; i++) {
            veinParams[i][0] = rand.nextFloat();
            veinParams[i][1] = rand.nextFloat();
            veinParams[i][2] = (float)(rand.nextFloat() * Math.PI * 2); // phase seed
            veinParams[i][3] = 0.3f + rand.nextFloat() * 0.7f;
            veinParams[i][4] = 8f + rand.nextFloat() * 18f;
            veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
            veinParams[i][6] = 60 + rand.nextInt(120);
            veinParams[i][7] = 1 + rand.nextInt(3);
            veinParams[i][8] = rand.nextFloat();
        }
    }
    private float animTime = 0f;

    public void render(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
        // --- NEW: advance time (frame-based fallback) ---
        animTime += 0.016f; // ~60 FPS approximation

        float time = animTime;

        gfx.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.fill(gx, gy, gx + gw, gy + gh, 0xFF0A0204);

        int cx = gx + gw / 2, cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 4) {
            float t = (float)ring / glowRadius;
            int alpha = (int)(35 * (1f - t));
            int red = (int)(40 * (1f - t));
            gfx.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16));
        }

        for (int i = 0; i < VEIN_COUNT; i++) {
            drawVeinTendril(gfx, i, time, gx, gy, gw, gh);
        }

        Random speckRand = new Random(12345L);
        for (int s = 0; s < 120; s++) {
            int spx = gx + speckRand.nextInt(gw), spy = gy + speckRand.nextInt(gh);
            int sr = 10 + speckRand.nextInt(20), sg = speckRand.nextInt(6), sa = 15 + speckRand.nextInt(25);
            gfx.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sr << 16) | (sg << 8));
        }

        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private void drawVeinTendril(GuiGraphics gfx, int index, float time, int gx, int gy, int gw, int gh) {
        float[] p = veinParams[index];

        float startX    = gx + p[0] * gw;
        float startY    = gy + p[1] * gh;
        float baseAngle = p[2]; // also used as phase seed
        float speed     = p[3];
        float amplitude = p[4];
        float frequency = p[5];
        int   length    = (int) p[6];
        int   thickness = (int) p[7];
        float brightness = p[8];

        // --- Slight drift, still deterministic ---
        float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);

        float cosA = Mth.cos(angleDrift);
        float sinA = Mth.sin(angleDrift);

        // --- NEW: seeded phase for consistency ---
        float timeOffset = time * speed * 2.0f + baseAngle;

        int baseRed   = (int)(40 + 50 * brightness);
        int baseGreen = (int)(2  + 8  * brightness);
        int baseBlue  = (int)(5  + 5  * brightness);

        for (int step = 0; step < length; step++) {
            float squiggle      = amplitude * Mth.sin(frequency * step + timeOffset);
            float microSquiggle = (amplitude * 0.3f) * Mth.sin(frequency * 2.7f * step + timeOffset * 1.4f + index);
            float displacement  = squiggle + microSquiggle;

            float px = startX + step * cosA * 1.5f - displacement * sinA;
            float py = startY + step * sinA * 1.5f + displacement * cosA;

            int ix = (int)px;
            int iy = (int)py;

            if (ix + thickness < gx || ix >= gx + gw || iy + thickness < gy || iy >= gy + gh) continue;

            float tipFade = 1f;
            if (step < 10)             tipFade = step / 10f;
            else if (step > length-10) tipFade = (length - step) / 10f;

            float pulse = 0.7f + 0.3f * Mth.sin(time * 1.5f + index * 0.5f + step * 0.02f);

            int a = (int)Mth.clamp(tipFade * pulse * 180, 20, 200);
            int r = (int)Mth.clamp(baseRed   * pulse,        0, 255);
            int g = (int)Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
            int b = (int)Mth.clamp(baseBlue  * pulse * 0.3f, 0, 255);

            gfx.fill(ix, iy, ix + thickness, iy + thickness, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }
}