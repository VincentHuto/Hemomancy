package com.vincenthuto.hemomancy.common.block.harbinger;

import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BloodCrystalBlock extends Block implements SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = DirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE_U = Block.box(3, 0, 3, 13, 12, 13);
	private static final VoxelShape SHAPE_N = Block.box(3, 3, 4, 13, 13, 16);
	private static final VoxelShape SHAPE_S = Block.box(3, 3, 0, 13, 13, 12);
	private static final VoxelShape SHAPE_E = Block.box(0, 3, 3, 12, 13, 13);
	private static final VoxelShape SHAPE_W = Block.box(4, 3, 3, 16, 13, 13);
	private static final VoxelShape SHAPE_D = Block.box(3, 4, 3, 13, 16, 13);

	public BloodCrystalBlock(Properties p_49795_) {
		super(p_49795_);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.UP)
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
		pBuilder.add(FACING, WATERLOGGED);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
		case NORTH:
			return SHAPE_N;
		case SOUTH:
			return SHAPE_S;
		case EAST:
			return SHAPE_E;
		case WEST:
			return SHAPE_W;
		case UP:
			return SHAPE_U;
		case DOWN:
			return SHAPE_D;
		default:
			return SHAPE_N;
		}
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
		return state.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(pContext));
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

	@Override
	public BlockState mirror(BlockState pState, Mirror pMirror) {
		return super.mirror(pState, pMirror);
	}

	@Override
	public BlockState rotate(BlockState state, LevelAccessor level, BlockPos pos, Rotation direction) {
		return super.rotate(state, level, pos, direction);
	}

}
