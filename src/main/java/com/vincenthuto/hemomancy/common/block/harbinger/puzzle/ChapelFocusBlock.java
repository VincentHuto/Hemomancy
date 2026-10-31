package com.vincenthuto.hemomancy.common.block.harbinger.puzzle;

import com.vincenthuto.hemomancy.common.block.shared.ModelShapes;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.harbinger.puzzle.ChapelFocusBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Map;

import javax.annotation.Nullable;

/**
 * A Chapel's focus: touched by a D8 player it begins that Chapel's trial; lit once the trial is complete. Its facing is
 * authored north in the template and turns with the placed piece, which is how the block entity knows which way to
 * turn its wing's marks. Models from tools/model_export/chapel_focus.py.
 */
public class ChapelFocusBlock extends Block implements EntityBlock {
	public static final BooleanProperty LIT = BooleanProperty.create("lit");
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final Map<Direction, VoxelShape> SHAPES = ModelShapes.horizontal(
			ModelShapes.of(new double[] {2, 0, 2, 14, 1.5, 14}, new double[] {2.75, 1.5, 2.75, 13.25, 2.5, 13.25},
					new double[] {3.5, 2.5, 4.25, 12.5, 9, 11.5}, new double[] {2.75, 9, 3.25, 13.25, 10.5, 12.5},
					new double[] {5, 10.5, 5, 11, 13, 11}));

	public ChapelFocusBlock(Properties properties) {
		super(properties);
		registerDefaultState(stateDefinition.any().setValue(LIT, false).setValue(FACING, Direction.NORTH));
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LIT, FACING);
	}

	@Override
	protected BlockState rotate(BlockState state, Rotation rotation) {
		return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
	}

	@Override
	protected BlockState mirror(BlockState state, Mirror mirror) {
		return state.rotate(mirror.getRotation(state.getValue(FACING)));
	}

	@Override
	protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPES.get(state.getValue(FACING));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ChapelFocusBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide ? null : HemoTickers.create(type, BlockEntityInit.chapel_focus.get(),
				ChapelFocusBlockEntity::serverTick);
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (level.isClientSide) return InteractionResult.SUCCESS;
		if (level.getBlockEntity(pos) instanceof ChapelFocusBlockEntity focus && player instanceof ServerPlayer server
				&& focus.phase() == ChapelFocusBlockEntity.Phase.IDLE) {
			focus.touch(server);
		}
		return InteractionResult.CONSUME;
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
			Player player, InteractionHand hand, BlockHitResult hit) {
		if (level.isClientSide) return ItemInteractionResult.SUCCESS;
		if (!(level.getBlockEntity(pos) instanceof ChapelFocusBlockEntity focus)) return ItemInteractionResult.FAIL;
		if (focus.phase() == ChapelFocusBlockEntity.Phase.IDLE) {
			if (player instanceof ServerPlayer server) focus.touch(server);
			return ItemInteractionResult.CONSUME;
		}
		focus.offer(stack, player);
		return ItemInteractionResult.CONSUME;
	}
}
