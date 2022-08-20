package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.tile.VisceralRecallerBlockEntity;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class VisceralRecallerBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.box(1.5, 5, 3.25, 2.5, 6, 12.75), Block.box(0.75, 0.1, 0.75, 3.25, 14.1, 3.25),
					Block.box(2, 10, 2, 14, 14, 14), Block.box(0.5, 0, 0.5, 3.5, 2, 3.5),
					Block.box(12.75, 0.1, 0.75, 15.25, 14.1, 3.25), Block.box(12.5, 0, 0.5, 15.5, 2, 3.5),
					Block.box(0.75, 0.1, 12.75, 3.25, 14.1, 15.25), Block.box(0.5, 0, 12.5, 3.5, 2, 15.5),
					Block.box(12.75, 0.1, 12.75, 15.25, 14.1, 15.25), Block.box(12.5, 0, 12.5, 15.5, 2, 15.5),
					Block.box(13.5, 7, 3, 14.5, 8, 12.75), Block.box(13.5, 5, 3, 14.5, 6, 12.75),
					Block.box(1.5, 7, 3.25, 2.5, 8, 12.75), Block.box(3.25, 7, 1.5, 12.75, 8, 2.5),
					Block.box(3.25, 5, 1.5, 12.75, 6, 2.5), Block.box(3.25, 5, 13.5, 12.75, 6, 14.5),
					Block.box(3.25, 7, 13.5, 12.75, 8, 14.5), Block.box(2.75, 12.75, 2.75, 13.25, 14.75, 13.25),
					Block.box(1, 12.25, 1, 15, 14.25, 15), Block.box(1, 13.35, -1, 15, 15.35, 1),
					Block.box(1, 13.35, 15, 15, 15.35, 17), Block.box(15, 13.35, 1, 17, 15.35, 15),
					Block.box(-1, 13.35, 1, 1, 15.35, 15), Block.box(12.5, 8.5, 12.5, 15.5, 15.5, 15.5),
					Block.box(0.5, 8.5, 12.5, 3.5, 15.5, 15.5), Block.box(0.5, 8.5, 0.5, 3.5, 15.5, 3.5),
					Block.box(12.5, 8.5, 0.5, 15.5, 15.5, 3.5))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public VisceralRecallerBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		ItemStack stack = player.getItemInHand(handIn);
		if (!worldIn.isClientSide) {
			BlockEntity tile = worldIn.getBlockEntity(pos);
			if (tile instanceof VisceralRecallerBlockEntity te) {
				if (!stack.isEmpty()) {
					boolean resultt = te.addItem(player, stack, handIn);
					te.sendUpdates();
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(te);
					return resultt ? InteractionResult.SUCCESS : InteractionResult.PASS;
				} else if (player.isCrouching()) {
					boolean resultt = te.addItem(player, stack, handIn);
					te.sendUpdates();
					VanillaPacketDispatcher.dispatchTEToNearbyPlayers(te);
					return resultt ? InteractionResult.SUCCESS : InteractionResult.PASS;
				}
	
			}
		}
		return InteractionResult.SUCCESS;

	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) {
			return createTickerHelper(type, BlockEntityInit.visceral_artificial_recaller.get(),
					VisceralRecallerBlockEntity::clientTick);
		} else {
			return createTickerHelper(type, BlockEntityInit.visceral_artificial_recaller.get(),
					VisceralRecallerBlockEntity::serverTick);
		}
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity tileentity = world.getBlockEntity(pos);
		return tileentity != null && tileentity.triggerEvent(id, param);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> candidate, BlockEntityType<E> desired, BlockEntityTicker<? super E> ticker) {
		return desired == candidate ? (BlockEntityTicker<A>) ticker : null;
	}

	@Override
	public VoxelShape getShape(BlockState p_60555_, BlockGetter p_60556_, BlockPos p_60557_,
			CollisionContext p_60558_) {
		return SHAPE_N;
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
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
		builder.add(FACING);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new VisceralRecallerBlockEntity(p_153215_, p_153216_);
	}

}
