package com.vincenthuto.hemomancy.common.rite.harbinger;

import com.vincenthuto.hemomancy.common.recipe.CardinalRiteType;
import com.vincenthuto.hemomancy.common.rite.WorkingCraftBed;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

final class WorkingCraftSubjectMatcherTest {
	private static final BlockPos FOCUS = new BlockPos(0, 64, 0);
	private static final WorkingCraftBed LESSER = WorkingCraftBed.forTier(CardinalRiteType.LESSER);

	@Test
	void aSubjectFillingOneSidesBedRests() {
		assertTrue(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(-1, 1, 1, 2, 1, 2)));
		assertTrue(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(0, 0, 2, 2, 1, 3)),
				"a narrow subject may stand anywhere in the bed, up to three blocks tall");
	}

	@Test
	void aSubjectMayStandAnywhereOnTheFloorBesideTheFocus() {
		// Shifted one block east, the frame's east column sits diagonal to the Focus: still on the floor's north side.
		assertTrue(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(0, 2, 1, 2, 1, 2)));
		// Pushed to the floor's north-east corner.
		assertTrue(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(1, 2, 1, 2, 1, 2)));
	}

	@Test
	void aSubjectPastTheFloorsEdgeRestsInNoBed() {
		// A lesser floor reaches two blocks from the Focus; three blocks east is off it.
		List<BlockPos> overhanging = northBox(2, 3, 1, 2, 1, 2);
		for (Direction side : Direction.Plane.HORIZONTAL) {
			assertFalse(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, side, overhanging), side.getName());
		}
	}

	@Test
	void aFloatingSubjectDoesNotRest() {
		assertFalse(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(-1, 1, 1, 2, 2, 3)),
				"the lowest cells must stand on the floor's upper layer");
	}

	@Test
	void aSubjectReachingOverTheFocusRowDoesNotRest() {
		assertFalse(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(-1, 1, 0, 1, 1, 1)));
		assertFalse(WorkingCraftSubjectMatcher.restsInBed(LESSER, FOCUS, Direction.NORTH, northBox(-1, 1, 1, 3, 1, 1)),
				"a lesser bed is two blocks deep");
	}

	/** Cells north of the Focus: lateral runs east, outward runs north, up runs from the Focus. */
	private static List<BlockPos> northBox(int lateralFrom, int lateralTo, int outwardFrom, int outwardTo,
			int upFrom, int upTo) {
		List<BlockPos> cells = new ArrayList<>();
		for (int lateral = lateralFrom; lateral <= lateralTo; lateral++)
			for (int outward = outwardFrom; outward <= outwardTo; outward++)
				for (int up = upFrom; up <= upTo; up++)
					cells.add(FOCUS.offset(lateral, up, -outward));
		return cells;
	}
}
