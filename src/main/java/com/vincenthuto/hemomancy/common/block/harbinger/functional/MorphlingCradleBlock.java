package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.MorphlingCradleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.*;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class MorphlingCradleBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<MorphlingCradleBlock> CODEC = simpleCodec(MorphlingCradleBlock::new);
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

	public MorphlingCradleBlock(Properties props) {
		super(props);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.NORTH)
				.setValue(FACE, AttachFace.FLOOR)
				.setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING, FACE, WATERLOGGED);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction clickedFace = context.getClickedFace();
		AttachFace attachFace;
		Direction horizontal;
		if (clickedFace.getAxis().isHorizontal()) {
			attachFace = AttachFace.WALL;
			horizontal = clickedFace;
		} else {
			attachFace = clickedFace == Direction.DOWN ? AttachFace.CEILING : AttachFace.FLOOR;
			horizontal = context.getHorizontalDirection().getOpposite();
		}
		BlockState state = this.defaultBlockState()
				.setValue(FACING, horizontal)
				.setValue(FACE, attachFace)
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
		return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
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
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos supportPos = getSupportPos(pos, state);
		Direction supportFace = getSupportFace(state);
		return level.getBlockState(supportPos).isFaceSturdy(level, supportPos, supportFace);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return state.canSurvive(level, pos) ? super.updateShape(state, direction, neighborState, level, pos, neighborPos)
				: WaterloggedBlockSupport.survivorOrWater(state);
	}

	private static BlockPos getSupportPos(BlockPos pos, BlockState state) {
		return switch (state.getValue(FACE)) {
			case CEILING -> pos.above();
			case FLOOR -> pos.below();
			case WALL -> pos.relative(state.getValue(FACING).getOpposite());
		};
	}

	private static Direction getSupportFace(BlockState state) {
		return switch (state.getValue(FACE)) {
			case CEILING -> Direction.DOWN;
			case FLOOR -> Direction.UP;
			case WALL -> state.getValue(FACING);
		};
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MorphlingCradleBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		if (level.isClientSide) return null;
		return createTickerHelper(type, BlockEntityInit.morphling_cradle.get(), MorphlingCradleBlockEntity::serverTick);
	}

	private InteractionResult handleUse(Level level, BlockPos pos, Player player, InteractionHand hand) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		BlockEntity be = level.getBlockEntity(pos);
		if (be instanceof MorphlingCradleBlockEntity cradle) {
			return cradle.handleInteraction(player, hand);
		}
		return InteractionResult.PASS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		return handleUse(level, pos, player, InteractionHand.MAIN_HAND);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		InteractionResult result = handleUse(level, pos, player, hand);
		return result == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			BlockEntity be = level.getBlockEntity(pos);
			if (be instanceof MorphlingCradleBlockEntity cradle) {
				var hosted = cradle.removeMorphlingForDrop();
				if (!hosted.isEmpty() && !level.isClientSide) {
					ItemEntity item = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.75, pos.getZ() + 0.5, hosted);
					level.addFreshEntity(item);
				}
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}
}
