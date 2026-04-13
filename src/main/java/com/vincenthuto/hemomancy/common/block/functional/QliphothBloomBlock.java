package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.block.IMultiBlock;
import com.vincenthuto.hemomancy.common.rite.CardinalRiteEvents;
import com.vincenthuto.hemomancy.common.tile.functional.QliphothBloomBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * The Qliphoth Bloom block — a 1×1×8 multi-block structure placed by the
 * "Bloom of the Qliphoth" cardinal rite. The base block sits at the ritual
 * center and 7 filler blocks extend upward. Rendering is handled entirely
 * by the block entity renderer (the tree geometry previously drawn as a
 * world render). Breaking the block (or any filler) removes the bloom.
 */
public class QliphothBloomBlock extends BaseEntityBlock implements IMultiBlock {

	/** Filler offsets: Y+1 through Y+7, giving a total height of 8 blocks. */
	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0),
			new BlockPos(0, 2, 0),
			new BlockPos(0, 3, 0),
			new BlockPos(0, 4, 0),
			new BlockPos(0, 5, 0),
			new BlockPos(0, 6, 0),
			new BlockPos(0, 7, 0)
	};

	/** Full-block collision shape for the base block. */
	private static final VoxelShape SHAPE = Shapes.block();

	public QliphothBloomBlock(Properties properties) {
		super(properties);
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		// Need 7 blocks above the base (Y+1 through Y+7)
		if (pos.getY() + 7 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState();
		}
		return null; // Prevents placement if there's not enough room
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
		}
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new QliphothBloomBlockEntity(pos, state);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		// Invisible model — the BER draws the tree geometry
		return RenderShape.INVISIBLE;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				// Remove filler blocks
				removeFillers(level, pos);
				// Remove the bloom from SavedData so effects and client sync update
				CardinalRiteEvents.removeBloomAt(level, pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		// No tick needed — effects are handled by QliphothBloomEvents via SavedData
		return null;
	}
}
