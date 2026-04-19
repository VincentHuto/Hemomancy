package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class SilverBellsTowerFeature extends Feature<NoneFeatureConfiguration> {

	private static final BlockState QUARTZ = Blocks.QUARTZ_BLOCK.defaultBlockState();
	private static final BlockState SMOOTH_QUARTZ = Blocks.SMOOTH_QUARTZ.defaultBlockState();
	private static final BlockState CHISELED_QUARTZ = Blocks.CHISELED_QUARTZ_BLOCK.defaultBlockState();

	public SilverBellsTowerFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		RandomSource random = ctx.random();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		BlockPos base = findGround(level, ctx.origin(), mutable);
		if (base == null || !isStableGround(level, base, mutable)) {
			return false;
		}

		int height = 18 + random.nextInt(13); // 18..30
		if (base.getY() + height + 2 >= level.getMaxBuildHeight()) {
			return false;
		}

		int baseRadius = 2;
		for (int y = 0; y < height; y++) {
			float progress = (float) y / Math.max(1, height - 1);
			float radius = Mth.lerp(progress, baseRadius, 1.0f);
			int intRadius = Mth.ceil(radius);
			for (int dx = -intRadius; dx <= intRadius; dx++) {
				for (int dz = -intRadius; dz <= intRadius; dz++) {
					if (Mth.sqrt(dx * dx + dz * dz) > radius + 0.15f) continue;
					mutable.set(base.getX() + dx, base.getY() + y, base.getZ() + dz);
					BlockState target = level.getBlockState(mutable);
					if (!(target.isAir() || target.canBeReplaced())) continue;

					boolean edge = Math.abs(dx) == intRadius || Math.abs(dz) == intRadius;
					BlockState toPlace = edge ? QUARTZ : SMOOTH_QUARTZ;
					if (edge && y % 6 == 0) {
						toPlace = CHISELED_QUARTZ;
					}
					level.setBlock(mutable, toPlace, 2);
				}
			}
		}

		int topY = base.getY() + height;
		for (int dx = -1; dx <= 1; dx++) {
			for (int dz = -1; dz <= 1; dz++) {
				mutable.set(base.getX() + dx, topY, base.getZ() + dz);
				if (level.getBlockState(mutable).isAir() || level.getBlockState(mutable).canBeReplaced()) {
					level.setBlock(mutable, SMOOTH_QUARTZ, 2);
				}
			}
		}

		BlockPos bellPos = new BlockPos(base.getX(), topY + 1, base.getZ());
		BlockState bellState = level.getBlockState(bellPos);
		if (!(bellState.isAir() || bellState.canBeReplaced())) {
			return false;
		}
		level.setBlock(bellPos, BlockInit.pale_silver_bells.get().defaultBlockState(), 2);
		return true;
	}

	private BlockPos findGround(WorldGenLevel level, BlockPos start, BlockPos.MutableBlockPos mutable) {
		mutable.set(start.getX(), start.getY(), start.getZ());
		for (int dy = 0; dy < 32; dy++) {
			mutable.move(Direction.DOWN);
			BlockState below = level.getBlockState(mutable);
			mutable.move(Direction.UP);
			if (below.isFaceSturdy(level, mutable.below(), Direction.UP) && !below.liquid()) {
				BlockState at = level.getBlockState(mutable);
				if (at.isAir() || at.canBeReplaced()) {
					return mutable.immutable();
				}
			}
			mutable.move(Direction.DOWN);
		}
		return null;
	}

	private boolean isStableGround(WorldGenLevel level, BlockPos base, BlockPos.MutableBlockPos mutable) {
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				mutable.set(base.getX() + dx, base.getY() - 1, base.getZ() + dz);
				BlockState below = level.getBlockState(mutable);
				if (!below.isFaceSturdy(level, mutable, Direction.UP) || below.liquid()) {
					return false;
				}
			}
		}
		return true;
	}
}
