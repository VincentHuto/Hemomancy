package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Icicles hanging from the underside of the block above, in Velorum's frozen monastery. {@link #LENGTH}: a
 * {@code short} cluster ends in its own cell; a two-cell icicle is a {@code base} (the thick upper cell) over a
 * {@code tip}. Short and base need a sturdy face above; a tip needs a base above, and breaks when it loses it.
 * Placing an icicle under a short cluster makes it a tip and the cluster above its base. No collision; drops only
 * with Silk Touch. Models from tools/model_export/velorum_materials.py.
 */
public class HangingIcicleBlock extends Block {
	public static final MapCodec<HangingIcicleBlock> CODEC = simpleCodec(HangingIcicleBlock::new);
	public static final EnumProperty<Length> LENGTH = EnumProperty.create("length", Length.class);
	// The models' extents over every layout and turn (velorum_materials.py prints them).
	private static final VoxelShape SHORT = box(3, 3.5, 3, 13, 16, 13);
	private static final VoxelShape BASE = box(4, 0, 4, 12, 16, 12);
	private static final VoxelShape TIP = box(6, 1, 6, 10, 16, 10);

	public HangingIcicleBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LENGTH, Length.SHORT));
	}

	@Override
	protected MapCodec<? extends Block> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LENGTH);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(LENGTH)) {
			case BASE -> BASE;
			case TIP -> TIP;
			default -> SHORT;
		};
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos above = pos.above();
		BlockState over = level.getBlockState(above);
		if (state.getValue(LENGTH) == Length.TIP) {
			// a short cluster above becomes this tip's base on the neighbour update that follows placement
			return over.is(this) && over.getValue(LENGTH) != Length.TIP;
		}
		return over.isFaceSturdy(level, above, Direction.DOWN);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction side, BlockState neighbour, LevelAccessor level,
			BlockPos pos, BlockPos neighbourPos) {
		if (side == Direction.UP && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		if (side == Direction.DOWN && state.getValue(LENGTH) == Length.SHORT && neighbour.is(this)
				&& neighbour.getValue(LENGTH) == Length.TIP) {
			return state.setValue(LENGTH, Length.BASE);
		}
		return super.updateShape(state, side, neighbour, level, pos, neighbourPos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		LevelReader level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		BlockState over = level.getBlockState(pos.above());
		BlockState state = defaultBlockState().setValue(LENGTH,
				over.is(this) && over.getValue(LENGTH) != Length.TIP ? Length.TIP : Length.SHORT);
		return state.canSurvive(level, pos) ? state : null;
	}

	public enum Length implements StringRepresentable {
		SHORT("short"), BASE("base"), TIP("tip");

		private final String name;

		Length(String name) {
			this.name = name;
		}

		@Override
		public String getSerializedName() {
			return name;
		}
	}
}
