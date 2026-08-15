package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ColoredRectBatch;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Random;

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
        render(gfx, gx, gy, gw, gh, 0.0f);
    }

    public void render(GuiGraphics gfx, int gx, int gy, int gw, int gh, float deepFade) {
		animTime += 0.016f; // ~60 FPS approximation

        float time = animTime;
        float fade = Mth.clamp(deepFade, 0.0f, 1.0f);

        gfx.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
		ColoredRectBatch batch = new ColoredRectBatch(gfx);

        batch.fill(gx, gy, gx + gw, gy + gh, lerpArgb(0xFF0A0204, 0xFF0E020D, fade));

        int cx = gx + gw / 2, cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 4) {
            float t = (float)ring / glowRadius;
            int alpha = (int)((35 + 10 * fade) * (1f - t));
            int red = (int)((40 + 7 * fade) * (1f - t));
            int green = (int)((4 + fade) * (1f - t));
            int blue = (int)((3 + 12 * fade) * (1f - t));
            batch.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16) | (green << 8) | blue);
        }

        for (int i = 0; i < VEIN_COUNT; i++) {
            drawVeinTendril(batch, i, time, gx, gy, gw, gh, fade);
        }

        Random speckRand = new Random(12345L);
        for (int s = 0; s < 120; s++) {
            int spx = gx + speckRand.nextInt(gw), spy = gy + speckRand.nextInt(gh);
            int sr = 10 + speckRand.nextInt(20) + (int)(7 * fade), sg = (int)(speckRand.nextInt(5) * (1f - fade)), sa = 15 + speckRand.nextInt(25);
            int sb = (int)(fade * (6 + speckRand.nextInt(7)));
            batch.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sr << 16) | (sg << 8) | sb);
        }
		batch.flush();

        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private void drawVeinTendril(ColoredRectBatch batch, int index, float time, int gx, int gy, int gw, int gh, float deepFade) {
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

		float angleDrift = baseAngle + 0.15f * Mth.sin(time * speed * 0.3f + index);

        float cosA = Mth.cos(angleDrift);
        float sinA = Mth.sin(angleDrift);

		float timeOffset = time * speed * 2.0f + baseAngle;

        int baseRed   = (int)Mth.lerp(deepFade, 40 + 50 * brightness, 34 + 24 * brightness);
        int baseGreen = (int)Mth.lerp(deepFade, 2  + 8  * brightness, 3  + 4  * brightness);
        int baseBlue  = (int)Mth.lerp(deepFade, 5  + 5  * brightness, 18 + 16 * brightness);
        float deepRedScale = Mth.lerp(deepFade, 1.0f, 0.86f);
        float deepBlueScale = Mth.lerp(deepFade, 0.3f, 0.74f);

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
            int r = (int)Mth.clamp(baseRed   * pulse * deepRedScale, 0, 255);
            int g = (int)Mth.clamp(baseGreen * pulse * 0.5f, 0, 255);
            int b = (int)Mth.clamp(baseBlue  * pulse * deepBlueScale, 0, 255);

            batch.fill(ix, iy, ix + thickness, iy + thickness, (a << 24) | (r << 16) | (g << 8) | b);
        }
    }

    private static int lerpArgb(int from, int to, float t) {
        int a = lerpChannel((from >>> 24) & 0xFF, (to >>> 24) & 0xFF, t);
        int r = lerpChannel((from >>> 16) & 0xFF, (to >>> 16) & 0xFF, t);
        int g = lerpChannel((from >>> 8) & 0xFF, (to >>> 8) & 0xFF, t);
        int b = lerpChannel(from & 0xFF, to & 0xFF, t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int lerpChannel(int from, int to, float t) {
        return (int) Mth.clamp(from + (to - from) * t, 0, 255);
    }
}
