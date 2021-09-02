package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import com.hutoslib.common.network.VanillaPacketDispatcher;
import com.vincenthuto.hemomancy.tile.BlockEntityChiselStation;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

public class BlockChiselStation extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.box(1.0999999999999996, 6.9, 1.0999999999999996, 14.9, 7.9, 14.899999999999999),
					Block.box(1.0999999999999996, 0.09999999999999998, 1.0999999999999996, 14.9, 1, 14.899999999999999),
					Block.box(12.5, 0, 12.7, 15, 8.2, 15.2), Block.box(12.5, 0, 1, 15, 8.2, 3.5),
					Block.box(1, 0, 1, 3.5, 8.2, 3.5), Block.box(1, 0, 12.7, 3.5, 8.2, 15.2),
					Block.box(2, 1, 2, 14, 8, 14))
			.reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public BlockChiselStation(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		if (!worldIn.isClientSide) {
			BlockEntity tile = worldIn.getBlockEntity(pos);

			if (tile instanceof BlockEntityChiselStation) {
				BlockEntityChiselStation te = (BlockEntityChiselStation) tile;
				te.sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldIn, pos);
				if (te.numPlayersUsing < 2) {
					NetworkHooks.openGui((ServerPlayer) player, (BlockEntityChiselStation) tile, pos);
					return InteractionResult.SUCCESS;
				}
			} else {
				return InteractionResult.PASS;

			}
		}
		return InteractionResult.FAIL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(state, world, pos, neighbor);
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
		return new BlockEntityChiselStation(p_153215_, p_153216_);
	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

}
