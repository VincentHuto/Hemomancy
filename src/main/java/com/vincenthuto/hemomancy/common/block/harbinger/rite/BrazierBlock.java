package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import com.vincenthuto.hemomancy.common.init.ParticleInit;
import com.vincenthuto.hemomancy.client.particle.data.BloodCellData;
import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.item.harbinger.memories.LivingWeaponGraftRite;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.MemoryBrazierRite;
import com.vincenthuto.hemomancy.common.rite.ScarBrazierRite;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hutoslib.client.particle.util.HLParticleUtils;
import com.vincenthuto.hutoslib.client.particle.util.ParticleColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.Shapes;
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

	// Dais, vertebral stem, and the bowl up to its talon tips (tools/model_export/iron_brazier.py).
	private static final VoxelShape SHAPE = Shapes.or(Block.box(3, 0, 3, 13, 2.2, 13),
			Block.box(6, 2.2, 6, 10, 8.4, 10), Block.box(1.5, 8.4, 1.5, 14.5, 18.5, 14.5));
	// Coal bed and flame tips in blocks above the brazier's base, from the same model.
	private static final double COAL_TOP = 11.3D / 16.0D;
	private static final double FLAME_TOP = 18.3D / 16.0D;
	private static final ParticleColor EMBER = new ParticleColor(232, 42, 36);
	private static final ParticleColor SANGUINE_EMBER = new ParticleColor(170, 0, 22);
	private static final ParticleColor HOT_CORE = new ParticleColor(255, 176, 150);
	private static final ParticleColor SOOT = new ParticleColor(22, 2, 6);

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
		int phase = pState.getValue(RITUAL_PHASE);
		if (phase == 0) {
			return;
		}
		double x = pPos.getX() + 0.5D;
		double z = pPos.getZ() + 0.5D;
		// Crimson embers lift off the coal bed and burn out around the offering.
		for (int i = 0; i < 3; i++) {
			double ox = (pRandom.nextDouble() - 0.5D) * 0.36D;
			double oz = (pRandom.nextDouble() - 0.5D) * 0.36D;
			pLevel.addParticle(HemoParticleData.ember(phase == 2 ? SANGUINE_EMBER : EMBER,
					0.05F + pRandom.nextFloat() * 0.035F, 0.85F, 26 + pRandom.nextInt(14)),
					x + ox, pPos.getY() + COAL_TOP, z + oz, -ox * 0.03D, 0.022D + pRandom.nextDouble() * 0.018D,
					-oz * 0.03D);
		}
		if (pRandom.nextInt(3) == 0) {
			pLevel.addParticle(HemoParticleData.ember(HOT_CORE, 0.035F, 0.95F, 16), x, pPos.getY() + COAL_TOP + 0.08D,
					z, 0.0D, 0.045D, 0.0D);
		}
		pLevel.addParticle(HemoParticleData.darkGlow(SOOT), x + (pRandom.nextDouble() - 0.5D) * 0.2D,
				pPos.getY() + FLAME_TOP, z + (pRandom.nextDouble() - 0.5D) * 0.2D, 0.0D, 0.012D, 0.0D);
		if (phase == 2) {
			// Sanguine flames: blood cells wheel up round the offering while the rite awaits its echo.
			for (int i = 0; i < 3; i++) {
				double angle = pRandom.nextDouble() * Math.PI * 2.0D;
				double cos = Math.cos(angle) * 0.2D;
				double sin = Math.sin(angle) * 0.2D;
				pLevel.addParticle(HemoParticleData.bloodCell(SANGUINE_EMBER), x + cos,
						pPos.getY() + FLAME_TOP - 0.25D + pRandom.nextDouble() * 0.3D, z + sin,
						-sin * 0.12D, 0.035D, cos * 0.12D);
			}
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
		return SHAPE;
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
			level.sendParticles(HemoParticleData.darkGlow(SOOT), pos.getX() + 0.5D, pos.getY() + COAL_TOP + 0.15D,
					pos.getZ() + 0.5D, 18, 0.18D, 0.12D, 0.18D, 0.025D);
			level.sendParticles(HemoParticleData.ember(SANGUINE_EMBER, 0.06F, 0.8F, 18), pos.getX() + 0.5D,
					pos.getY() + COAL_TOP, pos.getZ() + 0.5D, 10, 0.2D, 0.04D, 0.2D, 0.02D);
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
		HLParticleUtils.spawnPoof(level, pos, new BloodCellData(ParticleInit.blood_cell.get(), ParticleColor.BLOOD));
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
