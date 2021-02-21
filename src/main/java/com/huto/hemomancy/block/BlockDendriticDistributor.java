package com.huto.hemomancy.block;

import java.util.stream.Stream;

import com.huto.hemomancy.init.BlockInit;
import com.huto.hemomancy.init.ItemInit;
import com.huto.hemomancy.tile.TileEntityDendriticDistributor;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
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
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class BlockDendriticDistributor extends Block {
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.makeCuboidShape(5, 0, 5, 11, 2, 11), Block.makeCuboidShape(7, 2, 7, 9, 15, 9),
					Block.makeCuboidShape(8.5, 4, 7, 9.5, 12, 9), Block.makeCuboidShape(6.5, 4, 7, 7.5, 12, 9),
					Block.makeCuboidShape(7, 4, 8.5, 9, 12, 9.5), Block.makeCuboidShape(7, 4, 6.5, 9, 12, 7.5),
					Block.makeCuboidShape(10.5, 0.4, 5.5, 11.5, 1.400, 10.5),
					Block.makeCuboidShape(4.5, 0.4, 5.5, 5.5, 1.400, 10.5),
					Block.makeCuboidShape(5.5, 0.4, 4.5, 10.5, 1.400, 5.5),
					Block.makeCuboidShape(5.5, 0.4, 10.5, 10.5, 1.400, 11.5),
					Block.makeCuboidShape(6, 12.70, 6, 10, 14.8, 10), Block.makeCuboidShape(6, 0.700, 6, 10, 2.80, 10))
			.reduce((v1, v2) -> {
				return VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR);
			}).get();

	public BlockDendriticDistributor(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.SOUTH));

	}

	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player,
			Hand handIn, BlockRayTraceResult result) {

		worldIn.playSound(player, pos, SoundEvents.ENTITY_ZOMBIE_AMBIENT, SoundCategory.BLOCKS, 0.25f, 1f);
		ItemStack stack = player.getHeldItem(handIn);
		if (!player.isSneaking()) {
			if (stack.getItem() == ItemInit.sanguine_conduit.get()) {
				worldIn.destroyBlock(pos, false);
				stack.shrink(1);
				worldIn.setBlockState(pos, BlockInit.rune_mod_station.get().getDefaultState());
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
		return new TileEntityDendriticDistributor();
	}

}
