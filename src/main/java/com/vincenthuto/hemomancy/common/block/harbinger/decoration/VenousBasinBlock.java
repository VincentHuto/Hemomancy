package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A standing venous-stone basin banded in hematic iron, Hemorath's dry or brimming hall basin. {@link #FACING} is
 * the side its pouring spout faces, where a {@link BloodGutterBlock} meets it; {@link #FILL} as the gutter's.
 * Decorative: structures set the fill. Model from tools/model_export/venous_basin.py.
 */
public class VenousBasinBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty FILL = BloodGutterBlock.FILL;
	/** Plinth, pedestal and bowl, traced from the north-facing model (venous_basin.py). */
	private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(ModelShapes.of(
			new double[] {2, 0, 2, 14, 2, 14},
			new double[] {5, 2, 5, 11, 5, 11},
			new double[] {3, 5, 3, 13, 6, 13},
			new double[] {1.5, 6, 1.5, 14.5, 14, 14.5},
			new double[] {6, 10.5, 0, 10, 13, 2}));

	public VenousBasinBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FILL, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FILL);
	}

	/** The spout faces the player who sets the basin down. */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
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
