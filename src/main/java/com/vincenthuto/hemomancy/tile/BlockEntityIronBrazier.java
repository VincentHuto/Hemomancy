package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityIronBrazier extends BlockEntity  {

	public BlockEntityIronBrazier(BlockPos pos, BlockState state) {
		super(BlockEntityInit.iron_brazier.get(), pos, state);
	}
}