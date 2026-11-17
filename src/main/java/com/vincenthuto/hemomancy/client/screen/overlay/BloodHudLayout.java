package com.vincenthuto.hemomancy.client.screen.overlay;

import java.util.ArrayList;
import java.util.List;

/**
 * Art-space geometry of the living hematic HUD for one set of visible parts: the vessel (drawn 1:1 at 64×128), an
 * equipped reserve grafted on the inward flank, the Morphling jar above it, and the eight Temper vials. Unmirrored,
 * the HUD sits on the left and its inward side faces right; {@code mirrored} moves every position for the right-hand
 * corners while textures stay unflipped.
 */
public record BloodHudLayout(boolean blood, Reserve reserve, boolean temper, boolean jar, boolean mirrored) {
    public static final float SCALE = 0.6F;
    public static final int MARGIN = 8;
    public static final int LABEL_BAND = 16;
    public static final int VESSEL_W = 64;
    public static final int VESSEL_H = 128;
    public static final int JAR_W = 28;
    public static final int JAR_H = 40;
    /** The jar's glass, relative to the jar, where the Morphling sits. */
    public static final Rect JAR_WINDOW = new Rect(4, 14, 20, 20);
    /** How far a vial's sprite can reach from its mouth. */
    public static final int VIAL_REACH = 14;

    public enum Reserve {
        NONE(0), GOURD(64), HORN(48), RIB(64);

        private final int height;

        Reserve(int height) {
            this.height = height;
        }

        public int height() {
            return height;
        }
    }

    public record Rect(int x, int y, int w, int h) {
        Rect shift(int dx, int dy) {
            return new Rect(x + dx, y + dy, w, h);
        }
    }

    /**
     * One Temper vial. {@code entryX} is where its trickle enters the glass, or -1 with no vessel to feed;
     * {@code fitRow} is the row whose binding the vial is seated on once the binding is grown, or -1.
     */
    public record VialSeat(int school, int mouthX, int mouthY, boolean pointsRight, int entryX, int entryY, int fitRow) {
    }

    private record Geometry(int width, int height, Rect vessel, Rect reserveRect, Rect jarRect, Rect spine,
                            List<int[][]> grafts, List<VialSeat> vials) {
    }

    /**
     * The state's file name under {@code textures/gui/blood_hud/layouts/}, e.g. {@code vessel_gourd_vials_jar_right}:
     * the parts shown, then {@code _right} for the mirrored right-hand corners.
     */
    public String fileName() {
        StringBuilder name = new StringBuilder();
        if (blood) name.append("vessel_");
        if (reserve != Reserve.NONE) name.append(reserve.name().toLowerCase(java.util.Locale.ROOT)).append('_');
        if (temper) name.append("vials_");
        if (jar) name.append("jar_");
        if (name.isEmpty()) name.append("empty_");
        name.setLength(name.length() - 1);
        return mirrored ? name + "_right" : name.toString();
    }

    /** One more than the largest {@link #code()}. */
    public static final int CODES = 64;
    private static final Geometry[] GEOMETRY = new Geometry[CODES];

    private Geometry geometry() {
        // A benign race: two threads may both compute the same immutable geometry.
        Geometry cached = GEOMETRY[code()];
        if (cached == null) GEOMETRY[code()] = cached = compute();
        return cached;
    }

    /** A small integer naming this exact state, for array-indexed caches with no per-frame allocation. */
    public int code() {
        return (blood ? 1 : 0) | (temper ? 2 : 0) | (jar ? 4 : 0) | (mirrored ? 8 : 0) | reserve.ordinal() << 4;
    }

