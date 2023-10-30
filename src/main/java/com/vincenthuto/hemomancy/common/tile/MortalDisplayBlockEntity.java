package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.registry.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MortalDisplayBlockEntity extends BlockEntity {

	public MortalDisplayBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mortal_display.get(), pos, state);
	}

}