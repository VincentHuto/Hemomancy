package com.vincenthuto.hemomancy.common.tile.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ScarletVanityBlockEntity extends BlockEntity {
	public ScarletVanityBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.scarlet_vanity.get(), pos, state);
	}
}
