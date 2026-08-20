package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.block.harbinger.plant.RafflesiaBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class RafflesiaFeature extends Feature<NoneFeatureConfiguration> {
	private static final int SCAN_RADIUS = 6;
	private static final int SCAN_DEPTH = 48;

	public RafflesiaFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel level = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();
		int width = SCAN_RADIUS * 2 + 1;
		int start = random.nextInt(width * width);
		BlockPos.MutableBlockPos logPos = new BlockPos.MutableBlockPos();

		for (int i = 0; i < width * width; i++) {
			int index = (start + i) % (width * width);
			int x = origin.getX() + index % width - SCAN_RADIUS;
			int z = origin.getZ() + index / width - SCAN_RADIUS;
			int top = Math.min(level.getMaxBuildHeight() - 1, origin.getY() + 4);
			int bottom = Math.max(level.getMinBuildHeight(), origin.getY() - SCAN_DEPTH);

			for (int y = top; y >= bottom; y--) {
				logPos.set(x, y, z);
				BlockPos belowPos = logPos.below();
				BlockState below = level.getBlockState(belowPos);
				if (RafflesiaPlacementRules.isGroundedBase(level.getBlockState(logPos).is(BlockTags.LOGS), below.is(BlockTags.LOGS),
						below.isFaceSturdy(level, belowPos, Direction.UP))
						&& placeOnExposedSide(level, logPos, random)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean placeOnExposedSide(WorldGenLevel level, BlockPos base, RandomSource random) {
		int firstHeight = random.nextInt(3);
		Direction firstDirection = Direction.Plane.HORIZONTAL.getRandomDirection(random);
		for (int heightOffset = 0; heightOffset < 3; heightOffset++) {
			BlockPos supportPos = base.above((firstHeight + heightOffset) % 3);
			if (!level.getBlockState(supportPos).is(BlockTags.LOGS)) {
				continue;
			}
			Direction outward = firstDirection;
			for (int side = 0; side < 4; side++) {
				BlockPos flowerPos = supportPos.relative(outward);
				BlockState existing = level.getBlockState(flowerPos);
				BlockState rafflesia = BlockInit.rafflesia.get().defaultBlockState()
						.setValue(RafflesiaBlock.FACING, outward);
				if ((existing.isAir() || existing.canBeReplaced()) && rafflesia.canSurvive(level, flowerPos)) {
					level.setBlock(flowerPos, rafflesia, 2);
					return true;
				}
				outward = outward.getClockWise();
			}
		}
		return false;
	}
}
