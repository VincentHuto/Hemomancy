package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedVivianiteBlockEntity extends BlockEntity {

	public int time;

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
	}

	public SuspendedVivianiteBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.suspended_vivianite.get(), pos, state);
	}
}
