package com.vincenthuto.hemomancy.common.block.shared;

import java.util.Map;
import java.util.function.UnaryOperator;

import com.mojang.serialization.MapCodec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IronBarsBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HematicIronBarsBlock extends IronBarsBlock {
	public static final MapCodec<HematicIronBarsBlock> CODEC = simpleCodec(HematicIronBarsBlock::new);
	public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.AXIS;
	private static final Map<Direction.Axis, VoxelShape[]> SHAPES = Map.of(
			Direction.Axis.X, makeShapes(Direction.Axis.X),
			Direction.Axis.Y, makeShapes(Direction.Axis.Y),
			Direction.Axis.Z, makeShapes(Direction.Axis.Z));

	public HematicIronBarsBlock(Properties properties) {
		super(properties);
		BlockState state = defaultBlockState().setValue(AXIS, Direction.Axis.Y);
		for (Direction direction : Direction.values()) {
			state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), false);
		}
		registerDefaultState(state.setValue(WATERLOGGED, false));
	}

	@Override
	public MapCodec<HematicIronBarsBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction.Axis axis = context.getClickedFace().getAxis();
		BlockState state = defaultBlockState()
				.setValue(AXIS, axis)
				.setValue(WATERLOGGED, level.getFluidState(pos).getType() == Fluids.WATER);
		for (Direction direction : Direction.values()) {
			if (direction.getAxis() != axis) {
				state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction),
						connectsTo(level, pos.relative(direction), direction));
			}
		}
		return state;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
			LevelAccessor level, BlockPos currentPos, BlockPos neighborPos) {
		if (state.getValue(WATERLOGGED)) {
			level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
		}
		if (direction.getAxis() == state.getValue(AXIS)) {
			return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), false);
		}
		return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction),
				connectsTo(level, neighborPos, direction));
	}

	private boolean connectsTo(LevelReader level, BlockPos neighborPos, Direction direction) {
		BlockState neighbor = level.getBlockState(neighborPos);
		if (neighbor.getBlock() instanceof HematicIronBarsBlock) {
			return neighbor.getValue(AXIS) != direction.getAxis();
		}
		if (neighbor.getBlock() instanceof IronBarsBlock) {
			return direction.getAxis().isHorizontal();
		}
		return attachsTo(neighbor, neighbor.isFaceSturdy(level, neighborPos, direction.getOpposite()));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(AXIS))[shapeIndex(state)];
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return getShape(state, level, pos, context);
	}

	@Override
	protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction direction) {
		if (adjacentState.is(this)) {
			return direction.getAxis() != state.getValue(AXIS)
					&& direction.getAxis() != adjacentState.getValue(AXIS)
					&& state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))
					&& adjacentState.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction.getOpposite()));
		}
		return super.skipRendering(state, adjacentState, direction);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return transform(state, rotation::rotate);
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return transform(state, mirror::mirror);
	}

	private static BlockState transform(BlockState state, UnaryOperator<Direction> transform) {
		Direction axisDirection = Direction.get(Direction.AxisDirection.POSITIVE, state.getValue(AXIS));
		BlockState transformed = state.setValue(AXIS, transform.apply(axisDirection).getAxis());
		for (Direction direction : Direction.values()) {
			transformed = transformed.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), false);
		}
		for (Direction direction : Direction.values()) {
			if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
				Direction result = transform.apply(direction);
				transformed = transformed.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(result), true);
			}
		}
		return transformed;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(AXIS, PipeBlock.UP, PipeBlock.DOWN);
	}

	private static int shapeIndex(BlockState state) {
		int index = 0;
		for (Direction direction : Direction.values()) {
			if (state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction))) {
				index |= 1 << direction.ordinal();
			}
		}
		return index;
	}

	private static VoxelShape[] makeShapes(Direction.Axis axis) {
		VoxelShape[] shapes = new VoxelShape[64];
		for (int index = 0; index < shapes.length; index++) {
			VoxelShape shape = bar(axis, null);
			for (Direction direction : Direction.values()) {
				if (direction.getAxis() != axis && (index & 1 << direction.ordinal()) != 0) {
					shape = Shapes.or(shape, bar(axis, direction));
				}
			}
			shapes[index] = shape.optimize();
		}
		return shapes;
	}

	private static VoxelShape bar(Direction.Axis axis, Direction connection) {
		double[] min = { 7, 7, 7 };
		double[] max = { 9, 9, 9 };
		min[axis.ordinal()] = 0;
		max[axis.ordinal()] = 16;
		if (connection != null) {
			if (connection.getAxisDirection() == Direction.AxisDirection.NEGATIVE) {
				min[connection.getAxis().ordinal()] = 0;
			} else {
				max[connection.getAxis().ordinal()] = 16;
			}
		}
		return Block.box(min[0], min[1], min[2], max[0], max[1], max[2]);
	}
}
