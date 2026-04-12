package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AltarOfCleansingBlockEntity extends BlockEntity {

	public AltarOfCleansingBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.altar_of_cleansing.get(), pos, state);
	}

	public void tick() {
	}
}
