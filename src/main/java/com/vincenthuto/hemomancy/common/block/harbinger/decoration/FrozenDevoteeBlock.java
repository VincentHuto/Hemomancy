package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Locale;
import java.util.Map;

/**
 * One of Velorum's congregation: a robed devotee frozen where they prayed, rime in the folds of the robe. Two tall,
 * in one of five poses (kneeling, bowed, reaching, prostrate, striding in the Procession), the head facing ahead or
 * turned toward the figure's own left or right. Models from tools/model_export/frozen_devotee.py.
 */
public class FrozenDevoteeBlock extends StackedSetPieceBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	/** 0 kneeling, 1 bowed, 2 reaching, 3 prostrate, 4 striding. */
	public static final IntegerProperty POSE = IntegerProperty.create("pose", 0, 4);
	public static final int STRIDING = 4;
	public static final EnumProperty<Head> HEAD = EnumProperty.create("head", Head.class);

	// Per pose, the outlines traced from the north-facing models (pixels). The lower cell's outline is shared by
	// every head; the upper cell's follows the head, whose turned corners swing wider.
	private static final double[][][] LOWER = {
			{{2.5, 0, 1.5, 13.5, 7, 15}, {2, 7, 4.5, 14, 16, 15}},
			{{2.5, 0, 1.5, 13.5, 6.5, 15}, {2, 6.5, 2.5, 14, 16, 14.5}},
			{{2.5, 0, 1.5, 13.5, 6, 15}, {1.5, 6, 6.5, 14.5, 16, 13.5}},
			{{1.5, 0, 1, 14.5, 9, 15}, {3.5, 9, 8, 12.5, 13, 15}},
			{{3.5, 0, 1.5, 12.5, 3, 14.75}, {3.5, 3, 2.25, 12.5, 9, 14}, {2.5, 9, 3, 13.5, 16, 11}}};
	// The prostrate devotee lies within its lower cell: its upper cell is empty and is broken through the lower.
	private static final double[][][] UPPER = {
			{{3.5, 0, 6.5, 12.5, 7.5, 15}}, {{5, 0, 1, 11, 5, 10}}, {{1.5, 0, 1.5, 14.5, 6, 15}}, {},
			{{2, 0, 3, 14, 8, 11.25}, {3.75, 8, 3, 12.25, 14.5, 11.25}}};
	private static final double[][][] UPPER_TURNED = {
			{{2, 0, 3.75, 14, 7.5, 15}}, {{3.5, 0, 2, 12.5, 3.25, 11}}, {{1.5, 0, 1.5, 14.5, 6, 15}}, {},
			{{2, 0, 2, 14, 8, 12.5}, {2.25, 8, 1.25, 13.75, 14.5, 12.75}}};
	private static final Map<Direction, VoxelShape>[] LOWER_SHAPES = shapes(LOWER);
	private static final Map<Direction, VoxelShape>[] UPPER_SHAPES = shapes(UPPER);
	private static final Map<Direction, VoxelShape>[] UPPER_TURNED_SHAPES = shapes(UPPER_TURNED);

	public FrozenDevoteeBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
				.setValue(HALF, DoubleBlockHalf.LOWER).setValue(POSE, 0).setValue(HEAD, Head.FORWARD));
	}

	@SuppressWarnings("unchecked")
	private static Map<Direction, VoxelShape>[] shapes(double[][][] poses) {
		Map<Direction, VoxelShape>[] out = new Map[poses.length];
		for (int i = 0; i < poses.length; i++) out[i] = ModelShapes.horizontal(ModelShapes.of(poses[i]));
		return out;
	}

	/** A devotee cell's outline: also the Witness Devotee's, which stands in the striding pose. */
	public static VoxelShape outline(int pose, DoubleBlockHalf half, boolean turned, Direction facing) {
		Map<Direction, VoxelShape>[] shapes = half == DoubleBlockHalf.LOWER ? LOWER_SHAPES
				: turned ? UPPER_TURNED_SHAPES : UPPER_SHAPES;
		return shapes[pose].get(facing);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, POSE, HEAD);
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
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return outline(state.getValue(POSE), state.getValue(HALF), state.getValue(HEAD) != Head.FORWARD,
				state.getValue(FACING));
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		BlockState turned = state.rotate(mirror.getRotation(state.getValue(FACING)));
		return mirror == Mirror.NONE ? turned : turned.setValue(HEAD, state.getValue(HEAD).mirrored());
	}

	/** Where the head looks: ahead, or turned toward the figure's own left or right. */
	public enum Head implements StringRepresentable {
		FORWARD, LEFT, RIGHT;

		public Head mirrored() {
			return this == LEFT ? RIGHT : this == RIGHT ? LEFT : FORWARD;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
