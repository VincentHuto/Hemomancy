package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenEntity;
import com.vincenthuto.hemomancy.common.entity.boss.court.BloomWardenRules;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A bloom the Bloom Warden sows over the tier floor (Track 10, Tier 2). While a shaft of light reaches it from above
 * (open sky or a lamp up the shaft), it feeds the Warden; cut the light off and it withers. It breaks at a touch, drops
 * nothing, and clearing one is fighting the Warden, never a wrong against the Garden. A bloom with no Warden near
 * withers on its own, so the tier is restored after the fight even across a reload. Drawn with the Sunburst bloom's pad.
 */
public class WardenBloomBlock extends Block {
	public static final IntegerProperty TURN = IntegerProperty.create("turn", 0, 3);
	private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 1.5, 15);
	/** A bloom farther than this from any Warden withers on its next random tick. */
	public static final double ORPHAN_RANGE = 24.0D;

	public WardenBloomBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(TURN, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(TURN);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		return direction == Direction.DOWN && !canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, direction, neighbor, level, pos, neighborPos);
	}

	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getEntitiesOfClass(BloomWardenEntity.class, new AABB(pos).inflate(ORPHAN_RANGE)).isEmpty()) {
			level.removeBlock(pos, false);
		}
	}

	/** Whether a bloom can be sown at {@code pos}: open air over a sturdy floor. */
	public static boolean canSow(Level level, BlockPos pos) {
		return level.isEmptyBlock(pos) && level.getFluidState(pos).isEmpty()
				&& level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
	}

	/**
	 * Whether light reaches the bloom from above: up its column, past water and anything that lets light through, to
	 * open sky or a block giving off bright light (a lamp hung in the shaft). A solid roof cuts it off.
	 */
	public static boolean fed(Level level, BlockPos pos) {
		BlockPos.MutableBlockPos cursor = pos.mutable();
		for (int dy = 1; dy <= BloomWardenRules.SHAFT_HEIGHT; dy++) {
			cursor.setY(pos.getY() + dy);
			if (!level.isInWorldBounds(cursor)) return true;
			BlockState state = level.getBlockState(cursor);
			if (state.getLightEmission(level, cursor) >= BloomWardenRules.SHAFT_LIGHT) return true;
			if (state.canOcclude() && state.isSolidRender(level, cursor)) return false;
			if (level.canSeeSky(cursor)) return true;
		}
		return false;
	}
}
