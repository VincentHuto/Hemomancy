package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Activated befouling ash: floats freely in 3D multiblock structures like the inert trail. It would break off when
 * unsupported, but it never needs support, so in practice it stays.
 */
public class ActiveBefoulingAshTrailBlock extends AshTrailBlock {
	public ActiveBefoulingAshTrailBlock(BlockBehaviour.Properties properties) {
		super(properties);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return true;
	}

	@Override
	protected boolean dropsWhenUnsupported() {
		return true;
	}
}
