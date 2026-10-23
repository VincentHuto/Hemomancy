package com.vincenthuto.hemomancy.client.screen.skilltree.harbinger;

import com.mojang.blaze3d.systems.RenderSystem;
import com.vincenthuto.hemomancy.client.screen.skilltree.util.ColoredRectBatch;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;

import java.util.Random;

/**
 * The animated vein background behind Hemomancy's blood screens: a dark fill, a radial glow, drifting tendrils and
 * speckles, drawn in one batch inside a scissor. A screen keeps its look by choosing a {@link Style}: the seeded
 * tendril {@link Field}, the {@link Palette} and the {@link Speckles}.
 *
 * <p>{@link #render} advances the renderer's own clock by 0.016 per call; {@link #draw} takes the time from a screen
 * that keeps its own clock. A non-zero deep fade blends the palette towards {@link Style#deep}.
 */
public final class VeinBackgroundRenderer {
    /**
     * The seeded tendril layout: how many tendrils, and the ranges each one's speed, amplitude, length and thickness
     * are drawn from. {@code stride} skips steps along a tendril and {@code grow} widens each dot; {@code anglePhase}
     * offsets each tendril's wave by its base angle.
     */
    public record Field(int count, long seed, float speedBase, float speedRange, float amplitudeBase, float amplitudeRange,
                        int lengthBase, int lengthRange, int thicknessRange, int stride, int grow, boolean anglePhase) {
        /** The Harbinger screens' 28 long tendrils. */
        public static final Field STANDARD = new Field(28, 42L, 0.3f, 0.7f, 8f, 18f, 60, 120, 3, 1, 0, false);
        /** Shorter, finer tendrils for the machine screens. */
        public static final Field MACHINE = new Field(20, 27182L, 0.3f, 0.7f, 8f, 16f, 50, 110, 2, 1, 0, false);
        /** The stations' shallower tendrils. */
        public static final Field STATION = new Field(16, 31415L, 0.3f, 0.7f, 8f, 14f, 50, 100, 2, 1, 0, false);

        public Field count(int count) {
            return new Field(count, seed, speedBase, speedRange, amplitudeBase, amplitudeRange, lengthBase, lengthRange,
                    thicknessRange, stride, grow, anglePhase);
        }

        public Field seed(long seed) {
            return new Field(count, seed, speedBase, speedRange, amplitudeBase, amplitudeRange, lengthBase, lengthRange,
                    thicknessRange, stride, grow, anglePhase);
        }

        public Field withAnglePhase() {
            return new Field(count, seed, speedBase, speedRange, amplitudeBase, amplitudeRange, lengthBase, lengthRange,
                    thicknessRange, stride, grow, true);
        }
    }

    /**
     * Colours: the base fill, the glow rings (spacing and per-channel strength at the centre), and each tendril
     * channel as {@code base + bright * brightness}, scaled per channel by the pulse.
     */
    public record Palette(int background, int ringStep, float ringAlpha, float ringRed, float ringGreen, float ringBlue,
                          float redBase, float redBright, float greenBase, float greenBright, float blueBase,
                          float blueBright, float redScale, float blueScale) {
        /** The crimson of the Harbinger screens. */
        public static final Palette CRIMSON = new Palette(0xFF0A0204, 4, 35, 40, 0, 0, 40, 50, 2, 8, 5, 5, 1f, 0.3f);
        /** Crimson with a dimmer glow, for the machine and station screens. */
        public static final Palette MACHINE = CRIMSON.rings(30, 35, 0, 0);
        /** The progress screen's crimson, whose glow keeps a trace of green and blue. */
        public static final Palette PROGRESS = CRIMSON.rings(35, 40, 4, 3);
        /** Where {@link #PROGRESS} fades to as the deep path opens: violet. */
        public static final Palette PROGRESS_DEEP = new Palette(0xFF0E020D, 4, 45, 47, 5, 15, 34, 24, 3, 4, 18, 16, 0.86f, 0.74f);

        public Palette background(int background) {
            return new Palette(background, ringStep, ringAlpha, ringRed, ringGreen, ringBlue, redBase, redBright,
                    greenBase, greenBright, blueBase, blueBright, redScale, blueScale);
        }

        public Palette ringStep(int ringStep) {
            return new Palette(background, ringStep, ringAlpha, ringRed, ringGreen, ringBlue, redBase, redBright,
                    greenBase, greenBright, blueBase, blueBright, redScale, blueScale);
        }

        public Palette rings(float alpha, float red, float green, float blue) {
            return new Palette(background, ringStep, alpha, red, green, blue, redBase, redBright, greenBase, greenBright,
                    blueBase, blueBright, redScale, blueScale);
        }

        public Palette tendrils(float redBase, float redBright, float greenBase, float greenBright, float blueBase,
                                float blueBright) {
            return new Palette(background, ringStep, ringAlpha, ringRed, ringGreen, ringBlue, redBase, redBright,
                    greenBase, greenBright, blueBase, blueBright, redScale, blueScale);
        }
    }

