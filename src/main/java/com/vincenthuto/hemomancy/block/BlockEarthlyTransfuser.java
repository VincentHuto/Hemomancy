package com.vincenthuto.hemomancy.block;

import java.util.Random;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.tile.BlockEntityEarthlyTransfuser;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import com.vincenthuto.hutoslib.common.network.VanillaPacketDispatcher;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class BlockEarthlyTransfuser extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE_N = Stream.of(
			Block.box(3, 5, 3, 13, 7, 13),
			Block.box(5, 3, 5, 11, 5, 11),
			Block.box(4, 1, 4, 12, 3, 12),
			Block.box(0, 0, 0, 16, 1, 16)
			).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public BlockEarthlyTransfuser(BlockBehaviour.Properties p_48687_) {
		super(p_48687_);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(LIT, Boolean.valueOf(false)));
	}

	@Override
	public InteractionResult use(BlockState p_48706_, Level p_48707_, BlockPos p_48708_, Player p_48709_,
			InteractionHand p_48710_, BlockHitResult p_48711_) {
		this.openContainer(p_48707_, p_48708_, p_48709_);
		return InteractionResult.CONSUME;
	}

	protected void openContainer(Level level, BlockPos pos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BlockEntityEarthlyTransfuser te) {
			if (!level.isClientSide) {
				VanillaPacketDispatcher.dispatchTEToNearbyPlayers(level, pos);
				NetworkHooks.openGui((ServerPlayer) player, te, pos);
			}
		}
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext p_48689_) {
		return this.defaultBlockState().setValue(FACING, p_48689_.getHorizontalDirection().getOpposite());
	}

	@Override
	public void setPlacedBy(Level p_48694_, BlockPos p_48695_, BlockState p_48696_, LivingEntity p_48697_,
			ItemStack p_48698_) {
		if (p_48698_.hasCustomHoverName()) {
			BlockEntity blockentity = p_48694_.getBlockEntity(p_48695_);
			if (blockentity instanceof BlockEntityEarthlyTransfuser) {
				((BlockEntityEarthlyTransfuser) blockentity).setCustomName(p_48698_.getHoverName());
			}
		}

	}

	@Override
	public void onRemove(BlockState p_48713_, Level p_48714_, BlockPos p_48715_, BlockState p_48716_,
			boolean p_48717_) {
		if (!p_48713_.is(p_48716_.getBlock())) {
			BlockEntity blockentity = p_48714_.getBlockEntity(p_48715_);
			if (blockentity instanceof BlockEntityEarthlyTransfuser) {
				if (p_48714_ instanceof ServerLevel) {
					Containers.dropContents(p_48714_, p_48715_, (BlockEntityEarthlyTransfuser) blockentity);
					((BlockEntityEarthlyTransfuser) blockentity)
							.getRecipesToAwardAndPopExperience((ServerLevel) p_48714_, Vec3.atCenterOf(p_48715_));
				}

				p_48714_.updateNeighbourForOutputSignal(p_48715_, this);
			}
			super.onRemove(p_48713_, p_48714_, p_48715_, p_48716_, p_48717_);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState p_48700_) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState p_48702_, Level p_48703_, BlockPos p_48704_) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(p_48703_.getBlockEntity(p_48704_));
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
		builder.add(FACING, LIT);
	}

	@Nullable
	protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(
			BlockEntityType<A> p_152133_, BlockEntityType<E> p_152134_, BlockEntityTicker<? super E> p_152135_) {
		return p_152134_ == p_152133_ ? (BlockEntityTicker<A>) p_152135_ : null;
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(Level p_151988_,
			BlockEntityType<T> p_151989_, BlockEntityType<? extends BlockEntityEarthlyTransfuser> p_151990_) {
		return p_151988_.isClientSide ? null
				: createTickerHelper(p_151989_, p_151990_, BlockEntityEarthlyTransfuser::serverTick);
	}

	private static BlockEntityTicker<BlockEntityEarthlyTransfuser> createTickerHelper(
			BlockEntityType<BlockEntityEarthlyTransfuser> p_151989_,
			BlockEntityType<? extends BlockEntityEarthlyTransfuser> p_151990_, Object object) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153213_,
			BlockEntityType<T> p_153214_) {
		return level.isClientSide ? null
				: createTickerHelper(p_153214_, BlockEntityInit.earthly_transfuser.get(),
						BlockEntityEarthlyTransfuser::serverTick);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153277_, BlockState p_153278_) {
		return new BlockEntityEarthlyTransfuser(p_153277_, p_153278_);
	}

	@Override
	public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
		return super.getLightEmission(state, world, pos);
	}

	@Override
	public void animateTick(BlockState p_53635_, Level p_53636_, BlockPos p_53637_, Random p_53638_) {
		if (p_53635_.getValue(LIT)) {
			double d0 = p_53637_.getX() + 0.5D;
			double d1 = p_53637_.getY();
			double d2 = p_53637_.getZ() + 0.5D;
			if (p_53638_.nextDouble() < 0.1D) {
				p_53636_.playLocalSound(d0, d1, d2, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F,
						false);
			}
			Direction direction = p_53635_.getValue(FACING);
			Direction.Axis direction$axis = direction.getAxis();
			double d4 = p_53638_.nextDouble() * 0.6D - 0.3D;
			double d5 = direction$axis == Direction.Axis.X ? direction.getStepX() * 0.52D : d4;
			double d6 = p_53638_.nextDouble() * 6.0D / 16.0D;
			double d7 = direction$axis == Direction.Axis.Z ? direction.getStepZ() * 0.52D : d4;
			p_53636_.addParticle(ParticleTypes.LANDING_LAVA, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
			p_53636_.addParticle(EmberParticleFactory.createData(ParticleColor.RED, 2, 0.15f, 150), d0 + d5, d1 + d6,
					d2 + d7, 0.0D, 0.0D, 0.0D);
		}
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public void onNeighborChange(BlockState state, LevelReader world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(state, world, pos, neighbor);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

}
