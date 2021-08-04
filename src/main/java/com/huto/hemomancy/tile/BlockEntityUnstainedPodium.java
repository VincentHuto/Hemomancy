package com.huto.hemomancy.tile;

import com.huto.hemomancy.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityUnstainedPodium extends BlockEntity  {

	public BlockEntityUnstainedPodium(BlockPos pos, BlockState state) {
		super(BlockEntityInit.unstained_podium.get(), pos, state);
	}

	public void tick() {

	}
}
