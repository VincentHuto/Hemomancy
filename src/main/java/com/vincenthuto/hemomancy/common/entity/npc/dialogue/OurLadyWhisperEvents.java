package com.vincenthuto.hemomancy.common.entity.npc.dialogue;

import com.vincenthuto.hemomancy.Hemomancy;
import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumClarityStage;
import com.vincenthuto.hemomancy.common.capability.player.unstained.EnumPurityStage;
import com.vincenthuto.hemomancy.common.network.PacketHandler;
import com.vincenthuto.hemomancy.common.network.dialogue.OpenDialoguePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Periodically triggers Our Lady of Still Waters' whispers for players
 * progressing along the Unstained path. The whispers begin once the player
 * reaches TAINTED purity and continue through every clarity stage.
 *
 * <p>Whisper frequency increases with advancement along the path:
 * <ul>
 *   <li><b>TAINTED</b> (purity 25+): every ~45 min â€” barely perceptible, like water far away</li>
 *   <li><b>CLEANSING</b> (purity 50+): every ~30 min â€” more audible, gentle encouragement</li>
 *   <li><b>ABSOLVED</b> (purity 75+): every ~20 min â€” clear, personal, slightly urgent</li>
 *   <li><b>PURIFIED</b> (purity 100, pre-clarity): every ~15 min â€” direct, guides toward the Rite</li>
 *   <li><b>DISCERNING</b> (clarity 25+): every ~30 min â€” relief, warmth, early revelations</li>
 *   <li><b>VIGILANT</b> (clarity 50+): every ~20 min â€” deeper, hints about the world's true state</li>
 *   <li><b>RESOLUTE</b> (clarity 75+): every ~15 min â€” serious, begins to reveal her own limits</li>
 *   <li><b>ENLIGHTENED</b> (clarity 100): every ~10 min â€” final truths, she is ancient and cold</li>
 * </ul>
 */
@EventBusSubscriber(modid = Hemomancy.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public class OurLadyWhisperEvents {

	// Intervals in ticks between whisper opportunities
	private static final int TAINTED_INTERVAL     = 54000; // ~45 min
	private static final int CLEANSING_INTERVAL   = 36000; // ~30 min
	private static final int ABSOLVED_INTERVAL    = 24000; // ~20 min
	private static final int PURIFIED_INTERVAL    = 18000; // ~15 min
	private static final int DISCERNING_INTERVAL  = 36000; // ~30 min
	private static final int VIGILANT_INTERVAL    = 24000; // ~20 min
	private static final int RESOLUTE_INTERVAL    = 18000; // ~15 min
	private static final int ENLIGHTENED_INTERVAL = 12000; // ~10 min

	/** Number of dialogue variants per stage. */
	private static final int VARIANT_COUNT = 3;

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {

		Player player = event.getEntity();
		if (player.level().isClientSide) return;
		if (!(player instanceof ServerPlayer serverPlayer)) return;

		// Stagger per-player so all players don't whisper on the same frame.
		// Use a different multiplier from FungalWhisperEvents (137) to avoid
		// clustering with the fungal whisper checks.
		int stagger = player.getId() * 191;
		// Coarse pre-check: only evaluate once every ~10 seconds
		if ((player.tickCount + stagger) % 200 != 0) return;

		HemoCapabilityAccess.getUnstainedProgress(serverPlayer).ifPresent(progress -> {
			if (!progress.hasBegunPurification()) return;

			float purity = progress.getPurity();
			boolean clarityUnlocked = progress.hasClarityUnlocked();
			float clarity = progress.getClarity();

			int interval;
			boolean useClarity;
			int stageLevel;

			if (clarityUnlocked) {
				// Phase 2: clarity whispers
				EnumClarityStage clarityStage = EnumClarityStage.byClarity(clarity);
				if (clarityStage.getLevel() < EnumClarityStage.DISCERNING.getLevel()) {
					// AWAKENED â€” clarity just unlocked, not yet discerning; no whispers yet
					return;
				}
				useClarity = true;
				stageLevel = clarityStage.getLevel();
				interval = switch (clarityStage) {
					case DISCERNING  -> DISCERNING_INTERVAL;
					case VIGILANT    -> VIGILANT_INTERVAL;
					case RESOLUTE    -> RESOLUTE_INTERVAL;
					case ENLIGHTENED -> ENLIGHTENED_INTERVAL;
					default          -> DISCERNING_INTERVAL;
				};
			} else {
				// Phase 1: purity whispers
				EnumPurityStage purityStage = EnumPurityStage.byPurity(purity);
				if (purityStage.getLevel() < EnumPurityStage.TAINTED.getLevel()) {
					// CORRUPTED â€” too early for whispers
					return;
				}
				useClarity = false;
				stageLevel = purityStage.getLevel();
				interval = switch (purityStage) {
					case TAINTED   -> TAINTED_INTERVAL;
					case CLEANSING -> CLEANSING_INTERVAL;
					case ABSOLVED  -> ABSOLVED_INTERVAL;
					case PURIFIED  -> PURIFIED_INTERVAL;
					default        -> TAINTED_INTERVAL;
				};
			}

			if ((player.tickCount + stagger) % interval != 0) return;

			int variant = (int) ((player.tickCount / interval) % VARIANT_COUNT);
			var memo = useClarity
					? OurLadyWhisperDialogueTrees.memoForClarityStage(stageLevel, variant)
					: OurLadyWhisperDialogueTrees.memoForPurityStage(stageLevel, variant);
			if (!OurLadyWhisperDialogueTrees.shouldOfferMemoWhisper(player, memo)) return;

			DialogueTree tree = useClarity
					? OurLadyWhisperDialogueTrees.forClarityStage(stageLevel, variant)
					: OurLadyWhisperDialogueTrees.forPurityStage(stageLevel, variant);

			PacketHandler.sendToPlayer(serverPlayer, new OpenDialoguePacket(tree));
		});
	}
}
