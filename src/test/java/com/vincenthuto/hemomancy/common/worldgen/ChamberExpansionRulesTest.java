package com.vincenthuto.hemomancy.common.worldgen;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

final class ChamberExpansionRulesTest {
	@Test
	void physicalRadiusNeverShrinks() {
		assertEquals(8, ChamberExpansionRules.nextBuiltRadius(8, 6));
		assertEquals(10, ChamberExpansionRules.nextBuiltRadius(8, 10));
	}

	@Test
	void growthContainsOnlyNewlyUnlockedFloorBands() {
		Set<ChamberExpansionRules.Offset> band = ChamberExpansionRules.floorBand(4, 6);
		assertEquals(88, band.size());
		assertTrue(band.contains(new ChamberExpansionRules.Offset(6, 0)));
		assertTrue(band.contains(new ChamberExpansionRules.Offset(-5, 5)));
		assertFalse(band.contains(new ChamberExpansionRules.Offset(4, 4)));
		assertFalse(band.contains(new ChamberExpansionRules.Offset(7, 0)));
	}

	@Test
	void markerRelocationUsesPrecedingAndExpandedCorners() {
		assertEquals(Set.of(
				new ChamberExpansionRules.Offset(3, 3), new ChamberExpansionRules.Offset(-3, 3),
				new ChamberExpansionRules.Offset(3, -3), new ChamberExpansionRules.Offset(-3, -3)),
				ChamberExpansionRules.markerOffsets(4));
		assertEquals(Set.of(
				new ChamberExpansionRules.Offset(5, 5), new ChamberExpansionRules.Offset(-5, 5),
				new ChamberExpansionRules.Offset(5, -5), new ChamberExpansionRules.Offset(-5, -5)),
				ChamberExpansionRules.markerOffsets(6));
	}
}
