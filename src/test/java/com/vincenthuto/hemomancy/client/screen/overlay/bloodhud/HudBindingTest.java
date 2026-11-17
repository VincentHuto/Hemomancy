package com.vincenthuto.hemomancy.client.screen.overlay.bloodhud;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class HudBindingTest {
    private static HudMask block(int w, int h) {
        boolean[] solid = new boolean[w * h];
        Arrays.fill(solid, true);
        return new HudMask(w, h, solid);
    }

    @Test
    void bindingRingsAShapeAndLeavesItsInsideClear() {
        var s = HudSilhouette.of(80, 80);
        s.stamp(block(20, 40), 30, 20);
        int[] p = HudBinding.build(s, HudMasksFixture.bindingArt()).paint(7, false);
        assertEquals(0, p[40 * 80 + 40], "the wrapped part shows through");
        assertNotEquals(0, p[40 * 80 + 30 - 4], "4 px outside the edge is binding");
        assertEquals(0, p[40 * 80 + 30 - 10], "nothing past the binding's outer edge");
    }

    @Test
    void theLatticeIsCalm() {
        var s = HudSilhouette.of(160, 160);
        s.stamp(block(100, 100), 30, 30);
        var b = HudBinding.build(s, HudMasksFixture.bindingArt());
        assertEquals(Math.round(b.length() / HudBinding.COLLAR_SPACING), b.collarCount(), 1);
    }

    @Test
    void redClimbsByDegreeFromTheBase() {
        var s = HudSilhouette.of(96, 160);
        s.stamp(HudMasksFixture.vessel(), 16, 16);
        var b = HudBinding.build(s, HudMasksFixture.bindingArt());
        int previous = -1;
        for (int degree = 0; degree <= 7; degree++) {
            int count = reds(b.paint(degree, false)).length;
            assertTrue(count >= previous, "red never recedes as degree rises");
            previous = count;
            if (degree == 0) assertEquals(0, count, "no red before the first degree");
        }
        int[] third = reds(b.paint(3, false));
        double meanRow = Arrays.stream(third).map(i -> i / 96).average().orElseThrow();
        assertTrue(meanRow > 16 + 64, "at degree 3 the red sits in the lower half");
        assertTrue(reds(b.paint(7, false)).length > reds(b.paint(6, false)).length);
    }

    @Test
    void apotheosTurnsTheRedGold() {
        var s = HudSilhouette.of(96, 160);
        s.stamp(HudMasksFixture.vessel(), 16, 16);
        int[] gold = HudBinding.build(s, HudMasksFixture.bindingArt()).paint(8, true);
        var vein = java.util.Set.of(0x350107, 0x4B0000, 0x7A0610, 0xA80E1C, 0xD6343E, 0xE2283A);
        assertTrue(Arrays.stream(gold).noneMatch(c -> vein.contains(c & 0xFFFFFF)), "no blood red left at Apotheos");
        assertTrue(Arrays.stream(gold).anyMatch(c -> ((c >> 16) & 0xFF) > 0xC0 && ((c >> 8) & 0xFF) > 0x60));
    }

    @Test
    void theGraftKeepsTheDipAboveTheReserveShoulder() {
        int vx = 8, vy = 8, rx = vx + 70, ry = vy + 66;
        var s = HudSilhouette.of(130, 160);
        s.stamp(HudMasksFixture.vessel(), vx, vy);
        s.stamp(HudMasksFixture.gourd(), rx, ry);
        s.fillPolygon(new int[][]{{vx + 61, ry + 22}, {rx + 4, ry + 26}, {rx + 5, ry + 62}, {vx + 46, vy + 127}});
        int[] p = HudBinding.build(s, HudMasksFixture.bindingArt()).paint(7, false);
        assertEquals(0, p[(ry - 6) * 130 + rx + 6], "open notch between the vessel and the reserve's shoulder");
        assertNotEquals(0, p[(ry + 50) * 130 + vx + 68], "the lower gap is filled");
    }

    @Test
    void anAsymmetricReserveIsFittedPerSide() {
        HudMask horn = HudMasksFixture.horn();
        int w = 80;
        var left = HudSilhouette.of(w, 80);
        left.stamp(horn, 10, 16);
        var right = HudSilhouette.of(w, 80);
        right.stamp(horn, w - 10 - horn.width(), 16);
        int[] a = HudBinding.build(left, HudMasksFixture.bindingArt()).paint(7, false);
        int[] b = HudBinding.build(right, HudMasksFixture.bindingArt()).paint(7, false);
        int differ = 0;
        for (int y = 0; y < 80; y++) for (int x = 0; x < w; x++) {
            if ((a[y * w + x] != 0) != (b[y * w + (w - 1 - x)] != 0)) differ++;
        }
        assertTrue(differ > 20, "an unflipped horn needs its own outline, not a mirrored one");
    }

    @Test
    void beatBandsPartitionTheRevealedRed() {
        var s = HudSilhouette.of(96, 160);
        s.stamp(HudMasksFixture.vessel(), 16, 16);
        var b = HudBinding.build(s, HudMasksFixture.bindingArt());
        int[] paint = b.paint(7, false);
        boolean[] seen = new boolean[paint.length];
        for (int band = 0; band < 8; band++) {
            int[] mask = b.beatBand(7, band, 8);
            for (int i = 0; i < mask.length; i++) {
                if (mask[i] == 0) continue;
                assertFalse(seen[i], "bands are disjoint");
                seen[i] = true;
            }
        }
        for (int i : reds(paint)) assertTrue(seen[i], "every revealed red pixel beats");
        int[] none = b.beatBand(0, 0, 1);
        assertTrue(Arrays.stream(none).allMatch(c -> c == 0), "unrevealed red does not beat");
    }

    @Test
    void seatsFollowTheReserveOutline() {
        HudMask gourd = HudMasksFixture.gourd();
        var s = HudSilhouette.of(80, 100);
        s.stamp(gourd, 24, 16);
        var b = HudBinding.build(s, HudMasksFixture.bindingArt());
        int lobe = b.seatX(16 + 46, true), waist = b.seatX(16 + 33, true);
        assertTrue(lobe >= 0 && waist >= 0);
        assertTrue(lobe < waist, "the binding swells out around a lobe and tucks in at the waist");
        assertTrue(b.seatX(16 + 46, false) > 24 + 16);
    }

    @Test
    void theBeatRisesFromTheBaseWithoutASeam() {
        var s = HudSilhouette.of(96, 160);
        s.stamp(HudMasksFixture.vessel(), 16, 16);
        var b = HudBinding.build(s, HudMasksFixture.bindingArt());
        int[] band = new int[96 * 160];
        Arrays.fill(band, -1);
        double[] rowSum = new double[8];
        int[] count = new int[8];
        for (int i = 0; i < 8; i++) {
            int[] mask = b.beatBand(7, i, 8);
            for (int p = 0; p < mask.length; p++) {
                if (mask[p] == 0) continue;
                band[p] = i;
                rowSum[i] += p / 96;
                count[i]++;
            }
        }
        assertTrue(rowSum[0] / count[0] > rowSum[7] / count[7] + 40, "the first band beats at the base, the last at the top");
        for (int p = 0; p < band.length; p++) {
            if (band[p] < 0) continue;
            for (int q : new int[]{p + 1, p + 96}) {
                if (q < band.length && band[q] >= 0) {
                    assertTrue(Math.abs(band[p] - band[q]) <= 1, "neighbouring veins beat together, no seam at " + p);
                }
            }
        }
    }

    @Test
    void graftTendrilsAreUnbrokenStrandsAlongTheStruts() {
        var s = HudSilhouette.of(96, 96);
        s.fillRect(16, 16, 64, 64);
        int[] paint = HudBinding.build(s, HudMasksFixture.bindingArt()).paint(7, false);
        int broken = 0, total = 0;
        for (int y = 24; y < 72; y++) for (int x = 24; x < 72; x++) {
            if (Math.floorMod(x + y, 16) != 2 || Math.floorMod(x - y, 16) < 2) continue; // skip where the other strut crosses
            total++;
            int c = paint[y * 96 + x];
            int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF;
            if (!(r > 0x40 && r > g * 2)) broken++;
        }
        assertTrue(total > 50);
        assertEquals(0, broken, "a tendril beside a strut runs its whole length, never dotted");
    }

    @Test
    void bandRedIsShadedPerCellNotSpeckledPerPixel() {
        var s = HudSilhouette.of(160, 160);
        s.stamp(block(100, 100), 30, 30);
        int[] paint = HudBinding.build(s, HudMasksFixture.bindingArt()).paint(7, false);
        // A lone pixel of a different shade between two of the same (A-B-A) is per-pixel speckle; cells are 9 px long.
        int flickers = 0;
        for (int x = 30 - 7; x < 30; x++) for (int y = 40; y < 120; y++) {
            int a = paint[(y - 1) * 160 + x], b = paint[y * 160 + x], c = paint[(y + 1) * 160 + x];
            if (isRed(a) && isRed(b) && isRed(c) && a == c && a != b) flickers++;
        }
        assertEquals(0, flickers, "red in the band is shaded per cell, never speckled per pixel");
    }

    private static boolean isRed(int c) {
        int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF;
        return (c >>> 24) != 0 && r > 0x40 && r > g * 2;
    }

    @Test
    void theBindingIsPaintedFromItsTiles() {
        int[] green = new int[16 * 16];
        Arrays.fill(green, 0xFF00FF00);
        var real = HudMasksFixture.bindingArt();
        var art = new BindingArt(real.band(), new BindingTile(16, 16, green, green, green), real.collar());
        var s = HudSilhouette.of(96, 96);
        s.fillRect(16, 16, 64, 64);
        int[] paint = HudBinding.build(s, art).paint(7, false);
        assertEquals(0xFF00FF00, paint[48 * 96 + 48], "a hand-painted lattice tile is what the graft shows");
    }

    /** Indices of pixels in the red vein ramp. */
    private static int[] reds(int[] argb) {
        return java.util.stream.IntStream.range(0, argb.length).filter(i -> {
            int c = argb[i];
            if ((c >>> 24) == 0) return false;
            int r = (c >> 16) & 0xFF, g = (c >> 8) & 0xFF, bl = c & 0xFF;
            return r > 0x40 && r > g * 2 && r > bl * 2;
        }).toArray();
    }
}
