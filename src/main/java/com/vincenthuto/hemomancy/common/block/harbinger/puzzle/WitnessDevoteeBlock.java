package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.harbinger.decoration.FrozenDevoteeBlock;
import com.vincenthuto.hemomancy.common.block.harbinger.decoration.StackedSetPieceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
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

import java.util.Locale;

/**
 * Velorum's Beholding witness: one of the Procession who still turns its head. Two tall, in the Frozen Devotee's
 * striding pose; the mark is the lower cell. The Chapel focus flips {@link #LIT} there as it plays the memory, and
 * the upper cell follows: lit, the head turns to its {@link #TURN} side and the eyes open. Unbreakable, like the
 * witness stone it stands in for. Models from tools/model_export/frozen_devotee.py.
 */
public class WitnessDevoteeBlock extends StackedSetPieceBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	/** The witness stone's own property, so the focus drives either witness the same way. */
	public static final BooleanProperty LIT = WitnessStoneBlock.LIT;
	public static final EnumProperty<Side> TURN = EnumProperty.create("turn", Side.class);

	public WitnessDevoteeBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
				.setValue(HALF, DoubleBlockHalf.LOWER).setValue(LIT, false).setValue(TURN, Side.LEFT));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF, LIT, TURN);
	}

	@Override
	protected int storey(BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? 0 : 1;
	}

	@Override
	protected int storeys() {
		return 2;
	}

	/** Both halves keep {@link #LIT} in step: whichever half changes, its partner copies it, as a door's halves do. */
	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		BlockState updated = super.updateShape(state, direction, neighbour, level, pos, neighbourPos);
		Direction partner = state.getValue(HALF) == DoubleBlockHalf.LOWER ? Direction.UP : Direction.DOWN;
		if (direction == partner && updated.is(this) && neighbour.is(this)) {
			return updated.setValue(LIT, neighbour.getValue(LIT));
		}
		return updated;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return FrozenDevoteeBlock.outline(FrozenDevoteeBlock.STRIDING, state.getValue(HALF), state.getValue(LIT),
				state.getValue(FACING));
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		BlockState turned = state.rotate(mirror.getRotation(state.getValue(FACING)));
		return mirror == Mirror.NONE ? turned : turned.setValue(TURN, state.getValue(TURN).mirrored());
	}

	/** The side a lit witness turns its head to, the figure's own left or right. */
	public enum Side implements StringRepresentable {
		LEFT, RIGHT;

		public Side mirrored() {
			return this == LEFT ? RIGHT : LEFT;
		}

		@Override
		public String getSerializedName() {
			return name().toLowerCase(Locale.ROOT);
		}
	}
}
