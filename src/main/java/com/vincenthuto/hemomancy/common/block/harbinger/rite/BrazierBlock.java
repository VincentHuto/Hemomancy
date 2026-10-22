package com.vincenthuto.hemomancy.common.block.harbinger.rite;

import com.vincenthuto.hemomancy.common.block.harbinger.BlockBloodEndpoint;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.BloodVolumeEvents;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.bloodvolume.IBloodVolume;
import com.vincenthuto.hemomancy.common.init.BlockEntityInit;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodAbsorptionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.BloodProjectionItem;
import com.vincenthuto.hemomancy.common.item.harbinger.tool.living.LivingStaffItem;
import com.vincenthuto.hemomancy.common.particle.HemoParticleData;
import com.vincenthuto.hemomancy.common.rite.brazier.BrazierRites;
import com.vincenthuto.hemomancy.common.tile.harbinger.rite.IronBrazierBlockEntity;
import com.vincenthuto.hemomancy.common.util.HemoTickers;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
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
 *   <li><b>1</b> — Lit</li>
 * </ul>
 * Phase 2 (the retired "sanguine flames" reagent state) is never set any more. The property keeps its 0-2 range
 * because the blockstate file still keys a model on it and older worlds may hold it; such a brazier simply reads
 * as lit.
 * <p>
 * {@link #FACE} is its mount ({@link BrazierMount}): standing on a block (the default, so older worlds keep standing
 * braziers), a sconce on the side of one, or hanging from the underside of one. A sconce or hanging brazier falls,
 * spilling its offering, when what holds it up is gone.
 */
public class BrazierBlock extends Block implements EntityBlock, SimpleWaterloggedBlock, BlockBloodEndpoint {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	public static final double BLOOD_TO_LIGHT = 50.0D;

	/** Ritual phase: 0 = unlit, 1 = lit; 2 is retired (see the class comment) and treated as lit. */
	public static final IntegerProperty RITUAL_PHASE = IntegerProperty.create("ritual_phase", 0, 2);

	// Coal bed from the same model, and the blood pool and flame tips BrazierFlames draws when lit, in blocks above
	// the brazier's base.
	private static final double COAL_TOP = 11.3D / 16.0D;
	private static final double POOL_TOP = 12.3D / 16.0D;
	private static final double FLAME_TOP = 23.0D / 16.0D;
	private static final ParticleColor EMBER = new ParticleColor(232, 42, 36);
	private static final ParticleColor SANGUINE_EMBER = new ParticleColor(170, 0, 22);
	private static final ParticleColor SOOT = new ParticleColor(22, 2, 6);

