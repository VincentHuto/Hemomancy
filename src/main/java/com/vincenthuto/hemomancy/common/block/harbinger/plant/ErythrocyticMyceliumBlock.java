package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.LightEngine;

public class ErythrocyticMyceliumBlock extends SnowyDirtBlock {
	public ErythrocyticMyceliumBlock(BlockBehaviour.Properties pProperties) {
		super(pProperties);
	}

	private static boolean canBeGrass(BlockState pState, LevelReader pLevelReader, BlockPos pPos) {
		BlockPos blockpos = pPos.above();
		BlockState blockstate = pLevelReader.getBlockState(blockpos);
		if (blockstate.is(Blocks.SNOW) && blockstate.getValue(SnowLayerBlock.LAYERS) == 1) {
			return true;
		} else if (blockstate.getFluidState().getAmount() == 8) {
			return false;
		} else {
			int i = LightEngine.getLightBlockInto(pLevelReader, pState, pPos, blockstate, blockpos, Direction.UP,
					blockstate.getLightBlock(pLevelReader, blockpos));
			return i < pLevelReader.getMaxLightLevel();
		}
	}

	private static boolean canPropagate(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		BlockPos blockpos = pPos.above();
		return canBeGrass(pState, pLevel, pPos) && !pLevel.getFluidState(blockpos).is(FluidTags.WATER);
	}

	/**
	 * Performs a random tick on a block.
	 */
	@Override
	public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
		if (!canBeGrass(pState, pLevel, pPos)) {
			if (!pLevel.isAreaLoaded(pPos, 1))
				return; // Forge: prevent loading unloaded chunks when checking neighbor's light and
						// spreading
			pLevel.setBlockAndUpdate(pPos, BlockInit.erythrocytic_dirt.get().defaultBlockState());
		} else {
			if (!pLevel.isAreaLoaded(pPos, 3))
				return; // Forge: prevent loading unloaded chunks when checking neighbor's light and
						// spreading
			if (pLevel.getMaxLocalRawBrightness(pPos.above()) >= 9) {
				BlockState blockstate = this.defaultBlockState();

				for (int i = 0; i < 4; ++i) {
					BlockPos blockpos = pPos.offset(pRandom.nextInt(3) - 1, pRandom.nextInt(5) - 3,
							pRandom.nextInt(3) - 1);
					if (pLevel.getBlockState(blockpos).is(BlockInit.erythrocytic_dirt.get())
							&& canPropagate(blockstate, pLevel, blockpos)) {
						pLevel.setBlockAndUpdate(blockpos, blockstate.setValue(SNOWY,
								pLevel.getBlockState(blockpos.above()).is(Blocks.SNOW)));
					}
				}
			}

		}
	}

}
