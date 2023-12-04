package com.vincenthuto.hemomancy.common.block;

import java.util.stream.Stream;

import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.runes.PacketOpenRunesInv;
import com.vincenthuto.hemomancy.common.tile.ScryingPodiumBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
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

public class ScryingPodiumBlock extends BaseEntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(Block.box(3, 0, 3, 13, 2, 13), Block.box(3, 12, 3, 13, 14, 13),
			Block.box(6, 13, 6, 10, 15, 10), Block.box(6, 13, 6, 10, 15, 10), Block.box(5, 15, 5, 11, 16, 11),
			Block.box(3, 9, 3, 13, 12, 13), Block.box(2, 11, 2, 14, 13, 14), Block.box(4, 1, 4, 12, 3, 12),
			Block.box(4, 8, 4, 12, 10, 12), Block.box(5, 2, 5, 11, 9, 11))
			.reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

	public ScryingPodiumBlock(Properties properties) {
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
	public RenderShape getRenderShape(BlockState p_49232_) {
		// TODO Auto-generated method stub
		return RenderShape.MODEL;
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
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153183_,
			BlockEntityType<T> p_153184_) {
		return level.isClientSide
				? createTickerHelper(p_153184_, BlockEntityInit.scrying_podium.get(),
						ScryingPodiumBlockEntity::animTick)
				: null;
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
		return new ScryingPodiumBlockEntity(arg0, arg1);
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

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn,
			BlockHitResult result) {
		if (!player.isShiftKeyDown()) {
			if (worldIn.isClientSide) {
				PacketHandler.CHANNELRUNES.sendToServer(new PacketOpenRunesInv());
			}
		} else {
			if (!worldIn.isClientSide) {
				ItemEntity spawn = new ItemEntity(worldIn, pos.getX(), pos.getY() + 1, pos.getZ(),
						new ItemStack(ItemInit.scrying_dish.get(), 1));
				worldIn.destroyBlock(pos, false);
				worldIn.addFreshEntity(spawn);
				worldIn.setBlockAndUpdate(pos, BlockInit.unstained_podium.get().defaultBlockState());
			}
		}
//		if (worldIn.getBlockEntity(pos) instanceof BlockEntityRuneModStation) {
//			((BlockEntityRuneModStation) worldIn.getBlockEntity(pos)).onActivated(player, player.getMainHandItem());
//
//		}
		return InteractionResult.SUCCESS;

	}
}