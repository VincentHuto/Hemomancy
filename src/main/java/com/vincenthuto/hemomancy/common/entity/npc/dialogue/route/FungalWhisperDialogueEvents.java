package com.vincenthuto.hemomancy.common.entity.npc.dialogue.route;

import com.vincenthuto.hemomancy.common.capability.HemoCapabilityAccess;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.EnumArchonPath;
import com.vincenthuto.hemomancy.common.capability.player.harbinger.degree.InitiatoryDegreeEvents;
import com.vincenthuto.hemomancy.common.encounter.FungalGardenTravelHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import com.vincenthuto.hemomancy.common.entity.npc.dialogue.*;

/**
 * The fungal whisper's dialogue events (no speaker): Communion, the Archon's choice and dismissal.
 */
final class FungalWhisperDialogueEvents {
	private FungalWhisperDialogueEvents() {
	}

	/** Handles the event when it is one of this NPC's; false lets the next route try. */
	public static boolean handle(DialogueEvent event) {
		ServerPlayer player = event.getPlayer();
		switch (event.getEventId()) {
		case "qliphoth_communion_done" -> {
			// Player completed the full Qliphoth Communion — nine pomes consumed.
			// This is a narrative milestone; the apotheos_rite path is now spiritually prepared.
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.qliphoth_communion_done")
							.withStyle(ChatFormatting.DARK_GREEN),
					false);
		}
		case "archon_choice_silence" -> {
			if (!player.getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)) return true;
			player.getPersistentData().remove(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING);
			// Archon chose to carry the truth in silence — they turn back from the Eighth Degree.
			var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
			degree.setArchonPath(EnumArchonPath.SILENT_PENDING);
			InitiatoryDegreeEvents.syncDegree(player, degree);
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.archon_choice_silence")
							.withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC),
					false);
		}
		case "archon_choice_eighth_degree" -> {
			if (!player.getPersistentData().getBoolean(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING)) return true;
			player.getPersistentData().remove(FungalGardenTravelHelper.REVELATION_CHOICE_PENDING);
			// Archon chose to pursue the Eighth Degree — the Apotheos path opens.
			var degree = HemoCapabilityAccess.getInitiatoryDegree(player);
			degree.setArchonPath(EnumArchonPath.APOTHEOS_PENDING);
			InitiatoryDegreeEvents.syncDegree(player, degree);
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.archon_choice_eighth_degree")
							.withStyle(ChatFormatting.DARK_RED, ChatFormatting.ITALIC),
					false);
		}
		case "whisper_dismiss" -> {
			// Player dismissed the whisper — no gameplay effect, just acknowledged
		}
		case "whisper_truth_acknowledged" -> {
			player.displayClientMessage(
					Component.translatable("hemomancy.dialogue.event.whisper_truth_acknowledged")
							.withStyle(ChatFormatting.DARK_GREEN),
					false);
		}
		default -> {
			return false;
		}
		}
		return true;
	}
}
