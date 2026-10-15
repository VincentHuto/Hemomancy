package com.vincenthuto.hemomancy.common.rite;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftBedTest {
	@Test
	void aStructureMayStandAnywhereOnTheFloorUpToItsHalfWidthFromTheFocus() {
		// A 3x3 floor lets it stand one block out from the Focus, a 5x5 two, a 7x7 three, a 9x9 four, across the
		// floor's whole width on any side.
		assertEquals(new WorkingCraftBed(3, 1), WorkingCraftBed.forTier(CardinalRiteType.MINOR));
		assertEquals(new WorkingCraftBed(5, 2), WorkingCraftBed.forTier(CardinalRiteType.LESSER));
		assertEquals(new WorkingCraftBed(7, 3), WorkingCraftBed.forTier(CardinalRiteType.GREATER));
		assertEquals(new WorkingCraftBed(9, 4), WorkingCraftBed.forTier(CardinalRiteType.GRAND));
	}

	@Test
	void footprintIgnoresSpacePadding() {
		String[][] effigy = {{"   ", " P "}, {"PIP", "IBI"}, {"   ", " P "}};
		assertEquals(new WorkingCraftBed.Footprint(3, 3, 2), WorkingCraftBed.Footprint.of(effigy));
		String[][] forge = {{"H H", "ACG"}};
		assertEquals(new WorkingCraftBed.Footprint(3, 1, 2), WorkingCraftBed.Footprint.of(forge));
		String[][] padded = {{"     "}, {"  X  "}, {"     "}};
		assertEquals(new WorkingCraftBed.Footprint(1, 1, 1), WorkingCraftBed.Footprint.of(padded));
	}

	@Test
	void fitsInEitherRotationWithinHeight() {
		WorkingCraftBed lesser = WorkingCraftBed.forTier(CardinalRiteType.LESSER);
		assertTrue(lesser.fits(new WorkingCraftBed.Footprint(3, 2, 3)));
		assertTrue(lesser.fits(new WorkingCraftBed.Footprint(2, 3, 3)));
		assertFalse(lesser.fits(new WorkingCraftBed.Footprint(3, 3, 1)));
		assertFalse(lesser.fits(new WorkingCraftBed.Footprint(3, 2, 4)));
	}

	@Test
	void smallestFittingTier() {
		assertEquals(CardinalRiteType.MINOR, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(3, 1, 3)));
		assertEquals(CardinalRiteType.GREATER, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(3, 3, 3)));
		assertEquals(CardinalRiteType.GREATER, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(3, 4, 3)));
		assertEquals(CardinalRiteType.GRAND, WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(8, 4, 3)));
		assertNull(WorkingCraftBed.smallestFitting(new WorkingCraftBed.Footprint(6, 6, 2)));
	}

	@Test
	void containsUsesLateralOutwardAndUp() {
		WorkingCraftBed greater = WorkingCraftBed.forTier(CardinalRiteType.GREATER);
		assertTrue(greater.contains(0, 1, 1));
		assertTrue(greater.contains(-3, 3, 3), "out to the floor's corner");
		assertFalse(greater.contains(0, 0, 1), "the Focus row is not part of the bed");
		assertFalse(greater.contains(4, 1, 1), "past the floor's edge");
		assertFalse(greater.contains(0, 4, 1), "past the floor's edge");
		assertFalse(greater.contains(0, 1, 0), "the bed starts on the floor's upper layer");
		assertFalse(greater.contains(0, 1, 4));
	}

	@Test
	void onlyBlocksOnTheCraftFloorAreSwallowedWhileItFeeds() {
		net.minecraft.core.BlockPos focus = new net.minecraft.core.BlockPos(0, 64, 0);
		assertTrue(WorkingCraftBed.withinFloor(focus, focus.offset(3, 1, 2), 5.5F));
		assertTrue(WorkingCraftBed.withinFloor(focus, focus.offset(0, 3, 5), 5.5F), "the top of a three-high bed");
		assertFalse(WorkingCraftBed.withinFloor(focus, focus.offset(9, 1, 0), 5.5F), "an Alembic past the floor's edge");
		assertFalse(WorkingCraftBed.withinFloor(focus, focus.offset(4, 0, 4), 5.5F), "the floor's footprint is round");
		assertFalse(WorkingCraftBed.withinFloor(focus, focus.offset(0, 6, 0), 5.5F), "well above the bed");
	}

	@Test
	void theFourSidesBedsCoverTheWholeFloorButTheFocus() {
		// Every floor cell but the Focus lies in some side's bed, so a structure fits anywhere that leaves the Focus clear.
		WorkingCraftBed lesser = WorkingCraftBed.forTier(CardinalRiteType.LESSER);
		for (int x = -2; x <= 2; x++) {
			for (int z = -2; z <= 2; z++) {
				boolean inSome = lesser.contains(x, -z, 1) || lesser.contains(-x, z, 1)
						|| lesser.contains(z, x, 1) || lesser.contains(-z, -x, 1);
				assertEquals(x != 0 || z != 0, inSome, "cell " + x + "," + z);
			}
		}
	}
}
