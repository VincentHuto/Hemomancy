package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class OssuaryClockBlock extends Block {
	public static final net.minecraft.world.level.block.state.properties.EnumProperty<DoubleBlockHalf> HALF =
			BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape LOWER_SHAPE = Block.box(1.0D, 0.0D, 2.0D, 15.0D, 16.0D, 14.0D);
	private static final VoxelShape UPPER_SHAPE = Block.box(2.0D, 0.0D, 3.0D, 14.0D, 16.0D, 13.0D);

	public OssuaryClockBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(HALF, DoubleBlockHalf.LOWER)
				.setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(HALF, FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
			return null;
		}
		return this.defaultBlockState()
				.setValue(HALF, DoubleBlockHalf.LOWER)
				.setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER).setValue(FACING, state.getValue(FACING)),
				Block.UPDATE_ALL);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		DoubleBlockHalf half = state.getValue(HALF);
		BlockPos partnerPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
		BlockState partner = level.getBlockState(partnerPos);
		if (half == DoubleBlockHalf.LOWER) {
			return partner.canBeReplaced()
					|| partner.is(this) && partner.getValue(HALF) == DoubleBlockHalf.UPPER;
		}
		return partner.is(this) && partner.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		boolean partnerDirection = half == DoubleBlockHalf.LOWER ? direction == Direction.UP : direction == Direction.DOWN;
		if (partnerDirection && (!neighborState.is(this) || neighborState.getValue(HALF) == half)) {
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
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide) {
			DoubleBlockHalf half = state.getValue(HALF);
			BlockPos partnerPos = half == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
			BlockState partner = level.getBlockState(partnerPos);
			if (partner.is(this) && partner.getValue(HALF) != half) {
				level.setBlock(partnerPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
				level.levelEvent(player, 2001, partnerPos, Block.getId(partner));
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER_SHAPE : UPPER_SHAPE;
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hitResult) {
		return handleUse(level, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		return handleUse(level, player) == InteractionResult.SUCCESS
				? ItemInteractionResult.SUCCESS
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private InteractionResult handleUse(Level level, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}
		long dayTime = level.getDayTime();
		String time = OssuaryClockReadout.timeBand(dayTime);
		String moon = OssuaryClockReadout.moonPhase(dayTime / 24000L);
		player.displayClientMessage(Component.translatable("block.hemomancy.ossuary_clock.readout",
				Component.translatable("block.hemomancy.ossuary_clock.time." + time),
				Component.translatable("block.hemomancy.ossuary_clock.moon." + moon)), false);
		return InteractionResult.SUCCESS;
	}
}
