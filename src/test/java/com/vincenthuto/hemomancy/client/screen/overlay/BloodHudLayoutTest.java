package com.vincenthuto.hemomancy.client.screen.overlay;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Reserve;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloodHudLayoutTest {
    private static List<BloodHudLayout> every(boolean mirrored) {
        List<BloodHudLayout> out = new ArrayList<>();
        for (int bits = 1; bits < 16; bits++) for (Reserve reserve : Reserve.values()) {
            if (((bits & 2) != 0) == (reserve == Reserve.NONE)) continue;
            out.add(new BloodHudLayout((bits & 1) != 0, reserve, (bits & 4) != 0, (bits & 8) != 0, mirrored));
        }
        return out;
    }

    @Test
    void mirroringMovesTheGeometryButKeepsTheSize() {
        for (BloodHudLayout layout : every(false)) {
            BloodHudLayout mirror = new BloodHudLayout(layout.blood(), layout.reserve(), layout.temper(), layout.jar(), true);
            assertEquals(layout.width(), mirror.width(), layout.key());
            assertEquals(layout.height(), mirror.height(), layout.key());
            if (layout.blood()) assertEquals(layout.width() - layout.vessel().x() - 64, mirror.vessel().x());
            if (layout.jar()) assertEquals(layout.width() - layout.jarRect().x() - layout.jarRect().w(), mirror.jarRect().x());
            for (int i = 0; i < layout.vials().size(); i++) {
                var a = layout.vials().get(i);
                var b = mirror.vials().get(i);
                assertEquals(a.school(), b.school());
                assertNotEquals(a.pointsRight(), b.pointsRight());
                assertEquals(layout.width() - 1 - a.mouthX(), b.mouthX());
                assertEquals(a.mouthY(), b.mouthY());
            }
            assertNotEquals(layout.key(), mirror.key());
        }
    }

    @Test
    void theReserveAndJarSitOnTheInwardFlank() {
        for (Reserve reserve : new Reserve[]{Reserve.GOURD, Reserve.HORN, Reserve.RIB}) {
            var layout = new BloodHudLayout(true, reserve, true, true, false);
            assertTrue(layout.reserveRect().x() > layout.vessel().x() + 32);
            assertTrue(layout.jarRect().x() > layout.vessel().x() + 32);
            assertTrue(layout.jarRect().y() + layout.jarRect().h() + 12 <= layout.reserveRect().y(),
                    "the jar sits above the reserve with room for a dip");
            assertEquals(3, layout.graftPolygons().size(), "reserve graft, jar graft and the bridge between them");
            var mirrored = new BloodHudLayout(true, reserve, true, true, true);
            assertTrue(mirrored.reserveRect().x() + mirrored.reserveRect().w() < mirrored.vessel().x() + 32);
        }
    }

    @Test
    void vialsNeverCoverTheBloodWindow() {
        for (boolean mirrored : new boolean[]{false, true}) for (BloodHudLayout layout : every(mirrored)) {
            if (!layout.blood() || !layout.temper()) continue;
            int left = layout.vessel().x() + 22, right = layout.vessel().x() + 41;
            for (var vial : layout.vials()) {
                assertTrue(vial.mouthX() + 14 < left || vial.mouthX() - 14 > right, layout.key() + " school " + vial.school());
            }
        }
    }

    @Test
    void schoolsRunDownTheOpposingFlanks() {
        var layout = new BloodHudLayout(true, Reserve.NONE, true, false, false);
        assertEquals(8, layout.vials().size());
        for (int row = 0; row < 4; row++) {
            var outward = layout.vials().stream().filter(v -> v.school() == 0).findFirst().orElseThrow();
            assertTrue(outward.pointsRight(), "unmirrored outward vials tip in toward the vessel");
            var a = vial(layout, row);
            var b = vial(layout, 7 - row);
            assertEquals(a.mouthY(), b.mouthY(), "schools " + row + " and " + (7 - row) + " share a row");
            assertTrue(a.mouthX() < layout.vessel().x() + 32 && b.mouthX() > layout.vessel().x() + 32);
        }
    }

    @Test
    void streamsOnlyWithTheVessel() {
        for (BloodHudLayout layout : every(false)) for (var vial : layout.vials()) {
            assertEquals(layout.blood(), vial.entryX() >= 0, layout.key());
            if (layout.blood()) {
                assertTrue(vial.entryX() >= layout.vessel().x() + 22 && vial.entryX() <= layout.vessel().x() + 41,
                        "the trickle lands inside the glass");
            }
        }
    }

    @Test
    void vialsFitTheBindingOnlyAroundALoneReserve() {
        for (BloodHudLayout layout : every(false)) for (var vial : layout.vials()) {
            assertEquals(!layout.blood() && layout.reserve() != Reserve.NONE, vial.fitRow() >= 0, layout.key());
        }
    }

    @Test
    void labelsSitBelowTheBinding() {
        var layout = new BloodHudLayout(true, Reserve.GOURD, true, true, false);
        assertTrue(layout.degreeLabelY() >= layout.vessel().y() + 128 + 6);
        assertTrue(layout.volumeLabelY() + 7 <= layout.height());
        assertTrue(layout.reserveLabelY() >= layout.reserveRect().y() + layout.reserveRect().h() + 6);
    }

    @Test
    void everyCornerAndSmallViewportKeepsTheWholeAssemblyVisible() {
        for (BloodHudLayout layout : every(false)) for (int height : new int[]{180, 240, 360}) {
            for (boolean left : new boolean[]{true, false}) for (boolean top : new boolean[]{true, false}) {
                var p = layout.place(320, height, left, top, true, 3);
                assertTrue(p.x() >= 3.9 && p.y() >= 3.9, layout.key());
                assertTrue(p.screenX(layout.width()) <= 316.1, layout.key());
                assertTrue(p.screenY(layout.height()) <= height - 29.9, layout.key());
                assertEquals(0, p.localX(p.x()), 0.001);
            }
        }
    }

    @Test
    void bottomCornersClearTheHealthAndArmourRows() {
        for (BloodHudLayout layout : every(false)) for (int width : new int[]{320, 340, 427, 640}) {
            int height = 240;
            for (boolean left : new boolean[]{true, false}) {
                var p = layout.place(width, height, left, false, false, 3);
                float x0 = p.x(), x1 = p.screenX(layout.width());
                if (x1 > width / 2f - 91 && x0 < width / 2f + 91) {
                    assertTrue(p.screenY(layout.height()) <= height - 51.9, layout.key() + " at " + width);
                }
            }
        }
    }

    @Test
    void everyArtPixelIsAWholeNumberOfScreenPixels() {
        for (BloodHudLayout layout : every(false)) for (int gui = 1; gui <= 5; gui++) {
            int width = 1920 / gui, height = 1080 / gui;
            for (boolean left : new boolean[]{true, false}) for (boolean top : new boolean[]{true, false}) {
                var p = layout.place(width, height, left, top, false, gui);
                float screenPx = p.scale() * gui;
                assertEquals(Math.round(screenPx), screenPx, 1e-4, layout.key() + " at GUI " + gui);
                assertTrue(screenPx >= 1, "never below one screen pixel per art pixel when the screen has room");
                assertEquals(Math.round(p.x() * gui), p.x() * gui, 1e-3, "the left edge lands on a screen pixel");
                assertEquals(Math.round(p.y() * gui), p.y() * gui, 1e-3, "the top edge lands on a screen pixel");
            }
            var p = layout.place(width, height, true, true, false, gui);
            assertEquals(Math.max(1, Math.round(BloodHudLayout.SCALE * gui)), Math.round(p.scale() * gui),
                    "the nearest whole size to today's 0.6");
        }
    }

    @Test
    void everyStateHasItsOwnSmallCode() {
        java.util.Set<Integer> codes = new java.util.HashSet<>();
        for (boolean mirrored : new boolean[]{false, true}) for (BloodHudLayout layout : every(mirrored)) {
            assertTrue(layout.code() >= 0 && layout.code() < BloodHudLayout.CODES, layout.key());
            assertTrue(codes.add(layout.code()), "codes are unique: " + layout.key());
        }
    }

    @Test
    void everythingStaysOnTheCanvas() {
        for (boolean mirrored : new boolean[]{false, true}) for (BloodHudLayout layout : every(mirrored)) {
            for (var r : java.util.Arrays.asList(layout.vessel(), layout.reserveRect(), layout.jarRect(), layout.spine())) {
                if (r == null) continue;
                assertTrue(r.x() >= 0 && r.y() >= 0 && r.x() + r.w() <= layout.width() && r.y() + r.h() <= layout.height(), layout.key());
            }
            for (var vial : layout.vials()) {
                assertTrue(vial.mouthX() >= 14 && vial.mouthX() + 14 <= layout.width(), layout.key());
            }
        }
    }

    private static BloodHudLayout.VialSeat vial(BloodHudLayout layout, int school) {
        return layout.vials().stream().filter(v -> v.school() == school).findFirst().orElseThrow();
    }
}
