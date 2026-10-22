package com.vincenthuto.hemomancy.common.block.harbinger.crafting;

import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.tile.harbinger.crafting.ScarStationBlockEntity;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import java.util.Map;

public class ScarStationBlock extends Block implements EntityBlock, IMultiBlock, SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	/** Filler offset: 1×2×1 — one filler block directly above the base. */
	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0)
	};
	/** Plinth, cabinet, pitched bed, backboard pillars and the belled cortex of tools/model_export/scar_station.py. */
	private static final Map<Direction, Map<BlockPos, VoxelShape>> CELLS = ModelShapes.cells(
			ModelShapes.footprint(FILLER_OFFSETS),
			new double[] {0.6, 0, 0.8, 15.4, 1.9, 15.6}, new double[] {1.5, 1.9, 2.5, 14.5, 11, 14.8},
			new double[] {1.5, 11, 2.5, 14.5, 13, 8}, new double[] {1.5, 11, 8, 14.5, 15.5, 13},
			new double[] {0.6, 11, 12.4, 15.4, 30.4, 14.8}, new double[] {4.2, 25, 7.8, 11.8, 31.8, 14.4});

	public ScarStationBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(WATERLOGGED, false));
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	private InteractionResult handleInteraction(Level worldIn, BlockPos pos, Player player) {
		if (!worldIn.isClientSide) {
			BlockEntity tile = worldIn.getBlockEntity(pos);

			if (tile instanceof ScarStationBlockEntity te) {
				if (te.numPlayersUsing < 2) {
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldIn, pos);
					((ServerPlayer) player).openMenu(te, pos);
					return InteractionResult.SUCCESS;
				}
			} else {
				return InteractionResult.PASS;
			}
			return InteractionResult.PASS;
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(worldIn, pos, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		return handleInteraction(worldIn, pos, player) == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return CELLS.get(state.getValue(FACING)).getOrDefault(BlockPos.ZERO, Shapes.block());
	}

	@Override
	public VoxelShape fillerShape(BlockState controller, BlockPos offset) {
		return CELLS.get(controller.getValue(FACING)).get(offset);
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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		// Need 1 block above the base (Y+1) for the filler
		if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
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

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				removeFillers(level, pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new ScarStationBlockEntity(p_153215_, p_153216_);
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

}
