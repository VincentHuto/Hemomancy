package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.shared.FillerBlock;
import com.vincenthuto.hemomancy.common.block.shared.HorizontalFacingRotationHelper;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.RingDoorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The ring door before a Saint's chamber: five wide and five tall in a hematic-iron frame, this block at the bottom
 * centre and fillers for the rest. Each completed Chapel lights a node. With every node lit a Silentium projects into
 * it and three rings of their blood open it; an Apotheos shatters it whenever they like, and the Saint then fights
 * with the power of every Chapel skipped. An opened door stays open: the frame stands and the doorway is clear.
 * The door's state lives in its {@link RingDoorBlockEntity}.
 */
public class RingDoorBlock extends Block implements EntityBlock, IMultiBlock {
	/** The way the door's front looks, toward the nave. */
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty OPEN = BlockStateProperties.OPEN;

	public RingDoorBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(OPEN, false));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, OPEN);
	}

	private static BlockPos[] fillerCells(Direction facing) {
		return HorizontalFacingRotationHelper.rotateNorthOffsets(RingDoorGeometry.FILLERS, facing);
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return RingDoorGeometry.FILLERS;
	}

	@Override
	public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
		for (BlockPos offset : fillerCells(mainState.getValue(FACING))) {
			BlockPos fillerPos = mainPos.offset(offset);
			level.setBlockAndUpdate(fillerPos, BlockInit.filler_block.get().defaultBlockState()
					.setValue(FillerBlock.WATERLOGGED, level.getFluidState(fillerPos).is(FluidTags.WATER)));
			if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) filler.setMainBlockPos(mainPos);
		}
	}

	@Override
	public void removeFillers(Level level, BlockPos mainPos) {
		// every turn, so the fillers go even when the state is already gone
		for (Direction facing : Direction.Plane.HORIZONTAL) {
			for (BlockPos offset : fillerCells(facing)) releaseFiller(level, mainPos.offset(offset), mainPos);
		}
	}

	/**
	 * Takes away one of this door's fillers without taking the door with it: a filler removed while it still names
	 * its controller destroys the controller. A neighbouring multiblock's filler is left alone.
	 */
	public static void releaseFiller(Level level, BlockPos fillerPos, BlockPos mainPos) {
		BlockState fillerState = level.getBlockState(fillerPos);
		if (!fillerState.is(BlockInit.filler_block.get())) return;
		if (level.getBlockEntity(fillerPos) instanceof FillerBlockEntity filler) {
			if (filler.getMainBlockPos() != null && !filler.getMainBlockPos().equals(mainPos)) return;
			filler.setMainBlockPos(null);
		}
		if (fillerState.getValue(FillerBlock.WATERLOGGED)) level.setBlockAndUpdate(fillerPos, Blocks.WATER.defaultBlockState());
		else level.removeBlock(fillerPos, false);
	}

	@Override
	public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
		// the facing is not known yet; getStateForPlacement checks the one it will use
		return canPlaceMultiBlock(level, mainPos, Direction.NORTH);
	}

	private static boolean canPlaceMultiBlock(Level level, BlockPos mainPos, Direction facing) {
		for (BlockPos offset : fillerCells(facing)) {
			BlockPos fillerPos = mainPos.offset(offset);
			if (!level.isInWorldBounds(fillerPos) || !level.getBlockState(fillerPos).canBeReplaced()) return false;
		}
		return true;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getHorizontalDirection().getOpposite();
		return canPlaceMultiBlock(context.getLevel(), context.getClickedPos(), facing)
				? defaultBlockState().setValue(FACING, facing) : null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (level.isClientSide) return;
		placeFillers(level, pos, state);
		if (level.getBlockEntity(pos) instanceof RingDoorBlockEntity door) door.markFillersPlaced();
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) removeFillers(level, pos);
			super.onRemove(state, level, pos, newState, isMoving);
		}
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (player instanceof ServerPlayer server && level.getBlockEntity(pos) instanceof RingDoorBlockEntity door) {
			door.use(server);
		}
		return InteractionResult.CONSUME;
	}

	// ── Shape ───────────────────────────────────────────────────────────────

	/** The door's outline in the cell at {@code offset} (world space) from this block. */
	@Override
	public VoxelShape fillerShape(BlockState controller, BlockPos offset) {
		return RingDoorGeometry.shape(controller.getValue(FACING), offset, controller.getValue(OPEN));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return fillerShape(state, BlockPos.ZERO);
	}

	/** Open, the threshold plate is only an outline: nothing to trip on in the doorway. */
	@Override
	protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return state.getValue(OPEN) ? Shapes.empty() : fillerShape(state, BlockPos.ZERO);
	}

	@Override
	protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	@Override
	protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	/** The baked frame; the slab and its rings are the block entity renderer's. */
	@Override
	protected RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	// ── Block entity ────────────────────────────────────────────────────────

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RingDoorBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide
				? HemoTickers.create(type, BlockEntityInit.ring_door.get(), RingDoorBlockEntity::clientTick)
				: HemoTickers.create(type, BlockEntityInit.ring_door.get(), RingDoorBlockEntity::serverTick);
	}
}
