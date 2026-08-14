package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BloodCrystalBlock extends Block implements SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final int MAX_AGE = 3;

	private static final VoxelShape[] SHAPES_UP = {
			Block.box(5, 0, 5, 11, 3, 11),
			Block.box(4, 0, 4, 12, 5, 12),
			Block.box(3, 0, 3, 13, 8, 13),
			Block.box(3, 0, 3, 13, 12, 13)
	};
	private static final VoxelShape[] SHAPES_NORTH = {
			Block.box(5, 5, 13, 11, 11, 16),
			Block.box(4, 4, 11, 12, 12, 16),
			Block.box(3, 3, 8, 13, 13, 16),
			Block.box(3, 3, 4, 13, 13, 16)
	};
	private static final VoxelShape[] SHAPES_SOUTH = {
			Block.box(5, 5, 0, 11, 11, 3),
			Block.box(4, 4, 0, 12, 12, 5),
			Block.box(3, 3, 0, 13, 13, 8),
			Block.box(3, 3, 0, 13, 13, 12)
	};
	private static final VoxelShape[] SHAPES_EAST = {
			Block.box(0, 5, 5, 3, 11, 11),
			Block.box(0, 4, 4, 5, 12, 12),
			Block.box(0, 3, 3, 8, 13, 13),
			Block.box(0, 3, 3, 12, 13, 13)
	};
	private static final VoxelShape[] SHAPES_WEST = {
			Block.box(13, 5, 5, 16, 11, 11),
			Block.box(11, 4, 4, 16, 12, 12),
			Block.box(8, 3, 3, 16, 13, 13),
			Block.box(4, 3, 3, 16, 13, 13)
	};
	private static final VoxelShape[] SHAPES_DOWN = {
			Block.box(5, 13, 5, 11, 16, 11),
			Block.box(4, 11, 4, 12, 16, 12),
			Block.box(3, 8, 3, 13, 16, 13),
			Block.box(3, 4, 3, 13, 16, 13)
	};

	public BloodCrystalBlock(Properties p_49795_) {
		super(p_49795_);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.UP)
				.setValue(AGE, MAX_AGE)
				.setValue(WATERLOGGED, false));
	}

	@Override
	public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
		Direction direction = pState.getValue(FACING);
		return pLevel
				.getBlockState(pPos.offset(new Vec3i(direction.getOpposite().getStepX(),
						direction.getOpposite().getStepY(), direction.getOpposite().getStepZ())))
				.getBlock() != Blocks.AIR;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		super.createBlockStateDefinition(pBuilder);
		pBuilder.add(FACING, AGE, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		int age = state.getValue(AGE);
		return switch (state.getValue(FACING)) {
			case NORTH -> SHAPES_NORTH[age];
			case SOUTH -> SHAPES_SOUTH[age];
			case EAST -> SHAPES_EAST[age];
			case WEST -> SHAPES_WEST[age];
			case DOWN -> SHAPES_DOWN[age];
			default -> SHAPES_UP[age];
		};
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext pContext) {
		Direction direction = pContext.getClickedFace();
		BlockState blockstate = pContext.getLevel()
				.getBlockState(pContext.getClickedPos().offset(new Vec3i(direction.getOpposite().getStepX(),
						direction.getOpposite().getStepY(), direction.getOpposite().getStepZ())));
		BlockState state = blockstate.is(this) && blockstate.getValue(FACING) == direction
				? this.defaultBlockState().setValue(FACING, direction.getOpposite())
				: this.defaultBlockState().setValue(FACING, direction);
		return state.setValue(AGE, MAX_AGE)
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(pContext));
	}

	public boolean isMature(BlockState state) {
		return state.getValue(AGE) >= MAX_AGE;
	}

	public boolean tryGrow(Level level, BlockPos pos, BlockState state, RandomSource rand) {
		int age = state.getValue(AGE);
		if (age >= MAX_AGE) return false;
		level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
		return true;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos currentPos, BlockPos facingPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, currentPos);
		if (!state.canSurvive(level, currentPos)) {
			return WaterloggedBlockSupport.survivorOrWater(state);
		}
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

}
