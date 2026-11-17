package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The living binding: a thin band of crossed hematic-iron struts with red tendril between them, grown around a
 * {@link HudSilhouette} from the editable tiles of a {@link BindingArt}. The red is held back below the degree line,
 * studs stay grey until the red reaches them, and the heartbeat rises from the base, as up the frame's veins.
 */
public final class HudBinding {
    public static final int HALF = 3;
    public static final int RADIUS = 4;
    public static final int COLLAR_SPACING = 44;
    private static final float STEP = 0.35f;
    private static final int OUTLINE = 0, RED = 1, IRON = 2, COLLAR = 3;

    private final int width;
    private final int height;
    private final BindingArt art;
    private final float[] dist;
    private final int[] priority;
    private final int[] painted;
    private final int[] dormant;
    private final int[] apotheos;
    private final boolean[] red;
    private float[] heightFrac;
    private float length;
    private int collars;

    private HudBinding(int width, int height, BindingArt art, float[] dist) {
        this.width = width;
        this.height = height;
        this.art = art;
        this.dist = dist;
        int n = width * height;
        priority = new int[n];
        Arrays.fill(priority, -1);
        painted = new int[n];
        dormant = new int[n];
        apotheos = new int[n];
        red = new boolean[n];
    }

    public static HudBinding build(HudSilhouette silhouette, BindingArt art) {
        HudBinding binding = new HudBinding(silhouette.width(), silhouette.height(), art, distanceField(silhouette));
        binding.paintGraft(silhouette);
        for (List<Point> contour : binding.contours()) binding.paintContour(contour);
        binding.backTrappedSky(silhouette);
        binding.measureHeights();
        return binding;
    }

    /**
     * This binding's geometry (seats, length, collars) painted with a hand-edited baked layout instead of the tiles.
     * Opaque pixels in either layer are binding; pixels whose dormant colour differs are red, as with the tiles.
     */
    public HudBinding withLayers(BindingTile baked) {
        if (baked.width() != width || baked.height() != height) return this;
        HudBinding out = new HudBinding(width, height, art, dist);
        for (int i = 0; i < priority.length; i++) {
            if (baked.empty(i)) continue;
            out.priority[i] = IRON;
            out.painted[i] = baked.art()[i];
            out.dormant[i] = baked.dormant()[i];
            out.apotheos[i] = baked.apotheos()[i];
            out.red[i] = baked.red(i);
        }
        out.length = length;
        out.collars = collars;
        out.measureHeights();
        return out;
    }

    /** The three layers as painted now, for baking: as revealed, dormant and Apotheos. */
    public BindingTile layers() {
        int[] art = new int[priority.length], dorm = new int[priority.length], apo = new int[priority.length];
        for (int i = 0; i < priority.length; i++) {
            if (priority[i] < 0) continue;
            art[i] = painted[i];
            dorm[i] = dormant[i];
            apo[i] = apotheos[i];
        }
        return new BindingTile(width, height, art, dorm, apo);
    }

    public int width() {
        return width;
    }

    public int height() {
        return height;
    }

    public float length() {
        return length;
    }

    public int collarCount() {
        return collars;
    }

    /** ARGB pixels at this degree; 0 where the binding is absent. Degree 8 with {@code apotheos} shows the gold. */
    public int[] paint(int degree, boolean apotheos) {
        int[] out = new int[width * height];
        for (int i = 0; i < out.length; i++) {
            if (priority[i] < 0) continue;
            out[i] = red[i] && !revealed(i, degree) ? dormant[i] : apotheos ? this.apotheos[i] : painted[i];
        }
        return out;
    }

    /** White on the revealed red whose height falls in {@code band}; the renderer tints it toward deep blood. */
    public int[] beatBand(int degree, int band, int bands) {
        int[] out = new int[width * height];
        for (int i = 0; i < out.length; i++) {
            if (priority[i] < 0 || !red[i] || !revealed(i, degree) || (painted[i] >>> 24) == 0) continue;
            if (Math.min(bands - 1, (int) (heightFrac[i] * bands)) == band) out[i] = 0xFFFFFFFF;
        }
        return out;
    }

    /** The first column on {@code row}, from the left or right edge, that lies on the binding's middle line. */
    public int seatX(int row, boolean fromLeft) {
        return seatX(row, fromLeft, RADIUS);
    }

    /** The first column on {@code row}, from that edge, within {@code radius} of the wrapped parts. */
    public int seatX(int row, boolean fromLeft, int radius) {
        if (row < 0 || row >= height) return -1;
        for (int k = 0; k < width; k++) {
            int x = fromLeft ? k : width - 1 - k;
            if (dist[row * width + x] <= radius) return x;
        }
        return -1;
    }

