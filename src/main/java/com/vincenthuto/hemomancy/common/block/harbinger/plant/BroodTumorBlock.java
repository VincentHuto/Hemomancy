package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

/**
 * A brood tumor: a fleshy amber egg sac two blocks tall that the Spawn Mother's mortar roots in the Nursery floor. It
 * pulses, hatches a brood fungling if left alone, and breaks in a blow or two. While it stands it is solid cover, tall
 * enough to hide a standing player from the Gaze. Breaking either half breaks both; it never drops anything.
 */
public class BroodTumorBlock extends Block {
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	private static final VoxelShape LOWER = Shapes.or(Block.box(1, 0, 1, 15, 16, 15), Block.box(0, 2, 2, 16, 12, 14),
			Block.box(2, 2, 0, 14, 12, 16));
	private static final VoxelShape UPPER = Shapes.or(Block.box(2, 0, 2, 14, 11, 14), Block.box(4, 11, 4, 12, 14, 12));

	public BroodTumorBlock(Properties properties) {
		super(properties);
		registerDefaultState(this.stateDefinition.any().setValue(HALF, DoubleBlockHalf.LOWER));
	}

	/** Roots a tumor with its lower half at {@code pos}; false when the floor or the space above will not take it. */
	public boolean place(Level level, BlockPos pos) {
		if (!level.getBlockState(pos).canBeReplaced() || !level.getBlockState(pos.above()).canBeReplaced()) return false;
		if (!level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
		// never around a creature: a player, a fungling or her own claws standing there would be walled in
		if (!level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).expandTowards(0.0D, 1.0D, 0.0D)).isEmpty()) return false;
		level.setBlock(pos, defaultBlockState().setValue(HALF, DoubleBlockHalf.LOWER), Block.UPDATE_ALL);
		level.setBlock(pos.above(), defaultBlockState().setValue(HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
		return true;
	}

	/** Whether {@code pos} holds the lower half of a standing tumor. */
	public boolean isRoot(BlockGetter level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);
		return state.is(this) && state.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	/** Takes the whole tumor out, both halves, with the break burst and no drop. */
	public void burst(Level level, BlockPos lower) {
		if (isRoot(level, lower)) level.destroyBlock(lower, false);
		BlockState upper = level.getBlockState(lower.above());
		if (upper.is(this)) level.setBlock(lower.above(), Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER;
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos pos, BlockPos facingPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		boolean partner = half == DoubleBlockHalf.LOWER ? facing == Direction.UP : facing == Direction.DOWN;
		if (partner && !(facingState.is(this) && facingState.getValue(HALF) != half)) return Blocks.AIR.defaultBlockState();
		if (half == DoubleBlockHalf.LOWER && facing == Direction.DOWN && !state.canSurvive(level, pos))
			return Blocks.AIR.defaultBlockState();
		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
			BlockState below = level.getBlockState(pos.below());
			return below.is(this) && below.getValue(HALF) == DoubleBlockHalf.LOWER;
		}
		return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// Break the partner with its own burst so a hit on either half takes the whole sac.
		BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		BlockState partner = level.getBlockState(other);
		if (!level.isClientSide && partner.is(this) && partner.getValue(HALF) != state.getValue(HALF)) {
			level.setBlock(other, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
			level.levelEvent(player, 2001, other, Block.getId(partner));
		}
		return super.playerWillDestroy(level, pos, state, player);
	}
}
