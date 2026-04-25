package com.vincenthuto.hemomancy.client.screen.skilltree.unstained;

import java.util.Random;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

public final class UnstainedBackgroundRenderer {
    private static final int RHOMBUS_COUNT = 10;
    // [particle][param]:
    // 0=startX ratio, 1=startY ratio, 2=half-size, 3=velX, 4=velY, 5=phase, 6=brightness, 7=rotation speed
    private float[][] rhombusParams;

    public UnstainedBackgroundRenderer() {
        seed(99L);
    }

    private void seed(long seed) {
        Random rand = new Random(seed);
        rhombusParams = new float[RHOMBUS_COUNT][8];
        for (int i = 0; i < RHOMBUS_COUNT; i++) {
            rhombusParams[i][0] = rand.nextFloat();
            rhombusParams[i][1] = rand.nextFloat();
            rhombusParams[i][2] = 12 + rand.nextInt(24);
            rhombusParams[i][3] = (rand.nextFloat() - 0.5f) * 10f;
            rhombusParams[i][4] = (rand.nextFloat() - 0.5f) * 8f;
            rhombusParams[i][5] = rand.nextFloat() * (float) (Math.PI * 2);
            rhombusParams[i][6] = 0.5f + rand.nextFloat() * 0.5f;
            rhombusParams[i][7] = (rand.nextFloat() - 0.5f) * 1.2f;
        }
    }

    public void render(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
        gfx.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        gfx.fill(gx, gy, gx + gw, gy + gh, 0xFF060A1E);

        int centerX = gx + gw / 2;
        int centerY = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= 4) {
            float t = (float) ring / glowRadius;
            float intensity = (1f - t) * (1f - t);
            int alpha = (int) (50 * intensity);
            int r = (int) (200 * intensity);
            int g = (int) (210 * intensity);
            int b = (int) (255 * intensity);
            int color = (alpha << 24) | (r << 16) | (g << 8) | b;
            gfx.fill(centerX - ring, centerY - ring, centerX + ring, centerY + ring, color);
        }

        float time = com.vincenthuto.hemomancy.client.screen.util.ScreenAnimationClock.tick();
        for (int i = 0; i < RHOMBUS_COUNT; i++) {
            drawFloatingRhombus(gfx, i, time, gx, gy, gw, gh);
        }

        for (int s = 0; s < 120; s++) {
            int h1 = mix(54321, s);
            int h2 = mix(98731, s);
            int h3 = mix(24691, s);
            int spx = gx + Math.floorMod(h1, gw);
            int spy = gy + Math.floorMod(h2, gh);
            int sb = 10 + Math.floorMod(h3, 20);
            int sg = Math.floorMod(mix(11939, s), 8);
            int sa = 15 + Math.floorMod(mix(7727, s), 25);
            gfx.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sg << 8) | sb);
        }

        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private void drawFloatingRhombus(GuiGraphics gfx, int index, float time, int gx, int gy, int gw, int gh) {
        float[] p = rhombusParams[index];
        float startXRatio = p[0];
        float startYRatio = p[1];
        int halfSize = (int) p[2];
        float velX = p[3];
        float velY = p[4];
        float phase = p[5];
        float brightness = p[6];
        float rotSpeed = p[7];

        float rawX = startXRatio * gw + velX * time;
        float rawY = startYRatio * gh + velY * time;
        rawX += 5f * Mth.sin(time * 0.4f + phase);
        rawY += 4f * Mth.cos(time * 0.35f + phase * 1.3f);

        int cx = gx + ((int) rawX % gw + gw) % gw;
        int cy = gy + ((int) rawY % gh + gh) % gh;

        float angle = phase + rotSpeed * time;

        float pulse = 0.6f + 0.4f * Mth.sin(time * 0.7f + phase);
        int baseAlpha = (int) (50 + 80 * brightness * pulse);

        int r = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
        int g = (int) Mth.clamp(180 + 75 * brightness, 0, 255);
        int b = (int) Mth.clamp(200 + 55 * brightness, 0, 255);

        int color = (baseAlpha << 24) | (r << 16) | (g << 8) | b;
        int thickness = 3 + halfSize / 6;

        drawRotatedHollowRhombus(gfx, cx, cy, halfSize, thickness, angle, color);
    }

    private void drawRotatedHollowRhombus(GuiGraphics gfx, int cx, int cy, int halfSize, int thickness, float angle, int color) {
        float cosA = Mth.cos(angle);
        float sinA = Mth.sin(angle);
        int innerSize = halfSize - thickness;
        int bound = halfSize + 1;

        for (int dy = -bound; dy <= bound; dy++) {
            int spanStart = Integer.MIN_VALUE;

            for (int dx = -bound; dx <= bound; dx++) {
                float u = dx * cosA + dy * sinA;
                float v = -dx * sinA + dy * cosA;
                float dist = Math.abs(u) + Math.abs(v);

                boolean inRing = dist <= halfSize && (innerSize <= 0 || dist >= innerSize);

                if (inRing) {
                    if (spanStart == Integer.MIN_VALUE) {
                        spanStart = dx;
                    }
                } else if (spanStart != Integer.MIN_VALUE) {
                    gfx.fill(cx + spanStart, cy + dy, cx + dx, cy + dy + 1, color);
                    spanStart = Integer.MIN_VALUE;
                }
            }

            if (spanStart != Integer.MIN_VALUE) {
                gfx.fill(cx + spanStart, cy + dy, cx + bound + 1, cy + dy + 1, color);
            }
        }
    }

    private static int mix(int salt, int value) {
        int x = value * 0x45d9f3b ^ salt;
        x ^= x >>> 16;
        x *= 0x45d9f3b;
        x ^= x >>> 16;
        return x;
    }
}
