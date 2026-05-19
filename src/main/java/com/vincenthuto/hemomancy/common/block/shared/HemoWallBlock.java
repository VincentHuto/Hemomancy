package com.vincenthuto.hemomancy.common.block.shared;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HemoWallBlock extends WallBlock {
	private static final VoxelShape POST_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 9.0);
	private static final VoxelShape NORTH_TEST = Block.box(7.0, 0.0, 0.0, 9.0, 16.0, 9.0);
	private static final VoxelShape SOUTH_TEST = Block.box(7.0, 0.0, 7.0, 9.0, 16.0, 16.0);
	private static final VoxelShape WEST_TEST = Block.box(0.0, 0.0, 7.0, 9.0, 16.0, 9.0);
	private static final VoxelShape EAST_TEST = Block.box(7.0, 0.0, 7.0, 16.0, 16.0, 9.0);

	public HemoWallBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		FluidState fluidState = level.getFluidState(pos);
		BlockPos northPos = pos.north();
		BlockPos eastPos = pos.east();
		BlockPos southPos = pos.south();
		BlockPos westPos = pos.west();
		BlockPos abovePos = pos.above();
		BlockState northState = level.getBlockState(northPos);
		BlockState eastState = level.getBlockState(eastPos);
		BlockState southState = level.getBlockState(southPos);
		BlockState westState = level.getBlockState(westPos);
		BlockState aboveState = level.getBlockState(abovePos);
		boolean north = this.connectsTo(northState, northState.isFaceSturdy(level, northPos, Direction.SOUTH), Direction.SOUTH);
		boolean east = this.connectsTo(eastState, eastState.isFaceSturdy(level, eastPos, Direction.WEST), Direction.WEST);
		boolean south = this.connectsTo(southState, southState.isFaceSturdy(level, southPos, Direction.NORTH), Direction.NORTH);
		boolean west = this.connectsTo(westState, westState.isFaceSturdy(level, westPos, Direction.EAST), Direction.EAST);
		BlockState state = this.defaultBlockState().setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
		return this.updateWallShape(level, state, abovePos, aboveState, north, east, south, west);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}

		if (facing == Direction.DOWN) {
			return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
		}
		if (facing == Direction.UP) {
			return this.topUpdate(level, state, facingPos, facingState);
		}
		return this.sideUpdate(level, currentPos, state, facingPos, facingState, facing);
	}

	private boolean connectsTo(BlockState state, boolean sideSolid, Direction direction) {
		Block block = state.getBlock();
		boolean fenceGate = block instanceof FenceGateBlock && FenceGateBlock.connectsToDirection(state, direction);
		return state.is(BlockTags.WALLS)
				|| block instanceof WallBlock
				|| !isExceptionForConnection(state) && sideSolid
				|| block instanceof IronBarsBlock
				|| fenceGate;
	}

	private static boolean isConnected(BlockState state, Property<WallSide> heightProperty) {
		return state.getValue(heightProperty) != WallSide.NONE;
	}

	private static boolean isCovered(VoxelShape firstShape, VoxelShape secondShape) {
		return !Shapes.joinIsNotEmpty(secondShape, firstShape, BooleanOp.ONLY_FIRST);
	}

	private BlockState topUpdate(LevelReader level, BlockState state, BlockPos pos, BlockState aboveState) {
		boolean north = isConnected(state, NORTH_WALL);
		boolean east = isConnected(state, EAST_WALL);
		boolean south = isConnected(state, SOUTH_WALL);
		boolean west = isConnected(state, WEST_WALL);
		return this.updateWallShape(level, state, pos, aboveState, north, east, south, west);
	}

	private BlockState sideUpdate(LevelReader level, BlockPos firstPos, BlockState firstState, BlockPos secondPos,
			BlockState secondState, Direction direction) {
		Direction opposite = direction.getOpposite();
		boolean north = direction == Direction.NORTH
				? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite)
				: isConnected(firstState, NORTH_WALL);
		boolean east = direction == Direction.EAST
				? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite)
				: isConnected(firstState, EAST_WALL);
		boolean south = direction == Direction.SOUTH
				? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite)
				: isConnected(firstState, SOUTH_WALL);
		boolean west = direction == Direction.WEST
				? this.connectsTo(secondState, secondState.isFaceSturdy(level, secondPos, opposite), opposite)
				: isConnected(firstState, WEST_WALL);
		BlockPos abovePos = firstPos.above();
		BlockState aboveState = level.getBlockState(abovePos);
		return this.updateWallShape(level, firstState, abovePos, aboveState, north, east, south, west);
	}

	private BlockState updateWallShape(LevelReader level, BlockState state, BlockPos pos, BlockState neighbour,
			boolean northConnection, boolean eastConnection, boolean southConnection, boolean westConnection) {
		VoxelShape shape = neighbour.getCollisionShape(level, pos).getFaceShape(Direction.DOWN);
		BlockState updated = this.updateSides(state, northConnection, eastConnection, southConnection, westConnection, shape);
		return updated.setValue(UP, this.shouldRaisePost(updated, neighbour, shape));
	}

	private boolean shouldRaisePost(BlockState state, BlockState neighbour, VoxelShape shape) {
		boolean wallAbove = neighbour.getBlock() instanceof WallBlock && neighbour.getValue(UP);
		if (wallAbove) {
			return true;
		}

		WallSide north = state.getValue(NORTH_WALL);
		WallSide south = state.getValue(SOUTH_WALL);
		WallSide east = state.getValue(EAST_WALL);
		WallSide west = state.getValue(WEST_WALL);
		boolean noSouth = south == WallSide.NONE;
		boolean noWest = west == WallSide.NONE;
		boolean noEast = east == WallSide.NONE;
		boolean noNorth = north == WallSide.NONE;
		boolean unevenConnections = noNorth && noSouth && noWest && noEast || noNorth != noSouth || noWest != noEast;
		if (unevenConnections) {
			return true;
		}

		boolean straightTall = north == WallSide.TALL && south == WallSide.TALL || east == WallSide.TALL && west == WallSide.TALL;
		return straightTall ? false : neighbour.is(BlockTags.WALL_POST_OVERRIDE) || isCovered(shape, POST_TEST);
	}

	private BlockState updateSides(BlockState state, boolean northConnection, boolean eastConnection,
			boolean southConnection, boolean westConnection, VoxelShape wallShape) {
		return state.setValue(NORTH_WALL, this.makeWallState(northConnection, wallShape, NORTH_TEST))
				.setValue(EAST_WALL, this.makeWallState(eastConnection, wallShape, EAST_TEST))
				.setValue(SOUTH_WALL, this.makeWallState(southConnection, wallShape, SOUTH_TEST))
				.setValue(WEST_WALL, this.makeWallState(westConnection, wallShape, WEST_TEST));
	}

	private WallSide makeWallState(boolean allowConnection, VoxelShape shape, VoxelShape neighbourShape) {
		if (allowConnection) {
			return isCovered(shape, neighbourShape) ? WallSide.TALL : WallSide.LOW;
		}
		return WallSide.NONE;
	}
}
