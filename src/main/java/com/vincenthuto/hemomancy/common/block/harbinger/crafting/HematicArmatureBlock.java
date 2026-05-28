package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.entity.utility.ArmatureRestraintEntity;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.tile.crafting.HematicArmatureBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class HematicArmatureBlock extends BaseEntityBlock implements IMultiBlock, SimpleWaterloggedBlock {
	public static final MapCodec<HematicArmatureBlock> CODEC = simpleCodec(HematicArmatureBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final VoxelShape SHAPE = Block.box(1.0D, 0.0D, 1.0D, 15.0D, 16.0D, 15.0D);
	private static final VoxelShape COLLISION_SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
	private static final int MAX_FILLER_Y_OFFSET = 5;
	private static final BlockPos[] CENTER_FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0), new BlockPos(0, 2, 0)
	};
	private static final BlockPos[] BOWL_STAND_FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(-3, 0, 0), new BlockPos(-3, 1, 0),
			new BlockPos(-2, 0, 0), new BlockPos(-2, 1, 0),
			new BlockPos(-2, 0, 1),
			new BlockPos(3, 0, 0), new BlockPos(3, 1, 0),
			new BlockPos(2, 0, 0), new BlockPos(2, 1, 0),
			new BlockPos(2, 0, 1)
	};
	private static final BlockPos[] TOP_ARCH_FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(-2, 2, 0), new BlockPos(2, 2, 0),
			new BlockPos(-2, 3, 0), new BlockPos(-1, 3, 0),
			new BlockPos(0, 3, 0), new BlockPos(1, 3, 0),
			new BlockPos(2, 3, 0)
	};
	private static final BlockPos[] RESERVOIR_HEART_FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 4, 0), new BlockPos(0, 5, 0)
	};
	private static final BlockPos[] FILLER_OFFSETS = combineFillerOffsets();
	private static final double RESTRAINT_Y_OFFSET = 0.05D;

	public HematicArmatureBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		Direction facing = context.getHorizontalDirection().getOpposite();
		if (pos.getY() + MAX_FILLER_Y_OFFSET <= level.getMaxBuildHeight()
				&& canPlaceMultiBlock(level, pos, facing)) {
			return this.defaultBlockState()
					.setValue(FACING, facing)
					.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
		}
		return null;
	}

	@Override
	public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
		Direction facing = level.getBlockState(mainPos).hasProperty(FACING)
				? level.getBlockState(mainPos).getValue(FACING)
				: Direction.SOUTH;
		return canPlaceMultiBlock(level, mainPos, facing);
	}

	private boolean canPlaceMultiBlock(Level level, BlockPos mainPos, Direction facing) {
		for (BlockPos offset : rotatedFillerOffsets(facing)) {
			if (!level.getBlockState(mainPos.offset(offset)).canBeReplaced()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
		}
	}

	@Override
	public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
		Direction facing = mainState.hasProperty(FACING) ? mainState.getValue(FACING) : Direction.SOUTH;
		for (BlockPos offset : rotatedFillerOffsets(facing)) {
			BlockPos fillerPos = mainPos.offset(offset);
			boolean waterlogged = level.getFluidState(fillerPos).getType() == Fluids.WATER;
			level.setBlock(fillerPos, BlockInit.filler_block.get().defaultBlockState()
					.setValue(FillerBlock.WATERLOGGED, waterlogged), 3);
			if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) {
				filler.setMainBlockPos(mainPos);
			}
		}
	}

	@Override
	public void removeFillers(Level level, BlockPos mainPos) {
		BlockState mainState = level.getBlockState(mainPos);
		Direction facing = mainState.hasProperty(FACING) ? mainState.getValue(FACING) : Direction.SOUTH;
		removeFillers(level, mainPos, facing);
	}

	private void removeFillers(Level level, BlockPos mainPos, Direction facing) {
		for (BlockPos offset : rotatedFillerOffsets(facing)) {
			removeFillerAt(level, mainPos.offset(offset));
		}
		removeLinkedFillerBlocks(level, mainPos);
	}

	private void removeLinkedFillerBlocks(Level level, BlockPos mainPos) {
		for (int x = -4; x <= 4; x++) {
			for (int y = 0; y <= MAX_FILLER_Y_OFFSET; y++) {
				for (int z = -4; z <= 4; z++) {
					BlockPos fillerPos = mainPos.offset(x, y, z);
					if (isLinkedFiller(level, fillerPos, mainPos)) {
						removeFillerAt(level, fillerPos);
					}
				}
			}
		}
	}

	private static boolean isLinkedFiller(Level level, BlockPos fillerPos, BlockPos mainPos) {
		return level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler
				&& mainPos.equals(filler.getMainBlockPos());
	}

	private static void removeFillerAt(Level level, BlockPos fillerPos) {
		BlockState state = level.getBlockState(fillerPos);
		if (state.is(BlockInit.filler_block.get())) {
			boolean waterlogged = state.getValue(FillerBlock.WATERLOGGED);
			level.setBlock(fillerPos, waterlogged ? Blocks.WATER.defaultBlockState()
					: Blocks.AIR.defaultBlockState(), 3);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new HematicArmatureBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide ? null
				: createTickerHelper(type, BlockEntityInit.hematic_armature.get(),
						HematicArmatureBlockEntity::serverTick);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.ENTITYBLOCK_ANIMATED;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
			CollisionContext context) {
		return COLLISION_SHAPE;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
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
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				discardRestraintsForArmature(level, pos);
				removeFillers(level, pos, state.getValue(FACING));
			}
			if (level.getBlockEntity(pos) instanceof HematicArmatureBlockEntity armature) {
				Containers.dropContents(level, pos, armature);
				level.updateNeighbourForOutputSignal(pos, this);
			}
		}
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
			Player player, BlockHitResult hit) {
		return handleUse(ItemStack.EMPTY, level, pos, state, player, InteractionHand.MAIN_HAND, hit);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		InteractionResult result = handleUse(stack, level, pos, state, player, hand, hit);
		return result == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	private InteractionResult handleUse(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player,
			InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide || !(player instanceof ServerPlayer serverPlayer)) {
			return InteractionResult.SUCCESS;
		}
		if (!(level.getBlockEntity(pos) instanceof HematicArmatureBlockEntity armature)) {
			return InteractionResult.PASS;
		}

		if (player.isCrouching()) {
			return armature.extractMostRecentBowlItem(serverPlayer)
					? InteractionResult.SUCCESS
					: InteractionResult.PASS;
		}
		if (stack.isEmpty()) {
			return InteractionResult.PASS;
		}
		if (armature.useBloodContainerInHand(serverPlayer, hand)) {
			return InteractionResult.SUCCESS;
		}
		return armature.insertHeldBowlItems(serverPlayer, hand)
				? InteractionResult.SUCCESS
				: InteractionResult.PASS;
	}

	@Override
	public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
		super.stepOn(level, pos, state, entity);
		tryRestrainPlayer(level, pos, state, entity);
	}

	@Override
	protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		super.entityInside(state, level, pos, entity);
		tryRestrainPlayer(level, pos, state, entity);
	}

	private static void tryRestrainPlayer(Level level, BlockPos pos, BlockState state, Entity entity) {
		if (level.isClientSide || !(entity instanceof ServerPlayer player)) {
			return;
		}
		if (player.isCrouching() || player.isPassenger() || isRestrainedOnThisArmature(player, pos)) {
			return;
		}
		ArmatureRestraintEntity existing = findRestraintEntity(level, pos);
		if (existing != null && !existing.getPassengers().isEmpty()) {
			return;
		}
		restrainPlayer(level, pos, state, player);
	}

	private static boolean isRestrainedOnThisArmature(Player player, BlockPos pos) {
		return player.getVehicle() instanceof ArmatureRestraintEntity restraint && restraint.isForArmature(pos);
	}

	private static void restrainPlayer(Level level, BlockPos pos, BlockState state, ServerPlayer player) {
		Vec3 restraintPos = restraintPosition(pos, state);
		ArmatureRestraintEntity restraint = findRestraintEntity(level, pos);
		if (restraint == null) {
			restraint = new ArmatureRestraintEntity(level, pos, restraintPos.x, restraintPos.y, restraintPos.z);
			level.addFreshEntity(restraint);
		} else {
			restraint.setPos(restraintPos.x, restraintPos.y, restraintPos.z);
		}
		if (level.getBlockEntity(pos) instanceof HematicArmatureBlockEntity armature) {
			armature.setRestrainedPlayer(player.getUUID());
		}
		player.startRiding(restraint, true);
	}

	@Nullable
	private static ArmatureRestraintEntity findRestraintEntity(Level level, BlockPos pos) {
		AABB search = new AABB(pos).inflate(2.0D, 3.0D, 2.0D);
		for (ArmatureRestraintEntity restraint : level.getEntitiesOfClass(ArmatureRestraintEntity.class, search)) {
			if (restraint.isForArmature(pos) && restraint.isAlive()) {
				return restraint;
			}
		}
		return null;
	}

	private static Vec3 restraintPosition(BlockPos pos, BlockState state) {
		Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
		Vec3 nudge = Vec3.atLowerCornerOf(facing.getNormal()).scale(0.04D);
		return new Vec3(pos.getX() + 0.5D + nudge.x,
				pos.getY() + RESTRAINT_Y_OFFSET,
				pos.getZ() + 0.5D + nudge.z);
	}

	private static void discardRestraintsForArmature(Level level, BlockPos pos) {
		AABB search = new AABB(pos).inflate(3.0D, 4.0D, 3.0D);
		for (ArmatureRestraintEntity restraint : level.getEntitiesOfClass(ArmatureRestraintEntity.class, search)) {
			if (restraint.isForArmature(pos)) {
				restraint.discard();
			}
		}
	}

	private static BlockPos[] combineFillerOffsets() {
		BlockPos[] offsets = new BlockPos[CENTER_FILLER_OFFSETS.length + BOWL_STAND_FILLER_OFFSETS.length
				+ TOP_ARCH_FILLER_OFFSETS.length + RESERVOIR_HEART_FILLER_OFFSETS.length];
		System.arraycopy(CENTER_FILLER_OFFSETS, 0, offsets, 0, CENTER_FILLER_OFFSETS.length);
		System.arraycopy(BOWL_STAND_FILLER_OFFSETS, 0, offsets, CENTER_FILLER_OFFSETS.length,
				BOWL_STAND_FILLER_OFFSETS.length);
		int archStart = CENTER_FILLER_OFFSETS.length + BOWL_STAND_FILLER_OFFSETS.length;
		System.arraycopy(TOP_ARCH_FILLER_OFFSETS, 0, offsets, archStart, TOP_ARCH_FILLER_OFFSETS.length);
		int reservoirStart = archStart + TOP_ARCH_FILLER_OFFSETS.length;
		System.arraycopy(RESERVOIR_HEART_FILLER_OFFSETS, 0, offsets, reservoirStart,
				RESERVOIR_HEART_FILLER_OFFSETS.length);
		return offsets;
	}

	private static BlockPos[] rotatedFillerOffsets(Direction facing) {
		BlockPos[] rotated = new BlockPos[FILLER_OFFSETS.length];
		for (int i = 0; i < FILLER_OFFSETS.length; i++) {
			rotated[i] = rotateOffset(FILLER_OFFSETS[i], facing);
		}
		return rotated;
	}

	private static BlockPos rotateOffset(BlockPos offset, Direction facing) {
		return switch (facing) {
			case NORTH -> new BlockPos(-offset.getX(), offset.getY(), -offset.getZ());
			case EAST -> new BlockPos(offset.getZ(), offset.getY(), -offset.getX());
			case WEST -> new BlockPos(-offset.getZ(), offset.getY(), offset.getX());
			default -> offset;
		};
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
			LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}
}
