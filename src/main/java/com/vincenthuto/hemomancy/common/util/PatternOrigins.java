package com.vincenthuto.hemomancy.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.pattern.BlockPattern;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Predicate;

/**
 * Where a vanilla {@link BlockPattern} sits in the world. A pattern's origin is its front-top-left cell; cell
 * (x, y, z) lies x steps along {@code forwards × up}, y steps down and z steps along {@code forwards} from it.
 *
 * <p>Two searches are offered because callers rely on different winners when placements overlap:
 * {@link #findContaining} tries each orientation and then each pattern cell on the hit block, and
 * {@link #scanAround} tries every origin in a cube around a block, low corner first, and then each orientation.
 */
public final class PatternOrigins {
	private static final Direction[] DIRECTIONS = Direction.values();

	private PatternOrigins() {
	}

	/** The origin that puts pattern cell (x, y, z) on {@code cell}. */
	public static BlockPos originForCell(BlockPos cell, Direction forwards, Direction up, int x, int y, int z) {
		Vec3i right = forwards.getNormal().cross(up.getNormal());
		return cell.offset(
				up.getStepX() * y - right.getX() * x - forwards.getStepX() * z,
				up.getStepY() * y - right.getY() * x - forwards.getStepY() * z,
				up.getStepZ() * y - right.getZ() * x - forwards.getStepZ() * z);
	}

	/** {@code relative}, given as right, up and forwards steps, moved into the world from {@code origin}. */
	public static BlockPos offset(BlockPos origin, Direction forwards, Direction up, BlockPos relative) {
		Vec3i right = forwards.getNormal().cross(up.getNormal());
		return origin.offset(
				right.getX() * relative.getX() + up.getStepX() * relative.getY() + forwards.getStepX() * relative.getZ(),
				right.getY() * relative.getX() + up.getStepY() * relative.getY() + forwards.getStepY() * relative.getZ(),
				right.getZ() * relative.getX() + up.getStepZ() * relative.getY() + forwards.getStepZ() * relative.getZ());
	}

	/**
	 * The placement with one of its cells on {@code hit}: the first orientation (forwards, then up, in
	 * {@link Direction} order) that fits, and within it the lowest (x, y, z) cell.
	 */
	@Nullable
	public static BlockPattern.BlockPatternMatch findContaining(BlockPattern pattern, LevelReader level, BlockPos hit) {
		for (Direction forwards : DIRECTIONS) {
			for (Direction up : DIRECTIONS) {
				if (up.getAxis() == forwards.getAxis()) continue;
				for (int x = 0; x < pattern.getWidth(); ++x) {
					for (int y = 0; y < pattern.getHeight(); ++y) {
						for (int z = 0; z < pattern.getDepth(); ++z) {
							BlockPos origin = originForCell(hit, forwards, up, x, y, z);
							BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, up);
							if (match != null && match.getBlock(x, y, z).getPos().equals(hit)) return match;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * The first placement {@code accept} takes among origins within the pattern's largest dimension less one of
	 * {@code center}, scanned from the low corner (x fastest, then y, then z), each in every orientation. That cube
	 * holds every origin whose placement could cover {@code center}.
	 */
	@Nullable
	public static BlockPattern.BlockPatternMatch scanAround(BlockPattern pattern, LevelReader level, BlockPos center,
			Predicate<BlockPattern.BlockPatternMatch> accept) {
		int radius = Math.max(Math.max(pattern.getWidth(), pattern.getHeight()), pattern.getDepth()) - 1;
		for (BlockPos origin : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius),
				center.offset(radius, radius, radius))) {
			for (Direction forwards : DIRECTIONS) {
				for (Direction up : DIRECTIONS) {
					if (up.getAxis() == forwards.getAxis()) continue;
					BlockPattern.BlockPatternMatch match = pattern.matches(level, origin, forwards, up);
					if (match != null && accept.test(match)) return match;
				}
			}
		}
		return null;
	}

	/** Whether one of the placement's cells is {@code pos}. */
	public static boolean contains(BlockPattern.BlockPatternMatch match, BlockPattern pattern, BlockPos pos) {
		for (int x = 0; x < pattern.getWidth(); ++x) {
			for (int y = 0; y < pattern.getHeight(); ++y) {
				for (int z = 0; z < pattern.getDepth(); ++z) {
					if (match.getBlock(x, y, z).getPos().equals(pos)) return true;
				}
			}
		}
		return false;
	}

	/** The block-aligned box around every cell of the placement. */
	public static AABB bounds(BlockPattern.BlockPatternMatch match, BlockPattern pattern) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (int x = 0; x < pattern.getWidth(); ++x) {
			for (int y = 0; y < pattern.getHeight(); ++y) {
				for (int z = 0; z < pattern.getDepth(); ++z) {
					BlockPos pos = match.getBlock(x, y, z).getPos();
					minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
					minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
					minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
				}
			}
		}
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}

	/** The block-aligned box around {@code positions}, which must not be empty. */
	public static AABB bounds(Collection<BlockPos> positions) {
		int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
		for (BlockPos pos : positions) {
			minX = Math.min(minX, pos.getX()); maxX = Math.max(maxX, pos.getX());
			minY = Math.min(minY, pos.getY()); maxY = Math.max(maxY, pos.getY());
			minZ = Math.min(minZ, pos.getZ()); maxZ = Math.max(maxZ, pos.getZ());
		}
		return new AABB(minX, minY, minZ, maxX + 1, maxY + 1, maxZ + 1);
	}
}
