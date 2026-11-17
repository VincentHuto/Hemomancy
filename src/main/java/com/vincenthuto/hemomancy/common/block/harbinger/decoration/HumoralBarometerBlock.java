package com.vincenthuto.hemomancy.common.block.harbinger.decoration;

import com.vincenthuto.hemomancy.common.event.worldevent.BloodMoonSavedData;
import com.vincenthuto.hemomancy.common.world.answer.BloodSpilledLedger;
import com.vincenthuto.hemomancy.common.world.answer.WorldAnswerRules;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
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

/**
 * The Humoral Barometer: weather as the four humours, and (core-loop Track 10) the blood spilled in its region. Its
 * needle and column stand at the region's band ({@link #PRESSURE}, still to seething), refreshed every few seconds
 * on its own scheduled tick; using it reads the weather, the band and the Blood Moon.
 */
public class HumoralBarometerBlock extends Block {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final IntegerProperty PRESSURE = IntegerProperty.create("pressure", WorldAnswerRules.STILL,
			WorldAnswerRules.SEETHING);
	/** How often the needle reads the region. */
	public static final int READ_TICKS = 100;
	private static final VoxelShape NORTH_SHAPE = Block.box(3.0D, 0.0D, 0.0D, 13.0D, 16.0D, 2.5D);
	private static final VoxelShape SOUTH_SHAPE = Block.box(3.0D, 0.0D, 13.5D, 13.0D, 16.0D, 16.0D);
	private static final VoxelShape WEST_SHAPE = Block.box(0.0D, 0.0D, 3.0D, 2.5D, 16.0D, 13.0D);
	private static final VoxelShape EAST_SHAPE = Block.box(13.5D, 0.0D, 3.0D, 16.0D, 16.0D, 13.0D);

	public HumoralBarometerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(PRESSURE, 0));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, PRESSURE);
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
	protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (!level.isClientSide && !oldState.is(this)) level.scheduleTick(pos, this, 1);
	}

	@Override
	protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		read(level, pos, state);
		level.scheduleTick(pos, this, READ_TICKS);
	}

	/**
	 * A barometer a structure placed (the Surveyor's Bivouac) never saw {@code onPlace}; a random tick starts its
	 * reading cycle if nothing is scheduled.
	 */
	@Override
	protected boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!level.getBlockTicks().hasScheduledTick(pos, this)) level.scheduleTick(pos, this, 1);
	}

	/** Sets the needle to the region's band; returns the band. */
	public static int read(ServerLevel level, BlockPos pos, BlockState state) {
		int band = BloodSpilledLedger.band(level, pos);
		if (state.hasProperty(PRESSURE) && state.getValue(PRESSURE) != band) {
			level.setBlock(pos, state.setValue(PRESSURE, band), Block.UPDATE_CLIENTS);
		}
		return band;
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
		return handleUse(state, level, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hitResult) {
		return handleUse(state, level, pos, player) == InteractionResult.SUCCESS
				? ItemInteractionResult.SUCCESS
				: ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
	}

	private InteractionResult handleUse(BlockState state, Level level, BlockPos pos, Player player) {
		if (!(level instanceof ServerLevel serverLevel)) {
			return InteractionResult.SUCCESS;
		}
		String weather = HumoralBarometerReadout.weatherState(level.isRaining(), level.isThundering());
		ServerLevel overworld = serverLevel.getServer().overworld();
		BloodMoonSavedData data = BloodMoonSavedData.get(overworld);
		long remaining = Math.max(0L, data.getEndTick() - overworld.getGameTime());
		String pressure = HumoralBarometerReadout.bloodMoonPressure(data.isActive(), remaining);
		int band = read(serverLevel, pos, state);
		player.displayClientMessage(Component.translatable("block.hemomancy.humoral_barometer.readout",
				Component.translatable("block.hemomancy.humoral_barometer.weather." + weather),
				Component.translatable("world_answer.hemomancy.band." + band),
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
