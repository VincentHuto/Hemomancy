package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout;
import com.vincenthuto.hemomancy.client.screen.overlay.BloodHudLayout.Reserve;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HudAssemblyTest {
    @Test
    void theTrickleEndsAtTheSurfaceInsideTheGlass() {
        for (boolean mirrored : new boolean[]{false, true}) {
            var layout = new BloodHudLayout(true, Reserve.GOURD, true, true, mirrored);
            var assembly = HudAssembly.build(layout, HudMasksFixture.masks());
            int surface = assembly.vesselSurfaceY(0.4);
            var window = HudMasksFixture.vesselWindow();
            var vessel = layout.vessel();
            for (var seat : assembly.vials()) {
                int[][] path = TemperStream.path(seat, surface);
                int[] end = seat.entryY() < surface ? new int[]{seat.entryX(), surface - 1} : new int[]{seat.entryX(), seat.entryY()};
                assertArrayEquals(end, path[path.length - 1], "school " + seat.school() + ": above the blood it falls to the"
                        + " surface, below it it pours straight in");
                for (int[] p : path) {
                    if (p[1] <= seat.entryY()) continue;
                    assertTrue(window.at(p[0] - vessel.x(), p[1] - vessel.y()), "the fall stays inside the glass");
                }
            }
        }
    }

    @Test
    void theSurfaceRisesWithTheBlood() {
        var assembly = HudAssembly.build(new BloodHudLayout(true, Reserve.NONE, true, false, false), HudMasksFixture.masks());
        assertTrue(assembly.vesselSurfaceY(1.0) < assembly.vesselSurfaceY(0.5));
        assertTrue(assembly.vesselSurfaceY(0.5) < assembly.vesselSurfaceY(0.0));
    }

    @Test
    void fittedVialsSitOnTheBinding() {
        for (Reserve reserve : new Reserve[]{Reserve.GOURD, Reserve.HORN, Reserve.RIB}) for (boolean mirrored : new boolean[]{false, true}) {
            var layout = new BloodHudLayout(false, reserve, true, false, mirrored);
            var assembly = HudAssembly.build(layout, HudMasksFixture.masks());
            var raw = layout.vials();
            for (int i = 0; i < raw.size(); i++) {
                var seat = assembly.vials().get(i);
                int x = assembly.binding().seatX(raw.get(i).fitRow(), seat.pointsRight());
                assertTrue(Math.abs(seat.mouthX() - x) <= 6 && Math.abs(seat.mouthY() - raw.get(i).fitRow()) <= 6,
                        reserve + " school " + seat.school());
            }
        }
    }

    @Test
    void schoolAtFindsEveryVial() {
        for (boolean blood : new boolean[]{true, false}) {
            var layout = new BloodHudLayout(blood, blood ? Reserve.NONE : Reserve.GOURD, true, false, false);
            var assembly = HudAssembly.build(layout, HudMasksFixture.masks());
            for (var seat : assembly.vials()) {
                var sprite = assembly.vialArt().sprite(seat.pointsRight());
                int cx = seat.mouthX() + sprite.dx()[sprite.dx().length / 2];
                int cy = seat.mouthY() + sprite.dy()[sprite.dy().length / 2];
                assertEquals(seat.school(), assembly.schoolAt(cx + 0.5f, cy + 0.5f));
            }
            assertEquals(-1, assembly.schoolAt(-5, -5));
        }
    }

    @Test
    void theJarArtHasAClearWindow() {
        var back = HudMasksFixture.image("blood_hud/morphling_jar_back");
        var front = HudMasksFixture.image("blood_hud/morphling_jar_front");
        for (var image : new java.awt.image.BufferedImage[]{back, front}) {
            assertEquals(BloodHudLayout.JAR_W, image.getWidth());
            assertEquals(BloodHudLayout.JAR_H, image.getHeight());
        }
        var window = BloodHudLayout.JAR_WINDOW;
        int clear = 0, green = 0, cells = window.w() * window.h();
        for (int y = window.y(); y < window.y() + window.h(); y++) for (int x = window.x(); x < window.x() + window.w(); x++) {
            assertEquals(255, back.getRGB(x, y) >>> 24, "tinted glass behind the Morphling");
            int argb = front.getRGB(x, y);
            if ((argb >>> 24) < 128) clear++;
            if ((argb >>> 24) > 0 && ((argb >> 8) & 0xFF) > ((argb >> 16) & 0xFF) && ((argb >> 8) & 0xFF) > (argb & 0xFF)) green++;
        }
        // Vivianite glass: a thin green tint the Morphling shows through, pitted with a few pores.
        assertTrue(clear >= cells * 9 / 10, "the Morphling shows through the front glass: " + clear + "/" + cells);
        assertTrue(green >= cells * 8 / 10, "the front glass is vivianite green: " + green + "/" + cells);
    }

    @Test
    void noSkyIsTrappedInsideTheBinding() {
        var masks = HudMasksFixture.masks();
        for (boolean mirrored : new boolean[]{false, true}) for (int bits = 1; bits < 16; bits++) {
            for (Reserve reserve : Reserve.values()) {
                if (((bits & 2) != 0) == (reserve == Reserve.NONE)) continue;
                var layout = new BloodHudLayout((bits & 1) != 0, reserve, (bits & 4) != 0, (bits & 8) != 0, mirrored);
                var assembly = HudAssembly.build(layout, masks);
                int w = layout.width(), h = layout.height();
                boolean[] covered = new boolean[w * h];
                int[] paint = assembly.binding().paint(7, false);
                for (int i = 0; i < covered.length; i++) covered[i] = paint[i] != 0;
                cover(covered, w, masks.vessel(), layout.vessel());
                cover(covered, w, masks.reserve(reserve), layout.reserveRect());
                cover(covered, w, masks.jar(), layout.jarRect());
                // Flood the open sky in from the canvas edge; any uncovered pixel it cannot reach is a trapped gap.
                boolean[] sky = new boolean[w * h];
                java.util.ArrayDeque<Integer> queue = new java.util.ArrayDeque<>();
                for (int x = 0; x < w; x++) for (int y : new int[]{0, h - 1}) seed(sky, covered, queue, x + y * w);
                for (int y = 0; y < h; y++) for (int x : new int[]{0, w - 1}) seed(sky, covered, queue, x + y * w);
                while (!queue.isEmpty()) {
                    int i = queue.poll(), x = i % w, y = i / w;
                    if (x > 0) seed(sky, covered, queue, i - 1);
                    if (x < w - 1) seed(sky, covered, queue, i + 1);
                    if (y > 0) seed(sky, covered, queue, i - w);
                    if (y < h - 1) seed(sky, covered, queue, i + w);
                }
                int trapped = 0;
                StringBuilder where = new StringBuilder();
                for (int i = 0; i < covered.length; i++) if (!covered[i] && !sky[i]) {
                    if (trapped++ < 40) where.append(" (").append(i % w).append(',').append(i / w).append(')');
                }
                assertEquals(0, trapped, layout.key() + " has sky trapped behind its parts:" + where);
            }
        }
    }

    @Test
    void theJarAndReserveAreGraftedTogether() {
        for (Reserve reserve : new Reserve[]{Reserve.GOURD, Reserve.HORN, Reserve.RIB}) for (boolean mirrored : new boolean[]{false, true}) {
            var layout = new BloodHudLayout(true, reserve, true, true, mirrored);
            int[] paint = HudAssembly.build(layout, HudMasksFixture.masks()).binding().paint(7, false);
            var jar = layout.jarRect();
            var res = layout.reserveRect();
            int y = (jar.y() + jar.h() + res.y()) / 2;
            for (int x = jar.x() + 6; x < jar.x() + jar.w() - 6; x++) {
                assertNotEquals(0, paint[y * layout.width() + x], layout.key() + ": no sky between the jar and the reserve at x=" + x);
            }
        }
    }

    @Test
    void streamPathsAreBuiltOncePerBloodLevel() {
        var assembly = HudAssembly.build(new BloodHudLayout(true, Reserve.NONE, true, false, false), HudMasksFixture.masks());
        var seat = assembly.vials().get(0);
        int[][] first = assembly.streamPath(seat, 90);
        assertSame(first, assembly.streamPath(seat, 90), "same blood level, same cached path");
        assertArrayEquals(TemperStream.path(seat, 90), first);
        int[][] lower = assembly.streamPath(seat, 100);
        assertNotSame(first, lower);
        assertArrayEquals(TemperStream.path(seat, 100), lower);
    }

    @Test
    void theAssemblyCacheKeepsOnlyTheRecentStates() {
        var cache = new HudAssemblyCache(2);
        var masks = HudMasksFixture.masks();
        var a = new BloodHudLayout(true, Reserve.NONE, false, false, false);
        var b = new BloodHudLayout(true, Reserve.GOURD, false, false, false);
        var c = new BloodHudLayout(false, Reserve.NONE, true, false, false);
        var first = cache.get(a, masks);
        assertSame(first, cache.get(a, masks), "a hit returns the built assembly");
        cache.get(b, masks);
        cache.get(a, masks);
        cache.get(c, masks);
        assertEquals(2, cache.size());
        assertSame(first, cache.get(a, masks), "the recently used state survives");
        cache.clear();
        assertEquals(0, cache.size());
    }

    @Test
    void aBakedLayoutIsDrawnExactlyAsPainted() {
        var layout = new BloodHudLayout(true, Reserve.GOURD, true, true, false);
        var plain = HudMasksFixture.masks();
        var grown = HudAssembly.build(layout, plain).binding();
        BindingTile layers = grown.layers();
        int[] edited = layers.art().clone();
        int pixel = java.util.stream.IntStream.range(0, edited.length).filter(i -> edited[i] != 0).findFirst().orElseThrow();
        edited[pixel] = 0xFF00FF00;
        var baked = new BindingTile(layers.width(), layers.height(), edited, layers.dormant(), layers.apotheos());
        var masks = new HudMasks(plain.vessel(), plain.gourd(), plain.horn(), plain.rib(), plain.jar(), plain.vesselWindow(),
                plain.binding(), plain.vials(), name -> name.equals(layout.fileName()) ? baked : null, name -> null);
        int[] paint = HudAssembly.build(layout, masks).binding().paint(7, false);
        assertEquals(0xFF00FF00, paint[pixel], "the hand-edited pixel is what shows");
        assertEquals(grown.paint(3, false).length, paint.length);
    }

    @Test
    void anUneditedBakeLooksLikeTheGrownBinding() {
        var plain = HudMasksFixture.masks();
        var layout = new BloodHudLayout(true, Reserve.HORN, true, true, true);
        var grown = HudAssembly.build(layout, plain).binding();
        var masks = new HudMasks(plain.vessel(), plain.gourd(), plain.horn(), plain.rib(), plain.jar(), plain.vesselWindow(),
                plain.binding(), plain.vials(), name -> grown.layers(), name -> null);
        var baked = HudAssembly.build(layout, masks).binding();
        for (int degree : new int[]{0, 3, 7}) {
            assertArrayEquals(grown.paint(degree, false), baked.paint(degree, false), "degree " + degree);
        }
        assertArrayEquals(grown.paint(8, true), baked.paint(8, true));
        assertArrayEquals(grown.beatBand(7, 2, 8), baked.beatBand(7, 2, 8));
    }

    @Test
    void everyStateHasItsOwnFileName() {
        var names = new java.util.HashSet<String>();
        for (var layout : HudLayoutExport.everyState()) {
            assertTrue(layout.fileName().matches("[a-z_]+"), layout.fileName());
            assertTrue(names.add(layout.fileName()), "unique: " + layout.fileName());
        }
        assertEquals("vessel_gourd_vials_jar_right", new BloodHudLayout(true, Reserve.GOURD, true, true, true).fileName());
    }

    @Test
    void handPlacedVialsKeepTheirSchoolsMouthsAndLiquid() {
        var plain = HudMasksFixture.masks();
        for (var layout : new BloodHudLayout[]{new BloodHudLayout(true, Reserve.GOURD, true, true, false),
                new BloodHudLayout(false, Reserve.HORN, true, false, true), new BloodHudLayout(false, Reserve.NONE, true, false, false)}) {
            var shared = HudAssembly.build(layout, plain);
            int[][] layers = HudLayoutExport.vialLayers(shared);
            var baked = BakedVials.of(layout.width(), layout.height(), layers[0], layers[1], layers[2], layers[3]);
            var masks = new HudMasks(plain.vessel(), plain.gourd(), plain.horn(), plain.rib(), plain.jar(),
                    plain.vesselWindow(), plain.binding(), plain.vials(), name -> null, name -> baked);
            var assembly = HudAssembly.build(layout, masks);
            assertSame(baked, assembly.bakedVials());
            for (var seat : shared.vials()) {
                var sprite = shared.vialArt().sprite(seat.pointsRight());
                assertEquals(seat.mouthX(), baked.mouthX(seat.school()), layout.fileName());
                assertEquals(sprite.liquidDx().length, baked.liquid(seat.school()).length, "every liquid cell kept");
                int mid = sprite.dx().length / 2;
                assertEquals(seat.school(), assembly.schoolAt(seat.mouthX() + sprite.dx()[mid] + 0.5f,
                        seat.mouthY() + sprite.dy()[mid] + 0.5f), "art pixels belong to their own vial");
            }
        }
    }

    @Test
    void theFrontGlassGoesWithItsVial() {
        var plain = HudMasksFixture.masks();
        var layout = new BloodHudLayout(true, Reserve.NONE, true, false, false);
        var shared = HudAssembly.build(layout, plain);
        int[][] layers = HudLayoutExport.vialLayers(shared);
        var baked = BakedVials.of(layout.width(), layout.height(), layers[0], layers[1], layers[2], layers[3]);
        for (var seat : shared.vials()) {
            int[] count = {0};
            baked.forEachGlass(seat.school(), (x, y, argb) -> count[0]++);
            assertEquals(shared.vialArt().glass(seat.pointsRight()).dx().length, count[0], "school " + seat.school());
            assertTrue(count[0] > 3, "each vial has highlights and shadow on its front glass");
        }
    }

    @Test
    void aMovedVialMovesItsTrickle() {
        var plain = HudMasksFixture.masks();
        var layout = new BloodHudLayout(true, Reserve.NONE, true, false, false);
        int[][] layers = HudLayoutExport.vialLayers(HudAssembly.build(layout, plain));
        int w = layout.width();
        int[] mouth = new int[layers[2].length];
        for (int i = 0; i < mouth.length; i++) if (layers[2][i] != 0) mouth[i + 3 * w] = layers[2][i];
        var baked = BakedVials.of(w, layout.height(), layers[0], layers[1], mouth, layers[3]);
        var masks = new HudMasks(plain.vessel(), plain.gourd(), plain.horn(), plain.rib(), plain.jar(),
                plain.vesselWindow(), plain.binding(), plain.vials(), name -> null, name -> baked);
        var before = HudAssembly.build(layout, plain).vials().get(0);
        var after = HudAssembly.build(layout, masks).vials().get(0);
        assertEquals(before.mouthY() + 3, after.mouthY(), "the trickle leaves from the painted mouth");
    }

    private static void cover(boolean[] covered, int w, HudMask mask, BloodHudLayout.Rect rect) {
        if (mask == null || rect == null) return;
        for (int y = 0; y < mask.height(); y++) for (int x = 0; x < mask.width(); x++) {
            if (mask.at(x, y)) covered[(rect.y() + y) * w + rect.x() + x] = true;
        }
    }

    private static void seed(boolean[] sky, boolean[] covered, java.util.ArrayDeque<Integer> queue, int i) {
        if (sky[i] || covered[i]) return;
        sky[i] = true;
        queue.add(i);
    }
}
