package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.tile.TileEntityMortalDisplay;
import com.huto.hemomancy.tile.TileEntityVisceralRecaller;

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

public class BlockVisceralRecaller extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.makeCuboidShape(1.5, 9.15, 1.5, 14.5, 14.65, 14.5),
			Block.makeCuboidShape(3.25, 14.500000000000002, 3.25, 12.75, 18.5, 12.5),
			Block.makeCuboidShape(1.25, 11.95, 1.25, 14.75, 14.2, 14.75),
			Block.makeCuboidShape(2.75, 14.500000000000002, 2.75, 13.25, 18, 12.75),
			Block.makeCuboidShape(12.3, 0, 12.3, 15.2, 1, 15.2),
			Block.makeCuboidShape(12.5, 0.09999999999999964, 12.5, 15, 11.100000000000001, 15),
			Block.makeCuboidShape(12.5, 0.09999999999999964, 1, 15, 11.100000000000001, 3.5),
			Block.makeCuboidShape(13.25, 2.0999999999999996, 3.5, 14.25, 3, 12.5),
			Block.makeCuboidShape(13.25, 8.1, 3.5, 14.25, 9, 12.5),
			Block.makeCuboidShape(12.3, 0, 0.8000000000000007, 15.2, 1, 3.6999999999999993),
			Block.makeCuboidShape(2.5, 2.0999999999999996, 13.25, 12.5, 3, 14.25),
			Block.makeCuboidShape(2.5, 8.1, 13.25, 12.5, 9, 14.25),
			Block.makeCuboidShape(3.3500000000000014, 13.300000000000004, 12.550000000000002, 12.45, 17,
					12.550000000000002),
			Block.makeCuboidShape(12.050000000000002, 13.3, 3.45, 12.050000000000002, 17, 12.55),
			Block.makeCuboidShape(3.3500000000000014, 11.500000000000002, 3.850000000000002, 12.45, 15.2,
					3.850000000000002),
			Block.makeCuboidShape(4.249999999999998, 11.550000000000002, 3.45, 4.249999999999998, 15.25, 12.55),
			Block.makeCuboidShape(13.25, 5.1, 3.5, 14.25, 6, 12.5),
			Block.makeCuboidShape(0.75, 0.09999999999999964, 12.5, 3.25, 11.100000000000001, 15),
			Block.makeCuboidShape(0.5500000000000007, 0, 12.3, 3.4499999999999993, 1, 15.2),
			Block.makeCuboidShape(0.75, 0.09999999999999964, 0.75, 3.25, 11.100000000000001, 3.25),
			Block.makeCuboidShape(0.5500000000000007, 0, 0.5500000000000007, 3.4499999999999993, 1, 3.4499999999999993),
			Block.makeCuboidShape(2.5, 5.1, 13.25, 12.5, 6, 14.25), Block.makeCuboidShape(2.5, 5.1, 1.5, 12.5, 6, 2.5),
			Block.makeCuboidShape(2.5, 2.0999999999999996, 1.5, 12.5, 3, 2.5),
			Block.makeCuboidShape(2.5, 8.1, 1.5, 12.5, 9, 2.5),
			Block.makeCuboidShape(1.25, 2.0999999999999996, 3.5, 2.25, 3, 12.5),
			Block.makeCuboidShape(1.25, 5.1, 3.5, 2.25, 6, 12.5), Block.makeCuboidShape(1.25, 8.1, 3.5, 2.25, 9, 12.5),
			Block.makeCuboidShape(12.3, 11, 12.3, 15.2, 12, 15.2),
			Block.makeCuboidShape(12.3, 11, 0.8000000000000007, 15.2, 12, 3.6999999999999993),
			Block.makeCuboidShape(0.5500000000000007, 11, 12.3, 3.4499999999999993, 12, 15.2),
			Block.makeCuboidShape(0.5500000000000007, 11, 0.5500000000000007, 3.4499999999999993, 12,
					3.4499999999999993),
			Block.makeCuboidShape(6.5, 0.09999999999999964, 6.5, 9, 11.100000000000001, 9),
			Block.makeCuboidShape(6.300000000000001, 0, 6.300000000000001, 9.2, 1, 9.2),
			Block.makeCuboidShape(6.300000000000001, 8.1, 6.300000000000001, 9.2, 9.1, 9.2),
			Block.makeCuboidShape(1.25, 9.2, 1.25, 14.75, 11.45, 14.75)).reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockVisceralRecaller(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {

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
