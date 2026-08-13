package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.tile.functional.WarpChairBlockEntity;
import com.vincenthuto.hemomancy.common.worldgen.ChamberOfWillManager;
import com.vincenthuto.hemomancy.common.worldgen.ChamberVisitService;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayer.RespawnPosAngle;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.Optional;

public final class WarpChairBlock extends BaseEntityBlock implements IMultiBlock {
	public static final MapCodec<WarpChairBlock> CODEC = simpleCodec(WarpChairBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public WarpChairBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return new BlockPos[] { BlockPos.ZERO.above() };
	}

	@Override
	public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
		return level.getBlockState(mainPos.above()).canBeReplaced();
	}

	@Override
	public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
		BlockPos fillerPos = WarpChairStructureRules.fillerPos(mainPos, mainState.getValue(FACING));
		boolean waterlogged = level.getFluidState(fillerPos).getType() == Fluids.WATER;
		level.setBlock(fillerPos, BlockInit.warp_chair_filler.get().defaultBlockState()
				.setValue(WarpChairFillerBlock.FACING, mainState.getValue(FACING))
				.setValue(WarpChairFillerBlock.WATERLOGGED, waterlogged), 3);
		if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) filler.setMainBlockPos(mainPos);
	}

	@Override
	public void removeFillers(Level level, BlockPos mainPos) {
		BlockPos fillerPos = mainPos.above();
		BlockState filler = level.getBlockState(fillerPos);
		if (filler.is(BlockInit.warp_chair_filler.get())) {
			level.setBlock(fillerPos, filler.getValue(WarpChairFillerBlock.WATERLOGGED)
					? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState(), 3);
		}
	}

	@Nullable
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return canPlaceMultiBlock(context.getLevel(), context.getClickedPos())
				? defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()) : null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) placeFillers(level, pos, state);
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
		if (!state.is(newState.getBlock()) && !level.isClientSide) removeFillers(level, pos);
		super.onRemove(state, level, pos, newState, moving);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.PASS;
		if (level.dimension().equals(ChamberOfWillManager.CHAMBER_OF_WILL)) {
			if (level.getBlockEntity(pos) instanceof WarpChairBlockEntity chair && chair.isPaired()
					&& chair.owner().filter(player.getUUID()::equals).isPresent()) {
				ChamberVisitService.beginPairedChairExit(serverPlayer, pos);
				return InteractionResult.CONSUME;
			}
			player.displayClientMessage(Component.translatable("message.hemomancy.warp_chair.only_paired_exits"), true);
			return InteractionResult.FAIL;
		}
		if (HemoCapabilityAccess.getPlayerDegreeNumber(serverPlayer) < 3) {
			player.displayClientMessage(Component.translatable("message.hemomancy.warp_chair.degree"), true);
			return InteractionResult.FAIL;
		}
		if (player.isShiftKeyDown()) {
			return ChamberVisitService.beginChairVisit(serverPlayer) ? InteractionResult.CONSUME : InteractionResult.FAIL;
		}
		var result = serverPlayer.startSleepInBed(pos);
		if (result.right().isPresent()) {
			ChamberVisitService.markPendingChairSleep(serverPlayer, pos);
			return InteractionResult.CONSUME;
		}
		result.left().ifPresent(problem -> {
			Component message = problem.getMessage();
			if (message != null) player.displayClientMessage(message, true);
		});
		return InteractionResult.FAIL;
	}

	@Override
	public boolean isBed(BlockState state, BlockGetter level, BlockPos pos, LivingEntity sleeper) {
		return true;
	}

	@Override
	public Direction getBedDirection(BlockState state, LevelReader level, BlockPos pos) {
		return state.getValue(FACING);
	}

	@Override
	public void setBedOccupied(BlockState state, Level level, BlockPos pos, LivingEntity sleeper, boolean occupied) {
		// Warp Chairs do not need a persisted occupied blockstate; the sleeper is authoritative.
	}

	@Override
	public Optional<RespawnPosAngle> getRespawnPosition(BlockState state, EntityType<?> type,
			LevelReader level, BlockPos pos, float orientation) {
		return BedBlock.findStandUpPosition(type, level, pos, state.getValue(FACING), orientation)
				.map(stand -> RespawnPosAngle.of(stand, pos));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override public RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
	@Override public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return WarpChairShapeRules.shape(state.getValue(FACING));
	}
	@Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WarpChairBlockEntity(pos, state); }

	public static boolean isPaired(BlockGetter level, BlockPos pos) {
		BlockEntity entity = level.getBlockEntity(pos);
		if (entity instanceof FillerBlockEntity filler && filler.getMainBlockPos() != null) entity = level.getBlockEntity(filler.getMainBlockPos());
		return entity instanceof WarpChairBlockEntity chair && chair.isPaired();
	}

	public static void placePaired(ServerLevel level, BlockPos pos, Direction facing, UUID owner) {
		BlockState existing = level.getBlockState(pos);
		if (!existing.is(BlockInit.warp_chair.get())) {
			level.setBlock(pos, BlockInit.warp_chair.get().defaultBlockState().setValue(FACING, facing), 3);
		} else if (existing.getValue(FACING) != facing) {
			level.setBlock(pos, existing.setValue(FACING, facing), 3);
		}
		if (!level.getBlockState(pos.above()).is(BlockInit.warp_chair_filler.get())) {
			((WarpChairBlock) BlockInit.warp_chair.get()).placeFillers(level, pos, level.getBlockState(pos));
		}
		if (level.getBlockEntity(pos) instanceof WarpChairBlockEntity chair) chair.setPaired(owner);
	}
}
