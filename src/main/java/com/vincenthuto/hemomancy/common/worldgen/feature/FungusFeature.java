package com.vincenthuto.hemomancy.common.worldgen.feature;

import com.mojang.serialization.Codec;
import com.vincenthuto.hemomancy.common.init.BlockInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.HugeMushroomBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

public class FungusFeature extends Feature<NoneFeatureConfiguration> {

	public FungusFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> pContext) {
		WorldGenLevel worldgenlevel = pContext.level();
		BlockPos blockpos = pContext.origin();
		RandomSource randomsource = pContext.random();
		NoneFeatureConfiguration hugemushroomfeatureconfiguration = pContext.config();
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
			return true;
		}
	}

	private int getTreeHeight(RandomSource pRandom) {
		int i = pRandom.nextInt(3) + 4;
		if (pRandom.nextInt(12) == 0) {
			i *= 2;
		}

		return i;
	}

	private boolean isValidPosition(WorldGenLevel pLevel, BlockPos pPos, int pMaxHeight, MutableBlockPos pMutablePos,
			NoneFeatureConfiguration pConfig) {
		int i = pPos.getY();
		if (i >= pLevel.getMinBuildHeight() + 1 && i + pMaxHeight + 1 < pLevel.getMaxBuildHeight()) {
			BlockState blockstate = pLevel.getBlockState(pPos.below());
			if (!isDirt(blockstate) && !blockstate.is(BlockInit.erythrocytic_mycelium.get())) {
				return false;
			} else {
				for (int j = 0; j <= pMaxHeight; ++j) {
					int k = this.getTreeRadiusForHeight(-1, -1, 10, j);

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

	private void placeTrunk(WorldGenLevel pLevel, RandomSource randomsource, BlockPos pPos,
			NoneFeatureConfiguration hugemushroomfeatureconfiguration, int pMaxHeight, MutableBlockPos pMutablePos) {
		for (int i = 0; i < pMaxHeight; ++i) {
			pMutablePos.set(pPos).move(Direction.UP, i);
			if (!pLevel.getBlockState(pMutablePos).isSolidRender(pLevel, pMutablePos)) {
				this.setBlock(pLevel, pMutablePos, BlockInit.infected_stem.get().defaultBlockState());
			}
		}
	}

	private void makeCap(WorldGenLevel worldgenlevel, RandomSource randomsource, BlockPos blockpos, int q,
			MutableBlockPos blockpos$mutableblockpos, NoneFeatureConfiguration hugemushroomfeatureconfiguration) {
		int capSpread = 5;
		int foliageRadius = 4;
		for (int i = capSpread - 3; i <= capSpread; ++i) {
			int j = i < capSpread ? foliageRadius : foliageRadius - 1;
			int k = foliageRadius - 2;

			for (int l = -j; l <= j; ++l) {
				for (int i1 = -j; i1 <= j; ++i1) {
					boolean flag = l == -j;
					boolean flag1 = l == j;
					boolean flag2 = i1 == -j;
					boolean flag3 = i1 == j;
					boolean flag4 = flag || flag1;
					boolean flag5 = flag2 || flag3;
					if (i >= capSpread || flag4 != flag5) {
						blockpos$mutableblockpos.setWithOffset(blockpos, l, i, i1);
						if (!worldgenlevel.getBlockState(blockpos$mutableblockpos).isSolidRender(worldgenlevel,
								blockpos$mutableblockpos)) {
							BlockState blockstate = BlockInit.infected_cap.get().defaultBlockState();
							if (blockstate.hasProperty(HugeMushroomBlock.WEST)
									&& blockstate.hasProperty(HugeMushroomBlock.EAST)
									&& blockstate.hasProperty(HugeMushroomBlock.NORTH)
									&& blockstate.hasProperty(HugeMushroomBlock.SOUTH)
									&& blockstate.hasProperty(HugeMushroomBlock.UP)) {
								blockstate = blockstate
										.setValue(HugeMushroomBlock.UP, Boolean.valueOf(i >= capSpread - 1))
										.setValue(HugeMushroomBlock.WEST, Boolean.valueOf(l < -k))
										.setValue(HugeMushroomBlock.EAST, Boolean.valueOf(l > k))
										.setValue(HugeMushroomBlock.NORTH, Boolean.valueOf(i1 < -k))
										.setValue(HugeMushroomBlock.SOUTH, Boolean.valueOf(i1 > k));
							}

							this.setBlock(worldgenlevel, blockpos$mutableblockpos, blockstate);
						}
					}
				}
			}
		}
	}


	protected int getTreeRadiusForHeight(int p_65977_, int p_65978_, int p_65979_, int p_65980_) {
		int i = 0;
		if (p_65980_ < p_65978_ && p_65980_ >= p_65978_ - 3) {
			i = p_65979_;
		} else if (p_65980_ == p_65978_) {
			i = p_65979_;
		}

		return i;
	}

}