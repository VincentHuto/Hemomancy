package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.player.degree.InitiatoryDegreeProvider;
import com.vincenthuto.hemomancy.common.capability.player.volume.BloodVolumeProvider;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

/**
 * Periodically triggers mysterious "fungal whisper" dialogues for players who
 * have reached initiation tiers 5–7 (Illuminatus, Sanctified, Archon). The
 * whispers hint at the potential fungal origins of the blood infection itself.
 * <p>
 * The interval between whispers shortens as the player's degree increases,
 * and each tier has 3 variant messages to avoid repetition.
 */
@Mod.EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FungalWhisperEvents {

	/**
	 * Base interval in ticks between whisper checks.
	 * <ul>
	 *   <li>Illuminatus (5): every ~20 minutes (24000 ticks)</li>
	 *   <li>Sanctified (6): every ~15 minutes (18000 ticks)</li>
	 *   <li>Archon (7): every ~10 minutes (12000 ticks)</li>
	 * </ul>
	 */
	private static final int ILLUMINATUS_INTERVAL = 24000;
	private static final int SANCTIFIED_INTERVAL = 18000;
	private static final int ARCHON_INTERVAL = 12000;

	/** Number of dialogue variants per tier. */
	private static final int VARIANT_COUNT = 3;

	@SubscribeEvent
	public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;

		Player player = event.player;
		if (player.level().isClientSide) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;

		// Only check periodically — use a cheap modulo before doing capability lookups
		// Stagger per-player using their entity ID to avoid all players ticking on the same frame
		int stagger = player.getId() * 137;
		if ((player.tickCount + stagger) % 200 != 0) return;

		int degree = InitiatoryDegreeProvider.getPlayerDegreeNumber(player);
		if (degree < 5) return;

		// Ensure the player has active blood
		boolean hasBlood = player.getCapability(BloodVolumeProvider.VOLUME_CAPA)
				.map(vol -> vol.isActive())
				.orElse(false);
		if (!hasBlood) return;

		// Don't trigger if the player already has a screen open
		// (the actual screen check happens client-side, but we avoid spamming)

		int interval = switch (degree) {
			case 5 -> ILLUMINATUS_INTERVAL;
			case 6 -> SANCTIFIED_INTERVAL;
			default -> ARCHON_INTERVAL;
		};

		// Check if enough time has passed since the last whisper opportunity
		// We use tickCount modulo interval, accounting for stagger
		if ((player.tickCount + stagger) % interval != 0) return;

		// Choose a variant based on the player's tick count for variety
		int variant = (int) ((player.tickCount / interval) % VARIANT_COUNT);

		DialogueTree tree = FungalWhisperDialogueTrees.forDegree(degree, variant);

		PacketHandler.CHANNELBLOODVOLUME.send(
				PacketDistributor.PLAYER.with(() -> serverPlayer),
				new OpenDialoguePacket(tree));
	}
}
