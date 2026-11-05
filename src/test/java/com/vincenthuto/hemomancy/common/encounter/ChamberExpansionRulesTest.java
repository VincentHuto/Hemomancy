package com.vincenthuto.hemomancy.common.encounter;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

final class ChamberExpansionRulesTest {
	@Test
	void physicalRadiusNeverShrinks() {
		assertEquals(8, ChamberExpansionRules.nextBuiltRadius(8, 6));
		assertEquals(10, ChamberExpansionRules.nextBuiltRadius(8, 10));
	}

	@Test
	void growthContainsOnlyNewlyUnlockedFloor() {
		Set<ChamberExpansionRules.Offset> band = ChamberExpansionRules.floorBand(4, 6);
		assertTrue(band.contains(new ChamberExpansionRules.Offset(6, 0)));
		assertTrue(band.contains(new ChamberExpansionRules.Offset(0, -6)));
		assertTrue(band.contains(new ChamberExpansionRules.Offset(2, ChamberRoomShape.reach(6))));
		assertFalse(band.contains(new ChamberExpansionRules.Offset(4, 0)));
		assertFalse(band.contains(new ChamberExpansionRules.Offset(0, ChamberRoomShape.reach(4))));
		assertFalse(band.contains(new ChamberExpansionRules.Offset(7, 0)));
		for (ChamberExpansionRules.Offset offset : band) {
			assertTrue(ChamberRoomShape.contains(offset.x(), offset.z(), 6));
			assertFalse(ChamberRoomShape.contains(offset.x(), offset.z(), 4));
		}
	}

	@Test
	void aNewRoomGetsItsWholeFloor() {
		int cells = ChamberExpansionRules.floorBand(-1, 3).size();
		assertTrue(cells >= 49 && cells <= 62, cells + " cells");
		assertTrue(ChamberExpansionRules.floorBand(-1, 3).contains(new ChamberExpansionRules.Offset(0, 0)));
	}

	@Test
	void markersLieOnTheFloorAtEverySize() {
		for (int radius = 3; radius <= 10; radius++) {
			for (ChamberExpansionRules.Offset marker : ChamberExpansionRules.markerOffsets(radius)) {
				assertTrue(ChamberRoomShape.contains(marker.x(), marker.z(), radius), "radius " + radius);
				assertFalse(marker.x() == 0 && marker.z() == 0);
			}
		}
	}
}
