package com.vincenthuto.hemomancy.common.tile.harbinger.functional;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ScryingPodiumBlockEntity extends BlockEntity {

	public static void animTick(Level level, BlockPos pos, BlockState state, ScryingPodiumBlockEntity ent) {
	}

	public ScryingPodiumBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.scrying_podium.get(), pos, state);
	}
}