	public BrazierBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(FACING, Direction.SOUTH)
				.setValue(FACE, AttachFace.FLOOR)
				.setValue(RITUAL_PHASE, 0)
				.setValue(WATERLOGGED, false));
	}

	@Override
	public void animateTick(BlockState pState, Level pLevel, BlockPos pPos, RandomSource pRandom) {
		super.animateTick(pState, pLevel, pPos, pRandom);
		if (!isLit(pState)) {
			return;
		}
		double x = pPos.getX() + 0.5D;
		double z = pPos.getZ() + 0.5D;
		double base = pPos.getY() + BrazierMount.drop(BrazierMount.face(pState));
		// Crimson embers lift off the burning blood and burn out around the offering. The flame's hot core is
		// BrazierFlames' shader, so no white spark is thrown here.
		for (int i = 0; i < 3; i++) {
			double ox = (pRandom.nextDouble() - 0.5D) * 0.36D;
			double oz = (pRandom.nextDouble() - 0.5D) * 0.36D;
			pLevel.addParticle(HemoParticleData.emberSpark(EMBER,
					0.05F + pRandom.nextFloat() * 0.035F, 0.85F, 26 + pRandom.nextInt(14)),
					x + ox, base + POOL_TOP, z + oz, -ox * 0.03D, 0.022D + pRandom.nextDouble() * 0.018D,
					-oz * 0.03D);
		}
		pLevel.addParticle(HemoParticleData.darkGlow(SOOT), x + (pRandom.nextDouble() - 0.5D) * 0.2D,
				base + FLAME_TOP, z + (pRandom.nextDouble() - 0.5D) * 0.2D, 0.0D, 0.012D, 0.0D);
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, FACE, RITUAL_PHASE, WATERLOGGED);
	}

	@Override
	public RenderShape getRenderShape(BlockState p_60550_) {
		return RenderShape.MODEL;
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return BrazierMount.shape(state.getValue(FACE), state.getValue(FACING));
	}

	/**
	 * Mounts the brazier on the face the player clicked, as a torch or lantern does: onto a block's top, a wall's side
	 * or a ceiling, falling back through the player's other looking directions when that face cannot hold it.
	 */
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState base = this.defaultBlockState().setValue(RITUAL_PHASE, 0)
				.setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
		for (Direction looking : context.getNearestLookingDirections()) {
			AttachFace face = BrazierMount.placement(looking);
			Direction facing = face == AttachFace.WALL ? looking.getOpposite()
					: context.getHorizontalDirection().getOpposite();
			BlockState state = base.setValue(FACE, face).setValue(FACING, facing);
			if (state.canSurvive(context.getLevel(), context.getClickedPos())) return state;
		}
		return null;
	}

	/** A standing brazier keeps today's rules; a sconce needs its wall and a hanging brazier its ceiling. */
	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		AttachFace face = state.getValue(FACE);
		if (face == AttachFace.FLOOR) return true;
		Direction support = BrazierMount.support(face, state.getValue(FACING));
		BlockPos holder = pos.relative(support);
		return face == AttachFace.CEILING ? Block.canSupportCenter(level, holder, Direction.DOWN)
				: level.getBlockState(holder).isFaceSturdy(level, holder, support.getOpposite());
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
		// The mount is kept; only the facing turns.
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

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		return level.isClientSide ? null
				: HemoTickers.create(type, BlockEntityInit.iron_brazier.get(), IronBrazierBlockEntity::serverTick);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING))).setValue(RITUAL_PHASE, 0);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
			BlockPos currentPos, BlockPos facingPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, currentPos);
		if (state.getValue(FACE) != AttachFace.FLOOR
				&& facing == BrazierMount.support(state.getValue(FACE), state.getValue(FACING))
				&& !state.canSurvive(level, currentPos)) {
			// Its wall or ceiling is gone: it falls, and onRemove spills its offering.
			return Blocks.AIR.defaultBlockState();
		}
		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player,
			ItemStack stack) {
		if (worldIn.isClientSide) return InteractionResult.SUCCESS;

		BlockEntity be = worldIn.getBlockEntity(pos);
		IronBrazierBlockEntity brazierTE = be instanceof IronBrazierBlockEntity brazier ? brazier : null;
		if (player.isShiftKeyDown() && brazierTE != null && brazierTE.hasOffering()) {
			ItemStack extracted = brazierTE.extractOffering();
			if (!player.getInventory().add(extracted)) {
				player.drop(extracted, false);
			}
			return InteractionResult.SUCCESS;
		}

		return tryInsertOffering(worldIn, pos, player, stack, brazierTE, isLit(state))
				? InteractionResult.SUCCESS
				: InteractionResult.PASS;
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
						isLit(state), !brazier.hasOffering(), maxAmount)) {
			brazier.resetItemAbsorptionProgress();
			// The fire is drawn out with its blood: the bowl drains over about a second and goes out once it is dry.
			double pooled = brazier.getPooledBlood() > 0.0D ? brazier.getPooledBlood() : BLOOD_TO_LIGHT;
			double drawn = BrazierBloodInteractionRules.drainStep(pooled, maxAmount);
			brazier.setPooledBlood(pooled - drawn);
			if (!BrazierBloodInteractionRules.isDry(pooled - drawn)) {
				if (level.getGameTime() % 8L == 0L) {
					level.playSound(null, pos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 0.3F, 1.35F);
				}
				return drawn;
			}
			level.setBlock(pos, state.setValue(RITUAL_PHASE, 0), Block.UPDATE_ALL);
			double coals = pos.getY() + BrazierMount.drop(BrazierMount.face(state)) + COAL_TOP;
			level.sendParticles(HemoParticleData.darkGlow(SOOT), pos.getX() + 0.5D, coals + 0.15D,
					pos.getZ() + 0.5D, 18, 0.18D, 0.12D, 0.18D, 0.025D);
			level.sendParticles(HemoParticleData.emberSpark(SANGUINE_EMBER, 0.06F, 0.8F, 18), pos.getX() + 0.5D,
					coals, pos.getZ() + 0.5D, 10, 0.2D, 0.04D, 0.2D, 0.02D);
			level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.55F, 1.0F);
			return drawn;
		}
		// An offering is drawn out by whichever brazier rite claims it (Memory, Scar, Living Weapon Graft).
		return BrazierRites.absorb(level, pos, state, player, maxAmount);
	}

	@Override
	public double projectBloodIntoBlock(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player,
			double maxAmount) {
		if (state.getValue(WATERLOGGED) || isLit(state) || maxAmount <= 0.0D
				|| !(level.getBlockEntity(pos) instanceof IronBrazierBlockEntity brazier)) {
			return 0.0D;
		}
		IBloodVolume volume = HemoCapabilityAccess.getBloodVolume(player);
		double poured = volume == null || !volume.isActive() ? 0.0D
				: BrazierBloodInteractionRules.fillStep(brazier.getPooledBlood(), BLOOD_TO_LIGHT, maxAmount,
						volume.getBloodVolume());
		if (poured <= 0.0D) {
			if (level.getGameTime() % 20L == 0L) {
				player.displayClientMessage(Component.literal("No blood answers the brazier."), true);
			}
			return 0.0D;
		}
		// The lighting cost pours into the bowl over about a second; the fire takes once the bowl is full.
		volume.subtractBloodVolume(poured);
		volume.addBloodSpend(poured);
		BloodVolumeEvents.syncVolume(player, volume);
		brazier.setPooledBlood(brazier.getPooledBlood() + poured);
		if (!BrazierBloodInteractionRules.isFull(brazier.getPooledBlood(), BLOOD_TO_LIGHT)) {
			if (level.getGameTime() % 8L == 0L) {
				level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.3F, 1.35F);
			}
			return poured;
		}
		ItemStack offering = brazier.getOfferingDisplayStack();
		level.setBlock(pos, state.setValue(RITUAL_PHASE, 1), Block.UPDATE_ALL);
		HLParticleUtils.spawnPoof(level, pos, HemoParticleData.bloodCell(ParticleColor.BLOOD));
		BrazierSpecialOfferingEffects.spawn(level, pos, offering, true,
				BrazierSpecialOfferingRules.shouldEmitOnIgnition(false, true, !offering.isEmpty()));
		level.playSound(null, pos, SoundEvents.FIRECHARGE_USE, SoundSource.BLOCKS, 0.55F, 0.75F);
		return poured;
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
				Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, offering);
			}
		}
	}

	/**
	 * Returns true if the brazier is lit (any fire state).
	 */
	public static boolean isLit(BlockState state) {
		return state.getBlock() instanceof BrazierBlock && state.getValue(RITUAL_PHASE) > 0;
	}
}
