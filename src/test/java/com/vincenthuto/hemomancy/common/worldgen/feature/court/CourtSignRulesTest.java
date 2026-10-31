package com.vincenthuto.hemomancy.common.worldgen.feature.court;

import com.vincenthuto.hemomancy.common.item.harbinger.morphlings.CourtHouse;
import com.vincenthuto.hemomancy.common.worldgen.terrain.pelagic.PelagicLayer;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The sign tier's per-region caps and choices (Track 8), on {@link CourtSignRules} alone. */
class CourtSignRulesTest {
	private static final long SEED = 42L;

	@Test
	void everyRegionHoldsExactlyItsCapOfSlotsInsideItself() {
		for (var family : CourtSignRules.Family.values()) {
			for (int rx = -6; rx <= 6; rx++) {
				for (int rz = -6; rz <= 6; rz++) {
					var slots = CourtSignRules.slots(SEED, family, rx, rz);
					assertEquals(family.cap(), slots.size());
					for (var slot : slots) {
						assertEquals(rx, CourtSignRules.region(slot.x()), family + " slot left its region");
						assertEquals(rz, CourtSignRules.region(slot.z()), family + " slot left its region");
					}
				}
			}
		}
	}

	@Test
	void slotsAreSeededAndOrderFree() {
		var first = CourtSignRules.slots(SEED, CourtSignRules.Family.SUNBURST_BLOOM, 3, -2);
		CourtSignRules.slots(SEED, CourtSignRules.Family.SUNBURST_BLOOM, 9, 9);
		assertEquals(first, CourtSignRules.slots(SEED, CourtSignRules.Family.SUNBURST_BLOOM, 3, -2),
				"A region's slots depend on the seed and the region alone");
		assertNotEquals(first, CourtSignRules.slots(SEED + 1, CourtSignRules.Family.SUNBURST_BLOOM, 3, -2));
		assertNotEquals(first.get(0), CourtSignRules.slots(SEED, CourtSignRules.Family.MESSENGER_BUOY, 3, -2).get(0));
		var spots = new HashSet<Long>();
		for (int rx = 0; rx < 8; rx++)
			for (var slot : CourtSignRules.slots(SEED, CourtSignRules.Family.SUNBURST_BLOOM, rx, 0))
				spots.add((long) slot.x() << 32 | (slot.z() & 0xFFFFFFFFL));
		assertTrue(spots.size() > 40, "Slots spread over their regions: " + spots.size());
	}

	@Test
	void capsStaySparse() {
		assertEquals(6, CourtSignRules.Family.SUNBURST_BLOOM.cap());
		assertEquals(1, CourtSignRules.Family.HOUSE_MARKER.cap());
		assertEquals(2, CourtSignRules.Family.RELAY_MARKER.cap());
		assertEquals(2, CourtSignRules.Family.MESSENGER_BUOY.cap());
		assertEquals(3, CourtSignRules.Family.VENT_OFFERING.cap());
	}

	@Test
	void bloomsKeepToReefAndCoastAndThinOverOpenWater() {
		assertTrue(CourtSignRules.bloomSurface(PelagicLayer.REEF, 1));
		assertTrue(CourtSignRules.bloomSurface(PelagicLayer.SHORE, 1));
		for (var deep : new PelagicLayer[] {PelagicLayer.OPEN, PelagicLayer.MIDNIGHT}) {
			int open = 0;
			for (long h = 0; h < 3000; h++) if (CourtSignRules.bloomSurface(deep, h << 7)) open++;
			assertEquals(1000, open, "One open-sea slot in three keeps its bloom over " + deep);
		}
	}

	@Test
	void relaysStandAtTheDepthBoundaries() {
		assertTrue(CourtSignRules.nearBoundary(33));
		assertTrue(CourtSignRules.nearBoundary(10));
		assertTrue(CourtSignRules.nearBoundary(-31));
		assertFalse(CourtSignRules.nearBoundary(22));
		assertFalse(CourtSignRules.nearBoundary(-20));
	}

	@Test
	void eachLayerIsItsHousesWater() {
		assertEquals(CourtHouse.HOLDING_STONE, CourtSignRules.house(PelagicLayer.SHORE, false));
		assertEquals(CourtHouse.GIVING_LIGHT, CourtSignRules.house(PelagicLayer.REEF, false));
		assertEquals(CourtHouse.SHARED_WAKE, CourtSignRules.house(PelagicLayer.OPEN, false));
		assertEquals(CourtHouse.WOVEN_CURRENT, CourtSignRules.house(PelagicLayer.TWILIGHT, false));
		assertEquals(CourtHouse.VEILED_FOLD, CourtSignRules.house(PelagicLayer.MIDNIGHT, false));
		assertEquals(CourtHouse.STILL_BELL, CourtSignRules.house(PelagicLayer.MIDNIGHT, true));
		assertEquals(CourtHouse.LAST_MARROW, CourtSignRules.house(PelagicLayer.CARRION, false));
		assertEquals(CourtHouse.KINDLING, CourtSignRules.house(PelagicLayer.HYDROTHERMAL, false));
	}
}
