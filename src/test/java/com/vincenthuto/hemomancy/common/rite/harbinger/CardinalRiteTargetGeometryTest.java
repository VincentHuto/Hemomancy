package com.vincenthuto.hemomancy.common.rite.harbinger;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class CardinalRiteTargetGeometryTest {
	@Test
	void anchorAimPointMatchesTheRenderedRitePlane() {
		Vec3 point = CardinalRiteTargetGeometry.anchorAimPoint(
				new BlockPos(20, 64, -10), new BlockPos(3, 1, 0));

		assertEquals(23.5D, point.x, 0.0001D);
		assertEquals(CardinalRiteAnchorVisualRules.ritePlaneY(64), point.y, 0.0001D);
		assertEquals(-9.5D, point.z, 0.0001D);
	}

	@Test
	void fractionalSigilAimPointMatchesItsRenderedSurfaceMarker() {
		Vec3 point = CardinalRiteTargetGeometry.sigilAimPoint(
				new BlockPos(20, 64, -10), new BlockPos(24, 67, -11),
				4, 0, 0.0D, -1.5D);

		assertEquals(24.5D, point.x, 0.0001D);
		assertEquals(67.08D, point.y, 0.0001D);
		assertEquals(-11.0D, point.z, 0.0001D);
	}
}
