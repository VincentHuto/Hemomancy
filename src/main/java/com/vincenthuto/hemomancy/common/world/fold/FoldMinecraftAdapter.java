package com.vincenthuto.hemomancy.common.world.fold;

import com.vincenthuto.hemomancy.common.block.harbinger.functional.NonEuclideanHallwayBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class FoldMinecraftAdapter {
	private FoldMinecraftAdapter() {
	}

	public static FoldFacing fromDirection(Direction direction) {
		return switch (direction) {
			case NORTH -> FoldFacing.NORTH;
			case SOUTH -> FoldFacing.SOUTH;
			case WEST -> FoldFacing.WEST;
			case EAST -> FoldFacing.EAST;
			default -> throw new IllegalArgumentException("Folded hallway facing must be horizontal: " + direction);
		};
	}

	public static FoldedHallwaySpec fromController(ResourceKey<Level> dimension, BlockPos anchor, BlockState state) {
		return FoldedHallwaySpec.prototype(dimension.location().toString(), anchor.getX(), anchor.getY(),
				anchor.getZ(), fromDirection(state.getValue(NonEuclideanHallwayBlock.FACING)));
	}

	public static FoldVector fromVec3(Vec3 vec) {
		return new FoldVector(vec.x, vec.y, vec.z);
	}

	public static Vec3 toVec3(FoldVector vector) {
		return new Vec3(vector.x, vector.y, vector.z);
	}
}
