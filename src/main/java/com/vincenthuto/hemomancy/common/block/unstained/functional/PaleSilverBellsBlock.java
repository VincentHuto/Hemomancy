package com.vincenthuto.hemomancy.common.block.unstained.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FaceAttachedHorizontalDirectionalBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PaleSilverBellsBlock extends Block {

	public static final BooleanProperty RUNG = BooleanProperty.create("rung");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<BellAttachType> ATTACHMENT = BlockStateProperties.BELL_ATTACHMENT;
	private static final VoxelShape NORTH_SOUTH_FLOOR_SHAPE = Block.box(0.0, 0.0, 4.0, 16.0, 16.0, 12.0);
	private static final VoxelShape EAST_WEST_FLOOR_SHAPE = Block.box(4.0, 0.0, 0.0, 12.0, 16.0, 16.0);
	private static final VoxelShape BELL_TOP_SHAPE = Block.box(5.0, 6.0, 5.0, 11.0, 13.0, 11.0);
	private static final VoxelShape BELL_BOTTOM_SHAPE = Block.box(4.0, 4.0, 4.0, 12.0, 6.0, 12.0);
	private static final VoxelShape BELL_SHAPE = Shapes.or(BELL_BOTTOM_SHAPE, BELL_TOP_SHAPE);
	private static final VoxelShape NORTH_SOUTH_BETWEEN_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 16.0));
	private static final VoxelShape EAST_WEST_BETWEEN_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(0.0, 13.0, 7.0, 16.0, 15.0, 9.0));
	private static final VoxelShape TO_WEST_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(0.0, 13.0, 7.0, 13.0, 15.0, 9.0));
	private static final VoxelShape TO_EAST_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(3.0, 13.0, 7.0, 16.0, 15.0, 9.0));
	private static final VoxelShape TO_NORTH_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(7.0, 13.0, 0.0, 9.0, 15.0, 13.0));
	private static final VoxelShape TO_SOUTH_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(7.0, 13.0, 3.0, 9.0, 15.0, 16.0));
	private static final VoxelShape CEILING_SHAPE = Shapes.or(BELL_SHAPE,
			Block.box(7.0, 13.0, 7.0, 9.0, 16.0, 9.0));
	private static final float SILVER_BELLS_PURITY_BOOST = 30.0f;

	public PaleSilverBellsBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, net.minecraft.core.Direction.NORTH)
				.setValue(ATTACHMENT, BellAttachType.FLOOR)
				.setValue(RUNG, Boolean.FALSE));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, ATTACHMENT, RUNG);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();

		if (clickedFace.getAxis() == Direction.Axis.Y) {
			BlockState state = this.defaultBlockState()
					.setValue(ATTACHMENT, clickedFace == Direction.DOWN ? BellAttachType.CEILING : BellAttachType.FLOOR)
					.setValue(FACING, context.getHorizontalDirection())
					.setValue(RUNG, Boolean.FALSE);
			return state.canSurvive(level, pos) ? state : null;
		}

		boolean hasOpposingWalls = clickedFace.getAxis() == Direction.Axis.X
				&& level.getBlockState(pos.west()).isFaceSturdy(level, pos.west(), Direction.EAST)
				&& level.getBlockState(pos.east()).isFaceSturdy(level, pos.east(), Direction.WEST)
				|| clickedFace.getAxis() == Direction.Axis.Z
				&& level.getBlockState(pos.north()).isFaceSturdy(level, pos.north(), Direction.SOUTH)
				&& level.getBlockState(pos.south()).isFaceSturdy(level, pos.south(), Direction.NORTH);

		BlockState state = this.defaultBlockState()
				.setValue(FACING, clickedFace.getOpposite())
				.setValue(ATTACHMENT, hasOpposingWalls ? BellAttachType.DOUBLE_WALL : BellAttachType.SINGLE_WALL)
				.setValue(RUNG, Boolean.FALSE);
		if (state.canSurvive(level, pos)) {
			return state;
		}

		boolean hasFloor = level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
		state = state.setValue(ATTACHMENT, hasFloor ? BellAttachType.FLOOR : BellAttachType.CEILING);
		return state.canSurvive(level, pos) ? state : null;
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
	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState,
			LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		BellAttachType attachment = state.getValue(ATTACHMENT);
		Direction connectedDirection = getConnectedDirection(state).getOpposite();
		if (facing == connectedDirection && !state.canSurvive(level, currentPos)
				&& attachment != BellAttachType.DOUBLE_WALL) {
			return Blocks.AIR.defaultBlockState();
		}

		if (facing.getAxis() == state.getValue(FACING).getAxis()) {
			if (attachment == BellAttachType.DOUBLE_WALL && !facingState.isFaceSturdy(level, facingPos, facing)) {
				return state.setValue(ATTACHMENT, BellAttachType.SINGLE_WALL)
						.setValue(FACING, facing.getOpposite());
			}

			if (attachment == BellAttachType.SINGLE_WALL
					&& connectedDirection.getOpposite() == facing
					&& facingState.isFaceSturdy(level, facingPos, state.getValue(FACING))) {
				return state.setValue(ATTACHMENT, BellAttachType.DOUBLE_WALL);
			}
		}

		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		Direction connectedDirection = getConnectedDirection(state).getOpposite();
		return connectedDirection == Direction.UP
				? Block.canSupportCenter(level, pos.above(), Direction.DOWN)
				: FaceAttachedHorizontalDirectionalBlock.canAttach(level, pos, connectedDirection);
	}

	private static Direction getConnectedDirection(BlockState state) {
		return switch (state.getValue(ATTACHMENT)) {
			case FLOOR -> Direction.UP;
			case CEILING -> Direction.DOWN;
			default -> state.getValue(FACING).getOpposite();
		};
	}

	private static VoxelShape getVoxelShape(BlockState state) {
		Direction direction = state.getValue(FACING);
		BellAttachType attachment = state.getValue(ATTACHMENT);
		if (attachment == BellAttachType.FLOOR) {
			return direction.getAxis() == Direction.Axis.X ? EAST_WEST_FLOOR_SHAPE : NORTH_SOUTH_FLOOR_SHAPE;
		}
		if (attachment == BellAttachType.CEILING) {
			return CEILING_SHAPE;
		}
		if (attachment == BellAttachType.DOUBLE_WALL) {
			return direction.getAxis() == Direction.Axis.X ? EAST_WEST_BETWEEN_SHAPE : NORTH_SOUTH_BETWEEN_SHAPE;
		}
		return switch (direction) {
			case NORTH -> TO_NORTH_SHAPE;
			case SOUTH -> TO_SOUTH_SHAPE;
			case EAST -> TO_EAST_SHAPE;
			default -> TO_WEST_SHAPE;
		};
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return getVoxelShape(state);
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return getVoxelShape(state);
	}

	private InteractionResult handleInteraction(BlockState state, Level level, BlockPos pos, Player player) {
		if (level.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		if (!(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.CONSUME;
		}

		if (state.getValue(RUNG)) {
			player.displayClientMessage(Component.translatable("hemomancy.silver_bells.already_rung"), false);
			return InteractionResult.CONSUME;
		}

		HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
			if (!progress.hasBegunPurification()) {
				player.displayClientMessage(Component.translatable("hemomancy.silver_bells.not_on_path"), false);
				return;
			}
			if (progress.isPurified()) {
				player.displayClientMessage(Component.translatable("hemomancy.silver_bells.already_pure"), false);
				return;
			}

			progress.addPurity(SILVER_BELLS_PURITY_BOOST);
			UnstainedProgressEvents.syncProgress(serverPlayer, progress);

			level.setBlock(pos, state.setValue(RUNG, Boolean.TRUE), 3);
			level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 0.9f);
			level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.3f);
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.sendParticles(ParticleTypes.END_ROD,
						pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
						32, 0.35, 0.6, 0.35, 0.03);
				serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
						pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
						16, 0.45, 0.45, 0.45, 0.06);
			}
			player.displayClientMessage(Component.translatable("hemomancy.silver_bells.chimed"), false);
		});

		return InteractionResult.CONSUME;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return handleInteraction(state, level, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		handleInteraction(state, level, pos, player);
		return ItemInteractionResult.SUCCESS;
	}
}