    private boolean revealed(int i, int degree) {
        return degree >= 7 || (degree > 0 && heightFrac[i] <= degree / 7f);
    }

    /** Copies one texel of {@code tile} to (x, y) unless a higher-priority texel is already there. */
    private void put(int x, int y, BindingTile tile, int texel, int pri) {
        if (x < 0 || y < 0 || x >= width || y >= height || tile.empty(texel)) return;
        int i = y * width + x;
        if (pri < priority[i]) return;
        priority[i] = pri;
        painted[i] = tile.art()[texel];
        dormant[i] = tile.dormant()[texel];
        apotheos[i] = tile.apotheos()[texel];
        red[i] = tile.red(texel);
    }

    /** Iron over red, red over the outline, so crossing samples keep the struts whole. */
    private static int priorityOf(BindingTile tile, int texel, boolean edgeRow) {
        if (edgeRow) return OUTLINE;
        return tile.red(texel) ? RED : IRON;
    }

    private void paintGraft(HudSilhouette s) {
        for (int i = 0; i < s.graft().length; i++) if (s.graft()[i]) paintLattice(i % width, i / width);
    }

    /**
     * Sky the band encloses on every side (the crown's openwork, pinholes where the frame's edge meets the band,
     * pockets between grafted parts) is backed with the same lattice; dips still open to the outside stay open.
     */
    private void backTrappedSky(HudSilhouette s) {
        boolean[] open = new boolean[width * height];
        int[] queue = new int[width * height];
        int head = 0, tail = 0;
        for (int i = 0; i < open.length; i++) {
            int x = i % width, y = i / width;
            boolean edge = x == 0 || y == 0 || x == width - 1 || y == height - 1;
            if (edge && priority[i] < 0 && !s.solid()[i]) {
                open[i] = true;
                queue[tail++] = i;
            }
        }
        while (head < tail) {
            int i = queue[head++], x = i % width, y = i / width;
            for (int next : new int[]{x > 0 ? i - 1 : -1, x < width - 1 ? i + 1 : -1, y > 0 ? i - width : -1,
                    y < height - 1 ? i + width : -1}) {
                if (next < 0 || open[next] || priority[next] >= 0 || s.solid()[next]) continue;
                open[next] = true;
                queue[tail++] = next;
            }
        }
        for (int i = 0; i < open.length; i++) {
            if (!open[i] && priority[i] < 0 && !s.solid()[i]) paintLattice(i % width, i / width);
        }
    }

    /** The lattice tile repeats in screen space, so neighbouring fills line up. */
    private void paintLattice(int x, int y) {
        BindingTile lattice = art.lattice();
        int texel = lattice.index(x, y);
        put(x, y, lattice, texel, priorityOf(lattice, texel, false));
    }

    private void paintContour(List<Point> pts) {
        float total = pts.get(pts.size() - 1).s + STEP;
        length += total;
        BindingTile band = art.band();
        float half = band.height() / 2f;
        for (Point p : pts) {
            int column = Math.floorMod((int) Math.floor(p.s), band.width());
            for (float o = -half; o < half; o += 0.3f) {
                int x = (int) Math.floor(p.x + p.nx * o), y = (int) Math.floor(p.y + p.ny * o);
                int row = Math.min(band.height() - 1, (int) Math.floor(o + half));
                int texel = band.index(column, row);
                put(x, y, band, texel, priorityOf(band, texel, row == 0 || row == band.height() - 1));
            }
        }
        int count = Math.max(2, Math.round(total / COLLAR_SPACING));
        collars += count;
        BindingTile collar = art.collar();
        for (int k = 0; k < count; k++) {
            Point p = pts.get(Math.min(pts.size() - 1, Math.round(k * total / count / STEP)));
            int cx = (int) Math.floor(p.x) - collar.width() / 2, cy = (int) Math.floor(p.y) - collar.height() / 2;
            for (int ty = 0; ty < collar.height(); ty++) for (int tx = 0; tx < collar.width(); tx++) {
                put(cx + tx, cy + ty, collar, ty * collar.width() + tx, COLLAR);
            }
        }
    }

    private void measureHeights() {
        int minY = height, maxY = -1;
        for (int i = 0; i < priority.length; i++) {
            if (priority[i] < 0) continue;
            minY = Math.min(minY, i / width);
            maxY = Math.max(maxY, i / width);
        }
        heightFrac = new float[priority.length];
        if (maxY < 0) return;
        for (int i = 0; i < priority.length; i++) {
            heightFrac[i] = maxY == minY ? 0 : (maxY - i / width) / (float) (maxY - minY);
        }
    }

    private record Point(float x, float y, float s, float nx, float ny) {
    }

