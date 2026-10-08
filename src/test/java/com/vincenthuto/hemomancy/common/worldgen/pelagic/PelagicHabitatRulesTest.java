package com.vincenthuto.hemomancy.common.worldgen.pelagic;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static com.vincenthuto.hemomancy.common.worldgen.pelagic.PelagicHabitatRules.*;

class PelagicHabitatRulesTest {
    private Site water(PelagicLayer layer, int y) { return new Site(layer, y, true, true, true, 1, true, true, true); }

    @Test void vampireSquidsSwimThroughoutOnlyTheLowestTwoLayers() {
        var squid = java.util.Arrays.stream(Species.values()).filter(s -> s.name().equals("VAMPIRE_SQUID"))
                .findFirst().orElse(null);
        assertNotNull(squid, "Vampire Squid needs its own swimming habitat");
        for (var layer : PelagicLayer.values()) {
            boolean deep = layer == PelagicLayer.CARRION || layer == PelagicLayer.HYDROTHERMAL;
            for (int y : new int[]{-53, -45, -27, -20, -6}) {
                var openWater = new Site(layer, y, true, true, true, 8, false, false, false);
                assertEquals(deep, suitable(squid, openWater), layer + " at " + y);
            }
            assertFalse(suitable(squid, water(layer, -54)));
            assertFalse(suitable(squid, water(layer, -5)));
        }
        assertEquals(0, depthCorrection(squid, -45), "Hydrothermal swimmers should not be pushed into Carrion");
        assertEquals(0, depthCorrection(squid, -20));
        assertTrue(depthCorrection(squid, 0) < 0);
        assertTrue(depthCorrection(squid, -60) > 0);
        assertFalse(suitable(squid, new Site(PelagicLayer.CARRION, -20, true, false, true, 8, false, false, false)));
        assertFalse(suitable(squid, new Site(PelagicLayer.CARRION, -20, false, true, true, 8, false, false, false)));
        assertFalse(suitable(squid, new Site(PelagicLayer.CARRION, -20, true, true, false, 8, false, false, false)));
    }

    @Test void vampireSquidPacksUseTheSwimmingCapInBothDeepBiomesOnly() {
        for (var layer : PelagicLayer.values()) {
            var packs = PelagicPopulation.entries(layer).stream().filter(e -> e.entity().equals("hemomancy:vampire_squid")).toList();
            if (layer == PelagicLayer.CARRION || layer == PelagicLayer.HYDROTHERMAL) {
                assertEquals(1, packs.size(), layer.name());
                assertEquals("water_ambient", packs.getFirst().category());
                assertTrue(packs.getFirst().weight() > 0);
                assertEquals(1, packs.getFirst().min());
                assertEquals(2, packs.getFirst().max());
            } else assertTrue(packs.isEmpty(), layer.name());
        }
    }

    @Test void allSpeciesAcceptTheirOwnHabitatAndRejectBothDepthEdges() {
        for (Species s : Species.values()) {
            assertTrue(suitable(s, water(s.home, (s.minY + s.maxY) / 2)), s.name());
            assertTrue(suitable(s, water(s.home, s.minY)), s.name() + " lower boundary");
            assertTrue(suitable(s, water(s.home, s.maxY)), s.name() + " upper boundary");
            assertFalse(suitable(s, water(s.home, s.minY - 1)), s.name());
            assertFalse(suitable(s, water(s.home, s.maxY + 1)), s.name());
        }
    }

    @Test void neighboringVisitorsHaveLimitedOverlap() {
        assertTrue(suitable(Species.HERRING, water(PelagicLayer.TWILIGHT, 32)));
        assertTrue(suitable(Species.CUTTLE, water(PelagicLayer.OPEN, 36)));
        assertTrue(suitable(Species.LANTERN, water(PelagicLayer.REEF, 44)));
        assertFalse(suitable(Species.CUTTLE, water(PelagicLayer.OPEN, 50)));
        assertFalse(suitable(Species.HAGFISH, water(PelagicLayer.MIDNIGHT, 0)));
        assertFalse(suitable(Species.HERRING, water(PelagicLayer.SHORE, 60)));
    }

    @Test void unloadedEdgesAndBlockedClearanceNeverQualify() {
        assertFalse(suitable(Species.WHALE, new Site(PelagicLayer.OPEN, 45, false, true, true, 8, false, false, false)));
        assertFalse(suitable(Species.WHALE, new Site(PelagicLayer.OPEN, 45, true, true, false, 8, false, false, false)));
        assertFalse(suitable(Species.PYROSOME, new Site(PelagicLayer.OPEN, 45, true, false, true, 8, false, false, false)));
    }

    @Test void poolMarginsAndExposedWetRocksDifferFromOpenWater() {
        assertTrue(suitable(Species.CHITON, new Site(PelagicLayer.SHORE, 64, true, false, true, 1, true, false, false)));
        assertFalse(suitable(Species.CHITON, new Site(PelagicLayer.SHORE, 64, true, false, true, 1, false, false, false)));
        assertTrue(suitable(Species.HEMOLYMPHOPODA, water(PelagicLayer.SHORE, 59)));
        assertFalse(suitable(Species.HEMOLYMPHOPODA, new Site(PelagicLayer.SHORE, 59, true, true, true, 5, true, false, false)));
    }

    @Test void bottomAnimalsNeedSupportAndTheirOwnSubstrate() {
        assertFalse(suitable(Species.URCHIN, new Site(PelagicLayer.REEF, 45, true, true, true, 7, false, false, false)));
        assertFalse(suitable(Species.HAGFISH, new Site(PelagicLayer.CARRION, -20, true, true, true, 1, false, false, true)));
        assertFalse(suitable(Species.SNAIL, new Site(PelagicLayer.HYDROTHERMAL, -45, true, true, true, 1, false, true, false)));
    }

    @Test void depthPreferenceGentlyReturnsCuttlesTowardTwilight() {
        assertTrue(depthCorrection(Species.CUTTLE, 43) < 0);
        assertEquals(0, depthCorrection(Species.CUTTLE, 23));
        assertTrue(depthCorrection(Species.CUTTLE, 0) > 0);
        assertTrue(Math.abs(depthCorrection(Species.CUTTLE, 100)) <= .018);
    }
}

