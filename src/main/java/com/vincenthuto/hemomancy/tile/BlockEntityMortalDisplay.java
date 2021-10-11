package com.vincenthuto.hemomancy.tile;

import com.vincenthuto.hemomancy.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityMortalDisplay extends BlockEntity {

	public BlockEntityMortalDisplay(BlockPos pos, BlockState state) {
		super(BlockEntityInit.mortal_display.get(), pos, state);
	}

}