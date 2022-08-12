package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class UnstainedPodiumBlockEntity extends BlockEntity  {

	public UnstainedPodiumBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.unstained_podium.get(), pos, state);
	}

	public void tick() {

	}
}