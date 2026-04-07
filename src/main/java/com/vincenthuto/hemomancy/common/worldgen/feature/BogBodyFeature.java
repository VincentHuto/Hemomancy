package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.block.BogBodyBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.material.Fluids;

/**
 * World-gen feature that places bog body blocks underwater on solid ground.
 * Spawns 1–3 bodies per attempt in a small cluster, each with a random
 * horizontal facing and waterlogged by default.
 */
public class BogBodyFeature extends Feature<NoneFeatureConfiguration> {

	private static final Direction[] HORIZONTALS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

	public BogBodyFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		// Attempt to place 1–3 bog bodies in a small cluster
		int count = 1 + random.nextInt(3);
		boolean placed = false;

		for (int i = 0; i < count; i++) {
			// Random offset within ~4 blocks of origin
			int dx = random.nextInt(9) - 4;
			int dz = random.nextInt(9) - 4;
			BlockPos candidate = origin.offset(dx, 0, dz);

			// Scan downward up to 8 blocks to find a valid floor
			BlockPos placePos = findUnderwaterFloor(level, candidate, 8);
			if (placePos != null) {
				Direction facing = HORIZONTALS[random.nextInt(HORIZONTALS.length)];
				BlockState bogState = BlockInit.bog_body.get().defaultBlockState()
						.setValue(BogBodyBlock.FACING, facing)
						.setValue(BogBodyBlock.WATERLOGGED, true)
						.setValue(BogBodyBlock.HARVESTED, false);

				level.setBlock(placePos, bogState, 2);
				placed = true;
			}
		}

		return placed;
	}

	/**
	 * Scans downward from the given position to find a spot where:
	 * - The block below is solid (face sturdy on top)
	 * - The position itself is water
	 * Returns the position to place the bog body, or null if none found.
	 */
	private BlockPos findUnderwaterFloor(WorldGenLevel level, BlockPos start, int maxDepth) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(start.getX(), start.getY(), start.getZ());

		for (int dy = 0; dy < maxDepth; dy++) {
			BlockState currentState = level.getBlockState(mutable);
			BlockState belowState = level.getBlockState(mutable.below());

			// Check: current position has water, block below is solid
			if (currentState.getFluidState().is(Fluids.WATER)
					&& belowState.isFaceSturdy(level, mutable.below(), Direction.UP)) {
				return mutable.immutable();
			}

			mutable.move(Direction.DOWN);
		}

		return null;
	}
}
