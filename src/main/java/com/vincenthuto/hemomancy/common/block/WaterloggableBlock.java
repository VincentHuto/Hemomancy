package com.vincenthuto.hemomancy.common.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;

public abstract class WaterloggableBlock extends Block implements SimpleWaterloggedBlock {
	public WaterloggableBlock(BlockBehaviour.Properties properties, boolean startWaterlogged) {
		super(properties);
		this.registerDefaultState(
				(BlockState) this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED, startWaterlogged));
	}

	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(new Property[] { BlockStateProperties.WATERLOGGED });
	}

	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos blockpos = context.getClickedPos();
		BlockState blockstate = context.getLevel().getBlockState(blockpos);
		if (blockstate.is(this)) {
			return (BlockState) blockstate.setValue(BlockStateProperties.WATERLOGGED, false);
		} else {
			FluidState fluidstate = context.getLevel().getFluidState(blockpos);
			BlockState blockstate1 = (BlockState) this.defaultBlockState().setValue(BlockStateProperties.WATERLOGGED,
					fluidstate.getType() == Fluids.WATER);
			return blockstate1;
		}
	}

	public FluidState getFluidState(BlockState state) {
		return (Boolean) state.getValue(BlockStateProperties.WATERLOGGED) ? Fluids.WATER.getSource(false)
				: Fluids.EMPTY.defaultFluidState();
	}
	
	@Override
	public boolean canPlaceLiquid(BlockGetter p_56301_, BlockPos p_56302_, BlockState p_56303_, Fluid p_56304_) {
		return SimpleWaterloggedBlock.super.canPlaceLiquid(p_56301_, p_56302_, p_56303_, p_56304_);
	}
	
	@Override
	public boolean placeLiquid(LevelAccessor p_56306_, BlockPos p_56307_, BlockState p_56308_, FluidState p_56309_) {
		return SimpleWaterloggedBlock.super.placeLiquid(p_56306_, p_56307_, p_56308_, p_56309_);
	}

	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn,
			BlockPos currentPos, BlockPos facingPos) {
		if ((Boolean) stateIn.getValue(BlockStateProperties.WATERLOGGED)) {
			worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}

		return stateIn;
	}

	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		switch (type) {
		case LAND:
			return false;
		case WATER:
			return worldIn.getFluidState(pos).is(FluidTags.WATER);
		case AIR:
			return false;
		default:
			return false;
		}
	}
}
