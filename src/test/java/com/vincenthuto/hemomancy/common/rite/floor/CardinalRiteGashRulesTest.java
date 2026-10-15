package com.vincenthuto.hemomancy.common.rite.floor;

import com.vincenthuto.hemomancy.common.rite.CardinalRiteRingTuning;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/** Braziers may stand anywhere along a floor's gash: a circle just inside its edge. */
final class CardinalRiteGashRulesTest {
	@Test
	void theGashIsACircleJustInsideTheFloorsEdge() {
		assertEquals(2.24D, CardinalRiteGashRules.radius(2), 1.0e-9D, "the lesser floor keeps the circle it had");
		for (int half = 1; half <= 4; half++) {
			assertTrue(CardinalRiteGashRules.radius(half) < half + 0.5D, "on the floor");
			assertTrue(CardinalRiteGashRules.radius(half) > half - 0.5D, "round its outer ring, not its middle");
		}
	}

	@Test
	void everyCellTheGashCrossesTakesABrazier() {
		int[] expected = {4, 12, 20, 20};
		for (int half = 1; half <= 4; half++) {
			List<BlockPos> cells = CardinalRiteGashRules.cells(half);
			assertEquals(expected[half - 1], cells.size(), "half " + half);
			Set<BlockPos> unique = new HashSet<>(cells);
			assertEquals(cells.size(), unique.size(), "no cell twice");
			double radius = CardinalRiteGashRules.radius(half);
			for (BlockPos cell : cells) {
				assertEquals(1, cell.getY(), "a brazier stands on the floor");
				assertTrue(Math.max(Math.abs(cell.getX()), Math.abs(cell.getZ())) <= half, "on the floor: " + cell);
				assertTrue(Math.abs(Math.hypot(cell.getX(), cell.getZ()) - radius) <= 0.5D, "on the gash: " + cell);
				for (BlockPos image : List.of(new BlockPos(-cell.getX(), 1, cell.getZ()), new BlockPos(cell.getX(), 1, -cell.getZ()),
						new BlockPos(cell.getZ(), 1, cell.getX()))) {
					assertTrue(unique.contains(image), "symmetric: " + cell + " has no " + image);
				}
			}
		}
	}

	@Test
	void noBrazierSpotCoversACellWhereACeremonyAnchorSoreOpens() {
		Set<BlockPos> anchors = new HashSet<>();
		for (int ring = 0; ring < CardinalRiteRingTuning.RADIUS_BLOCKS.length; ring++) {
			for (int order = 0; order < 4; order++) {
				var anchor = CardinalRiteRingTuning.anchor(ring, order, 1);
				anchors.add(new BlockPos(anchor.x(), 1, anchor.z()));
			}
		}
		for (int half = 1; half <= 4; half++) {
			for (BlockPos cell : CardinalRiteGashRules.cells(half)) {
				assertFalse(anchors.contains(cell), "half " + half + " offers a brazier spot on an anchor sore: " + cell);
			}
		}
	}

	@Test
	void cellsRunClockwiseFromTheFocusesLeftSoTheBackFillsFirst() {
		List<BlockPos> lesser = CardinalRiteGashRules.cells(2);
		assertEquals(new BlockPos(-2, 1, 0), lesser.getFirst(), "starts at the Focus's left");
		assertTrue(lesser.get(3).getZ() == 2 && lesser.get(3).getX() == 0, "then round the back (+z)");
		double previous = -1.0D;
		for (BlockPos cell : lesser) {
			double around = CardinalRiteGashRules.around(cell.getX(), cell.getZ());
			assertTrue(around >= previous, "clockwise order");
			previous = around;
		}
	}
}
