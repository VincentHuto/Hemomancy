package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** A rite counts braziers anywhere in the N×N×N space above its floor, the Focus at its bottom centre. */
final class CardinalRiteBrazierSpaceTest {
	@Test
	void theSpaceIsTheCubeAboveTheFloorLessTheFocusColumnAndAnchorSores() {
		Set<BlockPos> anchors = anchorCells();
		for (int half = 1; half <= 4; half++) {
			int size = 2 * half + 1;
			List<BlockPos> cells = CardinalRiteBrazierSpace.cells(half);
			int anchorsOnFloor = 0;
			for (int x = -half; x <= half; x++) {
				for (int z = -half; z <= half; z++) {
					if ((x != 0 || z != 0) && anchors.contains(new BlockPos(x, 1, z))) anchorsOnFloor++;
				}
			}
			assertEquals((size * size - 1) * (size - 1) - anchorsOnFloor, cells.size(), "half " + half);
			assertEquals(cells.size(), new HashSet<>(cells).size(), "no cell twice");
			for (BlockPos cell : cells) {
				assertTrue(Math.abs(cell.getX()) <= half && Math.abs(cell.getZ()) <= half, "inside the floor: " + cell);
				assertTrue(cell.getY() >= 1 && cell.getY() <= 2 * half, "inside the cube: " + cell);
				assertFalse(cell.getX() == 0 && cell.getZ() == 0, "the Focus column stays clear: " + cell);
				if (cell.getY() == 1) assertFalse(anchors.contains(cell), "an anchor sore opens there: " + cell);
			}
		}
	}

	@Test
	void theGashSpotsAreAllInTheSpace() {
		for (int half = 1; half <= 4; half++) {
			assertTrue(new HashSet<>(CardinalRiteBrazierSpace.cells(half)).containsAll(CardinalRiteGashRules.cells(half)),
					"half " + half);
		}
	}

	@Test
	void cellsRunClockwiseFromTheFocusesLeftThenNearestThenLowest() {
		List<BlockPos> cells = CardinalRiteBrazierSpace.cells(2);
		assertEquals(new BlockPos(-1, 1, 0), cells.getFirst(), "the nearest, lowest cell at the Focus's left");
		for (int i = 1; i < cells.size(); i++) {
			BlockPos a = cells.get(i - 1);
			BlockPos b = cells.get(i);
			double aroundA = CardinalRiteGashRules.around(a.getX(), a.getZ());
			double aroundB = CardinalRiteGashRules.around(b.getX(), b.getZ());
			double nearA = Math.hypot(a.getX(), a.getZ());
			double nearB = Math.hypot(b.getX(), b.getZ());
			assertTrue(aroundA < aroundB || aroundA == aroundB && (nearA < nearB || nearA == nearB && a.getY() < b.getY()),
					a + " then " + b);
		}
	}

	private static Set<BlockPos> anchorCells() {
		Set<BlockPos> anchors = new HashSet<>();
		for (int ring = 0; ring < CardinalRiteRingTuning.RADIUS_BLOCKS.length; ring++) {
			for (int order = 0; order < 4; order++) {
				var anchor = CardinalRiteRingTuning.anchor(ring, order, 1);
				anchors.add(new BlockPos(anchor.x(), 1, anchor.z()));
			}
		}
		return anchors;
	}
}
