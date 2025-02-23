package com.vincenthuto.hemomancy.common.block;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.tile.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hemomancy.common.tile.MorphlingIncubatorBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.common.container.HLInvHelper;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.MyceliumBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class MorphlingIncubatorBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(7, 0, 7, 9, 3, 9), Block.box(6, 2, 6, 10, 4, 10),
			Block.box(10.75, 3, 5, 11.75, 4, 11), Block.box(4.25, 3, 5, 5.25, 4, 11),
			Block.box(5, 3, 10.75, 11, 4, 11.75), Block.box(5, 3, 4.25, 11, 4, 5.25), Block.box(4, 4, 4, 12, 5, 12),
			Block.box(5, 5, 5, 11, 6, 11), Block.box(11, 5, 11, 12, 13, 12), Block.box(11, 5, 4, 12, 13, 5),
			Block.box(4, 5, 4, 5, 13, 5), Block.box(4, 5, 11, 5, 13, 12), Block.box(4.5, 5, 4.5, 5, 12.5, 11.5),
			Block.box(6, 6, 6, 10, 10, 10), Block.box(11, 5, 4.5, 11.5, 12.5, 11.5),
			Block.box(4.5, 5, 4.75, 11.5, 12.5, 5), Block.box(4.5, 5, 11, 11.5, 12.5, 11.5),
			Block.box(5, 11, 5, 11, 12, 11), Block.box(6, 0, 6, 10, 0.5, 10)).reduce((v1, v2) -> {
				return Shapes.join(v1, v2, BooleanOp.OR);
			}).get();

	public MorphlingIncubatorBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));

	}

	@Override
	public void attack(BlockState state, Level worldIn, BlockPos pos, Player player) {
		super.attack(state, worldIn, pos, player);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new MorphlingIncubatorBlockEntity(arg0, arg1);
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int id, int param) {
		super.triggerEvent(state, world, pos, id, param);
		BlockEntity BlockEntity = world.getBlockEntity(pos);
		return BlockEntity != null && BlockEntity.triggerEvent(id, param);
	}

	@Nullable
	@SuppressWarnings("unchecked")
	public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> candidate, BlockEntityType<E> desired, BlockEntityTicker<? super E> ticker) {
		return desired == candidate ? (BlockEntityTicker<A>) ticker : null;
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) {
			return createTickerHelper(type, BlockEntityInit.morphling_incubator.get(),
					MorphlingIncubatorBlockEntity::clientTick);
		} else {
			return createTickerHelper(type, BlockEntityInit.morphling_incubator.get(),
					MorphlingIncubatorBlockEntity::serverTick);
		}
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		MorphlingIncubatorBlockEntity te = (MorphlingIncubatorBlockEntity) worldIn.getBlockEntity(pos);

		ItemStack stack = player.getItemInHand(handIn);

		if (player.isShiftKeyDown()) {
			HLInvHelper.withdrawFromInventory(te,player);
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(te);
			return InteractionResult.SUCCESS;
		} else if (!stack.isEmpty()) {
			
			boolean hit = te.addItem(player, stack, handIn);
			VanillaPacketDispatcher.dispatchTEToNearbyPlayers(te);
			return hit ? InteractionResult.SUCCESS : InteractionResult.PASS;
		}else {
			te.attemptStartup();
	
		}

		return InteractionResult.PASS;
	}

}
