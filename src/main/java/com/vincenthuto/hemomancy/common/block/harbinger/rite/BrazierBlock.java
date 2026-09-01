package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import com.vincenthuto.hemomancy.client.particle.factory.BloodCellParticleFactory;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRite;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.rite.MemoryBrazierRite;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Iron Brazier block — a versatile ritual apparatus. In its base form it
 * is lit only by Blood Projection or Living Staff projection.
 * The ritual phase property tracks the brazier's state:
 * <ul>
 *   <li><b>0</b> — Unlit (cold)</li>
 *   <li><b>1</b> — Normal fire (lit)</li>
 *   <li><b>2</b> — Sanguine flames (reagents accepted, awaiting echo)</li>
 * </ul>
 */
public class BrazierBlock extends Block implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final double BLOOD_TO_LIGHT = 50.0D;

	/**
	 * Ritual phase: 0 = unlit, 1 = normal fire, 2 = sanguine flames (awaiting echo).
	 */
	public static final IntegerProperty RITUAL_PHASE = IntegerProperty.create("ritual_phase", 0, 2);

	private static final VoxelShape SHAPE_N =Block.box(4, 0, 4, 12, 17, 12);

	public BrazierBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(RITUAL_PHASE, 0)
				.setValue(WATERLOGGED, false));
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		super.animateTick(pState, pLevel, pPos, pRandom);
		double d0 = (double) pPos.getX() + 0.5D;
		double d1 = (double) pPos.getY() + 1.2D;
		double d2 = (double) pPos.getZ() + 0.5D;
		int phase = pState.getValue(RITUAL_PHASE);

		if (phase == 1) {
			// Normal fire — standard smoke
			pLevel.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0D, 0.0D, 0.0D);
		} else if (phase == 2) {
			// Sanguine flames — dark red blood particles swirling upward
			for (int i = 0; i < 3; i++) {
				double ox = (pRandom.nextDouble() - 0.5) * 0.4;
				double oz = (pRandom.nextDouble() - 0.5) * 0.4;
				pLevel.addParticle(
						BloodCellParticleFactory.createData(new ParticleColor(180, 0, 20)),
						d0 + ox, d1 + pRandom.nextDouble() * 0.3, d2 + oz,
						0.0D, 0.04D, 0.0D);
			}
			pLevel.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, 0.0D, 0.02D, 0.0D);
		}
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, RITUAL_PHASE, WATERLOGGED);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_60550_) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite())
				.setValue(RITUAL_PHASE, 0)
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
	}

	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public boolean placeLiquid(LevelAccessor level, BlockPos pos, BlockState state, FluidState fluidState) {
		if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
			level.setBlock(pos, state.setValue(WATERLOGGED, true).setValue(RITUAL_PHASE, 0), Block.UPDATE_ALL);
			level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
			return true;
		}
		return false;
	}

	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING))).setValue(RITUAL_PHASE, 0);
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos arg0, BlockState arg1) {
		return new IronBrazierBlockEntity(arg0, arg1);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (level.isClientSide) return null;
		return (BlockEntityTicker<T>) (lvl, pos, st, te) -> {
			if (te instanceof IronBrazierBlockEntity brazierTE) {
				IronBrazierBlockEntity.serverTick(lvl, pos, st, brazierTE);
			}
		};
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING))).setValue(RITUAL_PHASE, 0);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos currentPos, BlockPos facingPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, currentPos);
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player,
			ItemStack stack) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		int phase = state.getValue(RITUAL_PHASE);
		BlockEntity be = worldIn.getBlockEntity(pos);
		IronBrazierBlockEntity brazierTE = be instanceof IronBrazierBlockEntity brazier ? brazier : null;
		if (player.isShiftKeyDown() && brazierTE != null && brazierTE.hasOffering()) {
			ItemStack extracted = brazierTE.extractOffering();
			if (!player.getInventory().add(extracted)) {
				player.drop(extracted, false);
			}
			return InteractionResult.SUCCESS;
		}

		if (phase == 0) {
			if (tryInsertOffering(worldIn, pos, player, stack, brazierTE, false)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		if (phase == 1) {
			if (tryInsertOffering(worldIn, pos, player, stack, brazierTE, true)) {
				return InteractionResult.SUCCESS;
			}

			return InteractionResult.PASS;
		}

		if (phase == 2) {
			if (tryInsertOffering(worldIn, pos, player, stack, brazierTE, true)) {
				return InteractionResult.SUCCESS;
			}
			return InteractionResult.PASS;
		}

		return InteractionResult.PASS;
	}

	private static boolean tryInsertOffering(Level level, BlockPos pos, Player player, ItemStack stack,
			IronBrazierBlockEntity brazier, boolean lit) {
		if (brazier == null) {
			return false;
		}
		ItemStack feedbackStack = stack.copy();
		if (!brazier.insertOffering(player, stack)) {
			return false;
		}
		if (level instanceof ServerLevel serverLevel) {
			BrazierSpecialOfferingEffects.spawn(serverLevel, pos, feedbackStack, lit, true);
		}
		return true;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player, ItemStack.EMPTY);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		if (isBloodTool(stack)) {
			return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
		}
		return handleInteraction(state, worldIn, pos, player, stack) == InteractionResult.PASS
				? ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
				: ItemInteractionResult.SUCCESS;
	}

	private static boolean isBloodTool(ItemStack stack) {
		return stack.getItem() instanceof BloodAbsorptionItem
				|| stack.getItem() instanceof BloodProjectionItem
				|| stack.getItem() instanceof LivingStaffItem;
	}

	@Override
	public boolean canDrawAbsorptionParticles(Level level, BlockPos pos, BlockState state) {
		return BrazierBloodInteractionRules.shouldDrawAbsorptionParticles(state.getValue(RITUAL_PHASE) > 0);
	}

	@Override
	public double absorbBloodFromBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier
				&& BrazierBloodInteractionRules.shouldExtinguishOnAbsorption(
						state.getValue(RITUAL_PHASE) > 0, !brazier.hasOffering(), maxAmount)) {
			brazier.resetItemAbsorptionProgress();
			level.setBlock(pos, state.setValue(RITUAL_PHASE, 0), Block.UPDATE_ALL);
			HLParticleUtils.spawnPoof(level, pos, ParticleTypes.SMOKE);
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 1.0F);
			return maxAmount;
		}
		double memoryHandled = MemoryBrazierRite.tryAbsorb(level, pos, state, player, maxAmount);
		if (memoryHandled > 0.0D) {
			return memoryHandled;
		}
		double scarHandled = ScarBrazierRite.tryAbsorb(level, pos, state, player, maxAmount);
		return scarHandled > 0.0D
				? scarHandled
				: LivingWeaponGraftRite.tryAbsorb(level, pos, state, player, maxAmount);
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (state.getValue(WATERLOGGED) || state.getValue(RITUAL_PHASE) > 0 || maxAmount < BLOOD_TO_LIGHT) {
			return 0.0D;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		if (volume == null || !volume.isActive() || volume.getBloodVolume() < BLOOD_TO_LIGHT) {
			if (level.getGameTime() % 20L == 0L) {
				player.displayClientMessage(Component.literal("No blood answers the brazier."), true);
			}
			return 0.0D;
		}
		volume.subtractBloodVolume(BLOOD_TO_LIGHT);
		volume.addBloodSpend(BLOOD_TO_LIGHT);
		BloodVolumeEvents.syncVolume(player, volume);
		ItemStack offering = level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier
				? brazier.getOfferingDisplayStack()
				: ItemStack.EMPTY;
		level.setBlock(pos, state.setValue(RITUAL_PHASE, 1), Block.UPDATE_ALL);
		HLParticleUtils.spawnPoof(level, pos, BloodCellParticleFactory.createData(ParticleColor.BLOOD));
		BrazierSpecialOfferingEffects.spawn(level, pos, offering, true,
				BrazierSpecialOfferingRules.shouldEmitOnIgnition(false, true, !offering.isEmpty()));
		level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.55F, 0.75F);
		return BLOOD_TO_LIGHT;
	}

	@Override
	protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
		if (!state.is(newState.getBlock())) {
			dropOffering(level, pos);
		}
		super.onRemove(state, level, pos, newState, movedByPiston);
	}

	private static void dropOffering(Level level, BlockPos pos) {
		if (level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier) {
			ItemStack offering = brazier.extractOffering();
			if (!offering.isEmpty()) {
				Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.9D, pos.getZ() + 0.5D, offering);
			}
		}
	}

	/**
	 * Convenience to check whether the brazier is in sanguine flame state.
	 */
	public static boolean isSanguineFlame(BlockState state) {
		return state.getBlock() instanceof BrazierBlock && state.getValue(RITUAL_PHASE) == 2;
	}

	/**
	 * Returns true if the brazier is lit (any fire state).
	 */
	public static boolean isLit(BlockState state) {
		return state.getBlock() instanceof BrazierBlock && state.getValue(RITUAL_PHASE) > 0;
	}
}
