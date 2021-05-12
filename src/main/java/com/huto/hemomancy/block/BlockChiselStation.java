package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.tile.TileEntityChiselStation;
import com.hutoslib.common.VanillaPacketDispatcher;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer.Builder;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockChiselStation extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.makeCuboidShape(1.0999999999999996, 6.9, 1.0999999999999996, 14.9, 7.9, 14.899999999999999),
					Block.makeCuboidShape(1.0999999999999996, 0.09999999999999998, 1.0999999999999996, 14.9, 1,
							14.899999999999999),
					Block.makeCuboidShape(12.5, 0, 12.7, 15, 8.2, 15.2),
					Block.makeCuboidShape(12.5, 0, 1, 15, 8.2, 3.5), Block.makeCuboidShape(1, 0, 1, 3.5, 8.2, 3.5),
					Block.makeCuboidShape(1, 0, 12.7, 3.5, 8.2, 15.2), Block.makeCuboidShape(2, 1, 2, 14, 8, 14))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockChiselStation(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (!worldIn.isRemote) {
			TileEntity tile = worldIn.getTileEntity(pos);

			if (tile instanceof TileEntityChiselStation) {
				TileEntityChiselStation te = (TileEntityChiselStation) tile;
				te.sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldIn, pos);
				if (te.numPlayersUsing < 2) {
					NetworkHooks.openGui((ServerPlayerEntity) player, (TileEntityChiselStation) tile, pos);
					return ActionResultType.SUCCESS;
				}
			} else {
				return ActionResultType.PASS;

			}
		}
		return ActionResultType.FAIL;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, IWorldReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileEntityChiselStation();
	}

	@Override
	public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		super.onBlockClicked(state, worldIn, pos, player);
	}

}
