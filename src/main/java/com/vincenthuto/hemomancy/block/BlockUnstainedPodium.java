package com.vincenthuto.hemomancy.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.init.BlockInit;
import com.vincenthuto.hemomancy.init.ItemInit;
import com.vincenthuto.hemomancy.tile.BlockEntityUnstainedPodium;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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

public class BlockUnstainedPodium extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream
			.of(Block.box(3, 0, 3, 13, 1, 13), Block.box(4, 12, 4, 12, 14.01, 12), Block.box(3, 12, 3, 13, 14, 13),
					Block.box(5.800000000000001, 14, 5.800000000000001, 10.2, 14.9, 10.2),
					Block.box(3, 10, 3, 13, 12, 13), Block.box(2, 11, 3, 3, 13, 13), Block.box(13, 11, 3, 14, 13, 13),
					Block.box(3, 11, 13, 13, 13, 14), Block.box(3, 11, 2, 13, 13, 3), Block.box(4, 1, 4, 12, 2, 12),
					Block.box(4, 9, 4, 12, 10, 12), Block.box(5, 2, 5, 11, 9, 11))
			.reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public BlockUnstainedPodium(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {

		worldIn.playSound(player, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.25f, 1f);
		ItemStack stack = player.getItemInHand(handIn);
		if (!player.isShiftKeyDown()) {
			if (stack.getItem() == ItemInit.sanguine_conduit.get()) {
				worldIn.destroyBlock(pos, false);
				stack.shrink(1);
				worldIn.setBlockAndUpdate(pos, BlockInit.rune_mod_station.get().defaultBlockState());
			}
			
			if (stack.getItem() == ItemInit.scrying_dish.get()) {
				worldIn.destroyBlock(pos, false);
				stack.shrink(1);
				worldIn.setBlockAndUpdate(pos, BlockInit.scrying_podium.get().defaultBlockState());
			}
		}

		return InteractionResult.SUCCESS;

	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
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
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new BlockEntityUnstainedPodium(arg0, arg1);
	}

}
