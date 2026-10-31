package com.vincenthuto.hemomancy.client.render.world;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

class FaneGroundGeometryTest {
	private static final BlockPos HEART = new BlockPos(0, 64, 0);
	private static final BlockPos STAKE = new BlockPos(60, 64, 0);
	private static final double HEART_RADIUS = 40.0D;
	private static final double STAKE_RADIUS = 32.0D;

	private static double depth(double x, double z) {
		return FaneGroundGeometry.depth(HEART, HEART_RADIUS, List.of(STAKE), STAKE_RADIUS, x, z);
	}

	private static double hardUnion(double x, double z) {
		return Math.max(FaneGroundGeometry.depthIn(HEART, HEART_RADIUS, x, z),
				FaneGroundGeometry.depthIn(STAKE, STAKE_RADIUS, x, z));
	}

	@Test
	void awayFromAJoinTheEdgeIsTheServers() {
		// The heart's far side and the stake's far side: one lobe only, so exactly the server's circle.
		assertEquals(hardUnion(-39.5D, 0.5D), depth(-39.5D, 0.5D), 1.0E-9);
		assertEquals(hardUnion(92.0D, 0.5D), depth(92.0D, 0.5D), 1.0E-9);
		assertTrue(depth(-41.5D, 0.5D) < 0.0D, "outside the heart's far side");
	}

	@Test
	void aJoinIsRoundedOutwardButOnlyALittle() {
		// Where the two circles cross, the hard union has a notch; the drawn edge fills it with a neck.
		double along = (HEART_RADIUS * HEART_RADIUS - STAKE_RADIUS * STAKE_RADIUS + 60.0D * 60.0D) / (2.0D * 60.0D);
		double creaseX = 0.5D + along;
		double creaseZ = 0.5D + Math.sqrt(HEART_RADIUS * HEART_RADIUS - along * along);
		double outside = creaseZ + 1.0D;
		assertTrue(hardUnion(creaseX, outside) < 0.0D, "just past the notch is outside the hard union");
		assertTrue(depth(creaseX, outside) > 0.0D, "the neck covers it");
		for (double x = -40.0D; x <= 92.0D; x += 0.5D) {
			for (double z = -40.0D; z <= 40.0D; z += 0.5D) {
				double gap = depth(x, z) - hardUnion(x, z);
				assertTrue(gap >= 0.0D && gap <= FaneGroundGeometry.BLEND * 0.25D + 1.0E-9,
						"the neck never pulls in and never reaches past BLEND / 4 at " + x + ", " + z);
			}
		}
	}

	@Test
	void theEdgeHasNoStepsAcrossAJoin() {
		double previous = depth(0.5D, 30.0D);
		for (double x = 0.5D; x <= 60.5D; x += 0.05D) {
			double next = depth(x, 30.0D);
			assertTrue(Math.abs(next - previous) < 0.1D, "a jump in depth at x " + x);
			previous = next;
		}
	}

	@Test
	void glyphsFaceTheLobeTheyLieDeepestIn() {
		assertEquals(FaneGroundGeometry.WEST, FaneGroundGeometry.facing(HEART, HEART_RADIUS, List.of(STAKE),
				STAKE_RADIUS, 30.0D, 0.5D), "inside the heart's ring, a glyph faces the heart");
		assertEquals(FaneGroundGeometry.NORTH, FaneGroundGeometry.facing(HEART, HEART_RADIUS, List.of(STAKE),
				STAKE_RADIUS, 60.5D, 30.0D), "on the stake's edge, it faces the stake");
	}
}
