package com.vincenthuto.hemomancy.common.tile;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class SuspendedBloodCrystalBlockEntity extends BlockEntity {

	public final AnimationState idleAnimationState = new AnimationState();

	public int time;

	public static <T> void tick(Level level, BlockPos pos, BlockState state, T blockEntity) {
	}

	public SuspendedBloodCrystalBlockEntity(BlockPos pos, BlockState state) {
		super(BlockEntityInit.suspended_blood_crystal.get(), pos, state);
	}
}
