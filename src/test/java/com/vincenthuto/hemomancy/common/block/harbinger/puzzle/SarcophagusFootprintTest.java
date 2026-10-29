package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.shared.HorizontalFacingRotationHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** The tomb is two blocks wide and four long, and its fillers turn with it exactly as its model and outline do. */
final class SarcophagusFootprintTest {
	@Test
	void theFillersStandUnderEveryPartOfTheTombInEveryFacing() {
		assertEquals(23, SarcophagusGeometry.FILLERS.length, "a 3x4x2 grid of cells less the main block");
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			Set<BlockPos> fillers = new HashSet<>();
			for (BlockPos offset : HorizontalFacingRotationHelper.rotateNorthOffsets(SarcophagusGeometry.FILLERS, facing)) {
				fillers.add(offset);
				assertFalse(SarcophagusGeometry.shape(facing, offset).isEmpty(),
						facing + " filler at " + offset + " would be an invisible full block");
			}
			for (BlockPos north : SarcophagusGeometry.FILLERS) {
				double[] centre = SarcophagusGeometry.point(facing, north.getX() * 16 + 8, 8, north.getZ() * 16 + 8);
				BlockPos turned = new BlockPos((int) Math.floor(centre[0]), north.getY(), (int) Math.floor(centre[2]));
				assertTrue(fillers.contains(turned), facing + ": the model's cell " + turned + " has no filler");
			}
		}
	}

	@Test
	void theFootFacesTheVisitorAndTheSaintRisesMidTomb() {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			double[] bowl = SarcophagusGeometry.point(facing, SarcophagusGeometry.BOWL_X, 0, SarcophagusGeometry.BOWL_Z);
			double[] centre = SarcophagusGeometry.point(facing, SarcophagusGeometry.CENTRE_X, 0, SarcophagusGeometry.CENTRE_Z);
			double along = (bowl[0] - centre[0]) * facing.getStepX() + (bowl[2] - centre[2]) * facing.getStepZ();
			assertTrue(along > 1.5, facing + ": the bowl is not at the end the tomb faces");
		}
		double[] north = SarcophagusGeometry.point(Direction.NORTH, SarcophagusGeometry.CENTRE_X, 0,
				SarcophagusGeometry.CENTRE_Z);
		assertEquals(0.5, north[0], 1e-9, "the tomb is centred on its block across its width");
		assertEquals(1.0, north[2], 1e-9, "the tomb's middle is a cell behind its block, between the second and third");
	}
}
