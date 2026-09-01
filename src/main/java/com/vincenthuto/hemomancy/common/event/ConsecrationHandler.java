package com.vincenthuto.hemomancy.common.event;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.init.BlockInit;
import com.vincenthuto.hemomancy.common.init.ItemInit;
import com.vincenthuto.hemomancy.common.mission.unstained.UnstainedObservances;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.Map;

/**
 * Consecration mechanic: Unstained players at CLEANSING+ purity (50+) can
 * right-click blood-faction blocks with Consecrated Copper Ingot to convert
 * them into purified versions. Each consecration consumes one ingot and
 * grants a small purity boost (+0.5).
 *
 * <h3>Conversion Table</h3>
 * <ul>
 *   <li>Venous Stone â†’ Cleansed Stone</li>
 *   <li>Sanguine Glass â†’ Cleansed Sanguine Glass</li>
 *   <li>Infested Venous Stone â†’ Cleansed Stone</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID)
public class ConsecrationHandler {

	/** Purity threshold to use consecration (CLEANSING = 50). */
	private static final float CONSECRATION_PURITY_THRESHOLD = 50.0f;
	/** Purity granted per block consecrated. */
	private static final float PURITY_PER_CONSECRATION = 0.5f;

	/**
	 * Lazy conversion map â€” built on first use from BlockInit registries.
	 * Maps blood-faction block â†’ cleansed block.
	 */
	private static Map<Block, Block> CONVERSIONS;

	private static Map<Block, Block> getConversions() {
		if (CONVERSIONS == null) {
			CONVERSIONS = Map.of(
					BlockInit.venous_stone.get(), BlockInit.cleansed_stone.get(),
					BlockInit.sanguine_glass.get(), BlockInit.cleansed_sanguine_glass.get(),
					BlockInit.infested_venous_stone.get(), BlockInit.cleansed_stone.get()
			);
		}
		return CONVERSIONS;
	}

	@SubscribeEvent
	public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
		Player player = event.getEntity();
		Level level = event.getLevel();
		if (level.isClientSide) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;

		ItemStack held = event.getItemStack();
		if (held.getItem() != ItemInit.consecrated_copper_ingot.get()) return;

		BlockPos pos = event.getPos();
		BlockState state = level.getBlockState(pos);
		Block targetBlock = getConversions().get(state.getBlock());

		if (targetBlock == null) return;

		// Check purity requirement
		boolean allowed = HemoCapabilityAccess.getUnstainedProgress(serverPlayer).map(progress -> {
			boolean novitiate = (progress.getAcceptedObservances()
					& UnstainedObservances.Observance.NOVITIATE_CLEAN_LABOR.mask()) != 0;
			if (!progress.hasBegunPurification()) return novitiate;
			EnumPurityStage stage = EnumPurityStage.byPurity(progress.getPurity());
			return stage.getLevel() >= EnumPurityStage.CLEANSING.getLevel();
		}).orElse(false);

		if (!allowed) {
			serverPlayer.displayClientMessage(
					Component.translatable("hemomancy.consecration.not_ready")
							.withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC),
					true);
			return;
		}

		// Perform the conversion
		level.setBlock(pos, targetBlock.defaultBlockState(), 3);
		if (!player.isCreative()) {
			held.shrink(1);
		}

		// Grant purity
		HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
			if (progress.hasBegunPurification()) progress.addPurity(PURITY_PER_CONSECRATION);
			else UnstainedObservances.recordConsecratedBlock(serverPlayer);
			com.vincenthuto.hemomancy.common.capability.player.unstained.UnstainedProgressEvents.syncProgress(serverPlayer, progress);
		});

		// Visual & audio feedback
		if (level instanceof ServerLevel serverLevel) {
			serverLevel.sendParticles(ParticleTypes.SCRAPE,
					pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
					12, 0.4, 0.4, 0.4, 0.02);
			serverLevel.sendParticles(ParticleTypes.END_ROD,
					pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
					5, 0.3, 0.5, 0.3, 0.01);
		}
		level.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1.0f, 1.2f);
		level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 0.5f, 1.5f);

		serverPlayer.displayClientMessage(
				Component.translatable("hemomancy.consecration.success")
						.withStyle(ChatFormatting.AQUA, ChatFormatting.ITALIC),
				true);

		event.setCancellationResult(InteractionResult.SUCCESS);
		event.setCanceled(true);
	}
}
