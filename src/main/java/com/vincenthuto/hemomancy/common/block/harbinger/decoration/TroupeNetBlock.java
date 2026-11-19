package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import java.util.Map;
import java.util.EnumMap;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.util.StringRepresentable;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The safety net under a rigging tower: knotted crimson thread strung on an iron frame, a thin sheet that breaks a
 * fall as a hay bale does. Each side is {@link Side#NET} beside more net, {@link Side#EDGE} (an iron rim) where the net
 * ends, or {@link Side#TIED} (the rim lashed with thread) where a pole or solid block stands beside it, so a slung net
 * is seen held by what it hangs from.
 */
public class TroupeNetBlock extends Block {
	/** The share of fall damage a net lets through. */
	public static final float FALL_DAMAGE = 0.2F;
	private static final VoxelShape SHAPE = box(0, 0, 0, 16, 3, 16);

	public enum Side implements StringRepresentable {
		NET, EDGE, TIED;

		@Override
		public String getSerializedName() {
			return name().toLowerCase(java.util.Locale.ROOT);
		}
	}

	public static final Map<Direction, EnumProperty<Side>> SIDES = new EnumMap<>(Map.of(
			Direction.NORTH, EnumProperty.create("north", Side.class), Direction.EAST, EnumProperty.create("east", Side.class),
			Direction.SOUTH, EnumProperty.create("south", Side.class), Direction.WEST, EnumProperty.create("west", Side.class)));

	public TroupeNetBlock(Properties properties) {
		super(properties);
		BlockState state = stateDefinition.any();
		for (EnumProperty<Side> side : SIDES.values()) state = state.setValue(side, Side.EDGE);
		registerDefaultState(state);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		SIDES.values().forEach(builder::add);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState state = defaultBlockState();
		for (Direction side : Direction.Plane.HORIZONTAL)
			state = state.setValue(SIDES.get(side), side(context.getLevel(), context.getClickedPos(), side));
		return state;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		return direction.getAxis().isHorizontal() ? state.setValue(SIDES.get(direction), side(level, pos, direction)) : state;
	}

	/** More net beside it, a pole or a solid face to tie off on, or nothing: the net's edge. */
	public static Side side(BlockGetter level, BlockPos pos, Direction direction) {
		BlockPos at = pos.relative(direction);
		BlockState beside = level.getBlockState(at);
		if (beside.getBlock() instanceof TroupeNetBlock) return Side.NET;
		if (beside.getBlock() instanceof TroupePoleBlock || beside.isFaceSturdy(level, at, direction.getOpposite()))
			return Side.TIED;
		return Side.EDGE;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		BlockState turned = state;
		for (Direction side : Direction.Plane.HORIZONTAL)
			turned = turned.setValue(SIDES.get(rotation.rotate(side)), state.getValue(SIDES.get(side)));
		return turned;
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		BlockState turned = state;
		for (Direction side : Direction.Plane.HORIZONTAL)
			turned = turned.setValue(SIDES.get(mirror.mirror(side)), state.getValue(SIDES.get(side)));
		return turned;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
		entity.causeFallDamage(fallDistance, FALL_DAMAGE, level.damageSources().fall());
	}
}
