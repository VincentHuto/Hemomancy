package com.vincenthuto.hemomancy.common.block.functional;

import com.vincenthuto.hemomancy.common.capability.player.kinship.BloodTendencyProvider;
import com.vincenthuto.hemomancy.common.capability.player.kinship.EnumBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.kinship.IBloodTendency;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.IBloodVolume;
import com.vincenthuto.hemomancy.common.encounter.HarbingerSaintEncounterHooks;
import com.vincenthuto.hemomancy.common.init.EffectInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.item.BloodVialItem;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.capa.BloodVolumeServerPacket;
import com.vincenthuto.hemomancy.common.saint.EnumCorpusState;
import com.vincenthuto.hemomancy.common.saint.EnumRejectionEvent;
import com.vincenthuto.hemomancy.common.saint.EnumSaintType;
import com.vincenthuto.hemomancy.common.tile.functional.SaintSarcophagusBlockEntity;

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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition.Builder;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.PacketDistributor;

/**
 * Saint Sarcophagus — the centerpiece of a Sainted Mausoleum.
 * Contains a Preserved Corpus from which players can extract Hallowed Residuum
 * by using a Blood Vial. Extraction success is based on the player's Blood Tendency
 * alignment with the interred saint. Failed extractions trigger rejection events.
 *
 * Corpus states:
 * - DORMANT: approachable, unstable
 * - RESPONSIVE: reacts to player's tendency/alignment
 * - AWAKENED: player has offended it, combat triggered
 */
