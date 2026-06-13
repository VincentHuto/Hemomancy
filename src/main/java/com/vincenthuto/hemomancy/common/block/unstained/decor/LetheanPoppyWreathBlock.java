package com.vincenthuto.hemomancy.common.block.unstained.decor;

import com.vincenthuto.hemomancy.common.init.ParticleInit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LetheanPoppyWreathBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape NORTH_SHAPE = Block.box(1.0D, 1.0D, 0.0D, 15.0D, 15.0D, 3.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(1.0D, 1.0D, 13.0D, 15.0D, 15.0D, 16.0D);
	private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 1.0D, 1.0D, 3.0D, 15.0D, 15.0D);
	private static final VoxelShape EAST_SHAPE = Block.box(13.0D, 1.0D, 1.0D, 16.0D, 15.0D, 15.0D);

	public LetheanPoppyWreathBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		if (clickedFace.getAxis().isVertical()) {
			return null;
		}
		return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos supportPos = pos.relative(facing);
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing.getOpposite());
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		if (direction == state.getValue(FACING) && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
			default -> NORTH_SHAPE;
		};
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (random.nextInt(10) != 0) {
			return;
		}
		Direction facing = state.getValue(FACING);
		double along = 0.24D + random.nextDouble() * 0.52D;
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 0.16D + random.nextDouble() * 0.12D;
		double z = pos.getZ() + 0.5D;
		double frontOffset = 0.43D;
		double sideOffset = along - 0.5D;
		if (facing.getAxis() == Direction.Axis.Z) {
			x += sideOffset;
			z += facing.getStepZ() * frontOffset;
		} else {
			x += facing.getStepX() * frontOffset;
			z += sideOffset;
		}
		level.addParticle(ParticleInit.lethean_drip.get(), x, y, z, 0.0D, 0.0D, 0.0D);
	}
}
