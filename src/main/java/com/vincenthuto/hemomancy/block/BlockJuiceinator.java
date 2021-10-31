package com.vincenthuto.hemomancy.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.vincenthuto.hemomancy.init.BlockEntityInit;
import com.vincenthuto.hemomancy.tile.BlockEntityJuicinator;
import com.vincenthuto.hutoslib.client.particle.factory.EmberParticleFactory;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public class BlockJuiceinator extends BaseEntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public BlockJuiceinator(BlockBehaviour.Properties p_48687_) {
		super(p_48687_);
		this.registerDefaultState(
				this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.valueOf(false)));
	}

	@Override
	public InteractionResult use(BlockState p_48706_, Level p_48707_, BlockPos p_48708_, Player p_48709_,
			InteractionHand p_48710_, BlockHitResult p_48711_) {
		if (p_48707_.isClientSide) {
			return InteractionResult.SUCCESS;
		} else {
			this.openContainer(p_48707_, p_48708_, p_48709_);
			return InteractionResult.CONSUME;
		}
	}

	protected void openContainer(Level level, BlockPos pos, Player player) {
		BlockEntity blockEntity = level.getBlockEntity(pos);
		if (blockEntity instanceof BlockEntityJuicinator furnace) {
			player.openMenu(furnace);
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
			if (blockentity instanceof BlockEntityJuicinator) {
				((BlockEntityJuicinator) blockentity).setCustomName(p_48698_.getHoverName());
			}
		}

	}

	@Override
	public void onRemove(BlockState p_48713_, Level p_48714_, BlockPos p_48715_, BlockState p_48716_,
			boolean p_48717_) {
		if (!p_48713_.is(p_48716_.getBlock())) {
			BlockEntity blockentity = p_48714_.getBlockEntity(p_48715_);
			if (blockentity instanceof BlockEntityJuicinator) {
				if (p_48714_ instanceof ServerLevel) {
					Containers.dropContents(p_48714_, p_48715_, (BlockEntityJuicinator) blockentity);
					((BlockEntityJuicinator) blockentity).getRecipesToAwardAndPopExperience((ServerLevel) p_48714_,
							Vec3.atCenterOf(p_48715_));
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
	public RenderShape getRenderShape(BlockState p_48727_) {
		return RenderShape.MODEL;
	}

	@Override
	public BlockState rotate(BlockState p_48722_, Rotation p_48723_) {
		return p_48722_.setValue(FACING, p_48723_.rotate(p_48722_.getValue(FACING)));
	}

	@Override
	@SuppressWarnings("deprecation")
	public BlockState mirror(BlockState p_48719_, Mirror p_48720_) {
		return p_48719_.rotate(p_48720_.getRotation(p_48719_.getValue(FACING)));
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_48725_) {
		p_48725_.add(FACING, LIT);
	}

	@Nullable
	protected static <T extends BlockEntity> BlockEntityTicker<T> createFurnaceTicker(Level p_151988_,
			BlockEntityType<T> p_151989_, BlockEntityType<? extends BlockEntityJuicinator> p_151990_) {
		return p_151988_.isClientSide ? null
				: createTickerHelper(p_151989_, p_151990_, BlockEntityJuicinator::serverTick);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153213_,
			BlockEntityType<T> p_153214_) {
		return level.isClientSide ? null
				: createTickerHelper(p_153214_, BlockEntityInit.juiceinator.get(), BlockEntityJuicinator::serverTick);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos p_153277_, BlockState p_153278_) {
		return new BlockEntityJuicinator(p_153277_, p_153278_);
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
}
