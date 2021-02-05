package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.tile.TileEntityMorphlingIncubator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.entity.player.PlayerEntity;
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

public class BlockMorphlingIncubator extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(7, 0, 7, 9, 3, 9),
			Block.makeCuboidShape(6, 2, 6, 10, 4, 10), Block.makeCuboidShape(10.75, 3, 5, 11.75, 4, 11),
			Block.makeCuboidShape(4.25, 3, 5, 5.25, 4, 11), Block.makeCuboidShape(5, 3, 10.75, 11, 4, 11.75),
			Block.makeCuboidShape(5, 3, 4.25, 11, 4, 5.25), Block.makeCuboidShape(4, 4, 4, 12, 5, 12),
			Block.makeCuboidShape(5, 5, 5, 11, 6, 11), Block.makeCuboidShape(11, 5, 11, 12, 13, 12),
			Block.makeCuboidShape(11, 5, 4, 12, 13, 5), Block.makeCuboidShape(4, 5, 4, 5, 13, 5),
			Block.makeCuboidShape(4, 5, 11, 5, 13, 12), Block.makeCuboidShape(4.5, 5, 4.5, 5, 12.5, 11.5),
			Block.makeCuboidShape(6, 6, 6, 10, 10, 10), Block.makeCuboidShape(11, 5, 4.5, 11.5, 12.5, 11.5),
			Block.makeCuboidShape(4.5, 5, 4.75, 11.5, 12.5, 5), Block.makeCuboidShape(4.5, 5, 11, 11.5, 12.5, 11.5),
			Block.makeCuboidShape(5, 11, 5, 11, 12, 11), Block.makeCuboidShape(6, 0, 6, 10, 0.5, 10))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockMorphlingIncubator(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		/*
		 * if (!worldIn.isRemote) { TileEntity tile = worldIn.getTileEntity(pos);
		 * 
		 * if (tile instanceof TileEntityChiselStation) { TileEntityChiselStation te =
		 * (TileEntityChiselStation) tile; te.sendUpdates();
		 * VanillaPacketDispatcher.dispatchTEToNearbyPlayers(worldIn, pos); if
		 * (te.numPlayersUsing < 2) { NetworkHooks.openGui((ServerPlayerEntity) player,
		 * (TileEntityChiselStation) tile, pos); return ActionResultType.SUCCESS; } }
		 * else { return ActionResultType.PASS;
		 * 
		 * } }
		 */
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

	@SuppressWarnings("deprecation")
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
		return new TileEntityMorphlingIncubator();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onBlockClicked(BlockState state, World worldIn, BlockPos pos, PlayerEntity player) {
		super.onBlockClicked(state, worldIn, pos, player);
	}

}
