package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public final class WarpChairFillerBlockEntity extends FillerBlockEntity {
	public WarpChairFillerBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.warp_chair_filler.get(), pos, state);
	}
}
