package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public final class HorizontalFacingRotationHelper {
	private HorizontalFacingRotationHelper() {
	}

	/**
	 * Rotates an offset defined in SOUTH-facing local space into world space for the given horizontal facing.
	 */
	public static BlockPos rotateSouthOffset(BlockPos offset, Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
			case EAST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
			case WEST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
			default -> offset;
		};
	}

	public static BlockPos[] rotateSouthOffsets(BlockPos[] offsets, Direction facing) {
		BlockPos[] rotated = new BlockPos[offsets.length];
		for (int i = 0; i < offsets.length; i++) {
			rotated[i] = rotateSouthOffset(offsets[i], facing);
		}
		return rotated;
	}

	/**
	 * Rotates an offset defined in SOUTH-facing local space into world space using the reversed east/west
	 * convention required by a few legacy multiblocks.
	 */
	public static BlockPos rotateSouthOffsetReversed(BlockPos offset, Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
			case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
			case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
			default -> offset;
		};
	}

	public static BlockPos[] rotateSouthOffsetsReversed(BlockPos[] offsets, Direction facing) {
		BlockPos[] rotated = new BlockPos[offsets.length];
		for (int i = 0; i < offsets.length; i++) {
			rotated[i] = rotateSouthOffsetReversed(offsets[i], facing);
		}
		return rotated;
	}

	/**
	 * Rotates an offset defined in NORTH-facing local space into world space for the given horizontal facing.
	 */
	public static BlockPos rotateNorthOffset(BlockPos offset, Direction facing) {
		return switch (facing) {
			case SOUTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
			case EAST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
			case WEST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
			default -> offset;
		};
	}

	public static BlockPos[] rotateNorthOffsets(BlockPos[] offsets, Direction facing) {
		BlockPos[] rotated = new BlockPos[offsets.length];
		for (int i = 0; i < offsets.length; i++) {
			rotated[i] = rotateNorthOffset(offsets[i], facing);
		}
		return rotated;
	}
}


