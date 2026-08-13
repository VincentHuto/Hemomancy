package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class WarpChairFillerBlockEntity extends FillerBlockEntity {
	public WarpChairFillerBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.warp_chair_filler.get(), pos, state);
	}
}
