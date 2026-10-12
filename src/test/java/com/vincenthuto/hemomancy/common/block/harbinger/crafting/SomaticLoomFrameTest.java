package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.block.harbinger.puzzle.SarcophagusGeometry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SomaticLoomFrameTest {
	private static void assertVec(double x, double y, double z, Vec3 v) {
		assertEquals(x, v.x, 1e-9);
		assertEquals(y, v.y, 1e-9);
		assertEquals(z, v.z, 1e-9);
	}

	@Test
	void northFacingIsTheModelFrame() {
		assertVec(10.5, 21.0, 30.25, SomaticLoomFrame.toWorld(new BlockPos(10, 20, 30), Direction.NORTH, 8, 16, 4));
	}

	/** The blockstate turns the model the same way the sarcophagus's generated anchors are turned. */
	@Test
	void everyFacingMatchesTheBlockstateConvention() {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			double[] expected = SarcophagusGeometry.point(facing, 3, 5, 11);
			assertVec(expected[0], expected[1], expected[2], SomaticLoomFrame.toWorld(BlockPos.ZERO, facing, 3, 5, 11));
		}
	}
}
