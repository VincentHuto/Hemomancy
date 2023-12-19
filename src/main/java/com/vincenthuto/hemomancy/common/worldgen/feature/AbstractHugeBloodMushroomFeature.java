package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.HugeMushroomFeatureConfiguration;

public abstract class AbstractHugeBloodMushroomFeature extends Feature<HugeMushroomFeatureConfiguration> {
	public AbstractHugeBloodMushroomFeature(Codec<HugeMushroomFeatureConfiguration> pCodec) {
		super(pCodec);
	}

	protected void placeTrunk(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos,
			HugeMushroomFeatureConfiguration pConfig, int pMaxHeight, BlockPos.MutableBlockPos pMutablePos) {
		for (int i = 0; i < pMaxHeight; ++i) {
			pMutablePos.set(pPos).move(Direction.UP, i);
			if (!pLevel.getBlockState(pMutablePos).isSolidRender(pLevel, pMutablePos)) {
				this.setBlock(pLevel, pMutablePos, pConfig.stemProvider.getState(pRandom, pPos));
			}
		}

	}

	protected void placeBulb(FeaturePlaceContext<HugeMushroomFeatureConfiguration> pContext, LevelAccessor pLevel,
			RandomSource pRandom, BlockPos pPos, HugeMushroomFeatureConfiguration pConfig, int pMaxHeight,
			BlockPos.MutableBlockPos pMutablePos) {
		BlockPos blockpos = pContext.origin();
		WorldGenLevel worldgenlevel = pContext.level();
		RandomSource randomsource = pContext.random();

		HugeMushroomFeatureConfiguration blockstateconfiguration;
		for (blockstateconfiguration = pContext.config(); blockpos.getY() > worldgenlevel.getMinBuildHeight()
				+ 3; blockpos = blockpos.below()) {
			if (!worldgenlevel.isEmptyBlock(blockpos.below())) {
				BlockState blockstate = worldgenlevel.getBlockState(blockpos.below());
				if (isDirt(blockstate) || isStone(blockstate)
						|| blockstate.getBlock() != BlockInit.infected_stem.get()
						|| blockstate.getBlock() != BlockInit.erythrocytic_mycelium.get()) {
					break;
				}
			}
		}

		if (!(blockpos.getY() <= worldgenlevel.getMinBuildHeight() + 3)) {
			{

				for (int l = 0; l < 3; ++l) {
					int i = randomsource.nextInt(2);
					int j = randomsource.nextInt(2);
					int k = randomsource.nextInt(2);
					float f = (float) (i + j + k) * 0.333F + 0.5F;

					for (BlockPos blockpos1 : BlockPos.betweenClosed(blockpos.offset(-i, -j, -k),
							blockpos.offset(i, j, k))) {
						if (blockpos1.distSqr(blockpos) <= (double) (f * f)) {
							worldgenlevel.setBlock(blockpos1, BlockInit.infected_stem.get().defaultBlockState(), 3);
						}
					}

					blockpos = blockpos.offset(-1 + randomsource.nextInt(2), -randomsource.nextInt(2),
							-1 + randomsource.nextInt(2));
				}
			}
		}
	}

	protected int getTreeHeight(RandomSource pRandom) {
		int i = pRandom.nextInt(3) + 4;
		if (pRandom.nextInt(12) == 0) {
			i *= 2;
		}

		return i;
	}

	protected boolean isValidPosition(LevelAccessor pLevel, BlockPos pPos, int pMaxHeight,
			BlockPos.MutableBlockPos pMutablePos, HugeMushroomFeatureConfiguration pConfig) {
		int i = pPos.getY();
		if (i >= pLevel.getMinBuildHeight() + 1 && i + pMaxHeight + 1 < pLevel.getMaxBuildHeight()) {
			BlockState blockstate = pLevel.getBlockState(pPos.below());
			if (!isDirt(blockstate) && !blockstate.is(BlockTags.MUSHROOM_GROW_BLOCK)
					&& !blockstate.is(BlockInit.erythrocytic_mycelium.get())) {
				return false;
			} else {
				for (int j = 0; j <= pMaxHeight; ++j) {
					int k = this.getTreeRadiusForHeight(-1, -1, pConfig.foliageRadius, j);

					for (int l = -k; l <= k; ++l) {
						for (int i1 = -k; i1 <= k; ++i1) {
							BlockState blockstate1 = pLevel.getBlockState(pMutablePos.setWithOffset(pPos, l, j, i1));
							if (!blockstate1.isAir() && !blockstate1.is(BlockTags.LEAVES)) {
								return false;
							}
						}
					}
				}

				return true;
			}
		} else {
			return false;
		}
	}

	public boolean place(FeaturePlaceContext<HugeMushroomFeatureConfiguration> pContext) {
		WorldGenLevel worldgenlevel = pContext.level();
		BlockPos blockpos = pContext.origin();
		RandomSource randomsource = pContext.random();
		HugeMushroomFeatureConfiguration hugemushroomfeatureconfiguration = pContext.config();
		int i = this.getTreeHeight(randomsource);
		BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();
		if (!this.isValidPosition(worldgenlevel, blockpos, i, blockpos$mutableblockpos,
				hugemushroomfeatureconfiguration)) {
			return false;
		} else {
	
			this.makeCap(worldgenlevel, randomsource, blockpos, i, blockpos$mutableblockpos,
					 hugemushroomfeatureconfiguration);
			this.placeTrunk(worldgenlevel, randomsource, blockpos, hugemushroomfeatureconfiguration, i,
					blockpos$mutableblockpos);
			this.placeBulb(pContext, worldgenlevel, randomsource, blockpos, hugemushroomfeatureconfiguration, i,
					blockpos$mutableblockpos);
			return true;
		}
	}

	protected abstract int getTreeRadiusForHeight(int p_65094_, int p_65095_, int pFoliageRadius, int pY);

	protected abstract void makeCap(LevelAccessor pLevel, RandomSource pRandom, BlockPos pPos, int pTreeHeight,
			BlockPos.MutableBlockPos pMutablePos, HugeMushroomFeatureConfiguration pConfig);
}