    /** One-pixel motes over the veins. */
    public sealed interface Speckles {
        /** Re-rolled from {@code seed} each frame across the whole area: red, green and alpha from their ranges. */
        record Scattered(long seed, int count, int redBase, int redRange, int greenRange, int alphaBase, int alphaRange)
                implements Speckles {
            /** The Harbinger screens' 120 crimson motes. */
            public static final Scattered CRIMSON = new Scattered(12345L, 120, 10, 20, 6, 15, 25);
            /** The machine screens' sparser, fainter motes. */
            public static final Scattered MACHINE = new Scattered(13579L, 70, 10, 20, 6, 15, 20);

            public Scattered count(int count) {
                return new Scattered(seed, count, redBase, redRange, greenRange, alphaBase, alphaRange);
            }

            public Scattered seed(long seed) {
                return new Scattered(seed, count, redBase, redRange, greenRange, alphaBase, alphaRange);
            }
        }

        /**
         * Rolled once over a {@code width} × {@code height} area and wrapped into the drawn one. Green comes from its
         * own range, or, when that is 0, is the red divided by {@code greenDivisor}.
         */
        record Fixed(long seed, int count, int width, int height, int redBase, int redRange, int greenRange,
                     int greenDivisor, int alphaBase, int alphaRange) implements Speckles {
        }

        /** The progress screen's motes, which turn violet with the deep fade. */
        record Deepening(long seed, int count) implements Speckles {
        }
    }

    /**
     * A screen's complete look. {@code deep} is what a non-zero deep fade blends towards. {@code fillOrder} draws each
     * rect with {@code GuiGraphics.fill}'s vertex order (see {@link ColoredRectBatch}): the screens that filled their
     * rects one by one keep it, the Harbinger screens that were batched from the start keep the batch's own order.
     */
    public record Style(Field field, Palette palette, Palette deep, Speckles speckles, boolean fillOrder) {
        public Style(Field field, Palette palette, Speckles speckles) {
            this(field, palette, palette, speckles, true);
        }

        /** The blood screens' crimson field. */
        public static final Style CRIMSON = new Style(Field.STANDARD, Palette.CRIMSON, Speckles.Scattered.CRIMSON);
        /** The machine screens' finer, dimmer field. */
        public static final Style MACHINE = new Style(Field.MACHINE, Palette.MACHINE, Speckles.Scattered.MACHINE);

        /** The Harbinger screens' default: 28 crimson tendrils that deepen to violet. */
        public static final Style PROGRESS = new Style(Field.STANDARD.withAnglePhase(), Palette.PROGRESS, Palette.PROGRESS_DEEP,
                new Speckles.Deepening(12345L, 120), false);
    }

    private final Style style;
    private float[][] veinParams;
    private int[][] fixedSpeckles;
    private float animTime = 0f;

    public VeinBackgroundRenderer() {
        this(Style.PROGRESS);
    }

    public VeinBackgroundRenderer(Style style) {
        this.style = style;
        seed(style.field.seed);
        if (style.speckles instanceof Speckles.Fixed fixed) {
            Random rand = new Random(fixed.seed);
            fixedSpeckles = new int[fixed.count][];
            for (int s = 0; s < fixed.count; s++) {
                int x = rand.nextInt(fixed.width), y = rand.nextInt(fixed.height);
                int red = fixed.redBase + rand.nextInt(fixed.redRange);
                int green = fixed.greenRange > 0 ? rand.nextInt(fixed.greenRange) : red / fixed.greenDivisor;
                int alpha = fixed.alphaBase + rand.nextInt(fixed.alphaRange);
                fixedSpeckles[s] = new int[]{x, y, red, green, alpha};
            }
        }
    }

    public void seed(long s) {
        Field field = style.field;
        Random rand = new Random(s);
        veinParams = new float[field.count][9];
        for (int i = 0; i < field.count; i++) {
            veinParams[i][0] = rand.nextFloat();
            veinParams[i][1] = rand.nextFloat();
            veinParams[i][2] = (float)(rand.nextFloat() * Math.PI * 2); // phase seed
            veinParams[i][3] = field.speedBase + rand.nextFloat() * field.speedRange;
            veinParams[i][4] = field.amplitudeBase + rand.nextFloat() * field.amplitudeRange;
            veinParams[i][5] = 0.04f + rand.nextFloat() * 0.08f;
            veinParams[i][6] = field.lengthBase + rand.nextInt(field.lengthRange);
            veinParams[i][7] = 1 + rand.nextInt(field.thicknessRange);
            veinParams[i][8] = rand.nextFloat();
        }
    }

    public void render(GuiGraphics gfx, int gx, int gy, int gw, int gh) {
        render(gfx, gx, gy, gw, gh, 0.0f);
    }

