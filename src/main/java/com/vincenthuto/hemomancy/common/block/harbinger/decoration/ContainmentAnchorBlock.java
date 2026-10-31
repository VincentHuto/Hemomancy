package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A containment anchor as Seraphae's builders left it: the chain pylon of her fight (ContainmentAnchorEntity) in its
 * dormant, built form, standing in her chamber and wings. Two tall. A {@link #SNAPPED} anchor is one whose binding
 * failed (her Downfall wing): the pillar broken through, the head slumped on the stump, the chains torn; both storeys
 * carry the flag. Models from tools/model_export/containment_anchor_block.py.
 */
public class ContainmentAnchorBlock extends StackedSetPieceBlock {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final BooleanProperty SNAPPED = BooleanProperty.create("snapped");
	private static final VoxelShape LOWER = ModelShapes.of(new double[] {1, 0, 1, 15, 2, 15},
			new double[] {3.5, 2, 3.5, 12.5, 3.5, 12.5}, new double[] {4.25, 3.5, 4.25, 11.75, 7.75, 11.75},
			new double[] {5.25, 7.75, 5.25, 10.75, 16, 10.75});
	private static final VoxelShape UPPER = ModelShapes.of(new double[] {5.25, 0, 5.25, 10.75, 7, 10.75},
			new double[] {4.5, 7, 4.5, 11.5, 13, 11.5}, new double[] {4.25, 13, 4.25, 11.75, 14.5, 11.75},
			new double[] {6.75, 14, 6.75, 9.25, 16, 9.25}, new double[] {1, 9.5, 6.75, 15, 12, 9.25},
			new double[] {6.75, 9.5, 1, 9.25, 12, 15});
	/** The snapped anchor's upper storey: the jagged stump, and the head slumped east across it. */
	private static final VoxelShape UPPER_SNAPPED = ModelShapes.of(new double[] {5.5, 0, 5.5, 10.5, 4.5, 10.5},
			new double[] {7, 2.5, 5.5, 12.5, 7, 10.5}, new double[] {7.25, 4.5, 4.5, 14, 9.5, 11.5},
			new double[] {9, 9.5, 4.5, 15.5, 13, 11.5}, new double[] {4.5, 9.5, 6.75, 8, 12.5, 9.25});

	public ContainmentAnchorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER).setValue(SNAPPED, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, SNAPPED);
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
		if (state.getValue(HALF) == DoubleBlockHalf.LOWER) return LOWER;
		return state.getValue(SNAPPED) ? UPPER_SNAPPED : UPPER;
	}
}
