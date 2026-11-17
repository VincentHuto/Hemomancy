package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * The Temper vials, read from {@code textures/gui/blood_hud/temper_vial_<side>.png}: square sprites with the vial's
 * mouth on the centre pixel, tipped toward the vessel. {@code temper_vial_<side>_liquid.png} marks the glass the
 * Temper fills; its cells are ordered lowest on screen first, so a partial fill settles the way a tipped vial would.
 * {@code temper_vial_<side>_glass.png} is the front of the glass, drawn over the liquid, and
 * {@code temper_socket_<side>.png} is the healed seat of a school never touched.
 */
public final class TemperVialArt {
    private final VialSprite right;
    private final VialSprite left;
    private final VialSprite socketRight;
    private final VialSprite socketLeft;
    private final VialSprite glassRight;
    private final VialSprite glassLeft;

    /** Every pixel as an offset from the mouth with its ARGB; liquid cells are a subset, lowest first. */
    public record VialSprite(int[] dx, int[] dy, int[] argb, int[] liquidDx, int[] liquidDy) {
    }

    private TemperVialArt(VialSprite right, VialSprite left, VialSprite socketRight, VialSprite socketLeft,
                          VialSprite glassRight, VialSprite glassLeft) {
        this.glassRight = glassRight;
        this.glassLeft = glassLeft;
        this.right = right;
        this.left = left;
        this.socketRight = socketRight;
        this.socketLeft = socketLeft;
    }

    /** All images are {@code size}×{@code size} ARGB with the mouth at the centre pixel. */
    public static TemperVialArt of(int size, int[] right, int[] rightLiquid, int[] left, int[] leftLiquid,
                                   int[] socketRight, int[] socketLeft, int[] glassRight, int[] glassLeft) {
        return new TemperVialArt(sprite(size, right, rightLiquid, true), sprite(size, left, leftLiquid, false),
                sprite(size, socketRight, null, true), sprite(size, socketLeft, null, false),
                sprite(size, glassRight, null, true), sprite(size, glassLeft, null, false));
    }

    public VialSprite sprite(boolean pointsRight) {
        return pointsRight ? right : left;
    }

    /** A closed, healed seat where a school has never been touched. */
    public VialSprite socket(boolean pointsRight) {
        return pointsRight ? socketRight : socketLeft;
    }

    /** The front of the glass, drawn over the liquid. */
    public VialSprite glass(boolean pointsRight) {
        return pointsRight ? glassRight : glassLeft;
    }

    public static int fillCount(VialSprite sprite, float fraction) {
        return Math.round(sprite.liquidDy().length * Math.max(0f, Math.min(1f, fraction)));
    }

    private static VialSprite sprite(int size, int[] art, int[] liquidMask, boolean pointsRight) {
        if (art.length != size * size || (liquidMask != null && liquidMask.length != size * size)) {
            throw new IllegalArgumentException("vial sprites must be " + size + "x" + size);
        }
        int mouth = size / 2;
        List<int[]> pixels = new ArrayList<>();
        List<int[]> liquid = new ArrayList<>();
        for (int i = 0; i < art.length; i++) {
            int x = i % size - mouth, y = i / size - mouth;
            if ((art[i] >>> 24) != 0) pixels.add(new int[]{x, y, art[i]});
            if (liquidMask != null && (liquidMask[i] >>> 24) != 0) liquid.add(new int[]{x, y});
        }
        liquid.sort(Comparator.comparingInt((int[] p) -> -p[1]).thenComparingInt(p -> pointsRight ? -p[0] : p[0]));
        int[] dx = new int[pixels.size()], dy = new int[pixels.size()], argb = new int[pixels.size()];
        for (int i = 0; i < pixels.size(); i++) {
            dx[i] = pixels.get(i)[0];
            dy[i] = pixels.get(i)[1];
            argb[i] = pixels.get(i)[2];
        }
        int[] lx = new int[liquid.size()], ly = new int[liquid.size()];
        for (int i = 0; i < liquid.size(); i++) {
            lx[i] = liquid.get(i)[0];
            ly[i] = liquid.get(i)[1];
        }
        return new VialSprite(dx, dy, argb, lx, ly);
    }
}
