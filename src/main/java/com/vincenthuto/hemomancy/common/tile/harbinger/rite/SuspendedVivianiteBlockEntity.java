package com.vincenthuto.hemomancy.common.tile.harbinger.rite;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedVivianiteBlockEntity extends BlockEntity {

	public int time;

	public SuspendedVivianiteBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.suspended_vivianite.get(), pos, state);
	}
}
