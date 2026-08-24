package com.vincenthuto.hemomancy.common.block.harbinger.functional;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.PathMutualExclusionHelper;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumInitiatoryDegree;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.IUnstainedProgress;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedEntryRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedAccessRules;
import com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedPhase;
import com.vincenthuto.hemomancy.common.capability.player.unstained.stillart.KnownStillArtEvents;
import com.vincenthuto.hemomancy.common.event.MachineAccessEvents;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.init.StillArtInit;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.harbinger.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.tile.functional.UnstainedPodiumBlockEntity;
import net.minecraft.ChatFormatting;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.vincenthuto.hemomancy.common.block.shared.WaterloggedBlockSupport;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class UnstainedPodiumBlock extends Block implements EntityBlock, SimpleWaterloggedBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	private static final VoxelShape SHAPE_N = Block.box(2, 0, 2, 14, 14, 14);


	public UnstainedPodiumBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH).setValue(WATERLOGGED, false));

	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING, WATERLOGGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE_N;
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, WaterloggedBlockSupport.waterloggedForPlacement(context));
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
		return new UnstainedPodiumBlockEntity(arg0, arg1);
	}

	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	private InteractionResult handleInteraction(BlockState state, Level worldIn, BlockPos pos, Player player, ItemStack stack) {

		worldIn.playSound(player, pos, SoundEvents.ZOMBIE_AMBIENT, SoundSource.BLOCKS, 0.25f, 1f);
		if (!player.isShiftKeyDown()) {
			if (stack.getItem() == ItemInit.sanguine_conduit.get()) {
				stack.shrink(1);
			}

			if (stack.getItem() == ItemInit.scrying_dish.get()) {
				worldIn.destroyBlock(pos, false);
				stack.shrink(1);
				worldIn.setBlockAndUpdate(pos, BlockInit.scrying_podium.get().defaultBlockState()
						.setValue(ScryingPodiumBlock.WATERLOGGED, state.getValue(WATERLOGGED)));
				if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
					MachineAccessEvents.awardMachineCrafted(serverPlayer, BlockInit.scrying_podium.get());
				}
			}

			// Unstained path interactions — server-side only
			if (!worldIn.isClientSide) {
				handleUnstainedInteraction(worldIn, pos, player, stack);
			}
		}

		return InteractionResult.SUCCESS;

	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player,
			BlockHitResult result) {
		return handleInteraction(state, worldIn, pos, player, ItemStack.EMPTY);
	}

	@Override
	protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos,
			Player player, InteractionHand handIn, BlockHitResult result) {
		handleInteraction(state, worldIn, pos, player, stack);
		return ItemInteractionResult.SUCCESS;
	}

	private void handleUnstainedInteraction(Level worldIn, BlockPos pos, Player player, ItemStack stack) {
		boolean maySeekCure = HemoCapabilityAccess.getInitiatoryDegree(player)
				.map(degree -> UnstainedEntryRules.canBeginCure(
						degree.hasFoundedBloodline(), degree.isFounderIntegrationSevered()))
				.orElse(true);
		HemoCapabilityAccess.getUnstainedProgress(player).ifPresent(unstained -> {
			if (!unstained.hasBegunPurification() && !maySeekCure) {
				player.displayClientMessage(Component.literal(
						"The covenant you founded is rooted through every vein. Ordinary cure can no longer reach you.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC), false);
				return;
			}

			if (stack.getItem() == ItemInit.hemolytic_solution.get()) {
				handleHemolyticSolution(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.consecrated_copper_ingot.get()) {
				handleConsecratedCopper(worldIn, pos, player, stack, unstained);
			} else if (stack.getItem() == ItemInit.hemolytic_plating.get()) {
				handleHemolyticPlating(worldIn, pos, player, stack, unstained);
			} else {
				// Empty-hand or unrecognized item — show purity/clarity stage lore
				showUnstainedProgress(player, unstained);
			}
		});
	}

	private void handleHemolyticSolution(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		var degree = HemoCapabilityAccess.getInitiatoryDegree(player).orElse(null);
		var blood = HemoCapabilityAccess.getBloodVolume(player).orElse(null);
		boolean eligible = degree != null && blood != null && UnstainedEntryRules.canSuppressForCure(
				blood.isActive(), degree.getDegreeNumber(), degree.hasFoundedBloodline(),
				degree.isFounderIntegrationSevered());
		if (!eligible) {
			player.displayClientMessage(Component.literal(blood == null || !blood.isActive()
					? "You carry no active infection. Speak with a Zealot and take the vows."
					: "Your infection lies beyond ordinary suppression.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
			return;
		}
		if (!unstained.isInfectionSuppressed()) {
			stack.shrink(1);
			unstained.setInfectionSuppressed(true);
			player.displayClientMessage(Component.literal(
					"The Podium quiets the infection. Seek Lethean Baptism before the silence breaks.")
					.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
			worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.0f, 1.0f);
			spawnPurityParticles(worldIn, pos);
			syncUnstainedProgress(worldIn, player, unstained);
		} else {
			player.displayClientMessage(Component.literal("The infection is already held in suppression.")
					.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), false);
		}
	}

	private void handleConsecratedCopper(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		boolean vowsComplete = UnstainedAccessRules.hasCompletedNovitiateVows(unstained.getClaimedObservances());
		boolean cleanBlood = HemoCapabilityAccess.getBloodVolume(player)
				.map(volume -> !volume.isActive() && volume.getBloodVolume() <= 0).orElse(true);
		if (!(unstained.isBaselineRestored() || vowsComplete) || !cleanBlood) return;
		if (unstained.isClarityPrepared() || unstained.hasClarityUnlocked()) return;
		stack.shrink(1);
		unstained.setClarityPrepared(true);
		player.displayClientMessage(Component.literal(
				"Consecrated Copper steadies the next crossing. The Rite of Clarity Ascension may now be performed.")
				.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC), false);
		worldIn.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.5f, 0.8f);
		spawnClarityParticles(worldIn, pos);
		syncUnstainedProgress(worldIn, player, unstained);
	}

	private void handleHemolyticPlating(Level worldIn, BlockPos pos, Player player, ItemStack stack,
			IUnstainedProgress unstained) {
		if (!unstained.hasClarityUnlocked()) {
			// Clarity not yet unlocked
			return;
		}
		if (unstained.isEnlightened()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.already_enlightened"), false);
			return;
		}
		// Add clarity progress
		stack.shrink(1);
		unstained.addClarity(15.0f);
		worldIn.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.2f);
		spawnClarityParticles(worldIn, pos);
		if (unstained.isEnlightened()) {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.enlightened"), false);
		} else {
			player.displayClientMessage(
					Component.translatable("hemomancy.unstained.clarity_progress",
							unstained.getClarity()),
					false);
		}
		syncUnstainedProgress(worldIn, player, unstained);
	}

	private void spawnPurityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void spawnClarityParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					30, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private void syncUnstainedProgress(Level worldIn, Player player, IUnstainedProgress unstained) {
		if (!worldIn.isClientSide && player instanceof ServerPlayer serverPlayer) {
			UnstainedProgressEvents.syncProgress(serverPlayer, unstained);
		}
	}

	private void showUnstainedProgress(Player player, IUnstainedProgress unstained) {
		UnstainedPhase phase = UnstainedAccessRules.phase(unstained);
		if (phase == UnstainedPhase.OUTSIDER) {
			player.displayClientMessage(
					Component.literal("The podium hums faintly. Speak with a Zealot to seek treatment or take the vows.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
			return;
		}

		if (phase == UnstainedPhase.NOVITIATE) {
			player.displayClientMessage(Component.literal("Novitiate vows: "
					+ UnstainedAccessRules.completedNovitiateVows(unstained.getClaimedObservances()) + "/5")
					.withStyle(ChatFormatting.AQUA), false);
			return;
		}
		if (phase == UnstainedPhase.CLEANSED_UNPLEDGED) {
			player.displayClientMessage(Component.literal("Your baseline is restored. Offer Consecrated Copper here, then perform Clarity Ascension to pledge.")
					.withStyle(ChatFormatting.AQUA), false);
			return;
		}

		EnumPurityStage purityStage = EnumPurityStage.byPurity(unstained.getPurity());
		player.displayClientMessage(
				Component.literal("Purity: " + String.format("%.0f", unstained.getPurity())
						+ "/100 — " + purityStage.getTitle())
						.withStyle(ChatFormatting.AQUA),
				false);

		if (unstained.hasClarityUnlocked()) {
			EnumClarityStage clarityStage = EnumClarityStage.byClarity(unstained.getClarity());
			player.displayClientMessage(
					Component.literal("Clarity: " + String.format("%.0f", unstained.getClarity())
							+ "/100 — " + clarityStage.getTitle())
							.withStyle(ChatFormatting.WHITE),
					false);
		} else if (phase == UnstainedPhase.CURE_READY) {
			player.displayClientMessage(
					Component.literal("Purity is complete. Perform Closed Vein to make the cleansing irreversible.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					false);
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
