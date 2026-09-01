package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.block.harbinger.functional.EarthenVeinBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * World-gen feature that places a single earthen vein block on solid ground.
 * The vein gets a randomly generated anatomical name on its first tick via
 * {@link com.vincenthuto.hemomancy.common.tile.harbinger.functional.EarthenVeinBlockEntity}.
 */
public class EarthenVeinFeature extends Feature<NoneFeatureConfiguration> {

	private static final Direction[] HORIZONTALS = Direction.Plane.HORIZONTAL.stream().toArray(Direction[]::new);

	public EarthenVeinFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		BlockPos placePos = findSurface(level, origin, 16);
		if (placePos == null) {
			return false;
		}

		Direction facing = HORIZONTALS[random.nextInt(HORIZONTALS.length)];
		BlockState veinState = BlockInit.earthen_vein.get().defaultBlockState()
				.setValue(EarthenVeinBlock.FACING, facing)
				.setValue(EarthenVeinBlock.STENTED, false)
				.setValue(EarthenVeinBlock.NAMED, false)
				.setValue(EarthenVeinBlock.WATERLOGGED, false);

		level.setBlock(placePos, veinState, 2);
		return true;
	}

	/**
	 * Scans downward from the origin to find a position where:
	 * - The block below is solid on top
	 * - The position itself (and the block above) are air or replaceable
	 */
	private BlockPos findSurface(WorldGenLevel level, BlockPos start, int maxDepth) {
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos(start.getX(), start.getY(), start.getZ());
		BlockPos.MutableBlockPos below = new BlockPos.MutableBlockPos();

		for (int dy = 0; dy < maxDepth; dy++) {
			below.setWithOffset(mutable, Direction.DOWN);
			BlockState current = level.getBlockState(mutable);
			BlockState belowState = level.getBlockState(below);

			if ((current.isAir() || current.canBeReplaced())
					&& belowState.isFaceSturdy(level, below, Direction.UP)
					&& belowState.getFluidState().isEmpty()) {
				return mutable.immutable();
			}

			mutable.move(Direction.DOWN);
		}
		return null;
	}
}
