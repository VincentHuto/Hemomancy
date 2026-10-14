package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Befouling ash: a magical ritual component that can float freely in 3D multiblock structures. Unlike redstone wire
 * it does not need a solid block beneath it.
 */
public class BefoulingAshTrailBlock extends AshTrailBlock {
	public BefoulingAshTrailBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return true;
	}
}
