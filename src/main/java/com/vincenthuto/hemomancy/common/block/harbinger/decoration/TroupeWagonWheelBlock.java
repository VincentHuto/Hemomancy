package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A painted wagon's wheel, two blocks across: four quarters round one hub, each with its own model from
 * tools/model_export/circus_set_pieces.py. {@code facing} is the side of the wagon the wheel is on. Quarter 0 is the
 * lower near cell; adding 1 steps along the wagon ({@code facing}'s clockwise side) and adding 2 steps up. When one quarter goes,
 * the wheel goes.
 */
public class TroupeWagonWheelBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty QUARTER = IntegerProperty.create("quarter", 0, 3);
	private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(ModelShapes.of(new double[] {0, 0, 5, 16, 16, 11}));

	public TroupeWagonWheelBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(QUARTER, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, QUARTER);
	}

	/** Where quarter {@code to} stands, from the cell of quarter {@code from}. */
	public static BlockPos offset(Direction facing, int from, int to) {
		Direction along = facing.getClockWise();
		int steps = (to & 1) - (from & 1);
		int up = ((to >> 1) & 1) - ((from >> 1) & 1);
		return new BlockPos(along.getStepX() * steps, up, along.getStepZ() * steps);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		Direction facing = context.getHorizontalDirection().getOpposite();
		BlockPos pos = context.getClickedPos();
		if (pos.getY() + 2 > level.getMaxBuildHeight()) return null;
		for (int quarter = 1; quarter < 4; quarter++) {
			if (!level.getBlockState(pos.offset(offset(facing, 0, quarter))).canBeReplaced(context)) return null;
		}
		return defaultBlockState().setValue(FACING, facing);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		for (int quarter = 1; quarter < 4; quarter++) {
			level.setBlock(pos.offset(offset(state.getValue(FACING), 0, quarter)), state.setValue(QUARTER, quarter),
					Block.UPDATE_ALL);
		}
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		Direction facing = state.getValue(FACING);
		int quarter = state.getValue(QUARTER);
		for (int partner : new int[] {quarter ^ 1, quarter ^ 2}) {
			if (!pos.offset(offset(facing, quarter, partner)).equals(neighbourPos)) continue;
			if (!neighbour.is(this) || neighbour.getValue(FACING) != facing || neighbour.getValue(QUARTER) != partner) {
				return Blocks.AIR.defaultBlockState();
			}
		}
		return super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	/** A mirror turns the wheel's side and swaps its near and far quarters, since "along" flips with it. */
	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		if (mirror == Mirror.NONE) return state;
		return state.rotate(mirror.getRotation(state.getValue(FACING))).setValue(QUARTER, state.getValue(QUARTER) ^ 1);
	}
}
