package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicLayer.*;

class PelagicDensityTest {
    @Test void coastalInfluenceDoesNotSnapAcrossASaturatedWaterColumn() {
        int previous = -1;
        for (int i = 25; i <= 45; i++) {
            var coast = floor(64, i / 100.0);
            int surface = 0;
            for (int y = 0; y < 80; y++)
                if (PelagicDensity.shape(y < 32 ? 1 : -.5, coast, y, false, false) > 0) surface = y;
            if (previous >= 0) assertTrue(Math.abs(surface - previous) <= 2,
                    "Gradual influence produced a sheer step: " + previous + " -> " + surface);
            previous = surface;
        }
    }

    private PelagicTerrainSampler.Column floor(double height, double influence) {
        return new PelagicTerrainSampler.Column(height, influence, PelagicTerrainSampler.Form.PLAIN, 0, false);
    }
    @Test void oceanDensityClearsOldStoneAndSealsTheNewSeabed() {
        assertTrue(PelagicDensity.shape(.5, floor(-53.5, 1), 20, false, false) < 0);
        for (int y = -64; y <= -54; y++)
            assertTrue(PelagicDensity.shape(-.5, floor(-53.5, 1), y, false, false) > 0, "Leaking foundation at " + y);
    }
    @Test void preliminarySurfaceTracksTheSameOceanFloor() {
        assertTrue(PelagicDensity.shape(9, floor(-53.5, 1), -53, true, false) < .390625);
        assertTrue(PelagicDensity.shape(-9, floor(-53.5, 1), -54, true, false) > .390625);
    }
    @Test void preservesLandAndCavesWellBelowShallowShelves() {
        for (int y = -64; y <= 320; y += 4)
            assertEquals(.37, PelagicDensity.shape(.37, floor(42, 0), y, false, false), 0);
        assertEquals(-.3, PelagicDensity.shape(-.3, floor(42, 1), 10, false, false), 0);
        assertTrue(PelagicDensity.shape(-.3, floor(42, 1), 39, false, false) > 0);
    }
    @Test void wetAlcovesCanOpenWithoutMovingTheEstimatedSurface() {
        assertTrue(PelagicDensity.shape(1, floor(10, 1), 7, false, true) < 0);
        assertTrue(PelagicDensity.shape(1, floor(10, 1), 7, true, true) > .390625);
    }
    @Test void layersFollowAbsoluteDepthIncludingNegativeHeights() {
        int[] y = {62,34,33,13,12,-8,-9,-30,-31,-53};
        PelagicLayer[] expected = {OPEN,OPEN,TWILIGHT,TWILIGHT,MIDNIGHT,MIDNIGHT,CARRION,CARRION,HYDROTHERMAL,HYDROTHERMAL};
        for (int i = 0; i < y.length; i++) assertEquals(expected[i], atWaterY(y[i]), "Y=" + y[i]);
    }
}