    public void render(GuiGraphics gfx, int gx, int gy, int gw, int gh, float deepFade) {
		animTime += 0.016f; // ~60 FPS approximation
        draw(gfx, gx, gy, gw, gh, animTime, deepFade);
    }

    public void draw(GuiGraphics gfx, int gx, int gy, int gw, int gh, float time) {
        draw(gfx, gx, gy, gw, gh, time, 0.0f);
    }

    public void draw(GuiGraphics gfx, int gx, int gy, int gw, int gh, float time, float deepFade) {
        Palette from = style.palette, to = style.deep;
        float fade = Mth.clamp(deepFade, 0.0f, 1.0f);

        gfx.enableScissor(gx, gy, gx + gw, gy + gh);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
		ColoredRectBatch batch = new ColoredRectBatch(gfx, style.fillOrder);

        batch.fill(gx, gy, gx + gw, gy + gh, lerpArgb(from.background, to.background, fade));

        int cx = gx + gw / 2, cy = gy + gh / 2;
        int glowRadius = Math.max(gw, gh) / 2;
        for (int ring = glowRadius; ring > 0; ring -= from.ringStep) {
            float t = (float)ring / glowRadius;
            int alpha = (int)(Mth.lerp(fade, from.ringAlpha, to.ringAlpha) * (1f - t));
            int red = (int)(Mth.lerp(fade, from.ringRed, to.ringRed) * (1f - t));
            int green = (int)(Mth.lerp(fade, from.ringGreen, to.ringGreen) * (1f - t));
            int blue = (int)(Mth.lerp(fade, from.ringBlue, to.ringBlue) * (1f - t));
            batch.fill(cx - ring, cy - ring, cx + ring, cy + ring, (alpha << 24) | (red << 16) | (green << 8) | blue);
        }

        for (int i = 0; i < style.field.count; i++) {
            drawVeinTendril(batch, i, time, gx, gy, gw, gh, fade);
        }

        drawSpeckles(batch, gx, gy, gw, gh, fade);
		batch.flush();

        RenderSystem.disableBlend();
        gfx.disableScissor();
    }

    private void drawSpeckles(ColoredRectBatch batch, int gx, int gy, int gw, int gh, float fade) {
        switch (style.speckles) {
            case Speckles.Scattered scattered -> {
                Random speckRand = new Random(scattered.seed);
                for (int s = 0; s < scattered.count; s++) {
                    int spx = gx + speckRand.nextInt(gw), spy = gy + speckRand.nextInt(gh);
                    int sr = scattered.redBase + speckRand.nextInt(scattered.redRange);
                    int sg = speckRand.nextInt(scattered.greenRange);
                    int sa = scattered.alphaBase + speckRand.nextInt(scattered.alphaRange);
                    batch.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sr << 16) | (sg << 8));
                }
            }
            case Speckles.Fixed fixed -> {
                for (int[] speck : fixedSpeckles) {
                    int spx = gx + speck[0] % gw, spy = gy + speck[1] % gh;
                    batch.fill(spx, spy, spx + 1, spy + 1, (speck[4] << 24) | (speck[2] << 16) | (speck[3] << 8));
                }
            }
            case Speckles.Deepening deepening -> {
                Random speckRand = new Random(deepening.seed);
                for (int s = 0; s < deepening.count; s++) {
                    int spx = gx + speckRand.nextInt(gw), spy = gy + speckRand.nextInt(gh);
                    int sr = 10 + speckRand.nextInt(20) + (int)(7 * fade), sg = (int)(speckRand.nextInt(5) * (1f - fade)), sa = 15 + speckRand.nextInt(25);
                    int sb = (int)(fade * (6 + speckRand.nextInt(7)));
                    batch.fill(spx, spy, spx + 1, spy + 1, (sa << 24) | (sr << 16) | (sg << 8) | sb);
                }
            }
        }
    }

    private void drawVeinTendril(ColoredRectBatch batch, int index, float time, int gx, int gy, int gw, int gh, float deepFade) {
        Field field = style.field;
        Palette from = style.palette, to = style.deep;
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

		float timeOffset = field.anglePhase ? time * speed * 2.0f + baseAngle : time * speed * 2.0f;

        int baseRed   = (int)Mth.lerp(deepFade, from.redBase + from.redBright * brightness, to.redBase + to.redBright * brightness);
        int baseGreen = (int)Mth.lerp(deepFade, from.greenBase + from.greenBright * brightness, to.greenBase + to.greenBright * brightness);
        int baseBlue  = (int)Mth.lerp(deepFade, from.blueBase + from.blueBright * brightness, to.blueBase + to.blueBright * brightness);
        float deepRedScale = Mth.lerp(deepFade, from.redScale, to.redScale);
        float deepBlueScale = Mth.lerp(deepFade, from.blueScale, to.blueScale);
        int size = thickness + field.grow;

        for (int step = 0; step < length; step += field.stride) {
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

            batch.fill(ix, iy, ix + size, iy + size, (a << 24) | (r << 16) | (g << 8) | b);
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
