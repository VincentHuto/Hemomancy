package com.vincenthuto.hemomancy.common.block.harbinger.plant;

import com.vincenthuto.hemomancy.common.init.BlockInit;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.TriState;

import java.util.List;

/**
 * Rafflesia arnoldii — the world's largest flower.
 * Parasitic, lacks leaves, stems, and roots; derives all nutrients from vines.
 * Very rare spawn. Causes nausea to nearby players.
 */
public class RafflesiaBlock extends FlowerBlock {
	public static final DirectionProperty FACING = DirectionProperty.create("facing", direction -> direction != Direction.DOWN);
	private static final VoxelShape FLOOR_SHAPE = Block.box(2, 0, 2, 14, 12, 14);
	private static final VoxelShape NORTH_SHAPE = Block.box(2, 2, 8, 14, 14, 16);
	private static final VoxelShape SOUTH_SHAPE = Block.box(2, 2, 0, 14, 14, 8);
	private static final VoxelShape EAST_SHAPE = Block.box(0, 2, 2, 8, 14, 14);
	private static final VoxelShape WEST_SHAPE = Block.box(8, 2, 2, 16, 14, 14);

	public RafflesiaBlock(Holder<MobEffect> effect, int effectDuration, Properties properties) {
		super(effect, effectDuration, properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getClickedFace();
		if (facing == Direction.DOWN) {
			return null;
		}
		BlockState state = defaultBlockState().setValue(FACING, facing);
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		if (facing.getAxis().isHorizontal()) {
			BlockState support = level.getBlockState(pos.relative(facing.getOpposite()));
			return support.is(BlockTags.LOGS) || support.is(BlockInit.infested_wood.get());
		}
		BlockPos below = pos.below();
		BlockState belowState = level.getBlockState(below);
		return belowState.canSustainPlant(level, below, Direction.UP, defaultBlockState()) == TriState.TRUE
				|| belowState.is(BlockInit.erythrocytic_mycelium.get())
				|| belowState.is(BlockInit.infested_wood.get())
				|| belowState.is(Blocks.MYCELIUM)
				|| belowState.is(BlockTags.LOGS);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		if (direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return switch (state.getValue(FACING)) {
			case NORTH -> NORTH_SHAPE;
			case SOUTH -> SOUTH_SHAPE;
			case EAST -> EAST_SHAPE;
			case WEST -> WEST_SHAPE;
			default -> FLOOR_SHAPE;
		};
	}

	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		AABB area = new AABB(pos).inflate(6.0);
		List<Player> players = level.getEntitiesOfClass(Player.class, area);
		for (Player player : players) {
			if (!player.hasEffect(MobEffects.CONFUSION)) {
				player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0, true, true, true));
			}
		}
	}
}
