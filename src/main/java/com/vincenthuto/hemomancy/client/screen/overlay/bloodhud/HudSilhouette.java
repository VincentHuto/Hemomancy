package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

/**
 * The union outline the binding grows around: every visible part's art, plus graft cells (the lattice-filled gaps
 * between parts and the vials' spine), which count as solid for the outline and are painted with lattice.
 */
public record HudSilhouette(int width, int height, boolean[] solid, boolean[] graft) {
    public static HudSilhouette of(int width, int height) {
        return new HudSilhouette(width, height, new boolean[width * height], new boolean[width * height]);
    }

    public void stamp(HudMask mask, int x, int y) {
        for (int row = 0; row < mask.height(); row++) for (int col = 0; col < mask.width(); col++) {
            if (mask.solid()[row * mask.width() + col] && inside(x + col, y + row)) solid[(y + row) * width + x + col] = true;
        }
    }

    /** Fills the polygon's open cells with graft lattice; cells already covered by a part are left to it. */
    public void fillPolygon(int[][] points) {
        for (int y = 0; y < height; y++) for (int x = 0; x < width; x++) {
            int i = y * width + x;
            if (!solid[i] && contains(points, x + 0.5, y + 0.5)) {
                solid[i] = true;
                graft[i] = true;
            }
        }
    }

    public void fillRect(int x, int y, int w, int h) {
        for (int row = y; row < y + h; row++) for (int col = x; col < x + w; col++) {
            if (!inside(col, row)) continue;
            solid[row * width + col] = true;
            graft[row * width + col] = true;
        }
    }

    private boolean inside(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    private static boolean contains(int[][] p, double x, double y) {
        boolean in = false;
        for (int i = 0, j = p.length - 1; i < p.length; j = i++) {
            if ((p[i][1] > y) != (p[j][1] > y)
                    && x < (double) (p[j][0] - p[i][0]) * (y - p[i][1]) / (p[j][1] - p[i][1]) + p[i][0]) in = !in;
        }
        return in;
    }
}
