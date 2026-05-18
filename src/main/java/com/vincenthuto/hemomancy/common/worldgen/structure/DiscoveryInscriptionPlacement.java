package com.vincenthuto.hemomancy.common.worldgen.structure;

import com.vincenthuto.hemomancy.common.block.inscription.DiscoveryInscriptionBlock;
import com.vincenthuto.hemomancy.common.tile.DiscoveryInscriptionBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public final class DiscoveryInscriptionPlacement {
	private static final int MAX_RADIUS = 9;

	private DiscoveryInscriptionPlacement() {
	}

	public static boolean placeOnInteriorFloor(WorldGenLevel level, BoundingBox fullBox, BlockPos origin,
			Block block, ResourceLocation inscriptionId) {
		int minY = Math.max(fullBox.minY(), origin.getY() - 2);
		int maxY = Math.min(fullBox.maxY(), origin.getY() + 6);

		for (int radius = 0; radius <= MAX_RADIUS; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
						continue;
					}
					for (int y = minY; y <= maxY; y++) {
						BlockPos candidate = origin.offset(dx, y - origin.getY(), dz);
						if (tryPlace(level, fullBox, origin, candidate, block, inscriptionId)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static boolean placeOnInteriorWall(WorldGenLevel level, BoundingBox fullBox, BlockPos origin,
			Block block, ResourceLocation inscriptionId) {
		int minY = Math.max(fullBox.minY(), origin.getY() - 1);
		int maxY = Math.min(fullBox.maxY(), origin.getY() + 5);

		for (int radius = 0; radius <= MAX_RADIUS; radius++) {
			for (int dx = -radius; dx <= radius; dx++) {
				for (int dz = -radius; dz <= radius; dz++) {
					if (Math.abs(dx) != radius && Math.abs(dz) != radius) {
						continue;
					}
					for (int y = minY; y <= maxY; y++) {
						BlockPos candidate = origin.offset(dx, y - origin.getY(), dz);
						if (tryPlaceWall(level, fullBox, candidate, block, inscriptionId)) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	private static boolean tryPlace(WorldGenLevel level, BoundingBox fullBox, BlockPos origin, BlockPos pos,
			Block block, ResourceLocation inscriptionId) {
		if (!fullBox.isInside(pos.getX(), pos.getY(), pos.getZ())) {
			return false;
		}
		if (!canReplaceForInscription(level, pos)) {
			return false;
		}
		BlockPos below = pos.below();
		if (!level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
			return false;
		}

		BlockState state = block.defaultBlockState();
		if (state.hasProperty(DiscoveryInscriptionBlock.FACING)) {
			state = state.setValue(DiscoveryInscriptionBlock.FACING, facingToward(origin, pos));
		}
		level.setBlock(pos, state, Block.UPDATE_CLIENTS);
		if (level.getBlockEntity(pos) instanceof DiscoveryInscriptionBlockEntity inscription) {
			inscription.setInscriptionId(inscriptionId);
		}
		return true;
	}

	private static boolean tryPlaceWall(WorldGenLevel level, BoundingBox fullBox, BlockPos pos,
			Block block, ResourceLocation inscriptionId) {
		if (!fullBox.isInside(pos.getX(), pos.getY(), pos.getZ())) {
			return false;
		}
		if (!canReplaceForInscription(level, pos)) {
			return false;
		}

		for (Direction backingDirection : Direction.Plane.HORIZONTAL) {
			BlockPos backingPos = pos.relative(backingDirection);
			if (!fullBox.isInside(backingPos.getX(), backingPos.getY(), backingPos.getZ())) {
				continue;
			}
			Direction facing = backingDirection.getOpposite();
			if (!level.getBlockState(backingPos).isFaceSturdy(level, backingPos, facing)) {
				continue;
			}

			BlockState state = block.defaultBlockState();
			if (state.hasProperty(DiscoveryInscriptionBlock.FACING)) {
				state = state.setValue(DiscoveryInscriptionBlock.FACING, facing);
			}
			level.setBlock(pos, state, Block.UPDATE_CLIENTS);
			if (level.getBlockEntity(pos) instanceof DiscoveryInscriptionBlockEntity inscription) {
				inscription.setInscriptionId(inscriptionId);
			}
			return true;
		}
		return false;
	}

	private static boolean canReplaceForInscription(WorldGenLevel level, BlockPos pos) {
		return level.getBlockState(pos).isAir() || level.getFluidState(pos).is(FluidTags.WATER);
	}

	private static Direction facingToward(BlockPos origin, BlockPos pos) {
		int dx = origin.getX() - pos.getX();
		int dz = origin.getZ() - pos.getZ();
		if (Math.abs(dx) > Math.abs(dz)) {
			return dx >= 0 ? Direction.EAST : Direction.WEST;
		}
		return dz >= 0 ? Direction.SOUTH : Direction.NORTH;
	}
}
