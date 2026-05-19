package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class WaterloggedBlockSupport {
	private WaterloggedBlockSupport() {
	}

	public static boolean waterloggedForPlacement(BlockPlaceContext context) {
		return context.getLevel().getFluidState(context.getClickedPos()).is(FluidTags.WATER);
	}

	public static FluidState fluidState(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED)
				? Fluids.WATER.getSource(false)
				: Fluids.EMPTY.defaultFluidState();
	}

	public static void scheduleWaterTick(BlockState state, LevelAccessor level, BlockPos pos) {
		if (state.getValue(BlockStateProperties.WATERLOGGED)) {
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
	}

	public static BlockState survivorOrWater(BlockState state) {
		return state.getValue(BlockStateProperties.WATERLOGGED)
				? Blocks.WATER.defaultBlockState()
				: Blocks.AIR.defaultBlockState();
	}
}
