package com.vincenthuto.hemomancy.common.block.shared;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.tile.shared.LidlessDoorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * A door-sized frame around a void. A player who steps into the panel is held in the doorway by
 * {@link LidlessPassageService} for a crossing and let out the far side; the panel is solid to everything that
 * cannot cross. No open state, no hinge, no redstone.
 */
public class LidlessDoorBlock extends BaseEntityBlock {
	public static final MapCodec<LidlessDoorBlock> CODEC = simpleCodec(LidlessDoorBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	private static final VoxelShape PANEL_Z = Block.box(0, 0, 7, 16, 16, 9);
	private static final VoxelShape PANEL_X = Block.box(7, 0, 0, 9, 16, 16);

	public LidlessDoorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, HALF);
	}

	/** The lower half's position for either half. */
	public static BlockPos lowerOf(BlockState state, BlockPos pos) {
		return state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos.below() : pos;
	}

	/** Sets the upper half above a freshly placed lower half. */
	public static void placeUpper(Level level, BlockPos lower, BlockState lowerState) {
		level.setBlock(lower.above(), lowerState.setValue(HALF, DoubleBlockHalf.UPPER), 3);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) return null;
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection()).setValue(HALF, DoubleBlockHalf.LOWER);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		placeUpper(level, pos, state);
	}

	@Override
	protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos currentPos, BlockPos facingPos) {
		DoubleBlockHalf half = state.getValue(HALF);
		if (facing.getAxis() == Direction.Axis.Y && half == DoubleBlockHalf.LOWER == (facing == Direction.UP)) {
			return facingState.is(this) && facingState.getValue(HALF) != half ? state : Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		if (!level.isClientSide) {
			BlockPos other = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos.above() : pos.below();
			if (level.getBlockState(other).is(this)) level.setBlock(other, Blocks.AIR.defaultBlockState(), 35);
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	private static VoxelShape panel(BlockState state) {
		return state.getValue(FACING).getAxis() == Direction.Axis.Z ? PANEL_Z : PANEL_X;
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return panel(state);
	}

	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (context instanceof EntityCollisionContext entityContext) {
			Entity entity = entityContext.getEntity();
			if (entity != null && LidlessPassageService.mayCross(entity)) return Shapes.empty();
		}
		return panel(state);
	}

	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (level.isClientSide || !(entity instanceof ServerPlayer player)) return;
		LidlessPassageService.offer(player, portalAt(level, lowerOf(state, pos)));
	}

	/** The doorway at {@code lower}, or null when no lower door half stands there. */
	@Nullable
	public static LidlessPortal portalAt(Level level, BlockPos lower) {
		BlockState state = level.getBlockState(lower);
		if (!isLowerDoor(state)) return null;
		return new DoorPortal(lower.immutable(), state.getValue(FACING));
	}

	private static boolean isLowerDoor(BlockState state) {
		return state.getBlock() instanceof LidlessDoorBlock && state.getValue(HALF) == DoubleBlockHalf.LOWER;
	}

	/** A door: anyone who may cross, the far cell on the side they are not standing on, the world beyond. */
	private record DoorPortal(BlockPos anchor, Direction facing) implements LidlessPortal {
		@Override
		public boolean admits(ServerPlayer player) {
			return LidlessPassageService.mayCross(player);
		}

		@Override
		public boolean towardFront(ServerPlayer player) {
			Vec3 facingVec = Vec3.atLowerCornerOf(facing.getNormal());
			return player.position().subtract(Vec3.atBottomCenterOf(anchor)).dot(facingVec) < 0;
		}

		@Override
		public LidlessPassageService.Mode mode() {
			return LidlessPassageService.Mode.DOOR;
		}

		@Override
		public boolean stillValid(ServerLevel level) {
			BlockState state = level.getBlockState(anchor);
			return isLowerDoor(state) && state.getValue(FACING) == facing;
		}

		@Override
		public void completed(ServerPlayer player, boolean towardFront) {
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new LidlessDoorBlockEntity(pos, state) : null;
	}
}
