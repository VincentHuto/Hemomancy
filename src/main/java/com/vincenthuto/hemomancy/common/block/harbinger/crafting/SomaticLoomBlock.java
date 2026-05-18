package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.crafting.SomaticLoomBlockEntity;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class SomaticLoomBlock extends Block implements EntityBlock, IMultiBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	/** Filler offsets: 1×2×1 — one filler block directly above the base. */
	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0),new BlockPos(0, 2, 0)
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
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
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
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_,
			CollisionContext p_60558_) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		// Need 1 block above the base (Y+1) for the filler
		if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}
		return null; // Prevents placement if there's not enough room
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) {
			return createTickerHelper(type, BlockEntityInit.somatic_loom.get(),
					SomaticLoomBlockEntity::clientTick);
		} else {
			return createTickerHelper(type, BlockEntityInit.somatic_loom.get(),
					SomaticLoomBlockEntity::serverTick);
		}
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
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new SomaticLoomBlockEntity(p_153215_, p_153216_);
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
				removeFillers(level, pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	/**
	 * All player interaction funnels through here.
	 *
	 * <p><b>With an item in hand:</b> delegates to {@code te.addItem()}.
	 * Enzymes, blood containers, memories, and catalysts are all handled there.</p>
	 *
	 * <p><b>Empty hand (not crouching):</b> if a ritual is active, does nothing
	 * (just stay near the block). Otherwise, freshly checks the recipe state and
	 * either starts the ritual or shows feedback about what's missing.</p>
	 *
	 * <p><b>Empty hand (crouching):</b> during a ritual, cancels it.
	 * Otherwise, removes items from the block (catalyst first, then memory).</p>
	 */
	private InteractionResult handleEmptyHandUse(Level worldIn, BlockPos pos, Player player) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof SomaticLoomBlockEntity te)) return InteractionResult.PASS;

		// ---- Empty hand + crouching ----
		if (player.isCrouching()) {
			if (te.isCrafting()) {
				te.cancelRitual(player);
				return InteractionResult.SUCCESS;
			}
			// Remove catalyst first, then memory
			if (te.removeItem(player, false) || te.removeItem(player, true)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		// ---- Empty hand + standing ----
		if (te.isCrafting()) {
			// Nothing to do — just stay near the block
			return InteractionResult.PASS;
		}

		// Freshly evaluate recipe before deciding
		te.refreshRecipe();
		if (te.hasValidRecipe()) {
			te.startRitual(player);
			return InteractionResult.SUCCESS;
		}

		te.provideTendencyFeedback(player);
		return InteractionResult.SUCCESS;
	}

	private InteractionResult handleItemUse(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			InteractionHand handIn) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof SomaticLoomBlockEntity te)) return InteractionResult.PASS;
		if (te.isCrafting()) return InteractionResult.PASS;
		return te.addItem(player, stack, handIn) ? InteractionResult.SUCCESS : InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleEmptyHandUse(worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		return handleItemUse(worldIn, pos, player, stack, handIn) == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

}
