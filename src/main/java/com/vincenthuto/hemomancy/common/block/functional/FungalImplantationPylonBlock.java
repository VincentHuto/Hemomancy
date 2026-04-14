package com.vincenthuto.hemomancy.common.block.functional;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.common.block.IMultiBlock;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.scars.PacketOpenSporeInv;
import com.vincenthuto.hemomancy.common.tile.functional.FungalImplantationPylonBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
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

public class FungalImplantationPylonBlock extends BaseEntityBlock implements IMultiBlock {

	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0),
			new BlockPos(0, 2, 0)
	};

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public FungalImplantationPylonBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
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
		return RenderShape.MODEL;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		if (pos.getY() + 2 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
		}
		return null; // Prevents placement if there's not enough room
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
		super.setPlacedBy(level, pos, state, placer, stack);
		if (!level.isClientSide) {
			placeFillers(level, pos, state);
		}
	}

	@Override
	public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			if (!level.isClientSide) {
				removeFillers(level, pos);
			}
		}
		super.onRemove(state, level, pos, newState, isMoving);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153183_,
			BlockEntityType<T> p_153184_) {
		return level.isClientSide
				? createTickerHelper(p_153184_, BlockEntityInit.fungal_implantation_pylon.get(),
						FungalImplantationPylonBlockEntity::clientTick)
				: createTickerHelper(p_153184_, BlockEntityInit.fungal_implantation_pylon.get(),
						FungalImplantationPylonBlockEntity::serverTick);
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
		return new FungalImplantationPylonBlockEntity(arg0, arg1);
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
				PacketHandler.CHANNELSCARS.sendToServer(new PacketOpenSporeInv());
			}
		} else {
			if (!worldIn.isClientSide) {
				ItemEntity spawn = new ItemEntity(worldIn, pos.getX(), pos.getY() + 1, pos.getZ(),
						new ItemStack(BlockInit.infected_fungus.get(), 1));
				worldIn.destroyBlock(pos, false);
				worldIn.addFreshEntity(spawn);
				worldIn.setBlockAndUpdate(pos, BlockInit.fungal_podium.get().defaultBlockState());
			}
		}
//		if (worldIn.getBlockEntity(pos) instanceof BlockEntityScarModStation) {
//			((BlockEntityScarModStation) worldIn.getBlockEntity(pos)).onActivated(player, player.getMainHandItem());
//
//		}
		return InteractionResult.SUCCESS;

	}
}
