package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.OpenRunesInvPacket;
import com.huto.hemomancy.tile.TileEntityRuneModStation;

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

public class BlockRuneModStation extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.makeCuboidShape(1, 0, 1, 15, 1, 15), Block.makeCuboidShape(3, 12, 3, 13, 14, 13),
					Block.makeCuboidShape(2, 1, 2, 14, 2, 14), Block.makeCuboidShape(2, 11, 2, 14, 12, 14),
					Block.makeCuboidShape(3, 2, 11, 5, 12, 13), Block.makeCuboidShape(11, 2, 11, 13, 12, 13),
					Block.makeCuboidShape(11, 2, 3, 13, 12, 5), Block.makeCuboidShape(13, 12, 1, 15, 14, 3),
					Block.makeCuboidShape(13.5, 14, 1.5, 14.5, 15, 2.5), Block.makeCuboidShape(13, 12, 13, 15, 14, 15),
					Block.makeCuboidShape(13.5, 14, 13.5, 14.5, 15, 14.5), Block.makeCuboidShape(1, 12, 1, 3, 14, 3),
					Block.makeCuboidShape(1.5, 14, 1.5, 2.5, 15, 2.5), Block.makeCuboidShape(1, 12, 13, 3, 14, 15),
					Block.makeCuboidShape(1.5, 14, 13.5, 2.5, 15, 14.5), Block.makeCuboidShape(3, 2, 3, 5, 12, 5))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockRuneModStation(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {
		if (worldIn.isRemote) {
			PacketHandler.INSTANCE.sendToServer(new OpenRunesInvPacket());
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

	@SuppressWarnings("deprecation")
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.toRotation(state.get(FACING)));
	}

	@Override
	protected void fillStateContainer(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@SuppressWarnings("deprecation")
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
		return new TileEntityRuneModStation();
	}

}
