package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.tile.TileEntityVisceralRecaller;
import com.hutoslib.common.VanillaPacketDispatcher;

import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
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

public class BlockVisceralRecaller extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(1.5, 1.96, 3.25, 2.5, 2.96, 12.75),
			Block.makeCuboidShape(13.5, 6.9, 3, 14.5, 7.8, 12.75), Block.makeCuboidShape(13.5, 9, 3, 14.5, 9.8, 12.75),
			Block.makeCuboidShape(0.75, 0.064, 0.75, 3.25, 14.101, 3.25),
			Block.makeCuboidShape(0.5, 0, 0.5, 3.5, 1, 3.5), Block.makeCuboidShape(0.5, 13.5, 0.5, 3.5, 14.5, 3.5),
			Block.makeCuboidShape(12.75, 0.064, 0.75, 15.25, 14.101, 3.25),
			Block.makeCuboidShape(12.5, 0, 0.5, 15.5, 1, 3.5), Block.makeCuboidShape(12.5, 13.5, 0.5, 15.5, 14.5, 3.5),
			Block.makeCuboidShape(0.75, 0.064, 12.75, 3.25, 14.101, 15.25),
			Block.makeCuboidShape(0.5, 0, 12.5, 3.5, 1, 15.5), Block.makeCuboidShape(0.5, 13.5, 12.5, 3.5, 14.5, 15.5),
			Block.makeCuboidShape(12.75, 0.064, 12.75, 15.25, 14.101, 15.25),
			Block.makeCuboidShape(12.5, 0, 12.5, 15.5, 1, 15.5),
			Block.makeCuboidShape(12.5, 13.5, 12.5, 15.5, 14.5, 15.5),
			Block.makeCuboidShape(13.5, 4, 3, 14.5, 5, 12.75), Block.makeCuboidShape(13.5, 1.96, 3, 14.5, 2.96, 12.75),
			Block.makeCuboidShape(1.5, 4, 3.25, 2.5, 5, 12.75), Block.makeCuboidShape(1.5, 6.9, 3.25, 2.5, 7.8, 12.75),
			Block.makeCuboidShape(1.5, 9, 3.25, 2.5, 9.8, 12.75),
			Block.makeCuboidShape(3.25, 6.9, 1.5, 12.75, 7.8, 2.5),
			Block.makeCuboidShape(3.25, 9, 1.5, 12.75, 9.8, 2.5), Block.makeCuboidShape(3.25, 4, 1.5, 12.75, 5, 2.5),
			Block.makeCuboidShape(3.25, 1.96, 1.5, 12.75, 2.96, 2.5),
			Block.makeCuboidShape(3.25, 1.96, 13.5, 12.75, 2.96, 14.5),
			Block.makeCuboidShape(3.25, 4, 13.5, 12.75, 5, 14.5),
			Block.makeCuboidShape(3.25, 6.9, 13.5, 12.75, 7.8, 14.5),
			Block.makeCuboidShape(3.25, 9, 13.5, 12.75, 9.8, 14.5), Block.makeCuboidShape(2, 7, 2, 14, 11.002, 14),
			Block.makeCuboidShape(2.75, 10.752, 2.75, 13.25, 14.75, 13.25),
			Block.makeCuboidShape(1, 12.25, 1, 15, 14.25, 15), Block.makeCuboidShape(-1, 11.752, -1, 2, 14.754, 2),
			Block.makeCuboidShape(-1, 13.351, 2, 1, 15.351, 14), Block.makeCuboidShape(-1, 11.752, 14, 2, 14.754, 17),
			Block.makeCuboidShape(2, 13.351, 15, 14, 15.351, 17), Block.makeCuboidShape(14, 11.752, 14, 17, 14.754, 17),
			Block.makeCuboidShape(15, 13.351, 2, 17, 15.351, 14), Block.makeCuboidShape(14, 11.752, -1, 17, 14.754, 2),
			Block.makeCuboidShape(2, 13.351, -1, 14, 15.351, 1), Block.makeCuboidShape(0.5, 9.5, 0.5, 3.5, 12.5, 3.5),
			Block.makeCuboidShape(0.5, 9.5, 12.5, 3.5, 12.5, 15.5),
			Block.makeCuboidShape(12.5, 9.5, 12.5, 15.5, 12.5, 15.5),
			Block.makeCuboidShape(12.5, 9.5, 0.5, 15.5, 12.5, 3.5),
			Block.makeCuboidShape(12.5, 1.5, 0.5, 15.5, 4.5, 3.5), Block.makeCuboidShape(0.5, 1.5, 0.5, 3.5, 4.5, 3.5),
			Block.makeCuboidShape(0.5, 1.5, 12.5, 3.5, 4.5, 15.5),
			Block.makeCuboidShape(12.5, 1.5, 12.5, 15.5, 4.5, 15.5)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockVisceralRecaller(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (!worldIn.isRemote) {
			TileEntity tile = worldIn.getTileEntity(pos);

			if (tile instanceof TileEntityVisceralRecaller) {
				TileEntityVisceralRecaller te = (TileEntityVisceralRecaller) tile;
				te.sendUpdates();
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldIn, pos);
				NetworkHooks.openGui((ServerPlayerEntity) player, (TileEntityVisceralRecaller) tile, pos);
			}
		}
		return ActionResultType.SUCCESS;

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
	public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		super.onBlockClicked(state, worldIn, pos, player);
	}

	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new TileEntityVisceralRecaller();
	}

}
