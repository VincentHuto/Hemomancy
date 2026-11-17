package com.vincenthuto.hemomancy.common.capability.player.harbinger.cling;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * Where a body can cling, read from the level on either side ({@link ClingRules} has the numbers). A face holds when
 * its block is loaded and that face is sturdy; a body touches it when the body, reached out {@link ClingRules#CONTACT}
 * toward the face, overlaps the block.
 */
public final class ClingSurface {
	private ClingSurface() {
	}

	/** Whether {@code face} of the block at {@code pos} can be clung to. */
	public static boolean holds(BlockGetter level, BlockPos pos, Direction face) {
		if (level instanceof Level loaded && !loaded.isLoaded(pos)) return false;
		return ClingRules.Surface.forFace(face) != ClingRules.Surface.NONE && level.getBlockState(pos).isFaceSturdy(level, pos, face);
	}

	/** Whether {@code player} is touching {@code face} of the block at {@code pos}, close enough to root to it. */
	public static boolean touching(Player player, BlockPos pos, Direction face) {
		if (!holds(player.level(), pos, face)) return false;
		AABB reach = reach(player.getBoundingBox(), face);
		AABB block = new AABB(pos);
		return reach.intersects(block) && !player.getBoundingBox().deflate(1.0E-3D).intersects(block);
	}

	/**
	 * The block the body holds now on {@code face}: of the blocks it touches whose face holds, the nearest to the
	 * body's middle. Empty when the body has crawled off the edge or the face was broken.
	 */
	public static Optional<BlockPos> find(Player player, Direction face) {
		AABB body = player.getBoundingBox();
		AABB reach = reach(body, face);
		Vec3 middle = body.getCenter();
		BlockPos best = null;
		double bestDistance = Double.MAX_VALUE;
		for (BlockPos pos : BlockPos.betweenClosed(BlockPos.containing(reach.minX, reach.minY, reach.minZ),
				BlockPos.containing(reach.maxX, reach.maxY, reach.maxZ))) {
			if (!touching(player, pos, face)) continue;
			double distance = Vec3.atCenterOf(pos).distanceToSqr(middle);
			if (distance < bestDistance) {
				bestDistance = distance;
				best = pos.immutable();
			}
		}
		return Optional.ofNullable(best);
	}

	/**
	 * Holdfast's test: the body is pressed against a wall that holds (any horizontal face it touches), not just any
	 * collision.
	 */
	public static boolean againstWall(Player player) {
		if (!player.horizontalCollision) return false;
		for (Direction face : Direction.Plane.HORIZONTAL) {
			if (find(player, face).isPresent()) return true;
		}
		return false;
	}

	/** The body reached out toward the block whose {@code face} it holds (the block lies on the far side of the face). */
	private static AABB reach(AABB body, Direction face) {
		Vec3 toward = Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(ClingRules.CONTACT);
		return body.expandTowards(toward);
	}
}