    /** One traced, smoothed and resampled ring per separate island of the outline. */
    private List<List<Point>> contours() {
        List<List<Point>> out = new ArrayList<>();
        boolean[] claimed = new boolean[width * height];
        for (int start = 0; start < claimed.length; start++) {
            if (claimed[start] || !near(start % width, start / width)) continue;
            claim(start, claimed);
            List<int[]> ring = trace(start % width, start / width);
            if (ring.size() >= 8) out.add(resample(ring));
        }
        return out;
    }

    private boolean near(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height && dist[y * width + x] <= RADIUS;
    }

    private void claim(int start, boolean[] claimed) {
        int[] stack = new int[width * height];
        int top = 0;
        stack[top++] = start;
        claimed[start] = true;
        while (top > 0) {
            int i = stack[--top], x = i % width, y = i / width;
            for (int dy = -1; dy <= 1; dy++) for (int dx = -1; dx <= 1; dx++) {
                int nx = x + dx, ny = y + dy;
                if (!near(nx, ny) || claimed[ny * width + nx]) continue;
                claimed[ny * width + nx] = true;
                stack[top++] = ny * width + nx;
            }
        }
    }

    private static final int[][] MOORE = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    private List<int[]> trace(int sx, int sy) {
        List<int[]> ring = new ArrayList<>();
        ring.add(new int[]{sx, sy});
        int x = sx, y = sy, d = 0;
        for (int guard = 0; guard < width * height * 2; guard++) {
            boolean found = false;
            for (int i = 0; i < 8; i++) {
                int k = (d + 5 + i) % 8, nx = x + MOORE[k][0], ny = y + MOORE[k][1];
                if (near(nx, ny)) {
                    x = nx;
                    y = ny;
                    d = k;
                    found = true;
                    break;
                }
            }
            if (!found || (x == sx && y == sy && ring.size() > 2)) break;
            ring.add(new int[]{x, y});
        }
        return ring;
    }

    private List<Point> resample(List<int[]> ring) {
        int m = ring.size();
        float[][] smooth = new float[m][2];
        for (int i = 0; i < m; i++) {
            float sx = 0, sy = 0;
            for (int j = -4; j <= 4; j++) {
                int[] p = ring.get(Math.floorMod(i + j, m));
                sx += p[0];
                sy += p[1];
            }
            smooth[i][0] = sx / 9 + 0.5f;
            smooth[i][1] = sy / 9 + 0.5f;
        }
        List<Point> out = new ArrayList<>();
        float acc = 0;
        for (int i = 0; i < m; i++) {
            float[] a = smooth[i], b = smooth[(i + 1) % m];
            float len = (float) Math.hypot(b[0] - a[0], b[1] - a[1]);
            if (len <= 0) continue;
            float tx = (b[0] - a[0]) / len, ty = (b[1] - a[1]) / len;
            for (float t = 0; t < len; t += STEP) {
                float px = a[0] + tx * t, py = a[1] + ty * t;
                float nx = -ty, ny = tx;
                int ox = Math.round(px + nx * 2), oy = Math.round(py + ny * 2);
                int cx = Math.min(width - 1, Math.max(0, Math.round(px))), cy = Math.min(height - 1, Math.max(0, Math.round(py)));
                if (ox < 0 || oy < 0 || ox >= width || oy >= height || dist[oy * width + ox] < dist[cy * width + cx]) {
                    nx = -nx;
                    ny = -ny;
                }
                out.add(new Point(px, py, acc + t, nx, ny));
            }
            acc += len;
        }
        return out;
    }

    private static float[] distanceField(HudSilhouette s) {
        int w = s.width(), h = s.height();
        float[] d = new float[w * h];
        for (int i = 0; i < d.length; i++) d[i] = s.solid()[i] ? 0 : 1e9f;
        float r2 = (float) Math.sqrt(2);
        int[][] forward = {{-1, 0}, {0, -1}, {-1, -1}, {1, -1}};
        int[][] backward = {{1, 0}, {0, 1}, {1, 1}, {-1, 1}};
        for (int pass = 0; pass < 3; pass++) {
            boolean fwd = pass != 1;
            int[][] nb = fwd ? forward : backward;
            for (int k = 0; k < w * h; k++) {
                int i = fwd ? k : w * h - 1 - k, x = i % w, y = i / w;
                float best = d[i];
                for (int[] o : nb) {
                    int nx = x + o[0], ny = y + o[1];
                    if (nx < 0 || ny < 0 || nx >= w || ny >= h) continue;
                    best = Math.min(best, d[ny * w + nx] + (o[0] != 0 && o[1] != 0 ? r2 : 1));
                }
                d[i] = best;
            }
        }
        return d;
    }



}
