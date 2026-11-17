package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Rect;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.VialSeat;

import java.util.ArrayList;
import java.util.List;

/** One layout made whole: its silhouette, its grown binding and its vials seated on it. */
public final class HudAssembly {
    private static final double TILT = Math.toRadians(50);

    private final BloodHudLayout layout;
    private final TemperVialArt vialArt;
    private final BakedVials bakedVials;
    private final HudBinding binding;
    private final List<VialSeat> vials;
    private final int windowTop;
    private final int windowBottom;
    private final int[][][] streamPaths = new int[8][][];
    private final int[] streamSurfaces = new int[8];

    private HudAssembly(BloodHudLayout layout, TemperVialArt vialArt, BakedVials bakedVials, HudBinding binding,
                        List<VialSeat> vials, int windowTop, int windowBottom) {
        this.layout = layout;
        this.vialArt = vialArt;
        this.bakedVials = bakedVials;
        this.binding = binding;
        this.vials = vials;
        this.windowTop = windowTop;
        this.windowBottom = windowBottom;
    }

    public static HudAssembly build(BloodHudLayout layout, HudMasks masks) {
        HudSilhouette silhouette = HudSilhouette.of(Math.max(1, layout.width()), Math.max(1, layout.height()));
        stamp(silhouette, masks.vessel(), layout.vessel());
        stamp(silhouette, masks.reserve(layout.reserve()), layout.reserveRect());
        stamp(silhouette, masks.jar(), layout.jarRect());
        Rect spine = layout.spine();
        if (spine != null) silhouette.fillRect(spine.x(), spine.y(), spine.w(), spine.h());
        for (int[][] polygon : layout.graftPolygons()) silhouette.fillPolygon(polygon);
        HudBinding grown = HudBinding.build(silhouette, masks.binding());
        BindingTile baked = masks.bakedLayouts().apply(layout.fileName());
        HudBinding binding = baked == null ? grown : grown.withLayers(baked);

        List<VialSeat> vials = new ArrayList<>();
        for (VialSeat seat : layout.vials()) {
            if (seat.fitRow() < 0) {
                vials.add(seat);
                continue;
            }
            // Seat the neck on the band's outer edge so the glass stands clear of the reserve's art.
            int x = binding.seatX(seat.fitRow(), seat.pointsRight(), HudBinding.RADIUS + HudBinding.HALF);
            if (x < 0) {
                vials.add(seat);
                continue;
            }
            double ax = seat.pointsRight() ? -Math.sin(TILT) : Math.sin(TILT), ay = -Math.cos(TILT);
            vials.add(new VialSeat(seat.school(), (int) Math.round(x - ax * 2), (int) Math.round(seat.fitRow() - ay * 2),
                    seat.pointsRight(), -1, -1, seat.fitRow()));
        }

        int top = Integer.MAX_VALUE, bottom = -1;
        HudMask window = masks.vesselWindow();
        for (int y = 0; y < window.height(); y++) for (int x = 0; x < window.width(); x++) {
            if (!window.at(x, y)) continue;
            top = Math.min(top, y);
            bottom = Math.max(bottom, y);
        }
        BakedVials placed = layout.temper() ? masks.bakedVials().apply(layout.fileName()) : null;
        if (placed != null && (placed.width() != layout.width() || placed.height() != layout.height())) placed = null;
        if (placed != null) {
            // Hand-placed mouths move each trickle's start; the entry into the glass stays the layout's.
            for (int i = 0; i < vials.size(); i++) {
                VialSeat seat = vials.get(i);
                if (placed.mouthX(seat.school()) < 0) continue;
                vials.set(i, new VialSeat(seat.school(), placed.mouthX(seat.school()), placed.mouthY(seat.school()),
                        seat.pointsRight(), seat.entryX(), seat.entryY(), seat.fitRow()));
            }
        }
        return new HudAssembly(layout, masks.vials(), placed, binding, List.copyOf(vials), top, bottom);
    }

    private static void stamp(HudSilhouette silhouette, HudMask mask, Rect rect) {
        if (mask != null && rect != null) silhouette.stamp(mask, rect.x(), rect.y());
    }

    public BloodHudLayout layout() {
        return layout;
    }

    public TemperVialArt vialArt() {
        return vialArt;
    }

    /** This state's hand-placed vials, or null when they are drawn from the shared sprites. */
    public BakedVials bakedVials() {
        return bakedVials;
    }

    public HudBinding binding() {
        return binding;
    }

    public List<VialSeat> vials() {
        return vials;
    }

    /** The school whose vial covers this art-space point, or -1. */
    public int schoolAt(float x, float y) {
        if (bakedVials != null) return bakedVials.schoolAt((int) Math.floor(x), (int) Math.floor(y));
        for (VialSeat seat : vials) {
            var sprite = vialArt.sprite(seat.pointsRight());
            int px = (int) Math.floor(x) - seat.mouthX(), py = (int) Math.floor(y) - seat.mouthY();
            for (int i = 0; i < sprite.dx().length; i++) {
                if (sprite.dx()[i] == px && sprite.dy()[i] == py) return seat.school();
            }
        }
        return -1;
    }

    /** {@link TemperStream#path}, rebuilt only when the blood's surface moves. */
    public int[][] streamPath(VialSeat seat, int surfaceY) {
        int school = seat.school();
        if (streamPaths[school] == null || streamSurfaces[school] != surfaceY) {
            streamPaths[school] = TemperStream.path(seat, surfaceY);
            streamSurfaces[school] = surfaceY;
        }
        return streamPaths[school];
    }

    /** The art-space row of the blood's surface in the vessel at this fill ratio. */
    public int vesselSurfaceY(double ratio) {
        int rows = windowBottom - windowTop + 1;
        int filled = (int) Math.round(rows * Math.max(0, Math.min(1, ratio)));
        return layout.vessel().y() + windowBottom + 1 - filled;
    }
}
