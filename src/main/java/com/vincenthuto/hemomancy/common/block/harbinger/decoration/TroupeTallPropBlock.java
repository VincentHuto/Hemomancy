package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A Crimson Troupe set piece two blocks tall (the knife wheel, the stilt rack, the Ringmaster's podium): one model per
 * storey from tools/model_export/circus_set_pieces.py, and when one storey goes the other follows.
 */
public class TroupeTallPropBlock extends StackedSetPieceBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	private final Map<Direction, VoxelShape> lower;
	private final Map<Direction, VoxelShape> upper;

	public TroupeTallPropBlock(Properties properties, double[][] lower, double[][] upper) {
		super(properties);
		this.lower = ModelShapes.horizontal(ModelShapes.of(lower));
		this.upper = ModelShapes.horizontal(ModelShapes.of(upper));
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	@Override
	protected int storey(BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1;
	}

	@Override
	protected int storeys() {
		return 2;
	}

	@Override
	protected BlockState withStorey(BlockState state, int storey) {
		return state.setValue(HALF, storey == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return (state.getValue(HALF) == DoubleBlockHalf.LOWER ? lower : upper).get(state.getValue(FACING));
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}
}