public class SaintSarcophagusBlock extends Block implements EntityBlock {

	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 14, 16);

	public SaintSarcophagusBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.SOUTH));
	}

	@Override
	protected void createBlockStateDefinition(Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return SHAPE;
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
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SaintSarcophagusBlockEntity(pos, state);
	}

	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
			BlockEntityType<T> type) {
		if (!level.isClientSide) {
			return (lvl, pos, st, be) -> {
				if (be instanceof SaintSarcophagusBlockEntity sarcophagus) {
					sarcophagus.tick();
				}
			};
		}
		return null;
	}

	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player,
			InteractionHand handIn, BlockHitResult result) {
		if (worldIn.isClientSide) {
			return InteractionResult.sidedSuccess(true);
		}

		BlockEntity be = worldIn.getBlockEntity(pos);
		if (!(be instanceof SaintSarcophagusBlockEntity sarcophagus)) {
			return InteractionResult.PASS;
		}

		// If the corpus is AWAKENED, trigger combat encounter and consume block
		if (sarcophagus.getCorpusState() == EnumCorpusState.AWAKENED) {
			player.displayClientMessage(
					Component.literal("The corpus seethes with hostility. It will not receive you.")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
			if (worldIn instanceof ServerLevel serverLevel) {
				HarbingerSaintEncounterHooks.spawnSaintBoss(serverLevel, pos, player);
			}
			worldIn.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.BLOCKS, 0.7f, 1.2f);
			worldIn.removeBlock(pos, false);
			return InteractionResult.CONSUME;
		}

		// Check for cooldown
		if (sarcophagus.isOnCooldown()) {
			player.displayClientMessage(
					Component.literal("The corpus is still... recovering. Wait.")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					true);
			return InteractionResult.CONSUME;
		}

		ItemStack stack = player.getItemInHand(handIn);

		// Empty-hand interaction: inspect the sarcophagus
		if (stack.isEmpty()) {
			showSarcophagusInfo(player, sarcophagus);
			return InteractionResult.CONSUME;
		}

		// Blood Vial interaction: attempt extraction
		if (stack.getItem() instanceof BloodVialItem && stack.hasTag()
				&& stack.getOrCreateTag().getBoolean(BloodVialItem.TAG_STATE)) {
			attemptExtraction(worldIn, pos, player, handIn, stack, sarcophagus);
			return InteractionResult.CONSUME;
		}

		return InteractionResult.PASS;
	}

	private void showSarcophagusInfo(Player player, SaintSarcophagusBlockEntity sarcophagus) {
		EnumSaintType saint = sarcophagus.getSaintType();
		player.displayClientMessage(
				Component.literal("A sarcophagus of Saint " + saint.getDisplayName() + ".")
						.withStyle(ChatFormatting.GOLD),
				false);
		player.displayClientMessage(
				Component.literal("State: " + sarcophagus.getCorpusState().getDisplayName())
						.withStyle(ChatFormatting.GRAY),
				false);
		player.displayClientMessage(
				Component.literal("Aligned tendencies: " + saint.getPrimaryTendency().name()
						+ " + " + saint.getSecondaryTendency().name())
						.withStyle(ChatFormatting.DARK_PURPLE),
				false);
	}

	/**
	 * Core extraction mechanic. The chance of the sarcophagus rejecting the user
	 * is based on their current Blood Tendency and whether or not it aligns with
	 * the saint they are trying to extract from.
	 */
	private void attemptExtraction(Level worldIn, BlockPos pos, Player player, InteractionHand hand,
			ItemStack vialStack, SaintSarcophagusBlockEntity sarcophagus) {
		EnumSaintType saint = sarcophagus.getSaintType();

		// Transition to RESPONSIVE on first interaction
		if (sarcophagus.getCorpusState() == EnumCorpusState.DORMANT) {
			sarcophagus.setCorpusState(EnumCorpusState.RESPONSIVE);
			player.displayClientMessage(
					Component.literal("The corpus stirs... it senses your blood.")
							.withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC),
					false);
			worldIn.playSound(null, pos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0f, 0.5f);
			spawnAmbienceParticles(worldIn, pos);
		}

		// Calculate success chance based on player's tendency alignment
		IBloodTendency tendency = player.getCapability(BloodTendencyProvider.TENDENCY_CAPA).orElse(null);
		if (tendency == null) {
			return;
		}

		float primaryAlign = tendency.getAlignmentByTendency(saint.getPrimaryTendency());
		float secondaryAlign = tendency.getAlignmentByTendency(saint.getSecondaryTendency());
		float successChance = saint.getBaseSuccessChance(primaryAlign, secondaryAlign);

		// Opposition penalty: if player's strongest tendency is opposed to the saint
		EnumBloodTendency strongest = getStrongestTendency(tendency);
		if (strongest != null && saint.isOpposed(strongest)) {
			successChance *= 0.80f;
		}

		// Marked by Canon debuff further reduces chances
		if (player.hasEffect(EffectInit.marked_by_canon.get())) {
			int amplifier = player.getEffect(EffectInit.marked_by_canon.get()).getAmplifier();
			successChance *= Math.max(0.10f, 0.50f - (amplifier * 0.10f));
		}

		// Consume the blood vial
		vialStack.shrink(1);
		if (vialStack.isEmpty()) {
			player.setItemInHand(hand, ItemStack.EMPTY);
		}

		// Set extraction cooldown (10 seconds)
		sarcophagus.setCooldownTicks(200);

		// Roll for success
		float roll = worldIn.getRandom().nextFloat();
		if (roll < successChance) {
			onExtractionSuccess(worldIn, pos, player, saint);
		} else {
			sarcophagus.incrementExtractionAttempts();
			onExtractionFailure(worldIn, pos, player, saint);
		}
	}

	private void onExtractionSuccess(Level worldIn, BlockPos pos, Player player, EnumSaintType saint) {
		ItemStack residuum = new ItemStack(getHallowedResiduum(saint));
		if (!player.getInventory().add(residuum)) {
			player.drop(residuum, false);
		}

		player.displayClientMessage(
				Component.literal("The corpus yields its essence. You receive Hallowed Residuum of "
						+ saint.getDisplayName() + ".")
						.withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD),
				false);
		worldIn.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 0.8f);
		spawnSuccessParticles(worldIn, pos);
	}

	private void onExtractionFailure(Level worldIn, BlockPos pos, Player player, EnumSaintType saint) {
		EnumRejectionEvent rejection = rollRejectionEvent(worldIn, player);

		switch (rejection) {
		case HEMATIC_REVERSAL:
			applyHematicReversal(worldIn, pos, player);
			break;
		case MEMORY_SCRAMBLE:
			applyMemoryScramble(worldIn, pos, player);
			break;
		case MANIFESTATION:
			applyManifestation(worldIn, pos, player, saint);
			break;
		case MARKED_BY_CANON:
			applyMarkedByCanon(worldIn, pos, player);
			break;
		}
	}

	private EnumRejectionEvent rollRejectionEvent(Level worldIn, Player player) {
		float roll = worldIn.getRandom().nextFloat();
		boolean alreadyMarked = player.hasEffect(EffectInit.marked_by_canon.get());

		if (alreadyMarked) {
			// Already marked: higher chance of severe events
			if (roll < 0.35f) return EnumRejectionEvent.MANIFESTATION;
			if (roll < 0.60f) return EnumRejectionEvent.HEMATIC_REVERSAL;
			if (roll < 0.85f) return EnumRejectionEvent.MEMORY_SCRAMBLE;
			return EnumRejectionEvent.MARKED_BY_CANON;
		} else {
			if (roll < 0.30f) return EnumRejectionEvent.HEMATIC_REVERSAL;
			if (roll < 0.55f) return EnumRejectionEvent.MEMORY_SCRAMBLE;
			if (roll < 0.75f) return EnumRejectionEvent.MANIFESTATION;
			return EnumRejectionEvent.MARKED_BY_CANON;
		}
	}

	/**
	 * Hematic Reversal — blood drained into the corpse. Drains 30% of
	 * the player's current blood volume.
	 */
	private void applyHematicReversal(Level worldIn, BlockPos pos, Player player) {
		player.displayClientMessage(
				Component.literal("Your blood surges toward the corpse! The saint drinks deeply.")
						.withStyle(ChatFormatting.DARK_RED),
				false);

		IBloodVolume volume = player.getCapability(BloodVolumeProvider.VOLUME_CAPA).orElse(null);
		if (volume != null) {
			double drainAmount = volume.getBloodVolume() * 0.30;
			volume.drain(drainAmount);
			if (player instanceof ServerPlayer sp) {
				PacketHandler.CHANNELBLOODVOLUME.send(
						PacketDistributor.PLAYER.with(() -> sp),
						new BloodVolumeServerPacket(volume));
			}
		}

		player.hurt(player.damageSources().magic(), 4.0f);
		worldIn.playSound(null, pos, SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 1.0f, 0.5f);
		spawnRejectionParticles(worldIn, pos);
	}

	/**
	 * Memory Scramble — equipped manipulations reshuffle/corrupt.
	 * Applies nausea and brief blindness to represent cognitive disruption.
	 */
	private void applyMemoryScramble(Level worldIn, BlockPos pos, Player player) {
		player.displayClientMessage(
				Component.literal("Fragmented memories assault your mind! Your manipulations feel... wrong.")
						.withStyle(ChatFormatting.LIGHT_PURPLE),
				false);

		player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200, 0));
		player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0));

		worldIn.playSound(null, pos, SoundEvents.ENDERMAN_SCREAM, SoundSource.BLOCKS, 0.5f, 2.0f);
		spawnRejectionParticles(worldIn, pos);
	}

	/**
	 * Manifestation — partial Saint entity spawns.
	 * Deals significant damage and knockback from a wrathful presence.
	 */
	private void applyManifestation(Level worldIn, BlockPos pos, Player player, EnumSaintType saint) {
		player.displayClientMessage(
				Component.literal("Saint " + saint.getDisplayName()
						+ " partially manifests! A wrathful presence fills the mausoleum.")
						.withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
				false);

		player.hurt(player.damageSources().magic(), 10.0f);
		double knockX = (player.getX() - (pos.getX() + 0.5)) * 0.8;
		double knockZ = (player.getZ() - (pos.getZ() + 0.5)) * 0.8;
		player.push(knockX, 0.5, knockZ);

		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					50, 1.5, 2.0, 1.5, 0.05);
		}

		worldIn.playSound(null, pos, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.BLOCKS, 0.8f, 0.7f);
	}

	/**
	 * Marked by Canon — persistent debuff; future mausoleums become more hostile.
	 * Stacks amplifier on repeated marks, lasting 5 minutes.
	 */
	private void applyMarkedByCanon(Level worldIn, BlockPos pos, Player player) {
		player.displayClientMessage(
				Component.literal("The saint's judgment sears into your blood. You are Marked by Canon.")
						.withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD),
				false);

		MobEffectInstance existing = player.getEffect(EffectInit.marked_by_canon.get());
		int newAmplifier = existing != null ? existing.getAmplifier() + 1 : 0;
		player.addEffect(new MobEffectInstance(EffectInit.marked_by_canon.get(), 6000, newAmplifier, false, true));

		worldIn.playSound(null, pos, SoundEvents.BELL_RESONATE, SoundSource.BLOCKS, 1.5f, 0.3f);
		spawnRejectionParticles(worldIn, pos);
	}

	private void spawnAmbienceParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SOUL,
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
					15, 0.5, 0.5, 0.5, 0.02);
		}
	}

	private void spawnSuccessParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					30, 0.5, 1.0, 0.5, 0.05);
		}
	}

	private void spawnRejectionParticles(Level worldIn, BlockPos pos) {
		if (worldIn instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
					pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5,
					20, 0.5, 0.5, 0.5, 0.1);
		}
	}

	private EnumBloodTendency getStrongestTendency(IBloodTendency tendency) {
		EnumBloodTendency strongest = null;
		float highestVal = 0;
		for (EnumBloodTendency t : EnumBloodTendency.values()) {
			float val = tendency.getAlignmentByTendency(t);
			if (val > highestVal) {
				highestVal = val;
				strongest = t;
			}
		}
		return strongest;
	}

	private net.minecraft.world.item.Item getHallowedResiduum(EnumSaintType saint) {
		switch (saint) {
		case HEMORATH:
			return ItemInit.hallowed_residuum_hemorath.get();
		case SERAPHAE:
			return ItemInit.hallowed_residuum_seraphae.get();
		case PUTRICIEL:
			return ItemInit.hallowed_residuum_putriciel.get();
		case VELORUM:
			return ItemInit.hallowed_residuum_velorum.get();
		default:
			return ItemInit.hallowed_residuum_hemorath.get();
		}
	}
}
