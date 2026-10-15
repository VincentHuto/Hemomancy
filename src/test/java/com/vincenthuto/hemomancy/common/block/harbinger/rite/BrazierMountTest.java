package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** An Iron Brazier stands on a block, mounts on the side of one, or hangs from the underside of one. */
final class BrazierMountTest {
	private static final BlockPos POS = new BlockPos(10, 64, 10);

	@Test
	void theBowlDropsLowerOnAWallAndLowerStillHanging() {
		assertEquals(0.0D, BrazierMount.drop(AttachFace.FLOOR));
		assertEquals(-0.25D, BrazierMount.drop(AttachFace.WALL));
		assertEquals(-0.375D, BrazierMount.drop(AttachFace.CEILING));
		assertEquals(Vec3.atCenterOf(POS).add(0.0D, 0.75D - 0.375D, 0.0D),
				BrazierMount.bowl(AttachFace.CEILING, POS, 0.75D));
	}

	@Test
	void theFaceLookedTowardChoosesTheMount() {
		assertEquals(AttachFace.FLOOR, BrazierMount.placement(Direction.DOWN), "looking down onto a block's top");
		assertEquals(AttachFace.CEILING, BrazierMount.placement(Direction.UP), "looking up at a block's underside");
		for (Direction side : Direction.Plane.HORIZONTAL) {
			assertEquals(AttachFace.WALL, BrazierMount.placement(side), "looking at a wall " + side);
		}
		assertEquals(Direction.DOWN, BrazierMount.support(AttachFace.FLOOR, Direction.NORTH));
		assertEquals(Direction.UP, BrazierMount.support(AttachFace.CEILING, Direction.NORTH));
		assertEquals(Direction.SOUTH, BrazierMount.support(AttachFace.WALL, Direction.NORTH),
				"a north-facing sconce hangs on the wall to its south");
	}

	@Test
	void mountedShapesStayInTheirBlockAndReachTheirSupport() {
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			AABB wall = BrazierMount.shape(AttachFace.WALL, facing).bounds();
			assertTrue(wall.minX >= 0 && wall.minY >= 0 && wall.minZ >= 0 && wall.maxX <= 1 && wall.maxY <= 1
					&& wall.maxZ <= 1, "a sconce stays in its block: " + wall);
			Direction back = facing.getOpposite();
			double reach = switch (back) {
				case NORTH -> -wall.minZ;
				case SOUTH -> wall.maxZ;
				case WEST -> -wall.minX;
				default -> wall.maxX;
			};
			assertEquals(back.getAxisDirection() == Direction.AxisDirection.POSITIVE ? 1.0D : 0.0D,
					Math.abs(reach), 1.0e-6D, "a sconce facing " + facing + " reaches its wall");
			AABB hanging = BrazierMount.shape(AttachFace.CEILING, facing).bounds();
			assertTrue(hanging.minY >= 0 && hanging.maxY == 1.0D, "a hanging brazier reaches its ceiling: " + hanging);
		}
		assertEquals(0.0D, BrazierMount.shape(AttachFace.FLOOR, Direction.NORTH).bounds().minY, "stands on its block");
	}

	@Test
	void theDaemonStopsWhereItCanReachTheBowl() {
		Vec3 focus = Vec3.atCenterOf(POS.offset(-3, -4, 0));
		assertEquals(Vec3.atCenterOf(POS).add(0.0D, 1.2D, 0.0D),
				BrazierMount.daemonStop(AttachFace.FLOOR, Direction.NORTH, POS, focus), "over a standing bowl, as before");
		Vec3 sconce = BrazierMount.daemonStop(AttachFace.WALL, Direction.NORTH, POS, focus);
		assertTrue(sconce.z < POS.getZ(), "out in front of a north-facing sconce: " + sconce);
		Vec3 bowl = BrazierMount.bowl(AttachFace.CEILING, POS, 0.75D);
		Vec3 hanging = BrazierMount.daemonStop(AttachFace.CEILING, Direction.NORTH, POS, focus);
		assertTrue(hanging.y < bowl.y, "below a hanging bowl, clear of the ceiling: " + hanging);
		assertTrue(hanging.distanceToSqr(focus) < bowl.distanceToSqr(focus), "toward the Focus: " + hanging);
	}
}