    private Geometry compute() {
        Rect vessel = null, reserveRect = null, jarRect = null, spine = null;
        List<int[][]> grafts = new ArrayList<>();
        List<VialSeat> vials = new ArrayList<>();
        int h = reserve.height();
        if (blood) {
            vessel = new Rect(0, 0, VESSEL_W, VESSEL_H);
            if (reserve != Reserve.NONE) {
                int rx = 70, ry = 130 - h;
                reserveRect = new Rect(rx, ry, 32, h);
                grafts.add(new int[][]{{61, ry + 22}, {rx + 4, ry + 26}, {rx + 5, ry + h - 2}, {46, 127}});
            }
            if (jar) {
                jarRect = new Rect(72, 12, JAR_W, JAR_H);
                grafts.add(new int[][]{{60, 24}, {75, 26}, {75, 46}, {60, 50}});
                // With a reserve below, the jar and reserve graft into one column; a channel between them reads as a gap.
                if (reserveRect != null) {
                    grafts.add(new int[][]{{60, 44}, {97, 46}, {99, reserveRect.y() + 10}, {61, reserveRect.y() + 24}});
                }
            }
            if (temper) for (int i = 0; i < 4; i++) {
                int row = 36 + 20 * i;
                vials.add(new VialSeat(i, 6, row, true, 23, row + 8, -1));
                vials.add(new VialSeat(7 - i, 57, row, false, 40, row + 8, -1));
            }
        } else if (reserve != Reserve.NONE) {
            int ry = jar ? 46 : 0;
            reserveRect = new Rect(0, ry, 32, h);
            if (jar) jarRect = new Rect(2, 0, JAR_W, JAR_H);
            if (temper) for (int i = 0; i < 4; i++) {
                int row = ry + (h == 48 ? 4 + 12 * i : 6 + 14 * i);
                vials.add(new VialSeat(i, -7, row, true, -1, -1, row + 4));
                vials.add(new VialSeat(7 - i, 39, row, false, -1, -1, row + 4));
            }
        } else if (temper) {
            int top = jar ? 44 : 0;
            spine = new Rect(-3, top, 7, 62);
            if (jar) jarRect = new Rect(-14, 0, JAR_W, JAR_H);
            for (int i = 0; i < 4; i++) {
                int row = top + 8 + 16 * i;
                vials.add(new VialSeat(i, -4, row, true, -1, -1, -1));
                vials.add(new VialSeat(7 - i, 4, row, false, -1, -1, -1));
            }
        } else if (jar) {
            jarRect = new Rect(0, 0, JAR_W, JAR_H);
        }
        if (vessel == null && reserveRect == null && jarRect == null && spine == null) {
            return new Geometry(0, 0, null, null, null, null, List.of(), List.of());
        }

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        for (Rect r : new Rect[]{vessel, reserveRect, jarRect, spine}) {
            if (r == null) continue;
            minX = Math.min(minX, r.x);
            minY = Math.min(minY, r.y);
            maxX = Math.max(maxX, r.x + r.w);
            maxY = Math.max(maxY, r.y + r.h);
        }
        for (VialSeat v : vials) {
            minX = Math.min(minX, v.mouthX - VIAL_REACH);
            maxX = Math.max(maxX, v.mouthX + VIAL_REACH);
            minY = Math.min(minY, v.mouthY - VIAL_REACH);
            maxY = Math.max(maxY, v.mouthY + VIAL_REACH);
        }
        int dx = MARGIN - minX, dy = MARGIN - minY;
        int width = maxX - minX + 2 * MARGIN;
        int height = maxY - minY + 2 * MARGIN + (vessel != null || reserveRect != null ? LABEL_BAND : 0);

        Rect[] rects = {vessel, reserveRect, jarRect, spine};
        for (int i = 0; i < rects.length; i++) {
            if (rects[i] == null) continue;
            Rect r = rects[i].shift(dx, dy);
            rects[i] = mirrored ? new Rect(width - r.x - r.w, r.y, r.w, r.h) : r;
        }
        List<int[][]> placedGrafts = new ArrayList<>();
        for (int[][] polygon : grafts) {
            int[][] placed = new int[polygon.length][];
            for (int i = 0; i < polygon.length; i++) {
                int x = polygon[i][0] + dx;
                placed[i] = new int[]{mirrored ? width - x : x, polygon[i][1] + dy};
            }
            placedGrafts.add(placed);
        }
        List<VialSeat> placedVials = new ArrayList<>();
        for (VialSeat v : vials) {
            int mouthX = v.mouthX + dx, entryX = v.entryX < 0 ? -1 : v.entryX + dx;
            if (mirrored) {
                mouthX = width - 1 - mouthX;
                if (entryX >= 0) entryX = width - 1 - entryX;
            }
            placedVials.add(new VialSeat(v.school, mouthX, v.mouthY + dy, v.pointsRight != mirrored, entryX,
                    v.entryY < 0 ? -1 : v.entryY + dy, v.fitRow < 0 ? -1 : v.fitRow + dy));
        }
        return new Geometry(width, height, rects[0], rects[1], rects[2], rects[3], List.copyOf(placedGrafts),
                List.copyOf(placedVials));
    }

