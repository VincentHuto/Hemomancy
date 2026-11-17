package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * One HUD state's hand-placed vials, read from {@code layouts/<state>_vials.png} (the art),
 * {@code <state>_vials_liquid.png} (each school's fillable glass in its key colour) and
 * {@code <state>_vials_mouth.png} (one key-coloured dot where each vial's trickle leaves), with an optional
 * {@code <state>_vials_glass.png} drawn over the liquid. Every art pixel belongs
 * to the school whose liquid or mouth is nearest, which hides an unseen school's vial and picks a tooltip's school.
 */
public final class BakedVials {
    /** Animus, Flammeus, Ductilis, Lux, Mortem, Congeatio, Ferric, Tenebris. */
    public static final int[] KEYS = {0xFF0000, 0xFF8000, 0xFFFF00, 0x00FFFF, 0x00FF00, 0x0000FF, 0x808080, 0xFF00FF};
    private static final int REACH = 16;

    private final int width;
    private final int height;
    private final int[] art;
    private final int[] owner;
    /** Per school, the layout indices of its art pixels. */
    private final int[][] artPixels;
    private final int[] glass;
    private final int[][] glassPixels;
    private final int[][] liquid;
    private final int[] mouthX;
    private final int[] mouthY;

    private BakedVials(int width, int height, int[] art, int[] glass, int[] owner, int[][] liquid, int[] mouthX,
                       int[] mouthY) {
        this.glass = glass;
        this.glassPixels = byOwner(owner, glass);
        this.width = width;
        this.height = height;
        this.art = art;
        this.owner = owner;
        int[] counts = new int[KEYS.length];
        for (int i = 0; i < art.length; i++) if (owner[i] >= 0 && (art[i] >>> 24) != 0) counts[owner[i]]++;
        this.artPixels = new int[KEYS.length][];
        for (int s = 0; s < KEYS.length; s++) artPixels[s] = new int[counts[s]];
        int[] fill = new int[KEYS.length];
        for (int i = 0; i < art.length; i++) if (owner[i] >= 0 && (art[i] >>> 24) != 0) artPixels[owner[i]][fill[owner[i]]++] = i;
        this.liquid = liquid;
        this.mouthX = mouthX;
        this.mouthY = mouthY;
    }

    public static BakedVials of(int width, int height, int[] art, int[] liquidKey, int[] mouthKey, int[] glass) {
        int n = width * height;
        if (glass == null) glass = new int[n];
        if (art.length != n || liquidKey.length != n || mouthKey.length != n || glass.length != n) {
            throw new IllegalArgumentException("vial layout layers differ in size");
        }
        List<List<Integer>> cells = new ArrayList<>();
        for (int s = 0; s < KEYS.length; s++) cells.add(new ArrayList<>());
        int[] mouthX = new int[KEYS.length], mouthY = new int[KEYS.length];
        Arrays.fill(mouthX, -1);
        Arrays.fill(mouthY, -1);
        int[] seed = new int[n];
        Arrays.fill(seed, -1);
        for (int i = 0; i < n; i++) {
            if ((liquidKey[i] >>> 24) != 0) {
                int school = school(liquidKey[i]);
                cells.get(school).add(i);
                seed[i] = school;
            }
            if ((mouthKey[i] >>> 24) != 0) {
                int school = school(mouthKey[i]);
                mouthX[school] = i % width;
                mouthY[school] = i / width;
                seed[i] = school;
            }
        }
        int[] owner = nearestOwner(width, height, art, seed);
        int[][] liquid = new int[KEYS.length][];
        for (int s = 0; s < KEYS.length; s++) {
            List<Integer> list = cells.get(s);
            list.sort(Comparator.comparingInt((Integer i) -> -(i / width)).thenComparingInt(i -> i % width));
            liquid[s] = list.stream().mapToInt(Integer::intValue).toArray();
        }
        return new BakedVials(width, height, art, glass, owner, liquid, mouthX, mouthY);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    /** The school a pixel of this layout belongs to, or -1. */
    public int schoolAt(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return -1;
        return owner[y * width + x];
    }

    /** Art pixel indices and colours of one school's vial, in reading order. */
    public void forEachArt(int school, PixelSink sink) {
        for (int i : artPixels[school]) sink.accept(i % width, i / width, art[i]);
    }

    /** Front-glass pixels of one school's vial, drawn over its liquid. */
    public void forEachGlass(int school, PixelSink sink) {
        for (int i : glassPixels[school]) sink.accept(i % width, i / width, glass[i]);
    }

    private static int[][] byOwner(int[] owner, int[] layer) {
        int[] counts = new int[KEYS.length];
        for (int i = 0; i < layer.length; i++) if (owner[i] >= 0 && (layer[i] >>> 24) != 0) counts[owner[i]]++;
        int[][] out = new int[KEYS.length][];
        for (int s = 0; s < KEYS.length; s++) out[s] = new int[counts[s]];
        int[] fill = new int[KEYS.length];
        for (int i = 0; i < layer.length; i++) if (owner[i] >= 0 && (layer[i] >>> 24) != 0) out[owner[i]][fill[owner[i]]++] = i;
        return out;
    }

    /** One school's liquid cells as layout indices, lowest on screen first. */
    public int[] liquid(int school) {
        return liquid[school];
    }

    public int mouthX(int school) {
        return mouthX[school];
    }

    public int mouthY(int school) {
        return mouthY[school];
    }

    public interface PixelSink {
        void accept(int x, int y, int argb);
    }

    private static int school(int argb) {
        int best = 0;
        long bestDistance = Long.MAX_VALUE;
        for (int s = 0; s < KEYS.length; s++) {
            long d = 0;
            for (int shift = 0; shift < 24; shift += 8) {
                long c = ((argb >> shift) & 0xFF) - ((KEYS[s] >> shift) & 0xFF);
                d += c * c;
            }
            if (d < bestDistance) {
                bestDistance = d;
                best = s;
            }
        }
        return best;
    }

    /** Each opaque art pixel and each seed takes the school of the nearest seed within reach. */
    private static int[] nearestOwner(int width, int height, int[] art, int[] seed) {
        int n = width * height;
        int[] owner = new int[n];
        Arrays.fill(owner, -1);
        int[] distance = new int[n];
        Arrays.fill(distance, Integer.MAX_VALUE);
        int[] queue = new int[n];
        int head = 0, tail = 0;
        for (int i = 0; i < n; i++) {
            if (seed[i] < 0) continue;
            owner[i] = seed[i];
            distance[i] = 0;
            queue[tail++] = i;
        }
        while (head < tail) {
            int i = queue[head++], x = i % width, y = i / width;
            if (distance[i] >= REACH) continue;
            for (int next : new int[]{x > 0 ? i - 1 : -1, x < width - 1 ? i + 1 : -1, y > 0 ? i - width : -1,
                    y < height - 1 ? i + width : -1}) {
                if (next < 0 || distance[next] != Integer.MAX_VALUE) continue;
                distance[next] = distance[i] + 1;
                owner[next] = owner[i];
                queue[tail++] = next;
            }
        }
        for (int i = 0; i < n; i++) {
            if (seed[i] < 0 && (art[i] >>> 24) == 0) owner[i] = -1;
        }
        return owner;
    }
}
