package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.block.harbinger.ErythrocoralGrowthBlock;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class ErythrocoralReefFeature extends Feature<NoneFeatureConfiguration> {
	public ErythrocoralReefFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
		WorldGenLevel level = ctx.level();
		RandomSource random = ctx.random();
		BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
		BlockPos floor = findReefFloor(level, ctx.origin(), mutable);
		if (floor == null || !hasStableShelf(level, floor, mutable)) {
			return false;
		}

		int clusters = 2 + random.nextInt(3);
		for (int i = 0; i < clusters; i++) {
			float angle = random.nextFloat() * Mth.TWO_PI;
			int distance = i == 0 ? 0 : 2 + random.nextInt(5);
			int x = floor.getX() + Mth.floor(Mth.cos(angle) * distance);
			int z = floor.getZ() + Mth.floor(Mth.sin(angle) * distance);
			BlockPos localFloor = findLocalFloor(level, x, z, floor.getY() + 6, mutable);
			if (localFloor != null && Math.abs(localFloor.getY() - floor.getY()) <= ErythrocoralReefRules.MAX_FLOOR_DELTA) {
				buildCluster(level, localFloor, 3 + random.nextInt(3), random, mutable);
			}
		}
		return true;
	}

	private BlockPos findReefFloor(WorldGenLevel level, BlockPos origin, BlockPos.MutableBlockPos mutable) {
		int startY = Math.min(level.getSeaLevel() + 3, level.getMaxBuildHeight() - 2);
		return findLocalFloor(level, origin.getX(), origin.getZ(), Math.max(origin.getY() + 3, startY), mutable);
	}

	private BlockPos findLocalFloor(WorldGenLevel level, int x, int z, int startY, BlockPos.MutableBlockPos mutable) {
		int yStart = Math.min(startY, level.getMaxBuildHeight() - 2);
		for (int y = yStart; y > level.getMinBuildHeight() + 1; y--) {
			mutable.set(x, y, z);
			BlockState originState = level.getBlockState(mutable);
			BlockState aboveState = level.getBlockState(mutable.above());
			mutable.move(Direction.DOWN);
			BlockState below = level.getBlockState(mutable);
			boolean sturdyFloor = below.isFaceSturdy(level, mutable, Direction.UP);
			boolean valid = ErythrocoralReefRules.canUseFloor(isWaterSource(originState), isWaterSource(aboveState),
					sturdyFloor, below.liquid(), waterDepthAbove(level, mutable.above()));
			mutable.move(Direction.UP);
			if (valid) {
				return mutable.immutable();
			}
		}
		return null;
	}

	private int waterDepthAbove(WorldGenLevel level, BlockPos pos) {
		int depth = 0;
		BlockPos.MutableBlockPos probe = pos.mutable();
		int top = Math.min(level.getSeaLevel() + 2, level.getMaxBuildHeight() - 1);
		while (probe.getY() <= top && isWaterSource(level.getBlockState(probe))) {
			depth++;
			probe.move(Direction.UP);
		}
		return depth;
	}

	private boolean hasStableShelf(WorldGenLevel level, BlockPos center, BlockPos.MutableBlockPos mutable) {
		int found = 0;
		for (int dx = -2; dx <= 2; dx++) {
			for (int dz = -2; dz <= 2; dz++) {
				BlockPos local = findLocalFloor(level, center.getX() + dx, center.getZ() + dz, center.getY() + 5, mutable);
				if (local != null && Math.abs(local.getY() - center.getY()) <= ErythrocoralReefRules.MAX_FLOOR_DELTA) {
					found++;
				}
			}
		}
		return found >= 15;
	}

	private void buildCluster(WorldGenLevel level, BlockPos center, int radius, RandomSource random,
			BlockPos.MutableBlockPos mutable) {
		for (int dx = -radius; dx <= radius; dx++) {
			for (int dz = -radius; dz <= radius; dz++) {
				if (!ErythrocoralReefRules.isInsideReefCluster(dx, dz, radius)) {
					continue;
				}
				float dist = Mth.sqrt(dx * dx + dz * dz);
				if (dist > radius - 0.35F + random.nextFloat() * 0.9F) {
					continue;
				}
				BlockPos floor = findLocalFloor(level, center.getX() + dx, center.getZ() + dz, center.getY() + 5, mutable);
				if (floor == null || Math.abs(floor.getY() - center.getY()) > ErythrocoralReefRules.MAX_FLOOR_DELTA) {
					continue;
				}

				mutable.set(floor);
				if (placeIfSubmerged(level, mutable, chooseBaseBlock(random, dist, radius))) {
					if (random.nextFloat() < growthChance(dist, radius)) {
						mutable.move(Direction.UP);
						placeGrowth(level, mutable, random);
					}
				}
			}
		}
	}

	private float growthChance(float dist, int radius) {
		return dist < radius * 0.55F ? 0.62F : 0.34F;
	}

	private BlockState chooseBaseBlock(RandomSource random, float dist, int radius) {
		if (dist > radius * 0.72F && random.nextInt(3) != 0) {
			return BlockInit.calcified_erythrocoral.get().defaultBlockState();
		}
		int roll = random.nextInt(42);
		if (roll == 0) {
			return BlockInit.sporite_crystal.get().defaultBlockState();
		}
		if (roll <= 2) {
			return BlockInit.conscious_mass.get().defaultBlockState();
		}
		if (roll <= 6) {
			return BlockInit.calcified_hyphae.get().defaultBlockState();
		}
		return BlockInit.erythrocoral_block.get().defaultBlockState();
	}

	private void placeGrowth(WorldGenLevel level, BlockPos.MutableBlockPos pos, RandomSource random) {
		if (!isWaterSource(level.getBlockState(pos)) || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
			return;
		}
		BlockState state = (random.nextInt(3) == 0 ? BlockInit.erythrocoral_tendril.get().defaultBlockState()
				: BlockInit.erythrocoral_fan.get().defaultBlockState())
				.setValue(ErythrocoralGrowthBlock.WATERLOGGED, Boolean.TRUE);
		level.setBlock(pos, state, 2);
	}

	private static boolean isWaterSource(BlockState state) {
		return state.getFluidState().is(FluidTags.WATER) && state.getFluidState().isSource();
	}

	private static boolean placeIfSubmerged(WorldGenLevel level, BlockPos.MutableBlockPos pos, BlockState state) {
		BlockState existing = level.getBlockState(pos);
		if (existing.isAir() || existing.canBeReplaced() || existing.getFluidState().is(FluidTags.WATER)) {
			level.setBlock(pos, state, 2);
			return true;
		}
		return false;
	}
}
