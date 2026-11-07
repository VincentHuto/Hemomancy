package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.mojang.serialization.MapCodec;
import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.scars.PacketOpenScarsInv;
import com.vincenthuto.hemomancy.common.tile.harbinger.functional.ScarletVanityBlockEntity;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Scarlet Vanity: a desk below and its tall mirror above, two blocks that stand and fall together. The lower half
 * holds the block entity and drops the item; the upper half is the mirror's frame, and its glass is drawn by
 * {@code ScarletVanityRenderer}. Either half opens the equipment menu.
 */
public class ScarletVanityBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	public static final MapCodec<ScarletVanityBlock> CODEC = simpleCodec(ScarletVanityBlock::new);
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	// Traced from the north-facing models (pixels): the desk with its backsplash, and the mirror's frame.
	private static final Map<Direction, VoxelShape> LOWER = ModelShapes.horizontal(ModelShapes.of(
			new double[] { 0, 0, 1, 16, 14, 15 }, new double[] { 1, 14, 13.25, 15, 16, 15.5 }));
	private static final Map<Direction, VoxelShape> UPPER = ModelShapes.horizontal(ModelShapes.of(
			new double[] { 1, 0, 13.25, 15, 4.7, 15.25 }, new double[] { 2, 4.7, 13.25, 14, 15.5, 15.25 }));

	public ScarletVanityBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH)
				.setValue(HALF, DoubleBlockHalf.LOWER).setValue(WATERLOGGED, false));
	}

	@Override
	protected MapCodec<? extends BaseEntityBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> pBuilder) {
		pBuilder.add(FACING, HALF, WATERLOGGED);
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return (state.getValue(HALF) == DoubleBlockHalf.LOWER ? LOWER : UPPER).get(state.getValue(FACING));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = context.getLevel();
		if (pos.getY() >= level.getMaxBuildHeight() - 1 || !level.getBlockState(pos.above()).canBeReplaced(context)) {
			return null;
		}
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(HALF, DoubleBlockHalf.LOWER)
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		BlockPos above = pos.above();
		level.setBlock(above, state.setValue(HALF, DoubleBlockHalf.UPPER)
				.setValue(WATERLOGGED, level.getFluidState(above).getType() == Fluids.WATER),
				Block.UPDATE_ALL);
	}

	@Override
	public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
		// Broken from the mirror without a drop due, the desk goes with it and drops nothing either.
		if (!level.isClientSide && state.getValue(HALF) == DoubleBlockHalf.UPPER
				&& (player.isCreative() || !player.hasCorrectToolForDrops(state))) {
			BlockPos below = pos.below();
			BlockState desk = level.getBlockState(below);
			if (desk.is(this) && desk.getValue(HALF) == DoubleBlockHalf.LOWER) {
				level.setBlock(below, WaterloggedBlockSupport.survivorOrWater(desk), Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
				level.levelEvent(player, 2001, below, Block.getId(desk));
			}
		}
		return super.playerWillDestroy(level, pos, state, player);
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return state.getValue(HALF) == DoubleBlockHalf.LOWER ? new ScarletVanityBlockEntity(pos, state) : null;
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	private InteractionResult handleUse(Level worldIn, BlockPos pos, BlockState state, Player player) {
		if (player.isShiftKeyDown()) {
			return InteractionResult.PASS;
		}
		if (worldIn.isClientSide) {
			BlockPos desk = state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
			PacketHandler.sendToServer(new PacketOpenScarsInv(desk));
		}
		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleUse(worldIn, pos, state, player);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		InteractionResult interactionResult = handleUse(worldIn, pos, state, player);
		return interactionResult == InteractionResult.PASS ? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		// The halves stand together: when one goes, so does the other. Air, so the level destroys this half properly,
		// dropping the desk's loot and leaving its water.
		DoubleBlockHalf half = state.getValue(HALF);
		if (direction.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER) == (direction == Direction.UP)
				&& !(neighborState.is(this) && neighborState.getValue(HALF) != half)) {
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}
}
