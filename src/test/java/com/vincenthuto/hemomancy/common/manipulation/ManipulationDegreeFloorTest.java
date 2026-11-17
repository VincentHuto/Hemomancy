package com.vincenthuto.hemomancy.common.manipulation;

import com.vincenthuto.hemomancy.common.capability.player.harbinger.temperament.EnumBloodTemper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.vascular.EnumVeinSections;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** C-6: a manipulation's own degree floor raises its rank gate and never lowers it. */
final class ManipulationDegreeFloorTest {
	@Test
	void theRankGateHoldsWithoutAFloor() {
		BloodManipulation summa = manip(EnumManipulationRank.SUMMA);
		assertEquals(3, ManipulationRankGates.minDegreeFor(summa));
		assertTrue(ManipulationRankGates.playerMeets(3, summa));
	}

	@Test
	void aFloorAboveTheRankGateWins() {
		BloodManipulation canon = manip(EnumManipulationRank.SUMMA).setRequiredDegree(8);
		assertEquals(8, ManipulationRankGates.minDegreeFor(canon));
		assertFalse(ManipulationRankGates.playerMeets(7, canon));
		assertTrue(ManipulationRankGates.playerMeets(8, canon));
	}

	@Test
	void aFloorBelowTheRankGateChangesNothing() {
		BloodManipulation perfectus = manip(EnumManipulationRank.PERFECTUS).setRequiredDegree(2);
		assertEquals(6, ManipulationRankGates.minDegreeFor(perfectus));
	}

	@Test
	void savedCopiesAdoptTheRegisteredFloor() {
		BloodManipulation registered = manip(EnumManipulationRank.SUMMA).setRequiredDegree(8);
		BloodManipulation saved = manip(EnumManipulationRank.SUMMA);
		assertTrue(saved.adoptRegisteredMetadata(registered));
		assertEquals(8, ManipulationRankGates.minDegreeFor(saved));
	}

	private static BloodManipulation manip(EnumManipulationRank rank) {
		return new BloodManipulation("floor_test", 10, 0, 0, EnumManipulationType.QUICK,
				rank, EnumBloodTemper.ANIMUS, EnumVeinSections.HEAD);
	}
}
