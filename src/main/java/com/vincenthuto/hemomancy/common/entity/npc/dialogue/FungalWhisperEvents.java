package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import net.neoforged.fml.common.EventBusSubscriber;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * Periodically triggers mysterious "fungal whisper" dialogues for players who
 * have reached initiation tiers 4â€“7 (Adept through Archon). The whispers hint
 * at the potential fungal origins of the blood infection itself.
 * <p>
 * At degree 4 the whispers are extremely rare and subliminal â€” barely
 * perceptible intrusive thoughts that plant seeds of doubt. As the player's
 * degree increases, the interval shortens and the content grows more explicit.
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class FungalWhisperEvents {

	/**
	 * Base interval in ticks between whisper checks.
	 * <ul>
	 *   <li>Adept (4): every ~30 minutes (36000 ticks) â€” very rare, subliminal</li>
	 *   <li>Illuminatus (5): every ~20 minutes (24000 ticks)</li>
	 *   <li>Sanctified (6): every ~15 minutes (18000 ticks)</li>
	 *   <li>Archon (7): every ~10 minutes (12000 ticks)</li>
	 * </ul>
	 */
	private static final int ADEPT_INTERVAL = 36000;
	private static final int ILLUMINATUS_INTERVAL = 24000;
	private static final int SANCTIFIED_INTERVAL = 18000;
	private static final int ARCHON_INTERVAL = 12000;

	/** Number of dialogue variants per tier. */
	private static final int VARIANT_COUNT = 3;

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {

		Player player = event.getEntity();
		if (player.level().isClientSide) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;

		// Only check periodically â€” use a cheap modulo before doing capability lookups
		// Stagger per-player using their entity ID to avoid all players ticking on the same frame
		int stagger = player.getId() * 137;
		if ((player.tickCount + stagger) % 200 != 0) return;

		int degree = HemoCapabilityAccess.getPlayerDegreeNumber(player);
		if (degree < 4) return;

		// Ensure the player has active blood
		boolean hasBlood = HemoCapabilityAccess.getBloodVolume(player)
				.map(vol -> vol.isActive())
				.orElse(false);
		if (!hasBlood) return;

		// Don't trigger if the player already has a screen open
		// (the actual screen check happens client-side, but we avoid spamming)

		int interval = switch (degree) {
			case 4 -> ADEPT_INTERVAL;
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

		PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
	}
}
