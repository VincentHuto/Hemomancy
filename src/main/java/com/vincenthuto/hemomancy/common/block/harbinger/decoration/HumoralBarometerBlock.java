package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HumoralBarometerBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape NORTH_SHAPE = Block.box(2.0D, 1.0D, 0.0D, 14.0D, 15.0D, 3.0D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(2.0D, 1.0D, 13.0D, 14.0D, 15.0D, 16.0D);
	private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 1.0D, 2.0D, 3.0D, 15.0D, 14.0D);
	private static final VoxelShape EAST_SHAPE = Block.box(13.0D, 1.0D, 2.0D, 16.0D, 15.0D, 14.0D);

	public HumoralBarometerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		if (clickedFace.getAxis().isVertical()) {
			return null;
		}
		return this.defaultBlockState().setValue(FACING, clickedFace.getOpposite());
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction facing = state.getValue(FACING);
		BlockPos supportPos = pos.relative(facing);
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, facing.getOpposite());
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		if (direction == state.getValue(FACING) && !state.canSurvive(level, pos)) {
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
			case SOUTH -> SOUTH_SHAPE;
			case WEST -> WEST_SHAPE;
			case EAST -> EAST_SHAPE;
			default -> NORTH_SHAPE;
		};
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
		String weather = HumoralBarometerReadout.weatherState(level.isRaining(), level.isThundering());
		String pressure = "quiet";
		if (level instanceof ServerLevel serverLevel) {
			ServerLevel overworld = serverLevel.getServer().overworld();
			BloodMoonSavedData data = BloodMoonSavedData.get(overworld);
			long remaining = Math.max(0L, data.getEndTick() - overworld.getGameTime());
			pressure = HumoralBarometerReadout.bloodMoonPressure(data.isActive(), remaining);
		}
		player.displayClientMessage(Component.translatable("block.hemomancy.humoral_barometer.readout",
				Component.translatable("block.hemomancy.humoral_barometer.weather." + weather),
				bloodMoonPressureComponent(pressure)), false);
		return InteractionResult.SUCCESS;
	}

	private Component bloodMoonPressureComponent(String pressure) {
		if (!pressure.startsWith("active_")) {
			return Component.translatable("block.hemomancy.humoral_barometer.blood_moon." + pressure);
		}
		String minutes = pressure.substring("active_".length(), pressure.length() - 1);
		return Component.translatable("block.hemomancy.humoral_barometer.blood_moon.active", minutes);
	}
}
