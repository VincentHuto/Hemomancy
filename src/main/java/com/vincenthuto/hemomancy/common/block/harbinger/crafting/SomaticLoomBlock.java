package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.block.shared.HorizontalFacingRotationHelper;
import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.SomaticLoomBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SomaticLoomBlock extends Block implements EntityBlock, IMultiBlock, SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0),	new BlockPos(0, 0, -1),	new BlockPos(0, 0, 1)
	};
	private static final VoxelShape SHAPE_N = Shapes.block();

	@Nullable
	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> candidate, BlockEntityType<E> desired, BlockEntityTicker<? super E> ticker) {
		return desired == candidate ? (BlockEntityTicker<A>) ticker : null;
	}

	public SomaticLoomBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH)
				.setValue(WATERLOGGED, false));
	}

	private static BlockPos[] rotatedFillerOffsets(Direction facing) {
		return HorizontalFacingRotationHelper.rotateNorthOffsets(FILLER_OFFSETS, facing);
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
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	@Override
	public void placeFillers(Level level, BlockPos mainPos, BlockState mainState) {
		Direction facing = mainState.hasProperty(FACING) ? mainState.getValue(FACING) : Direction.SOUTH;
		for (BlockPos offset : rotatedFillerOffsets(facing)) {
			BlockPos fillerPos = mainPos.offset(offset);
			level.setBlockAndUpdate(fillerPos,
					com.vincenthuto.hemomancy.common.init.BlockInit.filler_block.get().defaultBlockState()
							.setValue(com.vincenthuto.hemomancy.common.block.shared.FillerBlock.WATERLOGGED,
									level.getFluidState(fillerPos).is(net.minecraft.tags.FluidTags.WATER)));
			BlockEntity be = level.getBlockEntity(fillerPos);
			if (be instanceof com.vincenthuto.hemomancy.common.tile.shared.FillerBlockEntity filler) {
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
			BlockPos fillerPos = mainPos.offset(offset);
			BlockState fillerState = level.getBlockState(fillerPos);
			if (fillerState.is(com.vincenthuto.hemomancy.common.init.BlockInit.filler_block.get())) {
				if (fillerState.getValue(com.vincenthuto.hemomancy.common.block.shared.FillerBlock.WATERLOGGED)) {
					level.setBlockAndUpdate(fillerPos, net.minecraft.world.level.block.Blocks.WATER.defaultBlockState());
				} else {
					level.removeBlock(fillerPos, false);
				}
			}
		}
	}

	@Override
	public boolean canPlaceMultiBlock(Level level, BlockPos mainPos) {
		Direction facing = level.getBlockState(mainPos).hasProperty(FACING)
				? level.getBlockState(mainPos).getValue(FACING)
				: Direction.SOUTH;
		return canPlaceMultiBlock(level, mainPos, facing);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
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
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		Direction facing = context.getHorizontalDirection().getOpposite();
		if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos, facing)) {
			return this.defaultBlockState().setValue(FACING, facing)
					.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
		}
		return null;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) {
			return createTickerHelper(type, BlockEntityInit.somatic_loom.get(), SomaticLoomBlockEntity::clientTick);
		}
		return createTickerHelper(type, BlockEntityInit.somatic_loom.get(), SomaticLoomBlockEntity::serverTick);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SomaticLoomBlockEntity(pos, state);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity be = world.getBlockEntity(pos);
		return be != null && be.triggerEvent(id, param);
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (level.getBlockEntity(pos) instanceof SomaticLoomBlockEntity te) {
				te.dropContents();
			}
			if (!level.isClientSide) {
				Direction facing = state.hasProperty(FACING) ? state.getValue(FACING) : Direction.SOUTH;
				removeFillers(level, pos, facing);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	private InteractionResult handleEmptyHandUse(Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof SomaticLoomBlockEntity te)) return InteractionResult.PASS;

		if (player.isShiftKeyDown()) {
			if (te.isWeavingOrbs()) {
				te.cancelRitual(player);
				return InteractionResult.SUCCESS;
			}
			if (te.removeItem(player, false) || te.removeItem(player, true)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		if (te.isWeavingOrbs()) {
			te.provideTendencyFeedback(player);
			return InteractionResult.SUCCESS;
		}

		te.refreshRecipe();
		te.provideTendencyFeedback(player);
		return InteractionResult.SUCCESS;
	}

	private InteractionResult handleItemUse(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			InteractionHand handIn) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof SomaticLoomBlockEntity te)) return InteractionResult.PASS;
		if (isProjectionTool(stack)) return InteractionResult.PASS;
		if (te.isWeavingOrbs()) return InteractionResult.PASS;
		return te.addItem(player, stack, handIn) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	private static boolean isProjectionTool(ItemStack stack) {
		return stack.getItem() instanceof BloodProjectionItem || stack.getItem() instanceof LivingStaffItem;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleEmptyHandUse(worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		if (isProjectionTool(stack)) {
			return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
		}
		return handleItemUse(worldIn, pos, player, stack, handIn) == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}
}
