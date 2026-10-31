package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.StackedSetPieceBlock;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

/**
 * A Litany wing's standing stone: the words of the Litany carved in Abocipher on its face. A Chapel focus reads its
 * Litany to whoever comes up to the stone (its LITANY_STONE mark is the lower half). {@link #CHARRED} is Putriciel's
 * scorched stone: soot-black, most of its lines burned away, a few still legible. Two tall, unbreakable. Models from
 * tools/model_export/litany_stone.py.
 */
public class LitanyStoneBlock extends StackedSetPieceBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final BooleanProperty CHARRED = BooleanProperty.create("charred");
	private static final Map<Direction, VoxelShape> LOWER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {0.5, 0, 3.5, 15.5, 2, 12.5}, new double[] {1, 2, 4.5, 15, 3, 11.5},
					new double[] {1.5, 3, 5.5, 14.5, 16, 10}));
	private static final Map<Direction, VoxelShape> UPPER = ModelShapes.horizontal(
			ModelShapes.of(new double[] {1.25, 0, 5.25, 14.75, 12, 10}, new double[] {1, 12, 4.75, 15, 14, 10.5},
					new double[] {2, 14, 5.5, 14, 15, 10}, new double[] {4, 15, 6.5, 12, 16, 9}));

	public LitanyStoneBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER)
				.setValue(CHARRED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, CHARRED);
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
		return (state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER).get(state.getValue(FACING));
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
