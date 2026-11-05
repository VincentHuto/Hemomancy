package com.vincenthuto.hemomancy.common.tile.shared;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** The lower half of a Lidless Door: no data, only an anchor for the panel renderer (its bounds cover both halves). */
public class LidlessDoorBlockEntity extends BlockEntity {
	public LidlessDoorBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.lidless_door.get(), pos, state);
	}
}
