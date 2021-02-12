package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.network.PacketHandler;
import com.huto.hemomancy.network.capa.OpenRunesInvPacket;
import com.huto.hemomancy.tile.TileEntityRuneModStation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
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

@SuppressWarnings("deprecation")
public class BlockRuneModStation extends Block implements ITileEntityProvider {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.makeCuboidShape(3, 0, 3, 13, 1, 13), Block.makeCuboidShape(4, 12, 4, 12, 14.01, 12),
					Block.makeCuboidShape(3, 12, 3, 13, 14, 13), Block.makeCuboidShape(6, 14, 6, 10, 15, 10),
					Block.makeCuboidShape(5.800000000000001, 14, 5.800000000000001, 10.2, 14.9, 10.2),
					Block.makeCuboidShape(7.5, 15, 7.5, 8.5, 15.5, 8.5), Block.makeCuboidShape(6, 16, 6, 10, 20, 10),
					Block.makeCuboidShape(6.5, 16.5, 5.499999999999998, 9.5, 19.5, 6.499999999999998),
					Block.makeCuboidShape(6.5, 16.5, 9.499999999999998, 9.5, 19.5, 10.499999999999998),
					Block.makeCuboidShape(5.499999999999998, 16.5, 6.5, 6.499999999999998, 19.5, 9.5),
					Block.makeCuboidShape(9.499999999999998, 16.5, 6.5, 10.499999999999998, 19.5, 9.5),
					Block.makeCuboidShape(6.5, 19.499999999999993, 6.6, 9.5, 20.499999999999993, 9.5),
					Block.makeCuboidShape(6.5, 15.499999999999993, 6.6, 9.5, 16.499999999999993, 9.5),
					Block.makeCuboidShape(3, 10, 3, 13, 12, 13), Block.makeCuboidShape(2, 11, 3, 3, 13, 13),
					Block.makeCuboidShape(13, 11, 3, 14, 13, 13), Block.makeCuboidShape(3, 11, 13, 13, 13, 14),
					Block.makeCuboidShape(3, 11, 2, 13, 13, 3), Block.makeCuboidShape(4, 1, 4, 12, 2, 12),
					Block.makeCuboidShape(4, 9, 4, 12, 10, 12), Block.makeCuboidShape(5, 2, 5, 11, 9, 11))
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
		if (!player.isSneaking()) {
			if (worldIn.isRemote) {
				PacketHandler.INSTANCE.sendToServer(new OpenRunesInvPacket());
			}
		} else {
			if (!worldIn.isRemote) {
				ItemEntity spawn = new ItemEntity(worldIn, pos.getX(), pos.getY() + 1, pos.getZ(),
						new ItemStack(ItemInit.sanguine_conduit.get(), 1));
				worldIn.destroyBlock(pos, false);
				worldIn.addEntity(spawn);
				worldIn.setBlockState(pos, BlockInit.unstained_podium.get().getDefaultState());
			}
		}
		if (worldIn.getTileEntity(pos) instanceof TileEntityRuneModStation) {
			((TileEntityRuneModStation) worldIn.getTileEntity(pos)).onActivated(player, player.getHeldItemMainhand());

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
	public boolean eventReceived(BlockState state, World world, BlockPos pos, int id, int param) {
		super.eventReceived(state, world, pos, id, param);
		TileEntity tileentity = world.getTileEntity(pos);
		return tileentity != null && tileentity.receiveClientEvent(id, param);
	}

	@Override
	public TileEntity createNewTileEntity(IBlockReader worldIn) {
		return new TileEntityRuneModStation();
	}

}
