package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.tile.BlockEntityVisceralRecaller;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fmllegacy.network.NetworkHooks;

public class BlockVisceralRecaller extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(1.5, 1.96, 3.25, 2.5, 2.96, 12.75),
			Block.box(13.5, 6.9, 3, 14.5, 7.8, 12.75), Block.box(13.5, 9, 3, 14.5, 9.8, 12.75),
			Block.box(0.75, 0.064, 0.75, 3.25, 14.101, 3.25), Block.box(0.5, 0, 0.5, 3.5, 1, 3.5),
			Block.box(0.5, 13.5, 0.5, 3.5, 14.5, 3.5), Block.box(12.75, 0.064, 0.75, 15.25, 14.101, 3.25),
			Block.box(12.5, 0, 0.5, 15.5, 1, 3.5), Block.box(12.5, 13.5, 0.5, 15.5, 14.5, 3.5),
			Block.box(0.75, 0.064, 12.75, 3.25, 14.101, 15.25), Block.box(0.5, 0, 12.5, 3.5, 1, 15.5),
			Block.box(0.5, 13.5, 12.5, 3.5, 14.5, 15.5), Block.box(12.75, 0.064, 12.75, 15.25, 14.101, 15.25),
			Block.box(12.5, 0, 12.5, 15.5, 1, 15.5), Block.box(12.5, 13.5, 12.5, 15.5, 14.5, 15.5),
			Block.box(13.5, 4, 3, 14.5, 5, 12.75), Block.box(13.5, 1.96, 3, 14.5, 2.96, 12.75),
			Block.box(1.5, 4, 3.25, 2.5, 5, 12.75), Block.box(1.5, 6.9, 3.25, 2.5, 7.8, 12.75),
			Block.box(1.5, 9, 3.25, 2.5, 9.8, 12.75), Block.box(3.25, 6.9, 1.5, 12.75, 7.8, 2.5),
			Block.box(3.25, 9, 1.5, 12.75, 9.8, 2.5), Block.box(3.25, 4, 1.5, 12.75, 5, 2.5),
			Block.box(3.25, 1.96, 1.5, 12.75, 2.96, 2.5), Block.box(3.25, 1.96, 13.5, 12.75, 2.96, 14.5),
			Block.box(3.25, 4, 13.5, 12.75, 5, 14.5), Block.box(3.25, 6.9, 13.5, 12.75, 7.8, 14.5),
			Block.box(3.25, 9, 13.5, 12.75, 9.8, 14.5), Block.box(2, 7, 2, 14, 11.002, 14),
			Block.box(2.75, 10.752, 2.75, 13.25, 14.75, 13.25), Block.box(1, 12.25, 1, 15, 14.25, 15),
			Block.box(-1, 11.752, -1, 2, 14.754, 2), Block.box(-1, 13.351, 2, 1, 15.351, 14),
			Block.box(-1, 11.752, 14, 2, 14.754, 17), Block.box(2, 13.351, 15, 14, 15.351, 17),
			Block.box(14, 11.752, 14, 17, 14.754, 17), Block.box(15, 13.351, 2, 17, 15.351, 14),
			Block.box(14, 11.752, -1, 17, 14.754, 2), Block.box(2, 13.351, -1, 14, 15.351, 1),
			Block.box(0.5, 9.5, 0.5, 3.5, 12.5, 3.5), Block.box(0.5, 9.5, 12.5, 3.5, 12.5, 15.5),
			Block.box(12.5, 9.5, 12.5, 15.5, 12.5, 15.5), Block.box(12.5, 9.5, 0.5, 15.5, 12.5, 3.5),
			Block.box(12.5, 1.5, 0.5, 15.5, 4.5, 3.5), Block.box(0.5, 1.5, 0.5, 3.5, 4.5, 3.5),
			Block.box(0.5, 1.5, 12.5, 3.5, 4.5, 15.5), Block.box(12.5, 1.5, 12.5, 15.5, 4.5, 15.5)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public BlockVisceralRecaller(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		if (!worldIn.isClientSide) {
			BlockEntity tile = worldIn.getBlockEntity(pos);

			if (tile instanceof BlockEntityVisceralRecaller) {
				BlockEntityVisceralRecaller te = (BlockEntityVisceralRecaller) tile;
				te.sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldIn, pos);
				NetworkHooks.openGui((ServerPlayer) player, (BlockEntityVisceralRecaller) tile, pos);
			}
		}
		return InteractionResult.SUCCESS;

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
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
		return new BlockEntityVisceralRecaller(p_153215_, p_153216_);
	}

}
