package com.vincenthuto.hemomancy.common.block;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.capability.player.visceral.EnumOrgan;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.VisceralMirrorBlockEntity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * The Visceral Mirror — a ritualistic block that allows the player to gaze into
 * their own reflection, "reach" through the mirror, and extract organs for
 * sanguine modification. Requires initiatory degree 3+.
 *
 * <p>Interaction flow:</p>
 * <ul>
 *   <li><b>Empty hand + standing:</b> opens organ selection cycle / starts ritual</li>
 *   <li><b>Empty hand + crouching:</b> cancels active ritual</li>
 * </ul>
 */
public class VisceralMirrorBlock extends Block implements EntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	// A tall mirror-like shape (2 blocks tall appearance in 1 block)
	private static final VoxelShape SHAPE = Shapes.or(
			Block.box(1, 0, 1, 15, 2, 15),    // Base pedestal
			Block.box(3, 2, 3, 13, 16, 13)     // Mirror frame
	);

	@Nullable
	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> candidate, BlockEntityType<E> desired, BlockEntityTicker<? super E> ticker) {
		return desired == candidate ? (BlockEntityTicker<A>) ticker : null;
	}

	public VisceralMirrorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext ctx) {
		return SHAPE;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) {
			return createTickerHelper(type, BlockEntityInit.visceral_mirror.get(),
					VisceralMirrorBlockEntity::clientTick);
		} else {
			return createTickerHelper(type, BlockEntityInit.visceral_mirror.get(),
					VisceralMirrorBlockEntity::serverTick);
		}
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new VisceralMirrorBlockEntity(pos, state);
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

	/**
	 * Player interaction with the Visceral Mirror.
	 *
	 * <p><b>Crouching + empty hand:</b> cancel active ritual.</p>
	 * <p><b>Standing + empty hand:</b> cycle through organs and start extraction.</p>
	 */
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult result) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity tile = worldIn.getBlockEntity(pos);
		if (!(tile instanceof VisceralMirrorBlockEntity te)) return InteractionResult.PASS;

		// ---- Crouching = cancel ----
		if (player.isCrouching()) {
			if (te.getPhase() != VisceralMirrorBlockEntity.RitualPhase.IDLE) {
				te.cancelRitual(player);
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		// ---- Standing + empty hand = cycle organs and start ----
		if (!player.getItemInHand(handIn).isEmpty()) {
			return InteractionResult.PASS;
		}

		if (te.getPhase() != VisceralMirrorBlockEntity.RitualPhase.IDLE) {
			// Ritual already in progress
			player.displayClientMessage(
					Component.literal("The mirror ripples... the ritual is in progress.")
							.withStyle(ChatFormatting.DARK_PURPLE), true);
			return InteractionResult.SUCCESS;
		}

		// Cycle through organs: use the player's tick count to pick an organ
		// In practice, each click advances to the next organ
		EnumOrgan[] organs = EnumOrgan.values();
		int index = (int) ((player.tickCount / 5) % organs.length);
		EnumOrgan selectedOrgan = organs[index];

		if (te.startRitual(player, selectedOrgan)) {
			return InteractionResult.SUCCESS;
		}

		// Show which organ would be selected
		player.displayClientMessage(
				Component.literal("The mirror reveals your " + selectedOrgan.getName().toLowerCase()
						+ "... [Click again to extract]").withStyle(ChatFormatting.DARK_PURPLE),
				true);
		return InteractionResult.SUCCESS;
	}
}
