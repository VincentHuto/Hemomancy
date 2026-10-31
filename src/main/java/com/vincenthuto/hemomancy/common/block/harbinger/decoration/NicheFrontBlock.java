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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * The front of a burial loculus in a mausoleum's outer crypt galleries, set in the slot's opening with the recess
 * behind it. The sealed front is a bone-framed lacquer plate; the opened front keeps only its frame and a shelf lip,
 * so the barrel in the recess behind stays in reach. Models from tools/model_export/mausoleum_niche.py.
 */
public class NicheFrontBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	/** The sealed front's outline: frame and plate across the whole opening. */
	public static final double[][] SEALED = {{0, 0, 0, 16, 16, 5}};
	/** The opened front's frame and shelf only, so the middle of the opening stays clear. */
	public static final double[][] OPENED = {{0, 0, 0, 16, 2, 4}, {0, 14, 0, 16, 16, 2}, {0, 2, 0, 1, 14, 2},
			{15, 2, 0, 16, 14, 2}};
	private final Map<Direction, VoxelShape> shapes;

	public NicheFrontBlock(double[][] outline, Properties properties) {
		super(properties);
		this.shapes = ModelShapes.horizontal(ModelShapes.of(outline));
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return shapes.get(state.getValue(FACING));
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