    public int parts() {
        return (blood ? 1 : 0) | (reserve != Reserve.NONE ? 2 : 0) | (temper ? 4 : 0) | (jar ? 8 : 0);
    }

    public boolean empty() {
        return width() == 0;
    }

    public boolean gourd() {
        return reserve != Reserve.NONE;
    }

    public int width() {
        return geometry().width;
    }

    public int height() {
        return geometry().height;
    }

    public Rect vessel() {
        return geometry().vessel;
    }

    public Rect reserveRect() {
        return geometry().reserveRect;
    }

    public Rect jarRect() {
        return geometry().jarRect;
    }

    public Rect spine() {
        return geometry().spine;
    }

    public List<int[][]> graftPolygons() {
        return geometry().grafts;
    }

    public List<VialSeat> vials() {
        return geometry().vials;
    }

    public int degreeLabelY() {
        return vessel().y() + VESSEL_H + MARGIN;
    }

    public int volumeLabelY() {
        return degreeLabelY() + 7;
    }

    public int reserveLabelY() {
        Rect r = reserveRect();
        return r.y() + r.h() + MARGIN;
    }

    /** Where the bond meter hangs beside the jar: its left edge unmirrored, its right edge mirrored. */
    public int jarMeterX() {
        Rect j = jarRect();
        return mirrored ? j.x() - 3 : j.x() + j.w() + 3;
    }

    public int jarMeterY() {
        return jarRect().y() + 2;
    }

    public String key() {
        return (blood ? "b" : "-") + reserve.name().charAt(0) + (temper ? "t" : "-") + (jar ? "j" : "-") + (mirrored ? "M" : "");
    }

    /**
     * Fit the entire assembly inside the selected corner, above the hotbar and below status icons. In a bottom corner
     * on a narrow screen it also rises clear of the health and armour rows when it reaches over them. The scale is
     * snapped so every art pixel covers a whole number of screen pixels at {@code guiScale}; a fractional size, which
     * draws thin lattice and tendril as uneven grain, is the last resort for a screen too small for one pixel each.
     */
    public Placement place(int screenWidth, int screenHeight, boolean left, boolean top, boolean statusIcons, int guiScale) {
        int topInset = !left && statusIcons ? 30 : 4;
        Placement placement = placeAt(screenWidth, screenHeight, left, top, topInset, 30, guiScale);
        float x0 = placement.x(), x1 = placement.screenX(width());
        if (!top && x1 > screenWidth / 2F - 91 && x0 < screenWidth / 2F + 91) {
            placement = placeAt(screenWidth, screenHeight, left, false, topInset, 52, guiScale);
        }
        return placement;
    }

    private Placement placeAt(int screenWidth, int screenHeight, boolean left, boolean top, int topInset, int bottomInset,
                              int guiScale) {
        int gui = Math.max(1, guiScale);
        float scale = Math.max(1, Math.round(SCALE * gui)) / (float) gui;
        if (!empty()) {
            float fit = Math.min((screenWidth - 8F) / width(), (screenHeight - topInset - bottomInset) / (float) height());
            if (scale > fit) {
                int whole = (int) Math.floor(fit * gui);
                scale = whole >= 1 ? whole / (float) gui : Math.max(0.01F, fit);
            }
        }
        float x = left ? 4 : screenWidth - 4 - width() * scale;
        float y = top ? topInset : screenHeight - bottomInset - height() * scale;
        return new Placement(Math.round(x * gui) / (float) gui, Math.round(y * gui) / (float) gui, scale);
    }

    public record Placement(float x, float y, float scale) {
        public float localX(double mouseX) { return (float) ((mouseX - x) / scale); }
        public float localY(double mouseY) { return (float) ((mouseY - y) / scale); }
        public float screenX(float localX) { return x + localX * scale; }
        public float screenY(float localY) { return y + localY * scale; }
    }
}
