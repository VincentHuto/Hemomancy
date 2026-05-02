package com.vincenthuto.hemomancy.common.block.harbinger;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A blood crystal bud that grows upward from a surface. Placed by the
 * Ghastly Alembic leak mechanism when blood seeps through a venous stone
 * variant or bone block found anywhere in the 3×3 area one block below the alembic.
 *
 * <p>Grows through 4 stages (AGE 0–3). Stages 0–2 are indestructible without
 * Silk Touch and yield nothing.  Stage 3 (mature) drops
 * {@code hemomancy:blood_crystal_shard} × 2–4 when broken.
 *
 * <p>The block is purely upward-facing and requires a solid block directly below.
 */
public class BloodCrystalBudBlock extends Block {

	public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

	/** Number of growth stages (0 is freshest, MAX_AGE is mature). */
	public static final int MAX_AGE = 3;

	// Hitboxes per stage (growing upward)
	private static final VoxelShape SHAPE_0 = Block.box(5, 0, 5, 11, 3, 11);
	private static final VoxelShape SHAPE_1 = Block.box(4, 0, 4, 12, 5, 12);
	private static final VoxelShape SHAPE_2 = Block.box(3, 0, 3, 13, 8, 13);
	private static final VoxelShape SHAPE_3 = Block.box(3, 0, 3, 13, 12, 13);

	public BloodCrystalBudBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext ctx) {
		return switch (state.getValue(AGE)) {
			case 0 -> SHAPE_0;
			case 1 -> SHAPE_1;
			case 2 -> SHAPE_2;
			default -> SHAPE_3;
		};
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		BlockState belowState = level.getBlockState(below);
		return belowState.isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
			LevelAccessor level, BlockPos pos, BlockPos facingPos) {
		if (facing == Direction.DOWN && !canSurvive(state, level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, facing, facingState, level, pos, facingPos);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext ctx) {
		return this.defaultBlockState().setValue(AGE, 0);
	}

	/** Returns true if this bud has reached its maximum growth stage. */
	public boolean isMature(BlockState state) {
		return state.getValue(AGE) >= MAX_AGE;
	}

	/**
	 * Advances this bud by one age stage.  Returns false if already mature.
	 */
	public boolean tryGrow(Level level, BlockPos pos, BlockState state, RandomSource rand) {
		int age = state.getValue(AGE);
		if (age >= MAX_AGE) return false;
		level.setBlock(pos, state.setValue(AGE, age + 1), Block.UPDATE_ALL);
		return true;
	}
}
