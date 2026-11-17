package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

/** Which pixels of a HUD part's art are solid, as read from its texture's alpha. */
public record HudMask(int width, int height, boolean[] solid) {
    public HudMask {
        if (solid.length != width * height) throw new IllegalArgumentException("mask size " + solid.length + " != " + width + "x" + height);
    }

    public boolean at(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && solid[y * width + x];
    }

    public static HudMask fromArgb(int width, int height, int[] argb, int minAlpha) {
        boolean[] solid = new boolean[width * height];
        for (int i = 0; i < solid.length; i++) solid[i] = (argb[i] >>> 24) >= minAlpha;
        return new HudMask(width, height, solid);
    }

    public static HudMask union(HudMask a, HudMask b) {
        if (a.width != b.width || a.height != b.height) throw new IllegalArgumentException("masks differ in size");
        boolean[] solid = new boolean[a.solid.length];
        for (int i = 0; i < solid.length; i++) solid[i] = a.solid[i] || b.solid[i];
        return new HudMask(a.width, a.height, solid);
    }
}
