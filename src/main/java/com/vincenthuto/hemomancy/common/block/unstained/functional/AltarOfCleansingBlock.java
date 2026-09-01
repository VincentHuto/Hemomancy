package com.vincenthuto.hemomancy.common.block.unstained.functional;

import com.vincenthuto.hemomancy.common.block.shared.IMultiBlock;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedPacingRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.tile.unstained.functional.AltarOfCleansingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

/**
 * Altar of Cleansing — a sacred altar found in Unstained temples, blessed by
 * Our Lady of Still Waters. When a player on the Unstained path interacts with it
 * using Tears of Silthmere, they receive a large one-time purity boost and
 * advancement progress. The altar can only be used once per player.
 */
public class AltarOfCleansingBlock extends Block implements EntityBlock, IMultiBlock, SimpleWaterloggedBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE = Shapes.block();

	/** Filler offset: 1×2×1 — one filler block directly above the base. */
	private static final BlockPos[] FILLER_OFFSETS = new BlockPos[] {
			new BlockPos(0, 1, 0)
	};

	/** One-time purity boost granted by the altar. */
	private static final float ALTAR_PURITY_BOOST = 25.0f;

	public AltarOfCleansingBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(WATERLOGGED, false));
	}

	@Override
	public BlockPos[] getFillerOffsets() {
		return FILLER_OFFSETS;
	}

	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
		return Shapes.empty();
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
		return true;
	}

	@Override
	public float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
		return 1.0F;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockPos pos = context.getClickedPos();
		Level level = (Level) context.getLevel();
		if (pos.getY() + 1 <= level.getMaxBuildHeight() && canPlaceMultiBlock(level, pos)) {
			return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
		}
		return null;
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state,
			@Nullable LivingEntity placer, ItemStack stack) {
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
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos,
			boolean isMoving) {
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new AltarOfCleansingBlockEntity(pos, state);
	}

	private InteractionResult handleInteraction(Level worldIn, BlockPos pos, Player player, ItemStack stack) {
		if (worldIn.isClientSide) {
			return InteractionResult.SUCCESS;
		}

		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(unstained -> {
			if (!unstained.hasBegunPurification()) {
				player.displayClientMessage(
						Component.translatable("hemomancy.altar.not_on_path"), false);
				return;
			}

			if (stack.getItem() == ItemInit.tears_of_silthmere.get()) {
				handleTearsOfSilthmere(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.lethean_poppy_wreath.get()) {
				handlePoppyWreath(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.silver_chalice.get()) {
				handleSilverChalice(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.pallid_icon.get()) {
				handlePallidIcon(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.lethean_brew.get()) {
				handleLetheanBrew(worldIn, pos, player, stack, unstained);
			} else {
				// Empty-hand interaction — show lore
				player.displayClientMessage(
						Component.translatable("hemomancy.altar.lore"), false);
			}
		});

		return InteractionResult.SUCCESS;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(worldIn, pos, player, ItemStack.EMPTY);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleInteraction(worldIn, pos, player, stack);
		return ItemInteractionResult.SUCCESS;
	}

	private void handleTearsOfSilthmere(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (unstained.hasUsedAltarOfCleansing()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.altar.already_blessed"), false);
			return;
		}

		// One-time large purity boost
		stack.shrink(1);
		unstained.addPurity(ALTAR_PURITY_BOOST);
		unstained.setUsedAltarOfCleansing(true);
		unstained.addAdvancementEarned();

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.blessed"), false);
		worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1.2f);
		worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
		spawnBlessingParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
			com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.grantIfNotDone(
					serverPlayer, com.vincenthuto.hemomancy.common.event.UnstainedAdvancementGranter.ADV_BLESSED_BY_ALTAR);
		}
	}

	private void handlePoppyWreath(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (unstained.hasOfferedPoppyWreath()) {
			player.displayClientMessage(Component.translatable("hemomancy.altar.wreath_already_offered"), false);
			return;
		}
		stack.shrink(1);
		unstained.addPurity(5.0f);
		unstained.setOfferedPoppyWreath(true);

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.wreath_offered"), false);
		worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
		spawnPurityParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void handleSilverChalice(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		// Offering a chalice at the altar grants clarity progress (requires clarity unlocked)
		if (!unstained.hasClarityUnlocked()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.altar.clarity_not_unlocked"), false);
			return;
		}
		float reward = UnstainedPacingRules.silverChaliceReward(unstained.hasOfferedSilverChalice());
		if (reward <= 0.0F) {
			player.displayClientMessage(Component.translatable("hemomancy.altar.chalice_already_offered"), false);
			return;
		}
		stack.shrink(1);
		unstained.addClarity(reward);
		unstained.setOfferedSilverChalice(true);

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.chalice_offered"), false);
		worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.2f);
		spawnClarityParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void handlePallidIcon(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		// One-time rare offering: grants +10 clarity (requires clarity unlocked)
		if (!unstained.hasClarityUnlocked()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.altar.clarity_not_unlocked"), false);
			return;
		}
		if (unstained.hasOfferedPallidIcon()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.altar.icon_already_offered"), false);
			return;
		}
		stack.shrink(1);
		unstained.setOfferedPallidIcon(true);
		unstained.addClarity(10.0f);

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.icon_offered"), false);
		worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 1.0f);
		worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
		spawnClarityParticles(worldIn, pos);
		spawnBlessingParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void handleLetheanBrew(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		float reward = UnstainedPacingRules.letheanBrewReward(unstained.getLetheanBrewOfferings());
		if (reward <= 0.0F) {
			player.displayClientMessage(Component.translatable("hemomancy.altar.brew_exhausted"), false);
			return;
		}
		stack.shrink(1);
		unstained.addPurity(reward);
		unstained.setLetheanBrewOfferings(unstained.getLetheanBrewOfferings() + 1);

		player.displayClientMessage(
				Component.translatable("hemomancy.altar.brew_offered"), false);
		worldIn.playSound(null, pos, SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 1.0f, 1.0f);
		worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 0.8f);
		spawnPurityParticles(worldIn, pos);

		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void spawnBlessingParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					40, 0.5, 1.0, 0.5, 0.05);
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnPurityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					15, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnClarityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}
	@Override
	public FluidState getFluidState(BlockState state) {
		return WaterloggedBlockSupport.fluidState(state);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level,
			BlockPos pos, BlockPos neighborPos) {
		WaterloggedBlockSupport.scheduleWaterTick(state, level, pos);
		return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
	}

}
