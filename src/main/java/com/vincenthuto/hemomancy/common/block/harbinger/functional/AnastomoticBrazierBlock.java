package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.tile.functional.AnastomoticBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.stream.Stream;

public class AnastomoticBrazierBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final IntegerProperty RITUAL_PHASE = IntegerProperty.create("ritual_phase", 0, 1);

	private static final VoxelShape SHAPE = Stream
			.of(Block.box(4, 0, 4, 12, 2, 12), Block.box(5, 2, 5, 11, 4, 11), Block.box(6, 4, 6, 10, 13, 10),
					Block.box(5, 13, 5, 11, 16, 11), Block.box(6, 15, 6, 10, 17, 10), Block.box(5, 14, 11, 11, 17, 12),
					Block.box(11, 14, 5, 12, 17, 11), Block.box(5, 14, 4, 11, 17, 5), Block.box(4, 14, 5, 5, 17, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public AnastomoticBrazierBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(RITUAL_PHASE, 0)
				.setValue(WATERLOGGED, false));
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState()
				.setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getValue(RITUAL_PHASE) <= 0) {
			return;
		}
		double x = pos.getX() + 0.5D;
		double y = pos.getY() + 1.2D;
		double z = pos.getZ() + 0.5D;
		level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 0.0D, 0.02D, 0.0D);
		level.addParticle(BloodCellParticleFactory.createData(new ParticleColor(180, 0, 20)),
				x + (random.nextDouble() - 0.5D) * 0.35D, y + random.nextDouble() * 0.2D,
				z + (random.nextDouble() - 0.5D) * 0.35D, 0.0D, 0.04D, 0.0D);
	}

	private InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player,
			ItemStack stack) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		int phase = state.getValue(RITUAL_PHASE);
		if (phase == 0) {
			if (state.getValue(WATERLOGGED)) {
				return InteractionResult.PASS;
			}
			if (stack.isEmpty() || stack.getItem() == Items.FLINT_AND_STEEL || stack.getItem() == Items.FIRE_CHARGE) {
				level.setBlock(pos, state.setValue(RITUAL_PHASE, 1), Block.UPDATE_ALL);
				if (level instanceof ServerLevel serverLevel) {
					HLParticleUtils.spawnPoof(serverLevel, pos, BloodCellParticleFactory.createData(ParticleColor.BLOOD));
				}
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		if (stack.isEmpty() && player.isShiftKeyDown()) {
			level.setBlock(pos, state.setValue(RITUAL_PHASE, 0), Block.UPDATE_ALL);
			if (level instanceof ServerLevel serverLevel) {
				HLParticleUtils.spawnPoof(serverLevel, pos, ParticleTypes.SMOKE);
			}
			return InteractionResult.SUCCESS;
		}

		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof AnastomoticBrazierBlockEntity brazier) {
			if (!stack.isEmpty() && (brazier.tryLearnScar(player, stack) || brazier.tryCommitLoadout(player, stack)
					|| brazier.tryClearLoadout(player, stack))) {
				return InteractionResult.SUCCESS;
			}
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, level, pos, player, ItemStack.EMPTY);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult result) {
		return handleInteraction(state, level, pos, player, stack) == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AnastomoticBrazierBlockEntity(pos, state);
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			level.setBlock(pos, state.setValue(WATERLOGGED, true).setValue(RITUAL_PHASE, 0), Block.UPDATE_ALL);
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
			return true;
		}
		return false;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, RITUAL_PHASE, WATERLOGGED);
	}
}
